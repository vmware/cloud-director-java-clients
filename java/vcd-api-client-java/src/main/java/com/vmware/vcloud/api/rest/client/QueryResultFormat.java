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

import java.util.List;

import com.vmware.vcloud.api.rest.constants.RestConstants;
import com.vmware.vcloud.api.rest.schema_v1_5.QueryResultRecordType;

/**
 * Form of result returned by a query.
 *
 * @since vCloud API 1.5.
 */
public enum QueryResultFormat {
    /**
     * Return records from the query. The return type of such a query will be a {@link List} of
     * objects which are assignable to {@link QueryResultRecordType}.
     *
     * @since vCloud API 1.5.
     */
    RECORDS(RestConstants.MediaType.RECORDS, "records"),

    /**
     * Return records from the query, where references to other entities are typed by vCloud
     * globally-unique ID rather than by Entity. The return type of such a query will be a
     * {@link List} of objects which are assignable to {@link QueryResultRecordType}.
     *
     * @since vCloud API 1.5.
     */
    ID_RECORDS(RestConstants.MediaType.IDRECORDS, "idrecords"),

    /**
     * Return references only from the query. The return type of such a query will be a {@code List}
     * of {@link Entity} or {@link ResourceType} instances.
     *
     * @since vCloud API 1.5.
     */
    REFERENCES(RestConstants.MediaType.REFERENCES, "references");

    private String mediaType;
    private String apiString;

    private QueryResultFormat(String mediaType, String apiString) {
        this.mediaType = mediaType;
        this.apiString = apiString;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getApiString() {
        return apiString;
    }
}

