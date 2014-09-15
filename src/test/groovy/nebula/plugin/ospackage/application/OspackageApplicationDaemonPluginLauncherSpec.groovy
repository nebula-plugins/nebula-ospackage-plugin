package nebula.plugin.ospackage.application

import nebula.test.IntegrationSpec

class OspackageApplicationDaemonPluginLauncherSpec extends IntegrationSpec {

    def 'daemon script from application'() {
        writeHelloWorld('nebula.test')
        buildFile << """
            ${applyPlugin(OspackageApplicationDaemonPlugin)}
            mainClassName = 'nebula.test.HelloWorld'
        """.stripIndent()

        when:
        runTasksSuccessfully('buildDeb')

        then:
        def archivePath = file("build/distributions/${moduleName}_unspecified_all.deb")
        def scan = new com.netflix.gradle.plugins.deb.Scanner(archivePath)

        ["/service/${moduleName}/run", "/etc/init.d/${moduleName}", "/opt/${moduleName}/lib/${moduleName}.jar", "/opt/${moduleName}/bin/${moduleName}"].each {
            scan.getEntry(".${it}").isFile()
        }

        scan.controlContents.containsKey('./postinst')
        scan.controlContents['./postinst'].contains("/usr/sbin/update-rc.d ${moduleName} start 85 2 3 4 5 . stop 15 0 1 6 .")

    }
}
