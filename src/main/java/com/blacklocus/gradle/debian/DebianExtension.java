package com.blacklocus.gradle.debian;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;

import javax.inject.Inject;

public class DebianExtension {

    private final Property<String> installPath;
    private final DirectoryProperty provisioningDirectory;
    private final RegularFileProperty preInstallFile;
    private final RegularFileProperty postInstallFile;
    private final RegularFileProperty preUninstallFile;
    private final RegularFileProperty postUninstallFile;
    private final RegularFileProperty triggerInstallFile;
    private final RegularFileProperty triggerUninstallFile;
    private final RegularFileProperty triggerPostUninstallFile;

    @Inject
    public DebianExtension(ObjectFactory objectFactory) {
        this.installPath = objectFactory.property(String.class);
        this.provisioningDirectory = objectFactory.directoryProperty();
        this.preInstallFile = objectFactory.fileProperty();
        this.postInstallFile = objectFactory.fileProperty();
        this.preUninstallFile = objectFactory.fileProperty();
        this.postUninstallFile = objectFactory.fileProperty();
        this.triggerInstallFile = objectFactory.fileProperty();
        this.triggerUninstallFile = objectFactory.fileProperty();
        this.triggerPostUninstallFile = objectFactory.fileProperty();
    }

    @Optional
    public Property<String> getInstallPath() {
        return installPath;
    }

    @InputDirectory
    @Optional
    @PathSensitive(PathSensitivity.RELATIVE)
    public DirectoryProperty getProvisioningDirectory() {
        return provisioningDirectory;
    }

    @InputFile
    @Optional
    @PathSensitive(PathSensitivity.RELATIVE)
    RegularFileProperty getPreInstallFile() {
        return preInstallFile;
    }

    @InputFile
    @Optional
    @PathSensitive(PathSensitivity.RELATIVE)
    RegularFileProperty getPostInstallFile() {
        return postInstallFile;
    }

    @InputFile
    @Optional
    @PathSensitive(PathSensitivity.RELATIVE)
    RegularFileProperty getPreUninstallFile() {
        return preUninstallFile;
    }

    @InputFile
    @Optional
    @PathSensitive(PathSensitivity.RELATIVE)
    RegularFileProperty getPostUninstallFile() {
        return postUninstallFile;
    }

    @InputFile
    @Optional
    @PathSensitive(PathSensitivity.RELATIVE)
    RegularFileProperty getTriggerInstallFile() {
        return triggerInstallFile;
    }

    @InputFile
    @Optional
    @PathSensitive(PathSensitivity.RELATIVE)
    RegularFileProperty getTriggerUninstallFile() {
        return triggerUninstallFile;
    }

    @InputFile
    @Optional
    @PathSensitive(PathSensitivity.RELATIVE)
    RegularFileProperty getTriggerPostUninstallFile() {
        return triggerPostUninstallFile;
    }
}
