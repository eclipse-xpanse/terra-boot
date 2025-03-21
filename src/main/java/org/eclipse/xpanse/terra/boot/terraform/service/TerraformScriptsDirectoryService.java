/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.terraform.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.terra.boot.async.TaskConfiguration;
import org.eclipse.xpanse.terra.boot.models.enums.HealthStatus;
import org.eclipse.xpanse.terra.boot.models.exceptions.InvalidTerraformToolException;
import org.eclipse.xpanse.terra.boot.models.exceptions.TerraformExecutorException;
import org.eclipse.xpanse.terra.boot.models.request.directory.TerraformAsyncRequestWithScriptsDirectory;
import org.eclipse.xpanse.terra.boot.models.request.directory.TerraformRequestWithScriptsDirectory;
import org.eclipse.xpanse.terra.boot.models.response.TerraBootSystemStatus;
import org.eclipse.xpanse.terra.boot.models.response.TerraformPlan;
import org.eclipse.xpanse.terra.boot.models.response.TerraformResult;
import org.eclipse.xpanse.terra.boot.models.response.validation.TerraformValidationResult;
import org.eclipse.xpanse.terra.boot.terraform.TerraformExecutor;
import org.eclipse.xpanse.terra.boot.terraform.tool.TerraformInstaller;
import org.eclipse.xpanse.terra.boot.terraform.tool.TerraformVersionsHelper;
import org.eclipse.xpanse.terra.boot.terraform.utils.SystemCmdResult;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/** Terraform service classes are deployed form Directory. */
@Slf4j
@Service
public class TerraformScriptsDirectoryService {

    private static final String HELLO_WORLD_TF_NAME = "hello_world.tf";
    private static final String HELLO_WORLD_TEMPLATE =
            """
            output "hello_world" {
                value = "Hello, World!"
            }
            """;
    @Resource private TerraformExecutor executor;
    @Resource private TerraformInstaller installer;
    @Resource private RestTemplate restTemplate;
    @Resource private TerraformVersionsHelper versionHelper;
    @Resource private TerraformScriptsDirectoryHelper scriptsHelper;
    @Resource private TerraformResultPersistenceManage terraformResultPersistenceManage;

    /**
     * Perform Terra-Boot health checks by creating a Terraform test configuration file.
     *
     * @return TerraBootSystemStatus.
     */
    public TerraBootSystemStatus tfHealthCheck(UUID requestId) {
        String taskWorkspace = scriptsHelper.buildTaskWorkspace(requestId.toString());
        scriptsHelper.prepareDeploymentFilesWithScripts(
                taskWorkspace, Map.of(HELLO_WORLD_TF_NAME, HELLO_WORLD_TEMPLATE), null);
        TerraformRequestWithScriptsDirectory request = new TerraformRequestWithScriptsDirectory();
        request.setScriptsDirectory(taskWorkspace);
        TerraformValidationResult terraformValidationResult =
                tfValidateWithScriptsDirectory(request);
        TerraBootSystemStatus systemStatus = new TerraBootSystemStatus();
        systemStatus.setRequestId(requestId);
        if (terraformValidationResult.isValid()) {
            systemStatus.setHealthStatus(HealthStatus.OK);
            return systemStatus;
        }
        scriptsHelper.deleteTaskWorkspace(taskWorkspace);
        systemStatus.setHealthStatus(HealthStatus.NOK);
        return systemStatus;
    }

    /**
     * Executes terraform validate command.
     *
     * @return TfValidationResult.
     */
    public TerraformValidationResult tfValidateWithScriptsDirectory(
            TerraformRequestWithScriptsDirectory request) {
        try {
            String executorPath =
                    installer.getExecutorPathThatMatchesRequiredVersion(
                            request.getTerraformVersion());
            SystemCmdResult result =
                    executor.tfValidate(executorPath, request.getScriptsDirectory());
            TerraformValidationResult validationResult =
                    new ObjectMapper()
                            .readValue(
                                    result.getCommandStdOutput(), TerraformValidationResult.class);
            validationResult.setRequestId(request.getRequestId());
            validationResult.setTerraformVersionUsed(
                    versionHelper.getExactVersionOfExecutor(executorPath));
            return validationResult;
        } catch (JsonProcessingException | InvalidTerraformToolException ex) {
            throw new TerraformExecutorException("Failed get terraform validation result.", ex);
        }
    }

