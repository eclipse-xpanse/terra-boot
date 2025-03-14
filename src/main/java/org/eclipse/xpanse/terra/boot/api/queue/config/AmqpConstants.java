/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.api.queue.config;

/** Constants for AMQP queues, exchanges, and routing keys. */
public class AmqpConstants {

    /** Name of the queue for Terraform request with scripts in directory. */
    public static final String TERRAFORM_REQUEST_WITH_DIRECTORY_QUEUE_NAME =
            "org.eclipse.terra.boot.queue.request.directory";

    /** Name of the queue for Terraform request with scripts in git repo. */
    public static final String TERRAFORM_REQUEST_WITH_GIT_QUEUE_NAME =
            "org.eclipse.terra.boot.queue.request.git";

    /** Name of the queue for Terraform request with scripts map. */
    public static final String TERRAFORM_REQUEST_WITH_SCRIPTS_QUEUE_NAME =
            "org.eclipse.terra.boot.queue.request.scripts";

    /** Name of the queue for Terraform results. */
    public static final String TERRAFORM_RESULT_QUEUE_NAME = "org.eclipse.terra.boot.result";

    private AmqpConstants() {
        // Private constructor to prevent instantiation
    }
}
