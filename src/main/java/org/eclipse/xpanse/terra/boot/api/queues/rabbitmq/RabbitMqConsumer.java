/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.api.queues.rabbitmq;

import jakarta.annotation.Resource;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.terra.boot.api.queues.AmqpConsumer;
import org.eclipse.xpanse.terra.boot.api.queues.config.AmqpConstants;
import org.eclipse.xpanse.terra.boot.models.enums.HealthStatus;
import org.eclipse.xpanse.terra.boot.models.exceptions.UnsupportedEnumValueException;
import org.eclipse.xpanse.terra.boot.models.request.TerraformRequest;
import org.eclipse.xpanse.terra.boot.models.request.directory.TerraformRequestWithScriptsDirectory;
import org.eclipse.xpanse.terra.boot.models.request.git.TerraformRequestWithScriptsGitRepo;
import org.eclipse.xpanse.terra.boot.models.request.scripts.TerraformRequestWithScripts;
import org.eclipse.xpanse.terra.boot.models.response.TerraBootSystemStatus;
import org.eclipse.xpanse.terra.boot.terraform.service.TerraformRequestService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/** Implementation of AmqpConsumer interface for RabbitMQ. */
@Slf4j
@Component
@Profile("amqp")
@ConditionalOnProperty(name = "spring.amqp.provider", havingValue = "rabbitmq")
public class RabbitMqConsumer implements AmqpConsumer {

    @Resource private RabbitMqProducer producer;

    @Lazy @Resource private TerraformRequestService requestService;

    @Value("${springwolf.docket.servers.amqp-server.protocol}")
    private String serverProtocol;

    @Value("${springwolf.docket.servers.amqp-server.host}")
    private String serverUrl;

    @RabbitListener(
            queues = AmqpConstants.QUEUE_NAME_FOR_TERRAFORM_HEALTH_CHECK_REQUEST,
            containerFactory = "customRabbitListenerContainerFactory")
    @Override
    public void processTerraformHealthCheckRequestFromQueue(@Payload UUID requestId) {
        log.info("Processing received health check request with id {}", requestId);
        TerraBootSystemStatus result = null;
        try {
            result = requestService.healthCheck(requestId);
        } catch (Exception e) {
            log.error("Failed to process health request with id {}", requestId, e);
            result = new TerraBootSystemStatus();
            result.setRequestId(requestId);
            result.setHealthStatus(HealthStatus.NOK);
            result.setErrorMessage(e.getMessage());
        }
        result.setServiceType(serverProtocol);
        result.setServiceUrl(serverUrl);
        producer.sendTerraformHealthCheckResult(result);
    }

    @RabbitListener(
            queues = AmqpConstants.QUEUE_NAME_FOR_TERRAFORM_REQUEST_WITH_DIRECTORY,
            containerFactory = "customRabbitListenerContainerFactory")
    @Override
    public void processTerraformRequestWithDirectoryFromQueue(
            @Payload TerraformRequestWithScriptsDirectory request) {
        handleTerraformRequestAndSendResult(request);
    }

    @RabbitListener(
            queues = AmqpConstants.QUEUE_NAME_FOR_TERRAFORM_REQUEST_WITH_GIT,
            containerFactory = "customRabbitListenerContainerFactory")
    @Override
    public void processTerraformRequestWithGitFromQueue(
            @Payload TerraformRequestWithScriptsGitRepo request) {
        handleTerraformRequestAndSendResult(request);
    }

    @RabbitListener(
            queues = AmqpConstants.QUEUE_NAME_FOR_TERRAFORM_REQUEST_WITH_SCRIPTS,
            containerFactory = "customRabbitListenerContainerFactory")
    @Override
    public void processTerraformRequestWithScriptsFromQueue(
            @Payload TerraformRequestWithScripts request) {
        handleTerraformRequestAndSendResult(request);
    }

    private void handleTerraformRequestAndSendResult(TerraformRequest request) {
        try {
            processRequestByType(request);
        } catch (Exception e) {
            sendErrorResultToQueue(Objects.requireNonNull(request), e);
        }
    }

    private void processRequestByType(TerraformRequest request) {
        switch (request.getRequestType()) {
            case VALIDATE ->
                    producer.sendTerraformValidationResult(
                            requestService.handleTerraformValidateRequest(request));
            case PLAN ->
                    producer.sendTerraformPlanResult(
                            requestService.handleTerraformPlanRequest(request));
            case DEPLOY, MODIFY, DESTROY ->
                    producer.sendTerraformDeploymentResult(
                            requestService.handleTerraformDeploymentRequest(request));
            default ->
                    throw new UnsupportedEnumValueException(
                            String.format(
                                    "RequestType value %s is not supported.",
                                    request.getRequestType().toValue()));
        }
    }

    private void sendErrorResultToQueue(TerraformRequest request, Exception e) {
        log.error(
                "Failed to process request with id {} from amqp queues. {}",
                request.getRequestId(),
                e.getMessage(),
                e);
        try {
            processErrorByRequestType(request, e);
        } catch (Exception innerEx) {
            log.error("Error handling failed request: {}", innerEx.getMessage(), innerEx);
        }
    }

    private void processErrorByRequestType(TerraformRequest request, Exception e) {
        switch (request.getRequestType()) {
            case VALIDATE ->
                    producer.sendTerraformValidationResult(
                            requestService.getErrorValidateResult(request, e));
            case PLAN ->
                    producer.sendTerraformPlanResult(requestService.getErrorPlanResult(request, e));
            case DEPLOY, MODIFY, DESTROY ->
                    producer.sendTerraformDeploymentResult(
                            requestService.getErrorDeploymentResult(request, e));
            default ->
                    throw new UnsupportedEnumValueException(
                            String.format(
                                    "RequestType value %s is not supported.",
                                    request.getRequestType().toValue()));
        }
    }
}
