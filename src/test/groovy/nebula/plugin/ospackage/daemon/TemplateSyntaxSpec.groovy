package nebula.plugin.ospackage.daemon

import nebula.test.ProjectSpec

class TemplateSyntaxSpec extends ProjectSpec {

    def 'each template validates'() {
        when:
        def plugin = new OspackageDaemonPlugin()
        def templates = ['initd', 'log-run', 'run']
        def helper = new TemplateHelper(projectDir, '/nebula/plugin/ospackage/daemon')
        DaemonDefinition definition = new DaemonDefinition()
        def context = plugin.toContext(new DaemonDefinition(), definition)
        context['isRedhat'] = true
        templates.each {
            helper.generateFile(it, context)
        }

        then:
        noExceptionThrown()

    }
}
