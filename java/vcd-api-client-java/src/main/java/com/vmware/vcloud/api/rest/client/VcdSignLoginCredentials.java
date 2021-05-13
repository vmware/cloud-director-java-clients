
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Signature;
import java.text.MessageFormat;
import java.util.zip.GZIPOutputStream;

import org.apache.cxf.common.util.Base64Utility;

/**
 * Saml Token/Org-name credentials suitable for use in authenticating with a vCD server using the
 * vCloud API
 *
 * @since 5.6
 */
public class VcdSignLoginCredentials implements ClientCredentials {
    private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

    private static final String TOKEN_SIGNATURE_ALGORITHM = "SHA256withRSA";

    private final String signAuthenticationHeader;

    /**
     * Construct a {@link ClientCredentials} object to generate vCD custom format SIGN
     * Authentication header for login message to login into the specified org using the provided
     * bearer token.
     *
     * @param samlBearerTokenXml
     *            Bearer Token in xml format to be used to login.
     * @param org
     *            The organization to log into using the specified token.
     */
    public VcdSignLoginCredentials(final String samlBearerTokenXml, final String org) {
        final String encodedToken = encodeToken(samlBearerTokenXml);
        signAuthenticationHeader =
                MessageFormat.format("SIGN token=\"{0}\",org=\"{1}\"", encodedToken, org);
    }

    /**
     * Construct a {@link ClientCredentials} object to generate vCD custom format SIGN
     * Authentication header for login message to login into the specified org using the provided
     * Holder-of-Key token and its signature generated using the specified private key.
     *
     * @param samlHokTokenXml
     *            Holder-of-Key Token in xml format to be used to login.
     * @param org
     *            The organization to log into using the specified token.
     * @param privateKey
     *            {@link PrivateKey} to sign the token with. Signature is generated using
     *            {@value #TOKEN_SIGNATURE_ALGORITHM} algorithm.
     * @throws GeneralSecurityException
     *             if there is a problem generating the signature.
     */
    public VcdSignLoginCredentials(final String samlHokTokenXml, final String org, final PrivateKey privateKey)
            throws GeneralSecurityException {
        final String encodedToken = encodeToken(samlHokTokenXml);
        final String signature = signToken(samlHokTokenXml, privateKey);
        signAuthenticationHeader = MessageFormat.format("SIGN token=\"{0}\",org=\"{1}\",signature=\"{2}\",signature_alg=\"{3}\"",
                encodedToken, org, signature, TOKEN_SIGNATURE_ALGORITHM);
    }

    private String encodeToken(final String samlTokenXml) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            final GZIPOutputStream gzipOutputStream = new GZIPOutputStream(baos);
            gzipOutputStream.write(samlTokenXml.getBytes(UTF8_CHARSET));
            gzipOutputStream.flush();
            gzipOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException("Error zipping saml token!", e);
        }

        final byte[] encodedTokenBytes = baos.toByteArray();
        final String encodedToken = Base64Utility.encode(encodedTokenBytes);

        return encodedToken;
    }

    private String signToken(final String samlTokenXml, final PrivateKey privateKey) throws GeneralSecurityException {
        final Signature signer = Signature.getInstance(TOKEN_SIGNATURE_ALGORITHM);
        signer.initSign(privateKey);
        signer.update(samlTokenXml.getBytes(UTF8_CHARSET));

        final byte[] signatureBytes = signer.sign();
        final String signature = Base64Utility.encode(signatureBytes);

        return signature;
    }

    @Override
    public String getHeaderValue() {
        return signAuthenticationHeader;
    }

    @Override
    public String getHeaderName() {
        return "Authorization";
    }

    @Override
    public boolean supportsSessionless() {
        return true;
    }


}


