package nebula.plugin.ospackage.daemon

import nebula.test.ProjectSpec

class TemplateSyntaxSpec extends ProjectSpec {

    def 'each template validates'() {
        when:
        def plugin = new OspackageDaemonPlugin()
        def templates = ['initd', 'log-run', 'run']
        def helper = new TemplateHelper(projectDir, '/nebula/plugin/ospackage/daemon')
        DaemonDefinition definition = new DaemonDefinition()
        def context = plugin.toContext(plugin.getDefaultDaemonDefinition(false), definition)
        context['isRedhat'] = 'true'
        context['daemonName'] = 'foobar'
        context['command'] = 'foo'
        templates.each {
            helper.generateFile(it, context)
        }

        then:
        noExceptionThrown()

    }
    def 'template fails with a null'() {
        when:
        def helper = new TemplateHelper(projectDir, '/nebula/plugin/ospackage/daemon')

        def context = [:]
        context['isRedhat'] = true
        context['logUser'] = null
        helper.generateFile('log-run', context)

        then:
        thrown(IllegalArgumentException)

    }
}
