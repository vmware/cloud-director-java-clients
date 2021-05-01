/* **********************************************************
 * api-extension-template-vcloud-director
 * Copyright 2017 -2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 * **********************************************************/

package com.vmware.vcloud.api.rest.client;

import java.net.URI;
import java.util.Collection;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import com.vmware.cxfrestclient.JaxRsClient;
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
    <JaxRsClass> Collection<Link> getLastLinks(JaxRsClass proxy);

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
}

