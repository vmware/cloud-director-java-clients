package com.vmware.vcloud.object.extensibility.amqp.impl;

/*-
 * #%L
 * object-extensibility-amqp :: Extension AMQP library
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
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;

/**
 * Manages conversion of of vCloud Director Notification AMQP messages to a
 * Java map. <p>
 *
 * Conversion from a map to JSON AMQP message is not supported.
 */
public class JsonToMapMessageConverter implements MessageConverter {

    private final Logger logger = LoggerFactory.getLogger(JsonToMapMessageConverter.class);

    @Override
    public Message toMessage(final Object object, final MessageProperties messageProperties) throws MessageConversionException {

        throw new UnsupportedOperationException("Conversion to AMQP message not supported by: " +
                this.getClass().getCanonicalName());
    }

    @Override
    public Object fromMessage(final Message message) throws MessageConversionException {

        try {
            ObjectReader reader = new ObjectMapper().reader(Map.class);

            Map<String, Object> map = reader.readValue(message.getBody());

            return map;

        } catch (IOException e) {
            logger.error("Unexpected exception de-serializing json notification", e);
            return null;
        }
    }
}

