package com.tbordovsky.debian;

import org.gradle.api.internal.file.copy.CopyAction;
import org.gradle.api.internal.file.copy.CopyActionProcessingStream;
import org.gradle.api.internal.file.copy.FileCopyDetailsInternal;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.WorkResult;
import org.gradle.api.tasks.WorkResults;
import org.vafer.jdeb.DataProducer;
import org.vafer.jdeb.mapping.Mapper;
import org.vafer.jdeb.mapping.PermMapper;
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
        LOG.debug("Copying {} to {}", fileCopyDetails, target);
        fileCopyDetails.copyTo(target);

        if (!isControlFile(fileCopyDetails)) {
            // this mapper is required in order to preserve the file mode
            Mapper mapper = new PermMapper(-1, -1, null, null, fileCopyDetails.getMode(), -1, -1, null);
            DataProducer dataProducer = new DataProducerFile(
                    target, fileCopyDetails.getPath(), null, null, new Mapper[]{mapper});
            dataProducers.add(dataProducer);
        }
    }

    private void addDirectory(FileCopyDetailsInternal fileCopyDetails) {
        File target = new File(targetDir, fileCopyDetails.getPath());
        LOG.debug("Copying {} to {}", fileCopyDetails, target);
        fileCopyDetails.copyTo(target);

        // this mapper is required in order to preserve the dir mode
        Mapper mapper = new PermMapper(-1, -1, null, null, fileCopyDetails.getMode(), -1, -1, null);
        DataProducer dataProducer = new DataProducerDirectory(target, new String[]{}, new String[]{}, new Mapper[]{mapper});
        dataProducers.add(dataProducer);
    }

    private static Boolean isControlFile(FileCopyDetailsInternal fileCopyDetails) {
        // comparing to relative paths, so take off the leading /
        return fileCopyDetails.getPath().startsWith(BuildDeb.ARCHIVE_CONTROL_PATH.substring(1));
    }

}
