package com.blacklocus.gradle.debian;

import org.apache.commons.io.FileUtils;
import org.gradle.api.GradleException;
import org.gradle.api.internal.file.copy.CopyAction;
import org.gradle.api.internal.file.copy.CopyActionProcessingStream;
import org.gradle.api.internal.file.copy.FileCopyDetailsInternal;
import org.gradle.api.tasks.WorkResult;
import org.gradle.api.tasks.WorkResults;
import org.gradle.internal.UncheckedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vafer.jdeb.Compression;
import org.vafer.jdeb.Console;
import org.vafer.jdeb.DataProducer;
import org.vafer.jdeb.DebMaker;
import org.vafer.jdeb.PackagingException;
import org.vafer.jdeb.producers.DataProducerDirectory;
import org.vafer.jdeb.producers.DataProducerFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DebCopyAction implements CopyAction {

    private static final Logger LOG = LoggerFactory.getLogger(DebCopyAction.class);

    private final BuildDeb task;
    private final File tempDir;
    private final File debianDir;
    private final File rootDir;
    private final File debFile;
    private final List<DataProducer> dataProducers = new ArrayList<>();
    private final List<DataProducer> confProducers = new ArrayList<>();

    // probably just need to pass in the task, everything can be derived from its extension
    public DebCopyAction(BuildDeb task, File tempDir, File debianDir, File rootDir, File debFile) {
        this.task = task;
        this.tempDir = tempDir;
        this.debianDir = debianDir;
        this.rootDir = rootDir;
        this.debFile = debFile;
    }

    @Override
    public WorkResult execute(CopyActionProcessingStream stream) {
        try {
            stream.process(fileCopyDetails -> {
                LOG.info("Streaming: " + fileCopyDetails.getPath());
                if (fileCopyDetails.isDirectory()) {
                    addDirectory(fileCopyDetails);
                } else {
                    addFile(fileCopyDetails);
                }
            });

            FileUtils.copyDirectory(rootDir, task.getTemporaryDir());
            File targetDebianDir = new File(task.getTemporaryDir(), "debian");
            FileUtils.copyDirectory(debianDir, targetDebianDir);
            generateMaintainerScripts(targetDebianDir);

            DebMaker maker = new DebMaker(new GradleLoggerConsole(), dataProducers, confProducers);
            maker.setControl(targetDebianDir);
            maker.setDeb(debFile);

            try {
                LOG.info("Creating debian package: {}", debFile);
                maker.setCompression(Compression.GZIP.toString());
                maker.makeDeb();
            } catch (PackagingException e) {
                throw new GradleException("Can't build debian package " + debFile, e);
            }
        } catch (Exception e) {
            // TODO use something besides this gradle internal class
            UncheckedException.throwAsUncheckedException(e);
        }
        return WorkResults.didWork(true);
    }

    private void generateMaintainerScripts(File tmpDebianDir) {
        File control = new File(tmpDebianDir, "control");
        if (!control.exists()) {
            try {
                String content = String.join("\n",
                        "Package: " + task.getProject().getName(),
                        "Version: " + task.getProject().getVersion(),
                        "License: unknown",
                        "Vendor: BlackLocus",
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
                e.printStackTrace();
            }

        }
    }

    private void addFile(FileCopyDetailsInternal fileCopyDetails) {
        // we can manage file attributes here if necessary, e.g., users, groups, etc.
//        int fileMode = fileDetails.mode
        File target = new File(tempDir, fileCopyDetails.getPath());
        LOG.info("Copying {} to {}", fileCopyDetails, target);
        fileCopyDetails.copyTo(target);
        // probably need to implement a DataProducer with users and groups here
        DataProducer dataProducer = new DataProducerFile(target, tempDir.getPath(), null, null, null);
        dataProducers.add(dataProducer);
    }

    private void addDirectory(FileCopyDetailsInternal fileCopyDetails) {
        File target = new File(tempDir, fileCopyDetails.getPath());
        LOG.info("Copying {} to {}", fileCopyDetails, target);
        fileCopyDetails.copyTo(target);

        // probably need to implement a DataProducer with users and groups here
        DataProducer dataProducer = new DataProducerDirectory(target, new String[]{}, new String[]{}, null);
        dataProducers.add(dataProducer);
    }

    private static class GradleLoggerConsole implements Console {
        @Override
        public void debug(String message) {
            LOG.debug(message);
        }

        @Override
        public void info(String message) {
            LOG.info(message);
        }

        @Override
        public void warn(String message) {
            LOG.info(message);
        }
    }
}
