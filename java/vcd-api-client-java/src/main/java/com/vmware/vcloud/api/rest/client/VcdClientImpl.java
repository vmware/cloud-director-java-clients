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
import java.lang.annotation.Annotation;
import java.net.HttpURLConnection;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.vmware.cxfrestclient.CxfClientSecurityContext;
import com.vmware.vcloud.api.annotation.Supported;
import com.vmware.vcloud.api.http.converters.JAXBElementMixIn;
import com.vmware.vcloud.api.http.converters.QNameMixin;
import com.vmware.vcloud.api.rest.client.filters.ClientVersionCompatibilityFilter;
import com.vmware.vcloud.api.rest.client.filters.MultisiteAuthorizationFilter;
import com.vmware.vcloud.api.rest.client.impl.EventViewerImpl;
import com.vmware.vcloud.api.rest.client.impl.tasks.VcdTaskMonitorImpl;
import com.vmware.vcloud.api.rest.constants.RelationType;
import com.vmware.vcloud.api.rest.constants.RestAdminConstants;
import com.vmware.vcloud.api.rest.constants.RestConstants;
import com.vmware.vcloud.api.rest.constants.RestConstants.HttpStatusCodes;
import com.vmware.vcloud.api.rest.jaxrs.typeresolvers.GlobalIdToJavaTypeResolver;
import com.vmware.vcloud.api.rest.schema.versioning.SupportedVersionsType;
import com.vmware.vcloud.api.rest.schema.versioning.VersionInfoType;
import com.vmware.vcloud.api.rest.schema_v1_5.AdminOrgType;
import com.vmware.vcloud.api.rest.schema_v1_5.ApiExtensibilityType;
import com.vmware.vcloud.api.rest.schema_v1_5.ContainerType;
import com.vmware.vcloud.api.rest.schema_v1_5.EntityType;
import com.vmware.vcloud.api.rest.schema_v1_5.ErrorType;
import com.vmware.vcloud.api.rest.schema_v1_5.LinkType;
import com.vmware.vcloud.api.rest.schema_v1_5.MultisiteSessionUserInfoType;
import com.vmware.vcloud.api.rest.schema_v1_5.OrgListType;
import com.vmware.vcloud.api.rest.schema_v1_5.OrgType;
import com.vmware.vcloud.api.rest.schema_v1_5.OrganizationReferenceType;
import com.vmware.vcloud.api.rest.schema_v1_5.QueryListType;
import com.vmware.vcloud.api.rest.schema_v1_5.QueryResultRecordType;
import com.vmware.vcloud.api.rest.schema_v1_5.QueryResultRecordsType;
import com.vmware.vcloud.api.rest.schema_v1_5.ReferenceType;
import com.vmware.vcloud.api.rest.schema_v1_5.ReferencesType;
import com.vmware.vcloud.api.rest.schema_v1_5.ResourceType;
import com.vmware.vcloud.api.rest.schema_v1_5.SessionType;
import com.vmware.vcloud.api.rest.schema_v1_5.VCloudType;
import com.vmware.vcloud.api.rest.schema_v1_5.extension.VMWExtensionType;
import com.vmware.vcloud.api.rest.version.ApiVersion;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.provider.JAXBElementProvider;
import org.apache.cxf.jaxrs.provider.JAXBElementTypedProvider;
import org.apache.cxf.jaxrs.utils.HttpUtils;

public class VcdClientImpl extends AbstractVcdClientBase implements VcdClient {

    private static final String USER_AGENT = "vcd-client";

    private final VcdTaskMonitor taskMonitor = new VcdTaskMonitorImpl(this);
    private final EventViewer eventViewer = new EventViewerImpl(this);
    private Map<String, URI> queryListMap = null;
    private WireFormat wireFormat = WireFormat.XML;

    private URI sessionHref = null;

    private URI loggedInAdminOrgEndpoint = null;

    private Map<WellKnownEndpoint, URI> sessionEndpoints;

    private static final Class<?>[] OBJECT_FACTORIES = new Class<?>[] {
            com.vmware.vcloud.api.rest.schema.versioning.ObjectFactory.class,
            com.vmware.vcloud.api.rest.schema_v1_5.ObjectFactory.class,
            com.vmware.vcloud.api.rest.schema_v1_5.extension.ObjectFactory.class,
            com.vmware.vcloud.api.rest.schema.ovf.vmware.ObjectFactory.class,
            com.vmware.vcloud.api.rest.schema.ovf.ObjectFactory.class,
            com.vmware.vcloud.api.rest.schema.ovf.environment.ObjectFactory.class
    };

    // TODO: Remove once v35 is no longer supported, see VTEN-3875
    private Boolean isZeusOrAbove;

    /**
     * Implementation of {@link SessionToken} to represent a current session with vCD. It can be retrieved
     * from one {@link VcdClient} and used by another {@link VcdClient} to re-use an existing session.
     */
    public static final class SessionTokenImpl implements SessionToken {

        /**
         * The authentication token from a previous login.
         */
        private final String authenticationToken;

        /**
         * The vcloud-token cookie from a previous login.
         */
        private final String vCloudToken;

        public SessionTokenImpl(final String authenticationToken, final String vCloudToken) {
            this.authenticationToken = authenticationToken;
            this.vCloudToken = vCloudToken;
        }

        @Override
        public String getAuthenticationToken() {
            return authenticationToken;
        }

        public String getVCloudToken() {
            return vCloudToken;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            SessionTokenImpl other = (SessionTokenImpl) obj;
            if (authenticationToken == null) {
                if (other.authenticationToken != null) {
                    return false;
                }
            } else if (!authenticationToken.equals(other.authenticationToken)) {
                return false;
            }
            if (vCloudToken == null) {
                if (other.vCloudToken != null) {
                    return false;
                }
            } else if (!vCloudToken.equals(other.vCloudToken)) {
                return false;
            }
            return true;
        }
    }

    private final static List<?> PROVIDER_LIST = createJAXBElementProviderFromObjectFactories();

    private static final List<Object> JSON_PROVIDERS_LIST = createJsonJaxbProviders();

    /**
     * Creates a {@link JAXBElementProvider} that will return {@link JAXBContext}s that handle all
     * the JAXB-generated types using object factories supplied to this method.
     */
    protected static List<JAXBElementProvider<?>> createJAXBElementProviderFromObjectFactories() {
        List<JAXBElementProvider<?>> list = new ArrayList<>();

        JAXBElementTypedProvider jaxbProvider = new JAXBElementTypedProvider();
        jaxbProvider.setExtraClass(OBJECT_FACTORIES);
        list.add(jaxbProvider);

        JAXBElementProvider<Object> objProvider = new JAXBElementProvider<>();
        objProvider.setExtraClass(OBJECT_FACTORIES);
        list.add(objProvider);

        return list;
    }

