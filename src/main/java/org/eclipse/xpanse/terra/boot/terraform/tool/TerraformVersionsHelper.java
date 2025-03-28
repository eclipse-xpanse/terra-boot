/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.terraform.tool;

import jakarta.annotation.Resource;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.terra.boot.models.exceptions.InvalidTerraformToolException;
import org.eclipse.xpanse.terra.boot.terraform.utils.SystemCmd;
import org.eclipse.xpanse.terra.boot.terraform.utils.SystemCmdResult;
import org.semver4j.Semver;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** Defines methods for handling terraform with required version. */
@Slf4j
@Component
public class TerraformVersionsHelper {

    /** Terraform version required version regex. */
    public static final String TERRAFORM_REQUIRED_VERSION_REGEX =
            "^(=|>=|<=)\\s*[vV]?\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$";

    private static final Pattern TERRAFORM_REQUIRED_VERSION_PATTERN =
            Pattern.compile(TERRAFORM_REQUIRED_VERSION_REGEX);
    private static final Pattern TERRAFORM_VERSION_OUTPUT_PATTERN =
            Pattern.compile("^Terraform\\s+v(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\b");
    private static final String TERRAFORM_BINARY_DOWNLOAD_URL_FORMAT =
            "%s/%s/terraform_%s_%s_%s.zip";
    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    private static final String OS_ARCH = System.getProperty("os.arch").toLowerCase();
    private static final String TERRAFORM_EXECUTOR_PREFIX = "terraform-";

    @Resource private SystemCmd systemCmd;

    /**
     * Get terraform executor path which matches the required version.
     *
     * @param installationDir terraform installation directory
     * @param requiredOperator operator in required version
     * @param requiredNumber number in required version
     * @return return the version of terraform which is matched required, otherwise return null.
     */
    public String getExecutorPathMatchedRequiredVersion(
            String installationDir, String requiredOperator, String requiredNumber) {
        // Get path of terraform executor matched required version in the installation dir.
        File installDir = new File(installationDir);
        if (!installDir.exists() || !installDir.isDirectory()) {
            return null;
        }
        File[] terraformExecutors = installDir.listFiles();
        if (Objects.isNull(terraformExecutors) || terraformExecutors.length == 0) {
            return null;
        }
        Map<String, File> executorVersionFileMap = new HashMap<>();
        Arrays.stream(terraformExecutors)
                .filter(
                        f ->
                                f.isFile()
                                        && f.canExecute()
                                        && f.getName().startsWith(TERRAFORM_EXECUTOR_PREFIX))
                .forEach(
                        f -> {
                            String versionNumber = getVersionFromExecutorPath(f.getAbsolutePath());
                            executorVersionFileMap.put(versionNumber, f);
                        });
        if (CollectionUtils.isEmpty(executorVersionFileMap)) {
            return null;
        }
        String findBestVersion =
                findBestVersionFromAllAvailableVersions(
                        executorVersionFileMap.keySet(), requiredOperator, requiredNumber);
        if (StringUtils.isNotBlank(findBestVersion)) {
            File executorFile = executorVersionFileMap.get(findBestVersion);
            if (checkIfExecutorIsMatchedRequiredVersion(
                    executorFile, requiredOperator, requiredNumber)) {
                return executorFile.getAbsolutePath();
            }
        }
        return null;
    }

    /**
     * Get the operator and number from the required version.
     *
     * @param requiredVersion required version
     * @return string array, the first element is operator, the second element is number.
     */
    public String[] getOperatorAndNumberFromRequiredVersion(String requiredVersion) {

        String version = requiredVersion.replaceAll("\\s+", "").toLowerCase().replaceAll("v", "");
        if (StringUtils.isNotBlank(version)) {
            Matcher matcher = TERRAFORM_REQUIRED_VERSION_PATTERN.matcher(version);
            if (matcher.find()) {
                String[] operatorAndNumber = new String[2];
                operatorAndNumber[0] = matcher.group(1);
                operatorAndNumber[1] = matcher.group(0).replaceAll("^(=|>=|<=)", "");
                return operatorAndNumber;
            }
        }
        String errorMsg =
                String.format("Invalid terraform required version format:%s", requiredVersion);
        throw new InvalidTerraformToolException(errorMsg);
    }

    /**
     * Find the best version from all available versions.
     *
     * @param allAvailableVersions all available versions
     * @param requiredOperator operator in required version
     * @param requiredNumber number in required version
     * @return the best version
     */
    public String findBestVersionFromAllAvailableVersions(
            Set<String> allAvailableVersions, String requiredOperator, String requiredNumber) {
        if (CollectionUtils.isEmpty(allAvailableVersions)
                || StringUtils.isBlank(requiredOperator)
                || StringUtils.isBlank(requiredNumber)) {
            return null;
        }
        Semver requiredSemver = new Semver(requiredNumber);
        return switch (requiredOperator) {
            case "=" ->
                    allAvailableVersions.stream()
                            .filter(v -> new Semver(v).isEqualTo(requiredSemver))
                            .findAny()
                            .orElse(null);
            case ">=" ->
                    allAvailableVersions.stream()
                            .filter(v -> new Semver(v).isGreaterThanOrEqualTo(requiredSemver))
                            .min(Comparator.naturalOrder())
                            .orElse(null);
            case "<=" ->
                    allAvailableVersions.stream()
                            .filter(v -> new Semver(v).isLowerThanOrEqualTo(requiredSemver))
                            .max(Comparator.naturalOrder())
                            .orElse(null);
            default -> null;
        };
    }

