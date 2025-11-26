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

    public String getNameForDownloadedFile(String folder, String fileName) {
        int begin = fileName.indexOf(folder) + folder.length();
        return fileName.substring(begin);
    }

    public static String getNameForDownloadedDirectory(String path) {
        String[] folders = path.split("/");
        return folders[folders.length - 1];
    }

    // Если совпадает всё до последнего /, но отличается часть после него → это переименование.
    public boolean isRename(String from, String to) {
        String fromDir = getNameDir(from);
        String fromName = getName(from);

        String toDir = getNameDir(to);
        String toName = getName(to);

        return fromDir.equals(toDir)
                && !fromName.equals(toName)
                && from.charAt(from.length() - 1) == to.charAt(to.length() - 1);
    }

    // Если последняя часть (имя) совпадает, но различается путь до неё → это перемещение.
    public boolean isMove(String from, String to) {
        String fromDir = getNameDir(from);
        String fromName = getName(from);

        String toDir = getNameDir(to);
        String toName = getName(to);

        return !fromDir.equals(toDir)
                && fromName.equals(toName)
                && from.charAt(from.length() - 1) == to.charAt(to.length() - 1);
    }

    private static String getNameDir(String objectName) {
        objectName = objectName.substring(0, objectName.length() - 1);
        return objectName.substring(0, objectName.lastIndexOf("/") + 1);
    }

    private static String getName(String objectName) {
        objectName = objectName.substring(0, objectName.length() - 1);
        return objectName.substring(objectName.lastIndexOf("/") + 1);
    }

    public String getNewKeyForMovingFile(String key, String from, String to) {
        String path = getNameDir(from);
        return key.substring(0, key.lastIndexOf(path)) + to + key.substring(key.indexOf(from) + from.length());
    }
}
