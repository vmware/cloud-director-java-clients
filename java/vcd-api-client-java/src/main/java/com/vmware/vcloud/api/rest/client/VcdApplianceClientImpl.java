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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.ws.rs.RedirectionException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.core.UriBuilder;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.vmware.cxfrestclient.CxfClientSecurityContext;
import com.vmware.cxfrestclient.JaxRsClient;
import com.vmware.vcloud.api.rest.schema_v1_5.TaskType;
import com.vmware.vcloud.rest.openapi.model.Error;

import org.apache.cxf.jaxrs.client.Client;
import org.apache.cxf.jaxrs.client.WebClient;

/**
 * An Open API client implementation for VCD Appliance that allows both JAX-RS and RESTful calls to VCD
 * Appliance's endpoints. VcdApplianceApiClient instances can be created directly, instead of only through the
 * {@link ClientFactory} class.
 */
public final class VcdApplianceClientImpl extends AbstractVcdClientBase implements OpenApiClient {
    private static final String ACCEPT_HEADER = MediaType.APPLICATION_JSON;

    private static final String USER_AGENT = "vcd-appliance";

    private static final String BASE_PATH = "/";

    private static URI appendBasePath(URI baseUri) {
        return UriBuilder.fromUri(baseUri).path(BASE_PATH).build();
    }

    public VcdApplianceClientImpl(URI openApiEndpoint,
            final CxfClientSecurityContext cxfClientSecurityContext,
            final ClientCredentials credentials) {
        super(appendBasePath(openApiEndpoint), cxfClientSecurityContext, null, USER_AGENT);

        setCredentialsInternal(credentials);
    }

