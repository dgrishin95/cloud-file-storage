package com.mysite.cloudfilestorage.util;

import java.nio.file.Paths;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PathUtil {

    public boolean isDirectory(String path) {
        return path.endsWith("/");
    }

    public String getPathForDirectory(String objectName) {
        int first = objectName.indexOf("/");
        int last = objectName.lastIndexOf("/");

        String folderPath = objectName.substring(first, last);
        last = folderPath.lastIndexOf("/");

        return folderPath.substring(1, last + 1);
    }

    public String getNameForDirectory(String objectName) {
        int first = objectName.indexOf("/");
        int last = objectName.lastIndexOf("/");

        String name = objectName.substring(first, last);

        return name.substring(name.lastIndexOf("/") + 1);
    }

    public String getNameForFile(String objectName) {
        return Paths.get(objectName).getFileName().toString();
    }

    public String getPathForFile(String objectName) {
        String folderPath = "";

        int firstSlashIndex = objectName.indexOf('/');
        int lastSlashIndex = objectName.lastIndexOf('/');

        if (lastSlashIndex != -1) {
            folderPath = objectName.substring(firstSlashIndex + 1, lastSlashIndex + 1);
        }

        return folderPath;
    }
}