    protected static List<Object> createJsonJaxbProviders() {
        final ObjectMapper jaxbObjectMapper = new ObjectMapper()
                .addMixIn(JAXBElement.class, JAXBElementMixIn.class)
                .addMixIn(QName.class, QNameMixin.class)
                .setDateFormat(DATE_FORMAT)
                .enable(SerializationFeature.INDENT_OUTPUT);

        configureObjectMapper(jaxbObjectMapper);

        final JacksonJsonProvider vCloudJsonJaxrsProvider =
                new JacksonJsonProvider(jaxbObjectMapper);

        return Collections.singletonList(vCloudJsonJaxrsProvider);
    }

    private static void configureObjectMapper(final ObjectMapper jaxbObjectMapper) {
        final GlobalIdToJavaTypeResolver typeResolver = constructTypeResolver(jaxbObjectMapper);
        final SimpleFilterProvider filterProvider = new SimpleFilterProvider();
        filterProvider.addFilter(Supported.VCLOUD_LEGACY_FILTER_PARAM,
                new VcloudBeanPropertyFilter());
        jaxbObjectMapper.setFilterProvider(filterProvider);
        final StdTypeResolverBuilder typeResolverBuilder =
                new ObjectMapper.DefaultTypeResolverBuilder(DefaultTyping.JAVA_LANG_OBJECT)
                        .init(Id.NAME, typeResolver)
                        .inclusion(As.PROPERTY).typeProperty("_type");

        jaxbObjectMapper.setDefaultTyping(typeResolverBuilder);

        jaxbObjectMapper.addHandler(new DeserializationProblemHandler() {
            @Override
            public JavaType handleUnknownTypeId(DeserializationContext ctxt, JavaType baseType,
                    String subTypeId, TypeIdResolver idResolver, String failureMsg)
                    throws IOException {
                return typeResolver.getJavaTypeForId(ctxt, subTypeId);
            }
        });
    }

    private static GlobalIdToJavaTypeResolver constructTypeResolver(
            final ObjectMapper jaxbObjectMapper) {
        final List<String> packages = Arrays.asList("com.vmware.vcloud.api.rest.schema_v1_5",
                "com.vmware.vcloud.api.rest.schema.ovf",
                "com.vmware.vcloud.api.rest.schema.ovf.environment",
                "com.vmware.vcloud.api.rest.schema.ovf.vmware",
                "com.vmware.vcloud.api.rest.schema_v1_5.extension");
        final Class<? extends Annotation> schemaAnnotation =
                com.fasterxml.jackson.annotation.JsonFilter.class;

        final Class<?> schemaClass = com.vmware.vcloud.api.rest.schema_v1_5.VCloudType.class;

        return new GlobalIdToJavaTypeResolver(packages, schemaAnnotation, schemaClass,
                jaxbObjectMapper);
    }

    @Override
    protected List<?> getCxfProviders() {
        final List<Object> providers = new LinkedList<>(PROVIDER_LIST);
        providers.addAll(JSON_PROVIDERS_LIST);
        if (getCredentials() instanceof VcdMultisiteLoginCredentials) {
            providers.add(new MultisiteAuthorizationFilter(
                    (VcdMultisiteLoginCredentials) getCredentials()));
        }
        providers.add(new ClientVersionCompatibilityFilter());
        return providers;
    }

    /**
     * Creates an instance of VcdClientImpl with given endpoint, API version and cxfClientSecurityContext.
     */
    public VcdClientImpl(final URI endpoint, final String apiVersion, final CxfClientSecurityContext cxfClientSecurityContext) {
        super(endpoint, cxfClientSecurityContext, apiVersion, USER_AGENT);
    }

    /**
     * Creates an instance of VcdClientImpl with given endpoint, cxfClientSecurityContext and
     * determines an {@link ApiVersion} supported by both given vCD {@code endpoint}, and given list
     * of preferred API versions in {@code candidateVersions}
     *
     * @param endpoint
     *            URI of vCD
     * @param candidateVersions
     *            a list of {@code ApiVersion}s in order of most to least preferred by client
     * @param cxfClientSecurityContext
     *            security context to use when communicating with given HTTPS vCD {@code endpoint}
     */
    public VcdClientImpl(final URI endpoint, final List<ApiVersion> candidateVersions,
            final CxfClientSecurityContext cxfClientSecurityContext) {
        super(endpoint, cxfClientSecurityContext,
                VcdClientImpl.getApiVersion(candidateVersions, endpoint, cxfClientSecurityContext), USER_AGENT);
    }

    private VcdClientImpl(VcdClientImpl vcdClient) {
        super(vcdClient, vcdClient.getClientApiVersion(), USER_AGENT);
    }

    private URI getEndpoint(WellKnownEndpoint endpoint) {
        return sessionEndpoints == null ? null : sessionEndpoints.get(endpoint);
    }

    /**
     * Gets the openApi endpoint.
     */
    public URI getOpenApiEndpoint() {
        return Optional.ofNullable(getEndpoint(WellKnownEndpoint.OPENAPI))
                .orElseGet(() -> {
                    final URI openApiEndPoint = UriBuilder.fromUri(this.getEndpoint()).replacePath("cloudapi").build();
                    return openApiEndPoint;
        });
    }

    /**
     * Gets the openApi endpoint.
     */
    protected URI getNetworkApiEndpoint() {
        return sessionEndpoints.get(WellKnownEndpoint.NETWORK);
    }

    @Override
    public List<ReferenceType> getOrganizations() {
        final OrgListType orgList = createWebClient(validateEndpoint(WellKnownEndpoint.ORG_LIST)).get(OrgListType.class);
        return Collections.unmodifiableList(orgList.getOrg());
    }

    @Override
    public SessionType getSession() {
        validateEndpoint(sessionHref);
        return getResource(sessionHref, SessionType.class);
    }

    @Override
    public MultisiteSessionUserInfoType getSessionUserInfo() {
        validateEndpoint(sessionHref);
        return getResource(UriBuilder.fromUri(sessionHref).path("userInfo").build(),
                MultisiteSessionUserInfoType.class);
    }

