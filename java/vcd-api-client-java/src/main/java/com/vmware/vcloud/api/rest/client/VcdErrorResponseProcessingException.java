
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

/**
 * Exception to note that error returned by VCD cannot be processed. The original exception in its
 * raw form and the processing error are both captured
 *
 */
public class VcdErrorResponseProcessingException extends VcdErrorException {
    private static final long serialVersionUID = 1L;
    private final Exception processingException;

    public VcdErrorResponseProcessingException(int httpStatusCode, Exception processingException,
            WebApplicationException cause) {
        super(httpStatusCode, cause);
        this.processingException = processingException;
    }

    public final Exception getErrorResponseProcessingException() {
        return processingException;
    }

    @Override
    public String toString() {
        return String.format("[VcdErrorResponseProcessingException] HTTP Status Code=%d\n" +
                        "Processing Exception %s\n" +
                        "while processing VCD Exception received: %s",
                        getHttpStatusCode(),
                        processingException == null ? null : processingException.toString(),
                        getCause() == null ? null : getCause().toString());
    }

    @Override
    public String getMessage() {
        return String.format("Exception occured while processing Error response received from VCD.\n" +
                        "HTTP Status Code: %d, Original error: %s\n" +
                        "Processsing Error: %s",
                        getHttpStatusCode(),
                        getCause() == null ? null : getCause().getMessage(),
                        processingException == null ? null : processingException.getMessage());
    }
}


