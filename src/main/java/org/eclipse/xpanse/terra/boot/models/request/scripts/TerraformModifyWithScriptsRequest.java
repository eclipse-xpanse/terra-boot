/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.models.request.scripts;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.xpanse.terra.boot.models.request.directory.TerraformModifyFromDirectoryRequest;

/** Terraform uses the request object modify by the script. */
@EqualsAndHashCode(callSuper = true)
@Data
public class TerraformModifyWithScriptsRequest extends TerraformModifyFromDirectoryRequest {

    @Schema(description = "Id of the request.")
    private UUID taskId;

    @NotNull
    @NotEmpty
    @Schema(
            description =
                    "Map stores file name and content of all script files for modify request.")
    private Map<String, String> scriptFiles;

    @NotNull
    @Schema(description = "The .tfState file content after deployment")
    private String tfState;
}
