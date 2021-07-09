/*
 * !!! NOTE !!!
 *
 * Before editing this file in Eclipse, strongly recommend copying it to a simple text editor.
 * When you ask Eclipse to reformat your edited comments, all code samples will lose their
 * pre-formatting and it will be helpful to recover it from your text editor copy.
 */

/**
 * Contains necessary client(s) to facilitate API calls to vCloud Director
 * <P>
 * The starting point for all accesses is the {@link com.vmware.vcloud.api.rest.client.VcdClient}.
 * <P>
 * <B>Vcd Client</B><BR>
 * ==========<BR>
 * The supplied concrete implementation ({@link com.vmware.vcloud.api.rest.client.VcdClientImpl})
 * provides the necessary implementation and is correctly initialized using vCloud Director XML
 * Schemas and can handle the necessary marshalling/un This class serves as a java version of an
 * HTTP request tool (a-la Postman) customized to ensure correct communication with vCloud Director
 * by managing the necessary session, API versioning and other vCloud Director specific state. Once
 * correctly instantiated it allows the user to make the necessary HTTP calls ({@code GET},
 * {@code PUT}, {@code POST} and {@code DELETE} ). The response can be a returned as raw
 * {@link javax.ws.rs.core.Response} object or a parsed {@link javax.xml.bind.JAXBElement} utilizing
 * its familiarity with vCloud Director schema.
 * <P>
 * The client provides further support for interpreting REST navigational links embedded in each
 * response to speedily make subsequent calls and allows exploring the entire data model in a
 * RESTful fashion.
 * <P>
 * <U>Usage examples:</U><BR>
 * TBA
 * <P>
 * <B>Open API Client</B><BR>
 * ===============<BR>
 * An Open API client interacts with vCloud Director's endpoint that serves functionality using the
 * OpenAPI specification. This client supports both {@code JAX-RS} and {@code RESTful} calls to
 * VCD's {@code /cloudapi} endpoints
 * <P>
 * Unlike {@link com.vmware.vcloud.api.rest.client.VcdClientImpl}, which is directly instantiated,
 * an instance of {@link com.vmware.vcloud.api.rest.client.OpenApiClientImpl} can <i>only</i> be
 * acquired from {@link VcdClient#getOpenApiClient()} call. This ensures that both client's share
 * the necessary authentication and other internal state. Once initialized, this client is aware of
 * vCloud Director's OpenAPI compliant API definitions and manages the generation of necessary Java
 * JAX-RS stubs, serialization and de-serialization of JSON model objects and other HTTP compliance.
 * <P>
 * <U>Usage examples:</U> For each of the following examples, {@code ApiModel} represents the data
 * communicated with vCD via {@code ApiInterface} which provides the necessary CRUD methods for the
 * model. {@code ApiInterface} is assumed to be appropriately annotated with necessary JAX-RS
 * annotations.
 * <ol>
 * <li>JAX-RS usage:<BR>
 * Begin by acquiring a CXF Proxy for the the interface and then invoke the necessary API calls as
 * normal java calls. The CXF Proxy will handle the rest of the mechanics to communicate with vCloud
 * Director
 * <ol type="a">
 * <li>Simplest case<BR>
 * This example demonstrates making a Java API call to invoke the corresponding call on vCD.
 *
 * <pre>
 *         final OpenApiClient openApiClient = authenticatedVcdClient.getOpenApiClient();
 *         final ApiInterface api = openApiClient.createProxy(ApiInterface.class);
 *         final ApiModel model = api.getApiModel();
 *
 *         // model.update();
 *
 *         final ApiModel updatedModel = api.putModel(model);
 * </pre>
 *
 * All necessary headers will be configured as per the presence of
 * {@link javax.ws.rs.Produces @Produces}/{@link javax.ws.rs.Consumes @Consumes}</li></li> and other
 * similar JAX-RS headers
 *
 * <li>Configuring Requests<BR>
 * Before making a call, you can acquire a CXF WebClient that will allow you to utilize
 * {@link org.apache.cxf.jaxrs.client.Client} supplied conveniences to override headers as required.
 *
 * <pre>
 *         final OpenApiClient openApiClient = authenticatedVcdClient.getOpenApiClient();
 *         final ApiInterface api = openApiClient.createProxy(ApiInterface.class);
 *
 *         // Get hold of CXF Web
 *         final Client clientForCurrentCall  = openApiClient.getWebClientForNextCall(api);
 *         client.accept("*&sol;*");
 *         final ApiModel model = api.getApiModel();
 * </pre>
 *
 * CXF wire logging confirms that the Accept header used is <code>*&sol;*</code> instead of the
 * value specified in the {@link javax.ws.rs.Consumes @Consumes} annotation for the getter.</li>
 * <li>Accessing Response - <I>Old</I><BR>
 * {@link javax.ws.rs.core.Response} is also accessed via the CXF
 * {@link org.apache.cxf.jaxrs.client.Client} that must be retrieved <em>before</em> invoking the
 * API call. After the call completes, {@link org.apache.cxf.jaxrs.client.Client#getResponse()} will
 * have the raw {@link javax.ws.rs.core.Response} object that was received by the underlying JAX-RS
 * framework
 *
 * <pre>
 *         final OpenApiClient openApiClient = authenticatedVcdClient.getOpenApiClient();
 *         final ApiInterface api = openApiClient.createProxy(ApiInterface.class);
 *
 *         // Get hold of CXF Web
 *         final Client clientForCurrentCall  = openApiClient.getWebClientForNextCall(api);
 *         final ApiModel model = api.getApiModel();
 *
 *         // Retrieve the response and verify the status code.
 *         final Response response = clientForCurrentCall.getResponse();
 *         System.out.println("Response status code: " + response.getStatus());
 * </pre>
 *
 * <P>This style is handy if the client was already accessed for configuring the request. You can
 * continue to utilize it to retrieve a response.
 * </li>
 * <li><B>***NEW***</B> Accessing Response<BR>
 * After invoking an API call, various entities of a call may be accessed via a series of convenience
 * methods.
 *
 * <pre>
 *         final OpenApiClient openApiClient = authenticatedVcdClient.getOpenApiClient();
 *         final ApiInterface api = openApiClient.createProxy(ApiInterface.class);
 *
 *         // Invoke an API
 *         final ApiModel model = api.getApiModel();
 *
 *         // Retrieve the status code.
 *         final StatusType statusType = openApiClient.getLastStatus();
 *         System.out.println("Response status code: " + statusType.getStatusCode());
 *
 *         // Retrieve the content-type code.
 *         final Collection<javax.ws.rs.core.Link>  = openApiClient.getLastLinks();
 *
 *         // Retrieve the links.
 *         final StatusType statusType = openApiClient.getLastContentType();
 *         System.out.println("Response status code: " + statusType.getStatusCode());
 *
 *         // Retrieve task, if any
 *         final URI taskUri = openApiClient.getLastTaskURI();
 *         if (taskUri != null) {
 *             final TaskType task = openApiClient.getLastTask();
 *         }
 *
 *         // ... or get the whole response
 *         final Response response  = openApiClient.getLastResponse(api);
 *         final ApiModel model = api.getApiModel();
 *
 *         // When ready, invoke a different API
 *         final ApiModel2 model2 = api.getApiModel2();
 * </pre>
 *
 * <P>If the API call throws an error, you can retrieve the parsed Error object as such:
 *
 * <pre>
 *         final OpenApiClient openApiClient = authenticatedVcdClient.getOpenApiClient();
 *         final ApiInterface api = openApiClient.createProxy(ApiInterface.class);
 *
 *         try {
 *             // Invoke an API
 *             final ApiModel model = api.getApiModel();
 *         } catch (WebApplicationException wae) {
 *             final Error error = openApiClient.getLastError(api);
 *             // do error handling
 *         }
 * </pre>
 * </li>
 * <li>Putting together a workflow<BR>
 * Above components can be put together to produce a workflow that resembles a normal
 * <code>Java</code> method than a client implementation
 *
 * <pre>
 *         final OpenApiClient openApiClient = authenticatedVcdClient.getOpenApiClient();
 *         final ApiInterface api = openApiClient.createProxy(ApiInterface.class);
 *
 *         // Get
 *         final ApiModel model = api.getApiModel();
 *         // Check content-type
 *         Assert.assertEquals(openApiClient.getLastContentType(api), "application/json");
 *
 *         // Invoke a task
 *         final byte[] binaryData = getSomeData();
 *
 *         final Client clientForTask  = openApiClient.getWebClientForNextCall(api);
 *         clientForTask.accept(MediaType.APPLICATION_OCTET_STREAM);
 *         api.postAction(model, binaryData);
 *
 *         // Old-style accessing task.
 *         final Response taskResponse = clientForTask.getResponse();
 *         Assert.assertEquals(response.getStatus(), Response.Status.ACCEPTED);
 *
 *         // Update
 *         model.setXYZ(newXyz);
 *         api.putModel(model);
 *
 *         // New-style accessing task and status.
 *         Assert.assertNotNull(openApiClient.getLastTaskUri(api));
 *         final Response taskResponse = openApiClient.getLastTask(api);
 *         Assert.assertEquals(openApiClient.getLastStatus(api), Response.Status.ACCEPTED);
 *
 *         // And finally delete
 *         try {
 *             api.deleteModel(updatedModel);      // returns void.
 *             Assert.assertEquals(openApiClient.getLastStatus(api), Response.Status.NO_CONTENT);
 *         } catch (javax.ws.rs.WebApplicationException wae) {
 *             final Error error = openApiClient.getLastError(api);
 *             System.err.println(error.getMessage());
 *             throw wae;
 *         }
 * </pre>
 *
 * As can be seen, the same interface proxy can be used for any number of calls. Underlying client
 * will ensure any Client-based customization is reset between calls.</li>
 * </ol>
 * <li>RESTful usage:
 * <ol type="a">
 * TBA
 * </ol>
 * </li>
 * </ol>
 *
 */
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

