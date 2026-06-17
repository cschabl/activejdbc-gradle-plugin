# ActiveJDBC Gradle plugin

<div>

![Build Status](https://github.com/cschabl/activejdbc-gradle-plugin/actions/workflows/gradle.yml/badge.svg)

</div>

A Gradle plugin that instruments your project's model classes for the [ActiveJDBC](http://javalite.io/activejdbc) ORM framework.

As of version 3.0, of the plugin requires Gradle 9.
As of version 3.1, the plugin supports Gradle's configuration cache.
Use version 2.0 for Gradle 7.x - 8.x.

The plugin has been tested with ActiveJDBC versions 2.0 through 2.6-j8.

## Usage

Add the plugin in addition to Java plugin to the build.gradle file as follows:

```
plugins {
    id 'java'
    id 'de.schablinski.activejdbc-gradle-plugin' version '3.0.0'
}
```

This adds a `doLast` action to the `compileJava` task that instruments the ActiveJDBC model classes.

### Configuration

The plugin determines the version of the ActiveJDBC instrumentation tool from the version of `activejdbc.jar` used as a `compile` or `implementation` dependency in your build script.
For backward compatibility, or as a last resort, you can configure the instrumentation tool version manually:

```
activejdbc.toolVersion = '2.2'
```

If your project's ActiveJDBC model classes have compile-time dependencies on 3rd-party libraries, add them to the _activejdbc_ configuration (see example below).

```
dependencies {
    ...
    activejdbc 'org.scala-lang:scala-library:2.13.18'
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
    implementation 'org.scala-lang:scala-library:2.13.18'
    implementation 'org.javalite:activejdbc:2.3'
    
    activejdbc 'org.scala-lang:scala-library:2.13.18'
}

repositories {
    mavenCentral()
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
    implementation 'org.codehaus.groovy:groovy:3.0.25'
    implementation 'org.javalite:activejdbc:2.3'
    
    // The Groovy library is only required on the instrumentation classpath, if the model classes depend on the GDK
    // activejdbc 'org.codehaus.groovy:groovy:3.0.25'
}

repositories {
    mavenCentral()
}
```

## Attribution.

This project started as a hard fork of the gradle-plugin module of [ActiveJDBC](http://javalite.io/activejdbc) to make it available on Gradle's plugin portal.