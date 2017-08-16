package com.sddev.botmaker;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class TreeUtils {
    public static DefaultMutableTreeNode listFilesAsNode(Path path) {
        if(path.getFileName().toString().endsWith(".class"))
            return null;
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new PathNode(path));
        ArrayList<MutableTreeNode> directories = new ArrayList<>(), scripts = new ArrayList<>(), others = new ArrayList<>();
        if(Files.isDirectory(path)) {
            try(DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
                for(Path entry : entries) {
                    DefaultMutableTreeNode mutableTreeNode = listFilesAsNode(entry);
                    if(mutableTreeNode != null) {
                        PathNode pathNode = (PathNode) mutableTreeNode.getUserObject();
                        if(pathNode.isDirectory())
                            directories.add(mutableTreeNode);
                        else if(pathNode.isScript())
                            scripts.add(mutableTreeNode);
                        else
                            others.add(mutableTreeNode);
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        for(MutableTreeNode child : directories) {
            node.insert(child, node.getChildCount());
        }
        for(MutableTreeNode child : scripts) {
            node.insert(child, node.getChildCount());
        }
        for(MutableTreeNode child : others) {
            node.insert(child, node.getChildCount());
        }
        return node;
    }
}
