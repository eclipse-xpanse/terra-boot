/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.terraform.service;

import static org.eclipse.xpanse.terra.boot.logging.CustomRequestIdGenerator.REQUEST_ID;

import jakarta.annotation.Resource;
import java.io.File;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.terra.boot.models.TerraBootSystemStatus;
import org.eclipse.xpanse.terra.boot.models.enums.RequestType;
import org.eclipse.xpanse.terra.boot.models.exceptions.InvalidTerraformRequestException;
import org.eclipse.xpanse.terra.boot.models.request.TerraformRequest;
import org.eclipse.xpanse.terra.boot.models.request.directory.TerraformAsyncRequestWithScriptsDirectory;
import org.eclipse.xpanse.terra.boot.models.request.directory.TerraformRequestWithScriptsDirectory;
import org.eclipse.xpanse.terra.boot.models.request.git.TerraformAsyncRequestWithScriptsGitRepo;
import org.eclipse.xpanse.terra.boot.models.request.git.TerraformRequestWithScriptsGitRepo;
import org.eclipse.xpanse.terra.boot.models.request.scripts.TerraformAsyncRequestWithScripts;
import org.eclipse.xpanse.terra.boot.models.request.scripts.TerraformRequestWithScripts;
import org.eclipse.xpanse.terra.boot.models.response.TerraformPlan;
import org.eclipse.xpanse.terra.boot.models.response.TerraformResult;
import org.eclipse.xpanse.terra.boot.models.validation.TerraformValidationResult;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/** Terraform service classes are deployed form Directory. */
@Slf4j
@Service
public class TerraformRequestService {

    @Resource private TerraformScriptsDirectoryService terraformScriptsDirectoryService;
    @Resource private TerraformScriptsService terraformScriptsService;
    @Resource private TerraformScriptsGitRepoService terraformScriptsGitRepoService;
    @Resource private TerraformScriptsDirectoryHelper scriptsDirectoryHelper;

    /** Handle the request of health check. */
    public TerraBootSystemStatus healthCheck() {
        return terraformScriptsDirectoryService.tfHealthCheck();
    }

    /**
     * Handle the validation request.
     *
     * @param request request.
     * @return TerraformValidationResult.
     */
    public TerraformValidationResult handleValidateRequest(TerraformRequest request) {
        validateTerraformRequest(request);
        return switch (request) {
            case TerraformRequestWithScriptsDirectory requestWithDirectory ->
                    terraformScriptsDirectoryService.tfValidateFromDirectory(requestWithDirectory);
            case TerraformRequestWithScriptsGitRepo requestWithScriptsGitRepo ->
                    terraformScriptsGitRepoService.validateWithScriptsGitRepo(
                            requestWithScriptsGitRepo);
            case TerraformRequestWithScripts requestWithScripts ->
                    terraformScriptsService.validateWithScripts(requestWithScripts);
            default -> throw new InvalidTerraformRequestException("Invalid terraform request.");
        };
    }

    /**
     * Handle the plan request and return the TerraformPlan.
     *
     * @param request request.
     * @return TerraformPlan.
     */
    public TerraformPlan getTerraformPlan(TerraformRequest request) {
        validateTerraformRequest(request);
        return switch (request) {
            case TerraformRequestWithScriptsDirectory requestWithDirectory ->
                    terraformScriptsDirectoryService.getTerraformPlanFromDirectory(
                            requestWithDirectory);
            case TerraformRequestWithScriptsGitRepo requestWithScriptsGitRepo ->
                    terraformScriptsGitRepoService.getTerraformPlanFromGitRepo(
                            requestWithScriptsGitRepo);
            case TerraformRequestWithScripts requestWithScripts ->
                    terraformScriptsService.getTerraformPlanFromScripts(requestWithScripts);
            default -> throw new InvalidTerraformRequestException("Invalid terraform request.");
        };
    }

