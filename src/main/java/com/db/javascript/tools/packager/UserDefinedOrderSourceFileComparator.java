package com.db.javascript.tools.packager;

import java.io.*;
import java.util.Comparator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Checks the first line of two files for a given pattern indicating the file ordering
 * @author Maurice Nicholson
*/
public class UserDefinedOrderSourceFileComparator implements Comparator<SourceFile> {
    private Pattern commentPattern;

    public UserDefinedOrderSourceFileComparator(String pattern) {
        commentPattern = Pattern.compile(pattern);
    }

    public int compare(SourceFile a, SourceFile b) {
        try {
            BufferedReader ar = new BufferedReader(new InputStreamReader(a.getInputStream()));
            BufferedReader br = new BufferedReader(new InputStreamReader(b.getInputStream()));

            String aline = ar.readLine();
            String bline = br.readLine();

            // empty file ?
            if (aline == null || bline == null) {
                return 0;
            }
            Matcher am = commentPattern.matcher(aline);
            Matcher bm = commentPattern.matcher(bline);

            if (!am.matches() && !bm.matches()) {
                return 0; // todo this is platform dependent!
//                return a.getName().compareTo(b.getName());
            }

            int ai = am.matches() ? Integer.parseInt(am.group(1)) : 0;
            int bi = bm.matches() ? Integer.parseInt(bm.group(1)) : 0;
            return ai - bi;

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return 0;
        }
    }
}
