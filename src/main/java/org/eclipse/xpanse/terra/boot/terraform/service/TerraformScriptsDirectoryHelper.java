/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.terra.boot.terraform.service;

import jakarta.annotation.Resource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.terra.boot.models.exceptions.TerraformExecutorException;
import org.eclipse.xpanse.terra.boot.models.request.git.TerraformScriptsGitRepoDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** bean to host all generic methods shared from different types of Terraform deployers. */
@Slf4j
@Component
public class TerraformScriptsDirectoryHelper {

    public static final String TF_SCRIPT_FILE_EXTENSION = ".tf";
    private static final String TF_STATE_FILE_NAME = "terraform.tfstate";
    private static final List<String> EXCLUDED_FILE_SUFFIX_LIST =
            Arrays.asList(".tf", ".tfstate", ".binary", ".hcl");

    @Value("${terraform.root.module.directory}")
    private String moduleParentDirectoryPath;

    @Value("${clean.workspace.after.deployment.enabled:true}")
    private Boolean cleanWorkspaceAfterDeployment;

    @Resource private TerraformScriptsGitRepoHelper terraformScriptsGitRepoHelper;

    /**
     * Create workspace for the Terraform deployment task.
     *
     * @param taskId id of the Terraform deployment task.
     * @return workspace path for the Terraform deployment task.
     */
    public String buildTaskWorkspace(String taskId) {
        File ws = getTaskWorkspace(taskId);
        if (!ws.exists() && !ws.mkdirs()) {
            throw new TerraformExecutorException(
                    "Create task workspace failed, File path not created: " + ws.getAbsolutePath());
        }
        return ws.getAbsolutePath();
    }

    /**
     * Get the workspace path for the Terraform deployment task.
     *
     * @param taskId id of the Terraform deployment task.
     * @return path of the workspace.
     */
    public File getTaskWorkspace(String taskId) {
        return new File(getModuleParentDirectoryPath(), taskId);
    }

    /**
     * Create the tfstate file in the taskWorkspace for the Terraform deployment task.
     *
     * @param taskWorkspace taskWorkspace path for the Terraform deployment task.
     * @param tfState state file contents as string.
     */
    public File createTfStateFile(String taskWorkspace, String tfState) {
        if (StringUtils.isBlank(tfState)) {
            throw new TerraformExecutorException("tfState file create error");
        }
        File stateFile = new File(taskWorkspace, TF_STATE_FILE_NAME);
        try (FileWriter scriptWriter = new FileWriter(stateFile, StandardCharsets.UTF_8, false)) {
            scriptWriter.write(tfState);
            log.info("tfState file create success, fileName: {}", stateFile.getAbsolutePath());
            return stateFile;
        } catch (IOException ex) {
            log.error("tfState file create failed.", ex);
            throw new TerraformExecutorException("tfState file create failed.", ex);
        }
    }

    /**
     * Prepare deployment files with scripts in the workspace for the Terraform deployment task.
     *
     * @param taskWorkspace workspace path for the Terraform deployment task.
     * @param scriptsMap map of script name and script content.
     * @param tfState tfState file contents as string.
     * @return list of script files.
     */
    public List<File> prepareDeploymentFilesWithScripts(
            String taskWorkspace, Map<String, String> scriptsMap, String tfState) {
        List<File> scriptFiles = buildScriptFiles(taskWorkspace, scriptsMap);
        List<File> files = new ArrayList<>(scriptFiles);
        if (StringUtils.isNotBlank(tfState)) {
            File tfStateFile = createTfStateFile(taskWorkspace, tfState);
            files.add(tfStateFile);
        }
        return files;
    }

    /**
     * Prepare deployment files with git repo in the workspace for the Terraform deployment task.
     *
     * @param taskWorkspace workspace path for the Terraform deployment task.
     * @param gitRepoDetails git repo details.
     * @param tfState tfState file contents as string.
     * @return list of script files.
     */
    public List<File> prepareDeploymentFilesWithGitRepo(
            String taskWorkspace, TerraformScriptsGitRepoDetails gitRepoDetails, String tfState) {
        List<File> scriptFiles =
                terraformScriptsGitRepoHelper.checkoutScripts(taskWorkspace, gitRepoDetails);
        List<File> projectFiles = new ArrayList<>(scriptFiles);
        if (StringUtils.isNotBlank(tfState)) {
            File tfStateFile = createTfStateFile(taskWorkspace, tfState);
            projectFiles.add(tfStateFile);
        }
        return projectFiles;
    }

