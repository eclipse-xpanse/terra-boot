/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.models.request;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.terra.boot.models.request.directory.TerraformAsyncRequestWithScriptsDirectory;
import org.eclipse.xpanse.terra.boot.models.request.directory.TerraformRequestWithScriptsDirectory;
import org.eclipse.xpanse.terra.boot.models.request.git.TerraformAsyncRequestWithScriptsGitRepo;
import org.eclipse.xpanse.terra.boot.models.request.git.TerraformRequestWithScriptsGitRepo;
import org.eclipse.xpanse.terra.boot.models.request.scripts.TerraformAsyncRequestWithScripts;

/** TerraformRequestDeserializer is used to deserialize TerraformRequest. */
@Slf4j
public class TerraformRequestDeserializer extends StdDeserializer<TerraformRequest> {

    /** Constructs a new TerraformRequestDeserializer. */
    public TerraformRequestDeserializer() {
        super(TerraformRequest.class);
    }

    @Override
    public TerraformRequest deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        if (node.has("scriptFiles")) {
            if (node.has("webhookConfig")) {
                return p.getCodec().treeToValue(node, TerraformAsyncRequestWithScripts.class);
            }
            return p.getCodec().treeToValue(node, TerraformAsyncRequestWithScripts.class);
        } else if (node.has("gitRepoDetails")) {
            if (node.has("webhookConfig")) {
                return p.getCodec()
                        .treeToValue(node, TerraformAsyncRequestWithScriptsGitRepo.class);
            }
            return p.getCodec().treeToValue(node, TerraformRequestWithScriptsGitRepo.class);
        } else if (node.has("scriptsDirectory")) {
            if (node.has("webhookConfig")) {
                return p.getCodec()
                        .treeToValue(node, TerraformAsyncRequestWithScriptsDirectory.class);
            }
            return p.getCodec().treeToValue(node, TerraformRequestWithScriptsDirectory.class);
        }
        return p.getCodec().treeToValue(node, TerraformRequest.class);
    }
}
