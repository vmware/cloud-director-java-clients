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

import java.text.MessageFormat;

/**
 * Bearer Token credentials suitable for use in authenticating with a vCD server using the
 * vCloud API.
 *
 * @since 9.0
 */
public final class VcdBearerLoginCredentials implements ClientCredentials {

    private final String bearerAuthenticationHeader;

    /**
     * Construct a {@link VcdBearerLoginCredentials} object using an OAuth Bearer token and VCD org name
     *
     * @param oAuthBearerToken
     *            Bearer Token supplied by an OAuth IDP
     * @param org
     *            The VCD organization name.
     */
    public VcdBearerLoginCredentials(final String oAuthBearerToken, final String org) {
        bearerAuthenticationHeader =
                MessageFormat.format("Bearer {0};org={1}", oAuthBearerToken, org);
    }

    @Override
    public String getHeaderValue() {
        return bearerAuthenticationHeader;
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

