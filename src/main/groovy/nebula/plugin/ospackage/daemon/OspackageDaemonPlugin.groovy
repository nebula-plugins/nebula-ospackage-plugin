/*
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nebula.plugin.ospackage.daemon

import com.netflix.gradle.plugins.deb.Deb
import com.netflix.gradle.plugins.packaging.SystemPackagingBasePlugin
import com.netflix.gradle.plugins.packaging.SystemPackagingTask
import com.netflix.gradle.plugins.rpm.Rpm
import org.gradle.api.DomainObjectCollection
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.DefaultDomainObjectCollection

class OspackageDaemonPlugin implements Plugin<Project> {
    Project project
    DaemonExtension extension

    Map<String,Object> toContext(DaemonDefinition definitionDefaults, DaemonDefinition definition) {
        return [
                daemonName: definition.daemonName ?: definitionDefaults.daemonName,
                command: definition.command,
                user: definition.user ?: definitionDefaults.user,
                logCommand: definition.logCommand ?: definitionDefaults.logCommand,
                runLevels: definition.runLevels ?: definitionDefaults.runLevels,
                autoStart: definition.autoStart != null? definition.autoStart : definitionDefaults.autoStart,
                startSequence: definition.startSequence ?: definitionDefaults.startSequence,
                stopSequence: definition.stopSequence ?: definitionDefaults.stopSequence
        ]
    }

    @Override
    void apply(Project project) {
        this.project = project
        project.plugins.apply(SystemPackagingBasePlugin)

        // TODO Use NamedContainerProperOrder so that we can create tasks for each definition as they appear
        DomainObjectCollection<DaemonDefinition> daemonsList = new DefaultDomainObjectCollection<>(DaemonDefinition, []) // project.container(DaemonDefinition)
        //List<DaemonDefinition> daemonsList = new LinkedList<DaemonDefinition>()
        extension = project.extensions.create('daemons', DaemonExtension, daemonsList)

        // Add daemon to project
        project.ext.daemon = { Closure closure ->
            extension.daemon(closure)
        }

        daemonsList.all { DaemonDefinition definition ->
            // Check existing name
            def sameName = daemonsList.any { !it.is(definition) && it.daemonName == definition.daemonName }
            if (sameName) {
                if (definition.daemonName) {
                    throw new IllegalArgumentException("A daemon with the name ${definition.daemonName} is already defined")
                } else {
                    throw new IllegalArgumentException("A daemon with no name, and hence the default, is already defined")
                }
            }

            project.tasks.withType(SystemPackagingTask) { SystemPackagingTask task ->
                def isRedhat = task instanceof Rpm
                DaemonDefinition defaults = new DaemonDefinition(null, null, 'root', 'multilog t ./main', isRedhat?[3,4,5]:[2,3,4,5], Boolean.TRUE, 85, 15)
                Map<String, String> context = toContext(defaults, definition)

                context.daemonName = definition.daemonName ?: task.getPackageName()
                if (!context.daemonName) {
                    throw new IllegalArgumentException("Unable to find a name on definition ${definition}")
                }
                String cleanedName = context.daemonName.replaceAll("\\W", "").capitalize()

                context.isRedhat = isRedhat

                def outputDir = new File(project.buildDir, "daemon/${cleanedName}/${task.name}")

                def mapping = [
                        'log-run': "/service/${context.daemonName}/log/run",
                        'run': "/service/${context.daemonName}/run",
                        'down': "/service/${context.daemonName}/down",
                        'initd': isRedhat?"/etc/rc.d/init.d/${context.daemonName}":"/etc/init.d/${context.daemonName}"
                ]

                def templateTask = project.tasks.create("${task.name}${cleanedName}Daemon", DaemonTemplateTask)
                templateTask.destDir = outputDir
                templateTask.context = context
                templateTask.templates = mapping.keySet()

                task.dependsOn(templateTask)
                mapping.each { String templateName, String destPath ->
                    File rendered = new File(outputDir, templateName) // To be created by task, ok that it's not around yet

                    // Gradle CopySpec can't set the name of a file on the fly, we need to do a rename.
                    def slashIdx = destPath.lastIndexOf('/')
                    def destDir = destPath.substring(0,slashIdx)
                    def destFile = destPath.substring(slashIdx+1)
                    task.from(rendered) {
                        into(destDir)
                        rename('.*', destFile)
                        fileMode 0555 // Since source files don't have the correct permissions
                        user context.user
                    }
                }


                def installCmd = isRedhat?
                        "/sbin/chkconfig ${context.daemonName} on":
                        "/usr/sbin/update-rc.d ${context.daemonName} start ${context.startSequence} ${context.runLevels.join(' ')} . stop ${context.stopSequence} ${([0,1,2,3,4,5,6]-context.runLevels).join(' ')} ."
                println "***********************\n$context"
                if (context.autoStart) {

                    task.postInstall(installCmd)
                }
            }
        }
    }
}


