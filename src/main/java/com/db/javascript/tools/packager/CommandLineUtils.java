package com.db.javascript.tools.packager;

/**
 * @author Maurice Nicholson
 */
public abstract class CommandLineUtils {

    public static boolean getBooleanArg(String[] args, String name, boolean defaultValue) {
        String value = getStringArg(args, name, null);
        return value == null ? defaultValue : Boolean.valueOf(value);
    }

    public static String getStringArg(String[] args, String name, String defaultValue) {
        for (String arg : args) {
            String prefix = "-" + name + "=";
            if (arg.startsWith(prefix)) {
                return arg.substring(prefix.length());
            }
        }
        return defaultValue;
    }

    public static String getStringArg(String[] args, String name) {
        String value = getStringArg(args, name, null);
        if (value == null) {
            throw new IllegalStateException("Expected command line argument like \"-" + name + "=somevalue\" but didn't find it");
        }
        return value;
    }
}
