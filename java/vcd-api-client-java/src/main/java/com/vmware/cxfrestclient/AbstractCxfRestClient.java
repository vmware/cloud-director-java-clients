package com.vmware.cxfrestclient;

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

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;
import java.util.Objects;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NoContentException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;
import javax.xml.bind.JAXBElement;

import com.vmware.cxfrestclient.JaxRsClient.ErrorHandler.Disposition;
import com.vmware.vcloud.api.rest.constants.VCloudMediaTypes;
import com.vmware.vcloud.api.rest.schema_v1_5.ErrorType;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxrs.client.ClientConfiguration;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.utils.ExceptionUtils;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.http.HttpStatus;

/**
 * Base class for implementations of {@link JaxRsClient} that are based on CXF.
 */
public abstract class AbstractCxfRestClient implements JaxRsClient {

    private static final VCloudMediaTypes VCLOUD_MEDIA_TYPES = new VCloudMediaTypes();
    protected final URI endpoint;
    private ErrorHandler errorHandler;
    private final CxfClientSecurityContext cxfClientSecurityContext;

    protected AbstractCxfRestClient(URI endpoint) {
        this(endpoint, CxfClientSecurityContext.getDefaultCxfClientSecurityContext());
    }

    protected AbstractCxfRestClient(URI endpoint, final CxfClientSecurityContext cxfClientSecurityContext) {
        this.endpoint = endpoint;
        this.cxfClientSecurityContext = cxfClientSecurityContext;
    }

    protected AbstractCxfRestClient(AbstractCxfRestClient client) {
        this(client.endpoint, client.cxfClientSecurityContext);
    }

    protected AbstractCxfRestClient(URI endpoint, AbstractCxfRestClient client) {
        this(endpoint, client.cxfClientSecurityContext);
    }

    /**
     * @return a List of CXF providers for use with CXF proxies and {@link WebClient}s we create.
     */
    protected abstract List<?> getCxfProviders();

    /**
     * A method that must be implemented by derived classes to set any
     * HTTP request headers that the REST API implemented in such derived classes
     * require for proper operation.
     */
    protected abstract void configureHttpRequestHeaders(final org.apache.cxf.jaxrs.client.Client client);

    @Override
    public <JaxRsClass> JaxRsClass createProxy(final Class<JaxRsClass> jaxRsClass) {
        final JaxRsClass proxy = JAXRSClientFactory.create(endpoint.toString(), jaxRsClass, getCxfProviders(), true);
        configureClient(proxy);
        return proxy;
    }

    @Override
    public URI getEndpoint() {
        return endpoint;
    }

    protected boolean handleException(URI uri, WebApplicationException e, int failureCount) {
        return
            errorHandler != null &&
            errorHandler.handleError(this, uri, e, failureCount) == Disposition.RETRY;
    }

    @Override
    public Response getResource(URI uri) {
        int failureCount = 0;
        do {
            try {
                return createWebClient(uri).get();
            } catch (WebApplicationException e) {
                if (! handleException(uri, e, ++failureCount)) {
                    throw makeException(e);
                }
            }
        } while (true);
    }

    @Override
    public <ResourceClass> ResourceClass getResource(URI uri, Class<ResourceClass> resourceClass) {
        int failureCount = 0;
        do {
            try {
                final Response resource = createWebClient(uri).get();
                return parseResponseAsType(resource, resourceClass);
            } catch (WebApplicationException e) {
                if (! handleException(uri, e, ++failureCount)) {
                    throw makeException(e);
                }
            }
        } while (true);
    }

    private <ResourceClass> ResourceClass parseResponseAsType(final Response response,
                                                              final Class<ResourceClass> resourceClass) {
        Objects.requireNonNull(resourceClass);
        final StatusType statusInfo = response.getStatusInfo();
        if (statusInfo.getFamily().equals(Family.CLIENT_ERROR) || statusInfo.getFamily().equals(Family.SERVER_ERROR)) {
            throw extractException(response);
        }
        final MediaType contentType = response.getMediaType();
        final Class<?> matchingType;
        if (contentType == null) {
            matchingType = resourceClass;
        } else {
            if (StringUtils.startsWith(contentType.toString(), ErrorType.CONTENT_TYPE)) {
                throw extractException(response);
            } else {
                matchingType = ObjectUtils.firstNonNull(VCLOUD_MEDIA_TYPES.getClassFor(contentType), resourceClass);
            }
        }
        if (Void.class.equals(resourceClass)) {
            return null;
        }
        if (resourceClass.isAssignableFrom(Response.class)) {
            return resourceClass.cast(response);
        }
        if (response.getStatus() == HttpStatus.SC_NO_CONTENT) {
            return null;
        }

        try {
            if (resourceClass.isAssignableFrom(matchingType)) {
                return resourceClass.cast(response.readEntity(matchingType));
            } else {
                return response.readEntity(resourceClass);
            }
        } catch (ProcessingException pe) {
            if (pe.getCause() instanceof NoContentException) {
                return null;
            } else {
                throw pe;
            }
        }
    }

