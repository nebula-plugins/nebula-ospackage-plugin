package nebula.plugin.ospackage.application

import com.netflix.gradle.plugins.packaging.ProjectPackagingExtension
import com.netflix.gradle.plugins.packaging.SystemPackagingPlugin
import nebula.test.PluginProjectSpec
import org.gradle.api.plugins.ApplicationPlugin

class OspackageApplicationPluginSpec extends PluginProjectSpec {
    @Override
    String getPluginName() {
        'nebula-ospackage-application'
    }

    def 'project modified by plugin'() {
        when:
        project.plugins.apply OspackageApplicationPlugin

        then:
        project.plugins.getPlugin(ApplicationPlugin)
        project.plugins.getPlugin(SystemPackagingPlugin)

        def children = project.extensions.getByType(ProjectPackagingExtension).getChildren().collect()
        children.size() > 0
    }
}
