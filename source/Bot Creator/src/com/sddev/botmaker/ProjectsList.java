package com.sddev.botmaker;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class ProjectsList extends ArrayList<Project> {
    public void save(Path path) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(path)));
        dataOutputStream.writeInt(size());
        dataOutputStream.flush();
        for(Project project : this) {
            dataOutputStream.writeUTF(project.getName());
            dataOutputStream.writeUTF(project.getDirectory());
            dataOutputStream.flush();
        }
        dataOutputStream.close();
    }
    public static ProjectsList load(Path path) throws IOException, ClassNotFoundException {
        ProjectsList list = new ProjectsList();
        DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(Files.newInputStream(path)));
        int size = dataInputStream.readInt();
        for(int i = 0 ; i < size ; i++) {
            list.add(new Project(dataInputStream.readUTF(), dataInputStream.readUTF()));
        }
        dataInputStream.close();
        return list;
    }
}
