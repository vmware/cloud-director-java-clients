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


