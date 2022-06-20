/* **********************************************************************
 * Copyright 2016 VMware, Inc.  All rights reserved. VMware Confidential
 * *********************************************************************/

package com.vmware.vcloud.api.rest.client.impl.tasks;

import com.vmware.vcloud.api.rest.client.TaskMonitorImpl;
import com.vmware.vcloud.api.rest.client.VcdClient;
import com.vmware.vcloud.api.rest.client.VcdTaskMonitor;


/**
 * An implementation of {@link VcdTaskMonitor} that extends the functionality of
 * {@link TaskMonitorImpl}
 *
 * @since 8.10
 */
public class VcdTaskMonitorImpl extends TaskMonitorImpl implements VcdTaskMonitor {
    private final VcdClient vcdClient;

    public VcdTaskMonitorImpl(VcdClient vcdClient) {
        super(vcdClient);
        this.vcdClient = vcdClient;
    }

    @Override
    public MultiTaskTracker getMultiTaskTracker() {
        return new VcdBulkTaskMonitor(vcdClient.duplicate(false));
    }
}
