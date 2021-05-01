/* **********************************************************
 * api-extension-template-vcloud-director
 * Copyright 2017-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 * **********************************************************/

package com.vmware.vcloud.api.rest.client.filters;

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

