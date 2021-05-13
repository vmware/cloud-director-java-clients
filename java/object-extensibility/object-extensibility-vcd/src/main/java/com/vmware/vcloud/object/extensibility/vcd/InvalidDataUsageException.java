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

import java.net.URL;

/**
 * Exception thrown on an incorrect use of a data resource in an API call. <p>
 *
 * Thrown for example on specifying an invalid translation of returned data to another
 * Java type, like creating a {@link URL} from a {@link String} representation of a {@link URL}. <p>
 *
 * This exception can either indicate a misuse of an API data resource, or it
 * can indicate a problem in the underlying data resources in the API layer.
 */
public class InvalidDataUsageException extends RuntimeException {
    /**
     * Constructor for {@link InvalidDataUsageException}.
     *
     * @param message the detail message
     */
    public InvalidDataUsageException(final String message) {
        super(message);
    }

    /**
     * Constructor for {@link InvalidDataUsageException}.
     *
     * @param message the detail message
     * @param cause the root cause from the underlying vCloud Director system, or from
     * 	misuse of API data
     */
    public InvalidDataUsageException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

