package com.vmware.vcloud.api.rest.client.impl;

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

import java.util.ListIterator;
import java.util.Objects;

import com.vmware.vcloud.api.rest.client.EventViewer;
import com.vmware.vcloud.api.rest.client.VcdClient;
import com.vmware.vcloud.api.rest.client.VcdClient.QueryListPage;
import com.vmware.vcloud.api.rest.schema_v1_5.QueryResultEventRecordType;

public class EventViewerImpl implements EventViewer {
    private final VcdClient client;

    private static final String EVENT_QUERY_TYPE_NAME = "event";

    public EventViewerImpl(final VcdClient client) {
        this.client = Objects.requireNonNull(client);
    }

    @Override
    public QueryResultEventRecordType getSingleEvent(final String eventType,
            final String entityHref) {
        final ListIterator<QueryListPage<QueryResultEventRecordType>> it =
                client.getQuery(EVENT_QUERY_TYPE_NAME, QueryResultEventRecordType.class)
                        .setFilter("eventType=="
                        + eventType + ";entity==" + entityHref).execute();

        if (!it.hasNext()) {
            throw new RuntimeException("Event '" + eventType + "' for entity '"
                    + entityHref + "' not found.");
        }

        final QueryListPage<QueryResultEventRecordType> events = it.next();
        final long eventCount = events.getTotalItemCount();

        if (eventCount > 1) {
            throw new RuntimeException("Found multiple (" + eventCount
                    + ") events of type '" + eventType + "' for entity '" + entityHref
                    + "' when expected only one.");
        }

        final QueryResultEventRecordType event = events.getPage().get(0);
        return event;
    }

    @Override
    public QueryResultEventRecordType getLatestEvent(final String eventType) {

        final ListIterator<QueryListPage<QueryResultEventRecordType>> it =
                client.getQuery(EVENT_QUERY_TYPE_NAME, QueryResultEventRecordType.class)
                        .setFilter("eventType=="
                        + eventType).setSortDesc("timeStamp").execute();


        if (!it.hasNext()) {
            throw new RuntimeException("Event '" + eventType + "' not found.");
        }

        final QueryListPage<QueryResultEventRecordType> events = it.next();

        //return latest event
        return events.getPage().get(0);
    }
}


