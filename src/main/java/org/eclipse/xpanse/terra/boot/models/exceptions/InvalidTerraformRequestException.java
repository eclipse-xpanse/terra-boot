/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.models.exceptions;

/** Defines possible exceptions returned by Terraform requests with invalid params. */
public class InvalidTerraformRequestException extends RuntimeException {

    public InvalidTerraformRequestException(String message) {
        super(message);
    }
}
