
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
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBElement;

import com.vmware.cxfrestclient.JaxRsClient.ErrorHandler.Disposition;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.jaxrs.client.ClientConfiguration;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.http.HTTPConduit;

/**
 * Base class for implementations of {@link JaxRsClient} that are based on CXF.
 */
public abstract class AbstractCxfRestClient implements JaxRsClient {

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
                return createWebClient(uri).get(resourceClass);
            } catch (WebApplicationException e) {
                if (! handleException(uri, e, ++failureCount)) {
                    throw makeException(e);
                }
            }
        } while (true);
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
            throw makeException(new WebApplicationException(response));
        }
    }

    @Override
    public <ContentsClass, ResponseClass> ResponseClass putResource(URI uri, String type, JAXBElement<ContentsClass> contents, Class<ResponseClass> responseClass) {
        int failureCount = 0;
        do {
            try {
                return createWebClient(uri, type).invoke(HttpMethod.PUT, contents, responseClass);
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
                return createWebClient(uri, type).post(contents, responseClass);
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
                return createWebClient(uri).invoke(HttpMethod.DELETE, null, responseClass);
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
    }

    protected void configureClient(Object client) {
        adjustConfiguration(WebClient.getConfig(client));
        configureHttpRequestHeaders(client);
    }
}


