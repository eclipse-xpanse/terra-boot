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
import org.eclipse.xpanse.terra.boot.models.request.scripts.TerraformAsyncRequestWithScripts;
import org.eclipse.xpanse.terra.boot.models.request.scripts.TerraformRequestWithScripts;
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

/** API methods implemented by terra-boot. */
@Slf4j
@CrossOrigin
@Profile("!amqp")
@RestController
@RequestMapping("/terra-boot/scripts/")
public class TerraBootFromScriptsApi {

    @Resource private TerraformRequestService requestService;

    /**
     * Method to validate resources by scripts.
     *
     * @return Returns the status of the deployment.
     */
    @Tag(
            name = "TerraformFromScripts",
            description =
                    "APIs for running Terraform commands on the scripts sent via request body.")
    @Operation(description = "Deploy resources via Terraform")
    @PostMapping(value = "/validate", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public TerraformValidationResult validateWithScripts(
            @Valid @RequestBody TerraformRequestWithScripts request) {
        return requestService.handleTerraformValidateRequest(request);
    }

    /**
     * Method to deploy resources by scripts.
     *
     * @return Returns the status of the deployment.
     */
    @Tag(
            name = "TerraformFromScripts",
            description =
                    "APIs for running Terraform commands on the scripts sent via request body.")
    @Operation(description = "Deploy resources via Terraform")
    @PostMapping(value = "/deploy", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public TerraformResult deployWithScripts(
            @Valid @RequestBody TerraformRequestWithScripts request) {
        return requestService.handleTerraformDeploymentRequest(request);
    }

    /**
     * Method to modify resources by scripts.
     *
     * @return Returns the status of the deployment.
     */
    @Tag(
            name = "TerraformFromScripts",
            description =
                    "APIs for running Terraform commands on the scripts sent via request body.")
    @Operation(description = "Modify resources via Terraform")
    @PostMapping(value = "/modify", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public TerraformResult modifyWithScripts(
            @Valid @RequestBody TerraformRequestWithScripts request) {
        return requestService.handleTerraformDeploymentRequest(request);
    }

    /**
     * Method to destroy resources by scripts.
     *
     * @return Returns the status of to Destroy.
     */
    @Tag(
            name = "TerraformFromScripts",
            description =
                    "APIs for running Terraform commands on the scripts sent via request body.")
    @Operation(description = "Destroy resources via Terraform")
    @PostMapping(value = "/destroy", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public TerraformResult destroyWithScripts(
            @Valid @RequestBody TerraformRequestWithScripts request) {
        return requestService.handleTerraformDeploymentRequest(request);
    }

    /** Method to async deploy resources by scripts. */
    @Tag(
            name = "TerraformFromScripts",
            description =
                    "APIs for running Terraform commands on the scripts sent via request body.")
    @Operation(description = "async deploy resources via Terraform")
    @PostMapping(value = "/deploy/async", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void asyncDeployWithScripts(
            @Valid @RequestBody TerraformAsyncRequestWithScripts request) {
        requestService.processAsyncDeploymentRequest(request);
    }

    /** Method to async modify resources by scripts. */
    @Tag(
            name = "TerraformFromScripts",
            description =
                    "APIs for running Terraform commands on the scripts sent via request body.")
    @Operation(description = "async modify resources via Terraform")
    @PostMapping(value = "/modify/async", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void asyncModifyWithScripts(
            @Valid @RequestBody TerraformAsyncRequestWithScripts request) {
        requestService.processAsyncDeploymentRequest(request);
    }

    /** Method to async destroy resources by scripts. */
    @Tag(
            name = "TerraformFromScripts",
            description =
                    "APIs for running Terraform commands on the scripts sent via request body.")
    @Operation(description = "Async destroy the Terraform modules")
    @DeleteMapping(value = "/destroy/async", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void asyncDestroyWithScripts(
            @Valid @RequestBody TerraformAsyncRequestWithScripts request) {
        requestService.processAsyncDeploymentRequest(request);
    }

    /**
     * Method to get Terraform plan as a JSON string from the list of script files provided.
     *
     * @return Returns the terraform plan as a JSON string.
     */
    @Tag(
            name = "TerraformFromScripts",
            description =
                    "APIs for running Terraform commands on the scripts sent via request body.")
    @Operation(
            description =
                    "Get Terraform Plan as JSON string from the list of script files provided")
    @PostMapping(value = "/plan", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public TerraformPlan planWithScripts(@Valid @RequestBody TerraformRequestWithScripts request) {
        return requestService.handleTerraformPlanRequest(request);
    }
}
