/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.api.queue;

import org.eclipse.xpanse.terra.boot.models.request.directory.TerraformRequestWithScriptsDirectory;
import org.eclipse.xpanse.terra.boot.models.request.git.TerraformRequestWithScriptsGitRepo;
import org.eclipse.xpanse.terra.boot.models.request.scripts.TerraformRequestWithScripts;
import org.eclipse.xpanse.terra.boot.models.response.TerraformResult;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/** AMQP producer. */
@Profile("amqp")
@Component
public interface AmqpProducer {

    /**
     * Send terraform request with scripts to amqp queue.
     *
     * @param request received request.
     */
    void sendTerraformRequestWithDirectory(@Payload TerraformRequestWithScriptsDirectory request);

    /**
     * Send terraform request with scripts git repo to amqp queue.
     *
     * @param request received request.
     */
    void sendTerraformRequestWithScriptsGitRepo(
            @Payload TerraformRequestWithScriptsGitRepo request);

    /**
     * Send terraform request with scripts to amqp queue.
     *
     * @param request received request.
     */
    void sendTerraformRequestWithScripts(@Payload TerraformRequestWithScripts request);

    /**
     * Send terraform result to amqp queue after the order completed.
     *
     * @param result result to send.
     */
    void sendTerraformResult(@Payload TerraformResult result);
}
