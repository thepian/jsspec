package com.db.javascript.tools.packager;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * @author Maurice Nicholson
 */
public class JsPackager extends FilePackager {

    public JsPackager() {
        setExtensions(new String[] {".js", ".jsml"});
//        setFileComparator(new UserDefinedOrderSourceFileComparator("//#initscript (-*\\d+)"));
        setFileTransformers(new HashMap<FileType, FileTransformer>() {{
            put(FileType.JS, new JsFileTransformer());
            put(FileType.JSML, new JsmlFileTransformer());
        }});
        setStartDestFile("(function(){\n\n");
        setEndDestFile("})();\n");
    }

    public static void main(String[] args) throws Exception {
        /*
        for testing

        args = new String[] {
//            "-sourcedir=C:\\projects\\rps\\crs\\trunk\\js-client\\src\\js\\ui.js",
            "-sourcedir=C:\\projects\\rps\\rpw\\trunk\\src\\js",
            "-destdir=C:\\projects\\rps\\rpw\\trunk\\web\\js",
            "-force=true",
            "-output=summary"
        };
//         */

        boolean force = CommandLineUtils.getBooleanArg(args, "force", true);
        String sourceDir = CommandLineUtils.getStringArg(args, "sourcedir");
        String destDir = CommandLineUtils.getStringArg(args, "destdir");

        JsPackager jsPackager = new JsPackager();
        jsPackager.setForce(force);
        jsPackager.setSourceDir(new File(sourceDir));

        jsPackager.writePackagesTo(new File(destDir));
    }

    // quick hacks for JS specific stuff
    protected void beginFileType(FileType fileType, SourceFile sourceFile, OutputStream os) throws IOException {
        if (fileType.equals(FileType.JSML)) {
            os.write("(function(){\nvar f='firstChild',n='nextSibling',c='childNodes',p='parentNode';var E = Ext.get, EU = Ext.unbind;\n".getBytes());
        }
    }

    // quick hacks for JS specific stuff
    protected void endFileType(FileType fileType, SourceFile sourceFile, OutputStream os) throws IOException {
        if (fileType.equals(FileType.JSML)) {
            os.write("\n\n})();".getBytes());
        }
    }

}
