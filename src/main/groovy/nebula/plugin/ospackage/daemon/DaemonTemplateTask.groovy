package nebula.plugin.ospackage.daemon

import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Monster class that does everything.
 */
class DaemonTemplateTask extends ConventionTask {
    //@Input
    Map<String, String> context

    //@Input
    Collection<String> templates

    @Input
    File destDir

    @TaskAction
    def template() {
        TemplateHelper templateHelper = new TemplateHelper(getDestDir(), '/nebula/plugin/ospackage/daemon')
        getTemplates().collect { String templateName ->
            templateHelper.generateFile(templateName, getContext())
        }
    }

    //@OutputFiles
    Collection<File> getTemplatesOutout() {
        return templates.collect {
            new File(destDir, it)
        }
    }
}
