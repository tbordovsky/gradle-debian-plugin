package com.blacklocus.gradle.debian;

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
                extension.getDebianDirectory(),
                this.getArchiveFile().get().getAsFile());
    }
}
