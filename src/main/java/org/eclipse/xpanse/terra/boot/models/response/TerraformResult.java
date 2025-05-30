/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.models.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Data model for the Terraform command execution results. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TerraformResult implements Serializable {

    @Serial private static final long serialVersionUID = 5138160212124102583L;

    @NotNull
    @Schema(description = "Id of the request")
    private UUID requestId;

    @NotNull
    @Schema(description = "defines if the command was successfully executed")
    private boolean isCommandSuccessful;

    @Schema(description = "stdout of the command returned as string.")
    private String commandStdOutput;

    @Schema(description = "stderr of the command returned as string.")
    private String commandStdError;

    @Schema(description = ".tfstate file contents returned as string.")
    private String terraformState;

    @Schema(
            description =
                    "Data of all other files generated by the terraform execution.The map key"
                            + " contains the file name and value is the file contents as string.")
    private Map<String, String> generatedFileContentMap;

    @Schema(description = "The version of the Terraform binary used to execute scripts.")
    private String terraformVersionUsed;
}
