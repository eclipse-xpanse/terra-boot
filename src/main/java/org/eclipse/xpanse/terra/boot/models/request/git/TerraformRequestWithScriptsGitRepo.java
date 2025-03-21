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
import org.eclipse.xpanse.terra.boot.models.request.TerraformRequest;

/** The terraform request for executing command based on the scripts git repo details. */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "Terraform async request with scripts git repo")
public class TerraformRequestWithScriptsGitRepo extends TerraformRequest implements Serializable {

    @Serial private static final long serialVersionUID = -9154036923917579783L;

    @NotNull
    @Schema(description = "GIT Repo details from where the scripts can be fetched.")
    private TerraformScriptsGitRepoDetails gitRepoDetails;
}
