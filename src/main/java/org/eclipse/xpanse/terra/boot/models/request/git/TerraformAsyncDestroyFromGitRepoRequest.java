/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.models.request.git;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.xpanse.terra.boot.models.request.webhook.WebhookConfig;

/** Data model for terraform async destroy requests using scripts from a GIT Repo. */
@EqualsAndHashCode(callSuper = true)
@Data
public class TerraformAsyncDestroyFromGitRepoRequest extends TerraformDestroyFromGitRepoRequest {

    @NotNull
    @Schema(description = "Configuration information of webhook.")
    private WebhookConfig webhookConfig;
}
