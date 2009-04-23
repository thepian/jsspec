package com.db.javascript.tools.packager;

import java.io.*;
import java.util.*;

/**
 * @author Maurice Nicholson
 */
public class FilePackager implements Appender {
    private static final byte[] NEWLINE_BYTES = "\n".getBytes();

    public static void main(String[] args) throws Exception {
        /*
        args = new String[] {
//            "-sourcedir=C:\\projects\\rps\\crs\\trunk\\js-client\\src\\css\\style.css",
            "-sourcedir=C:\\projects\\rps\\crs\\trunk\\js-client\\src\\css",
            "-destdir=C:\\projects\\rps\\crs\\trunk\\creditrisk\\web\\css",
            "-extensions=css",
            "-force=true"
        };
*/
        boolean force = CommandLineUtils.getBooleanArg(args, "force", true);
        String sourceDir = CommandLineUtils.getStringArg(args, "sourcedir");
        String destDir = CommandLineUtils.getStringArg(args, "destdir");
        String extensions = CommandLineUtils.getStringArg(args, "extensions");

        FilePackager filePackager = new FilePackager();
        filePackager.setForce(force);
        filePackager.setExtensions(extensions.split(","));
        filePackager.setSourceDir(new File(sourceDir));
        filePackager.writePackagesTo(new File(destDir));
    }

    private boolean force = true;
    private File sourceDir;
    private String[] extensions;

    private Map<FileType, FileTransformer> fileTransformers;
    private byte[] startDestFile, endDestFile;
    private Set<Package> packages = new HashSet<Package>();

    public FilePackager() {
    }

    public long lastModified() {
        createPackages();
        long lastest = 0;
        for (Package pkg : packages) {
            long lastModified = pkg.lastModified();
            if (lastModified > lastest) {
                lastest = lastModified;
            }
        }
        return lastest;
    }

    public void writePackagesTo(File destDir) throws Exception {
        // for each pacjkage, writePackageTo(OutputStream os)
        createPackages();
        long time = System.currentTimeMillis();
        long count = 0;
        for (Package pkg : packages) {
            if (writePackageTo(destDir, pkg)) {
                count++;
            }
        }
        System.out.format("INFO: %d files written [%d millis]", count, System.currentTimeMillis() - time);
    }

    public void writePackageTo(OutputStream os) throws Exception {
        createPackages();
        if (packages.isEmpty()) {
            throw new IllegalStateException("No packages to write! There should be a single package when writing to an OutputStream");
        }
        if (packages.size() > 1) {
            throw new IllegalStateException("Too many packages to write! There should be a single package when writing to an OutputStream");
        }
        writePackageTo(os, packages.iterator().next());
    }

    protected boolean writePackageTo(File destDir, Package pkg) throws Exception {
        // create dir?
        if (!destDir.exists()) {
            if (!destDir.mkdirs()) {
                throw new IllegalStateException("Unable to make directories [" + destDir.getAbsolutePath() + "]");
            }
        }

        // Anything need doing?
        File destFile = new File(destDir, pkg.getName());
        if (!force && destFile.exists() && destFile.lastModified() > pkg.lastModified()) {
            System.out.format("[%s] up-to-date\n", destFile.getName());
            return false;
        }

        FileOutputStream os = null;
        try {
            os = new FileOutputStream(destFile);
            writePackageTo(os, pkg);
        } finally {
            if (os != null) {
                try {
                    os.flush();
                    os.close();
                } catch (Exception ex) {
                    // forget about it
                }
            }
        }
        return true;
    }

    protected void writePackageTo(OutputStream os, Package pkg) throws Exception {
        if (startDestFile != null) {
            os.write(startDestFile, 0, startDestFile.length);
        }
        concatentate(pkg.getSourcFiles(), os);
        if (endDestFile != null) {
            os.write(endDestFile, 0, endDestFile.length);
        }
    }

    private void concatentate(List<SourceFile> sourceFiles, OutputStream os) throws IOException {
        FileType lastFileType = FileType.ANY;
        for (SourceFile sourceFile : sourceFiles) {
//            System.out.format("  => source file %s\n", sourceFile.getName());
            FileInputStream is = null;
            try {
                is = sourceFile.getInputStream();
                if (is.available() == 0) {
                    continue;
                }
                FileTransformer transformer = null;
                FileType fileType = sourceFile.getFileType();
                if (fileTransformers != null) {
                    transformer = fileTransformers.get(fileType);
                }
                if (!fileType.equals(lastFileType)) {
                    endFileType(lastFileType, sourceFile,  os);
                    beginFileType(fileType, sourceFile,  os);
                    lastFileType = fileType;
                }
                if (transformer != null) {
                    transformer.transform(sourceFile, is, os, this);
                } else {
                    append(is, os);
                    os.write(NEWLINE_BYTES);
                }
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }
    }

    private void createPackages() {
        if (!packages.isEmpty()) {
            return;
        }

        if (!sourceDir.exists()) {
            System.err.format("ERROR: source dir does not exist: %s\n", sourceDir.getAbsolutePath());
            return;
        }

        // multi-directory mode?
        FileFilter dirFilter = new SimpleFileFilter(extensions, false);
        File[] sourceDirs = sourceDir.listFiles(dirFilter);
        if (sourceDirs.length == 0) {
            sourceDirs = new File[] { sourceDir };
        }

        for (File dir : sourceDirs) {
            Package pkg = new Package();
            pkg.setExtensions(extensions);
            pkg.setSourceDir(dir);
            packages.add(pkg);
        }
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public void setSourceDir(File sourceDir) {
        this.sourceDir = sourceDir;
    }

    public void setExtensions(String[] extensions) {
        this.extensions = extensions;
    }

    public void setFileTransformers(Map<FileType, FileTransformer> transformerMap) {
        this.fileTransformers = transformerMap;
    }

    public void setStartDestFile(String startDestFile) {
        this.startDestFile = startDestFile.getBytes();
    }

    public void setEndDestFile(String endDestFile) {
        this.endDestFile = endDestFile.getBytes();
    }

    // quick hacks for JS specific stuff
    protected void beginFileType(FileType fileType, SourceFile sourceFile, OutputStream os) throws IOException {
    }

    // quick hacks for JS specific stuff
    protected void endFileType(FileType fileType, SourceFile sourceFile, OutputStream os) throws IOException {
    }

    public void append(InputStream is, OutputStream os) throws IOException {
        byte[] buf = new byte[4096];
        int read;
        while ((read = is.read(buf, 0, buf.length)) > 0) {
            os.write(buf, 0, read);
        }
        try {
            is.close();
        } catch (Exception ex) {
            // ignore
        }
    }
}
