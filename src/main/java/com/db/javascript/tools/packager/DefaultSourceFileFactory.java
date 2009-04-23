package com.db.javascript.tools.packager;

import java.io.File;
import java.io.IOException;

/**
 * @author Maurice Nicholson
 */
public class DefaultSourceFileFactory implements SourceFileFactory {

    public SourceFile createSourceFile(File sourceDir, File file) throws IOException {
        if (file.getName().endsWith(".js")) {
            return new JavaScriptSourceFile(sourceDir, file);
        }
        return new SourceFile(sourceDir, file);
    }
}
