/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.api.queues;

import java.util.UUID;
import org.eclipse.xpanse.terra.boot.models.request.directory.TerraformRequestWithScriptsDirectory;
import org.eclipse.xpanse.terra.boot.models.request.git.TerraformRequestWithScriptsGitRepo;
import org.eclipse.xpanse.terra.boot.models.request.scripts.TerraformRequestWithScripts;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/** AMQP consumer. */
@Profile("amqp")
@Component
public interface AmqpConsumer {

    /**
     * Get terraform health check request and process it.
     *
     * @param requestId request id
     */
    void processTerraformHealthCheckRequestFromQueue(@Payload UUID requestId);

    /**
     * Get terraform request with scripts directory from queue and process it.
     *
     * @param request request
     */
    void processTerraformRequestWithDirectoryFromQueue(
            @Payload TerraformRequestWithScriptsDirectory request);

    /**
     * Get terraform request with scripts directory from queue and process it.
     *
     * @param request request
     */
    void processTerraformRequestWithGitFromQueue(
            @Payload TerraformRequestWithScriptsGitRepo request);

    /**
     * Get terraform request with scripts directory from queue and process it.
     *
     * @param request request
     */
    void processTerraformRequestWithScriptsFromQueue(@Payload TerraformRequestWithScripts request);
}
