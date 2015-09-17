/*
 * Copyright 2014-2015 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * <http://www.apache.org/licenses/LICENSE-2.0>
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nebula.plugin.ospackage.daemon

import com.netflix.gradle.plugins.packaging.SystemPackagingPlugin
import nebula.test.IntegrationSpec

class OspackageDaemonPluginLauncherSpec extends IntegrationSpec {

    def 'single daemon'() {
        buildFile << """
            ${applyPlugin(OspackageDaemonPlugin)}
            ${applyPlugin(SystemPackagingPlugin)}
            daemon {
                daemonName = 'foobar'
                command = 'sleep infinity'
            }
            """.stripIndent()

        when:
        runTasksSuccessfully('buildDeb', 'buildRpm')

        then:
        new File(projectDir, "build/distributions/${moduleName}-unspecified.noarch.rpm").exists()

        def rpmTemplateDir = new File(projectDir, 'build/daemon/Foobar/buildRpm/')
        def rpmInitFile = new File(rpmTemplateDir, 'initd')
        rpmInitFile.exists()
        rpmInitFile.text.contains('''
            # chkconfig: 345 85 15
            # description: Control Script for foobar

            . /etc/rc.d/init.d/functions'''.stripIndent())

        new File(projectDir, "build/distributions/${moduleName}_unspecified_all.deb").exists()

        def debTemplateDir = new File(projectDir, 'build/daemon/Foobar/buildDeb/')
        def debInitFile = new File(debTemplateDir, 'initd')
        debInitFile.exists()
        debInitFile.text.contains('''
            ### BEGIN INIT INFO
            # Provides:          foobar
            # Default-Start:     2 3 4 5
            # Default-Stop:      0 1 6
            # Required-Start:
            # Required-Stop:
            # Description: Control Script for foobar
            ### END INIT INFO'''.stripIndent())
    }

    def 'all the bells an whistles'() {
        buildFile << """
            ${applyPlugin(OspackageDaemonPlugin)}
            ${applyPlugin(SystemPackagingPlugin)}
            daemon {
              daemonName = "foobaz" // default = packageName
              command = "sleep infinity" // required
            }
            daemons {
                daemon {
                  // daemonName default = packageName
                  command = 'exit 0'
                }
                daemon {
                  daemonName = "foobar"
                  command = "sleep infinity" // required
                }
                daemon {
                  daemonName = "fooqux" // default = packageName
                  command = "sleep infinity" // required
                  user = "nobody" // default = "root"
                  logUser = "root" // default = "nobody"
                  logDir = "/tmp" // default = "./main"
                  logCommand = "cronolog /logs/foobar/foobar.log" // default = "multilog t ./main"
                  runLevels = [3,4] // rpm default == [3,4,5], deb default = [2,3,4,5]
                  autoStart = false // default = true
                  startSequence = 99 // default 85
                  stopSequence = 1 // default 15
                }
            }""".stripIndent()

        when:
        runTasksSuccessfully('buildDeb', 'buildRpm')

        then:
        // DEB
        def archivePath = file("build/distributions/${moduleName}_unspecified_all.deb")
        def scan = new com.netflix.gradle.plugins.deb.Scanner(archivePath)

        0555 == scan.getEntry('./service/foobar/run').mode
        0555 == scan.getEntry('./service/foobar/log/run').mode
        0555 == scan.getEntry('./etc/init.d/foobar').mode

        ['/service/foobaz/run', '/service/foobaz/log/run', '/etc/init.d/foobaz'].each {
            scan.getEntry(".${it}").isFile()
        }

        scan.controlContents.containsKey('./postinst')
        scan.controlContents['./postinst'].contains("/usr/sbin/update-rc.d foobaz start 85 2 3 4 5 . stop 15 0 1 6 .")
        scan.controlContents['./postinst'].contains("/usr/sbin/update-rc.d all-the-bells-an-whistles start 85 2 3 4 5 . stop 15 0 1 6 .")
        !scan.controlContents['./postinst'].contains("/usr/sbin/update-rc.d fooqux") // no autostart

        // RPM
        def rpmScan = com.netflix.gradle.plugins.rpm.Scanner.scan(file("build/distributions/${moduleName}-unspecified.noarch.rpm"))
        def files = rpmScan.files.collect { it.name }
        files.any { it == './etc/rc.d/init.d/foobar' }
        files.any { it == './service/foobar/run' }
        files.any { it == './etc/rc.d/init.d/foobaz' }
    }

}
