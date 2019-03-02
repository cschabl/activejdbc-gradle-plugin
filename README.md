# ActiveJDBC Gradle plugin

<div align="left">

[![Build Status](https://travis-ci.com/cschabl/activejdbc-gradle-plugin.svg?branch=master)](https://travis-ci.com/cschabl/activejdbc-gradle-plugin)

</div>

Gradle plugin for instrumenting your project's model classes for the [ActiveJDBC](http://javalite.io/activejdbc) ORM framework.

## Usage

Add the plugin in addition to Java plugin to the build.gradle file as follows:

```
plugins {
    id 'java'
    id 'de.schablinski.activejdbc-gradle-plugin' version '1.0'
}
```

This will create a task `instrumentModels` in the task group `build` which is added as `doLast` action to the `compileJava` task.

### Configuration

The version of the ActiveJDBC instrumentation tool can be configured as follows:

```
activejdbc.toolVersion = 2.2
```

The default version is 2.2.

## Other JVM languages

### Scala

To instrument ActiveJDBC model classes written in Scala, configure the plugin as shown in the build script below:

```
plugins {
    id 'scala'
    id 'de.schablinski.activejdbc-gradle-plugin'
}   

instrumentModels {
    // assuming Gradle 4.0 or higher
    classesDir = project.sourceSets.main.scala.outputDir.getPath()
    outputDir = classesDir
}

compileScala.doLast {
    instrumentModels.instrument()
}

dependencies {
    compile 'org.scala-lang:scala-library:2.12.6'
    compile 'org.javalite:activejdbc:2.2'
    
    activejdbc 'org.scala-lang:scala-library:2.12.6'
}

repositories {
    jcenter()
}
```

1. The task _instrumentModels_ is configured to search the Scala plugin's output directory for model classes
1. This task is added is added as _doLast_ action of the Scala plugin 
1. Finally the Scala library is put on the classpath of ActiveJDBC instrumentation tool (s. configuration _activejdbc_), because the model classes have a dependency on the Scala library.

## Attribution

This project started as a hard fork of the gradle-plugin module of [ActiveJDBC](http://javalite.io/activejdbc) to make it available on Gradle's plugin portal.