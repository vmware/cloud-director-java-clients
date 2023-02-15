
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


import java.net.URI;
import java.util.UUID;

import com.vmware.cxfrestclient.AbstractCxfRestClient;
import com.vmware.cxfrestclient.CxfClientSecurityContext;
import com.vmware.vcloud.api.rest.client.VcdClient.ClientRequestIdProvider;
import com.vmware.vcloud.api.rest.constants.RestConstants;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.client.Client;

/**
 * Base class for all clients that can interact with VCD
 * <P>
 * Provides common request header management functionality
 */
abstract class AbstractVcdClientBase extends AbstractCxfRestClient {

    private ClientRequestIdProvider clientRequestIdProvider =
            () -> UUID.randomUUID().toString();

    protected AbstractVcdClientBase(URI endpoint) {
        super(endpoint);
    }

    protected AbstractVcdClientBase(URI endpoint, CxfClientSecurityContext cxfClientSecurityContext) {
        super(endpoint, cxfClientSecurityContext);
    }

    protected AbstractVcdClientBase(AbstractCxfRestClient client) {
        super(client);
    }

    /**
     * Configure authentication headers on the supplied clients
     * <P>
     * May possibly set cookies too, if applicable.
     *
     * @param client
     *            {@link Client} to set authentication headers on.
     */
    protected abstract void setAuthenticationHeaders(final org.apache.cxf.jaxrs.client.Client client);

    /**
     * Accept headers that must be included with the request.
     *
     * @return Appropriately formatted accept values in String format.
     */
    protected abstract String[] getAcceptHeaders();

    /**
     * Get the value to be sent in the X-VMWARE-VCLOUD-TENANT-CONTEXT header
     */
    protected String getOrgContextHeader() {
        return null;
    }

    public AbstractVcdClientBase(URI endpoint, AbstractCxfRestClient client) {
        super(endpoint, client);
    }

    protected ClientRequestIdProvider getClientRequestIdProvider() {
        return clientRequestIdProvider;
    }

    @Override
    protected final void configureHttpRequestHeaders(final org.apache.cxf.jaxrs.client.Client client) {
        client.accept(getAcceptHeaders());

        setAuthenticationHeaders(client);

        if (clientRequestIdProvider != null) {
            final String clientRequestId = clientRequestIdProvider.getClientRequestId();
            if (!StringUtils.isEmpty(clientRequestId)) {
                client.header(RestConstants.VCLOUD_CLIENT_REQUEST_ID_HEADER, clientRequestId);
            }
        }
    }

    protected void setClientRequestIdProvider(ClientRequestIdProvider clientRequestIdGenerator) {
        this.clientRequestIdProvider = clientRequestIdGenerator;
    }
}


