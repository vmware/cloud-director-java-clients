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

import com.vmware.vcloud.api.rest.client.VcdClient;
import com.vmware.vcloud.api.rest.constants.RelationType;
import com.vmware.vcloud.api.rest.schema_v1_5.extension.AmqpSettingsType;
import com.vmware.vcloud.api.rest.schema_v1_5.extension.SystemSettingsType;
import com.vmware.vcloud.object.extensibility.amqp.AmqpConnectionInfo;
import com.vmware.vcloud.object.extensibility.amqp.impl.DefaultAmqpConnectionInfo;
import com.vmware.vcloud.object.extensibility.vcd.AmqpSettingsManager;
import com.vmware.vcloud.object.extensibility.vcd.VcdNotificationListener;

/**
 * Default implementation of {@link AmqpSettingsManager}.
 */
public class AmqpSettingsManagerImpl implements AmqpSettingsManager {
    private static final String NOTIFICATION_EXCHANGE_ROOT = "notifications20";

    private final VcdClient vcdClient;

    public AmqpSettingsManagerImpl(final VcdClient vcdClient) {
        this.vcdClient = vcdClient;
    }

    @Override
    public AmqpConnectionInfo getAmqpConnectionInfo() {
        return getAmqpConnectionInfo(getAmqpSettingsType());
    }

    @Override
    public VcdNotificationListener getVcdListener(final String username, final String password) {
        AmqpSettingsType amqpSettings = getAmqpSettingsType();
        return new VcdNotificationListenerEventBusImpl(getAmqpConnectionInfo(amqpSettings), username, password, getNotificationExchange(amqpSettings));
    }

    private AmqpSettingsType getAmqpSettingsType() {
        final SystemSettingsType systemSettings = vcdClient.getResource(vcdClient.getExtension(), RelationType.DOWN,
                ExtensionConstants.MediaType.SYSTEM_SETTINGSM, SystemSettingsType.class);
        return vcdClient.getResource(systemSettings, RelationType.DOWN,
                ExtensionConstants.MediaType.AMQP_SETTINGSM, AmqpSettingsType.class);
    }

    private AmqpConnectionInfo getAmqpConnectionInfo(final AmqpSettingsType amqpSettings) {
        return new DefaultAmqpConnectionInfo(amqpSettings.getAmqpHost(), amqpSettings.getAmqpPort(),
                amqpSettings.isAmqpUseSSL(), amqpSettings.isAmqpSslAcceptAll());
    }

    private String getNotificationExchange(final AmqpSettingsType amqpSettings) {
        return String.format("%s.%s", amqpSettings.getAmqpPrefix(), NOTIFICATION_EXCHANGE_ROOT);
    }
}


