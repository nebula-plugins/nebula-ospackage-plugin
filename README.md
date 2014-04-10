nebula-ospackage-plugin
==============

Opinionated plugins that wrap the gradle-ospackage-plugin

## Usage

### Applying the Plugin

To include, add the following to your build.gradle

    buildscript {
      repositories { jcenter() }

      dependencies {
        classpath 'com.netflix.nebula:nebula-ospackage-plugin:1.9.+'
      }
    }

    apply plugin: 'nebula-ospackage-daemon'

### Tasks Provided

`<your tasks>`

### Extensions Provided

`<your extensions>`
