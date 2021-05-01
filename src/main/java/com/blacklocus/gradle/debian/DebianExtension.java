package com.blacklocus.gradle.debian;

import java.io.File;

public class DebianExtension {

    private String sourcePath;

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getSourcePath() {
        return sourcePath;
    }
}
