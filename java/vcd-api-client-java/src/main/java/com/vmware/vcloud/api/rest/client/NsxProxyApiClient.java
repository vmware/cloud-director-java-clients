
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
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.vmware.vcloud.api.rest.client.filters.MultisiteAuthorizationFilter;

import org.apache.cxf.jaxrs.client.Client;
import org.apache.cxf.jaxrs.client.ClientConfiguration;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.http.HTTPConduit;

/**
 * A client to interact with VCD's NSX/networking proxy API.
 * <P>
 * An instance of this can only be acquired from {@link VcdClient#getOpenApiClient()} call and this
 * client's authentication is tied to the parent {@link VcdClient} from which this is instantiated
 */
public class NsxProxyApiClient extends AbstractVcdClientBase {

    private final VcdClientImpl parentVcdClient;
    private ClientCredentials clientCredentials;

    /**
     * Creates an instance of NsxProxyApiClient by as dependent on the specified {@link VcdClientImpl}
     * <P>
     * This allows current authentication state to be inherited from the parent
     * {@code VcdClientImpl}
     *
     * @param vcdClient {@link VcdClientImpl} from which this {@link NsxProxyApiClient} has been inherited.
     */
    NsxProxyApiClient(VcdClientImpl vcdClient) {
        super(vcdClient.getNetworkApiEndpoint(), vcdClient);
        this.parentVcdClient = vcdClient;
        this.clientCredentials = vcdClient.getCredentials();
    }

    @Override
    protected List<?> getCxfProviders() {
        final List<Object> providers = new ArrayList<>();
        if (clientCredentials instanceof VcdMultisiteLoginCredentials) {
            providers.add(new MultisiteAuthorizationFilter((VcdMultisiteLoginCredentials) clientCredentials));
        }
        providers.add(new XStreamXmlProvider());
        return providers;
    }

    @Override
    protected void setAuthenticationHeaders(Client client) {
        parentVcdClient.setAuthenticationHeaders(client);
    }

    @Override
    protected String[] getAcceptHeaders() {
        return new String[] { MediaType.APPLICATION_XML };
    }

    @Override
    protected void configureClient(Object client) {
        super.configureClient(client);
        increaseReceiveTimeout(WebClient.getConfig(client));
    }

    @Override
    protected String getOrgContextHeader() {
        return parentVcdClient.getOrgContextHeader();
    }

    private void increaseReceiveTimeout(ClientConfiguration config) {
        HTTPConduit httpConduit = (HTTPConduit) config.getConduit();
        // Default is 60 seconds.  We'll set it to 2 minutes since we're also depending on NSX calls, which also call VC/ESX
        // http://cxf.apache.org/docs/client-http-transport-including-ssl-support.html
        httpConduit.getClient().setReceiveTimeout(120000);
    }

    @Override
    public Response postResourceObject(URI uri, String type, Object contents) {
        return super.postResourceObject(uri, type, contents);
    }
}


