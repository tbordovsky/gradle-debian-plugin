package com.blacklocus.gradle.debian;

import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.internal.file.copy.CopyAction;
import org.gradle.api.internal.file.copy.CopyActionExecuter;
import org.gradle.api.internal.file.copy.CopySpecInternal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.WorkResult;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

import java.io.File;

public class BuildDeb extends AbstractArchiveTask {

    public static final String TASK_NAME = "buildDeb";
    public static final String TASK_GROUP = "debian";
    public static final String TASK_DESCRIPTION = "Package a java application for Debian GNU/Linux";

    CopySpecInternal rootSpec;

    @TaskAction
    public void run() {
        this.setDuplicatesStrategy(DuplicatesStrategy.INCLUDE);
        CopyActionExecuter copyActionExecuter = this.createCopyActionExecuter();
        CopyAction copyAction = this.createCopyAction();
        WorkResult didWork = copyActionExecuter.execute(this.rootSpec, copyAction);
    }

    @Override
    protected CopyAction createCopyAction() {
        return new DebCopyAction(
                new File(this.getProject().getBuildDir(), "debian"),
                this.getTemporaryDir(),
                this.getArchiveFile().get().getAsFile());
    }
}
