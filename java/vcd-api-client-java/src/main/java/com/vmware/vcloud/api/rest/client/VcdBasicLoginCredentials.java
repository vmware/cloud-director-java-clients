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

import org.apache.cxf.common.util.Base64Utility;

/**
 * Username@Org/password Credentials suitable for use in authenticating with a vCD server using the
 * vCloud API
 */
public class VcdBasicLoginCredentials implements ClientCredentials {

    private final String authorizationHeader;

    /**
     * Construct credentials from a valid vCD organization qualified username  (username@orgname)
     * and a password.
     */
    public VcdBasicLoginCredentials(String userNameAtOrg, String password) {
        this(userNameAtOrg + ":" + password);
    }

    /**
     * Construct credentials from a valid vCD user and org names and a password.
     */
    public VcdBasicLoginCredentials(String userName, String orgName, String password) {
        this(userName + "@" + orgName + ":" + password);
    }

    private VcdBasicLoginCredentials(String userString) {
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
        return false;
    }


}

