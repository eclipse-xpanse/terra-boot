/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.terraform.service;

import jakarta.annotation.Resource;
import java.io.File;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.terra.boot.models.request.directory.TerraformAsyncRequestWithScriptsDirectory;
import org.eclipse.xpanse.terra.boot.models.request.directory.TerraformRequestWithScriptsDirectory;
import org.eclipse.xpanse.terra.boot.models.request.scripts.TerraformAsyncRequestWithScripts;
import org.eclipse.xpanse.terra.boot.models.request.scripts.TerraformRequestWithScripts;
import org.eclipse.xpanse.terra.boot.models.response.TerraformPlan;
import org.eclipse.xpanse.terra.boot.models.response.TerraformResult;
import org.eclipse.xpanse.terra.boot.models.validation.TerraformValidationResult;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/** Terraform service classes are deployed form Scripts. */
@Slf4j
@Service
public class TerraformScriptsService {

    @Resource private TerraformScriptsDirectoryHelper scriptsHelper;
    @Resource private TerraformScriptsDirectoryService directoryService;

    /** /** Method of deployment a service using a script. */
    public TerraformValidationResult validateWithScripts(TerraformRequestWithScripts request) {
        TerraformRequestWithScriptsDirectory requestWithDirectory =
                getTerraformRequestWithDirectory(request);
        return directoryService.tfValidateFromDirectory(requestWithDirectory);
    }

    /** Method of deployment a service using a script. */
    public TerraformResult deployWithScripts(TerraformRequestWithScripts request) {
        TerraformRequestWithScriptsDirectory requestWithDirectory =
                getTerraformRequestWithDirectory(request);
        return directoryService.deployFromDirectory(requestWithDirectory);
    }

    /** Method of modify a service using a script. */
    public TerraformResult modifyWithScripts(TerraformRequestWithScripts request) {
        TerraformRequestWithScriptsDirectory requestWithDirectory =
                getTerraformRequestWithDirectory(request);
        return directoryService.modifyFromDirectory(requestWithDirectory);
    }

    /** Method of destroy a service using a script. */
    public TerraformResult destroyWithScripts(TerraformRequestWithScripts request) {
        TerraformRequestWithScriptsDirectory requestWithDirectory =
                getTerraformRequestWithDirectory(request);
        return directoryService.destroyFromDirectory(requestWithDirectory);
    }

    /** Method to get terraform plan. */
    public TerraformPlan getTerraformPlanFromScripts(TerraformRequestWithScripts request) {
        TerraformRequestWithScriptsDirectory requestWithDirectory =
                getTerraformRequestWithDirectory(request);
        return directoryService.getTerraformPlanFromDirectory(requestWithDirectory);
    }

    /** Async deploy a source by terraform. */
    public void asyncDeployWithScripts(TerraformAsyncRequestWithScripts asyncRequest) {
        TerraformAsyncRequestWithScriptsDirectory requestWithDirectory =
                (TerraformAsyncRequestWithScriptsDirectory)
                        getTerraformRequestWithDirectory(asyncRequest);
        directoryService.asyncDeployWithScripts(requestWithDirectory);
    }

    /** Async modify a source by terraform. */
    public void asyncModifyWithScripts(TerraformAsyncRequestWithScripts asyncRequest) {
        TerraformAsyncRequestWithScriptsDirectory requestWithDirectory =
                (TerraformAsyncRequestWithScriptsDirectory)
                        getTerraformRequestWithDirectory(asyncRequest);
        directoryService.asyncDeployWithScripts(requestWithDirectory);
    }

    /** Async destroy resource of the service. */
    public void asyncDestroyWithScripts(TerraformAsyncRequestWithScripts asyncRequest) {
        TerraformAsyncRequestWithScriptsDirectory requestWithDirectory =
                (TerraformAsyncRequestWithScriptsDirectory)
                        getTerraformRequestWithDirectory(asyncRequest);
        directoryService.asyncDeployWithScripts(requestWithDirectory);
    }

    private TerraformRequestWithScriptsDirectory getTerraformRequestWithDirectory(
            TerraformRequestWithScripts request) {
        TerraformRequestWithScriptsDirectory requestWithDirectory =
                new TerraformRequestWithScriptsDirectory();
        if (request instanceof TerraformAsyncRequestWithScripts) {
            requestWithDirectory = new TerraformAsyncRequestWithScriptsDirectory();
        }

        BeanUtils.copyProperties(request, requestWithDirectory);
        String scriptsPath = scriptsHelper.buildTaskWorkspace(request.getRequestId().toString());
        requestWithDirectory.setScriptsDirectory(scriptsPath);
        List<File> scriptFilesList =
                scriptsHelper.prepareDeploymentFilesWithScripts(
                        scriptsPath, request.getScriptFiles(), request.getTfState());
        requestWithDirectory.setScriptFiles(scriptFilesList);
        return requestWithDirectory;
    }
}