    @Override
    public void setWireFormat(WireFormat wireFormat) {
        this.wireFormat = wireFormat;
    }

    @Override
    public SupportedVersionsType getVersions() {
        return getResource(UriBuilder.fromUri(endpoint).path(RestConstants.Uri.VERSIONS).build(),
                SupportedVersionsType.class);
    }

    public SupportedVersionsType getVersions(final URI endpoint) {
        return getResource(UriBuilder.fromUri(endpoint).path(RestConstants.Uri.VERSIONS).build(),
                SupportedVersionsType.class);
    }


    private <ResponseClass> ResponseClass getResource(WellKnownEndpoint endpoint, Class<ResponseClass> resourceClass) {
        return getResource(validateEndpoint(endpoint), resourceClass);
    }

    @Override
    public VCloudType getAdmin() {
        return getResource(WellKnownEndpoint.ADMIN, VCloudType.class);
    }

    @Override
    public VMWExtensionType getExtension() {
        return getResource(WellKnownEndpoint.EXTENSION, VMWExtensionType.class);
    }

    @Override
    public QueryListType getQueryList() {
        return getResource(WellKnownEndpoint.QUERY_LIST, QueryListType.class);
    }

    @Override
    public ApiExtensibilityType getApiExtensibility() {
        return getResource(WellKnownEndpoint.API_EXTENSIBILITY, ApiExtensibilityType.class);
    }

    @Override
    public OrgType getLoggedInOrg() {
        return getResource(WellKnownEndpoint.LOGGED_IN_ORG, OrgType.class);
    }

    @Override
    public EntityType resolveEntityById(String id) {
        return getResource(UriBuilder.fromUri(validateEndpoint(WellKnownEndpoint.ENTITY_RESOLVER))
                .path(id).build(), EntityType.class);
    }

    @Override
    public AdminOrgType getLoggedInAdminOrg() {

        OrgType org = getLoggedInOrg();
        LinkType link = VcdUtils.findLink(org, RelationType.ALTERNATE,
                                          RestAdminConstants.MediaType.ORGANIZATIONM, false);
        if (link != null) { // link is only present in newer versions.
            return getResource(link.getHref(), AdminOrgType.class);
        } else {
            // We indicate lack of admin access by throwing a missing link exception
            // for the 'admin' link as that would be pre-requisite to getting admin org link
            if(loggedInAdminOrgEndpoint == null){
                loggedInAdminOrgEndpoint = getLoggedInOrgAdminURI();
            }
            validateEndpoint(loggedInAdminOrgEndpoint, RelationType.DOWN,
                             RestAdminConstants.MediaType.VCLOUDM);
            return getResource(loggedInAdminOrgEndpoint, AdminOrgType.class);
        }
    }

    /*
     * @deprecated Use {@link com.vmware.vcloud.api.rest.client.RestClientFactory#createOpenApiClient(VcdClientImpl)}
     */
    @Override
    @Deprecated
    public OpenApiClient getOpenApiClient() {
        return new OpenApiClientImpl(this);
    }

    @Override
    public NsxProxyApiClient getNsxProxyApiClient() {
        return new NsxProxyApiClient(this);
    }

    private void validateEndpoint(URI endpoint) {
        if (endpoint == null) {
            throw new IllegalStateException("Can't perform operation when not logged in");
        }
    }

    /**
     * Validates the endpoint to make sure it is accessible by the user
     *
     * @param endpoint
     *            {@link URI} of the endpoint to validate
     * @param rel
     *            {@link RelationType} enum value describing the relation of the endpoint to the
     *            &lt;Session&gt; element in the schema. Used to generate
     *            {@link MissingLinkException} if the endpoint is unavailable
     * @param mediaType
     *            {@code MediaType} of the object pointed to by the relationship above. Used to
     *            generate {@link MissingLinkException} if the endpoint is unavailable
     * @throws MissingLinkException
     *             to indicate link is unavailable
     */
    private void validateEndpoint(URI endpoint, RelationType rel, String mediaType) {
        if (endpoint == null) {
            throw new MissingLinkException(sessionHref.toASCIIString(), rel, mediaType);
        }
    }

    /**
     * Validates the well-known endpoint to make sure it is accessible by the user and
     * return its corresponding URI if it is.
     * @param endpoint the well-known endpoint to validate
     * @throws MissingLinkException to indicate link is unavailable
     */
    private URI validateEndpoint(WellKnownEndpoint endpoint) {
        URI uri = getEndpoint(endpoint);
        validateEndpoint(uri, endpoint.rel, endpoint.mediaType);
        return uri;
    }

    @Override
    protected String[] getAcceptHeaders() {
        StringBuffer acceptHeader = new StringBuffer("application/*+" + wireFormat.toString());

        if (getClientApiVersion() != null) {
            acceptHeader.append(";" + RestConstants.API_VERSION_ATTR);
            acceptHeader.append(getClientApiVersion());
        }
        if (StringUtils.isNotBlank(getMultisiteLocations())) {
            acceptHeader.append(MessageFormat.format(MULTISITE_ACCEPT_HEADER_FORMAT, getMultisiteLocations()));
        }
        final String accept = acceptHeader.toString();
        return new String[] { accept };
    }

    @Override
    public void login(ClientCredentials credentials) {
        dologinInternal(credentials);
    }

    @Override
    public void relogin() {
        if (getCredentials() == null) {
            throw new RuntimeException("Expected client credentials to not be null");
        }
        dologinInternal(getCredentials());
    }

    @Override
    public void loginWithJwt(final String jwt, final String orgSecurityContext) {
        setAuthContextHeader(orgSecurityContext);

        setCredentialsInternal(null);
        setJwtToken(jwt);

        doInitClient();
    }

    @Override
    public void loginWithToken(final SessionToken sessionToken) {

        setCredentialsInternal(null);

        // Get the values from the session token necessary to initialize this client
        if (!(sessionToken instanceof SessionTokenImpl)) {
            throw new AssertionError("Invalid session token.");
        }
        final SessionTokenImpl sessionTokenImpl = (SessionTokenImpl) sessionToken;
        setAuthenticationToken(sessionTokenImpl.getAuthenticationToken());
        final String vCloudTokenCookie = sessionTokenImpl.getVCloudToken();
        if (vCloudTokenCookie != null) {
            getCookies().put(RestConstants.VCLOUD_COOKIE_NAME, vCloudTokenCookie);
        }

        doInitClient();
    }

