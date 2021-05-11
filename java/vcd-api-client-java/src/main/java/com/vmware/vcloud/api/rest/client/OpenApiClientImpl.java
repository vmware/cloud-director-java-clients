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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.ws.rs.Produces;
import javax.ws.rs.RedirectionException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.vmware.cxfrestclient.CxfClientSecurityContext;
import com.vmware.cxfrestclient.JaxRsClient;
import com.vmware.vcloud.api.rest.constants.RestConstants;
import com.vmware.vcloud.api.rest.constants.RestConstants.HttpStatusCodes;
import com.vmware.vcloud.api.rest.schema_v1_5.TaskType;
import com.vmware.vcloud.rest.openapi.api.EntityApi;
import com.vmware.vcloud.rest.openapi.api.SessionsApi;
import com.vmware.vcloud.rest.openapi.model.Error;
import com.vmware.vcloud.rest.openapi.model.Session;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.client.Client;
import org.apache.cxf.jaxrs.client.WebClient;

/**
 * An Open API client implementation for VCD that allows both JAX-RS and RESTful calls to VCD's
 * /cloudapi endpoints OpenApiClient instances should only be created through the
 * {@link ClientFactory} class: Do not create {@code OpenApiClientImpl} instances directly.
 *
 * OpeApiClient and VcdClient instances can share authentication and authorization context. See
 * {@link ClientFactory}
 */
public class OpenApiClientImpl extends AbstractVcdClientBase implements OpenApiClient {

    private final String acceptHeader;

    private static final String MODEL_CLASS_PACKAGE = "com.vmware.vcloud.rest.openapi.model.";

    private static final String USER_AGENT = "vcd-openapi-client";

    /**
     * Creates an instance of OpenApiClient by as dependent on the specified {@link VcdClientImpl}
     * <P>
     * This allows current authentication state to be inherited from the parent
     * {@code VcdClientImpl}
     *
     * @param vcdClient
     *            {@link VcdClientImpl} from which this {@link OpenApiClientImpl} has been
     *            inherited.
     */
    protected OpenApiClientImpl(VcdClientImpl vcdClient) {
        super(vcdClient.getOpenApiEndpoint(), vcdClient, vcdClient.getClientApiVersion(), USER_AGENT);
        this.setUserSecurityContext(vcdClient.getUserSecurityContext());
        try {
            if (vcdClient.isSystemClient() && vcdClient.getCredentials() != null) {
                vcdClient.getUserSecurityContext()
                        .setClientCredentials(
                            new CredentialsWrapper(vcdClient.getCredentials()) {
                        @Override
                        public boolean isProvider() {
                            return true;
                        }
                    });
            }

        } catch (Exception e) {
            //Ignore Exceptions for backward compatibility

        }

        MediaType acceptHeaderType =
                new MediaType(MediaType.APPLICATION_JSON_TYPE.getType(),
                        MediaType.APPLICATION_JSON_TYPE.getSubtype(),
                        Collections.singletonMap("version", vcdClient.getClientApiVersion()));
        acceptHeader = acceptHeaderType.toString();
    }

    OpenApiClientImpl(URI openApiEndpoint, String apiVersion,
            final CxfClientSecurityContext cxfClientSecurityContext,
            final ClientCredentials credentials, final boolean isProvider) {
        this(openApiEndpoint, apiVersion, cxfClientSecurityContext);
        setCredentialsInternal(isProvider ? new CredentialsWrapper(credentials) {
            @Override
            public boolean isProvider() {
                return true;
            }
        } : credentials);
        doInitClient();
    }

    OpenApiClientImpl(URI openApiEndpoint, String apiVersion,
            CxfClientSecurityContext cxfClientSecurityContext, String jwtToken) {
        this(openApiEndpoint, apiVersion, cxfClientSecurityContext);
        setJwtToken(jwtToken);
    }


    OpenApiClientImpl(URI openApiEndpoint, String apiVersion,
            final CxfClientSecurityContext cxfClientSecurityContext) {
        super(openApiEndpoint, cxfClientSecurityContext, apiVersion, USER_AGENT);
        final MediaType acceptHeaderType = new MediaType(MediaType.APPLICATION_JSON_TYPE.getType(),
                MediaType.APPLICATION_JSON_TYPE.getSubtype(),
                Collections.singletonMap("version", apiVersion));
        acceptHeader = acceptHeaderType.toString();
    }

