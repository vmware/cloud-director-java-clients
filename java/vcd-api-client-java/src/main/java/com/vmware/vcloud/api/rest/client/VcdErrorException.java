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
 * Base exception to represent errors returned by VCD.
 * <P>
 * These are specifically errors returned by VCD in response to a request. The class of exceptions
 * caused while communicating with VCD or other such extraneous errors are NOT represented by this
 * exception or its sub-classes; those exceptions will be thrown as {@link ClientException}
 *
 */
public abstract class VcdErrorException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final int httpStatusCode;

    protected VcdErrorException(final int httpStatusCode, final WebApplicationException cause) {
        super(cause);
        this.httpStatusCode = httpStatusCode;
    }

    public final int getHttpStatusCode() {
        return httpStatusCode;
    }

    /**
     * Returns the original {@link WebApplicationException} returned by the framework
     *
     * {@inheritDoc}
     */
    @Override
    public final WebApplicationException getCause() {
        return (WebApplicationException)super.getCause();
    }

    @Override
    public String toString() {
        final String newLine = System.getProperty("line.separator");
        final StringBuilder builder = new StringBuilder();
        builder.append("VcdErrorException [HTTP status code = ").append(httpStatusCode).append("]").append(newLine);
        builder.append("Original cause: ").append(newLine).append(getCause());
        return builder.toString();
    }

    @Override
    public String getMessage() {
        return String.format("Error reported by VCD. HTTP status code: %d\n" +
                "Error message: %s", getHttpStatusCode(), getCause().getMessage());
    }
}

