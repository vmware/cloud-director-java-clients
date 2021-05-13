package com.vmware.vcloud.object.extensibility.vcd;

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

import com.vmware.vcloud.object.extensibility.vcd.event.VcdEvent;

/**
 * Interface that defines functions for interacting with vCloud Directors AMQP-
 * based notifications feature. <p>
 *
 * The interface contract allows a caller to enable notifications just for the entities
 * that it cares about.  Implementations must bind to all possible events for those
 * enabled {@link EntityType}s.  For example, enabling notifications for {@link EntityType#VC}
 * must enable notifications for all the VC event types:
 * <ul>
 *   <li>vc/create</li>
 *   <li>vc/modify</li>
 *   <li>vc/delete</li>
 * </ul>
 */
public interface VcdNotificationListener {
    /**
     * Entities that are recognized by the {@link VcdNotificationListener}
     */
    enum EntityType {
        VC("vc");

        private final String key;

        private EntityType(final String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    /**
     * Instructs the vCloud Director listener to listen for events of the specified entity types. <p>
     *
     * The {@link VcdNotificationListener} instance will only bind to, and listen for notification messages
     * for entities that have been explicitly enabled via this method.
     *
     * @param entityTypes the types of entities to receive notification events for
     */
    void enableNotificationsForEntities(EntityType... entityTypes);

    /**
     * Registers an object that can handle some set of {@link VcdEvent} subclasses. <p>
     *
     * This method does not assume any particular mechanism for how events will be sent to
     * the registered handler; that is left as an implementation decision.
     *
     * @param handler an object that will process {@link VcdEvent}s
     * @see com.vmware.vcloud.object.extensibility.vcd.event
     */
    void registerEventHandler(final Object handler);
}

