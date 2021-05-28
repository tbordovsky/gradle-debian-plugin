package com.blacklocus.gradle.debian;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public class DebianExtension {

    private final DirectoryProperty debianDirectory;
    private final DirectoryProperty provisioningDirectory;

    @Inject
    public DebianExtension(ObjectFactory objectFactory) {
        debianDirectory = objectFactory.directoryProperty();
        provisioningDirectory = objectFactory.directoryProperty();
    }

    public DirectoryProperty getDebianDirectory() {
        return debianDirectory;
    }

    public DirectoryProperty getProvisioningDirectory() {
        return provisioningDirectory;
    }
}