    /**
     * Initialize this {@link VcdClient}'s internals with a session that will either be created from
     * this {@link WebClient} via login, or with this {@link WebClient} via retrieving an existing
     * session.
     *
     * @param client
     *            The client to create or retrieve the session.
     * @param isLogin
     *            {@code true} if a new session should be created by logging in, {@code false} to
     *            reuse an existing session.
     */
    private void initializeWithSession(final WebClient client, final boolean isLogin) {

        configureHttpRequestHeaders(client);
        configureSSLTrustManager(WebClient.getConfig(client));

        /*
         * Get/create the session and process the response.
         */
        final Response response = isLogin ? client.post(null) : client.get();
        checkResponse(response, HttpURLConnection.HTTP_OK);

        SessionType session = response.readEntity(SessionType.class);

        // Processing the headers is only necessary after the login, because if the session
        // is being reused, the authentication token and vcloud token have already been set.
        // In the case of a login (new session) we need to process the headers and reconfigure
        // the client's headers, as the auth token and vcloud token have been retrieved.
        if (isLogin) {
            processHeaders(response.getMetadata());
            configureHttpRequestHeaders(client);
        }

        sessionHref = URI.create(session.getHref());
        sessionEndpoints = WellKnownEndpoint.getSessionEndpoints(session);
    }


    @Override
    public SessionToken getSessionToken() {
        if (getAuthenticationToken() != null) {
            return new SessionTokenImpl(getAuthenticationToken(),
                    getCookies().get(RestConstants.VCLOUD_COOKIE_NAME));
        }
        return null;
    }



    private enum WellKnownEndpoint {
        // Endpoints that are always present when VCD is not in maintenance mode:
        ENTITY_RESOLVER(RelationType.ENTITY_RESOLVER, RestConstants.MediaType.ENTITY),
        LOGGED_IN_ORG(RelationType.DOWN, RestConstants.MediaType.ORGANIZATION),
        ORG_LIST(RelationType.DOWN, RestConstants.MediaType.ORGANIZATION_LIST),
        QUERY_LIST(RelationType.DOWN, RestConstants.MediaType.QUERY_LIST),

        // Endpoints whose presence depends on the credentials used when establishing the session:
        ADMIN(RelationType.DOWN, RestAdminConstants.MediaType.VCLOUDM),
        API_EXTENSIBILITY(RelationType.API_EXTENSIBILITY, RestConstants.MediaType.API_EXTENSIBILITY),
        EXTENSION(RelationType.DOWN, RestConstants.MediaType.VMW_EXTENSION),
        OPENAPI(RelationType.OPENAPI, RestConstants.MediaType.APPLICATION_JSON),
        NETWORK(RelationType.NSX, MediaType.APPLICATION_XML),
        ;

        private final String mediaType;
        private final RelationType rel;

        WellKnownEndpoint(RelationType rel, String mediaType) {
            this.rel = rel;
            this.mediaType = mediaType;
        }

        static Map<WellKnownEndpoint, URI> getSessionEndpoints(SessionType session) {
            Map<WellKnownEndpoint, URI> map = new HashMap<>();
            for (WellKnownEndpoint endpoint : WellKnownEndpoint.values()) {
                LinkType link = VcdUtils.findLink(session, endpoint.rel, endpoint.mediaType, false);
                if (link != null) {
                    map.put(endpoint, URI.create(link.getHref()));
                }
            }
            return map;
        }
    }

    private URI getLoggedInOrgAdminURI() {
        if (getEndpoint(WellKnownEndpoint.ADMIN) == null) {
            // If no access to admin resource, just return now.
            return null;
        }

        final OrgType loggedInOrg = getLoggedInOrg();
        final String loggedInOrgId = loggedInOrg.getId();

        final List<OrganizationReferenceType> adminOrganizationReferences =
                getAdmin().getOrganizationReferences().getOrganizationReference();

        for (final OrganizationReferenceType adminOrganizationReference : adminOrganizationReferences) {
            // Unfortunately adminOrganizationReference is not going to have an 'id'
            final AdminOrgType adminOrgType = getResource(adminOrganizationReference, AdminOrgType.class);

            if (loggedInOrgId.equals(adminOrgType.getId())) {
                return URI.create(adminOrgType.getHref());
            }
        }

        throw new AssertionError("Admin org reference not found despite user having access to admin resources!!");
    }

    @Override
    public void logout() {
        try {
            final Response response =
                    createWebClient(
                            UriBuilder.fromUri(endpoint).path(RestConstants.Uri.SESSION).build())
                            .delete();

            checkResponse(response, HttpURLConnection.HTTP_NO_CONTENT);

        } finally {
            clearSessionData();
        }
    }

    @Override
    public VcdClient duplicate(boolean newSession) throws VcdErrorException {
        VcdClientImpl duplicateClient = new VcdClientImpl(this);
        if (newSession) {
            duplicateClient.setCredentials(getCredentials());
        } else if (getJwtToken() != null) {
            duplicateClient.loginWithJwt(getJwtToken(), getAuthContextHeader());
        } else {
            duplicateClient.loginWithToken(getSessionToken());
        }
        return duplicateClient;
    }

    @Override
    protected void clearSessionData() {
        super.clearSessionData();
        sessionEndpoints = null;
        loggedInAdminOrgEndpoint = null;
        sessionHref = null;
    }

    @Override
    public WebClient createWebClient(URI uri, String type) {
        WebClient client = super.createWebClient(uri, type);

        /*
          WebClient will create a Content-Type header (oddly, even in the case of GETs) that in some cases
          might not be acceptable to the server.  The problem seems limited to requests to vCloud API
          extensions and/or the HCS extension in particular.  In any case perhaps the server being too picky.
           */
        client.type(type != null ? type : "application/*+xml");

        return client;
    }

    @Override
    public Response getServerStatus() {
        final WebClient webClient = createWebClient(UriBuilder.fromUri(endpoint).path("server_status").build());
        webClient.replaceHeader(HttpHeaders.ACCEPT, "*/*");
        return webClient.get();
    }

    @Override
    public <ResourceClass> ResourceClass getResource(ResourceType resource, RelationType rel, String mediaType, Class<ResourceClass> resourceClass) {
        LinkType link = VcdUtils.findLink(resource, rel, mediaType);
        return getResource(link.getHref(), resourceClass);
    }

    @Override
    public <ResourceClass extends ResourceType> ResourceClass getResource(ResourceType resourceType, Class<ResourceClass> resourceClass) {
        return getResource(resourceType.getHref(), resourceClass);
    }

