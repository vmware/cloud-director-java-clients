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


import java.net.HttpURLConnection;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.vmware.cxfrestclient.AbstractCxfRestClient;
import com.vmware.cxfrestclient.CxfClientSecurityContext;
import com.vmware.vcloud.api.rest.constants.RestConstants;
import com.vmware.vcloud.api.rest.schema_v1_5.ErrorType;
import com.vmware.vcloud.api.utils.UrnUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.client.Client;

/**
 * Base class for all clients that can interact with VCD
 * <P>
 * Provides common request header management functionality
 */
abstract class AbstractVcdClientBase extends AbstractCxfRestClient {

    private static final String BEARER = "Bearer";
    protected static final String AUTHORIZATION_HEADER = "Authorization";

    protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXX");

    protected static final String MULTISITE_ACCEPT_HEADER_FORMAT =
            ";" + RestConstants.MULTISITE_ATTR + "{0}";

    private Supplier<String> clientRequestIdProvider = () -> UUID.randomUUID().toString();

    private final String apiVersion;

    private final String userAgent;

    private final Map<String, String> cookies = new LinkedHashMap<>();

    private volatile UserSecurityContext context = new UserSecurityContext();

    protected AbstractVcdClientBase(URI endpoint,
            CxfClientSecurityContext cxfClientSecurityContext, String apiVersion, String userAgent) {
        super(endpoint, cxfClientSecurityContext);
        this.apiVersion = apiVersion;
        this.userAgent = userAgent;
    }

    protected AbstractVcdClientBase(AbstractCxfRestClient client, String apiVersion, String userAgent) {
        super(client);
        this.apiVersion = apiVersion;
        this.userAgent = userAgent;
    }

    protected AbstractVcdClientBase(URI endpoint, AbstractCxfRestClient client, String apiVersion, String userAgent) {
        super(endpoint, client);
        this.apiVersion = apiVersion;
        this.userAgent = userAgent;
    }


    /**
     * Accept headers that must be included with the request.
     *
     * @return Appropriately formatted accept values in String format.
     */
    protected abstract String[] getAcceptHeaders();

    @Override
    protected final void configureHttpRequestHeaders(final org.apache.cxf.jaxrs.client.Client client) {
        client.accept(getAcceptHeaders());

        setAuthenticationHeaders(client);

        if (clientRequestIdProvider != null) {
            final String clientRequestId = clientRequestIdProvider.get();
            if (!StringUtils.isEmpty(clientRequestId)) {
                client.header(RestConstants.VCLOUD_CLIENT_REQUEST_ID_HEADER, clientRequestId);
            }
        }
        client.header(HttpHeaders.USER_AGENT, userAgent);
    }

    protected void setClientRequestIdProvider(Supplier<String> clientRequestIdGenerator) {
        this.clientRequestIdProvider = clientRequestIdGenerator;
    }

    protected void addCookie(final String cookieName, final Client client) {
        final String rawCookie = cookies.get(cookieName);
        final String cookieValue = rawCookie.substring(rawCookie.indexOf("=") + 1);
        final Cookie cookie = new Cookie(cookieName, cookieValue);
        client.cookie(cookie);
    }

    protected Map<String, String> getCookies() {
        return cookies;
    }

    public void setAuthenticationHeader(final Client client) {
        if (hasSessionlessClientCredentials()) {
            client.header(context.getClientCredentials().getHeaderName(),
                    context.getClientCredentials().getHeaderValue());
        } else if (context.getJwtToken() != null) {
            client.header(AUTHORIZATION_HEADER, BEARER + " " + context.getJwtToken());
            if (context.getAuthContext() != null) {
                client.header(RestConstants.VCLOUD_AUTH_CONTEXT_HEADER, context.getAuthContext());
            }
        } else if (getAuthenticationToken() != null) {
            client.header(RestConstants.VCLOUD_AUTHENTICATION_HEADER, getAuthenticationToken());
        }
        if (getOrgContextHeader() != null) {
            client.header(RestConstants.VCLOUD_TENANT_CONTEXT_HEADER, getOrgContextHeader());
        }
    }

