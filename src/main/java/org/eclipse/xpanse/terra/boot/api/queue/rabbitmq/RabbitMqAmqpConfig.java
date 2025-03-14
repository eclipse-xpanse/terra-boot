/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.api.queue.rabbitmq;

import jakarta.annotation.Resource;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** RabbitMQ configuration for AMQP. */
@Component
@Profile("amqp")
@ConditionalOnProperty(name = "amqp.provider", havingValue = "rabbitmq")
@EnableRabbit
public class RabbitMqAmqpConfig {

    @Qualifier("customJsonMessageConverter")
    @Resource
    private Jackson2JsonMessageConverter jsonMessageConverter;

    /**
     * Configuration for RabbitTemplate.
     *
     * @param connectionFactory connectionFactory
     * @return RabbitTemplate
     */
    @Bean("customRabbitTemplate")
    public RabbitTemplate customRabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }

    /**
     * Configuration for RabbitListenerContainerFactory.
     *
     * @param connectionFactory connectionFactory
     * @return RabbitListenerContainerFactory
     */
    @Bean
    public RabbitListenerContainerFactory<SimpleMessageListenerContainer>
            rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setMessageConverter(jsonMessageConverter);
        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        factory.setPrefetchCount(5);
        factory.setConcurrentConsumers(5);
        factory.setMaxConcurrentConsumers(10);
        return factory;
    }
}