    @Override
    public <ResourceClass> ResourceClass getResource(ReferenceType reference, Class<ResourceClass> resourceClass) {
        return getResource(reference.getHref(), resourceClass);
    }

    private <ResourceClass> ResourceClass getResource(String href, Class<ResourceClass> resourceClass) {
        return super.getResource(URI.create(href), resourceClass);
    }

    @Override
    public <ResponseClass extends EntityType> ResponseClass getEntity(String entityId, String mediaType, Class<ResponseClass> responseClass) {
        EntityType resolvedEntity = resolveEntityById(entityId);
        return getResource(
                        resolvedEntity,
                        RelationType.ALTERNATE,
                        mediaType,
                        responseClass);
    }


    @Override
    public <T extends EntityType> T getEntity(
            ReferenceType referenceType,
            Class<T> resourceClass,
            ReferenceTypeChangedCallBack referenceTypeChangedCallBack) {

        /*
         * Get entity using current href if not null, discard result if href is stale
         */
        RuntimeException savedException = null;
        try {
            String href = referenceType.getHref();
            if (href != null) {
                final T result = getResource(URI.create(href), resourceClass);
                if (referenceType.getId().equals(result.getId())) {
                    return result;
                }
            }
        } catch (ProcessingException e) {
            // href is invalid (example: host not reachable), fall through
            savedException = e;
        } catch (VcdErrorException e) {
            if (e.getHttpStatusCode() == HttpStatusCodes.SC_UNAUTHORIZED) {
                throw e;
            }
            savedException = e;
            // href is invalid (example: 404 RESOURCE_NOT_FOUND), fall through
        }
        /*
         * href is invalid, attempt to use vcd entity resolver with
         * the API endpoint associated with this VcdClient (which presumably is not
         * a prefix of the invalid href).
         */
        final T result;
        try {
            result = getEntity(referenceType.getId(), referenceType.getType(), resourceClass);
        } catch (RuntimeException e) {
            if (savedException != null) {
                throw savedException;
            }
            throw e;
        }
        /*
         * Success, let caller know of this repaired href
         */
        referenceTypeChangedCallBack.hrefChanged(result.getHref());

        return result;
    }

    @Override
    public <ContentsClass, ResponseClass> ResponseClass putResource(ResourceType resource, RelationType rel, String mediaType, JAXBElement<ContentsClass> contents, Class<ResponseClass> responseClass) {
        LinkType link = VcdUtils.findLink(resource, rel, mediaType);
        return putResource(link.getHref(), mediaType, contents, responseClass);
    }

    @Override
    public <ContentsClass, ResponseClass> ResponseClass putResource(ReferenceType reference, String mediaType, JAXBElement<ContentsClass> contents, Class<ResponseClass> responseClass) {
        return putResource(reference.getHref(), mediaType, contents, responseClass);
    }

    @Override
    public <ContentsClass extends ResourceType, ResponseClass> ResponseClass putResource(String mediaType, JAXBElement<ContentsClass> contents, Class<ResponseClass> responseClass) {
        return putResource(contents.getValue().getHref(), mediaType, contents, responseClass);
    }

    private <ContentsClass, ResponseClass> ResponseClass putResource(String href, String mediaType, JAXBElement<ContentsClass> contents, Class<ResponseClass> responseClass) throws VcdErrorException {
        mediaType = ObjectUtils.firstNonNull(wireFormat.switchMediaType(mediaType));
        return super.putResource(URI.create(href), mediaType, contents, responseClass);
    }

    @Override
    public <ContentsClass, ResponseClass> ResponseClass postResource(ResourceType resource, RelationType rel, String mediaType, JAXBElement<ContentsClass> contents, Class<ResponseClass> responseClass) {
        LinkType link = VcdUtils.findLink(resource, rel, mediaType);
        return postResource(link.getHref(), mediaType, contents, responseClass);
    }

    @Override
    public <ContentsClass, ResponseClass> ResponseClass postResource(ReferenceType reference, String mediaType, JAXBElement<ContentsClass> contents, Class<ResponseClass> responseClass) {
        return postResource(reference.getHref(), mediaType, contents, responseClass);
    }

    private <ContentsClass, ResponseClass> ResponseClass postResource(String href, String mediaType, JAXBElement<ContentsClass> contents, Class<ResponseClass> responseClass) throws VcdErrorException {
        mediaType = ObjectUtils.firstNonNull(wireFormat.switchMediaType(mediaType));
        return super.postResource(URI.create(href), mediaType, contents, responseClass);
    }

    @Override
    protected VcdErrorException makeException(WebApplicationException webApplicationException) {
        final Response response = webApplicationException.getResponse();
        return parseException(response, webApplicationException);
    }

    private VcdErrorException parseException(final Response response, WebApplicationException webApplicationException) {
        final StatusType statusInfo = response.getStatusInfo();
        final int responseStatus = statusInfo.getStatusCode();
        try {
            final String requestId = getRequestId(response);
            final String contentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
            if (StringUtils.contains(contentType, ErrorType.CONTENT_TYPE)) {
                final ErrorType error = response.readEntity(ErrorType.class);
                return new VcdErrorResponseException(responseStatus, requestId, error, webApplicationException);
            } else if (StringUtils.contains(contentType, MediaType.TEXT_PLAIN)
                    || StringUtils.contains(contentType, MediaType.TEXT_XML)) {
                final String errorMsg = response.readEntity(String.class);
                return new VcdErrorResponseException(responseStatus, requestId, errorMsg, webApplicationException);
            } else if (response.getLength() <= 0
                        &&  webApplicationException != null
                        && (statusInfo.getFamily().equals(Family.CLIENT_ERROR)
                                || statusInfo.getFamily().equals(Family.SERVER_ERROR))) {
                return new VcdErrorResponseException(responseStatus, requestId, (ErrorType)null, webApplicationException);
            }
            return new VcdErrorResponseProcessingException(responseStatus, null, webApplicationException);

        } catch (ProcessingException pe) {
            return new VcdErrorResponseProcessingException(responseStatus, pe, webApplicationException);
        } catch (IllegalStateException ise) {
            return new VcdErrorResponseProcessingException(responseStatus, ise, webApplicationException);
        }
    }

    @Override
    public Response removeResource(ResourceType resource) {
        return removeResource(resource, null, null, Response.class);
    }

