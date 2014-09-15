package nebula.plugin.ospackage.application

import nebula.plugin.ospackage.daemon.DaemonExtension
import nebula.plugin.ospackage.daemon.OspackageDaemonPlugin
import nebula.test.PluginProjectSpec
import org.gradle.api.plugins.ApplicationPlugin

class OspackageApplicationDaemonPluginSpec extends PluginProjectSpec {
    @Override
    String getPluginName() {
        'nebula-ospackage-application-daemon'
    }

    def 'project modified by plugin'() {
        when:
        project.plugins.apply OspackageApplicationDaemonPlugin

        then:
        project.plugins.getPlugin(ApplicationPlugin)
        project.plugins.getPlugin(OspackageDaemonPlugin)
        project.ext.has('applicationdaemon')

        def daemonExt = project.extensions.getByType(DaemonExtension)
        daemonExt.daemons.size() == 0

        when:
        project.evaluate()

        then:
        daemonExt.daemons.size() == 1

    }
}