    @Override
    public <ContentsClass> Response putResource(URI uri, String type, JAXBElement<ContentsClass> contents) {
        int failureCount = 0;
        do {
            try {
                return createWebClient(uri, type).put(contents);
            } catch (WebApplicationException e) {
                if (! handleException(uri, e, ++failureCount)) {
                    throw makeException(e);
                }
            }
        } while (true);
    }

    @Override
    public <ContentsClass> void putResourceVoid(URI uri, String type, JAXBElement<ContentsClass> contents) {
        Response response = putResource(uri, type, contents);
        handleNoContentResponse(response);
    }

    protected void handleNoContentResponse(Response response) {
        if (response.getStatus() != HttpURLConnection.HTTP_NO_CONTENT) {
            throw makeException(extractException(response));
        }
    }

    private WebApplicationException extractException(Response r) {
        try {
            final Class<?> responseExceptionClass =
                    ExceptionUtils.getWebApplicationExceptionClass(r, WebApplicationException.class);
            final Constructor<?> ctr = responseExceptionClass.getConstructor(Response.class);
            return (WebApplicationException)ctr.newInstance(r);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            // We shouldn't get here. However, if we do, then just return a default WebApplicationException
            return new WebApplicationException(r);
        }
    }

    @Override
    public <ContentsClass, ResponseClass> ResponseClass putResource(URI uri, String type, JAXBElement<ContentsClass> contents, Class<ResponseClass> responseClass) {
        int failureCount = 0;
        do {
            try {
                final Response response = createWebClient(uri, type).put(contents);
                return parseResponseAsType(response, responseClass);
            } catch (WebApplicationException e) {
                if (! handleException(uri, e, ++failureCount)) {
                    throw makeException(e);
                }
            }
        } while (true);
    }

    @Override
    public Response putFile(
            URI uri,
            File file,
            String type) {
        int failureCount = 0;
        do {
            try {
                Response put = createWebClient(uri, type).put(file);
                return put;
            } catch (WebApplicationException e) {
                if (! handleException(uri, e, ++failureCount)) {
                    throw e;
                }
            }
        } while (true);
    }

    @Override
    public <ContentsClass> Response postResource(URI uri, String type, JAXBElement<ContentsClass> contents) {
        return postResourceObject(uri, type, contents);
    }

    @Override
    public <ContentsClass> void postResourceVoid(URI uri, String type, JAXBElement<ContentsClass> contents) {
        Response response = postResource(uri, type, contents);
        handleNoContentResponse(response);
    }

    @Override
    public <ContentsClass, ResponseClass> ResponseClass postResource(URI uri, String type, JAXBElement<ContentsClass> contents, Class<ResponseClass> responseClass) {
        int failureCount = 0;
        do {
            try {
                final Response response = createWebClient(uri, type).post(contents);
                return parseResponseAsType(response, responseClass);
            } catch (WebApplicationException e) {
                if (! handleException(uri, e, ++failureCount)) {
                    throw makeException(e);
                }
            }
        } while (true);
    }

    @Override
    public Response deleteResource(URI uri) {
        int failureCount = 0;
        do {
            try {
                return createWebClient(uri).delete();
            } catch (WebApplicationException e) {
                if (! handleException(uri, e, ++failureCount)) {
                    throw makeException(e);
                }
            }
        } while (true);
    }

    protected RuntimeException makeException(WebApplicationException exception) {
        throw exception;
    }

    @Override
    public void deleteResourceVoid(URI uri) {
        Response response = deleteResource(uri);
        handleNoContentResponse(response);
    }

