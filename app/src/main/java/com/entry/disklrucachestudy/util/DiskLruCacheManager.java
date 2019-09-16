package com.entry.disklrucachestudy.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;

import androidx.annotation.NonNull;

import java.io.File;

public class DiskLruCacheManager implements DiskCache{

    private Context context;

    /**
     * 获得缓存路径
     * @param context context
     * @param uniqueName 对不同数据类型文件夹进行区分，例如'bitmap'，'object'
     * @return
     */
    @NonNull
    @Override
    public File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        }else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    /**
     * 获得应用程序版本号
     * 每当版本号改变时，DiskLruCache 中数据均自动清空
     * @param context context
     * @return 版本号
     */
    @NonNull
    @Override
    public int getAppVersion(@NonNull Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        }catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

}
