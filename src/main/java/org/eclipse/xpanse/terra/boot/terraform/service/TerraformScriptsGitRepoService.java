/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.terraform.service;

import jakarta.annotation.Resource;
import java.io.File;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.terra.boot.models.request.directory.TerraformAsyncRequestWithScriptsDirectory;
import org.eclipse.xpanse.terra.boot.models.request.directory.TerraformRequestWithScriptsDirectory;
import org.eclipse.xpanse.terra.boot.models.request.git.TerraformAsyncRequestWithScriptsGitRepo;
import org.eclipse.xpanse.terra.boot.models.request.git.TerraformRequestWithScriptsGitRepo;
import org.eclipse.xpanse.terra.boot.models.request.git.TerraformScriptsGitRepoDetails;
import org.eclipse.xpanse.terra.boot.models.response.TerraformPlan;
import org.eclipse.xpanse.terra.boot.models.response.TerraformResult;
import org.eclipse.xpanse.terra.boot.models.validation.TerraformValidationResult;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/** Bean to manage all Terraform execution using scripts from a GIT Repo. */
@Slf4j
@Component
public class TerraformScriptsGitRepoService {

    @Resource private TerraformScriptsDirectoryHelper scriptsHelper;
    @Resource private TerraformScriptsDirectoryService directoryService;

    /** Method of deployment a service using a script. */
    public TerraformValidationResult validateWithScriptsGitRepo(
            TerraformRequestWithScriptsGitRepo request) {
        TerraformRequestWithScriptsDirectory requestWithDirectory =
                getTerraformRequestWithDirectory(request);
        return directoryService.tfValidateFromDirectory(requestWithDirectory);
    }

    /** Method to get terraform plan. */
    public TerraformPlan getTerraformPlanFromGitRepo(TerraformRequestWithScriptsGitRepo request) {
        TerraformRequestWithScriptsDirectory requestWithDirectory =
                getTerraformRequestWithDirectory(request);
        return directoryService.getTerraformPlanFromDirectory(requestWithDirectory);
    }

    /** Method of deployment a service using a script. */
    public TerraformResult deployFromGitRepo(TerraformRequestWithScriptsGitRepo request) {
        TerraformRequestWithScriptsDirectory requestWithDirectory =
                getTerraformRequestWithDirectory(request);
        return directoryService.deployFromDirectory(requestWithDirectory);
    }

    /** Method of modify a service using a script. */
    public TerraformResult modifyFromGitRepo(TerraformRequestWithScriptsGitRepo request) {
        TerraformRequestWithScriptsDirectory requestWithDirectory =
                getTerraformRequestWithDirectory(request);
        return directoryService.modifyFromDirectory(requestWithDirectory);
    }

    /** Method of destroy a service using a script. */
    public TerraformResult destroyFromGitRepo(TerraformRequestWithScriptsGitRepo request) {
        TerraformRequestWithScriptsDirectory requestWithDirectory =
                getTerraformRequestWithDirectory(request);
        return directoryService.destroyFromDirectory(requestWithDirectory);
    }

    /** deploy a source by terraform. */
    public void asyncDeployFromGitRepo(TerraformAsyncRequestWithScriptsGitRepo asyncRequest) {
        TerraformAsyncRequestWithScriptsDirectory asyncRequestWithDirectory =
                (TerraformAsyncRequestWithScriptsDirectory)
                        getTerraformRequestWithDirectory(asyncRequest);
        directoryService.asyncDeployWithScripts(asyncRequestWithDirectory);
    }

    /** modify a source by terraform. */
    public void asyncModifyFromGitRepo(TerraformAsyncRequestWithScriptsGitRepo asyncRequest) {
        TerraformAsyncRequestWithScriptsDirectory asyncRequestWithDirectory =
                (TerraformAsyncRequestWithScriptsDirectory)
                        getTerraformRequestWithDirectory(asyncRequest);
        directoryService.asyncModifyWithScripts(asyncRequestWithDirectory);
    }

    /** destroys resource of the service. */
    public void asyncDestroyFromGitRepo(TerraformAsyncRequestWithScriptsGitRepo asyncRequest) {
        TerraformAsyncRequestWithScriptsDirectory asyncRequestWithDirectory =
                (TerraformAsyncRequestWithScriptsDirectory)
                        getTerraformRequestWithDirectory(asyncRequest);
        directoryService.asyncDestroyWithScripts(asyncRequestWithDirectory);
    }

    private TerraformRequestWithScriptsDirectory getTerraformRequestWithDirectory(
            TerraformRequestWithScriptsGitRepo request) {
        TerraformRequestWithScriptsDirectory requestWithDirectory =
                new TerraformRequestWithScriptsDirectory();
        if (request instanceof TerraformAsyncRequestWithScriptsGitRepo) {
            requestWithDirectory = new TerraformAsyncRequestWithScriptsDirectory();
        }
        BeanUtils.copyProperties(request, requestWithDirectory);
        String taskWorkspace = scriptsHelper.buildTaskWorkspace(request.getRequestId().toString());
        String scriptsPath =
                getScriptsLocationInTaskWorkspace(request.getGitRepoDetails(), taskWorkspace);
        requestWithDirectory.setScriptsDirectory(scriptsPath);
        List<File> scriptFiles =
                scriptsHelper.prepareDeploymentFilesWithGitRepo(
                        taskWorkspace, request.getGitRepoDetails(), request.getTfState());
        requestWithDirectory.setScriptFiles(scriptFiles);
        return requestWithDirectory;
    }

    private String getScriptsLocationInTaskWorkspace(
            TerraformScriptsGitRepoDetails scriptsGitRepoDetails, String taskWorkSpace) {
        if (StringUtils.isNotBlank(scriptsGitRepoDetails.getScriptPath())) {
            return taskWorkSpace + File.separator + scriptsGitRepoDetails.getScriptPath();
        }
        return taskWorkSpace;
    }
}