    @Override
    protected List<?> getCxfProviders() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(DATE_FORMAT);
        mapper.setSerializationInclusion(Include.NON_NULL);
        final SimpleFilterProvider filterProvider = new SimpleFilterProvider();
        filterProvider.addFilter("vcloud", SimpleBeanPropertyFilter.serializeAll());
        mapper.setFilterProvider(filterProvider);
        final JacksonJsonProvider provider = new JacksonJsonProvider(mapper);
        return Collections.singletonList(provider);
    }


    @Override
    protected String[] getAcceptHeaders() {
        if (StringUtils.isNotBlank(getMultisiteLocations())) {

            return new String[] {
                    acceptHeader + MessageFormat.format(MULTISITE_ACCEPT_HEADER_FORMAT, getMultisiteLocations()) };
        }

        return new String[] { acceptHeader };
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
        final EntityApi proxy = this.createProxy(EntityApi.class);

        proxy.resolveEntity(urn);

        final Response entityResponse = this.getLastResponse(proxy);
        final List<?> objectTypeHeader = entityResponse.getHeaders().get(RestConstants.RESOLVED_OBJECT_TYPE_HEADER);

        if (objectTypeHeader == null || objectTypeHeader.isEmpty()) {
            return null;
        }

        final String header = (String)objectTypeHeader.get(0);

        final Class<?> clazz;
        try {
            clazz = Class.forName(MODEL_CLASS_PACKAGE + header);
        }  catch (ClassNotFoundException ite) {
            throw new AssertionError(ite);
        }

        return entityResponse.readEntity(clazz);
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
        private final OpenApiClientImpl parentClient;
        private final Class<JaxRsApi> api;
        private final AtomicReference<JaxRsApi> apiProxy = new AtomicReference<>(null);
        private final AtomicReference<Response> response = new AtomicReference<>(null);
        private final AtomicReference<Error> error = new AtomicReference<>(null);

        SingleUseProxyInvoker(Class<JaxRsApi> api, OpenApiClientImpl parentClient) {
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

            computeAcceptHeaders(webClient, method);

            try {
                parentClient.preInvocation(method, webClient, args);

                final Object result = method.invoke(apiProxy, args);

                final Response r = webClient.getResponse();

                // See {@code AbstractCxfClient#adjustConfiguration}
                webClient.getResponse().bufferEntity();

                response.set(r);
                error.set(null);
                return result;
            } catch (InvocationTargetException ite) {
                Throwable cause = ite.getCause();
                if (cause instanceof IllegalStateException) {
                    final Response r = webClient.getResponse();
                    // See {@code AbstractCxfClient#adjustConfiguration}
                    webClient.getResponse().bufferEntity();

                    if (r.getStatus() == 202) {
                        response.set(r);
                        error.set(null);
                        return null;
                    }
                }
                if (!(cause instanceof WebApplicationException)) {
                    throw cause;
                }

                final WebApplicationException wae = (WebApplicationException) cause;
                final Response r = wae.getResponse();
                // See {@code AbstractCxfClient#adjustConfiguration}
                webClient.getResponse().bufferEntity();

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
                parentClient.postInvocation(method, response.get());
            }
        }

        /**
         * Compute the proper set of {@code accept} headers for this invocation of the method.
         *
         * @param client
         *            The {@link Client} to set the headers on
         * @param method
         *            The {@link Method} about to be invoked
         */
        private void computeAcceptHeaders(final Client client, final Method method) {

            /*
             * If they've already been set, presumably by another caller
             * explicitly setting them, then leave them alone.
             */
            final MultivaluedMap<String, String> headers = client.getHeaders();
            final List<String> acceptHeaders = headers.get(HttpHeaders.ACCEPT);
            if (acceptHeaders != null && !acceptHeaders.isEmpty()) {
                return;
            }

            /*
             * Here we know that there have been no explicit headers set, and that we are
             * about to invoke a method. So let's compute a valid set.
             */
            final List<MediaType> mediaTypes = computeMediaTypePermutations(method);

            client.accept(mediaTypes.toArray(new MediaType[0]));
        }

        private List<MediaType> computeMediaTypePermutations(final Method method) {

            // Get all media types defined in the parent Client
            final List<MediaType> parentMediaTypes = Arrays.stream(parentClient.getAcceptHeaders())
                                                           .map(MediaType::valueOf)
                                                           .collect(Collectors.toList());

            // Get all media types defined in the method's annotation
            final Produces produces = method.getAnnotation(Produces.class);
            final List<MediaType> methodMediaTypes;
            if (produces != null) {
                methodMediaTypes = Arrays.stream(produces.value())
                                         .map(MediaType::valueOf)
                                         .collect(Collectors.toList());
            } else {
                // If the method has no defined media types, accept what the parent client does
                methodMediaTypes = parentMediaTypes;
            }

            /*
             * Create the valid set of media types by taking the content types the method produces
             * and combining them with any parameters (multisite, version, etc.) specified in the
             * parent's client.
             */
            final List<MediaType> mediaTypes = new ArrayList<>();
            parentMediaTypes.stream().forEach(pmt -> {
                methodMediaTypes.stream().forEach(mmt -> {
                    mediaTypes.add(new MediaType(mmt.getType(), mmt.getSubtype(), pmt.getParameters()));
                });
            });

            return mediaTypes;
        }

        @Override
        protected void setAuthenticationHeaders(Client client) {
            parentClient.setAuthenticationHeaders(client);
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

    }

    private void preInvocation(final Method m, final Client webClient, final Object[] args) {
        if (m.getName().equals("login") || m.getName().equals("providerLogin")) {
            final String authHeaderArg = String.valueOf(args[0]);
            final String credentialsHeaderVal =
                    getCredentials() != null ? getCredentials().getHeaderValue() : null;
            if (!Objects.equals(authHeaderArg, credentialsHeaderVal)) {
                /* We enter this block when a user directly invokes
                 * SessionsApi#login or SessionApi#providerLogin with a credential
                 * that differs from the one used to create the OpenApiClient.
                 * We need to build and save the new ClientCredentials instance
                 */
                clearSessionData();
                setCredentialsInternal(
                        buildCredentials(authHeaderArg, m.getName().equals("providerLogin")));
            }
            webClient.header(AUTHORIZATION_HEADER, args[0]);
        }
    }

    private void postInvocation(final Method m, final Response r) {
        if ((m.getName().equals("login") || m.getName().equals("providerLogin"))
                && (r != null && r.getStatus() == HttpStatusCodes.SC_OK)) {
            processHeaders(r.getHeaders());
        } else if (m.getName().contentEquals("logout")) {
            clearSessionData();
        }
    }

    @Override
    public void doInitClient() {
        final ClientCredentials creds = getCredentials();
        final SessionsApi sessionsApi = createProxy(SessionsApi.class);
        if (creds == null) {
            return;
        }
        if (creds.isProvider()) {
            sessionsApi.providerLogin(creds.getHeaderValue());
        } else {
            sessionsApi.login(creds.getHeaderValue());
        }
    }

    @Override
    public void reLogin() {
        if (getCredentials() == null) {
            throw new RuntimeException("Expected client credentials to not be null");
        }
        doInitClient();
    }

    @Override
    public void logout() {
        final SessionsApi sessionsApi = createProxy(SessionsApi.class);
        final Session session = sessionsApi.getCurrentSessionForAuthCredential();
        sessionsApi.logout(session.getId());
    }

    private ClientCredentials buildCredentials(final String headerValue, final boolean isProviderCredential) {
        return new ClientCredentials() {

            @Override
            public String getHeaderName() {
                return AUTHORIZATION_HEADER;
            }

            @Override
            public String getHeaderValue() {
                return headerValue;
            }

            @Override
            public boolean supportsSessionless() {
                return false;
            }

            @Override
            public boolean isProvider() {
                return isProviderCredential;
            }

        };
    }
}

