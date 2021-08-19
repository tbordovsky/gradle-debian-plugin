package com.blacklocus.gradle.debian;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.distribution.plugins.DistributionPlugin;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.internal.file.copy.CopyAction;
import org.gradle.api.internal.file.copy.CopyActionExecuter;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;
import org.gradle.api.tasks.WorkResult;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.vafer.jdeb.Compression;
import org.vafer.jdeb.Console;
import org.vafer.jdeb.DataProducer;
import org.vafer.jdeb.DebMaker;
import org.vafer.jdeb.PackagingException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class BuildDeb extends AbstractArchiveTask {

    public static final String TASK_NAME = "buildDeb";
    public static final String TASK_GROUP = "debian";
    public static final String TASK_DESCRIPTION = "Package a java application for Debian GNU/Linux";

    private static final Logger LOG = Logging.getLogger(BuildDeb.class);

    protected static final String ARCHIVE_CONTROL_PATH = "/debian";
    protected static final String ARCHIVE_PROVISIONING_PATH = "/";
    protected static final String DEFAULT_INSTALL_PATH = "/opt";

    private final List<DataProducer> dataProducers = new ArrayList<>();
    private final List<DataProducer> confProducers = new ArrayList<>();

    public BuildDeb() {
        super();
        this.getArchiveExtension().set("deb");
    }

    protected void configureArchiveCopySpecs(Project project, DebianExtension extension) {
        LOG.info("Configure copy specs for debian archive.");

        Task installTask = project.getTasks().getByName(DistributionPlugin.TASK_INSTALL_NAME);
        String javaInstallDirectory = installTask.getOutputs().getFiles().getSingleFile().getParent();
        LOG.info("Collecting java files from javaInstallDirectory: {}", javaInstallDirectory);
        String archiveInstallPath = extension.getInstallPath().getOrElse(DEFAULT_INSTALL_PATH);
        CopySpec dataCopySpec = project.copySpec(copy -> copy
                .from(javaInstallDirectory)
                .into(archiveInstallPath));
        this.with(dataCopySpec);

        Set<RegularFile> controlFiles = getControlFiles(extension);
        if (!controlFiles.isEmpty()) {
            LOG.info("Collecting control files: {}", controlFiles);
            CopySpec controlCopySpec = project.copySpec(copy -> copy
                    .from(controlFiles)
                    .into(ARCHIVE_CONTROL_PATH));
            this.with(controlCopySpec);
        }

        DirectoryProperty provisioningDirectory = extension.getProvisioningDirectory();
        LOG.info("provisioningDirectory source: {}", provisioningDirectory.getOrNull());
        if (provisioningDirectory.isPresent()) {
            LOG.info("Collecting provisioning files from directory: {}", provisioningDirectory);
            CopySpec rootfsCopySpec = project.copySpec(copy -> copy
                    .from(provisioningDirectory.get()))
                    .into(ARCHIVE_PROVISIONING_PATH);
            this.with(rootfsCopySpec);
        }
    }

    private Set<RegularFile> getControlFiles(DebianExtension extension) {
        Set<RegularFile> controlFiles = new HashSet<>();
        controlFiles.add(extension.getPreInstallFile().getOrNull());
        controlFiles.add(extension.getPostInstallFile().getOrNull());
        controlFiles.add(extension.getPreUninstallFile().getOrNull());
        controlFiles.add(extension.getPostUninstallFile().getOrNull());
        controlFiles.add(extension.getTriggerInstallFile().getOrNull());
        controlFiles.add(extension.getTriggerUninstallFile().getOrNull());
        controlFiles.add(extension.getTriggerPostUninstallFile().getOrNull());
        return controlFiles.stream().filter(Objects::nonNull).collect(Collectors.toSet());
    }

    @OutputFile
    File getChangesFile() {
        return new File(getArchiveFile().get().getAsFile().getPath().replaceFirst("deb$", "changes"));
    }

    @TaskAction
    @Override
    protected void copy() {
        DebianExtension extension = this.getProject().getExtensions().findByType(DebianExtension.class);

        CopyActionExecuter copyActionExecuter = this.createCopyActionExecuter();
        CopyAction copyAction = this.createCopyAction();
        WorkResult didWork = copyActionExecuter.execute(this.getRootSpec(), copyAction);
        this.setDidWork(didWork.getDidWork());

        File controlDir = new File(this.getTemporaryDir(), ARCHIVE_CONTROL_PATH);
        controlDir.mkdir();
        generateControlFile(controlDir, extension);

        File debFile = this.getArchiveFile().get().getAsFile();
        DebMaker maker = new DebMaker(new GradleLoggerConsole(), dataProducers, confProducers);
        maker.setControl(controlDir);
        maker.setDeb(debFile);
        maker.setCompression(Compression.GZIP.toString());

        try {
            maker.makeDeb();
        } catch (PackagingException ex) {
            throw new TaskExecutionException(this, ex);
        }
    }

    @Override
    protected CopyAction createCopyAction() {
        return new DebCopyAction(this.getTemporaryDir(), dataProducers);
    }

    private void generateControlFile(File tmpDebianDir, DebianExtension extension) {
        File control = new File(tmpDebianDir, "control");
        if (!control.exists()) {
            try {
                String content = String.join("\n",
                        "Package: " + extension.getPackageName().getOrElse(this.getProject().getName()),
                        "Version: " + extension.getVersion().getOrElse("0"),
                        "Section: " + extension.getSection().getOrElse("java"),
                        "Priority: " + extension.getPriority().getOrElse("optional"),
                        "Architecture: " + extension.getArchitecture().getOrElse("all"),
                        "Maintainer: " + extension.getMaintainer().getOrElse("<root@localhost>"),
                        "Description: " + extension.getPackageDescription().getOrElse("no description given")
                );

                control.createNewFile();
                FileWriter fw = new FileWriter(control.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(content);
                bw.close();
            } catch (IOException e) {
                LOG.error("Failed to write debian control file.", e);
            }
        }
    }

    private static class GradleLoggerConsole implements Console {
        @Override
        public void debug(String message) {
            LOG.debug(message);
        }

        @Override
        public void info(String message) {
            LOG.quiet(message);
        }

        @Override
        public void warn(String message) {
            LOG.info(message);
        }
    }
}
