package nebula.plugin.ospackage.daemon

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction

/**
 * Monster class that does everything.
 */
class DaemonTemplateTask extends DefaultTask {
    //@Input
    Map<String, String> context

    //@Input
    Collection<String> templates

    @Input
    File destDir

    @TaskAction
    def template() {
        TemplateHelper templateHelper = new TemplateHelper(destDir, '/nebula/plugin/ospackage/daemon')
        templates.collect{ String templateName ->
            templateHelper.generateFile(templateName, context)
        }
    }

    //@OutputFiles
    Collection<File> getTemplatesOutout() {
        return templates.collect {
            new File(destDir, it)
        }
    }
}
