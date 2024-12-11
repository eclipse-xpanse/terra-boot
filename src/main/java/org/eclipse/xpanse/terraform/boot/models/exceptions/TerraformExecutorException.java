/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terraform.boot.models.exceptions;

/** Defines possible exceptions returned by Terraform execution. */
public class TerraformExecutorException extends RuntimeException {

    public TerraformExecutorException(String message) {
        super("TFExecutor Exception: " + message);
    }

    public TerraformExecutorException(String message, String output) {
        super("Executor Exception:" + message + System.lineSeparator() + output);
    }

    public TerraformExecutorException(String message, Throwable ex) {
        super(message, ex);
    }
}
