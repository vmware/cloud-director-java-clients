package com.vmware.vcloud.object.extensibility.vcd;

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

/**
 * Interface that defines the properties that are needed to connect to the vCloud
 * Director API.
 */
public interface ApiConnectionInfo {
    /**
     * Gets the {@link URI} endpoint of the vCloud Director API.
     *
     * @return a {@link URI} endpoint
     */
    URI getEndpoint();

    /**
     * Gets the security context to use for communicating with the vCloud Director
     * API via HTTPS.
     *
     * @return a security context instance
     */
    CxfClientSecurityContext getSecurityContext();

    /**
     * Gets the version of the vCloud Director API preferred by the API client.
     *
     * @return an API version string
     */
    String getVersion();
}
