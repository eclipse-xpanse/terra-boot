/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.api.queues.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
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

    /** Create JSON message converter for Spring AMQP. */
    @Bean("customJsonMessageConverter")
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        ObjectMapper localObjectMapper = objectMapper.copy();
        localObjectMapper
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .deactivateDefaultTyping()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        Jackson2JsonMessageConverter converter =
                new Jackson2JsonMessageConverter(localObjectMapper);
        converter.setAlwaysConvertToInferredType(true);
        return converter;
    }

    private Queue createDurableQueue(String queueName) {
        return QueueBuilder.durable(queueName).build();
    }

    /** Create durable queue for Terraform health check request. */
    @Bean
    public Queue queueForTerraformHealthCheckRequest() {
        return createDurableQueue(AmqpConstants.QUEUE_NAME_FOR_TERRAFORM_HEALTH_CHECK_REQUEST);
    }

    /** Create durable queue for Terraform request with directory. */
    @Bean
    public Queue queueForTerraformRequestWithDirectory() {
        return createDurableQueue(AmqpConstants.QUEUE_NAME_FOR_TERRAFORM_REQUEST_WITH_DIRECTORY);
    }

    /** Create durable queue for Terraform request with scripts. */
    @Bean
    public Queue queueForTerraformRequestWithScripts() {
        return createDurableQueue(AmqpConstants.QUEUE_NAME_FOR_TERRAFORM_REQUEST_WITH_SCRIPTS);
    }

    /** Create durable queue for Terraform request with git repo. */
    @Bean
    public Queue queueForTerraformRequestWithScriptsGitRepo() {
        return createDurableQueue(AmqpConstants.QUEUE_NAME_FOR_TERRAFORM_REQUEST_WITH_GIT);
    }

    /** Create durable queue for Terraform health check result. */
    @Bean
    public Queue queueForTerraformHealthCheckResult() {
        return createDurableQueue(AmqpConstants.QUEUE_NAME_FOR_TERRAFORM_HEALTH_CHECK_RESULT);
    }

    /** Create durable queue for Terraform plan result. */
    @Bean
    public Queue queueForTerraformPlanResult() {
        return createDurableQueue(AmqpConstants.QUEUE_NAME_FOR_TERRAFORM_PLAN_RESULT);
    }

    /** Create durable queue for Terraform validation result. */
    @Bean
    public Queue queueForTerraformValidationResult() {
        return createDurableQueue(AmqpConstants.QUEUE_NAME_FOR_TERRAFORM_VALIDATION_RESULT);
    }

    /** Create durable queue for Terraform deployment result. */
    @Bean
    public Queue queueForTerraformDeploymentResult() {
        return createDurableQueue(AmqpConstants.QUEUE_NAME_FOR_TERRAFORM_DEPLOYMENT_RESULT);
    }

    /** Create direct exchange for Terraform message. */
    @Bean
    public DirectExchange terraformDirectExchange() {
        return new DirectExchange(AmqpConstants.EXCHANGE_NAME_FOR_TERRAFORM, true, false);
    }

    /** Bind Terraform health check request queue to Terraform direct exchange. */
    @Bean
    public Binding bindHealthCheckRequest(
            DirectExchange terraformDirectExchange, Queue queueForTerraformHealthCheckRequest) {
        return BindingBuilder.bind(queueForTerraformHealthCheckRequest)
                .to(terraformDirectExchange)
                .with(AmqpConstants.ROUTING_KEY_FOR_TERRAFORM_HEALTH_CHECK_REQUEST);
    }

    /** Bind Terraform request with directory queue to Terraform direct exchange. */
    @Bean
    public Binding bindRequestWithDirectory(
            DirectExchange terraformDirectExchange, Queue queueForTerraformRequestWithDirectory) {
        return BindingBuilder.bind(queueForTerraformRequestWithDirectory)
                .to(terraformDirectExchange)
                .with(AmqpConstants.ROUTING_KEY_FOR_TERRAFORM_REQUEST_WITH_DIRECTORY);
    }

    /** Bind Terraform request with scripts queue to Terraform direct exchange. */
    @Bean
    public Binding bindRequestWithScripts(
            DirectExchange terraformDirectExchange, Queue queueForTerraformRequestWithScripts) {
        return BindingBuilder.bind(queueForTerraformRequestWithScripts)
                .to(terraformDirectExchange)
                .with(AmqpConstants.ROUTING_KEY_FOR_TERRAFORM_REQUEST_WITH_SCRIPTS);
    }

    /** Bind Terraform request with git repo queue to Terraform direct exchange. */
    @Bean
    public Binding bindRequestWithGitRepo(
            DirectExchange terraformDirectExchange,
            Queue queueForTerraformRequestWithScriptsGitRepo) {
        return BindingBuilder.bind(queueForTerraformRequestWithScriptsGitRepo)
                .to(terraformDirectExchange)
                .with(AmqpConstants.ROUTING_KEY_FOR_TERRAFORM_REQUEST_WITH_GIT);
    }

    /** Bind Terraform health check result queue to Terraform direct exchange. */
    @Bean
    public Binding bindHealthCheckResult(
            DirectExchange terraformDirectExchange, Queue queueForTerraformHealthCheckResult) {
        return BindingBuilder.bind(queueForTerraformHealthCheckResult)
                .to(terraformDirectExchange)
                .with(AmqpConstants.ROUTING_KEY_FOR_TERRAFORM_HEALTH_CHECK_RESULT);
    }

    /** Bind Terraform plan result queue to Terraform direct exchange. */
    @Bean
    public Binding bindPlanResult(
            DirectExchange terraformDirectExchange, Queue queueForTerraformPlanResult) {
        return BindingBuilder.bind(queueForTerraformPlanResult)
                .to(terraformDirectExchange)
                .with(AmqpConstants.ROUTING_KEY_FOR_TERRAFORM_PLAN_RESULT);
    }

    /** Bind Terraform validation result queue to Terraform direct exchange. */
    @Bean
    public Binding bindValidationResult(
            DirectExchange terraformDirectExchange, Queue queueForTerraformValidationResult) {
        return BindingBuilder.bind(queueForTerraformValidationResult)
                .to(terraformDirectExchange)
                .with(AmqpConstants.ROUTING_KEY_FOR_TERRAFORM_VALIDATION_RESULT);
    }

    /** Bind Terraform deployment result queue to Terraform direct exchange. */
    @Bean
    public Binding bindDeploymentResult(
            DirectExchange terraformDirectExchange, Queue queueForTerraformDeploymentResult) {
        return BindingBuilder.bind(queueForTerraformDeploymentResult)
                .to(terraformDirectExchange)
                .with(AmqpConstants.ROUTING_KEY_FOR_TERRAFORM_DEPLOYMENT_RESULT);
    }
}
