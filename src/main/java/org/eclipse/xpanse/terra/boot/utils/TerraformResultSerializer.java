/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.utils;

import org.eclipse.serializer.Serializer;
import org.eclipse.serializer.SerializerFoundation;
import org.eclipse.xpanse.terra.boot.models.response.TerraformResult;
import org.springframework.stereotype.Component;

/** Class to manage TerraformResult serialization and deserialization using eclipse-serializer. */
@Component
public class TerraformResultSerializer {

    private final Serializer<byte[]> serializer;

    /** Constructor to initialize the Serializer with TerraformResult class registered. */
    public TerraformResultSerializer() {
        final SerializerFoundation<?> foundation =
                SerializerFoundation.New().registerEntityTypes(TerraformResult.class);
        this.serializer = Serializer.Bytes(foundation);
    }

    /**
     * Serialize TerraformResult object.
     *
     * @param result TerraformResult.
     * @return byte[].
     */
    public byte[] serialize(TerraformResult result) {
        return serializer.serialize(result);
    }

    /**
     * Deserialize TerraformResult object.
     *
     * @param data byte[].
     * @return TerraformResult.
     */
    public TerraformResult deserialize(byte[] data) {
        return serializer.deserialize(data);
    }
}
