package nebula.plugin.ospackage.application

import nebula.test.IntegrationSpec

class OspackageApplicationPluginLauncherSpec extends IntegrationSpec {
    def 'application shows up in deb'() {
        writeHelloWorld('nebula.test')
        buildFile << """
            ${applyPlugin(OspackageApplicationPlugin)}
            mainClassName = 'nebula.test.HelloWorld'
        """.stripIndent()

        when:
        runTasksSuccessfully('buildDeb')

        then:
        def archivePath = file("build/distributions/${moduleName}_unspecified_all.deb")
        def scan = new com.netflix.gradle.plugins.deb.Scanner(archivePath)

        0755 == scan.getEntry("./opt/${moduleName}/bin/${moduleName}").mode

        ["/opt/${moduleName}/lib/${moduleName}.jar", "/opt/${moduleName}/bin/${moduleName}.bat"].each {
            scan.getEntry(".${it}").isFile()
        }

        scan.controlContents.containsKey('./postinst')
        scan.controlContents['./postinst'] =~ /configure\)\s+;;/ // No commands

    }

    def 'can customize destination'() {
        writeHelloWorld('nebula.test')
        buildFile << """
            ${applyPlugin(OspackageApplicationPlugin)}
            mainClassName = 'nebula.test.HelloWorld'
            ospackage_application {
                prefix = '/usr/local'
            }
            applicationName = 'myapp'
        """.stripIndent()

        when:
        runTasksSuccessfully('buildDeb')

        then:
        def archivePath = file("build/distributions/${moduleName}_unspecified_all.deb")
        def scan = new com.netflix.gradle.plugins.deb.Scanner(archivePath)

        0755 == scan.getEntry("./usr/local/myapp/bin/myapp").mode

        ["/usr/local/myapp/lib/${moduleName}.jar", "/usr/local/myapp/bin/myapp.bat"].each {
            scan.getEntry(".${it}").isFile()
        }

    }
}
