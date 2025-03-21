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
import org.eclipse.xpanse.terra.boot.models.request.directory.TerraformAsyncRequestWithScriptsDirectory;
import org.eclipse.xpanse.terra.boot.models.request.directory.TerraformRequestWithScriptsDirectory;
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

/** REST controller for running terraform modules directly on the provided directory. */
@Slf4j
@Profile("!amqp")
@CrossOrigin
@RestController
@RequestMapping("/terra-boot/directory")
public class TerraBootFromDirectoryApi {

    @Resource private TerraformRequestService requestService;

    /**
     * Method to validate Terraform modules.
     *
     * @return Returns the validation status of the Terraform module in a workspace.
     */
    @Tag(
            name = "TerraformFromDirectory",
            description = "APIs for running Terraform commands inside a provided directory.")
    @Operation(description = "Validate the Terraform modules in the given directory.")
    @PostMapping(value = "/validate", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public TerraformValidationResult validateFromDirectory(
            @Valid @RequestBody TerraformRequestWithScriptsDirectory request) {
        return requestService.handleTerraformValidateRequest(request);
    }

    /**
     * Method to get Terraform plan as a JSON string from a directory.
     *
     * @return Returns the terraform plan as a JSON string.
     */
    @Tag(
            name = "TerraformFromDirectory",
            description = "APIs for running Terraform commands inside a provided directory.")
    @Operation(description = "Get Terraform Plan as JSON string from the given directory.")
    @PostMapping(value = "/plan", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public TerraformPlan plan(@Valid @RequestBody TerraformRequestWithScriptsDirectory request) {
        return requestService.handleTerraformPlanRequest(request);
    }

    /**
     * Method to deploy resources requested in a workspace.
     *
     * @return Returns the status of the deployment.
     */
    @Tag(
            name = "TerraformFromDirectory",
            description = "APIs for running Terraform commands inside a provided directory.")
    @Operation(description = "Deploy resources via Terraform from the given directory.")
    @PostMapping(value = "/deploy", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public TerraformResult deployFromDirectory(
            @Valid @RequestBody TerraformRequestWithScriptsDirectory request) {
        return requestService.handleTerraformDeploymentRequest(request);
    }

    /**
     * Method to modify resources requested in a workspace.
     *
     * @return Returns the status of the deployment.
     */
    @Tag(
            name = "TerraformFromDirectory",
            description = "APIs for running Terraform commands inside a provided directory.")
    @Operation(description = "Modify resources via Terraform from the given directory.")
    @PostMapping(value = "/modify", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public TerraformResult modifyFromDirectory(
            @Valid @RequestBody TerraformRequestWithScriptsDirectory request) {
        return requestService.handleTerraformDeploymentRequest(request);
    }

    /**
     * Method to destroy resources requested in a workspace.
     *
     * @return Returns the status of the resources destroy.
     */
    @Tag(
            name = "TerraformFromDirectory",
            description = "APIs for running Terraform commands inside a provided directory.")
    @Operation(description = "Destroy the resources from the given directory.")
    @DeleteMapping(value = "/destroy", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public TerraformResult destroyFromDirectory(
            @Valid @RequestBody TerraformRequestWithScriptsDirectory request) {
        return requestService.handleTerraformDeploymentRequest(request);
    }

    /** Method to async deploy resources from the given directory. */
    @Tag(
            name = "TerraformFromDirectory",
            description = "APIs for running Terraform commands inside a provided directory.")
    @Operation(description = "async deploy resources via Terraform from the given directory.")
    @PostMapping(value = "/deploy/async", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void asyncDeployFromDirectory(
            @Valid @RequestBody TerraformAsyncRequestWithScriptsDirectory request) {
        requestService.processAsyncDeploymentRequest(request);
    }

    /** Method to async modify resources from the given directory. */
    @Tag(
            name = "TerraformFromDirectory",
            description = "APIs for running Terraform commands inside a provided directory.")
    @Operation(description = "async modify resources via Terraform from the given directory.")
    @PostMapping(value = "/modify/async", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void asyncModifyFromDirectory(
            @Valid @RequestBody TerraformAsyncRequestWithScriptsDirectory request) {
        requestService.processAsyncDeploymentRequest(request);
    }

    /** Method to async destroy resources from the given directory. */
    @Tag(
            name = "TerraformFromDirectory",
            description = "APIs for running Terraform commands inside a provided directory.")
    @Operation(description = "async destroy resources via Terraform from the given directory.")
    @DeleteMapping(value = "/destroy/async", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void asyncDestroyFromDirectory(
            @Valid @RequestBody TerraformAsyncRequestWithScriptsDirectory request) {
        requestService.processAsyncDeploymentRequest(request);
    }
}
