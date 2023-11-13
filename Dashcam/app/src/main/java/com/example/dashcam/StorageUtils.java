package com.example.dashcam;

import android.os.Environment;
import android.os.StatFs;

import java.text.DecimalFormat;

public class StorageUtils {
    public static String getExternalStorageTotalSpace(){
        String path = Environment.getExternalStorageDirectory().getPath();
        StatFs stat = new StatFs(path);
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        double totalSpaceGB = (double) (totalBlocks * blockSize) / (1024 * 1024 * 1024); //B를 GB로 변환
        return formatDecimal(totalSpaceGB);
    }

    public static String getExternalStorageFreeSpace(){
        String path = Environment.getExternalStorageDirectory().getPath();
        StatFs stat = new StatFs(path);
        long blockSize = stat.getBlockSizeLong();
        long freeBlocks = stat.getAvailableBlocksLong();
        double freeSpaceGB = (double) (freeBlocks * blockSize) / (1024 * 1024 * 1024);
        return formatDecimal(freeSpaceGB);
    }

    private static String formatDecimal(double value){
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        return decimalFormat.format(value);
    }
}
