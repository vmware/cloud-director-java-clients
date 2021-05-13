
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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Future;

import com.vmware.vcloud.api.rest.schema_v1_5.TaskType;


/**
 * VCD specific enhancement for {@link TaskMonitor}.
 *
 * @since   8.10
 */
public interface VcdTaskMonitor extends TaskMonitor {
    /**
     * A monitor for many tasks that will eventually report the final {@link TaskStatus} of each
     * task identified by the task {@code URN}
     * <P>
     * Tasks to track can be added at any time until it is indicated to the tracker that all tasks
     * have been added and a tracking {@link Future} is requested. At that time, the tracker will
     * begin reconciling the results and make them available when all added tasks have completed and
     * their final results know via the tracking {@link Future}.
     *
     * @since 8.10
     */
    public interface MultiTaskTracker {
        /**
         * Add a list of tasks for the monitor to track.
         * <P>
         * Tasks may be added until {@link #toTrackingFuture()} is called to indicate that no more
         * tasks will be added to this tracker.
         *
         * @param tasks
         *            List of Tasks to be tracked
         * @throws IllegalStateException
         *             if {@link #toTrackingFuture()} has been called.
         */
        void track(TaskType... tasks) throws IllegalStateException;

        /**
         * Add a list of tasks for the monitor to track.
         * <P>
         * Tasks may be added until {@link #toTrackingFuture()} is called to indicate that no more
         * tasks will be added to this tracker.
         *
         * @param tasks
         *            List of Tasks to be tracked
         * @throws IllegalStateException
         *             if {@link #toTrackingFuture()} has been called.
         */
        void track(Collection<TaskType> tasks) throws IllegalStateException;

        /**
         * Indicate to the tracker that all tasks desirous to be tracked have been added to the
         * tracker.
         * <P>
         * This will signal to the tracker to begin final reconciliation of the results.
         *
         * @return a {@link Future} representing the {@link MultiTaskTracker}'s activities. When
         *         completion state of all status's is known the {@code Future} will be updated to
         *         return a {@link Map} of {@code task id's} to {@link TaskStatus}. Canceling the
         *         {@code Future} will stop the underlying tracking.
         */
        Future<Map<String, TaskStatus>> toTrackingFuture();
    }

    /**
     * Get a new instance of {@link MultiTaskTracker} that will track all tasks that complete after this call.
     *
     * @return a new instance of {@link MultiTaskTracker}
     */
    MultiTaskTracker getMultiTaskTracker();
}