    /**
     * Handle the terraform request and return the TerraformResult.
     *
     * @param request request.
     * @return TerraformResult.
     */
    public TerraformResult handleTerraformRequest(TerraformRequest request) {
        try {
            validateTerraformRequest(request);
            switch (request) {
                case TerraformRequestWithScriptsDirectory requestWithDirectory -> {
                    if (RequestType.DEPLOY == requestWithDirectory.getRequestType()) {
                        return terraformScriptsDirectoryService.deployFromDirectory(
                                requestWithDirectory);
                    } else if (RequestType.MODIFY == requestWithDirectory.getRequestType()) {
                        return terraformScriptsDirectoryService.modifyFromDirectory(
                                requestWithDirectory);
                    } else if (RequestType.DESTROY == requestWithDirectory.getRequestType()) {
                        return terraformScriptsDirectoryService.destroyFromDirectory(
                                requestWithDirectory);
                    }
                    throw new InvalidTerraformRequestException("Invalid request type.");
                }
                case TerraformRequestWithScriptsGitRepo requestWithScriptsGitRepo -> {
                    if (RequestType.DEPLOY == requestWithScriptsGitRepo.getRequestType()) {
                        return terraformScriptsGitRepoService.deployFromGitRepo(
                                requestWithScriptsGitRepo);
                    } else if (RequestType.MODIFY == requestWithScriptsGitRepo.getRequestType()) {
                        return terraformScriptsGitRepoService.modifyFromGitRepo(
                                requestWithScriptsGitRepo);
                    } else if (RequestType.DESTROY == requestWithScriptsGitRepo.getRequestType()) {
                        return terraformScriptsGitRepoService.destroyFromGitRepo(
                                requestWithScriptsGitRepo);
                    }
                    throw new InvalidTerraformRequestException("Invalid request type.");
                }
                case TerraformRequestWithScripts requestWithScripts -> {
                    if (RequestType.DEPLOY == requestWithScripts.getRequestType()) {
                        return terraformScriptsService.deployWithScripts(requestWithScripts);
                    } else if (RequestType.MODIFY == requestWithScripts.getRequestType()) {
                        return terraformScriptsService.modifyWithScripts(requestWithScripts);
                    } else if (RequestType.DESTROY == requestWithScripts.getRequestType()) {
                        return terraformScriptsService.destroyWithScripts(requestWithScripts);
                    }
                    throw new InvalidTerraformRequestException("Invalid request type.");
                }
                default -> throw new InvalidTerraformRequestException("Invalid terraform request.");
            }
        } catch (Exception e) {
            log.error("Error occurred while handling terraform request.", e);
            return TerraformResult.builder()
                    .requestId(request.getRequestId())
                    .isCommandSuccessful(false)
                    .commandStdOutput(null)
                    .commandStdError(e.getMessage())
                    .terraformState(null)
                    .generatedFileContentMap(null)
                    .build();
        }
    }

    /**
     * Handle the async deployment request.
     *
     * @param request request.
     */
    public void handleAsyncDeploymentRequest(TerraformRequest request) {
        validateTerraformRequest(request);
        processAsyncDeploymentRequest(request);
    }

    /**
     * Process the async deployment request.
     *
     * @param request request.
     */
    public void processAsyncDeploymentRequest(TerraformRequest request) {
        switch (request) {
            case TerraformAsyncRequestWithScriptsDirectory requestWithDirectory -> {
                if (RequestType.DEPLOY == requestWithDirectory.getRequestType()) {
                    terraformScriptsDirectoryService.asyncDeployWithScripts(requestWithDirectory);
                } else if (RequestType.MODIFY == requestWithDirectory.getRequestType()) {
                    terraformScriptsDirectoryService.asyncModifyWithScripts(requestWithDirectory);
                } else if (RequestType.DESTROY == requestWithDirectory.getRequestType()) {
                    terraformScriptsDirectoryService.asyncDestroyWithScripts(requestWithDirectory);
                }
            }
            case TerraformAsyncRequestWithScriptsGitRepo requestWithScriptsGitRepo -> {
                if (RequestType.DEPLOY == requestWithScriptsGitRepo.getRequestType()) {
                    terraformScriptsGitRepoService.asyncDeployFromGitRepo(
                            requestWithScriptsGitRepo);
                } else if (RequestType.MODIFY == requestWithScriptsGitRepo.getRequestType()) {
                    terraformScriptsGitRepoService.asyncModifyFromGitRepo(
                            requestWithScriptsGitRepo);
                } else if (RequestType.DESTROY == requestWithScriptsGitRepo.getRequestType()) {
                    terraformScriptsGitRepoService.asyncDestroyFromGitRepo(
                            requestWithScriptsGitRepo);
                }
            }
            case TerraformAsyncRequestWithScripts requestWithScripts -> {
                if (RequestType.DEPLOY == requestWithScripts.getRequestType()) {
                    terraformScriptsService.asyncDeployWithScripts(requestWithScripts);
                } else if (RequestType.MODIFY == requestWithScripts.getRequestType()) {
                    terraformScriptsService.asyncModifyWithScripts(requestWithScripts);
                } else if (RequestType.DESTROY == requestWithScripts.getRequestType()) {
                    terraformScriptsService.asyncDestroyWithScripts(requestWithScripts);
                }
            }
            default -> throw new InvalidTerraformRequestException("Invalid terraform request.");
        }
    }

    /**
     * Validate the terraform request.
     *
     * @param request request.
     */
    public void validateTerraformRequest(TerraformRequest request) {
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
}
