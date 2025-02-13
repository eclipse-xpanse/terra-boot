/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.terraform.utils;

import lombok.Data;

/** Encapsulates a result of system command execution. */
@Data
public class SystemCmdResult {

    private String commandExecuted;
    private boolean isCommandSuccessful;
    private String commandStdOutput;
    private String commandStdError;
}
