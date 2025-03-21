/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.models.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.terra.boot.models.enums.HealthStatus;

/** Describes health status of the system. */
@Data
public class TerraBootSystemStatus implements Serializable {

    @Serial private static final long serialVersionUID = -6039352796356100544L;

    @NotNull
    @Schema(description = "ID of the request.")
    private UUID requestId;

    @NotNull
    @Schema(description = "The health status of terra-boot service.")
    private HealthStatus healthStatus;

    @NotBlank
    @Schema(description = "The service type of terra-boot.")
    private String serviceType;

    @NotBlank
    @Schema(description = "The url of terra-boot service.")
    private String serviceUrl;

    @Schema(description = "The error message of terra-boot service.")
    private String errorMessage;
}
