package nebula.plugin.ospackage.application

import com.netflix.gradle.plugins.packaging.ProjectPackagingExtension
import com.netflix.gradle.plugins.packaging.SystemPackagingBasePlugin
import com.netflix.gradle.plugins.packaging.SystemPackagingPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.IConventionAware
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.plugins.ApplicationPluginConvention
import org.gradle.api.tasks.application.CreateStartScripts

/**
 * Combine the os-package with the Application plugin. Currently heavily opinionated to where
 * the code will live, though that is slightly configurable using the ospackage-application extension.
 *
 * TODO Make a base plugin, so that this plugin can require os-package
 *
 * Usage:
 * <ul>
 *     <li>User has to provide a mainClassName
 *     <li>User has to create SystemPackaging tasks, the easiest way is to apply plugin: 'os-package'
 * </ul>
 */
class OspackageApplicationPlugin implements Plugin<Project> {

    OspackageApplicationExtension extension

    @Override
    void apply(Project project) {

        extension = project.extensions.create('ospackage_application', OspackageApplicationExtension)
        ((IConventionAware) extension).conventionMapping.map('prefix') { '/opt'}

        project.plugins.apply(ApplicationPlugin)
        ApplicationPluginConvention appConvention = project.convention.getPlugin(ApplicationPluginConvention)
        CreateStartScripts startScripts = (CreateStartScripts) project.tasks.getByName(ApplicationPlugin.TASK_START_SCRIPTS_NAME)

        project.plugins.apply(SystemPackagingBasePlugin.class)
        def packagingExt = project.extensions.getByType(ProjectPackagingExtension)
        packagingExt.with(appConvention.applicationDistribution)

        // Using a closure here to delay evaluation of prefix
        packagingExt.into { "${extension.getPrefix()}/${startScripts.applicationName}" }

        // Use post-install to fix permissions

        // Maybe stepping over the line, but it'll make it much easier to use.
        project.plugins.apply(SystemPackagingPlugin)

    }
}