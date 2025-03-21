/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.api.queues.rabbitmq;

import io.github.springwolf.bindings.amqp.annotations.AmqpAsyncOperationBinding;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher;
import jakarta.annotation.Resource;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.terra.boot.api.queues.AmqpProducer;
import org.eclipse.xpanse.terra.boot.api.queues.config.AmqpConstants;
import org.eclipse.xpanse.terra.boot.models.request.directory.TerraformRequestWithScriptsDirectory;
import org.eclipse.xpanse.terra.boot.models.request.git.TerraformRequestWithScriptsGitRepo;
import org.eclipse.xpanse.terra.boot.models.request.scripts.TerraformRequestWithScripts;
import org.eclipse.xpanse.terra.boot.models.response.TerraBootSystemStatus;
import org.eclipse.xpanse.terra.boot.models.response.TerraformPlan;
import org.eclipse.xpanse.terra.boot.models.response.TerraformResult;
import org.eclipse.xpanse.terra.boot.models.response.validation.TerraformValidationResult;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/** implementation of AmqpProducer interface for RabbitMQ. */
@Slf4j
@Component
@Profile("amqp")
@ConditionalOnProperty(name = "spring.amqp.provider", havingValue = "rabbitmq")
public class RabbitMqProducer implements AmqpProducer {

    @Qualifier("customRabbitTemplate")
    @Resource
    private RabbitTemplate rabbitTemplate;

    @AsyncPublisher(
            operation =
                    @AsyncOperation(
                            channelName =
                                    AmqpConstants.QUEUE_NAME_FOR_TERRAFORM_HEALTH_CHECK_REQUEST,
                            description = "Send terraform health check request to rabbitmq queue."))
    @AmqpAsyncOperationBinding
    @Override
    public void sendTerraformHealthCheckRequest(@Payload UUID requestId) {
        log.info("Received terraform health check request with id {}", requestId);
        sendMessageViaExchange(
                AmqpConstants.ROUTING_KEY_FOR_TERRAFORM_HEALTH_CHECK_REQUEST,
                requestId,
                "Health check request " + requestId);
    }

    @AsyncPublisher(
            operation =
                    @AsyncOperation(
                            channelName =
                                    AmqpConstants.QUEUE_NAME_FOR_TERRAFORM_REQUEST_WITH_DIRECTORY,
                            description = "Send terraform request with scripts to rabbitmq queue."))
    @AmqpAsyncOperationBinding
    @Override
    public void sendTerraformRequestWithDirectory(
            @Payload TerraformRequestWithScriptsDirectory request) {
        log.info(
                "Received terraform directory request with type {} and id {}",
                request.getRequestType(),
                request.getRequestId());
        sendMessageViaExchange(
                AmqpConstants.ROUTING_KEY_FOR_TERRAFORM_REQUEST_WITH_DIRECTORY,
                request,
                "Terraform directory request " + request.getRequestId());
    }

    @AsyncPublisher(
            operation =
                    @AsyncOperation(
                            channelName =
                                    AmqpConstants.QUEUE_NAME_FOR_TERRAFORM_REQUEST_WITH_SCRIPTS,
                            description = "Send terraform request with scripts to rabbitmq queue."))
    @AmqpAsyncOperationBinding
    @Override
    public void sendTerraformRequestWithScripts(@Payload TerraformRequestWithScripts request) {
        log.info(
                "Received terraform scripts request with type {} and id {}",
                request.getRequestType(),
                request.getRequestId());
        sendMessageViaExchange(
                AmqpConstants.ROUTING_KEY_FOR_TERRAFORM_REQUEST_WITH_SCRIPTS,
                request,
                "Terraform scripts request " + request.getRequestId());
    }

    @AsyncPublisher(
            operation =
                    @AsyncOperation(
                            channelName = AmqpConstants.QUEUE_NAME_FOR_TERRAFORM_REQUEST_WITH_GIT,
                            description =
                                    "Send terraform request with scripts git repo to rabbitmq"
                                            + " queue."))
    @AmqpAsyncOperationBinding
    @Override
    public void sendTerraformRequestWithScriptsGitRepo(
            @Payload TerraformRequestWithScriptsGitRepo request) {
        log.info(
                "Received terraform git request with type {} and id {}",
                request.getRequestType(),
                request.getRequestId());
        sendMessageViaExchange(
                AmqpConstants.ROUTING_KEY_FOR_TERRAFORM_REQUEST_WITH_GIT,
                request,
                "Terraform git request " + request.getRequestId());
    }

    @Override
    public void sendTerraformHealthCheckResult(@Payload TerraBootSystemStatus result) {
        sendMessageViaExchange(
                AmqpConstants.ROUTING_KEY_FOR_TERRAFORM_HEALTH_CHECK_RESULT,
                result,
                "Terraform health check result " + result.getRequestId());
    }

    @Override
    public void sendTerraformPlanResult(@Payload TerraformPlan result) {
        sendMessageViaExchange(
                AmqpConstants.ROUTING_KEY_FOR_TERRAFORM_PLAN_RESULT,
                result,
                "Terraform plan result " + result.getRequestId());
    }

    @Override
    public void sendTerraformValidationResult(@Payload TerraformValidationResult result) {
        sendMessageViaExchange(
                AmqpConstants.ROUTING_KEY_FOR_TERRAFORM_VALIDATION_RESULT,
                result,
                "Terraform validation result " + result.getRequestId());
    }

    @Override
    public void sendTerraformDeploymentResult(@Payload TerraformResult result) {
        sendMessageViaExchange(
                AmqpConstants.ROUTING_KEY_FOR_TERRAFORM_DEPLOYMENT_RESULT,
                result,
                "Terraform deployment result " + result.getRequestId());
    }

    private <T> void sendMessageViaExchange(String routingKey, T message, String logPrefix) {
        try {
            if (Objects.isNull(message)) {
                throw new IllegalArgumentException("Message cannot be null");
            }
            if (StringUtils.isBlank(routingKey)) {
                throw new IllegalArgumentException("Routing key cannot be empty");
            }
            rabbitTemplate.convertAndSend(
                    AmqpConstants.EXCHANGE_NAME_FOR_TERRAFORM, routingKey, message);
            log.debug(
                    "{} sent via exchange {} with key {} completed",
                    logPrefix,
                    AmqpConstants.EXCHANGE_NAME_FOR_TERRAFORM,
                    routingKey);
        } catch (AmqpException e) {
            String errorMsg =
                    String.format(
                            "Failed to send message to exchange: %s (key: %s). Error: %s",
                            AmqpConstants.EXCHANGE_NAME_FOR_TERRAFORM, routingKey, e.getMessage());
            log.error("{}", errorMsg, e);
            throw new AmqpException(errorMsg, e);
        }
    }
}