    @Override
    public <ResponseClass> ResponseClass deleteResource(URI uri, Class<ResponseClass> responseClass) {
        int failureCount = 0;
        do {
            try {
                final Response response = createWebClient(uri).delete();
                return parseResponseAsType(response, responseClass);
            } catch (WebApplicationException e) {
                if (! handleException(uri, e, ++failureCount)) {
                    throw makeException(e);
                }
            }
        } while (true);
    }

    @Override
    public Response optionsResource(URI uri) {
        int failureCount = 0;
        do {
            try {
                return createWebClient(uri).invoke(HttpMethod.OPTIONS, null);
            } catch (WebApplicationException e) {
                if (! handleException(uri, e, ++failureCount)) {
                    throw makeException(e);
                }
            }
        } while (true);
    }

    @Override
    public WebClient createWebClient(URI uri) {
        return createWebClient(uri, null);
    }

    @Override
    public WebClient createWebClient(URI uri, String type) {
        WebClient client = WebClient.create(uri.toASCIIString(), getCxfProviders());
        if (type != null) {
            client.type(type);
        }
        configureClient(client);
        return client;
    }

    @Override
    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    private void configureHttpRequestHeaders(Object proxy) {
        configureHttpRequestHeaders(WebClient.client(proxy));
    }

    protected Response postResourceObject(URI uri, String type, Object contents) {
        int failureCount = 0;
        do {
            try {
                return createWebClient(uri, type).post(contents);
            } catch (WebApplicationException e) {
                if (! handleException(uri, e, ++failureCount)) {
                    throw makeException(e);
                }
            }
        } while (true);
    }

    /**
     * Set the appropriate ssl context and https hostname verification for this client
     */
    protected final void configureSSLTrustManager(ClientConfiguration config) {
        HTTPConduit httpConduit = (HTTPConduit) config.getConduit();

        final TLSClientParameters tlsParams = new TLSClientParameters();
        tlsParams.setUseHttpsURLConnectionDefaultSslSocketFactory(false);
        tlsParams.setSSLSocketFactory(cxfClientSecurityContext.getSSLSocketFactory());
        if (cxfClientSecurityContext.isHostnameVerificationEnabled()) {
            tlsParams.setUseHttpsURLConnectionDefaultHostnameVerifier(true);
        } else {
            tlsParams.setDisableCNCheck(true);
        }
        httpConduit.setTlsClientParameters(tlsParams);
    }

    private void addHttpChunking(ClientConfiguration config) {
        HTTPConduit httpConduit = (HTTPConduit) config.getConduit();
        httpConduit.getClient().setAllowChunking(true);
    }

    private void increaseHttpClientConnectionTimeout(ClientConfiguration config) {
        HTTPConduit httpConduit = (HTTPConduit) config.getConduit();
        httpConduit.getClient().setConnectionTimeout(90000);
    }

    private void adjustConfiguration(ClientConfiguration config) {
        configureSSLTrustManager(config);
        addHttpChunking(config);
        increaseHttpClientConnectionTimeout(config);

        config.getInInterceptors().add(new LoggingInInterceptor());
        config.getOutInterceptors().add(new LoggingOutInterceptor());

        /*
         * All for the empty "" in Path parameter values; the default
         * behavior is to throw an exception in 3.4.x but in previous
         * versions the empty value was converted to null and then
         * eventually to the empty string ("")
         */
        config.getBus().setProperty("allow.empty.path.template.value", Boolean.TRUE);

        /*
         * As part of Apache CXF 3.4.x, a change was made to be more compliant
         * with the JAX-RS specification. This closes the Response.getEntity()
         * method, making it impossible to get the Entity without incurring an
         * Exception.
         *
         * This configuration pre-buffers the Entity allowing access to the Entity
         * and also "last" Entity.
         *
         * This is in keeping with the previous behavior.
         *
         * This is also sensible because the API response has already been parsed
         * in to an Entity in the Response object. The "closed" check was denying
         * access to an Entity that was already materialized in to a Java Object
         * generated from our XSDs or YAML.
         */
        config.getResponseContext().put("buffer.proxy.response", Boolean.TRUE);
    }

    protected void configureClient(Object client) {
        adjustConfiguration(WebClient.getConfig(client));
        configureHttpRequestHeaders(client);
    }

    public CxfClientSecurityContext getClientSecurityContext() {
        return this.cxfClientSecurityContext;
    }
}