    @Override
    protected List<?> getCxfProviders() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        final SimpleFilterProvider filterProvider = new SimpleFilterProvider();
        filterProvider.addFilter("vcloud", SimpleBeanPropertyFilter.serializeAll());
        mapper.setFilterProvider(filterProvider);
        final JacksonJsonProvider provider = new JacksonJsonProvider(mapper);
        return Collections.singletonList(provider);
    }


    @Override
    protected String[] getAcceptHeaders() {
        return new String[] { ACCEPT_HEADER };
    }

    /**
     * Return a custom proxy that ensures that a new proxy object is generated for each call.
     * <P>
     * This ensures that when underlying {@link Client} is utilized for a particular call, custom
     * settings for that call are reset before the next call. {@link JaxRsClient#createProxy(Class)
     * super.createProxy(Class)} is invoked to create proxy objects as needed.
     * <P>
     * In case of an error, the client can parse the error message returned from vCD and include it
     * in the {@link WebApplicationException} that will be thrown.
     *
     * @param jaxrsApiInterface
     *            {@link Class} for a open api generated interface
     *
     * @return a {@link Proxy} for the specified interface as described above.
     */
    @Override
    public <JaxRsClass> JaxRsClass createProxy(Class<JaxRsClass> jaxrsApiInterface) {
        if (!jaxrsApiInterface.isInterface()) {
            throw new IllegalArgumentException(String.format("Class %s to proxy must be an interface",
                    jaxrsApiInterface.getName()));
        }
        @SuppressWarnings("unchecked")
        final JaxRsClass proxiedProxy =
            (JaxRsClass) Proxy.newProxyInstance(jaxrsApiInterface.getClassLoader(), new Class[] { jaxrsApiInterface },
                    new SingleUseProxyInvoker<>(jaxrsApiInterface, this));
        return proxiedProxy;
    }

    @Override
    public <JaxRsClass> Client getWebClientForNextCall(JaxRsClass proxy) {
        return getInvocationHandler(proxy).getNextCallClient();
    }

    @Override
    public <JaxRsClass> Response getLastResponse(JaxRsClass proxy) {
        return getInvocationHandler(proxy).getResponse();
    }

    @Override
    public <JaxRsClass> Error getLastVcdError(JaxRsClass proxy) {
        return getInvocationHandler(proxy).getError();
    }

    @Override
    public <JaxRsClass> StatusType getLastStatus(JaxRsClass proxy) {
        return getLastResponse(proxy).getStatusInfo();
    }

    @Override
    public <JaxRsClass> URI getLastTaskUri(JaxRsClass proxy) {
        final Response lastResponse = getLastResponse(proxy);
        if (lastResponse == null) {
            return null;
        }
        return (Status.ACCEPTED.getStatusCode() == lastResponse.getStatusInfo().getStatusCode())
                ? lastResponse.getLocation()
                : null;
    }

    @Override
    public<JaxRsClass> TaskType getLastTask(JaxRsClass proxy) {
        final URI lastTaskUri = getLastTaskUri(proxy);
        return (lastTaskUri == null) ? null
                : ClientFactory.createVcdClient(this).getResource(lastTaskUri, TaskType.class);
    }

    @Override
    public <JaxRsClass> Set<Link> getLastLinks(JaxRsClass proxy) {
        return getLastResponse(proxy).getLinks();
    }

    @Override
    public <JaxRsClass> String getLastContentType(JaxRsClass proxy) {
        return getLastResponse(proxy).getHeaderString(HttpHeaders.CONTENT_TYPE);
    }

    @Override
    public Object resolveEntity(String urn) {
        return null;
    }

    private <JaxRsClass> SingleUseProxyInvoker<JaxRsClass> getInvocationHandler(JaxRsClass proxy) {
        @SuppressWarnings("unchecked")
        final SingleUseProxyInvoker<JaxRsClass> invocationHandler =
                (SingleUseProxyInvoker<JaxRsClass>) Proxy.getInvocationHandler(proxy);
        return invocationHandler;
    }


    private static final class SingleUseProxyInvoker<JaxRsApi>
            extends AbstractVcdClientBase
    implements InvocationHandler {
        private final VcdApplianceClientImpl parentClient;
        private final Class<JaxRsApi> api;
        private final AtomicReference<JaxRsApi> apiProxy = new AtomicReference<>(null);
        private final AtomicReference<Response> response = new AtomicReference<>(null);
        private final AtomicReference<Error> error = new AtomicReference<>(null);

        SingleUseProxyInvoker(Class<JaxRsApi> api, VcdApplianceClientImpl parentClient) {
            super(parentClient, parentClient.getClientApiVersion(), USER_AGENT);
            this.parentClient = parentClient;
            this.api = api;
            resetWebClientProxy();
        }

        private void resetWebClientProxy() {
            apiProxy.set(null);
        }

        private JaxRsApi getCurrentClientProxy() {
            apiProxy.compareAndSet(null, createProxy(api));
            return apiProxy.get();
        }

        Client getNextCallClient() {
            return WebClient.client(getCurrentClientProxy());
        }

        Response getResponse() {
            return response.get();
        }

        Error getError() {
            return error.get();
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            final JaxRsApi apiProxy = getCurrentClientProxy();
            final Client webClient = getNextCallClient();

            try {
                final Object result = method.invoke(apiProxy, args);

                final Response r = webClient.getResponse();
                response.set(r);
                error.set(null);
                return result;
            } catch (InvocationTargetException ite) {
                Throwable cause = ite.getCause();
                if (!(cause instanceof WebApplicationException)) {
                    throw cause;
                }

                final WebApplicationException wae = (WebApplicationException) cause;
                final Response r = wae.getResponse();

                response.set(r);

                final Constructor<? extends WebApplicationException> constructor =
                        wae.getClass().getConstructor(String.class, Response.class);

                final WebApplicationException newException;
                if (!(cause instanceof RedirectionException)) {
                    final Error e = r.readEntity(Error.class);
                    error.set(e);
                    newException = constructor.newInstance(e.getMessage(), r);
                } else {
                    newException = constructor.newInstance(null, r);
                }

                newException.setStackTrace(wae.getStackTrace());

                throw newException;
            } finally {
                resetWebClientProxy();
            }
        }

        /**
         * {@inheritDoc}
         * <p>
         * This always returns an empty array because we don't want to interfere with whatever
         * {@code accept} headers the underlying {@link Client} may have had set. We will recompute
         * the {@code accept} headers right before invocation of the API call, if necessary.
         * <p>
         *
         * @see #invoke(Object, Method, Object[])
         * @see #computeAcceptHeaders(Client, Method)
         */
        @Override
        protected String[] getAcceptHeaders() {
            return new String[0];
        }

        @Override
        protected List<?> getCxfProviders() {
            return parentClient.getCxfProviders();
        }

        @Override
        protected void setAuthenticationHeaders(Client client) {
            parentClient.setAuthenticationHeaders(client);
        }
    }


    @Override
    public void doInitClient() {
    }

    @Override
    public void reLogin() {
    }

    @Override
    public void logout() {
    }
}

