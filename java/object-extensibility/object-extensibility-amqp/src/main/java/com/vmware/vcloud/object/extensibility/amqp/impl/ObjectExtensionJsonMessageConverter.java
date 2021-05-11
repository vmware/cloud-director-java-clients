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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.DefaultJavaTypeMapper;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;

/**
 * Manages conversion of of extensibility AMQP messages. Supports conversion to and from JSON
 */
public class ObjectExtensionJsonMessageConverter implements MessageConverter {

    private final Logger logger = LoggerFactory.getLogger(ObjectExtensionJsonMessageConverter.class);
    
    private org.springframework.amqp.support.converter.JsonMessageConverter jsonMessageConverter = 
            new AmqpJsonMessageConverter();
    final ObjectMapper jsonMapper = new ObjectMapper();

    @Override
    public Message toMessage(final Object object, final MessageProperties messageProperties) throws MessageConversionException {

        Objects.requireNonNull(object, "message cannot be null");
        Objects.requireNonNull(messageProperties, "message properties cannot be null");
        Objects.requireNonNull(messageProperties.getContentType(), "message content type cannot be null");

        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        
        return jsonMessageConverter.toMessage(object, messageProperties);
    }

    @Override
    public Object fromMessage(final Message message) throws MessageConversionException {

        try {
            Objects.requireNonNull(message, "message cannot be null");
            Objects.requireNonNull(message.getMessageProperties(), "message properties cannot be null");
            Objects.requireNonNull(message.getMessageProperties().getContentType(), "message content type cannot be null");
            
            Object jsonObject = jsonMapper.readValue(message.getBody(), Object.class);
            String formattedJsonMsg = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
            logger.info("Converting AMQP JSON message:\n {}", formattedJsonMsg);

            final Object responseMessage = jsonMessageConverter.fromMessage(message);

            if (responseMessage != null && RuntimeException.class.isAssignableFrom(responseMessage.getClass())) {
                throw (RuntimeException) responseMessage;
            }

            return responseMessage;
        } catch (Exception e) {
            logger.error("Unexpected exception processing incoming AMQP message. Returning null", e);
            return null;
        }
    }
    
    class AmqpJsonMessageConverter extends org.springframework.amqp.support.converter.JsonMessageConverter {

        
        @Override
        protected void initializeJsonObjectMapper() {
            final ObjectMapper jsonMapper = new ObjectMapper();
            //jsonMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, true);
            jsonMapper.setSerializationInclusion(Inclusion.NON_NULL);
            this.setJsonObjectMapper(jsonMapper);

            final AmqpMessageTypeMapper typeMapper = new AmqpMessageTypeMapper();
            final Map<String, Class<?>> mappings = new HashMap<>();
            typeMapper.setIdClassMapping(mappings);
            this.setJavaTypeMapper(typeMapper);
        }
    }
    
    class AmqpMessageTypeMapper extends DefaultJavaTypeMapper {

        @Override
        public String getClassIdFieldName() {
            return "messageType";
        }
    }
}
