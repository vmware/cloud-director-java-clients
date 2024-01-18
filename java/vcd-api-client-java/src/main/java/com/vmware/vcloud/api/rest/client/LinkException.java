/*-
 * #%L
 * vcd-api-client-java :: Implementation of VCD schemas
 * %%
 * Copyright (C) 2022 VMware, Inc.
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

package com.vmware.vcloud.api.rest.client;

import com.vmware.vcloud.api.rest.links.LinkRelation;

/**
 * Base class for exceptions related to operations on &lt;link&gt; elements in responses.
 */
public abstract class LinkException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final String href;
    private final LinkRelation rel;
    private final String mediaType;

    protected LinkException(String href, LinkRelation rel, String mediaType) {
        this.href = href;
        this.rel = rel;
        this.mediaType = mediaType;
    }

    public LinkRelation getRel() {
        return rel;
    }

    public String getMediaType() {
        return mediaType;
    }

    @Override
    public String toString() {
        return String.format("%s; href: %s, rel: %s, mediaType: %s", super.toString(), href, rel, mediaType);
    }
}
