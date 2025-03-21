/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.terraform.service;

import static org.eclipse.xpanse.terra.boot.logging.CustomRequestIdGenerator.REQUEST_ID;

import jakarta.annotation.Resource;
import java.io.File;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.terra.boot.models.enums.RequestType;
import org.eclipse.xpanse.terra.boot.models.exceptions.InvalidTerraformRequestException;
import org.eclipse.xpanse.terra.boot.models.exceptions.UnsupportedEnumValueException;
import org.eclipse.xpanse.terra.boot.models.request.TerraformRequest;
import org.eclipse.xpanse.terra.boot.models.request.directory.TerraformAsyncRequestWithScriptsDirectory;
import org.eclipse.xpanse.terra.boot.models.request.directory.TerraformRequestWithScriptsDirectory;
import org.eclipse.xpanse.terra.boot.models.request.git.TerraformAsyncRequestWithScriptsGitRepo;
import org.eclipse.xpanse.terra.boot.models.request.git.TerraformRequestWithScriptsGitRepo;
import org.eclipse.xpanse.terra.boot.models.request.git.TerraformScriptsGitRepoDetails;
import org.eclipse.xpanse.terra.boot.models.request.scripts.TerraformAsyncRequestWithScripts;
import org.eclipse.xpanse.terra.boot.models.request.scripts.TerraformRequestWithScripts;
import org.eclipse.xpanse.terra.boot.models.response.TerraBootSystemStatus;
import org.eclipse.xpanse.terra.boot.models.response.TerraformPlan;
import org.eclipse.xpanse.terra.boot.models.response.TerraformResult;
import org.eclipse.xpanse.terra.boot.models.response.validation.TerraformValidateDiagnostics;
import org.eclipse.xpanse.terra.boot.models.response.validation.TerraformValidationResult;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/** Terraform service classes are deployed form Directory. */
@Slf4j
@Service
public class TerraformRequestService {

    @Resource private TerraformScriptsDirectoryService terraformScriptsDirectoryService;
    @Resource private TerraformScriptsDirectoryHelper scriptsDirectoryHelper;

    /** Handle the request of health check. */
    public TerraBootSystemStatus healthCheck(UUID requestId) {
        MDC.put(REQUEST_ID, requestId.toString());
        return terraformScriptsDirectoryService.tfHealthCheck(requestId);
    }

    /**
     * Handle terraform validate request and return result.
     *
     * @param request request.
     * @return TerraformValidationResult.
     */
    public TerraformValidationResult handleTerraformValidateRequest(TerraformRequest request) {
        TerraformRequestWithScriptsDirectory requestWithDirectory =
                convertRequestWithScriptsDirectory(request);
        return terraformScriptsDirectoryService.tfValidateWithScriptsDirectory(
                requestWithDirectory);
    }

    /**
     * Handle the plan request and return the TerraformPlan.
     *
     * @param request request.
     * @return TerraformPlan.
     */
    public TerraformPlan handleTerraformPlanRequest(TerraformRequest request) {
        TerraformRequestWithScriptsDirectory requestWithDirectory =
                convertRequestWithScriptsDirectory(request);
        return terraformScriptsDirectoryService.getTerraformPlanWithScriptsDirectory(
                requestWithDirectory);
    }

    /**
     * Handle the terraform request and return the TerraformResult.
     *
     * @param request request.
     * @return TerraformResult.
     */
    public TerraformResult handleTerraformDeploymentRequest(TerraformRequest request) {
        TerraformRequestWithScriptsDirectory requestWithDirectory =
                convertRequestWithScriptsDirectory(request);
        switch (request.getRequestType()) {
            case RequestType.DEPLOY -> {
                return terraformScriptsDirectoryService.deployWithScriptsDirectory(
                        requestWithDirectory);
            }
            case RequestType.MODIFY -> {
                return terraformScriptsDirectoryService.modifyWithScriptsDirectory(
                        requestWithDirectory);
            }
            case RequestType.DESTROY -> {
                return terraformScriptsDirectoryService.destroyWithScriptsDirectory(
                        requestWithDirectory);
            }
            default ->
                    throw new UnsupportedEnumValueException(
                            String.format(
                                    "RequestType value %s is not supported.",
                                    request.getRequestType().toValue()));
        }
    }

    /**
     * Process the async deployment request.
     *
     * @param request request.
     */
    public void processAsyncDeploymentRequest(TerraformRequest request) {
        TerraformAsyncRequestWithScriptsDirectory requestWithDirectory =
                (TerraformAsyncRequestWithScriptsDirectory)
                        convertRequestWithScriptsDirectory(request);
        switch (request.getRequestType()) {
            case RequestType.DEPLOY ->
                    terraformScriptsDirectoryService.asyncDeployWithScriptsDirectory(
                            requestWithDirectory);
            case RequestType.MODIFY ->
                    terraformScriptsDirectoryService.asyncModifyWithScriptsDirectory(
                            requestWithDirectory);
            case RequestType.DESTROY ->
                    terraformScriptsDirectoryService.asyncDestroyWithScriptsDirectory(
                            requestWithDirectory);
            default ->
                    throw new UnsupportedEnumValueException(
                            String.format(
                                    "RequestType value %s is not supported.",
                                    request.getRequestType().toValue()));
        }
    }

