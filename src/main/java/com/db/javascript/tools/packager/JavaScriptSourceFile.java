package com.db.javascript.tools.packager;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Maurice Nicholson
 */
public class JavaScriptSourceFile extends SourceFile {
    private static Set<String> JS_CLASSES = new HashSet<String>(
        Arrays.asList(new String[] {"Array", "Date", "String", "Function"})
    );

    public String className;
    public boolean isClass = false;

    public JavaScriptSourceFile(File sourceDir, File file) throws IOException {
        super(sourceDir, file, FileType.JS);
        className = basename();

        // todo do a proper job
        if (Character.isUpperCase(file.getName().charAt(0)) && !JS_CLASSES.contains(className) && !sourceDir.getName().startsWith("ext")) {
            isClass = true;
        }
    }
}
