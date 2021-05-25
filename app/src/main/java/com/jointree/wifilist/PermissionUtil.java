package com.jointree.wifilist;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

public class PermissionUtil {

    public static boolean checkPermissions(Activity activity, String permission){
        int permissionResult = ActivityCompat.checkSelfPermission(activity,permission);
        if(permissionResult == PackageManager.PERMISSION_GRANTED) return true;
        else return false;
    }

    public static void requestPermissions(final @NonNull Activity activity, final @NonNull String[] permissions, final int requestCode){
        if (Build.VERSION.SDK_INT >=23){
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
        }else if(activity instanceof ActivityCompat.OnRequestPermissionsResultCallback){
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(()->{
                final int[] grantResults = new int[permissions.length];

                PackageManager packageManager = activity.getPackageManager();
                String packageName = activity.getPackageName();

                final int permissionCount = permissions.length;
                for (int i = 0; i < permissionCount; i++){
                    grantResults[i] = packageManager.checkPermission(permissions[i], packageName);
                }
                ((ActivityCompat.OnRequestPermissionsResultCallback)activity).onRequestPermissionsResult(requestCode,permissions,grantResults);
            });
        }
    }
}
