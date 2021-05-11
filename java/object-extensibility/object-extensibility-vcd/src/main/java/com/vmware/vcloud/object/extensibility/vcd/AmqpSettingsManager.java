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

import com.vmware.vcloud.object.extensibility.amqp.AmqpConnectionInfo;

/**
 * Interface that defines functions for interacting with AMQP features
 * of the vCloud API. <p>
 *
 * Before making these calls, the extension must authenticate to the vCloud API as
 * a system administrator.
 */
public interface AmqpSettingsManager {
    /**
     * Gets the AMQP connection details that vCloud Director is currently configured to use.
     *
     * @return AMQP connection details
     */
    AmqpConnectionInfo getAmqpConnectionInfo();

    /**
     * Creates a VcdListener instance that can listen for notifications
     * from a vCloud Director instance. <p>
     *
     * This method must not enable any notification bindings by default; those
     * should be specified by consumers of the listener via the {@link VcdNotificationListener}'s
     * provided interface.
     *
     * @param username username portion of credentials for monitoring notifications
     * @param password password portion of credentials for monitoring notifications
     * @return a listener instance that can monitor vCloud Director notifications
     * @see VcdNotificationListener#enableNotificationsForEntities(com.vmware.vcloud.object.extensibility.vcd.VcdNotificationListener.EntityType...)
     */
    VcdNotificationListener getVcdListener(String username, String password);
}
