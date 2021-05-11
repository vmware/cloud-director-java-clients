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
import java.util.List;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import com.vmware.cxfrestclient.JaxRsClient;
import com.vmware.vcloud.api.rest.constants.RestConstants;
import com.vmware.vcloud.api.rest.schema_v1_5.TaskType;
import com.vmware.vcloud.rest.openapi.model.Error;

import org.apache.cxf.jaxrs.client.Client;

/**
 * REST API client built on OpenAPI framework. Extends {@link JaxRsClient} with behaviors specific
 * to vCloud's Open API.
 * <P>
 * Package {@code com.vmware.vcloud.api.rest.client} Javadoc for detailed description for various
 * usage patterns
 *
 * @see com.vmware.vcloud.api.rest.client
 *
 */
public interface OpenApiClient extends JaxRsClient {

    /**
     * Returns the underlying {@link Client} for next call only.
     * <P>
     * Before invoking the next call, use the returned {@link Client} to manipulate the request.
     * After invocation, the client may be used to it to access the raw {@link Response} object. If
     * not manipulating the request, you may also consider one of the {@code getLastXXX} methods to
     * access various Response artifacts.
     *
     * @param proxy
     *            Proxy of an open API interface acquired from {@link #createProxy(Class)} call
     *
     * @return {@link Client} for the next call invoked.
     *
     * @see #getLastResponse(Object)
     * @see #getLastVcdError(Object)
     * @see #getLastStatus(Object)
     * @see #getLastTaskUri(Object)
     * @see #getLastTask(Object)
     */
    <JaxRsClass> Client getWebClientForNextCall(JaxRsClass proxy);

    /**
     * Returns the {@link Response} object from the last API call made.
     *
     * @param proxy
     *            Proxy of an open API interface acquired from {@link #createProxy(Class)} call upon
     *            which an API call was invoked, whose {@link Response} object is needed.
     * @return {@link Response} object representing successful or error response received from vCD
     */
    <JaxRsClass> Response getLastResponse(JaxRsClass proxy);

    /**
     * Returns the parsed {@link Error} if last API call completed with a failure.
     *
     * @param proxy
     *            Proxy of an open API interface acquired from {@link #createProxy(Class)} call upon
     *            which an API call was invoked, whose {@link Response} object is needed.
     * @return {@link Error} that was parsed, otherwise {@code null}
     */
    <JaxRsClass> Error getLastVcdError(JaxRsClass proxy);

    /**
     * Returns the URI of the task if the last API call completed with {@link Status#ACCEPTED
     * response status 202}
     *
     * @param proxy
     *            Proxy of an open API interface acquired from {@link #createProxy(Class)} call upon
     *            which an API call was invoked, whose {@link Response} object is needed.
     * @return {@link URI} of the task or {@code null} if no task was returned.
     */
    <JaxRsClass> URI getLastTaskUri(JaxRsClass proxy);

    /**
     * If the last API call completed with {@link Status#ACCEPTED response status 202}, retrieve and
     * return {@link TaskType the corresponding vCD Task}
     *
     * @param proxy
     *            Proxy of an open API interface acquired from {@link #createProxy(Class)} call upon
     *            which an API call was invoked, whose {@link Response} object is needed.
     * @return {@link TaskType task} from the previous call or {@code null} if no task was returned.
     */
    <JaxRsClass> TaskType getLastTask(JaxRsClass proxy);

    /**
     * Returns the {@link StatusType HTTP response status} for the last API call invoked on the
     * proxy.
     *
     * @param proxy
     *            Proxy of an open API interface acquired from {@link #createProxy(Class)} call upon
     *            which an API call was invoked, whose {@link Response} object is needed.
     * @return {@link StatusType HTTP response status} for the last API invocation
     */
    <JaxRsClass> StatusType getLastStatus(JaxRsClass proxy);

    /**
     * Returns links returned via the {@link Link} header from the last API call.
     *
     * @param proxy
     *            Proxy of an open API interface acquired from {@link #createProxy(Class)} call upon
     *            which an API call was invoked, whose {@link Response} object is needed.
     * @return links retrieved from the {@link Link} header from the last API call.
     */
    <JaxRsClass> Set<Link> getLastLinks(JaxRsClass proxy);

    /**
     * Returns the response {@link HttpHeaders#CONTENT_TYPE Content-Type} header value, if available
     *
     * @param proxy
     *            Proxy of an open API interface acquired from {@link #createProxy(Class)} call upon
     *            which an API call was invoked, whose {@link Response} object is needed.
     * @return the last response's {@link HttpHeaders#CONTENT_TYPE Content-Type} value if present,
     *         else {@code null}
     */
    <JaxRsClass> String getLastContentType(JaxRsClass proxy);

    /**
     * Convenience method to resolve an entity within the OpenAPI. Returns generic java
     * object to be cast to the appropriate entityType by the user by calling
     * {@link #getClass()} on the objected returned.
     *
     * @param urn the URN of the entity to be resolved
     * @return the entity object resolved
     * @throws WebApplicationException when the entity is not found or cannot be resolved
     * @throws AssertionError when unable to find entity type class in open api model package
     */
    Object resolveEntity(String urn);

    /**
     * Controls whether requests made with this client request multisite behavior or not.
     *
     * @param shouldGlobalFanout
     *            True for global fanout, False for no fanout
     *
     * @deprecated This method is deprecated in favor of
     *             {@link OpenApiClient#setMultisiteLocations(List)} and
     *             {@link OpenApiClient#setMultisiteLocationHeaderValue(String)} which provide more
     *             control over the same setting
     *
     */
    @Deprecated
    void setMultisiteRequests(boolean shouldFanout);

    /**
     * A string with {@link RestConstants#MULTISITE_ATTR_GLOBAL},
     * {@link RestConstants#MULTISITE_ATTR_LOCAL}, or a number of locationIds separated by
     * {@link RestConstants#MULTISITE_ATTR_SEPARATOR}. Pass {@code null} to turn multisite off. Can
     * also be set by {@link OpenApiClient#setMultisiteLocations(List)}
     *
     */
    public void setMultisiteLocationHeaderValue(String multisiteLocations);

    /**
     * Sets the locationIds in the accept header for multisite fanout. Can also be set by
     * {@link OpenApiClient#setMultisiteLocationHeaderValue(String)}
     *
     * @param multisiteLocations
     *            List of locationIds or {@code null} to turn multisite off
     */
    public void setMultisiteLocations(List<String> multisiteLocations);

    /**
     * Get the multisite locations (if any) set for this client
     */
    public String getMultisiteLocations();

    /**
     * Sets the X-VMWARE-VCLOUD-TENANT-CONTEXT header to the specified value
     */
    void setTenantContextHeader(String tenantContext);

    /**
     * Get the current JWT for this client. This can be used to attach a new {@code OpenApiClient}
     * to an existing logged in session and for making a request to a different, associated, org.
     *
     * @return The current JWT for this client. {@code null} if there is none.
     */
    String getJwtToken();

    /**
     * Create a new session using the Credentials associated with this instance. Credentials are
     * associated with an instance of {@code OpenApiClient} when one is created by invoking a
     * {@link ClientFactory} method.
     *
     * Credentials are immutable. If you want to create a session with another set of Credentials,
     * use {@link ClientFactory} to create another {@code OpenApiClient} with this set of
     * Credentials
     *
     * @throws RuntimeException
     *             when Credentials are null.
     */
    void reLogin();

    /**
     * Terminate the current session
     */
    void logout();

}

