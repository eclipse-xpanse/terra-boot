/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.models.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.terra.boot.models.enums.RequestType;
import org.eclipse.xpanse.terra.boot.models.request.directory.TerraformAsyncRequestWithScriptsDirectory;
import org.eclipse.xpanse.terra.boot.models.request.directory.TerraformRequestWithScriptsDirectory;
import org.eclipse.xpanse.terra.boot.models.request.git.TerraformAsyncRequestWithScriptsGitRepo;
import org.eclipse.xpanse.terra.boot.models.request.git.TerraformRequestWithScriptsGitRepo;
import org.eclipse.xpanse.terra.boot.models.request.scripts.TerraformAsyncRequestWithScripts;
import org.eclipse.xpanse.terra.boot.models.request.scripts.TerraformRequestWithScripts;
import org.eclipse.xpanse.terra.boot.terraform.tool.TerraformVersionsHelper;

/** Data model for the Terraform request. */
@Data
@Schema(
        description = "Terraform request base",
        subTypes = {
            TerraformRequestWithScriptsDirectory.class,
            TerraformAsyncRequestWithScriptsDirectory.class,
            TerraformRequestWithScripts.class,
            TerraformAsyncRequestWithScripts.class,
            TerraformRequestWithScriptsGitRepo.class,
            TerraformAsyncRequestWithScriptsGitRepo.class,
        })
public abstract class TerraformRequest implements Serializable {

    @Serial private static final long serialVersionUID = 10696793105264423L;

    @Schema(description = "Id of the request.")
    @NotNull
    private UUID requestId;

    @NotNull
    @Schema(description = "Type of the terraform request.")
    private RequestType requestType;

    @NotNull
    @NotBlank
    @Pattern(regexp = TerraformVersionsHelper.TERRAFORM_REQUIRED_VERSION_REGEX)
    @Schema(description = "The required version of terraform which will execute the scripts.")
    private String terraformVersion;

    @NotNull
    @Schema(
            description =
                    "Flag to control if the deployment must only generate the terraform "
                            + "or it must also apply the changes.")
    private Boolean isPlanOnly;

    @NotNull
    @Schema(
            description =
                    "Key-value pairs of variables that must be used to execute the "
                            + "Terraform request.")
    private Map<String, Object> variables;

    @Schema(
            description =
                    "Key-value pairs of variables that must be injected as environment "
                            + "variables to terraform process.")
    private Map<String, String> envVariables;

    @Schema(description = "Terraform state as a string.")
    private String tfState;
}
