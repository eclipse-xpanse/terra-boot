/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.api.queues.config;

/** Constants for AMQP queues, exchanges, and routing keys. */
public class AmqpConstants {

    /** Name of the queue for Terraform health check request. */
    public static final String QUEUE_NAME_FOR_TERRAFORM_HEALTH_CHECK_REQUEST =
            "org.eclipse.terra.boot.queue.request.health-check";

    /** Name of the queue for Terraform request with scripts in directory. */
    public static final String QUEUE_NAME_FOR_TERRAFORM_REQUEST_WITH_DIRECTORY =
            "org.eclipse.terra.boot.queue.request.directory";

    /** Name of the queue for Terraform request with scripts in git repo. */
    public static final String QUEUE_NAME_FOR_TERRAFORM_REQUEST_WITH_GIT =
            "org.eclipse.terra.boot.queue.request.git";

    /** Name of the queue for Terraform request with scripts map. */
    public static final String QUEUE_NAME_FOR_TERRAFORM_REQUEST_WITH_SCRIPTS =
            "org.eclipse.terra.boot.queue.request.scripts";

    /** Name of the queue for Terraform health check results. */
    public static final String QUEUE_NAME_FOR_TERRAFORM_HEALTH_CHECK_RESULT =
            "org.eclipse.terra.boot.queue.result.health-check";

    /** Name of the queue for Terraform health check results. */
    public static final String QUEUE_NAME_FOR_TERRAFORM_PLAN_RESULT =
            "org.eclipse.terra.boot.queue.result.plan";

    /** Name of the queue for Terraform health check results. */
    public static final String QUEUE_NAME_FOR_TERRAFORM_VALIDATION_RESULT =
            "org.eclipse.terra.boot.queue.result.validation";

    /** Name of the queue for Terraform results. */
    public static final String QUEUE_NAME_FOR_TERRAFORM_DEPLOYMENT_RESULT =
            "org.eclipse.terra.boot.queue.result.deployment";

    /** Exchange name for Terraform messages. */
    public static final String EXCHANGE_NAME_FOR_TERRAFORM = "terraform.direct.exchange";

    /** Routing keys for Terraform health check request. */
    public static final String ROUTING_KEY_FOR_TERRAFORM_HEALTH_CHECK_REQUEST =
            "request.health-check";

    /** Routing keys for Terraform request with directory. */
    public static final String ROUTING_KEY_FOR_TERRAFORM_REQUEST_WITH_DIRECTORY =
            "request.directory";

    /** Routing keys for Terraform request with scripts. */
    public static final String ROUTING_KEY_FOR_TERRAFORM_REQUEST_WITH_SCRIPTS = "request.scripts";

    /** Routing keys for Terraform request with git repo. */
    public static final String ROUTING_KEY_FOR_TERRAFORM_REQUEST_WITH_GIT = "request.git";

    /** Routing keys for result of Terraform health check. */
    public static final String ROUTING_KEY_FOR_TERRAFORM_HEALTH_CHECK_RESULT =
            "result.health-check";

    /** Routing keys for result of Terraform plan. */
    public static final String ROUTING_KEY_FOR_TERRAFORM_PLAN_RESULT = "result.plan";

    /** Routing keys for result of Terraform validation. */
    public static final String ROUTING_KEY_FOR_TERRAFORM_VALIDATION_RESULT = "result.validation";

    /** Routing keys for result of Terraform deployment. */
    public static final String ROUTING_KEY_FOR_TERRAFORM_DEPLOYMENT_RESULT = "result.deployment";

    private AmqpConstants() {
        // Private constructor to prevent instantiation
    }
}
