package com.blacklocus.gradle.debian;

import org.gradle.api.internal.file.copy.CopyAction;
import org.gradle.api.internal.file.copy.CopyActionProcessingStream;
import org.gradle.api.internal.file.copy.FileCopyDetailsInternal;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.WorkResult;
import org.gradle.api.tasks.WorkResults;
import org.vafer.jdeb.DataProducer;
import org.vafer.jdeb.producers.DataProducerDirectory;
import org.vafer.jdeb.producers.DataProducerFile;

import java.io.File;
import java.util.List;

public class DebCopyAction implements CopyAction {

    private static final Logger LOG = Logging.getLogger(DebCopyAction.class);

    private final File targetDir;
    private final List<DataProducer> dataProducers;

    public DebCopyAction(File targetDir, List<DataProducer> dataProducers) {
        this.targetDir = targetDir;
        this.dataProducers = dataProducers;
    }

    @Override
    public WorkResult execute(CopyActionProcessingStream stream) {
        stream.process(fileCopyDetails -> {
            if (fileCopyDetails.isDirectory()) {
                addDirectory(fileCopyDetails);
            } else {
                addFile(fileCopyDetails);
            }
        });
        return WorkResults.didWork(true);
    }

    private void addFile(FileCopyDetailsInternal fileCopyDetails) {
        // we can manage file attributes here if necessary, e.g., users, groups, etc.
        File target = new File(targetDir, fileCopyDetails.getPath());
        LOG.info("Copying {} to {}", fileCopyDetails, target);
        fileCopyDetails.copyTo(target);

        if (!isControlFile(fileCopyDetails)) {
            DataProducer dataProducer = new DataProducerFile(target, fileCopyDetails.getPath(), null, null, null);
            dataProducers.add(dataProducer);
        }
    }

    private void addDirectory(FileCopyDetailsInternal fileCopyDetails) {
        File target = new File(targetDir, fileCopyDetails.getPath());
        LOG.info("Copying {} to {}", fileCopyDetails, target);
        fileCopyDetails.copyTo(target);

        DataProducer dataProducer = new DataProducerDirectory(target, new String[]{}, new String[]{}, null);
        dataProducers.add(dataProducer);
    }

    private static Boolean isControlFile(FileCopyDetailsInternal fileCopyDetails) {
        // comparing to relative paths, so take off the leading /
        return fileCopyDetails.getPath().startsWith(BuildDeb.CONTROL_DIRECTORY_PATH.substring(1));
    }

}
