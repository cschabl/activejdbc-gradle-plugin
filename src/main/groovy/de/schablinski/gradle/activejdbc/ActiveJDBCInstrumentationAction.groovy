package de.schablinski.gradle.activejdbc

import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.logging.Logger;
import org.gradle.api.model.ObjectFactory
import org.gradle.process.ExecOperations
import org.gradle.process.JavaExecSpec
import org.gradle.process.ProcessExecutionException
import org.javalite.instrumentation.Main

import javax.inject.Inject

/**
 * Configuration cache compatible Action for performing ActiveJDBC instrumentation.
 */
abstract class ActiveJDBCInstrumentationAction implements Action<Task> {

    private final String classesDir
    private final ConfigurableFileCollection activeJdbcDependencies

    @Inject
    abstract ExecOperations getExecOperations()
    @Inject
    abstract ObjectFactory getObjectFactory()

    @Inject
    ActiveJDBCInstrumentationAction(String classesDir, ConfigurableFileCollection activeJdbcDependencies) {
        this.classesDir = classesDir
        this.activeJdbcDependencies = activeJdbcDependencies
    }

    @Override
    void execute(Task task) {
        String slf4JLogLevel = getSlf4LogLevel(task.getLogger())
        StdOutCaptor stdOutCaptor = StdOutCaptor.newInstance()
        StdOutCaptor stdErrCaptor = StdOutCaptor.newInstance()
        FileCollection instrumentationClasspath = activeJdbcDependencies + getObjectFactory().fileCollection().from(classesDir)
        def instrumentationOutputDir = classesDir

        task.getLogger().info "Instrumenting ActiveJDBC model classes under $classesDir"

        try {
            execOperations.javaexec { JavaExecSpec jes ->
                task.getLogger().info "Running ActiveJDBC instrumentation instance with environment: ${jes.environment}"
                jes.classpath = instrumentationClasspath
                jes.mainClass.set(Main.canonicalName)
                jes.systemProperties = ['outputDirectory'                       : instrumentationOutputDir,
                                        'org.slf4j.simpleLogger.defaultLogLevel': slf4JLogLevel]

                jes.standardOutput = stdOutCaptor
                jes.errorOutput = stdErrCaptor
            }
        }
        catch (ProcessExecutionException ex) {
            throw ex
        }
        finally {
            if (stdOutCaptor.getContent()) {
                task.getLogger().info stdOutCaptor.getContent()
            }
            if (stdErrCaptor.getContent()) {
                task.getLogger().error stdErrCaptor.getContent()
            }
        }
    }

    private String getSlf4LogLevel(Logger logger) {
        if (logger.isTraceEnabled()) return 'trace'
        if (logger.isDebugEnabled()) return 'debug'
        if (logger.isInfoEnabled()) return 'info'
        if (logger.isWarnEnabled()) return 'warn'
        if (logger.isErrorEnabled()) return 'error'
        return 'off'
    }

    private static class StdOutCaptor extends PrintStream
    {

        private ByteArrayOutputStream capturingOutputStream
        private String content

        static StdOutCaptor newInstance() {
            def out = new ByteArrayOutputStream()
            def stdOutCaptor = new StdOutCaptor(out)

            // Can't access protected field 'out' of PrintStream's super class in Java 17, anymore
            stdOutCaptor.capturingOutputStream = out
            return stdOutCaptor
        }

        StdOutCaptor(ByteArrayOutputStream out) {
            super(out, true)
        }

        String getContent() {
            return content
        }

        @Override
        void close() {
            content = capturingOutputStream.toString()
            super.close()
        }
    }
}
