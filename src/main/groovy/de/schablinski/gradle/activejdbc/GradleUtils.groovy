package de.schablinski.gradle.activejdbc

import org.gradle.api.Project

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
}
