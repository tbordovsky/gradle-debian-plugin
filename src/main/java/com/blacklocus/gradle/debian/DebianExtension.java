package com.blacklocus.gradle.debian;

import org.gradle.api.file.Directory;

public class DebianExtension {

    private Directory debianDirectory;

    public Directory getDebianDirectory() {
        return debianDirectory;
    }

    public void setDebianDirectory(Directory debianDirectory) {
        this.debianDirectory = debianDirectory;
    }

    private Directory provisioningDirectory;

    public Directory getProvisioningDirectory() {
        return provisioningDirectory;
    }

    public void setProvisioningDirectory(Directory provisioningDirectory) {
        this.provisioningDirectory = provisioningDirectory;
    }
}
