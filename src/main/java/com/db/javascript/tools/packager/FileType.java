package com.db.javascript.tools.packager;

import java.io.File;

/**
 * @author Maurice Nicholson
 */
public enum FileType {
    CSS, JS, JSML, ANY;

    public static FileType forExtension(String extension) {
        if (extension.startsWith(".")) {
            extension = extension.substring(1);
        }
        extension = extension.toLowerCase();
        if (extension.equals("css")) return CSS;
        if (extension.equals("js")) return JS;
        if (extension.equals("jsml")) return JSML;
        return ANY;
    }

    public static FileType forFile(File file) {
        String name = file.getName();
        String extension = name.substring(name.lastIndexOf("."));
        return forExtension(extension);
    }
}
