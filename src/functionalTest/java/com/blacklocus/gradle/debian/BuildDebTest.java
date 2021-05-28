package com.blacklocus.gradle.debian;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BuildDebTest {
    @Test
    public void canRunTask() throws IOException {
        // Setup the test build
        File projectDir = new File("examples/hello");

        // Run the build
        BuildResult result = GradleRunner.create()
                .withPluginClasspath()
                .withProjectDir(projectDir)
                .withArguments("buildDeb")
                .forwardOutput()
                .build();

        // Verify the result
        assertTrue(result.getOutput().contains("buildDeb"));
        assertEquals(TaskOutcome.SUCCESS, result.task(":buildDeb").getOutcome());
    }
}
