
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

import com.vmware.vcloud.api.rest.schema_v1_5.ErrorType;
import com.vmware.vcloud.api.rest.schema_v1_5.ReferenceType;

/**
 * Exception thrown when task failed to complete.
 */
public class VcdTaskException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final ReferenceType owner;
    private final ErrorType error;
    private final String errorMessage;

    public VcdTaskException(ReferenceType owner, final String errorMessage, final ErrorType error) {
        this.owner = owner;
        this.errorMessage = errorMessage;
        this.error = error;
    }

    public ReferenceType getOwner() {
        return owner;
    }

    /**
     * @return the value of error property.
     */
    public ErrorType getError() {
        return error;
    }

    /**
     * @return error message.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return String.format("[VcdTaskException] %s\n" +
                        "Server stack trace: %s",
                        getMessage(), (error == null) ? null : error.getStackTrace());
    }

    @Override
    public String getMessage() {
        return String.format("VCD Error: %s\n" +
                        "VCD ErrorType: major error code = %d, minor error code = %s",
                (error == null) ? null : error.getMessage(),
                (error == null) ? 0 : error.getMajorErrorCode(),
                (error == null) ? "-" : error.getMinorErrorCode());
    }
}


