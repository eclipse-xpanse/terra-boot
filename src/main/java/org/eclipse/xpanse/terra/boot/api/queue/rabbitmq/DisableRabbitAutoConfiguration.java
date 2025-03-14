/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.api.queue.rabbitmq;

import org.springframework.boot.actuate.autoconfigure.amqp.RabbitHealthContributorAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** Disable RabbitMQ configuration and health check. */
@Profile("!amqp")
@Configuration
@EnableAutoConfiguration(
        exclude = {RabbitAutoConfiguration.class, RabbitHealthContributorAutoConfiguration.class})
public class DisableRabbitAutoConfiguration {}
