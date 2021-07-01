# ActiveJDBC Gradle plugin

<div align="left">

[![Build Status](https://travis-ci.com/cschabl/activejdbc-gradle-plugin.svg?branch=master)](https://travis-ci.com/cschabl/activejdbc-gradle-plugin)

</div>

Gradle plugin for instrumenting your project's model classes for the [ActiveJDBC](http://javalite.io/activejdbc) ORM framework.

Version 2.0 of the plugin requires Gradle 7 or higher.
Use Version 1.5.2 for older Gradle versions.

The plugin has been tested with ActiveJDBC 2.0 to 2.5-j8.

## Usage

Add the plugin in addition to Java plugin to the build.gradle file as follows:

```
plugins {
    id 'java'
    id 'de.schablinski.activejdbc-gradle-plugin' version '2.0'
}
```

This will create a task `instrumentModels` in the task group `build` which is added as `doLast` action to the `compileJava` task.

### Configuration

The version of the ActiveJDBC instrumentation tool is determined by the plugin from the version of the activejdbc.jar used as compile or implementation dependency in the build script.
For backward compatibility or as a last resort, the instrumentation tool version can be configured as follows:

```
activejdbc.toolVersion = '2.2'
```

If the ActiveJDBC model classes of the project have compile-time dependencies on 3rd-party libraries, they must be added to the _activejdbc_ configuration (see example below).

```
dependencies {
    ...
    activejdbc 'org.scala-lang:scala-library:2.12.6'
    ...
}
```

## Other JVM languages

### Scala

To instrument ActiveJDBC model classes written in Scala, configure the plugin as shown in the build script below (Gradle >= 4.0):

```
plugins {
    id 'scala'
    id 'de.schablinski.activejdbc-gradle-plugin'
}   

dependencies {
    implementation 'org.scala-lang:scala-library:2.12.6'
    implementation 'org.javalite:activejdbc:2.3'
    
    activejdbc 'org.scala-lang:scala-library:2.12.6'
}

repositories {
    jcenter()
}
```

The Scala library must be added on the classpath of ActiveJDBC instrumentation tool (s. configuration _activejdbc_), because the model classes have a dependency on the Scala library.

### Groovy

To instrument ActiveJDBC model classes written in Groovy, configure the plugin as shown in the build script below (Gradle >= 4.0):

```
plugins {
    id 'groovy'
    id 'de.schablinski.activejdbc-gradle-plugin'
}   

dependencies {
    implementation 'org.codehaus.groovy:groovy:2.4.15'
    implementation 'org.javalite:activejdbc:2.3'
    
    // The Groovy library is only required on the instrumentation classpath, if the model classes depend on the GDK
    // activejdbc 'org.codehaus.groovy:groovy:2.4.15'
}

repositories {
    jcenter()
}
```

### JVM languages not yet supported supported by the plugin

The ActiveJDBC model instrumentation is executed by the task [ActiveJDBCInstrumentation](./src/main/groovy/de/schablinski/gradle/activejdbc/ActiveJDBCInstrumentation.groovy).
An instance of this task is added as doLast-action to the task _compileJava_ by the ActiveJDBC Gradle plugin, e.g.

This task can be used to instrument ActiveJDBC model classes written in JVM languages not yet supported by this plugin.
First, a task of type _ActiveJDBCInstrumentation_ must be created with its property _classesDir_ set to the output directory of the language's compiler.
Then, this task must be added as doLast-action to the language's compile-task. 

The following build script snippet demonstrates these steps for Kotlin. 
A complete [build script](https://github.com/cschabl/active-jdbc-gradle-kotlin-example/blob/master/build.gradle) can be found in the Kotlin example project [active-jdbc-gradle-kotlin-example](https://github.com/cschabl/active-jdbc-gradle-kotlin-example) at Github.

```groovy
task instrumentKotlinModels(type: ActiveJDBCInstrumentation) {
    group = 'build'
    classesDir = "${project.buildDir}/classes/kotlin/main"
}

tasks['compileKotlin'].doLast {
    instrumentKotlinModels.instrument()
}
```

## Attribution.

This project started as a hard fork of the gradle-plugin module of [ActiveJDBC](http://javalite.io/activejdbc) to make it available on Gradle's plugin portal.