package com.sddev.botmaker;

import java.nio.file.Files;
import java.nio.file.Path;

public class PathNode {
    private Path path;
    private boolean isDirectory, isScript;

    public PathNode(Path path) {
        this.path = path;
        isDirectory = Files.isDirectory(path);
        isScript = path.toString().endsWith(".py");
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public boolean isScript() {
        return isScript;
    }

    private static String[] imagesExtensions = {"png", "jpg", "jpeg", "bmp", "tiff"};
    public boolean isImage() {
        String name = path.getFileName().toString().toLowerCase();
        for(String extension : imagesExtensions)
            if(name.endsWith(extension))
                return true;
        return false;
    }

    @Override
    public String toString() {
        return path.getFileName().toString();
    }
}
