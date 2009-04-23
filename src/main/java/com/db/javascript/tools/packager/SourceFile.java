package com.db.javascript.tools.packager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * A basic wrapper around java.io.File adding packager-specufuc
 * @author Maurice Nicholson
 */
public class SourceFile {
    private File file;
    private FileType fileType;
    private File sourceDir;
    private String relativeName = null;
    private long lastModified;

    public SourceFile(File sourceDir, File file) {
        this(sourceDir, file, FileType.forFile(file));
    }

    public SourceFile(File sourceDir, File file, FileType fileType) {
        this.sourceDir = sourceDir;
        this.file = file;
        this.fileType = fileType;
        this.lastModified = file.lastModified();
    }

    protected String basename() {
        String name = file.getName();
        return name.substring(0, name.indexOf("."));
    }

    public String getName() {
        return file.getName();
    }

    public String getRelativeName() {
        if (relativeName == null) {
            relativeName = file.getAbsolutePath().substring(sourceDir.getAbsolutePath().lastIndexOf(File.separator) + 1).replace(File.separator, "/");
        }
        return relativeName;
    }

    public long lastModified() {
        return lastModified;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public Object getAbsolutePath() {
        return file.getAbsolutePath();
    }

    public FileInputStream getInputStream() throws FileNotFoundException {
        return new FileInputStream(file);
    }
}
