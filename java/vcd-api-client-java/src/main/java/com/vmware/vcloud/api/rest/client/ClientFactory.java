/* ***************************************************************************
 * Copyright 2019 VMware, Inc.  All rights reserved. VMware Confidential
 * **************************************************************************/
package com.vmware.vcloud.api.rest.client;

import java.net.URI;
import java.util.Arrays;

import javax.ws.rs.core.UriBuilder;

import com.vmware.cxfrestclient.CxfClientSecurityContext;

import org.apache.commons.lang3.Validate;

/**
 * Factory that creates initialized OpenApiClient and VcdClient instances.
 *
 * The Credentials specified when creating an OpenApiClient are immutable: They are the same for the
 * lifetime of the client instance.
 *
 * If you need an OpenApiClient instance with a set of Credentials that differs from those in an
 * existing OpenApiClient instance, create a new instance with the desired Credentials by using one
 * of the factory methods in this class.
 *
 * There is no need to immediately login after creating an OpenApiClient instance. A login is
 * performed as part of the creation process so that clients with invalid credentials fail fast.
 */
public class ClientFactory {

    /**
     * Creates an initialized OpenApiClient for a non-provider user
     */
    public static OpenApiClient createOpenApiClient(final URI openApiEndpoint,
            final String apiVersion, final CxfClientSecurityContext cxfClientSecurityContext,
            final ClientCredentials credentials) {
        Validate.noNullElements(
                Arrays.asList(openApiEndpoint, apiVersion, cxfClientSecurityContext, credentials),
                "One or more specified arguments are null");
        return new OpenApiClientImpl(openApiEndpoint, apiVersion,
                cxfClientSecurityContext, credentials, false);
    }

    /**
     * Creates an initialized OpenApiClient for a provider user
     */
    public static OpenApiClient createOpenApiClientForProvider(final URI openApiEndpoint,
            final String apiVersion, final CxfClientSecurityContext cxfClientSecurityContext,
            final ClientCredentials credentials) {
        Validate.noNullElements(
                Arrays.asList(openApiEndpoint, apiVersion, cxfClientSecurityContext, credentials),
                "One or more specified arguments are null");
        return new OpenApiClientImpl(openApiEndpoint, apiVersion,
                cxfClientSecurityContext, credentials, true);
    }

    /**
     * Creates an OpenApiClient linked to the session associated with the jwtToken
     */
    public static OpenApiClient createOpenApiClientWithJwt(final URI openApiEndpoint,
            final String apiVersion, final CxfClientSecurityContext cxfClientSecurityContext,
            final String jwtToken) {
        Validate.noNullElements(
                Arrays.asList(openApiEndpoint, apiVersion, cxfClientSecurityContext, jwtToken),
                "One or more specified arguments are null");
        return new OpenApiClientImpl(openApiEndpoint, apiVersion,
                cxfClientSecurityContext, jwtToken);
    }

    /**
     * Creates an OpenApiClient linked to the session associated with the jwtToken and the specified
     * organization context header
     */
    public static OpenApiClient createOpenApiClientWithJwt(final URI openApiEndpoint,
            final String apiVersion,
            final CxfClientSecurityContext cxfClientSecurityContext,
            final String jwtToken, final String orgContextHeader) {
        Validate.noNullElements(Arrays.asList(openApiEndpoint, apiVersion, cxfClientSecurityContext,
                jwtToken, orgContextHeader), "One or more specified arguments are null");
        final OpenApiClientImpl clientImpl = new OpenApiClientImpl(openApiEndpoint, apiVersion,
                cxfClientSecurityContext,
                        jwtToken);
        clientImpl.setAuthContextHeader(orgContextHeader);
        return clientImpl;
    }


    /**
     * Creates an OpenApiClient linked to the initialized VcdClient: Both clients share the same
     * {@link com.vmware.vcloud.api.rest.client.AbstractVcdClientBase.UserSecurityContext}
     */
    public static OpenApiClient createOpenApiClient(final VcdClientImpl vcdClient) {
        Validate.notNull(vcdClient, "vcdClient cannot be null");
        try {
            if (vcdClient.isSystemClient() && vcdClient.getCredentials() != null) {
                vcdClient.getUserSecurityContext()
                        .setClientCredentials(new CredentialsWrapper(vcdClient.getCredentials()) {
                    @Override
                    public boolean isProvider() {
                        return true;
                    }

                });
            }

        } catch (IllegalStateException e) {
            throw new IllegalStateException("This operation requires an initialized VcdClient");
        }
        final OpenApiClientImpl clientImpl = new OpenApiClientImpl(vcdClient.getOpenApiEndpoint(),
                vcdClient.getClientApiVersion(), vcdClient.getClientSecurityContext());
        clientImpl.setUserSecurityContext(vcdClient.getUserSecurityContext());
        return clientImpl;
    }

    /**
     * Creates a VcdClient linked to the OpenApiClient: Both clients share the same
     * {@link com.vmware.vcloud.api.rest.client.AbstractVcdClientBase.UserSecurityContext}
     */
    public static VcdClient createVcdClient(final OpenApiClient openApiClient) {
        Validate.notNull(openApiClient, "openApiClient cannot be null");
        final OpenApiClientImpl openApiClientImpl = (OpenApiClientImpl) openApiClient;
        final URI endpoint =
                UriBuilder.fromUri(openApiClientImpl.getEndpoint()).replacePath("api").build();
        final String apiVersion = openApiClientImpl.getClientApiVersion();
        final CxfClientSecurityContext cxfSecurityContext =
                openApiClientImpl.getClientSecurityContext();
        final VcdClientImpl vcdClient = new VcdClientImpl(endpoint, apiVersion, cxfSecurityContext);
        vcdClient.setUserSecurityContext(openApiClientImpl.getUserSecurityContext());
        //If this is an anonymous OpenApiClient, skip doInitClient
        if (!(openApiClientImpl.getCredentials() == null
                && openApiClientImpl.getJwtToken() == null)) {
            vcdClient.doInitClient();
        }
        return vcdClient;
    }

    /**
     * Creates an initialized VcdClient
     */
    public static VcdClient createVcdClient(final URI vcdApiEndpoint,
            final String apiVersion, final CxfClientSecurityContext cxfClientSecurityContext,
            final ClientCredentials credentials) {
        Validate.noNullElements(
                Arrays.asList(vcdApiEndpoint, apiVersion, cxfClientSecurityContext, credentials),
                "One or more specified arguments are null");
        final VcdClientImpl vcdClient = new VcdClientImpl(vcdApiEndpoint, apiVersion,
                cxfClientSecurityContext);
        vcdClient.setCredentials(credentials);
        return vcdClient;
    }


    /**
     * Create an anonymous OpenApiClient
     */
    public static OpenApiClient createAnonymousOpenApiClient(final URI openApiEndpoint,
            final String apiVersion, final CxfClientSecurityContext cxfClientSecurityContext) {
        Validate.noNullElements(
                Arrays.asList(openApiEndpoint, apiVersion, cxfClientSecurityContext),
                "One or more specified arguments are null");
        return new OpenApiClientImpl(openApiEndpoint, apiVersion,
                cxfClientSecurityContext);
    }
}
