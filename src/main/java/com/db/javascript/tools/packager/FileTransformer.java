package com.db.javascript.tools.packager;

import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Maurice Nicholson
 */
public interface FileTransformer {

    void transform(SourceFile sourceFile, InputStream is, OutputStream os, Appender appender) throws IOException;

}