    private List<File> buildScriptFiles(String taskWorkspace, Map<String, String> scriptsMap) {
        log.info("start build Terraform script");
        if (Objects.isNull(scriptsMap) || scriptsMap.isEmpty()) {
            throw new TerraformExecutorException(
                    "Terraform scripts create error, script files is empty.");
        }
        List<File> scriptFiles = new ArrayList<>();
        for (Map.Entry<String, String> scriptEntry : scriptsMap.entrySet()) {
            String scriptName = scriptEntry.getKey();
            String scriptContent = scriptEntry.getValue();
            if (StringUtils.isNotBlank(scriptName) && StringUtils.isNotBlank(scriptContent)) {
                File scriptFile = createScriptFile(taskWorkspace, scriptName, scriptContent);
                scriptFiles.add(scriptFile);
            }
        }
        if (CollectionUtils.isEmpty(scriptFiles)) {
            throw new TerraformExecutorException(
                    "Terraform scripts create error, all scripts is empty.");
        }
        return scriptFiles;
    }

    private File createScriptFile(String taskWorkspace, String scriptName, String scriptContent) {
        File scriptFile = new File(taskWorkspace, scriptName);
        try (FileWriter scriptWriter = new FileWriter(scriptFile, StandardCharsets.UTF_8, false)) {
            scriptWriter.write(scriptContent);
            log.info("Terraform script create success, fileName: {}", scriptFile.getAbsolutePath());
            return scriptFile;
        } catch (IOException ex) {
            log.error("Terraform script create failed.", ex);
            throw new TerraformExecutorException("Terraform script create failed.", ex);
        }
    }

    /**
     * Get the content of the tfState file in the workspace for the Terraform deployment task.
     *
     * @param taskWorkspace workspace path for the Terraform deployment task.
     * @return tfState file contents as string.
     */
    public String getTerraformState(String taskWorkspace) {
        String state = null;
        try {
            File tfState = new File(taskWorkspace, TF_STATE_FILE_NAME);
            if (tfState.exists()) {
                state = Files.readString(tfState.toPath());
            }
        } catch (IOException ex) {
            log.error("Read state file failed.", ex);
        }
        return state;
    }

    /**
     * Get the list of files in the workspace for the Terraform deployment task.
     *
     * @param taskWorkspace workspace path for the Terraform deployment task.
     * @return list of files.
     */
    public List<File> getDeploymentFilesFromTaskWorkspace(String taskWorkspace) {
        List<File> scriptFiles = new ArrayList<>();
        File workPath = new File(taskWorkspace);
        if (workPath.isDirectory() && workPath.exists()) {
            File[] files = workPath.listFiles();
            if (Objects.nonNull(files)) {
                Arrays.stream(files)
                        .forEach(
                                file -> {
                                    if (file.isFile()) {
                                        scriptFiles.add(file);
                                    }
                                });
            }
        }
        return scriptFiles;
    }

    /**
     * Get map of name and content of these generated files in the workspace for the Terraform
     * deployment task.
     *
     * @param taskWorkspace workspace path for the Terraform deployment task.
     * @param scriptFiles List of script files.
     * @return Map of file name and file content.
     */
    public Map<String, String> getDeploymentGeneratedFilesContent(
            String taskWorkspace, List<File> scriptFiles) {
        Map<String, String> fileContentMap = new HashMap<>();
        File workPath = new File(taskWorkspace);
        if (workPath.isDirectory() && workPath.exists()) {
            File[] files = workPath.listFiles();
            if (Objects.nonNull(files)) {
                Arrays.stream(files)
                        .filter(
                                file ->
                                        !scriptFiles.contains(file)
                                                && file.isFile()
                                                && !isExcludedFile(file.getName()))
                        .forEach(
                                file -> {
                                    String content = readFileContent(file);
                                    fileContentMap.put(file.getName(), content);
                                });
            }
        }
        return fileContentMap;
    }

    private String readFileContent(File file) {
        try {
            return Files.readString(file.toPath());
        } catch (IOException e) {
            log.error("Read file content with name:{} error.", file.getName(), e);
        }
        return null;
    }

    /**
     * Delete the workspace of the Terraform deployment task.
     *
     * @param taskWorkspace workspace path for the Terraform deployment task.
     */
    public void deleteTaskWorkspace(String taskWorkspace) {
        if (cleanWorkspaceAfterDeployment) {
            Path path = Paths.get(taskWorkspace).toAbsolutePath().normalize();
            try (Stream<Path> pathStream = Files.walk(path)) {
                pathStream
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(
                                file -> {
                                    if (!file.delete()) {
                                        log.warn(
                                                "Failed to delete file {}.",
                                                file.getAbsolutePath());
                                    }
                                });
            } catch (IOException e) {
                log.error("Delete task workspace:{} error", taskWorkspace, e);
            }
        }
    }

    private boolean isExcludedFile(String fileName) {
        if (StringUtils.isNotBlank(fileName) && fileName.contains(".")) {
            String fileSuffix = fileName.substring(fileName.lastIndexOf("."));
            return EXCLUDED_FILE_SUFFIX_LIST.contains(fileSuffix);
        }
        return false;
    }

    private String getModuleParentDirectoryPath() {
        return StringUtils.isNotBlank(moduleParentDirectoryPath)
                ? moduleParentDirectoryPath
                : System.getProperty("java.io.tmpdir");
    }
}
