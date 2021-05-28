package com.blacklocus.gradle.debian;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

public class DebianExtension {

    private final DirectoryProperty debianDirectory;
    private final DirectoryProperty provisioningDirectory;
    private final Property<String> installPath;

    @Inject
    public DebianExtension(ObjectFactory objectFactory) {
        this.debianDirectory = objectFactory.directoryProperty();
        this.provisioningDirectory = objectFactory.directoryProperty();
        this.installPath = objectFactory.property(String.class);
    }

    public DirectoryProperty getDebianDirectory() {
        return debianDirectory;
    }

    public DirectoryProperty getProvisioningDirectory() {
        return provisioningDirectory;
    }

    public Property<String> getInstallPath() {
        return installPath;
    }
}
