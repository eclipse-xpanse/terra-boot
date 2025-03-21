/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.models.request.scripts;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.xpanse.terra.boot.models.request.webhook.WebhookConfig;

/** The terraform async request for executing command based on the scripts files. */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "Terraform async request with scripts files")
public class TerraformAsyncRequestWithScripts extends TerraformRequestWithScripts
        implements Serializable {

    @Serial private static final long serialVersionUID = -751134396582688022L;

    @NotNull
    @Schema(description = "Configuration information of webhook.")
    private WebhookConfig webhookConfig;
}
