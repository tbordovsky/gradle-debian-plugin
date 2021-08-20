package com.tbordovsky.debian;

import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.api.Project;
import org.junit.Test;
import static org.junit.Assert.*;

public class DebianPluginTest {
    @Test public void pluginRegistersATask() {
        // Create a test project and apply the plugin
        Project project = ProjectBuilder.builder().build();
        project.getPlugins().apply("com.tbordovsky.debian");

        // Verify the result
        assertNotNull(project.getTasks().findByName("buildDeb"));
    }
}
