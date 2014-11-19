package nebula.plugin.ospackage.daemon

import org.gradle.api.internal.DefaultDomainObjectCollection
import spock.lang.Specification

class DaemonExtensionSpec extends Specification {
    def definitionList = new DefaultDomainObjectCollection<DaemonDefinition>(DaemonDefinition, [])
    DaemonExtension extension = new DaemonExtension( new DefaultDomainObjectCollection<DaemonDefinition>(DaemonDefinition, definitionList) )

    def 'configures on add'() {
        when:
        extension.daemon {
            daemonName = 'foobar'
            command = 'exit 0'
            user = 'builds'
            logUser = 'root'
            logDir = '/tmp'
            logCommand = 'multipass'
            runLevels = [1,2]
            autoStart = false
            startSequence = 85
            stopSequence = 15
        }

        then:
        !definitionList.isEmpty()
        def definition = definitionList.iterator().next()
        definition.daemonName == 'foobar'
        definition.command == 'exit 0'
        definition.user == 'builds'
        definition.logUser == 'root'
        definition.logDir == '/tmp'
        definition.logCommand == 'multipass'
        definition.runLevels == [1,2]
        !definition.autoStart
        definition.startSequence == 85
        definition.stopSequence == 15
    }
}
