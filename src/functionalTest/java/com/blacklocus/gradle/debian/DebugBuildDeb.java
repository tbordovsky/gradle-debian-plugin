package com.blacklocus.gradle.debian;

import org.gradle.testkit.runner.GradleRunner;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

@Ignore
public class DebugBuildDeb {

    private static final File TEST_PROJECT_DIR = new File("examples/hello");

    @Test
    public void debug() {
        GradleRunner.create()
                .withPluginClasspath()
                .withProjectDir(TEST_PROJECT_DIR)
                .withArguments("buildDeb", "--info")
                .forwardOutput()
                .withDebug(true)
                .build();
    }

}
