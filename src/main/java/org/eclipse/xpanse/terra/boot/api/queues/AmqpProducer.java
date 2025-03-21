/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.api.queues;

import java.util.UUID;
import org.eclipse.xpanse.terra.boot.models.request.directory.TerraformRequestWithScriptsDirectory;
import org.eclipse.xpanse.terra.boot.models.request.git.TerraformRequestWithScriptsGitRepo;
import org.eclipse.xpanse.terra.boot.models.request.scripts.TerraformRequestWithScripts;
import org.eclipse.xpanse.terra.boot.models.response.TerraBootSystemStatus;
import org.eclipse.xpanse.terra.boot.models.response.TerraformPlan;
import org.eclipse.xpanse.terra.boot.models.response.TerraformResult;
import org.eclipse.xpanse.terra.boot.models.response.validation.TerraformValidationResult;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/** AMQP producer. */
@Profile("amqp")
@Component
public interface AmqpProducer {

    /** Send terraform health check request to amqp queue. */
    void sendTerraformHealthCheckRequest(@Payload UUID requestId);

    /**
     * Send terraform request with scripts to amqp queue.
     *
     * @param request request to send.
     */
    void sendTerraformRequestWithDirectory(@Payload TerraformRequestWithScriptsDirectory request);

    /**
     * Send terraform request with scripts git repo to amqp queue.
     *
     * @param request request to send.
     */
    void sendTerraformRequestWithScriptsGitRepo(
            @Payload TerraformRequestWithScriptsGitRepo request);

    /**
     * Send terraform request with scripts to amqp queue.
     *
     * @param request request to send.
     */
    void sendTerraformRequestWithScripts(@Payload TerraformRequestWithScripts request);

    /**
     * Send terraform health check result to amqp queue.
     *
     * @param result result to send.
     */
    void sendTerraformHealthCheckResult(@Payload TerraBootSystemStatus result);

    /**
     * Send terraform plan result to amqp queue.
     *
     * @param result result to send.
     */
    void sendTerraformPlanResult(@Payload TerraformPlan result);

    /**
     * Send terraform validation result to amqp queue.
     *
     * @param result result to send.
     */
    void sendTerraformValidationResult(@Payload TerraformValidationResult result);

    /**
     * Send terraform deployment result to amqp queue.
     *
     * @param result result to send.
     */
    void sendTerraformDeploymentResult(@Payload TerraformResult result);
}
