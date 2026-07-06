package com.pubgm;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;

import com.pubgm.utils.FLog;
import com.pubgm.utils.FPrefs;
import com.google.android.material.color.DynamicColors;

import org.lsposed.lsparanoid.Obfuscate;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.app.configuration.AppLifecycleCallback;
import top.niunaijun.blackbox.app.configuration.ClientConfiguration;

import java.io.File;

@Obfuscate
public class BoxApplication extends Application {

    public static BoxApplication gApp;
    private BlackBoxCore mBlackBoxCore;

    public static BoxApplication get() { return gApp; }

    private final String[] process_names = {
        "com.pubg.krmobile",   // KOREA
        "com.tencent.ig",      // GLOBAL
        "com.rekoo.pubgm",     // TAIWAN
        "com.vng.pubgmobile",  // VIETNAM
        "com.pubg.imobile"     // BGMI
    };

    static {
        try {
            System.loadLibrary("client");
        } catch (UnsatisfiedLinkError w) {
            // FIX: Don't crash — log and continue
            FLog.error("client lib load failed: " + w.getMessage());
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        try {
            BlackBoxCore.get().doAttachBaseContext(base, new ClientConfiguration() {
                @Override
                public String getHostPackageName() {
                    return base.getPackageName();
                }
            });
        } catch (Exception e) {
            // FIX: Don't crash on BlackBox init failure
            Log.e("BoxApp", "BlackBox attachBaseContext failed: " + e.getMessage());
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        gApp = this;

        // BlackBox init
        try {
            BlackBoxCore.SdkKey("access_7d1f3c8a9e2b5c60");
            BlackBoxCore.get().doCreate();
        } catch (Exception e) {
            Log.e("BoxApp", "BlackBox create failed: " + e.getMessage());
        }

        // App lifecycle callback for game lib loading
        try {
            BlackBoxCore.get().addAppLifecycleCallback(new AppLifecycleCallback() {
                @Override
                public void beforeApplicationOnCreate(String packageName, String processName,
                                                       Application application, int userId) {
                    try {
                        for (String pkg : process_names) {
                            if (pkg.equals(packageName) && pkg.equals(processName)) {
                                if (pkg.equals("com.pubg.imobile")) {
                                    File p1 = new File(getFilesDir(), "loader/libbgmi.so");
                                    if (p1.exists()) {
                                        System.load(p1.getAbsolutePath());
                                        Log.d("BoxApp", "Loaded libbgmi.so for BGMI");
                                    } else {
                                        Log.e("BoxApp", "libbgmi.so not found at: " + p1.getAbsolutePath());
                                        // FIX: Don't call System.exit — just log and continue
                                    }
                                }
                            }
                        }
                    } catch (UnsatisfiedLinkError e) {
                        // FIX: Don't crash app on native lib load failure
                        Log.e("BoxApp", "Native lib load failed: " + e.getMessage());
                    } catch (Exception e) {
                        Log.e("BoxApp", "Error in lifecycle callback: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            // FIX: Don't crash if addAppLifecycleCallback fails
            Log.e("BoxApp", "addAppLifecycleCallback failed: " + e.getMessage());
        }

        // UI config
        try {
            DynamicColors.applyToActivitiesIfAvailable(this);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } catch (Exception e) {
            Log.e("BoxApp", "UI config error: " + e.getMessage());
        }
    }

    public void toast(CharSequence msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
