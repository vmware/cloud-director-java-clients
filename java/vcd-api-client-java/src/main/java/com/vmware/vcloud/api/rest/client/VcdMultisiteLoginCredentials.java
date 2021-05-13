
package com.vmware.vcloud.api.rest.client;

/*-
 * #%L
 * vcd-api-client-java :: vCloud Director REST Client
 * %%
 * Copyright (C) 2018 - 2021 VMware
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import java.io.IOException;
import java.io.StringReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

import javax.ws.rs.ProcessingException;

import com.vmware.vcloud.api.rest.client.filters.MultisiteAuthorizationFilter;
import com.vmware.vcloud.api.rest.version.ApiVersion;

import org.apache.cxf.message.Message;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import static com.vmware.vcloud.api.rest.client.VcdMultisiteSignatureFormatConstants.AUTH_TYPE;
import static com.vmware.vcloud.api.rest.client.VcdMultisiteSignatureFormatConstants.MULTISITE_BASIC_AUTH_USERORG_TEMPLATE;
import static com.vmware.vcloud.api.rest.client.VcdMultisiteSignatureFormatConstants.MULTISITE_UNVERSIONED_HEADER_TEMPLATE;
import static com.vmware.vcloud.api.rest.client.VcdMultisiteSignatureFormatConstants.MULTISITE_VERSIONED_HEADER_TEMPLATE;
import static com.vmware.vcloud.api.rest.client.VcdMultisiteSignatureFormatConstants.SIGNATURE_ALGORITHM;
import static com.vmware.vcloud.api.rest.client.VcdMultisiteSignatureFormatConstants.V1_DIGEST_ALG;
import static com.vmware.vcloud.api.rest.client.VcdMultisiteSignatureFormatConstants.V1_SIGNING_STRING_TEMPLATE;
import static com.vmware.vcloud.api.rest.client.VcdMultisiteSignatureFormatConstants.V2_SIGNING_STRING_TEMPLATE;

/**
 *
 * Certificate based credentials for signing the body of a multisite request. Unlike most
 * {@link ClientCredentials}, the credentials here are inserted by the
 * {@link MultisiteAuthorizationFilter} when we also know the body of the message. Signature
 * string created in partial compliance with
 * https://tools.ietf.org/html/draft-cavage-http-signatures-06
 *
 * Steps for signature:\n
 *
 * 1. During creation of the request {@link #getHeaderValue()} is used to add
 * {@value VcdMultisiteLoginCredentials#AUTH_TYPE} to the {@code Authorization} header.\n
 *
 * 2. When sending the request, the {@link MultisiteAuthorizationFilter} intercepts the request.
 *
 * 3. The authorization header is crafted using {@link #createMultisiteAuthorizationHeader(String, String, String, String, byte[])
 *  and inserted by the filter.
 *
 * @since 8.22
 */
public class VcdMultisiteLoginCredentials implements ClientCredentials {



    private final String delegateCredentials;
    private final PrivateKey privateKey;
    private final UUID localSiteId;
    private final UUID localOrgId;
    private final ApiVersion apiVersion;

    /**
     * Construct the multisite credentials with the key to sign with and basic credentials of the
     * user the multisite request is made on behalf of
     *
     * @param localSiteId
     *            local site UUID
     * @param localOrgId
     *            local Org UUID
     * @param userName
     *            UserName to login with
     * @param orgName
     *            org to log into
     * @param privateKey
     *            Private key to sign the contents with
     * @throws IOException
     */
    public VcdMultisiteLoginCredentials(final UUID localSiteId, final UUID localOrgId,
            final String userName, final String orgName, final String pemEncodedKey,
            final ApiVersion apiVersion)
            throws IOException {
        this.delegateCredentials = MessageFormat.format(MULTISITE_BASIC_AUTH_USERORG_TEMPLATE,
                Objects.requireNonNull(userName, "username id is required"),
                Objects.requireNonNull(orgName, "org name is required"));
        this.privateKey = getPrivateKeyFromPemEncoding(Objects.requireNonNull(pemEncodedKey, "private key is required"));
        this.localSiteId = Objects.requireNonNull(localSiteId, "local site id is required");
        this.localOrgId = Objects.requireNonNull(localOrgId, "local org id is required");
        this.apiVersion = apiVersion;
    }

