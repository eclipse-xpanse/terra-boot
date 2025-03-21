/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.models.request.directory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.File;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.xpanse.terra.boot.models.request.TerraformRequest;

/**
 * The terraform request for executing command based on the directory where the scripts file exist.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "Terraform request with directory where scripts files exist")
public class TerraformRequestWithScriptsDirectory extends TerraformRequest {

    @NotNull
    @NotBlank
    @Schema(description = "Directory where the Terraform scripts files exist.")
    private String scriptsDirectory;

    @Hidden @JsonIgnore private List<File> scriptFiles;
}
