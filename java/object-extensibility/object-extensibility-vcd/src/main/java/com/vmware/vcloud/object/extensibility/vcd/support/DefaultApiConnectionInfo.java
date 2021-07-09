package com.vmware.vcloud.object.extensibility.vcd.support;

/*-
 * #%L
 * object-extensibility-vcd :: Object Extension vCD client
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

import com.vmware.cxfrestclient.CxfClientSecurityContext;
import com.vmware.vcloud.object.extensibility.vcd.ApiConnectionInfo;

/**
 * Default implementation of the {@link ApiConnectionInfo} interface.
 */
public class DefaultApiConnectionInfo implements ApiConnectionInfo {
    private final URI endpoint;
    private final String version;
    private final CxfClientSecurityContext securityContext;

    public DefaultApiConnectionInfo(final URI endpoint, final String version) {
        this.endpoint = endpoint;
        this.version = version;
        this.securityContext = CxfClientSecurityContext.getDefaultCxfClientSecurityContext();
    }

    @Override
    public URI getEndpoint() {
        return endpoint;
    }

    @Override
    public CxfClientSecurityContext getSecurityContext() {
        return securityContext;
    }

    @Override
    public String getVersion() {
        return version;
    }
}

