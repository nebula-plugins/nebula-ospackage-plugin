package nebula.plugin.ospackage.daemon

import groovy.transform.Canonical
import org.gradle.api.DomainObjectCollection
import org.gradle.util.ConfigureUtil

@Canonical
class DaemonExtension {
    final DomainObjectCollection<DaemonDefinition> daemons

    // TBD Add defaults, like user name for all daemons

    def daemon(Closure configure) {
        def definition = new DaemonDefinition()
        ConfigureUtil.configure(configure, definition)
        daemons.add(definition)
    }
}
