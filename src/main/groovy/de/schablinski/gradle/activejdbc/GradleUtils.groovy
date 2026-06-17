package de.schablinski.gradle.activejdbc

import org.gradle.api.Project
import org.gradle.api.logging.Logger

class GradleUtils {
    static File getJavaMainOutputDir(Project project)
    {
        def classesDir = project.sourceSets.main.java.classesDirectory.getAsFile().get()
        return classesDir
    }

    static int getGradleMajorVersion(Project project)
    {
        String gradleVersion = project.getGradle().getGradleVersion()
        Integer.valueOf(gradleVersion.substring(0, gradleVersion.indexOf(".")))
    }

    static String getSlf4LogLevel(Logger logger) {
        if (logger.isTraceEnabled()) return 'trace'
        if (logger.isDebugEnabled()) return 'debug'
        if (logger.isInfoEnabled()) return 'info'
        if (logger.isWarnEnabled()) return 'warn'
        if (logger.isErrorEnabled()) return 'error'
        return 'off'
    }
}
