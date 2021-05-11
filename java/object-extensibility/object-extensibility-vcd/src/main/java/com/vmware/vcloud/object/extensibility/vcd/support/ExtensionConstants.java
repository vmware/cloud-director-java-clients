
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

import com.vmware.vcloud.api.rest.schema_v1_5.extension.AmqpSettingsType;
import com.vmware.vcloud.api.rest.schema_v1_5.extension.ObjectExtensionType;
import com.vmware.vcloud.api.rest.schema_v1_5.extension.ObjectExtensionsType;
import com.vmware.vcloud.api.rest.schema_v1_5.extension.SystemSettingsType;
import com.vmware.vcloud.api.rest.schema_v1_5.extension.VimServerType;

interface ExtensionConstants {

    public static interface MediaType {
        public static final String XML_FORMAT_SUFFIX = "+xml";

        public static final String VIRTUAL_CENTERM = VimServerType.CONTENT_TYPE + XML_FORMAT_SUFFIX;
        public static final String SYSTEM_SETTINGSM = SystemSettingsType.CONTENT_TYPE + XML_FORMAT_SUFFIX;
        public static final String AMQP_SETTINGSM = AmqpSettingsType.CONTENT_TYPE + XML_FORMAT_SUFFIX;

        public static final String OBJECT_EXTENSION = ObjectExtensionType.CONTENT_TYPE + XML_FORMAT_SUFFIX;
        public static final String OBJECT_EXTENSIONS = ObjectExtensionsType.CONTENT_TYPE + XML_FORMAT_SUFFIX;
    }
}