    /** Deploy a source by terraform. */
    public TerraformResult deployWithScriptsDirectory(
            TerraformRequestWithScriptsDirectory request) {
        SystemCmdResult result;
        String executorPath = null;
        try {
            executorPath =
                    installer.getExecutorPathThatMatchesRequiredVersion(
                            request.getTerraformVersion());
            if (Boolean.TRUE.equals(request.getIsPlanOnly())) {
                result =
                        executor.tfPlan(
                                executorPath,
                                request.getVariables(),
                                request.getEnvVariables(),
                                request.getScriptsDirectory());
            } else {
                result =
                        executor.tfApply(
                                executorPath,
                                request.getVariables(),
                                request.getEnvVariables(),
                                request.getScriptsDirectory());
            }
        } catch (InvalidTerraformToolException | TerraformExecutorException tfEx) {
            log.error("Terraform deploy service failed. error:{}", tfEx.getMessage());
            result = new SystemCmdResult();
            result.setCommandSuccessful(false);
            result.setCommandStdError(tfEx.getMessage());
        }
        TerraformResult terraformResult = transSystemCmdResultToTerraformResult(result, request);
        terraformResult.setTerraformVersionUsed(
                versionHelper.getExactVersionOfExecutor(executorPath));
        scriptsHelper.deleteTaskWorkspace(request.getScriptsDirectory());
        return terraformResult;
    }

    /** Modify a source by terraform. */
    public TerraformResult modifyWithScriptsDirectory(
            TerraformRequestWithScriptsDirectory request) {
        SystemCmdResult result;
        String executorPath = null;
        try {
            executorPath =
                    installer.getExecutorPathThatMatchesRequiredVersion(
                            request.getTerraformVersion());
            if (Boolean.TRUE.equals(request.getIsPlanOnly())) {
                result =
                        executor.tfPlan(
                                executorPath,
                                request.getVariables(),
                                request.getEnvVariables(),
                                request.getScriptsDirectory());
            } else {
                result =
                        executor.tfApply(
                                executorPath,
                                request.getVariables(),
                                request.getEnvVariables(),
                                request.getScriptsDirectory());
            }
        } catch (InvalidTerraformToolException | TerraformExecutorException tfEx) {
            log.error("Terraform deploy service failed. error:{}", tfEx.getMessage());
            result = new SystemCmdResult();
            result.setCommandSuccessful(false);
            result.setCommandStdError(tfEx.getMessage());
        }
        TerraformResult terraformResult = transSystemCmdResultToTerraformResult(result, request);
        terraformResult.setTerraformVersionUsed(
                versionHelper.getExactVersionOfExecutor(executorPath));
        scriptsHelper.deleteTaskWorkspace(request.getScriptsDirectory());
        terraformResult.setRequestId(request.getRequestId());
        return terraformResult;
    }

    /** Destroy resource of the service. */
    public TerraformResult destroyWithScriptsDirectory(
            TerraformRequestWithScriptsDirectory request) {
        SystemCmdResult result;
        String executorPath = null;
        try {
            executorPath =
                    installer.getExecutorPathThatMatchesRequiredVersion(
                            request.getTerraformVersion());
            result =
                    executor.tfDestroy(
                            executorPath,
                            request.getVariables(),
                            request.getEnvVariables(),
                            request.getScriptsDirectory());
        } catch (InvalidTerraformToolException | TerraformExecutorException tfEx) {
            log.error("Terraform destroy service failed. error:{}", tfEx.getMessage());
            result = new SystemCmdResult();
            result.setCommandSuccessful(false);
            result.setCommandStdError(tfEx.getMessage());
        }
        TerraformResult terraformResult = transSystemCmdResultToTerraformResult(result, request);
        terraformResult.setTerraformVersionUsed(
                versionHelper.getExactVersionOfExecutor(executorPath));
        scriptsHelper.deleteTaskWorkspace(request.getScriptsDirectory());
        terraformResult.setRequestId(request.getRequestId());
        return terraformResult;
    }

