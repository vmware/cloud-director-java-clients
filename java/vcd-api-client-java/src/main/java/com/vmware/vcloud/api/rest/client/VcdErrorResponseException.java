
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

import javax.ws.rs.WebApplicationException;

import com.vmware.vcloud.api.rest.schema_v1_5.ErrorType;

/**
 * Exception for encapsulating errors returned by VCD as Jaxb {@link ErrorType} object
 *
 */
public class VcdErrorResponseException extends VcdErrorException {
    private static final long serialVersionUID = 1L;

    private final ErrorType vcdError;
    private final String requestId;
    private final String errorMsg;

    public VcdErrorResponseException(int httpStatusCode, final String requestId,
            ErrorType vcdError, WebApplicationException cause) {
        super(httpStatusCode, cause);
        this.requestId = requestId;
        this.vcdError = vcdError;
        this.errorMsg = vcdError != null ? vcdError.getMessage() : null;
    }

    public VcdErrorResponseException(int httpStatusCode, final String requestId,
            String vcdError, WebApplicationException cause) {
        super(httpStatusCode, cause);
        this.requestId = requestId;
        this.errorMsg = vcdError;
        this.vcdError = null;
    }

    /**
     *
     * @return Jaxb {@link ErrorType} object representing the error returned by VCD as defined by
     */
    public final ErrorType getVcdError() {
        return vcdError;
    }

    /**
     * @return ID of the failed request
     */
    public final String getRequestId() {
        return requestId;
    }

    @Override
    public String toString() {
        return String.format("[VcdErrorResponseException] %s\n" +
                        "Server stack trace: %s",
                        getMessage(), (vcdError == null) ? errorMsg : vcdError.getStackTrace());
    }

    @Override
    public String getMessage() {
        return String.format("HTTP status code = %d\n" +
                        "Request ID: %s\n" +
                        "VCD Error: %s\n" +
                        "VCD ErrorType: major error code = %d, minor error code = %s",
                getHttpStatusCode(),
                requestId,
                (vcdError == null) ? errorMsg : vcdError.getMessage(),
                (vcdError == null) ? 0 : vcdError.getMajorErrorCode(),
                (vcdError == null) ? "-" : vcdError.getMinorErrorCode());
    }
}


