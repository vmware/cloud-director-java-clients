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


package com.vmware.vcloud.api.rest.client.impl.tasks;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Manages known earliest known start and end times and calculates best end time value to use when
 * querying for completed tasks.
 * <P>
 * This class's implementation is closely tied to the implementation of {@link VcdBulkTaskMonitor}
 * and is meant for its exclusive use.
 *
 * @since 8.10
 */
class TaskTimingInfo {
    private final AtomicReference<Date> earliestKnownStartTime = new AtomicReference<>();

    private final AtomicReference<Date> earliestKnownEndTime = new AtomicReference<>();

    TaskTimingInfo() {}

    /**
     * Updates the earliest known start time, if necessary.
     * <P>
     * If the start time passed is later currently known earliest start time, it is ignored.
     * <BR>
     * If the start time passed as a argument is before the {@link #earliestKnownStartTime}, its
     * value is updated and {@link #earliestKnownEndTime} is cleared. This allows for the following
     * query to run with the {@code #earliestKnownStartTime} so that tasks that may have potentially
     * ended before tracking began can now be found an updated. This reset functionality is expected
     * to be rarely used in most known circumstances as most tasks are expected to be added in order
     * of their creation, but this reset behavior can account for odd edge cases.
     *
     * @param startTime
     *            a new start time that was encountered.
     */
    void updateStartTime(Date startTime) {
        if (startTime == null) {
            return;
        }
        final Date accumulatedStartTime =
                earliestKnownStartTime.accumulateAndGet(
                        startTime,
                        (currentStartTime, newStartTime) ->
                            currentStartTime == null || newStartTime.before(currentStartTime)
                                ? newStartTime : currentStartTime
                );
        if (accumulatedStartTime.equals(startTime)) {
            earliestKnownEndTime.set(null);
        }
    }

    /**
     * Updates the latest known end time to be the passed value, unless the latest known end time
     * has been erased by {@link #updateStartTime(Date)}
     *
     * @param endTime
     *            latest known end time that was encountered
     */
    void updateEndTime(Date endTime) {
        earliestKnownEndTime.accumulateAndGet(endTime,
                (currentEndTime, newEndTime) -> currentEndTime == null ? null : newEndTime);
    }

    /**
     * Atomically determines, updates {@link #earliestKnownEndTime} and returns that value for use
     * in the next task query.
     *
     * @return earliest known end time that is used to query for tasks that complete at or after
     *         this time.
     */
    Date getEndTime() {
        return earliestKnownEndTime.updateAndGet(
                (endTime) -> Optional.ofNullable(endTime).orElse(earliestKnownStartTime.get()));
    }
}
