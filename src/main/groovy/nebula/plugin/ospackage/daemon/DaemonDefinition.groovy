package nebula.plugin.ospackage.daemon

import groovy.transform.Canonical
import org.gradle.api.Named

/**
 * Template variables. Fields start off empty an only at
 */
@Canonical
class DaemonDefinition {

    String daemonName // defaults to packageName
    String command // Required
    String user // defaults to "root"
    String logCommand // defaults to "multilog t ./main"
    List<Integer> runLevels = new LinkedList<>() // rpm default == [3,4,5], deb default = [2,3,4,5]
    Boolean autoStart // default true
    Integer startSequence // default 85
    Integer stopSequence // default 15

}
