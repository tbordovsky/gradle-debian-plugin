package com.blacklocus.gradle.debian;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.distribution.plugins.DistributionPlugin;
import org.gradle.api.file.CopySpec;
import org.gradle.api.internal.file.copy.CopyAction;
import org.gradle.api.internal.file.copy.CopyActionExecuter;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
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
import java.util.List;

public class BuildDeb extends AbstractArchiveTask {

    public static final String TASK_NAME = "buildDeb";
    public static final String TASK_GROUP = "debian";
    public static final String TASK_DESCRIPTION = "Package a java application for Debian GNU/Linux";

    private static final Logger LOG = Logging.getLogger(BuildDeb.class);

    protected static final String CONTROL_DIRECTORY_PATH = "/debian";
    protected static final String DEFAULT_INSTALL_PATH = "/opt";
    protected static final String PROVISIONING_PATH = "/";

    private final List<DataProducer> dataProducers = new ArrayList<>();
    private final List<DataProducer> confProducers = new ArrayList<>();

    public BuildDeb() {
        super();
        this.getArchiveExtension().set("deb");
    }

    protected void configureArchiveCopySpecs(Project project, DebianExtension extension) {
        Task installTask = project.getTasks().getByName(DistributionPlugin.TASK_INSTALL_NAME);
        String installOutputPath = installTask.getOutputs().getFiles().getSingleFile().getParent();
        CopySpec dataCopySpec = project.copySpec(copy -> copy
                .from(installOutputPath)
                .into(extension.getInstallPath().convention(DEFAULT_INSTALL_PATH)));
        CopySpec controlCopySpec = project.copySpec(copy -> copy
                .from(extension.getPreInstallFile().getOrNull())
                .from(extension.getPostInstallFile().getOrNull())
                .from(extension.getPreUninstallFile().getOrNull())
                .from(extension.getPostUninstallFile().getOrNull())
                .from(extension.getTriggerInstallFile().getOrNull())
                .from(extension.getTriggerUninstallFile().getOrNull())
                .from(extension.getTriggerPostUninstallFile().getOrNull())
                .into(CONTROL_DIRECTORY_PATH));
        CopySpec rootfsCopySpec = project.copySpec(copy -> copy
                .from(extension.getProvisioningDirectory()))
                .into(PROVISIONING_PATH);
        this.with(dataCopySpec, controlCopySpec, rootfsCopySpec);
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

        File controlDir = new File(this.getTemporaryDir(), CONTROL_DIRECTORY_PATH);
        generateMaintainerScripts(controlDir);

        File debFile = this.getArchiveFile().get().getAsFile();
        DebMaker maker = new DebMaker(new GradleLoggerConsole(), dataProducers, confProducers);
        maker.setControl(controlDir);
        maker.setDeb(debFile);

        try {
            maker.setCompression(Compression.GZIP.toString());
            maker.makeDeb();
        } catch (PackagingException e) {
            throw new GradleException("Can't build debian package " + debFile, e);
        }
    }

    @Override
    protected CopyAction createCopyAction() {
        return new DebCopyAction(this.getTemporaryDir(), dataProducers);
    }

    private void generateMaintainerScripts(File tmpDebianDir) {
        File control = new File(tmpDebianDir, "control");
        if (!control.exists()) {
            try {
                String content = String.join("\n",
                        "Package: " + this.getProject().getName(),
                        "Version: " + this.getArchiveVersion().getOrElse("0"),
                        "License: unknown",
                        "Vendor: vendor",
                        "Architecture: all",
                        "Maintainer: <root@localhost>",
                        "Section: default",
                        "Priority: extra",
                        "Homepage: http://example.com/no-uri-given",
                        "Description: no description given"
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