    /**
     * Configure authentication headers on the supplied clients
     * <P>
     * May possibly set cookies too, if applicable.
     *
     * @param client
     *            {@link Client} to set authentication headers on.
     */
    protected void setAuthenticationHeaders(
            final org.apache.cxf.jaxrs.client.Client client) {
        setAuthenticationHeader(client);
        if (!hasSessionlessClientCredentials()) {
            if (cookies.containsKey(RestConstants.JWT_COOKIE_NAME)) {
                addCookie(RestConstants.JWT_COOKIE_NAME, client);
            }

            if (cookies.containsKey(RestConstants.SESSION_COOKIE_NAME)) {
                addCookie(RestConstants.SESSION_COOKIE_NAME, client);
            } else if (cookies.containsKey(RestConstants.VCLOUD_COOKIE_NAME)) {
                addCookie(RestConstants.VCLOUD_COOKIE_NAME, client);
            }
        }
    }

    private boolean hasSessionlessClientCredentials() {
        return context.getClientCredentials() != null
                && context.getClientCredentials().supportsSessionless();
    }

    protected void setCredentialsInternal(final ClientCredentials credentials) {
        context.setClientCredentials(credentials);
        context.setJwtToken(null);
    }

    public ClientCredentials getCredentials() {
        return context.getClientCredentials();
    }


    public String getClientApiVersion() {
        return apiVersion;
    }

    protected void dologinInternal(ClientCredentials credentials) {
        //NO-OP
    }

    protected void doInitClient() {
        //NO-OP
    };


    public void setAuthContextHeader(String authContext) {
        context.setAuthContext(authContext);
    }

    public String getAuthContextHeader() {
        return context.getAuthContext();
    }

    public String getAuthenticationToken() {
        return context.getAuthenticationToken();
    }

    public void setAuthenticationToken(String authenticationToken) {
        context.setAuthenticationToken(authenticationToken);
    }

    public String getJwtToken() {
        return context.getJwtToken();
    }


    public void setJwtToken(String jwtToken) {
        context.setJwtToken(jwtToken);
    }

    /**
     * Controls whether requests made with this client request multisite behavior or not.
     *
     * @param shouldGlobalFanout
     *            True for global fanout, False for no fanout
     *
     * @deprecated This method is deprecated in favor of
     *             {@link AbstractVcdClientBase#setMultisiteLocations(List)} and
     *             {@link AbstractVcdClientBase#setMultisiteLocationHeaderValue(String)} which
     *             provide more control over the same setting
     *
     */
    @Deprecated
    public void setMultisiteRequests(boolean shouldGlobalFanout) {
        context.setMultiSiteRequests(shouldGlobalFanout ? RestConstants.MULTISITE_ATTR_GLOBAL : null);
    }

    /**
     * A string with {@link RestConstants#MULTISITE_ATTR_GLOBAL},
     * {@link RestConstants#MULTISITE_ATTR_LOCAL}, or a number of locationIds separated by
     * {@link RestConstants#MULTISITE_ATTR_SEPARATOR}. Pass {@code null} to turn multisite off. Can
     * also be set by {@link AbstractVcdClientBase#setMultisiteLocations(List)}
     *
     */
    public void setMultisiteLocationHeaderValue(String multisiteLocations) {
        context.setMultiSiteRequests(multisiteLocations);
    }

    /**
     * Sets the locationIds in the accept header for multisite fanout. Can also be set by
     * {@link AbstractVcdClientBase#setMultisiteLocationHeaderValue(String)}
     *
     * @param multisiteLocations
     *            List of locationIds or {@code null} to turn multisite off
     */
    public void setMultisiteLocations(List<String> multisiteLocations) {
        context.setMultiSiteRequests(multisiteLocations == null ? null
                : multisiteLocations.stream().collect(Collectors.joining(RestConstants.MULTISITE_ATTR_SEPARATOR)));
    }

    public String getMultisiteLocations() {
        return context.getMultiSiteRequests();
    }

