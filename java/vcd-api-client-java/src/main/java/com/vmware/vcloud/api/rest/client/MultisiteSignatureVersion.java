/* **********************************************************
 * api-extension-template-vcloud-director
 * Copyright 2018-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 * **********************************************************/

package com.vmware.vcloud.api.rest.client;

/**
 *
 * Enum with supported Multisite Signature version.
 *
 */
public enum MultisiteSignatureVersion {
    VERSION_1_0("1.0"),
    VERSION_2_0("2.0"),

    /** Larger than all versions. Keep last! */
    VERSION_MAX("");

    private String version;

    MultisiteSignatureVersion(final String version) {
        this.version = version;
    }

    /**
     * @return the version as string
     */
    public String value() {
        return version;
    }

    public static MultisiteSignatureVersion fromValue(final String value) {
        for (final MultisiteSignatureVersion v : MultisiteSignatureVersion.values()) {
            if (v.value().equals(value)) {
                return v;
            }
        }
        return null;
    }
}

