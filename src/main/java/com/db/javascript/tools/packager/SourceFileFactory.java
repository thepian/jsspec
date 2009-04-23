package com.db.javascript.tools.packager;

import java.io.File;
import java.io.IOException;

/**
 * @author Maurice Nicholson
 */
public interface SourceFileFactory {

    SourceFile createSourceFile(File sourceDir, File file) throws IOException;
}
