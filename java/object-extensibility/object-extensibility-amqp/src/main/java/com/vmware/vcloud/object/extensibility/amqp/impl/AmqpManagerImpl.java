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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.ContentTypeDelegatingMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

import com.vmware.vcloud.object.extensibility.amqp.AmqpManager;

public class AmqpManagerImpl implements AmqpManager {

    private final Logger logger = LoggerFactory.getLogger(AmqpManagerImpl.class);

    private RabbitAdmin rabbitAdmin;
    private ConnectionFactory connectionFactory;


    @Override
    public void configure(String host, String user, String password) {
        logger.info("Configuring AMQ host. host: {}, user: {}", 
                host, user);
        this.connectionFactory = createRabbitConnectionFactory(host, user, password);
        this.rabbitAdmin = new RabbitAdmin(connectionFactory);
    }

    @Override
    public SimpleMessageListenerContainer registerObjectExtensionListener(
            String exchangeName,
            String queueName,
            String routingKey,
            Object messageConsumer,
            String consumerMethodName,
            ContentType contentType,
            int concurrentConsumers) {
        
        MessageConverter msgConverter = ContentType.JSON.equals(contentType) ? 
                new ObjectExtensionJsonMessageConverter() : new ObjectExtensionXMLMessageConverter();

        return registerListener(exchangeName, queueName, routingKey, messageConsumer,
                consumerMethodName, concurrentConsumers, msgConverter);
    }
    
    @Override
    public SimpleMessageListenerContainer registerNotificationListener(String exchangeName,
            String queueName, String routingKey, Object messageConsumer,
            String consumerMethodName, int concurrentConsumers) {
        ContentTypeDelegatingMessageConverter msgConverter = new ContentTypeDelegatingMessageConverter();
        msgConverter.addDelgate(MessageProperties.CONTENT_TYPE_JSON, new JsonToMapMessageConverter());

        return registerListener(exchangeName, queueName, routingKey, messageConsumer,
                consumerMethodName, concurrentConsumers, msgConverter);
    }

    @Override
    public SimpleMessageListenerContainer registerListener(String exchangeName, String queueName,
            String routingKey, Object messageConsumer, String consumerMethodName, int concurrentConsumers,
            final MessageConverter msgConverter) {
        logger.info("Declaring RMQ objects. exchange: {}, queue: {}, binding routingKey: {}",
                exchangeName, queueName, routingKey);

        rabbitAdmin.declareExchange(new TopicExchange(exchangeName));
        rabbitAdmin.declareQueue(new Queue(queueName, false));
        rabbitAdmin.declareBinding(new Binding(queueName, DestinationType.QUEUE, exchangeName, routingKey, null));

        logger.info("Creating AMQP listener. exchange: {}, queue: {}, routingKey: {}",
                exchangeName, queueName, routingKey);

        final SimpleMessageListenerContainer container = createMessageListenerContainer(queueName);
        final MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(messageConsumer);
        messageListenerAdapter.setDefaultListenerMethod(consumerMethodName);
        messageListenerAdapter.setMessageConverter(msgConverter);
        container.setMessageListener(messageListenerAdapter);
        container.setConcurrentConsumers(concurrentConsumers);
        container.start();

        logger.info("AMQP listener container started. exchange: {}, queue: {}, routingKey: {}",
                exchangeName, queueName, routingKey);

        return container;
    }

    public SimpleMessageListenerContainer createMessageListenerContainer(String queueName) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queueName);
        return container;
    }

    public ConnectionFactory createRabbitConnectionFactory(String host, String user, String password) {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host);
        connectionFactory.setUsername(user);
        connectionFactory.setPassword(password);
        return connectionFactory;
    }
}
