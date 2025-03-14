/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.api.queue.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Configuration for Spring AMQP (Advanced Message Queuing Protocol). */
@Slf4j
@Profile("amqp")
@Component
public class AmqpConfig {

    @Resource private ObjectMapper objectMapper;

    /** Create durable queue for Terraform request with scripts in directory. */
    @Bean
    public Queue queueForTerraformRequestWithDirectory() {
        return QueueBuilder.durable(AmqpConstants.TERRAFORM_REQUEST_WITH_DIRECTORY_QUEUE_NAME)
                .build();
    }

    /** Create durable queue for Terraform request with scripts in git repo. */
    @Bean
    public Queue queueForTerraformRequestWithScripts() {
        return QueueBuilder.durable(AmqpConstants.TERRAFORM_REQUEST_WITH_SCRIPTS_QUEUE_NAME)
                .build();
    }

    /** Create durable queue for Terraform request with scripts in git repo. */
    @Bean
    public Queue queueForTerraformRequestWithScriptsGitRepo() {
        return QueueBuilder.durable(AmqpConstants.TERRAFORM_REQUEST_WITH_GIT_QUEUE_NAME).build();
    }

    /** Create durable queue for Terraform results. */
    @Bean
    public Queue terraformResultQueue() {
        return QueueBuilder.durable(AmqpConstants.TERRAFORM_RESULT_QUEUE_NAME).build();
    }

    /** Define the Jackson2JsonMessageConverter bean. */
    @Bean("customJsonMessageConverter")
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        ObjectMapper localObjectMapper = objectMapper.copy();
        localObjectMapper
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
                .deactivateDefaultTyping()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        Jackson2JsonMessageConverter converter =
                new Jackson2JsonMessageConverter(localObjectMapper);
        converter.setAlwaysConvertToInferredType(true);
        return converter;
    }
}
