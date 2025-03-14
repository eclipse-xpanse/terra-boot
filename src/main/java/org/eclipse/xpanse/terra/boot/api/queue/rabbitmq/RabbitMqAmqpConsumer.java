/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.api.queue.rabbitmq;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.terra.boot.api.queue.AmqpConsumer;
import org.eclipse.xpanse.terra.boot.api.queue.config.AmqpConstants;
import org.eclipse.xpanse.terra.boot.models.request.TerraformRequest;
import org.eclipse.xpanse.terra.boot.models.request.directory.TerraformRequestWithScriptsDirectory;
import org.eclipse.xpanse.terra.boot.models.request.git.TerraformRequestWithScriptsGitRepo;
import org.eclipse.xpanse.terra.boot.models.request.scripts.TerraformRequestWithScripts;
import org.eclipse.xpanse.terra.boot.models.response.TerraformResult;
import org.eclipse.xpanse.terra.boot.terraform.service.TerraformRequestService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/** Implementation of AmqpConsumer interface for RabbitMQ. */
@Slf4j
@Component
@Profile("amqp")
@ConditionalOnProperty(name = "amqp.provider", havingValue = "rabbitmq")
public class RabbitMqAmqpConsumer implements AmqpConsumer {

    @Resource private RabbitMqAmqpProducer producer;

    @Lazy @Resource private TerraformRequestService requestService;

    /**
     * Get terraform request with scripts directory from queue and process it.
     *
     * @param request request
     */
    @RabbitListener(queues = AmqpConstants.TERRAFORM_REQUEST_WITH_DIRECTORY_QUEUE_NAME)
    @Override
    public void processTerraformRequestWithDirectoryFromQueue(
            @Payload TerraformRequestWithScriptsDirectory request) {
        handleTerraformRequestAndSendResult(request);
    }

    /**
     * Get terraform request with scripts directory from queue and process it.
     *
     * @param request request
     */
    @RabbitListener(queues = AmqpConstants.TERRAFORM_REQUEST_WITH_GIT_QUEUE_NAME)
    public void processTerraformRequestWithGitFromQueue(
            @Payload TerraformRequestWithScriptsGitRepo request) {
        handleTerraformRequestAndSendResult(request);
    }

    /**
     * Get terraform request with scripts directory from queue and process it.
     *
     * @param request request
     */
    @RabbitListener(queues = AmqpConstants.TERRAFORM_REQUEST_WITH_SCRIPTS_QUEUE_NAME)
    public void processTerraformRequestWithScriptsFromQueue(
            @Payload TerraformRequestWithScripts request) {
        handleTerraformRequestAndSendResult(request);
    }

    private void handleTerraformRequestAndSendResult(TerraformRequest request) {
        try {
            log.info(
                    "Start processing request with id:{} from amqp queues.",
                    request.getRequestId());
            TerraformResult result = requestService.handleTerraformRequest(request);
            log.info(
                    "Completed processing request with id:{} from amqp queues.",
                    request.getRequestId());
            producer.sendTerraformResult(result);
        } catch (Exception e) {
            log.error(
                    "Failed to process request with id:{} from amqp queues.{}",
                    request.getRequestId(),
                    e.getMessage());
        }
    }
}
