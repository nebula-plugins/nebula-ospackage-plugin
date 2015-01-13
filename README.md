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


nebula-ospackage-application
=======================

Takes the output of the [Application plugin](http://www.gradle.org/docs/current/userguide/application_plugin.html) and  
packages it into a system package like a RPM or DEB.  It uses the os-package plugin to accomplish this.

## Applying the Plugin

To include, add the following to your build.gradle

    buildscript {
        repositories { jcenter() }

        dependencies {
            classpath 'com.netflix.nebula:nebula-ospackage-plugin:1.12.+'
        }
    }

    apply plugin: 'nebula-ospackage-application'

## Usage

There is a single property available on a _ospackage-application_ extension which controls the prefix.

    ospackage_application {
        prefix = '/usr/local'
    }
    
Otherwise the prefix defaults to _/opt_, the actual installation will be to into _/opt/$applicationName_, where 
_applicationName_ comes from the Application plugin. As usual to the Application plugin, the user has to provide a
_mainClassName_.

Once configured with a system packaging task, the project will produce a DEB or RPM with the context of the application:

    gradlew buildDeb


nebula-ospackage-application-daemon
=======================

Combine the above two plugins to create a self-running daemon out of a [Application plugin](http://www.gradle.org/docs/current/userguide/application_plugin.html) 
project.

## Applying the Plugin

To include, add the following to your build.gradle

    buildscript {
        repositories { jcenter() }

        dependencies {
            classpath 'com.netflix.nebula:nebula-ospackage-plugin:1.12.+'
        }
    }

    apply plugin: 'nebula-ospackage-application-daemon'

## Usage

Since this plugin is making the daemon for you, it could be difficult to access the standard daemon configuration. To
alleviate this, there's a extension provided for configuring the application daemon, called _applicationdaemon_:

    applicationdaemon {
        user = "nobody"
    }

Once configured, the project will produce a DEB and a RPM with the context of the application and the relevant daemon scripts:

    gradlew buildDeb buildRpm
