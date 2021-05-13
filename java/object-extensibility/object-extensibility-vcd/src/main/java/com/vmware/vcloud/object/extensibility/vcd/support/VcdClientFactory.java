package com.vmware.vcloud.object.extensibility.vcd.support;

/*-
 * #%L
 * object-extensibility-vcd :: Object Extension vCD client
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

import java.net.HttpURLConnection;
import java.net.URI;

import javax.ws.rs.WebApplicationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.cxfrestclient.JaxRsClient;
import com.vmware.cxfrestclient.JaxRsClient.ErrorHandler;
import com.vmware.vcloud.api.rest.client.ClientCredentials;
import com.vmware.vcloud.api.rest.client.VcdClient;
import com.vmware.vcloud.api.rest.client.VcdClientImpl;
import com.vmware.vcloud.object.extensibility.vcd.ApiConnectionInfo;

/**
 * Utility class for creating {@link VcdClient} instances.
 */
public final class VcdClientFactory {
    private static final Logger LOG = LoggerFactory.getLogger(VcdClientFactory.class);

    private VcdClientFactory() {}

    /**
     * Creates a {@link VcdClient} instance that can connect to a specific version of the vCloud Director at the
     * specified endpoint. <p>
     *
     * The client instance will use the provided credentials to immediately perform a login.  A single attempt
     * will be made to re-authenticate the {@link VcdClient} instance with the specified credentials in the event
     * that an {@link HttpURLConnection#HTTP_FORBIDDEN} status is received, which may indicate a session timeout.
     *
     * @param connectionInfo details about the vCloud Director API to connect to
     * @param credentials user credentials for API access
     * @return a new, authenticated {@link VcdClient} instance
     */
    public static final VcdClient createVcdClient(final ApiConnectionInfo connectionInfo, final ClientCredentials credentials) {
        final VcdClient vcdClient = new VcdClientImpl(connectionInfo.getEndpoint(), connectionInfo.getVersion(), connectionInfo.getSecurityContext());
        vcdClient.setCredentials(credentials);
        vcdClient.setErrorHandler(new ErrorHandler() {
            @Override
            public Disposition handleError(final JaxRsClient client, final URI ref, final WebApplicationException e, final int failureCount) {
                if (failureCount > 1 ||
                        (e.getResponse().getStatus() != HttpURLConnection.HTTP_UNAUTHORIZED &&
                         e.getResponse().getStatus() != HttpURLConnection.HTTP_FORBIDDEN)) {
                    return Disposition.FAIL;
                }

                LOG.debug("Re-establishing vCloud Director session for client {}", client);
                try {
                    ((VcdClient) client).setCredentials(credentials);
                } catch (final Exception exception) {
                    LOG.error("Failed to re-establish the vCloud Director session for client {}", client, exception);
                    return Disposition.FAIL;
                }

                return Disposition.RETRY;
            }
        });

        return vcdClient;
    }
}

