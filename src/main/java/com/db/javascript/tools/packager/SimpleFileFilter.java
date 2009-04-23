package com.db.javascript.tools.packager;

import java.io.FileFilter;
import java.io.File;
import java.util.Set;
import java.util.HashSet;

/**
 * Matches files or directories according to given extensions, with hard-coded exclusions for
 * typical unwanted dirs
 * @author Maurice Nicholson
 */
public class SimpleFileFilter implements FileFilter {
    private Set<String> excluded = new HashSet<String>() {{
        add(".");
        add("..");
        add(".svn");
    }};
    private String[] includedExtensions;
    private boolean files;

    public SimpleFileFilter(String[] extensions, boolean files) {
        includedExtensions = extensions;
        this.files = files;
    }

    public SimpleFileFilter(boolean files) {
        this.files = files;
    }

    public boolean accept(File file) {
        if (file.isFile() != files) {
            return false;
        }
        String name = file.getName();
        if (excluded.contains(name)) {
            return false;
        }
        if (includedExtensions == null) {
            return true;
        }
        for (int i = 0, len = includedExtensions.length; i < len; i++) {
            if (name.endsWith(includedExtensions[i])) {
                return true;
            }
        }
        return false;
    }
}
