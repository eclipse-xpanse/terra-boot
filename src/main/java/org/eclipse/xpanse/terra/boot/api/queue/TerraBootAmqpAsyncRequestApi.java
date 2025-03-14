/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.api.queue;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.terra.boot.models.request.directory.TerraformRequestWithScriptsDirectory;
import org.eclipse.xpanse.terra.boot.models.request.git.TerraformRequestWithScriptsGitRepo;
import org.eclipse.xpanse.terra.boot.models.request.scripts.TerraformRequestWithScripts;
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
@Profile("amqp")
@CrossOrigin
@RestController
@RequestMapping("/terra-boot/async/request/")
public class TerraBootAmqpAsyncRequestApi {

    @Resource private AmqpProducer amqpProducer;
    @Resource private TerraformRequestService terraformRequestService;

    /** Method to receive terraform async request with scripts in directory. */
    @Tag(
            name = "TerraformAsyncRequest",
            description = "APIs for receiving terraform async requests using queue.")
    @Operation(description = "Receive async terraform request with scripts in directory.")
    @PostMapping(value = "/directory", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void asyncRequestWithScriptsDirectory(
            @Valid @RequestBody TerraformRequestWithScriptsDirectory request) {
        terraformRequestService.validateTerraformRequest(request);
        amqpProducer.sendTerraformRequestWithDirectory(request);
    }

    /** Method to receive terraform async request with scripts in git repo. */
    @Tag(
            name = "TerraformAsyncRequest",
            description = "APIs for receiving terraform async requests using queue.")
    @Operation(description = "Receive async terraform request with scripts in git repo.")
    @PostMapping(value = "/git", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void asyncRequestWithScriptsGitRepo(
            @Valid @RequestBody TerraformRequestWithScriptsGitRepo request) {
        terraformRequestService.validateTerraformRequest(request);
        amqpProducer.sendTerraformRequestWithScriptsGitRepo(request);
    }

    /** Method to async terraform async request with scripts map. */
    @Tag(
            name = "TerraformAsyncRequest",
            description = "APIs for receiving terraform async requests using queue.")
    @Operation(description = "Receive async terraform request with scripts map.")
    @DeleteMapping(value = "/scripts", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void asyncRequestWithScripts(@Valid @RequestBody TerraformRequestWithScripts request) {
        terraformRequestService.validateTerraformRequest(request);
        amqpProducer.sendTerraformRequestWithScripts(request);
    }
}
