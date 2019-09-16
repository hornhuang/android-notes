package com.entry.disklrucachestudy.util;

import android.content.Context;
import android.content.pm.PackageInfo;

import androidx.annotation.NonNull;

import java.io.File;

/**
 * 硬盘缓存
 */
public interface DiskCache {

    /**
     * 获得缓存路径
     */
    @NonNull
    File getDiskCacheDir(@NonNull Context context, @NonNull String uniqueName);

    // 获得应用程序版本号
    @NonNull
    int getAppVersion(@NonNull Context context);
}
