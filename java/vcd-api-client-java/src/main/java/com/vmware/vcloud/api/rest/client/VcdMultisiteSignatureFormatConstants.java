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

