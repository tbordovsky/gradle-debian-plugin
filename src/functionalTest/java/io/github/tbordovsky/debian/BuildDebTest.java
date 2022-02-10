package io.github.tbordovsky.debian;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BuildDebTest {

    private static final File TEST_PROJECT_DIR = new File("examples/hello");

    @Test
    public void canRunTask() {
        BuildResult result = GradleRunner.create()
                .withPluginClasspath()
                .withProjectDir(TEST_PROJECT_DIR)
                .withArguments("buildDeb")
                .build();

        assertTrue(result.getOutput().contains(BuildDeb.TASK_NAME));
        assertEquals(TaskOutcome.SUCCESS, result.task(":buildDeb").getOutcome());
    }
}
