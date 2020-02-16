package l2.commons.compiler;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;
import javax.tools.SimpleJavaFileObject;
import javax.tools.JavaFileObject.Kind;

public class MemoryByteCode extends SimpleJavaFileObject {
    private ByteArrayOutputStream oStream;
    private final String className;

    public MemoryByteCode(String className, URI uri) {
        super(uri, Kind.CLASS);
        this.className = className;
    }

    public OutputStream openOutputStream() {
        this.oStream = new ByteArrayOutputStream();
        return this.oStream;
    }

    public byte[] getBytes() {
        return this.oStream.toByteArray();
    }

    public String getName() {
        return this.className;
    }
}
