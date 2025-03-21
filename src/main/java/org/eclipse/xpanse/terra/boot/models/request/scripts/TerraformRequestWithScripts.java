/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.models.request.scripts;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.xpanse.terra.boot.models.request.TerraformRequest;

/** The terraform request for executing command based on the scripts files. */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "Terraform request with scripts files")
public class TerraformRequestWithScripts extends TerraformRequest implements Serializable {

    @Serial private static final long serialVersionUID = 7464467836284819109L;

    @NotNull
    @NotEmpty
    @Schema(
            description =
                    "Map stores file name and content of all script files for deploy request.")
    private Map<String, String> scriptFiles;
}
