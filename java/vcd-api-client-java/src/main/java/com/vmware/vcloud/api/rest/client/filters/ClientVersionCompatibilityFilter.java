package com.vmware.vcloud.api.rest.client.filters;

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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Priority;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBElement;

import com.vmware.vcloud.api.annotation.Supported;
import com.vmware.vcloud.api.rest.version.ApiVersion;

import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeaderValueParser;
import org.apache.http.message.HeaderValueParser;
import org.apache.http.message.ParserCursor;
import org.apache.http.util.CharArrayBuffer;

/**
 * Filter to remove elements that are not applicable for current request version before transmitting
 * the request.
 * <P>
 * The filter removes (sets to {@code null}) any annotated value, which is not supported by the
 * requested version, which is determined from the {@value HttpHeaders#CONTENT_TYPE} header (if it
 * specifies a version) or from the {@value HttpHeaders#ACCEPT} header with the highest specified
 * version.
 */
@Provider
@Priority(100)
public class ClientVersionCompatibilityFilter implements ClientRequestFilter {
    private final static Pattern rxGetter = Pattern.compile("(?:(?:get)|(?:is))([A-Z0-9_].*)");
    final HeaderValueParser headerParser = new BasicHeaderValueParser();

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        final Object payload = requestContext.getEntity();
        if (payload == null) {
            return;
        }

        final MultivaluedMap<String, Object> headers = requestContext.getHeaders();

        final List<Object> contentTypeHeaders = headers.get(HttpHeaders.CONTENT_TYPE);
        final List<Object> acceptHeaders = headers.get(HttpHeaders.ACCEPT);

        if (contentTypeHeaders.size() != 1) {
            throw new WebApplicationException("Request must contain exactly 1 content-type header");
        }

        final Optional<ApiVersion> requestVersion = Optional.ofNullable(findRequestVersion(contentTypeHeaders));
        final ApiVersion resolvedApiVersion = requestVersion.orElseGet(() -> findRequestVersion(acceptHeaders));

        final Object filteredPayload = filter(payload, resolvedApiVersion);
        requestContext.setEntity(filteredPayload);
    }

    final ApiVersion findRequestVersion(List<Object> headerValues) {
        return headerValues.stream()
            .filter(Objects::nonNull)
            .map(String.class::cast)
            .map(this::getVersionFromHeader)
            .filter(Objects::nonNull)
            .max(ApiVersion::compareTo)
            .orElse(null);
    }

    private final ApiVersion getVersionFromHeader(final String headerValue) {
        final int headerLength = headerValue.length();
        final CharArrayBuffer buffer = new CharArrayBuffer(headerLength);
        buffer.append(headerValue);

        final HeaderElement[] headerElements = headerParser.parseElements(buffer, new ParserCursor(0, headerLength));
        return Arrays.stream(headerElements)
            .map(he -> he.getParameterByName("version"))
            .filter(Objects::nonNull)
            .findAny()
            .map(NameValuePair::getValue)
            .map(ApiVersion::fromValue)
            .orElse(null);
    }

    /*
     * All following code is mostly copied from com.vmware.vcloud.rest.RestApiVersionFilter in
     * common-core. It cannot be referenced from here directly due to project dependency issues.
     * <P>
     * Will be fixed along with VCDA-572
     */

    private Object filter(final Object o, final ApiVersion requestedVersion) {
        if (o == null) {
            return null;
        }

        // JAXB container? Filter the cargo.
        if (o instanceof JAXBElement<?>) {
            JAXBElement<?> el = (JAXBElement<?>) o;
            Object cargo = el.getValue();
            Object filteredValue = filter(cargo, requestedVersion);
            if (filteredValue == null) {
                el.setValue(null);
                return null;
            }
            return el;
        }

        if (o instanceof Collection) {
            return filterCollection((Collection<?>) o, requestedVersion);
        }

        for (Class<? extends Object> c = o.getClass(); c != null; c = c.getSuperclass()) {
            final Supported supported = c.getAnnotation(Supported.class);

            // Stop filtering as soon as the (super)class in not annotated with
            // @Supported
            if (supported == null) {
                break;
            }

            filterProperties(o, c, requestedVersion);
        }

        return o;
    }

    private void filterProperties(Object o, Class<? extends Object> c, ApiVersion requestedVersion) {
        for (Method m : c.getDeclaredMethods()) {
            final Supported supported = m.getAnnotation(Supported.class);

            if (supported == null) {
                continue;
            }

            PropInfo propInfo = getPropInfo(m, o);
            if (propInfo == null) {
                continue;
            }

            try {
                final Object value = m.invoke(o);
                if (value == null) {
                    continue;
                } else if (value instanceof Collection) {
                    filterCollection((Collection<?>) value, requestedVersion);
                } else if (propInfo.setter != null) {
                    final Object newValue =
                            requestedVersion.isInRange(supported) ? filter(value, requestedVersion)
                                    : null;
                    if (newValue != value) {
                        propInfo.setter.invoke(o, newValue);
                    }
                }
            } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                throw new WebApplicationException(e);
            }
        }
    }

    private PropInfo getPropInfo(Method m, Object o) {
        final Matcher matcher = rxGetter.matcher(m.getName());
        if (!matcher.matches()) {
            return null;
        }

        // Check for return type
        final Class<?> type = m.getReturnType();
        if (type == Void.TYPE) {
            return null;
        }

        // Check for parameters
        if (m.getParameterTypes().length != 0) {
            return null;
        }

        // Check for public
        if (!Modifier.isPublic(m.getModifiers())) {
            return null;
        }

        // Check for setter
        String setterName = "set" + matcher.group(1);
        try {
            Method setter = o.getClass().getMethod(setterName, type);
            setter = Modifier.isPublic(setter.getModifiers()) ? setter : null;
            return new PropInfo(setter);
        } catch (SecurityException e) {
        } catch (NoSuchMethodException e) {
            return new PropInfo(null);
        }

        return null;
    }

    private Object filterCollection(Collection<?> c, final ApiVersion requestedVersion) {
        try {
            for (Iterator<?> iter = c.iterator(); iter.hasNext();) {
                Object next = iter.next();
                if (filter(next, requestedVersion) == null) {
                    iter.remove();
                }
            }
        } catch (UnsupportedOperationException e) {
        }
        return c;
    }

    private static class PropInfo {
        public final Method setter;

        public PropInfo(Method setter) {
            this.setter = setter;
        }
    }

}

