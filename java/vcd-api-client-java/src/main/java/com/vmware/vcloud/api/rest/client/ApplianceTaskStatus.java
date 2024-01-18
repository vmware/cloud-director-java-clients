/* ********************************************************************************
 * Copyright 2019 VMware, Inc.  All rights reserved. -- VMware Confidential
 * *******************************************************************************/

package com.vmware.vcloud.api.rest.client;

/**
 * The status of a task.
 *
 * Note: This Enum matches the TaskStatus enum specified in asynclib.py of the vcd_ova_ui
 * 		service in the vcd appliance.
 */

public enum ApplianceTaskStatus {
    /**
     * The new task has been queued for execution.
     */
    NEW("new"),

    /**
     * The task is stopped.
     */
    STOPPED("stopped"),

    /**
     * The task is running.
     */
    RUNNING("running"),

    /**
     * The task failed.
     */
    FAILED("failed");

    public static final ApplianceTaskStatus[] TERMINAL_STATUSES = new ApplianceTaskStatus[] { STOPPED, FAILED };

    private final String label;

    ApplianceTaskStatus(String label) {
        this.label = label;
    }

    /**
     * Getter for {@link ApplianceTaskStatus#label}
     */
    public String getLabel() {
        return label;
    }

    /**
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return getLabel();
    }
}

