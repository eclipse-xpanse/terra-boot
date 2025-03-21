/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.models.exceptions;

/** Used to indicate Terraform health check anomalies. */
public class InvalidTerraformRequestException extends RuntimeException {

    public InvalidTerraformRequestException(String message) {
        super(message);
    }
}
