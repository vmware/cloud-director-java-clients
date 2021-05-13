
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.security.AnyTypePermission;

/**
 * JAX-RS's Provider to handle XStream in XML format.
 */
@Produces({"application/xml", "application/*+xml", "text/xml" })
@Consumes({"application/xml", "application/*+xml", "text/xml" })
public class XStreamXmlProvider implements MessageBodyReader<Object>, MessageBodyWriter<Object> {
    private static final Set<Class> processed = new HashSet<>();
    private static final XStream xstream = new XStream();
    private static final String DEFAULT_ENCODING = "utf-8";

    static {
        XStream.setupDefaultSecurity(xstream);
        xstream.addPermission(AnyTypePermission.ANY);
    }

    @Override
    public boolean isReadable(Class<?> classType, Type genericType, Annotation[] annotations, javax.ws.rs.core.MediaType mediaType) {
        return classType.getAnnotation(XStreamAlias.class) != null;
    }

    @Override
    public Object readFrom(Class<Object> classType, Type genericType, Annotation[] annotations, javax.ws.rs.core.MediaType mediaType,
                           MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        String encoding = getCharsetAsString(mediaType);
        XStream xStream = getXStream(classType);
        return xStream.fromXML(new InputStreamReader(entityStream, encoding));
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, javax.ws.rs.core.MediaType mediaType) {
        return type.getAnnotation(XStreamAlias.class) != null;
    }

    @Override
    public long getSize(Object o, Class<?> type, Type genericType, Annotation[] annotations, javax.ws.rs.core.MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(Object o, Class<?> type, Type genericType, Annotation[] annotations, javax.ws.rs.core.MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        String encoding = getCharsetAsString(mediaType);
        XStream xStream = getXStream(o.getClass());
        xStream.toXML(o, new OutputStreamWriter(entityStream, encoding));
    }


    protected static String getCharsetAsString(javax.ws.rs.core.MediaType mediaType) {
        if (mediaType == null) {
            return DEFAULT_ENCODING;
        }
        String result = mediaType.getParameters().get("charset");
        return (result == null) ? DEFAULT_ENCODING : result;
    }

    protected XStream getXStream(Class type) {
        synchronized (processed) {
            if (!processed.contains(type)) {
                xstream.processAnnotations(type);
                processed.add(type);
            }
        }
        return xstream;
    }
}