    /** Executes terraform plan command on a directory and returns the plan as a JSON string. */
    public TerraformPlan getTerraformPlanWithScriptsDirectory(
            TerraformRequestWithScriptsDirectory request) {
        String executorPath =
                installer.getExecutorPathThatMatchesRequiredVersion(request.getTerraformVersion());
        String result =
                executor.getTerraformPlanAsJson(
                        executorPath,
                        request.getVariables(),
                        request.getEnvVariables(),
                        request.getScriptsDirectory());
        scriptsHelper.deleteTaskWorkspace(request.getScriptsDirectory());
        TerraformPlan terraformPlan =
                TerraformPlan.builder().plan(result).requestId(request.getRequestId()).build();
        terraformPlan.setTerraformVersionUsed(
                versionHelper.getExactVersionOfExecutor(executorPath));
        return terraformPlan;
    }

    /** Async deploy a source by terraform. */
    @Async(TaskConfiguration.TASK_EXECUTOR_NAME)
    public void asyncDeployWithScriptsDirectory(
            TerraformAsyncRequestWithScriptsDirectory asyncDeployRequest) {
        TerraformResult result;
        try {
            result = deployWithScriptsDirectory(asyncDeployRequest);
        } catch (RuntimeException e) {
            result =
                    TerraformResult.builder()
                            .commandStdOutput(null)
                            .commandStdError(e.getMessage())
                            .isCommandSuccessful(false)
                            .terraformState(null)
                            .generatedFileContentMap(new HashMap<>())
                            .build();
        }
        result.setRequestId(asyncDeployRequest.getRequestId());
        String url = asyncDeployRequest.getWebhookConfig().getUrl();
        log.info("Deployment service complete, callback POST url:{}, requestBody:{}", url, result);
        sendTerraformResult(url, result);
    }

    /** Async modify a source by terraform. */
    @Async(TaskConfiguration.TASK_EXECUTOR_NAME)
    public void asyncModifyWithScriptsDirectory(
            TerraformAsyncRequestWithScriptsDirectory asyncModifyRequest) {
        TerraformResult result;
        try {
            result = modifyWithScriptsDirectory(asyncModifyRequest);
        } catch (RuntimeException e) {
            result =
                    TerraformResult.builder()
                            .commandStdOutput(null)
                            .commandStdError(e.getMessage())
                            .isCommandSuccessful(false)
                            .terraformState(null)
                            .generatedFileContentMap(new HashMap<>())
                            .build();
        }
        result.setRequestId(asyncModifyRequest.getRequestId());
        String url = asyncModifyRequest.getWebhookConfig().getUrl();
        log.info("Deployment service complete, callback POST url:{}, requestBody:{}", url, result);
        sendTerraformResult(url, result);
    }

    /** Async destroy resource of the service. */
    @Async(TaskConfiguration.TASK_EXECUTOR_NAME)
    public void asyncDestroyWithScriptsDirectory(
            TerraformAsyncRequestWithScriptsDirectory request) {
        TerraformResult result;
        try {
            result = destroyWithScriptsDirectory(request);
        } catch (RuntimeException e) {
            result =
                    TerraformResult.builder()
                            .commandStdOutput(null)
                            .commandStdError(e.getMessage())
                            .isCommandSuccessful(false)
                            .terraformState(null)
                            .generatedFileContentMap(new HashMap<>())
                            .build();
        }
        result.setRequestId(request.getRequestId());
        String url = request.getWebhookConfig().getUrl();
        log.info("Destroy service complete, callback POST url:{}, requestBody:{}", url, result);
        sendTerraformResult(url, result);
    }

    private void sendTerraformResult(String url, TerraformResult result) {
        try {
            restTemplate.postForLocation(url, result);
        } catch (RestClientException e) {
            log.error("error while sending terraform result", e);
            terraformResultPersistenceManage.persistTerraformResult(result);
        }
    }

    private TerraformResult transSystemCmdResultToTerraformResult(
            SystemCmdResult result, TerraformRequestWithScriptsDirectory request) {
        TerraformResult terraformResult =
                TerraformResult.builder()
                        .isCommandSuccessful(result.isCommandSuccessful())
                        .requestId(request.getRequestId())
                        .build();
        try {
            BeanUtils.copyProperties(result, terraformResult);
            terraformResult.setTerraformState(
                    scriptsHelper.getTerraformState(request.getScriptsDirectory()));
            terraformResult.setGeneratedFileContentMap(
                    scriptsHelper.getDeploymentGeneratedFilesContent(
                            request.getScriptsDirectory(), request.getScriptFiles()));
        } catch (Exception e) {
            log.error("Failed to get terraform state and generated files content.", e);
        }
        return terraformResult;
    }
}