    /**
     * Check the version of installed executor is matched required version.
     *
     * @param executorFile executor file
     * @param requiredOperator operator in required version
     * @param requiredNumber number in required version
     * @return true if the version is valid, otherwise return false.
     */
    public boolean checkIfExecutorIsMatchedRequiredVersion(
            File executorFile, String requiredOperator, String requiredNumber) {
        String versionNumber = getExactVersionOfExecutor(executorFile.getAbsolutePath());
        if (StringUtils.isNotBlank(versionNumber)) {
            return isVersionSatisfied(versionNumber, requiredOperator, requiredNumber);
        }
        return false;
    }

    /**
     * Check if the executor can be executed.
     *
     * @param executorFile executor file
     * @return If true, the executor can be executed, otherwise return false.
     */
    public boolean checkIfExecutorCanBeExecuted(File executorFile) {
        String versionOutput = getVersionCommandOutput(executorFile.getAbsolutePath());
        return StringUtils.isNotBlank(versionOutput);
    }

    /**
     * Get exact version of executor.
     *
     * @param executorPath executor path
     * @return exact version of executor.
     */
    public String getExactVersionOfExecutor(String executorPath) {
        String versionOutput = getVersionCommandOutput(executorPath);
        if (StringUtils.isNotBlank(versionOutput)) {
            Matcher matcher = TERRAFORM_VERSION_OUTPUT_PATTERN.matcher(versionOutput);
            if (matcher.find()) {
                // return only the version number.
                return matcher.group(1);
            }
        }
        return null;
    }

    private String getVersionCommandOutput(String executorPath) {
        try {
            File executorFile = new File(executorPath);
            if (executorFile.exists() && !executorFile.canExecute()) {
                SystemCmdResult chmodResult =
                        systemCmd.execute(
                                String.format("chmod +x %s", executorFile.getAbsolutePath()),
                                5,
                                System.getProperty("java.io.tmpdir"),
                                false,
                                new HashMap<>());
                if (!chmodResult.isCommandSuccessful()) {
                    log.error(chmodResult.getCommandStdError());
                }
            }
            SystemCmdResult versionCheckResult =
                    systemCmd.execute(
                            executorPath + " version",
                            5,
                            System.getProperty("java.io.tmpdir"),
                            false,
                            new HashMap<>());
            if (versionCheckResult.isCommandSuccessful()) {
                log.info(
                        "Get version of executor {} output: {}",
                        executorPath,
                        versionCheckResult.getCommandStdOutput());
                return versionCheckResult.getCommandStdOutput();
            } else {
                log.error(
                        "Get version of executor {} output error :{}",
                        executorPath,
                        versionCheckResult.getCommandStdError());
            }
        } catch (Exception e) {
            log.error("Failed to get version of executor {}.", executorPath, e);
        }
        return null;
    }

    /**
     * Install terraform with specific version.
     *
     * @param versionNumber the version number
     * @param downloadBaseUrl download base url
     * @param installDir installation directory
     * @return the path of the installed executor.
     */
    public File installTerraformWithVersion(
            String versionNumber, String downloadBaseUrl, String installDir) {
        // Install the executor with specific version into the path.
        String terraformExecutorName = getTerraformExecutorName(versionNumber);
        File terraformExecutorFile = new File(installDir, terraformExecutorName);
        File parentDir = terraformExecutorFile.getParentFile();
        try {
            if (!parentDir.exists()) {
                log.info(
                        "Created the installation dir {} {}.",
                        parentDir.getAbsolutePath(),
                        parentDir.mkdirs() ? "successfully" : "failed");
            }
            // download the binary zip file into the installation directory
            File terraformZipFile =
                    downloadTerraformBinaryZipFile(versionNumber, downloadBaseUrl, installDir);
            // unzip the zip file and move the executable binary to the installation directory
            unzipBinaryZipToGetExecutor(terraformZipFile, terraformExecutorFile);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new InvalidTerraformToolException(e.getMessage());
        }
        // delete the non-executable files
        deleteNonExecutorFiles(parentDir);
        return terraformExecutorFile;
    }