    @Override
    public <ResponseClass> ResponseClass removeResource(ResourceType resource, Boolean force, Boolean recursive, Class<ResponseClass> responseClass) {
        try {
            final LinkType link = VcdUtils.findLink(resource, RelationType.REMOVE, null);
            return deleteResource(VcdUtils.buildDeleteUri(URI.create(link.getHref()), force, recursive), responseClass);
        } catch (MissingLinkException e) {
            throw new UnsupportedOperationException("Unable to delete " + resource.getHref(), e);
        }
    }

    @Override
    public <ResponseClass> ResponseClass deleteResource(ReferenceType ref, Class<ResponseClass> responseClass) {
        return this.deleteResource(ref, null, null, responseClass);
    }

    @Override
    public <ResponseClass> ResponseClass deleteResource(ResourceType ref, Class<ResponseClass> responseClass) {
        return this.deleteResource(ref, null, null, responseClass);
    }

    @Override
    public <ResponseClass> ResponseClass deleteResource(ResourceType resource, Boolean force, Boolean recursive, Class<ResponseClass> responseClass) {
        return super.deleteResource(VcdUtils.buildDeleteUri(URI.create(resource.getHref()), force, recursive), responseClass);
    }

    @Override
    public <ResponseClass> ResponseClass deleteResource(ReferenceType ref, Boolean force, Boolean recursive, Class<ResponseClass> responseClass) {
        return super.deleteResource(VcdUtils.buildDeleteUri(URI.create(ref.getHref()), force, recursive), responseClass);
    }

    @Override
    public com.vmware.vcloud.api.rest.schema_v1_5.ObjectFactory getVCloudObjectFactory() {
        return new com.vmware.vcloud.api.rest.schema_v1_5.ObjectFactory();
    }

    @Override
    public com.vmware.vcloud.api.rest.schema_v1_5.extension.ObjectFactory getVCloudExtensionObjectFactory() {
        return new com.vmware.vcloud.api.rest.schema_v1_5.extension.ObjectFactory();
    }

    @Override
    public com.vmware.vcloud.api.rest.schema.ovf.ObjectFactory getOvfObjectFactory() {
        return new com.vmware.vcloud.api.rest.schema.ovf.ObjectFactory();
    }

    @Override
    public com.vmware.vcloud.api.rest.schema.ovf.vmware.ObjectFactory getOvfVmwareObjectFactory() {
        return new com.vmware.vcloud.api.rest.schema.ovf.vmware.ObjectFactory();
    }

    @Override
    public VcdTaskMonitor getTaskMonitor() {
        return taskMonitor;
    }

    @Override
    public EventViewer getEventViewer() {
        return eventViewer;
    }

    @Override
    public void setClientRequestIdProvider(Supplier<String> clientRequestIdGenerator) {
        super.setClientRequestIdProvider(clientRequestIdGenerator);
    }

    private synchronized Map<String, URI> getQueryListMap() {
        if (queryListMap == null) {
            queryListMap = new HashMap<>();
            for (final LinkType link : getQueryList().getLink()) {
                final String queryListKey = makeQueryListMapKey(link.getType(), link.getName());
                queryListMap.put(queryListKey, URI.create(link.getHref()));
            }
        }

        return queryListMap;
    }


    @Override
    public void setCredentials(ClientCredentials credentials) {
        setCredentialsInternal(credentials);
        if (credentials.supportsSessionless()) {
            doInitClient();
        } else {
            dologinInternal(credentials);
        }
    }

    private final class QueryResultPageIterator<T> implements ListIterator<QueryListPage<T>> {

        private ContainerType container;
        private final Class<T> queryResultClass;
        // boolean indicating whether 'container' has the results which can be returned from next()
        // or whether next() should fetch results by following next-page link in the container
        private boolean nextPageAvailable;

        public QueryResultPageIterator(final ContainerType containerType, final Class<T> queryResultClass) {
            this.container = containerType;
            this.queryResultClass = queryResultClass;
            this.nextPageAvailable = true;
        }

        @Override
        public boolean hasNext() {
            if (nextPageAvailable && container.getTotal() > 0) {
                return true;
            }

            return getNextPageUri() != null;
        }

        @Override
        public QueryListPage<T> next() {

            if (nextPageAvailable) {
                nextPageAvailable = false;
            } else {
                container = getResource(getNextPageUri(), container.getClass());
            }

            return getResultsPage();
        }

        @Override
        public boolean hasPrevious() {
            if (nextPageAvailable) {
                return false;
            }

            return getPrevPageUri() != null;
        }

        @Override
        public QueryListPage<T> previous() {

            container = getResource(getPrevPageUri(), container.getClass());

            return getResultsPage();
        }

        @Override
        public int nextIndex() {
            return container.getPage();
        }

