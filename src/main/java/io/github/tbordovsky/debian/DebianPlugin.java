package io.github.tbordovsky.debian;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.distribution.plugins.DistributionPlugin;
import org.gradle.api.plugins.ApplicationPlugin;
import org.gradle.api.plugins.BasePlugin;

/**
 * Package java applications for Debian GNU/Linux
 */
public class DebianPlugin implements Plugin<Project> {

    private static final String PLUGIN_NAME = "debian_packaging";

    public void apply(Project project) {
        DebianExtension extension = project.getExtensions().create(PLUGIN_NAME, DebianExtension.class);
        project.getPlugins().apply(BasePlugin.class);
        project.getPlugins().apply(ApplicationPlugin.class);

        project.getTasks().register(
                BuildDeb.TASK_NAME,
                BuildDeb.class,
                task -> {
                    task.setGroup(BuildDeb.TASK_GROUP);
                    task.setDescription(BuildDeb.TASK_DESCRIPTION);
                    task.dependsOn(project.getTasks().getByName(DistributionPlugin.TASK_INSTALL_NAME));
                    task.configureArchiveCopySpecs(project, extension);
                });
    }
}
