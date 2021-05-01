package com.blacklocus.gradle.debian;

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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DebCopyAction implements CopyAction {

    private static final Logger LOG = LoggerFactory.getLogger(DebCopyAction.class);

    private final File tempDir;
    private final File debianDir;
    private final File debFile;
    private final List<DataProducer> dataProducers = new ArrayList<>();
    private final List<DataProducer> confProducers = new ArrayList<>();

    public DebCopyAction(File tempDir, File debianDir, File debFile) {
        this.debianDir = debianDir;
        this.tempDir = tempDir;
        this.debFile = debFile;
    }

    @Override
    public WorkResult execute(CopyActionProcessingStream stream) {
        try {
            debianDir.delete();
            debianDir.mkdirs();

            stream.process(fileCopyDetails -> {
                if (fileCopyDetails.isDirectory()) {
                    addDirectory(fileCopyDetails);
                } else {
                    addFile(fileCopyDetails);
                }
            });

            DebMaker maker = new DebMaker(new GradleLoggerConsole(), dataProducers, confProducers);
            maker.setControl(debianDir);
            maker.setDeb(debFile);

            try {
                LOG.info("Creating debian package: {}", debFile);
                maker.setCompression(Compression.GZIP.toString());
                maker.makeDeb();
            } catch (PackagingException e) {
                throw new GradleException("Can't build debian package " + debFile, e);
            }
        } catch (Exception e) {
            UncheckedException.throwAsUncheckedException(e);
        }
        return WorkResults.didWork(true);
    }

    private void addFile(FileCopyDetailsInternal fileCopyDetails) {
//        String user = lookup(specToLookAt, 'user') ?: task.user
//        Integer uid = (Integer) lookup(specToLookAt, 'uid') ?: task.uid ?: 0
//        String group = lookup(specToLookAt, 'permissionGroup') ?: task.permissionGroup
//        Integer gid = (Integer) lookup(specToLookAt, 'gid') ?: task.gid ?: 0
//
//        int fileMode = fileDetails.mode
//
//        debFileVisitorStrategy.addFile(fileDetails, inputFile, user, uid, group, gid, fileMode)
        File inputFile = fileCopyDetails.getFile();
        // probably need to implement a DataProducer with users and groups here
        DataProducer dataProducer = new DataProducerFile(inputFile, debianDir.getPath(), null, null, null);
        dataProducers.add(dataProducer);
    }

    private void addDirectory(FileCopyDetailsInternal fileCopyDetails) {
        File inputDirectory = fileCopyDetails.getFile();
        // probably need to implement a DataProducer with users and groups here
        DataProducer dataProducer = new DataProducerDirectory(inputDirectory, new String[]{}, new String[]{}, null);
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
