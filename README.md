nebula-ospackage-plugin
==============

Opinionated plugins that wraps the gradle-ospackage-plugin.

nebula-ospackage-daemon
=======================

Builds the proper scripts for running a daemon (with daemontools) on CentOS and Ubuntu. 

## Applying the Plugin

To include, add the following to your build.gradle

    buildscript {
        repositories { jcenter() }

        dependencies {
            classpath 'com.netflix.nebula:nebula-ospackage-plugin:1.12.+'
        }
    }

    apply plugin: 'nebula-ospackage-daemon'

## Usage

An extension is provided to configure possible daemons.

The simplest usage is a single daemon:

    daemon {
        daemonName = "foobar" // default = packageName
        command = "sleep infinity" // required
    }
            
Technically only the command field is needed, and the daemon's name will default to the package being built. This is the
complete list of fields available:

* daemonName:String - Name used to start and stop the dameon
* command:String - Command to ensure is running
* user:String - User to run as, defaults to "root"
* logCommand:String - Log command to run, defaults to "multilog t ./main"
* runLevels:List<Integer> - Run levels for daemon, rpm defaults to [3,4,5], deb defaults to [2,3,4,5]
* autoStart:Boolean - Should auto start, defaults to true
* startSequence:Integer - Boot ordering, default is 85
* stopSequence:Integer - Shutdown ordering, default is 15

Multiple daemons can be defined using the _daemons_ extension, e.g.

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
            daemonName = "fooqux"
            command = "sleep infinity"
            user = "nobody"
            logCommand = "cronolog /logs/foobar/foobar.log"
            runLevels = [3,4]
            autoStart = false
            startSequence = 99
            stopSequence = 1
        }
    }

### Tasks Provided

A task used for templating will be created for each combination of System Packaging task and daemon. For example, if there's
a daemon called Foobar, a Rpm task, and a Deb task, there would also be a buildDebFoobarDaemon task and a buildRpmFoobarDaemon 
task.
