/* **********************************************************
 * api-extension-template-vcloud-director
 * Copyright 2018-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 * **********************************************************/

package com.vmware.vcloud.api.rest.client;

import java.util.regex.Pattern;

/**
 *
 * Format information for the multisite signature authentication
 *
 */
public class VcdMultisiteSignatureFormatConstants {
    public static final String AUTH_TYPE = "Multisite";

    public static final Pattern MULTISITE_LOGIN_PATTERN = Pattern.compile("^Multisite (.+)$",
            Pattern.CASE_INSENSITIVE);

    public static final Pattern MULTISITE_SIGNATURE_V1_PATTERN = Pattern
            .compile("(.+):(.+) (.+); (.+)@(.+)");
    public static final Pattern MULTISITE_SITNATURE_V2_PATTERN = Pattern
            .compile("(.+)@(.+) (.+); (.+)@(.+)");

    public static final Pattern MULTISITE_LOGIN_PATTERN_VERSIONED = Pattern.compile(
            "^Multisite v:(.+?); (.+)$", Pattern.CASE_INSENSITIVE);

    public static final String MULTISITE_UNVERSIONED_HEADER_TEMPLATE = "{0} {1}:{2} {3}; {4}";
    public static final String MULTISITE_BASIC_AUTH_USERORG_TEMPLATE = "{0}@{1}";
    public static final String MULTISITE_VERSIONED_HEADER_TEMPLATE = "{0} v:{1}; {2}@{3} {4}; {5}";

    public static final String V1_SIGNING_STRING_TEMPLATE = "(request-target): {0} {1}\n"
            + "date: {2}\n" + "digest: {3}\n" + "content-length: {4}";
    public static final String V1_DIGEST_ALG = "SHA-256";

    public static final String V2_SIGNING_STRING_TEMPLATE = "(request-target): {0} {1}\n"
            + "date: {2}\n" + "content-type: {3}";

    public static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
}