    private TerraformRequestWithScriptsDirectory convertRequestWithScriptsDirectory(
            TerraformRequest request) {
        validateTerraformRequest(request);
        return switch (request) {
            case TerraformRequestWithScriptsDirectory requestWithDirectory -> requestWithDirectory;
            case TerraformRequestWithScriptsGitRepo requestWithScriptsGitRepo ->
                    convertRequestWithGitToDirectory(requestWithScriptsGitRepo);
            case TerraformRequestWithScripts requestWithScripts ->
                    convertRequestWithScriptsToDirectory(requestWithScripts);
            default ->
                    throw new UnsupportedEnumValueException(
                            String.format(
                                    "RequestType value %s is not supported.",
                                    request.getRequestType().toValue()));
        };
    }

    /**
     * Validate the terraform request.
     *
     * @param request request.
     */
    private void validateTerraformRequest(TerraformRequest request) {
        MDC.put(REQUEST_ID, request.getRequestId().toString());
        if (RequestType.DESTROY == request.getRequestType()
                || RequestType.MODIFY == request.getRequestType()) {
            if (StringUtils.isBlank(request.getTfState())) {
                String errorMessage =
                        String.format(
                                "Terraform state is required for request with order type %s.",
                                request.getRequestType());
                log.error(errorMessage);
                throw new InvalidTerraformRequestException(errorMessage);
            }
        }
        if (request instanceof TerraformRequestWithScriptsDirectory requestWithDirectory) {
            List<File> scriptFiles =
                    scriptsDirectoryHelper.getDeploymentFilesFromTaskWorkspace(
                            requestWithDirectory.getScriptsDirectory());
            if (CollectionUtils.isEmpty(scriptFiles)) {
                String errorMessage =
                        String.format(
                                "No Terraform scripts files found in the directory %s.",
                                requestWithDirectory.getScriptsDirectory());
                log.error(errorMessage);
                throw new InvalidTerraformRequestException(errorMessage);
            }
            requestWithDirectory.setScriptFiles(scriptFiles);
        }
    }

    /**
     * Get the error validate result.
     *
     * @param request request
     * @param e exception
     * @return TerraformValidationResult
     */
    public TerraformValidationResult getErrorValidateResult(TerraformRequest request, Exception e) {
        TerraformValidationResult result = new TerraformValidationResult();
        result.setRequestId(request.getRequestId());
        result.setValid(false);
        result.setTerraformVersionUsed(request.getTerraformVersion());
        TerraformValidateDiagnostics diagnostics = new TerraformValidateDiagnostics();
        diagnostics.setDetail(e.getMessage());
        result.setDiagnostics(List.of(diagnostics));
        return result;
    }

    /**
     * Get the error plan result.
     *
     * @param request request
     * @param e exception
     * @return TerraformPlan
     */
    public TerraformPlan getErrorPlanResult(TerraformRequest request, Exception e) {
        return TerraformPlan.builder()
                .requestId(request.getRequestId())
                .terraformVersionUsed(request.getTerraformVersion())
                .errorMessage(e.getMessage())
                .build();
    }

    /**
     * Get the error deployment result.
     *
     * @param request request
     * @param e exception
     * @return TerraformResult
     */
    public TerraformResult getErrorDeploymentResult(TerraformRequest request, Exception e) {
        return TerraformResult.builder()
                .requestId(request.getRequestId())
                .terraformVersionUsed(request.getTerraformVersion())
                .isCommandSuccessful(false)
                .commandStdError(e.getMessage())
                .build();
    }

    /**
     * Transform TerraformRequestWithScriptsGitRepo to TerraformRequestWithScriptsDirectory.
     *
     * @param request request with git repo.
     * @return request with scripts directory.
     */
    private TerraformRequestWithScriptsDirectory convertRequestWithGitToDirectory(
            TerraformRequestWithScriptsGitRepo request) {
        TerraformRequestWithScriptsDirectory requestWithDirectory =
                new TerraformRequestWithScriptsDirectory();
        if (request instanceof TerraformAsyncRequestWithScriptsGitRepo) {
            requestWithDirectory = new TerraformAsyncRequestWithScriptsDirectory();
        }
        BeanUtils.copyProperties(request, requestWithDirectory);
        String taskWorkspace =
                scriptsDirectoryHelper.buildTaskWorkspace(request.getRequestId().toString());
        String scriptsPath =
                getScriptsLocationInTaskWorkspace(request.getGitRepoDetails(), taskWorkspace);
        requestWithDirectory.setScriptsDirectory(scriptsPath);
        List<File> scriptFiles =
                scriptsDirectoryHelper.prepareDeploymentFilesWithGitRepo(
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

    /**
     * Transform TerraformRequestWithScripts to TerraformRequestWithScriptsDirectory.
     *
     * @param request request with scripts.
     * @return request with scripts directory.
     */
    private TerraformRequestWithScriptsDirectory convertRequestWithScriptsToDirectory(
            TerraformRequestWithScripts request) {
        TerraformRequestWithScriptsDirectory requestWithDirectory =
                new TerraformRequestWithScriptsDirectory();
        if (request instanceof TerraformAsyncRequestWithScripts) {
            requestWithDirectory = new TerraformAsyncRequestWithScriptsDirectory();
        }
        BeanUtils.copyProperties(request, requestWithDirectory);
        String scriptsPath =
                scriptsDirectoryHelper.buildTaskWorkspace(request.getRequestId().toString());
        requestWithDirectory.setScriptsDirectory(scriptsPath);
        List<File> scriptFilesList =
                scriptsDirectoryHelper.prepareDeploymentFilesWithScripts(
                        scriptsPath, request.getScriptFiles(), request.getTfState());
        requestWithDirectory.setScriptFiles(scriptFilesList);
        return requestWithDirectory;
    }
}
