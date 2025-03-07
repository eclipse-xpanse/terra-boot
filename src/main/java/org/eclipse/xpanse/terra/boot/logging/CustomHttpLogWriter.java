/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Precorrelation;

/** Defines the log level format for the HTTP logs generated by Logbook. */
@Slf4j
public class CustomHttpLogWriter implements HttpLogWriter {

    @Override
    public boolean isActive() {
        return HttpLoggingConfig.isHttpLoggingEnabled();
    }

    @Override
    public void write(final @NonNull Precorrelation precorrelation, @NonNull final String request) {
        log.info(request);
    }

    @Override
    public void write(final @NonNull Correlation correlation, final @NonNull String response) {
        log.info(response);
    }
}
