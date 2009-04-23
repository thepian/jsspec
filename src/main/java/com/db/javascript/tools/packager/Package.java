package com.db.javascript.tools.packager;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Maurice Nicholson
 */
public class Package {
    private static Comparator<SourceFile> fileComparator = new JavaScriptSourceFileComparator();

    private String[] extensions;
    private File sourceDir;
    private List<SourceFile> files;
    private long lastModified = 0;

    // note package-private
    Package() {
    }

    public long lastModified() {
        try {
            build();
        } catch (Exception e) {
            System.err.format("ERROR: %s", e.getMessage());
        }
        return lastModified;
    }

    public String getName() {
        return sourceDir.getName();
    }

    public List<SourceFile> getSourcFiles() {
        try {
            build();
        } catch (Exception e) {
            return Collections.emptyList();
        }
        return files;
    }

    private void build() throws Exception {
        if (files == null) {
            FileFilter fileFilter = new SimpleFileFilter(extensions, true);
            FilesetBuilder filesetBuilder = new FilesetBuilder(fileFilter, new SimpleFileFilter(false), new DefaultSourceFileFactory());
            files = createFileset(sourceDir, filesetBuilder, fileComparator);
            for (SourceFile file : files) {
                if (file.lastModified() > lastModified) {
                    lastModified = file.lastModified();
                }
            }
        }
    }

    private static List<SourceFile> createFileset(File sourceDir, FilesetBuilder filesetBuilder, Comparator<SourceFile> fileComparator) throws Exception {
        // Gather files
        List<SourceFile> files = filesetBuilder.buildSourceFiles(sourceDir);

        // Order files?
        if (fileComparator != null) {
            Collections.sort(files, fileComparator);
        }
        return files;
    }

    public void setSourceDir(File sourceDir) {
        this.sourceDir = sourceDir;
    }

    public void setExtensions(String[] extensions) {
        this.extensions = extensions;
    }

    /**
     *
     * @author Maurice Nicholson
     */
    private static class FilesetBuilder {
        private FileFilter fileFilter;
        private FileFilter dirFilter;
        private SourceFileFactory sourceFileFactory;

        public FilesetBuilder(FileFilter fileFilter, FileFilter dirFilter, SourceFileFactory sourceFileFactory) {
            this.fileFilter = fileFilter;
            this.dirFilter = dirFilter;
            this.sourceFileFactory = sourceFileFactory;
        }

        public List<SourceFile> buildSourceFiles(File sourceDir) throws IOException {
            List<SourceFile> files = new ArrayList<SourceFile>();
            addFiles(sourceDir, sourceDir, files, 0);
            return files;
        }

        private void addFiles(File originalSourceDir, File sourceDir, List<SourceFile> files, long lastModified) throws IOException {
            for (File file : sourceDir.listFiles()) {
                if (file.isFile() && fileFilter.accept(file)) {
                    files.add(sourceFileFactory.createSourceFile(originalSourceDir, file));
                } else if (dirFilter.accept(file)) {
                    addFiles(originalSourceDir, file, files, lastModified);
                }
            }
        }
    }
}
