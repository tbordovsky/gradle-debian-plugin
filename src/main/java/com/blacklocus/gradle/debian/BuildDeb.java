package com.blacklocus.gradle.debian;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.distribution.plugins.DistributionPlugin;
import org.gradle.api.file.CopySpec;
import org.gradle.api.internal.file.copy.CopyAction;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

public class BuildDeb extends AbstractArchiveTask {

    public static final String TASK_NAME = "buildDeb";
    public static final String TASK_GROUP = "debian";
    public static final String TASK_DESCRIPTION = "Package a java application for Debian GNU/Linux";

    public BuildDeb() {
        super();
        this.getArchiveExtension().set("deb");
    }

    @Override
    protected CopyAction createCopyAction() {
        DebianExtension extension = this.getProject().getExtensions().findByType(DebianExtension.class);
        return new DebCopyAction(
                this,
                this.getTemporaryDir(),
                extension.getDebianDirectory().get(),
                this.getArchiveFile().get().getAsFile());
    }

    void configureArchiveCopySpecs(Project project, DebianExtension extension) {
        Task installTask = project.getTasks().getByName(DistributionPlugin.TASK_INSTALL_NAME);
        String installOutputPath = installTask.getOutputs().getFiles().getSingleFile().getParent();
        CopySpec dataCopySpec = project.copySpec(copy -> copy.from(installOutputPath).into("/opt/blacklocus"));
        CopySpec controlCopySpec = project.copySpec(copy -> copy.from(extension.getDebianDirectory()).into("/debian"));
        CopySpec rootfsCopySpec = project.copySpec(copy -> copy.from(extension.getProvisioningDirectory())).into("/");
        this.with(dataCopySpec, controlCopySpec, rootfsCopySpec);
    }
}
