/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.terra.boot.models.response.ReFetchResult;
import org.eclipse.xpanse.terra.boot.terraform.service.TerraformResultPersistenceManage;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for manage the task form terra-boot. */
@Slf4j
@Profile("!amqp")
@CrossOrigin
@RestController
@RequestMapping("/terra-boot/task")
public class TerraBootTaskResultApi {

    @Resource private TerraformResultPersistenceManage terraformResultPersistenceManage;

    /**
     * Fetch the stored terraform result.
     *
     * @param requestId id of the request
     * @return terraform result
     */
    @Tag(
            name = "RetrieveTerraformResult",
            description = "APIs to manually fetching task results from terra-boot.")
    @Operation(
            description =
                    "Method to retrieve stored terraform result in case terra-boot "
                            + "receives a failure while sending the terraform result via callback.")
    @GetMapping(value = "/result/{requestId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ReFetchResult getStoredTaskResultByRequestId(
            @Parameter(name = "requestId", description = "id of the request")
                    @PathVariable("requestId")
                    UUID requestId) {
        return terraformResultPersistenceManage.retrieveTerraformResultByRequestId(requestId);
    }

    /**
     * Batch retrieve stored terraform results.
     *
     * @param requestIds list of requestIds
     * @return list of reFetchResults
     */
    @Tag(
            name = "RetrieveTerraformResult",
            description = "APIs to manually fetching task results from terra-boot.")
    @Operation(description = "Method to batch retrieve stored terraform result from terra-boot.")
    @PostMapping(value = "/results/batch", consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<ReFetchResult> getBatchTaskResults(
            @Parameter(description = "List of request IDs", required = true) @RequestBody
                    List<UUID> requestIds) {
        if (CollectionUtils.isEmpty(requestIds)) {
            throw new IllegalArgumentException("requestIds cannot be empty.");
        }
        List<ReFetchResult> reFetchResults = new ArrayList<>();
        requestIds.forEach(
                requestId -> {
                    ReFetchResult reFetchResult =
                            terraformResultPersistenceManage.retrieveTerraformResultByRequestId(
                                    requestId);
                    reFetchResults.add(reFetchResult);
                });
        return reFetchResults;
    }
}
