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

## Attribution

This project started as a hard fork of the gradle-plugin module of [ActiveJDBC](http://javalite.io/activejdbc) to make it available on Gradle's plugin portal.