/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.models.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Data model to represent terraform plan output. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TerraformPlan implements Serializable {

    @Serial private static final long serialVersionUID = 4957985190544009199L;

    @NotNull
    @Schema(description = "The id of the Terraform plan request")
    private UUID requestId;

    @NotNull
    @Schema(description = "Terraform plan as a JSON string")
    private String plan;

    @Schema(description = "The version of the Terraform binary used to execute scripts.")
    private String terraformVersionUsed;

    @Schema(description = "The error message of executing terraform plan command.")
    private String errorMessage;
}