        @Override
        public int previousIndex() {
            // index is zero based, but page numbers start with 1; current index is page-1; therefore previous index is page -2
            return container.getPage()-2;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(QueryListPage<T> e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(QueryListPage<T> e) {
            throw new UnsupportedOperationException();
        }

        private QueryListPage<T> getResultsPage() {
            if (container instanceof QueryResultRecordsType) {
                return extractQueryResults((QueryResultRecordsType)container, queryResultClass);
            } else {
                return extractQueryResults((ReferencesType)container, queryResultClass);
            }
        }

        private <QueryResultClass> QueryListPage<QueryResultClass> extractQueryResults(
                final QueryResultRecordsType queryResultRecordsType,
                Class<QueryResultClass> queryResultClass) {

            final List<QueryResultClass> queryResultRecordList =
                    new ArrayList<>();

            for (JAXBElement<? extends QueryResultRecordType> element : queryResultRecordsType
                    .getRecord()) {
                final QueryResultRecordType queryResultRecord = element.getValue();
                assert queryResultRecord != null : "Record is present but value is null";
                queryResultRecordList.add(queryResultClass.cast(queryResultRecord));
            }

            return new QueryListPage<>(queryResultRecordList,
                    queryResultRecordsType.getTotal());
        }

        private <QueryResultClass> QueryListPage<QueryResultClass> extractQueryResults(
                ReferencesType referencesType, Class<QueryResultClass> queryResultClass) {
            final List<QueryResultClass> referenceList =
                    new ArrayList<>();

            for (JAXBElement<ReferenceType> ref : referencesType.getReference()) {
                final QueryResultClass referenceType = (QueryResultClass) ref.getValue();

                assert referencesType != null : "Reference present but value is null";
                referenceList.add(referenceType);
            }

            return new QueryListPage<>(referenceList,
                    referencesType.getTotal());
        }

        private LinkType getNextPageUri() {
            return VcdUtils.findLink(container, RelationType.NEXT_PAGE, container.getType(), false);
        }


        private LinkType getPrevPageUri() {
            return VcdUtils.findLink(container, RelationType.PREVIOUS_PAGE, container.getType(), false);
        }
    }

    private static String makeQueryListMapKey(String mediaType, String queryTypeName) {
        return mediaType + "!" + queryTypeName;
    }

    private final class TypedQuery<QueryResultClass> extends AbstractQuery<QueryResultClass> {
        private final String queryTypeName;

        public TypedQuery(String queryTypeName, Class<QueryResultClass> queryResultClass) {
            super(queryResultClass);
            this.queryTypeName = queryTypeName;
        }

        @Override
        protected URI findQueryUri(final QueryResultFormat format) {
            final String queryKey = makeQueryListMapKey(format.getMediaType(), queryTypeName);

            final URI queryHref = getQueryListMap().get(queryKey);
            if (queryHref == null) {
                throw new IllegalArgumentException(
                        "No query found with given query format '"
                        + format.getApiString() + "' and name '" + queryTypeName + "'");
            }
            return queryHref;
        }
    }

    private abstract class AbstractQuery<QueryResultClass> implements Query<QueryResultClass> {

        final int DEFAULT_PAGE_SIZE = -1;

        private final Class<QueryResultClass> queryResultClass;
        private QueryResultFormat queryResultFormat;
        private int page = 1;
        private int pageSize = DEFAULT_PAGE_SIZE;
        private boolean includeLinks = false;
        private String filter;
        private String fields;
        private String sortDesc;
        private String sortAsc;

        public AbstractQuery(final Class<QueryResultClass> queryResultClass) {
            this.queryResultClass = queryResultClass;

            if (ReferenceType.class.isAssignableFrom(queryResultClass)) {
                queryResultFormat = QueryResultFormat.REFERENCES;
            } else if (QueryResultRecordType.class.isAssignableFrom(queryResultClass)) {
                queryResultFormat = QueryResultFormat.RECORDS;
            } else {
                throw new IllegalArgumentException("queryResultClass");
            }
        }

        @Override
        public Query<QueryResultClass> setQueryResultFormat(QueryResultFormat queryResultFormat) {
            switch (queryResultFormat) {
            case REFERENCES:
                if (!ReferenceType.class.isAssignableFrom(queryResultClass)) {
                    throw new IllegalArgumentException("queryResultFormat");
                }
                break;
            case RECORDS:
            case ID_RECORDS:
                if (!QueryResultRecordType.class.isAssignableFrom(queryResultClass)) {
                    throw new IllegalArgumentException("queryResultFormat");
                }
                break;
            }

            this.queryResultFormat = queryResultFormat;
            return this;
        }

        @Override
        public Query<QueryResultClass> setPage(int page) {
            this.page = page;
            return this;
        }

        @Override
        public Query<QueryResultClass> setPageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        @Override
        public Query<QueryResultClass> setFilter(String filter) {
            this.filter = filter;
            return this;
        }

        @Override
        public Query<QueryResultClass> setFields(Collection<String> fields) {
            this.fields = fields.stream().collect(Collectors.joining(","));
            return this;
        }

        @Override
        public Query<QueryResultClass> setEqualityFilter(String name, String value) {
            final StringBuffer sb = new StringBuffer();

            if (this.filter != null) {
                sb.append(this.filter);
                sb.append(";");
            }
            sb.append(name);
            sb.append("==");
            sb.append(value);

            this.filter = sb.toString();

            return this;
        }

        @Override
        public Query<QueryResultClass> setSortDesc(String fieldName) {
            this.sortDesc = fieldName;
            return this;
        }

        @Override
        public Query<QueryResultClass> setSortAsc(String fieldName) {
            this.sortAsc = fieldName;
            return this;
        }

        @Override
        public Query<QueryResultClass> setIncludeLinks(boolean includeLinks) {
            this.includeLinks = includeLinks;
            return this;
        }

        @Override
        public ListIterator<QueryListPage<QueryResultClass>> execute() {
            final URI queryUri =
                    buildQueryUri(findQueryUri(queryResultFormat), page, pageSize,
                            filter, includeLinks);


            if (QueryResultRecordType.class.isAssignableFrom(queryResultClass)) {
                final QueryResultRecordsType queryResultRecordsType =
                        getResource(queryUri, QueryResultRecordsType.class);
                return new QueryResultPageIterator<>(
                        queryResultRecordsType, queryResultClass);
            }

            if (ReferenceType.class.isAssignableFrom(queryResultClass)) {
                final ReferencesType referencesType =
                        getResource(queryUri, ReferencesType.class);
                return new QueryResultPageIterator<>(referencesType,
                        queryResultClass);
            }

            assert false : "Unexpected type of query results: " + queryResultClass;
            return null;
        }

        @Override
        public Stream<QueryResultClass> stream() {
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(execute(), Spliterator.ORDERED), false)
                                .flatMap(page -> page.getPage().stream());
        }

        abstract protected URI findQueryUri(final QueryResultFormat format);

        private URI buildQueryUri(final URI baseQueryHref, final int page, final int pageSize,
                final String filter, final boolean includeLinks) {
            final UriBuilder builder = UriBuilder.fromUri(baseQueryHref);
            // AtomicInteger for use in lambda
            final AtomicInteger index = new AtomicInteger(0);
            final Map<String, Object> paramArgs = new HashMap<>();

            final Function<Object, String> wrapArgInTemplate = argValue -> {
                final String arg = String.format("arg%d", index.getAndIncrement());
                if (isZeusOrAbove()) {
                    paramArgs.put(arg, argValue);
                } else {
                    paramArgs.put(arg, ObjectUtils.defaultIfNull(argValue, "").toString()
                            .replaceAll("\\+", "%2B"));
                }
                return String.format("{%s}", arg);
            };

            builder.queryParam("page", wrapArgInTemplate.apply(page));

            if (pageSize != DEFAULT_PAGE_SIZE) {
                builder.queryParam("pageSize", pageSize);
            }

            if (!StringUtils.isEmpty(fields)) {
                builder.queryParam(RestConstants.QueryParams.FIELDS,
                        wrapArgInTemplate.apply(fields));
            }

            boolean isPartiallyEncoded = false;
            if (!StringUtils.isEmpty(filter)) {
                isPartiallyEncoded = HttpUtils.isPartiallyEncoded(filter);
                if (!isZeusOrAbove()) {
                    builder.queryParam("filterEncoded", wrapArgInTemplate.apply(true));
                }
                builder.queryParam(RestConstants.QueryParams.FILTER,
                        wrapArgInTemplate.apply(filter));
            }

            if (!StringUtils.isEmpty(sortAsc)) {
                builder.queryParam("sortAsc", wrapArgInTemplate.apply(sortAsc));
            }

            if (includeLinks) {
                builder.queryParam("links", wrapArgInTemplate.apply(true));
            }

            if (!StringUtils.isEmpty(sortDesc)) {
                builder.queryParam("sortDesc", wrapArgInTemplate.apply(sortDesc));
            }

            return isPartiallyEncoded
                    && isZeusOrAbove()
                    ? builder.buildFromEncodedMap(paramArgs)
                    : builder.buildFromMap(paramArgs);
        }

        @Override
        public QueryResultClass findUnique() {
            final ListIterator<QueryListPage<QueryResultClass>> result = execute();
            if (! result.hasNext()) {
                throw new MissingRecordException(toString());
            }
            final QueryListPage<QueryResultClass> page = result.next();
            final List<QueryResultClass> pageContents = page.getPage();
            final int size = pageContents.size();
            if (size != 1) {
                throw (size > 1) ? new MultipleRecordsException(toString()) : new MissingRecordException(toString());
            }
            return pageContents.get(0);
        }

        @Override
        public String toString() {
            URI u = buildQueryUri(findQueryUri(queryResultFormat), page, pageSize, filter, includeLinks);
            return u.toASCIIString();
        }

    }

