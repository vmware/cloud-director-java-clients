package com.vmware.vcloud.object.extensibility.vcd.support;

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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.vcloud.api.rest.client.VcdClient;
import com.vmware.vcloud.api.rest.schema_v1_5.QueryResultVirtualCenterRecordType;
import com.vmware.vcloud.api.rest.schema_v1_5.extension.VimServerType;
import com.vmware.vcloud.object.extensibility.vcd.InvalidDataUsageException;
import com.vmware.vcloud.object.extensibility.vcd.VcenterInfo;
import com.vmware.vcloud.object.extensibility.vcd.VcenterManager;

/**
 * Default implementation of {@link VcenterManager}.
 */
public class VcenterManagerImpl implements VcenterManager {
    private static final Logger LOG = LoggerFactory.getLogger(VcenterManagerImpl.class);

    private final VcdClient vcdClient;

    public VcenterManagerImpl(final VcdClient vcdClient) {
        this.vcdClient = vcdClient;
    }

    @Override
    public Set<VcenterInfo> getAllRegisteredVcenters() {
        return vcdClient.getPackagedQuery("/admin/extension/vimServerReferences/query", QueryResultVirtualCenterRecordType.class)
            .setPageSize(128)
            .stream()
            .map(record -> processVcenterRecord(record)).collect(Collectors.toSet());
    }

    @Override
    public VcenterInfo getVcenterInfo(final String entity) {
        return createVcenterInfo(vcdClient.getEntity(entity, ExtensionConstants.MediaType.VIRTUAL_CENTERM, VimServerType.class));
    }

    private VcenterInfo processVcenterRecord(final QueryResultVirtualCenterRecordType record) {
        try {
            return new DefaultVcenterInfo(new URL(record.getUrl()), record.isIsEnabled(), record.getVcVersion());
        } catch (final MalformedURLException e) {
            LOG.error("Cannot create URL instance from the vCenter record's URL String {0}", record.getUrl());
            throw new InvalidDataUsageException("URL creation failed", e);
        }
    }

    private VcenterInfo createVcenterInfo(final VimServerType type) {
        try {
            return new DefaultVcenterInfo(new URL(type.getUrl()), type.isIsEnabled(), type.getVcVersion());
        } catch (final MalformedURLException e) {
            LOG.error("Cannot create URL instance from the vCenter record's URL String {0}", type.getUrl());
            throw new InvalidDataUsageException("URL creation failed", e);
        }
    }
}
