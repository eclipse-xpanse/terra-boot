/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.models.request.git;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.xpanse.terra.boot.models.request.webhook.WebhookConfig;

/** The terraform async request for executing command based on the scripts git repo details. */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "Terraform request with scripts git repo")
public class TerraformAsyncRequestWithScriptsGitRepo extends TerraformRequestWithScriptsGitRepo
        implements Serializable {

    @Serial private static final long serialVersionUID = 6509125273252260415L;

    @NotNull
    @Schema(description = "Configuration information of webhook.")
    private WebhookConfig webhookConfig;
}