    @Override
    public String getHeaderName() {
        return "Authorization";
    }

    @Override
    public String getHeaderValue() {
        /*
         * Simply specify the authentication type for now, to be replaced by the real authentication
         * header in the filter when we have access to the message body.
         */
        return AUTH_TYPE;
    }

    @Override
    public boolean supportsSessionless() {
        return true;
    }

    /**
     * Create the multisite authorization header from the provided information
     *
     * @param date
     *            Current date in RFC_1123_DATE_TIME format
     * @param method
     *            Rest method
     * @param path
     *            path of the request (e.g. '/cloud/org')
     * @param host
     *            Destination of the request
     * @param contentBytes
     *            Byte array of the content
     * @return Authorization header string
     */
    public String createMultisiteAuthorizationHeader(final String date, final String method,
            final String path, final String contentType) {
        try {
            final String signingString;
            if (apiVersion.isAtMost(ApiVersion.VERSION_30_0)) {
                signingString = constructV1SignatureString(date, method, path);
            } else {
                signingString =
                        MessageFormat.format(V2_SIGNING_STRING_TEMPLATE, method, path, date,
                                contentType);
            }
            final String signature = signMessage(signingString);

            if (apiVersion.isAtMost(ApiVersion.VERSION_30_0)) {
                return MessageFormat.format(MULTISITE_UNVERSIONED_HEADER_TEMPLATE, AUTH_TYPE,
                        localSiteId, localOrgId, signature,
                        delegateCredentials);
            } else {
                return MessageFormat.format(MULTISITE_VERSIONED_HEADER_TEMPLATE, AUTH_TYPE,
                        MultisiteSignatureVersion.VERSION_2_0.value(), localOrgId, localSiteId,
                        signature, delegateCredentials);
            }

        } catch (final Exception e) {
            throw new ProcessingException(e);
        }
    }

    private String signMessage(final String signingString) throws SignatureException {

        try {
            Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
            sig.initSign(privateKey);
            sig.update(signingString.getBytes());
            return Base64.getEncoder().encodeToString(sig.sign());
        } catch (Exception e) {
            throw new SignatureException(e);
        }
    }

    private String constructV1SignatureString(final String date, final String method,
            final String path)
            throws NoSuchAlgorithmException, IOException {

        // These values are kept for backwards compatibility purposes
        final String digest = createDigest(new byte[0]);
        final int contentLength = new byte[0].length;

        //Construct the string of details to sign
        final String signingString =
                MessageFormat.format(V1_SIGNING_STRING_TEMPLATE, method, path, date, digest,
                        contentLength);
        return signingString;
    }

    /**
     * Creates a digest of the message contents using the specified {@code DIGEST_ALG}
     *
     * @param message
     *            {@link Message} to get the content from
     * @return Base64 encoded digest
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    private String createDigest(final byte[] contentBytes) throws NoSuchAlgorithmException,
            IOException {
        final MessageDigest md = MessageDigest.getInstance(V1_DIGEST_ALG);
        md.update(contentBytes);
        final byte[] digestBytes = md.digest();
        return Base64.getEncoder().encodeToString(digestBytes);
    }

    /**
     * Converts a PEM encoded string representation of a private key to a {@link PrivateKey}
     *
     * @param pemString
     *            Pem encoded string
     * @return {@link PrivateKey}
     * @throws IOException
     */
    private PrivateKey getPrivateKeyFromPemEncoding(String pemString) throws IOException {

        if (pemString != null) {
            try (final StringReader reader = new StringReader(pemString);
                    final PEMParser pemParser = new PEMParser(reader)) {
                final Object object = pemParser.readObject();
                if (object instanceof PrivateKeyInfo) {
                    final JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
                    return converter.getPrivateKey((PrivateKeyInfo) object);
                }
                if (object instanceof PEMKeyPair) {
                    final JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
                    return converter.getKeyPair((PEMKeyPair) object).getPrivate();
                }
            }
        }
        throw new IllegalArgumentException("Supplied key string is not a valid PEM encoded private key");
    }

}


