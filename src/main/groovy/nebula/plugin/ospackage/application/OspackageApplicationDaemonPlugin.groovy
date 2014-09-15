package nebula.plugin.ospackage.application

import nebula.plugin.ospackage.daemon.DaemonExtension
import nebula.plugin.ospackage.daemon.OspackageDaemonPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.tasks.application.CreateStartScripts
import org.gradle.util.ConfigureUtil

/**
 * Combine the nebula-ospackage-application with the nebula-ospackage-daemon plugin. As with the nebula-ospackage-application,
 * this is opinionated to where the application lives, but is relatively flexible to how the daemon runs.
 *
 * TODO Make a base plugin, so that this plugin can require os-package
 *
 * Usage (from nebula-ospackage-application):
 * <ul>
 *     <li>User has to provide a mainClassName
 *     <li>User has to create SystemPackaging tasks, the easiest way is to apply plugin: 'os-package'
 * </ul>
 */
class OspackageApplicationDaemonPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        project.plugins.apply(OspackageApplicationPlugin)
        def ospackageApplicationExtension = project.extensions.getByType(OspackageApplicationExtension)

        CreateStartScripts startScripts = (CreateStartScripts) project.tasks.getByName(ApplicationPlugin.TASK_START_SCRIPTS_NAME)

        project.plugins.apply(OspackageDaemonPlugin)

        // Mechanism for user to configure daemon further
        List<Closure> daemonConfiguration = []
        project.ext.applicationdaemon = { Closure closure ->
            daemonConfiguration << closure
        }

        // TODO Convention mapping on definition instead of afterEvaluate
        project.afterEvaluate {
            // TODO Sanitize name
            def name = startScripts.applicationName

            // Add daemon to project
            def daemonExt = project.extensions.getByType(DaemonExtension)
            def definition = daemonExt.daemon {
                daemonName = name
                command = "${ospackageApplicationExtension.prefix}/${name}/bin/${name}"
            }

            daemonConfiguration.each { confClosure ->
                ConfigureUtil.configure(confClosure, definition)
            }
        }
    }
}