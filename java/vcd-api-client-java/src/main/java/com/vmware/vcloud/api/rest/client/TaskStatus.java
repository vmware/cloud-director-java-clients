
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

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * The status of a task
 *
 * Note: This Enum is clone of {@link com.vmware.vcloud.api.presentation.entity.common.TaskStatus}.
 * It is cloned in-order to avoid rest-client dependency on presentation layer.
 */

public enum TaskStatus {
    /**
     * The task has been queued for execution
     */
    PENDING("pending"),

    /**
     * The task is assigned for pre-processing
     */
    PRE_RUNNING("pre-running"),

    /**
     * The task is running
     */
    RUNNING("running"),

    /**
     * The task completed successfully
     */
    SUCCESS("success"),

    /**
     * The task was aborted
     */
    ABORTED("aborted"),

    /**
     * The task completed with an error. The {@link TaskSpec#error} field would
     * be set with information about the error
     */
    ERROR("error"),

    /**
     * The task was canceled.
     */
    CANCELED("canceled");

    public static final TaskStatus[] TERMINAL_STATUSES = new TaskStatus[] { SUCCESS, ABORTED, ERROR, CANCELED };

    private static final Map<String, TaskStatus> REVERSE_LOOKUP_MAP;
    static {
        final Map<String, TaskStatus> reverseLookupMap =
                Stream.of(TaskStatus.values()).collect(Collectors.toMap(TaskStatus::getLabel, ts -> ts));

        REVERSE_LOOKUP_MAP = Collections.unmodifiableMap(reverseLookupMap);
    }

    private final String label;

    TaskStatus(String label) {
        this.label = label;
    }

    /**
     * Getter for {@link TaskStatus#label}
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

    public static TaskStatus from(String s) {
        final Optional<TaskStatus> ts = Optional.ofNullable(REVERSE_LOOKUP_MAP.get(s));
        return ts.orElseThrow(IllegalArgumentException::new);
    }
}



