/*-
 * #%L
 * vcd-api-client-java :: Implementation of VCD schemas
 * %%
 * Copyright (C) 2022 VMware, Inc.
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


package com.vmware.vcloud.api.rest.client;

import org.apache.cxf.common.util.Base64Utility;

/**
 * Username/password Credentials suitable for use in authenticating with a vCD Appliance using the
 * Appliance API
 */
public class ApplianceBasicLoginCredentials implements ClientCredentials {

    private final String authorizationHeader;


    /**
     * Construct credentials from a valid vCD appliance username and a password.
     */
    public ApplianceBasicLoginCredentials(String userName, String password) {
        this(userName + ":" + password);
    }

    private ApplianceBasicLoginCredentials(String userString) {
        authorizationHeader = "Basic " + Base64Utility.encode(userString.getBytes());
    }

    @Override
    public boolean equals(Object obj) {
        return authorizationHeader.equals(obj);
    }

    @Override
    public int hashCode() {
        return authorizationHeader.hashCode();
    }

    @Override
    public String getHeaderValue() {
        return authorizationHeader;
    }

    @Override
    public String getHeaderName() {
        return "Authorization";
    }

    @Override
    public boolean supportsSessionless() {
        return true;
    }

}