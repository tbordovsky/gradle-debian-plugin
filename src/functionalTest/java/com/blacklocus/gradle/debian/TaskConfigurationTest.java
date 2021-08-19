package com.blacklocus.gradle.debian;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.io.FileWriter;
import java.nio.file.Files;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TaskConfigurationTest {

    private File testProjectDir;

    @Before
    public void setup() throws IOException {
        testProjectDir = new File("build/functionalTest");
        Files.createDirectories(testProjectDir.toPath());
        writeStringToFile(new File(testProjectDir, "settings.gradle"), "");
        writeStringToFile(new File(testProjectDir, "build.gradle"), String.join("\n",
                "plugins {",
                "   id('com.blacklocus.gradle.debian')",
                "}"
        ));
    }

    @Test
    public void buildDebExists() {
        BuildResult result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir)
            .withArguments("help", "--task", "buildDeb")
            .build();

        assertTrue(result.getOutput().contains(BuildDeb.TASK_NAME));
        assertTrue(result.getOutput().contains(BuildDeb.TASK_GROUP));
        assertTrue(result.getOutput().contains(BuildDeb.TASK_DESCRIPTION));
        assertEquals(TaskOutcome.SUCCESS, result.task(":help").getOutcome());
    }

    private void writeStringToFile(File file, String string) throws IOException {
        try (Writer writer = new FileWriter(file)) {
            writer.write(string);
        }
    }
}
