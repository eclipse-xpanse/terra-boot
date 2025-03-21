/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.terra.boot.models.request.git.TerraformAsyncRequestWithScriptsGitRepo;
import org.eclipse.xpanse.terra.boot.models.request.git.TerraformRequestWithScriptsGitRepo;
import org.eclipse.xpanse.terra.boot.models.response.TerraformPlan;
import org.eclipse.xpanse.terra.boot.models.response.TerraformResult;
import org.eclipse.xpanse.terra.boot.models.response.validation.TerraformValidationResult;
import org.eclipse.xpanse.terra.boot.terraform.service.TerraformRequestService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for running terraform modules from a GIT repo. */
@Slf4j
@CrossOrigin
@Profile("!amqp")
@RestController
@RequestMapping("/terra-boot/git")
public class TerraBootFromGitRepoApi {

    @Resource private TerraformRequestService requestService;

    /**
     * Method to validate resources by scripts.
     *
     * @return Returns the status of the deployment.
     */
    @Tag(
            name = "TerraformFromGitRepo",
            description =
                    "APIs for running Terraform commands using Terraform scripts from a GIT Repo.")
    @Operation(description = "Deploy resources via Terraform")
    @PostMapping(value = "/validate", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public TerraformValidationResult validateScriptsFromGitRepo(
            @Valid @RequestBody TerraformRequestWithScriptsGitRepo request) {
        return requestService.handleTerraformValidateRequest(request);
    }

    /**
     * Method to get Terraform plan as a JSON string from the GIT repo provided.
     *
     * @return Returns the terraform plan as a JSON string.
     */
    @Tag(
            name = "TerraformFromGitRepo",
            description =
                    "APIs for running Terraform commands using Terraform scripts from a GIT Repo.")
    @Operation(
            description =
                    "Get Terraform Plan as JSON string from the list of script files provided")
    @PostMapping(value = "/plan", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public TerraformPlan planFromGitRepo(
            @Valid @RequestBody TerraformRequestWithScriptsGitRepo request) {
        return requestService.handleTerraformPlanRequest(request);
    }

    /**
     * Method to deploy resources using scripts from the GIT Repo provided.
     *
     * @return Returns the status of the deployment.
     */
    @Tag(
            name = "TerraformFromGitRepo",
            description =
                    "APIs for running Terraform commands using Terraform scripts from a GIT Repo.")
    @Operation(description = "Deploy resources via Terraform")
    @PostMapping(value = "/deploy", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public TerraformResult deployFromGitRepo(
            @Valid @RequestBody TerraformRequestWithScriptsGitRepo request) {
        return requestService.handleTerraformDeploymentRequest(request);
    }

    /**
     * Method to modify resources using scripts from the GIT Repo provided.
     *
     * @return Returns the status of the deployment.
     */
    @Tag(
            name = "TerraformFromGitRepo",
            description =
                    "APIs for running Terraform commands using Terraform scripts from a GIT Repo.")
    @Operation(description = "Modify resources via Terraform")
    @PostMapping(value = "/modify", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public TerraformResult modifyFromGitRepo(
            @Valid @RequestBody TerraformRequestWithScriptsGitRepo request) {
        return requestService.handleTerraformDeploymentRequest(request);
    }

    /**
     * MMethod to deploy resources using scripts from the GIT Repo provided.
     *
     * @return Returns the status of to Destroy.
     */
    @Tag(
            name = "TerraformFromGitRepo",
            description =
                    "APIs for running Terraform commands using Terraform scripts from a GIT Repo.")
    @Operation(description = "Destroy resources via Terraform")
    @PostMapping(value = "/destroy", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public TerraformResult destroyFromGitRepo(
            @Valid @RequestBody TerraformRequestWithScriptsGitRepo request) {
        return requestService.handleTerraformDeploymentRequest(request);
    }

    /** Method to async deploy resources from the provided GIT Repo. */
    @Tag(
            name = "TerraformFromGitRepo",
            description =
                    "APIs for running Terraform commands using Terraform scripts from a GIT Repo.")
    @Operation(description = "async deploy resources via Terraform")
    @PostMapping(value = "/deploy/async", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void asyncDeployFromGitRepo(
            @Valid @RequestBody TerraformAsyncRequestWithScriptsGitRepo request) {
        requestService.processAsyncDeploymentRequest(request);
    }

    /** Method to async modify resources from the provided GIT Repo. */
    @Tag(
            name = "TerraformFromGitRepo",
            description =
                    "APIs for running Terraform commands using Terraform scripts from a GIT Repo.")
    @Operation(description = "async deploy resources via Terraform")
    @PostMapping(value = "/modify/async", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void asyncModifyFromGitRepo(
            @Valid @RequestBody TerraformAsyncRequestWithScriptsGitRepo request) {
        requestService.processAsyncDeploymentRequest(request);
    }

    /** Method to async destroy resources by scripts. */
    @Tag(
            name = "TerraformFromGitRepo",
            description =
                    "APIs for running Terraform commands using Terraform scripts from a GIT Repo.")
    @Operation(description = "Async destroy the Terraform modules")
    @DeleteMapping(value = "/destroy/async", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void asyncDestroyFromGitRepo(
            @Valid @RequestBody TerraformAsyncRequestWithScriptsGitRepo request) {
        requestService.processAsyncDeploymentRequest(request);
    }
}
