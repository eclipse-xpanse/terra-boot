/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.api.queue.rabbitmq;

import io.github.springwolf.bindings.amqp.annotations.AmqpAsyncOperationBinding;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.terra.boot.api.queue.AmqpProducer;
import org.eclipse.xpanse.terra.boot.api.queue.config.AmqpConstants;
import org.eclipse.xpanse.terra.boot.models.request.TerraformRequest;
import org.eclipse.xpanse.terra.boot.models.request.directory.TerraformRequestWithScriptsDirectory;
import org.eclipse.xpanse.terra.boot.models.request.git.TerraformRequestWithScriptsGitRepo;
import org.eclipse.xpanse.terra.boot.models.request.scripts.TerraformRequestWithScripts;
import org.eclipse.xpanse.terra.boot.models.response.TerraformResult;
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
@ConditionalOnProperty(name = "amqp.provider", havingValue = "rabbitmq")
public class RabbitMqAmqpProducer implements AmqpProducer {

    @Qualifier("customRabbitTemplate")
    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * Send terraform request with scripts to amqp queue.
     *
     * @param request received request.
     */
    @AsyncPublisher(
            operation =
                    @AsyncOperation(
                            channelName = AmqpConstants.TERRAFORM_REQUEST_WITH_DIRECTORY_QUEUE_NAME,
                            description =
                                    "send terraform request with scripts to rabbitmq amqp queue"))
    @AmqpAsyncOperationBinding
    public void sendTerraformRequestWithDirectory(
            @Payload TerraformRequestWithScriptsDirectory request) {
        sendTerraformRequestToQueue(
                AmqpConstants.TERRAFORM_REQUEST_WITH_DIRECTORY_QUEUE_NAME, request);
    }

    /**
     * Send terraform request with scripts to amqp queue.
     *
     * @param request received request.
     */
    @AsyncPublisher(
            operation =
                    @AsyncOperation(
                            channelName = AmqpConstants.TERRAFORM_REQUEST_WITH_SCRIPTS_QUEUE_NAME,
                            description =
                                    "send terraform request with scripts to rabbitmq amqp queue"))
    @AmqpAsyncOperationBinding
    public void sendTerraformRequestWithScripts(@Payload TerraformRequestWithScripts request) {
        sendTerraformRequestToQueue(
                AmqpConstants.TERRAFORM_REQUEST_WITH_DIRECTORY_QUEUE_NAME, request);
    }

    /**
     * Send terraform request with scripts to amqp queue.
     *
     * @param request received request.
     */
    @AsyncPublisher(
            operation =
                    @AsyncOperation(
                            channelName = AmqpConstants.TERRAFORM_REQUEST_WITH_GIT_QUEUE_NAME,
                            description =
                                    "send terraform request with scripts to rabbitmq amqp queue"))
    @AmqpAsyncOperationBinding
    public void sendTerraformRequestWithScriptsGitRepo(
            @Payload TerraformRequestWithScriptsGitRepo request) {
        sendTerraformRequestToQueue(AmqpConstants.TERRAFORM_REQUEST_WITH_GIT_QUEUE_NAME, request);
    }

    /**
     * Send terraform result to amqp queue.
     *
     * @param result received request.
     */
    public void sendTerraformResult(@Payload TerraformResult result) {
        try {
            rabbitTemplate.convertAndSend(AmqpConstants.TERRAFORM_RESULT_QUEUE_NAME, result);
            log.info(
                    "Send terraform result with id:{} to result queue successfully.",
                    result.getRequestId());
        } catch (AmqpException e) {
            log.error(
                    "Failed to send terraform result with id:{} result queue.",
                    result.getRequestId(),
                    e);
        }
    }

    private void sendTerraformRequestToQueue(String queueName, TerraformRequest request) {
        try {
            rabbitTemplate.convertAndSend(queueName, request);
            log.info(
                    "Send terraform request with id:{} to queue:{} successfully.",
                    request.getRequestId(),
                    queueName);
        } catch (AmqpException e) {
            log.error(
                    "Failed to send terraform request with id:{} to queue:{}.",
                    request.getRequestId(),
                    queueName,
                    e);
        }
    }
}
