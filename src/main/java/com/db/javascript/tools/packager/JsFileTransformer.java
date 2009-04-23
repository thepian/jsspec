package com.db.javascript.tools.packager;

import java.io.*;

/**
 * @author Maurice Nicholson
*/
public class JsFileTransformer implements FileTransformer {

    // TODO parse the file and output errors, eg, duplicate function definitions:
    // Someclass.prototype.xxxx = function...
    public void transform(SourceFile sourceFile, InputStream is, OutputStream os, Appender appender) throws IOException {
        JavaScriptSourceFile jsSourceFile = (JavaScriptSourceFile) sourceFile;
        if (jsSourceFile.isClass) {
            String baseName = jsSourceFile.className;
            // ApplicationEventManager=(window.__classes__||(window.__classes__={}))["ApplicationEventManager"]=(function(){
            os.write((baseName + "=(window.__classes__||(window.__classes__={}))[\"" + baseName + "\"]=(function(){").getBytes());
        } else if (!jsSourceFile.getName().equals("scope.js")) {
            os.write("(function(){".getBytes());
        }

        appender.append(is, os);

        if (jsSourceFile.isClass) {
            String baseName = jsSourceFile.className;
            // ;return ApplicationEventManager;})();
            os.write((";return " + baseName + ";})();\n").getBytes());
        } else if (!jsSourceFile.getName().equals("scope.js")) {
            os.write("})();".getBytes());
        }
    }
}
