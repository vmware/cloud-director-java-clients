
package com.vmware.vcloud.api.rest.client.filters;

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
import java.net.URI;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MultivaluedMap;

import com.vmware.vcloud.api.rest.client.VcdMultisiteLoginCredentials;

/**
 *
 * Filter for intercepting outgoing requests and adding the Multisite authorization header to them
 *
 * @since 8.22
 */
public class MultisiteAuthorizationFilter implements ClientRequestFilter {

    private final VcdMultisiteLoginCredentials credentials;

    public MultisiteAuthorizationFilter(final VcdMultisiteLoginCredentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public void filter(final ClientRequestContext requestContext) throws IOException {

        final String date = getDate();
        final String method = requestContext.getMethod();
        final URI requestUri = requestContext.getUri();
        final String path = requestUri.getPath();
        final String contentType = requestContext.getHeaderString("Content-Type");

        final String authHeader =
                credentials.createMultisiteAuthorizationHeader(date, method, path, contentType);
        final MultivaluedMap<String, Object> headers = requestContext.getHeaders();
        headers.putSingle("Authorization", authHeader);
        headers.putSingle("Date", date);
    }

    /**
     * Gets the current UTC date time in RFC 1123 format (e.g. "Tue, 3 Jun 2008 11:05:30 GMT")
     *
     * This format is used as specified in the HTTP/1.1 rfc:
     * https://tools.ietf.org/html/rfc2616#page-20
     *
     * @return date
     */
    private String getDate() {
        final ZonedDateTime utc = ZonedDateTime.now(ZoneId.of("UTC"));
        return DateTimeFormatter.RFC_1123_DATE_TIME.format(utc);
    }

}


