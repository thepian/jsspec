package com.db.javascript.tools.packager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Maurice Nicholson
 */
public interface Appender {

    void append(InputStream is, OutputStream os) throws IOException;
}