    private File downloadTerraformBinaryZipFile(
            String versionNumber, String downloadBaseUrl, String installDir) throws IOException {
        String binaryDownloadUrl = getTerraformBinaryDownloadUrl(downloadBaseUrl, versionNumber);
        String binaryZipFileName = getTerraformBinaryZipFileName(binaryDownloadUrl);
        File binaryZipFile = new File(installDir, binaryZipFileName);
        URL url = URI.create(binaryDownloadUrl).toURL();
        try (ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                FileOutputStream fos = new FileOutputStream(binaryZipFile, false)) {
            log.info(
                    "Downloading terraform binary file from {} to {}",
                    url,
                    binaryZipFile.getAbsolutePath());
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            log.info(
                    "Downloaded terraform binary file from {} to {} successfully.",
                    url,
                    binaryZipFile.getAbsolutePath());
        }
        return binaryZipFile;
    }

    private void unzipBinaryZipToGetExecutor(File binaryZipFile, File executorFile)
            throws IOException {
        if (!binaryZipFile.exists()) {
            String errorMsg =
                    String.format(
                            "Terraform binary zip file %s not found.",
                            binaryZipFile.getAbsolutePath());
            log.error(errorMsg);
            throw new IOException(errorMsg);
        }
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(binaryZipFile))) {
            log.info("Unzipping terraform binary zip file {}", binaryZipFile.getAbsolutePath());
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    String entryName = entry.getName();
                    File entryDestinationFile = new File(executorFile.getParentFile(), entryName);
                    if (isExecutorFileInZipForTerraform(entryName)) {
                        extractFile(zis, entryDestinationFile);
                        Files.move(
                                entryDestinationFile.toPath(),
                                executorFile.toPath(),
                                StandardCopyOption.REPLACE_EXISTING);
                        log.info(
                                "Unzipped terraform file {} and extract the executor {} "
                                        + "successfully.",
                                binaryZipFile.getAbsolutePath(),
                                executorFile.getAbsolutePath());
                    }
                }
            }
        }
    }

    private boolean isExecutorFileInZipForTerraform(String entryName) {
        return entryName.startsWith("terraform");
    }

    private void extractFile(ZipInputStream zis, File destinationFile) throws IOException {
        try (BufferedOutputStream bos =
                new BufferedOutputStream(new FileOutputStream(destinationFile))) {
            byte[] bytesIn = new byte[4096];
            int read;
            while ((read = zis.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }

    private void deleteNonExecutorFiles(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteNonExecutorFiles(file);
                } else {
                    if (!file.getName().startsWith(TERRAFORM_EXECUTOR_PREFIX) && !file.delete()) {
                        log.warn("Failed to delete file {}.", file.getAbsolutePath());
                    }
                }
            }
        }
    }

    private String getVersionFromExecutorPath(String executorPath) {
        if (executorPath.contains("-")) {
            return Arrays.asList(executorPath.split("-")).getLast();
        }
        return null;
    }

    private boolean isVersionSatisfied(
            String actualNumber, String requiredOperator, String requiredNumber) {
        Semver actualSemver = new Semver(actualNumber);
        Semver requiredSemver = new Semver(requiredNumber);
        if ("=".equals(requiredOperator)) {
            return actualSemver.isEqualTo(requiredSemver);
        } else if (">=".equals(requiredOperator)) {
            return actualSemver.isGreaterThanOrEqualTo(requiredSemver);
        } else if ("<=".equals(requiredOperator)) {
            return actualSemver.isLowerThanOrEqualTo(requiredSemver);
        }
        return false;
    }

    /**
     * Get terraform executor name with version.
     *
     * @param versionNumber version number
     * @return binary file name
     */
    public String getTerraformExecutorName(String versionNumber) {
        return TERRAFORM_EXECUTOR_PREFIX + versionNumber;
    }

    /**
     * Get whole download url of the executor binary file.
     *
     * @param downloadBaseUrl download base url
     * @param versionNumber version number
     * @return whole download url of the executor binary file
     */
    private String getTerraformBinaryDownloadUrl(String downloadBaseUrl, String versionNumber) {
        return String.format(
                TERRAFORM_BINARY_DOWNLOAD_URL_FORMAT,
                downloadBaseUrl,
                versionNumber,
                versionNumber,
                getOperatingSystemCode(),
                OS_ARCH);
    }

    private String getTerraformBinaryZipFileName(String executorBinaryDownloadUrl) {
        return executorBinaryDownloadUrl.substring(executorBinaryDownloadUrl.lastIndexOf("/") + 1);
    }

    private String getOperatingSystemCode() {
        if (OS_NAME.contains("windows")) {
            return "windows";
        } else if (OS_NAME.contains("linux")) {
            return "linux";
        } else if (OS_NAME.contains("mac")) {
            return "darwin";
        } else if (OS_NAME.contains("freebsd")) {
            return "freebsd";
        } else if (OS_NAME.contains("openbsd")) {
            return "openbsd";
        } else if (OS_NAME.contains("solaris") || OS_NAME.contains("sunos")) {
            return "solaris";
        }
        return "Unsupported OS";
    }
}
