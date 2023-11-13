package com.example.dashcam;

import android.os.Build;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileExistsChecker {
    public static boolean isFileExisit(String filePath){
        Path path = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            path = FileSystems.getDefault().getPath(filePath);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Files.exists(path) && !Files.isDirectory(path);
        }
        return false;
    }
}
