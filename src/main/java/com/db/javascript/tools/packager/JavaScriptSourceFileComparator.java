package com.db.javascript.tools.packager;

import java.util.Comparator;

/**
 * @author Maurice Nicholson
 */
public class JavaScriptSourceFileComparator implements Comparator<SourceFile> {
    private Comparator<SourceFile> userDefinedOrderComparator = new UserDefinedOrderSourceFileComparator("//#initscript (-*\\d+)");

    public int compare(SourceFile a, SourceFile b) {
//        if (a instanceof JavaScriptSourceFile && ((JavaScriptSourceFile) a).isClass) {
        if (a.getName().equals("scope.js")) {
            return -1;
        }
//        if (b instanceof JavaScriptSourceFile && ((JavaScriptSourceFile) b).isClass) {
        if (b.getName().equals("scope.js")) {
            return 1;
        }

        if (a.getFileType().equals(FileType.JSML)) {
            return -1;
        }
        if (b.getFileType().equals(FileType.JSML)) {
            return 1;
        }

        return userDefinedOrderComparator.compare(a, b);
    }
}