    @Override
    public <QueryResultClass> Query<QueryResultClass> getQuery(String queryTypeName, Class<QueryResultClass> queryResultClass) {
        return new TypedQuery<>(queryTypeName, queryResultClass);
    }

    private final class PackagedQuery<QueryResultClass> extends AbstractQuery<QueryResultClass> {
        private final String queryPath;

        public PackagedQuery(String queryPath,
                Class<QueryResultClass> queryResultClass) {
            super(queryResultClass);
            this.queryPath = queryPath;
        }

        @Override
        protected URI findQueryUri(QueryResultFormat format) {
            return UriBuilder.fromUri(endpoint).path(queryPath)
                    .queryParam("format", format.getApiString()).build();
        }
    }

    @Override
    public <QueryResultClass> Query<QueryResultClass> getPackagedQuery(String queryPath, Class<QueryResultClass> queryResultClass) {
        return new PackagedQuery<>(queryPath, queryResultClass);
    }

    @Override
    protected void dologinInternal(ClientCredentials credentials) {
        setCredentialsInternal(credentials);
        WebClient client =
                createWebClient(UriBuilder.fromUri(endpoint).path(RestConstants.Uri.SESSIONS)
                        .build());
        client.header(credentials.getHeaderName(), credentials.getHeaderValue());

        // Initialize this client with the newly constructed session
        initializeWithSession(client, true); /*is login*/
    }

    @Override
    protected void doInitClient() {
        // Construct a web client to retrieve a session based on ClientCredentials or authentication token
        WebClient client =
                createWebClient(UriBuilder.fromUri(endpoint).path(RestConstants.Uri.SESSION)
                        .build());

        // Initialize this client using the existing session
        initializeWithSession(client, false /*do not login*/);
    }

    @Override
    protected void processHeaders(final MultivaluedMap<String, Object> responseHeaders) {
        setAuthenticationToken(
                (String) responseHeaders.getFirst(RestConstants.VCLOUD_AUTHENTICATION_HEADER));
        if (getAuthenticationToken() == null) {
            throw new RuntimeException("The login response is missing a "
                    + RestConstants.VCLOUD_AUTHENTICATION_HEADER + " cookie");
        }
        super.processHeaders(responseHeaders);
    }

    private static String getApiVersion(final List<ApiVersion> candidateVersions,
            final URI endpoint,
            final CxfClientSecurityContext cxfClientSecurityContext) {
        final String minApiVersion = ApiVersion.Alias.MIN_SUPPORTED.getMapping().value();
        final VcdClientImpl minClient =
                new VcdClientImpl(endpoint, minApiVersion, cxfClientSecurityContext);

        final SupportedVersionsType supportedVersions = minClient.getVersions(endpoint);

        final Set<String> cloudSupportedApiVersions = new HashSet<>();

        for (final VersionInfoType versionType : supportedVersions.getVersionInfo()) {
            cloudSupportedApiVersions.add(versionType.getVersion());
        }

        if (supportedVersions.getAlphaVersion() != null) {
            cloudSupportedApiVersions.add(supportedVersions.getAlphaVersion().getVersion());
        }

        for (final ApiVersion candidateVersion : candidateVersions) {
            if (cloudSupportedApiVersions.contains(candidateVersion.value())) {
                return candidateVersion.value();
            }
        }

        throw new RuntimeException(
                "No preferred API Version is supported by cloud. Preferred versions: "
                        + candidateVersions.toString() + "; vCD supported versions:"
                        + cloudSupportedApiVersions.toString() + "; Cloud URL:" + endpoint);
    }

    boolean isSystemClient() {
        return getSession().getOrg().equals("System");
    }

    // TODO: Remove once v35 is no longer supported, see VTEN-3875
    private boolean isZeusOrAbove() {
        if (isZeusOrAbove == null) {
            final String multisiteRequest = getMultisiteLocations();
            setMultisiteRequests(false);
            isZeusOrAbove = getVersions().getVersionInfo()
                    .stream()
                    .map(VersionInfoType::getVersion)
                    .map(this::backwardsCompatSafeApiVersion)
                    .max(ApiVersion::compareTo)
                    .get()
                    .isAtLeast(ApiVersion.Alias.FILTER_ENCODED_REMOVED);
            setMultisiteLocationHeaderValue(multisiteRequest);
        }
        return isZeusOrAbove;
    }

    private ApiVersion backwardsCompatSafeApiVersion(String v) {
        final ApiVersion apiVersion;
        try {
            apiVersion = ApiVersion.fromValue(v);
        } catch (IllegalArgumentException e) {
            return ApiVersion.VERSION_MAX;
        }

        return apiVersion;
    }

    /**
     * This class definition is necessary because the no-arg constructor in SimpleBeanPropertyFilter
     * is protected, so it can't be directly instantiated in this class.
     *
     */
    private static class VcloudBeanPropertyFilter extends SimpleBeanPropertyFilter {

    }
}