    /**
     * Sets the X-VMWARE-VCLOUD-TENANT-CONTEXT header to the specified value
     */
    public void setTenantContextHeader(String tenantContextHeader) {
        context.setTenantContextHeader(tenantContextHeader);
    }

    /**
     * Get the value to be sent in the X-VMWARE-VCLOUD-TENANT-CONTEXT header
     */
    protected String getOrgContextHeader() {
        return context.getTenantContextHeader();
    }


    /**
     * Checks HTTP status in the specified response.
     *
     * @param response
     *            HTTP response to check
     * @param expectedStatus
     *            Expected HTTP status
     * @throws {@link
     *             VcdErrorResponseException} if HTTP status doesn't match {@code expectedStatus}
     */
    protected void checkResponse(final Response response, final int expectedStatus) {
        if (response.getStatus() == expectedStatus) {
            return;
        }

        final int responseStatus = response.getStatus();

        ErrorType error = null;
        if (responseStatus != HttpURLConnection.HTTP_UNAUTHORIZED) {

            try {
                // readEntity fails in scenarios where the response is not of ErrorType
                // for example HTTP Error 301
                error = response.readEntity(ErrorType.class);
            } catch (final Exception e) {
                // ignore
            }
        }

        final String requestId = getRequestId(response);

        throw new VcdErrorResponseException(responseStatus, requestId, error, null);
    }

    protected String getRequestId(final Response response) {
        final String requestId = response.getHeaderString(RestConstants.VCLOUD_REQUEST_ID_HEADER);
        return requestId;
    }


    protected void processHeaders(final MultivaluedMap<String, Object> responseHeaders) {

        final String accessToken =
                (String) responseHeaders.getFirst(RestConstants.VCLOUD_ACCESS_TOKEN_HEADER);
        if (accessToken != null) {
            setJwtToken(accessToken);
        }

        final List<Object> rawCookies = responseHeaders.get("Set-Cookie");
        if (rawCookies == null) {
            return;
        }

        for (Object o : rawCookies) {
            final String rawCookie = (String) o;
            final String name = rawCookie.substring(0, rawCookie.indexOf("="));
            getCookies().put(name, rawCookie);
        }
    }

    protected void clearSessionData() {
        getCookies().clear();
        this.context.clearSessionData();
    }

    protected void setUserSecurityContext(final UserSecurityContext context) {
        this.context = context;
    }

    protected UserSecurityContext getUserSecurityContext() {
        return this.context;
    }

    /**
     * Encapsulating the elements that can be shared across 'linked' instances of an OpenApiClient
     * and a VcdClient
     */
    protected final class UserSecurityContext {
        private volatile String multiSiteRequests;
        private volatile ClientCredentials clientCredentials;
        private volatile String authContext;
        private volatile String tenantContextHeader;
        private volatile String authenticationToken;
        private volatile String jwtToken;

        public ClientCredentials getClientCredentials() {
            return clientCredentials;
        }

        public void setClientCredentials(final ClientCredentials clientCredentials) {
            this.clientCredentials = clientCredentials;
        }

        public String getJwtToken() {
            return jwtToken;
        }

        public void setJwtToken(final String jwtToken) {
            this.jwtToken = jwtToken;
        }

        public String getAuthContext() {
            return authContext;
        }

        public void setAuthContext(final String authContext) {
            this.authContext = authContext;
        }

        public String getAuthenticationToken() {
            return authenticationToken;
        }

        public void setAuthenticationToken(final String authenticationToken) {
            this.authenticationToken = authenticationToken;
        }

        public String getMultiSiteRequests() {
            return multiSiteRequests;
        }

        public void setMultiSiteRequests(String multiSiteRequests) {
            this.multiSiteRequests = multiSiteRequests;
        }

        public String getTenantContextHeader() {
            return tenantContextHeader;
        }

        public void setTenantContextHeader(String tenantContext) {
            if (UrnUtils.isUrn(tenantContext)) {
                tenantContext = UrnUtils.getEntityId(tenantContext);
            }
            this.tenantContextHeader = tenantContext;
        }

        public void clearSessionData() {
            setJwtToken(null);
            setAuthenticationToken(null);
        }
    }

}

