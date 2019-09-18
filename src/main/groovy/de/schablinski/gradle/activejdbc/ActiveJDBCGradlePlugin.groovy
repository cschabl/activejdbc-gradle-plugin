/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.schablinski.gradle.activejdbc

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.plugins.scala.ScalaPlugin


/**
 * Gradle plugin the registers an {@link ActiveJDBCInstrumentation} task to run as part of the classes step.
 */
class ActiveJDBCGradlePlugin implements Plugin<Project> {

    static final String EXTENSION_NAME = 'activejdbc'

    @Override
    void apply(Project project) {
        ActiveJDBCExtension activeJdbcExtension = project.extensions.create(EXTENSION_NAME, ActiveJDBCExtension)
        activeJdbcExtension.toolVersion = loadToolVersion()

        if (!project.getPluginManager().hasPlugin("java")) {
            project.logger.debug "ActiveJDBCGradlePlugin.apply: java plugin has not been applied"
        }

        Configuration activeJdbcConfig = project.configurations.maybeCreate("activejdbc")
        activeJdbcConfig.setVisible(false)
        activeJdbcConfig.setDescription("The ActiveJDBC libraries to be used for this project.")
        activeJdbcConfig.withDependencies { dependencies ->
            dependencies.add(project.dependencies.create("org.javalite:activejdbc-instrumentation:"
                    + activeJdbcExtension.toolVersion))
            dependencies.add(project.dependencies.create("org.javalite:activejdbc:"
                    + activeJdbcExtension.toolVersion))
        }

        project.plugins.withType(JavaPlugin) {
            Task instrumentModels = project.tasks.create('instrumentModels', ActiveJDBCInstrumentation)
            instrumentModels.classesDir = GradleUtils.getJavaMainOutputDir(project)
            instrumentModels.group = "build"

            // use it as doLast action, because Gradle takes hashes of class files for incremental build afterwards
            project.tasks.compileJava.doLast {
                project.logger.info "ActiveJDBCGradlePlugin: tool version=" + activeJdbcExtension.toolVersion
                instrumentModels.instrument()
            }
        }

        project.plugins.withType(GroovyPlugin) {
            Task instrumentGroovyModels = project.tasks.create('instrumentGroovyModels', ActiveJDBCInstrumentation)
            instrumentGroovyModels.classesDir = project.sourceSets.main.groovy.outputDir.getPath()
            instrumentGroovyModels.group = "build"
            // use it as doLast action, because Gradle takes hashes of class files for incremental build afterwards
            project.tasks.compileGroovy.doLast {
                instrumentGroovyModels.instrument()
            }
        }

        project.plugins.withType(ScalaPlugin) {
            Task instrumentScalaModels = project.tasks.create('instrumentScalaModels', ActiveJDBCInstrumentation)
            instrumentScalaModels.classesDir = project.sourceSets.main.scala.outputDir.getPath()
            instrumentScalaModels.group = "build"
            // use it as doLast action, because Gradle takes hashes of class files for incremental build afterwards
            project.tasks.compileScala.doLast {
                instrumentScalaModels.instrument()
            }
        }
    }

    private String loadToolVersion() {
        URL url = ActiveJDBCGradlePlugin.class.getClassLoader().getResource("activejdbc-gradle-plugin.properties")
        InputStream input

        try {
            input = url.openStream()
            Properties prop = new Properties()
            prop.load(input)
            return prop.getProperty("activejdbc-version")
        }
        finally {
            input?.close()
        }
    }

}
