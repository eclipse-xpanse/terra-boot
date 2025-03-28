/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.models.response.validation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** This data class holds the diagnostics details returned by the Terraform validator. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TerraformValidateDiagnostics {

    @Schema(description = "Detail of validation error.")
    private String detail;
}
