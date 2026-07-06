package com.pubgm.floating;

import android.annotation.*;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import java.io.*;
import java.lang.Process;
import android.view.WindowManager.LayoutParams;
import androidx.annotation.Keep;
import com.pubgm.activity.MainActivity;
import com.pubgm.floating.HideRecorder;
import com.pubgm.utils.FLog;
import com.pubgm.utils.FPrefs;
import java.io.IOException;

public class Overlay extends Service {

    static {
        try {
            System.loadLibrary("client");
        } catch (UnsatisfiedLinkError w) {
            FLog.error(w.getMessage());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public FPrefs getPref() {
        return FPrefs.with(this);
    }
    
    private native boolean getReady();
    private native void Close();
    public static native void DrawOn(ESPView espView, Canvas canvas);

    private WindowManager windowManager;
    private ESPView overlayView;
    private Overlay Instance;
    Process process;

    @SuppressLint("StaticFieldLeak")
    public static Context ctx;

    @SuppressLint("InflateParams")
    @Override
    public void onCreate() {
        super.onCreate();
        ctx = this;
        Start();
        windowManager = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        overlayView = new ESPView(ctx);
        DrawCanvas();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Close();
        if (overlayView != null) {
            ((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE)).removeView(overlayView);
            overlayView = null;
        }
        process.destroy();
    }

    public void Start() {
        if (Instance == null) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    getReady();
                }
            });
            t.start();
            Thread t2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    StartDaemon();
                }
            });
            t2.start();

        }
    }

    private void DrawCanvas() {
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        }

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, 0, getNavigationBarHeight(), LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_FULLSCREEN, PixelFormat.RGBA_8888
        );

        if (getPref().readBoolean("anti_recorder")) {
			HideRecorder.setFakeRecorderWindowLayoutParams(params);
        }

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 0;
        //params.alpha = 0.8f;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        windowManager.addView(overlayView, params);
    }

    private int getNavigationBarHeight() {
        boolean hasMenuKey = ViewConfiguration.get(this).hasPermanentMenuKey();
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0 && !hasMenuKey) {
            return getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }
    
    public void StartDaemon() {
        Shell(MainActivity.socket);
    }

    public void Shell(String str) {

        DataOutputStream dataOutputStream = null;
        try {
            process = Runtime.getRuntime().exec(str);
        } catch (IOException e) {
            e.printStackTrace();
            process = null;
        }
        if (process != null) {
            dataOutputStream = new DataOutputStream(process.getOutputStream());
        }
        try {
            dataOutputStream.flush();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        try {
            process.waitFor();
        } catch (InterruptedException e3) {
            e3.printStackTrace();
        }
    }

    static boolean getConfig(String key) {
        SharedPreferences sp = ctx.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        return sp.getBoolean(key, false);
    }

}
