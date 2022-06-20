/* **********************************************************************
 * Copyright 2011-2013 VMware, Inc.  All rights reserved. VMware Confidential
 * *********************************************************************/

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