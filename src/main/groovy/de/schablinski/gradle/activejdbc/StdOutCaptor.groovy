package de.schablinski.gradle.activejdbc

class StdOutCaptor extends PrintStream
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
