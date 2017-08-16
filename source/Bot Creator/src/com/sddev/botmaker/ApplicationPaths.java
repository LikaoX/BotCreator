package com.sddev.botmaker;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class ApplicationPaths {
    private ApplicationPaths() {}
    public static final Path applicationDirectory = Paths.get(System.getenv("APPDATA"), "Bot Creator");
    public static final Path projectsList = applicationDirectory.resolve("projects.dat");
}
