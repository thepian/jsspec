package com.db.javascript.tools.packager;

import java.io.IOException;
import java.io.OutputStream;

/**
 * "Decorates" the output of a file with before and after content
 * @author Maurice Nicholson
 */
public interface FileOutputDecorator {

    void beforeFile(SourceFile sourceFile, OutputStream os) throws IOException;
    void afterFile(SourceFile sourceFile, OutputStream os) throws IOException;
}
