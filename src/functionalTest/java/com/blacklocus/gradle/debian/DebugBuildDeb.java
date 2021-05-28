package com.blacklocus.gradle.debian;


import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

@Ignore
public class DebugBuildDeb {

    @Test
    public void debug() {
        File testProjectDir = new File("examples/hello");

        // Run the build
        BuildResult result = GradleRunner.create()
                .withPluginClasspath()
                .withProjectDir(testProjectDir)
                .withArguments("buildDeb", "--info")
                .forwardOutput()
                .withDebug(true)
                .build();
    }

}
