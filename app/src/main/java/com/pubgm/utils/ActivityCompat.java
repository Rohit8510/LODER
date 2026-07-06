package com.pubgm.utils;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.pubgm.Config;
import com.pubgm.R;
import com.pubgm.activity.MainActivity;
import com.pubgm.libhelper.ApkEnv;
import com.pubgm.libhelper.FileHelper;
import com.pubgm.libhelper.Loader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.pubgm.Config.GAME_LIST_ICON;
import static com.pubgm.Config.GAME_LIST_PKG;

public class ActivityCompat extends AppCompatActivity {

    private static ActivityCompat activityCompat;
    public static int REQUEST_OVERLAY_PERMISSION        = 5469;
    public static int PERMISSION_REQUEST_STORAGE        = 100;
    public static int REQUEST_MANAGE_UNKNOWN_APP_SOURCES = 200;

    public boolean isLogin = false;
    public FPrefs  prefs;

    private BottomSheetDialog bottomSheetDialog;
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static String gamename;
    public static String name;
    public static int    version;
    public static String url;

    public static ActivityCompat getActivityCompat() { return activityCompat; }

    public FPrefs getPref() { return FPrefs.with(this); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activityCompat = this;
        super.onCreate(savedInstanceState);

        // FIX 1: setNavBar wrapped in try/catch — won't crash if color missing
        try {
            setNavBar(R.color.background);
        } catch (Exception e) {
            // ignore — just skip nav bar styling
        }

        prefs = getPref();

        // FIX 2: ManageFiles() REMOVED from onCreate
        // It was calling OverlayPermision() → startActivityForResult()
        // BEFORE setContentView() in child activities → BLACK SCREEN
        // Permissions are now requested only when actually needed
    }

    // ── FIX 3: setNavBar — combine flags in ONE call ──────────────────────
    public void setNavBar(int color) {
        try {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // FIX: was two separate setSystemUiVisibility calls, second overwrote first
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
            getWindow().setStatusBarColor(ContextCompat.getColor(this, color));
        } catch (Exception e) {
            // ignore nav bar errors
        }
    }

    public void restartApp(String clazz) {
        try {
            Intent lauchIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
            if (lauchIntent != null) {
                lauchIntent.addFlags(335577088);
                lauchIntent.putExtra("restartApp", clazz);
                startActivity(lauchIntent);
            }
            Runtime.getRuntime().exit(0);
        } catch (Exception e) {
            FLog.error("restartApp error: " + e.getMessage());
        }
    }

    public void toast(CharSequence msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void RestartAppp() {
        try {
            PackageManager pm = getPackageManager();
            Intent intent = pm.getLaunchIntentForPackage(getPackageName());
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        } catch (Exception e) {
            FLog.error("RestartAppp error: " + e.getMessage());
        }
    }

    // ── Permission helpers — called manually when needed ──────────────────

    public void takeFilePermissions() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            } else {
                androidx.core.app.ActivityCompat.requestPermissions(this,
                    new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.MANAGE_EXTERNAL_STORAGE
                    }, PERMISSION_REQUEST_STORAGE);
            }
        } catch (Exception e) {
            FLog.error("takeFilePermissions error: " + e.getMessage());
        }
    }

    public boolean isPermissionGranted() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                return Environment.isExternalStorageManager();
            } else {
                return ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public void InstllUnknownApp() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!getPackageManager().canRequestPackageInstalls()) {
                    Intent intent = new Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, REQUEST_MANAGE_UNKNOWN_APP_SOURCES);
                } else {
                    if (!isPermissionGranted()) takeFilePermissions();
                }
            }
        } catch (Exception e) {
            FLog.error("InstllUnknownApp error: " + e.getMessage());
        }
    }

    public void OverlayPermision() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Intent intent = new Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
                } else {
                    InstllUnknownApp();
                }
            }
        } catch (Exception e) {
            FLog.error("OverlayPermision error: " + e.getMessage());
        }
    }

    // FIX 4: ManageFiles now public so child activities can call it when ready
    public void ManageFiles() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED) {
                    androidx.core.app.ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_STORAGE);
                } else {
                    OverlayPermision();
                }
            }
        } catch (Exception e) {
            FLog.error("ManageFiles error: " + e.getMessage());
        }
    }

    protected AndroidDeferredManager defer() { return UiKit.defer(); }

    private long backPressedTime = 0;

    @Override
    public void onBackPressed() {
        if (isLogin) {
            long t = System.currentTimeMillis();
            if (t - backPressedTime > 2000) {
                backPressedTime = t;
                toast("Press back again to exit");
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemUI();
        else showSystemUI();
    }

    private void hideSystemUI() {
        try {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
        } catch (Exception ignored) {}
    }

    private void showSystemUI() {
        try {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        } catch (Exception ignored) {}
    }

    private void doActionAnimation(CircularProgressIndicator progressIndicator,
                                    TextView txt, String pkg) {
        txt.setText("Starting Client " + pkg + " ...");
        progressIndicator.setVisibility(View.VISIBLE);
        progressIndicator.setIndeterminate(true);
    }

    public void launch(AlertDialog dialog, String pkg) {
        UiKit.defer().when(() -> {
            long startTime = System.currentTimeMillis();
            dialog.dismiss();
            long delta = 500L - (System.currentTimeMillis() - startTime);
            if (delta > 0) UiKit.sleep(delta);
        }).done((ree) -> ApkEnv.getInstance().launchApk(pkg));
    }

    public void launchSplash(String pkg) {
        try {
            View view = getLayoutInflater().inflate(R.layout.launcher, null);
            CardView cv                         = view.findViewById(R.id.cv_lauch);
            ImageView appIcon                   = view.findViewById(R.id.app_icon);
            TextView gameName                   = view.findViewById(R.id.game_name);
            TextView packageInfo                = view.findViewById(R.id.package_info);
            TextView loadingText                = view.findViewById(R.id.loading_text);
            CircularProgressIndicator progress  = view.findViewById(R.id.progress_indicator);
            TextView progressPercent            = view.findViewById(R.id.progress_percent);
            TextView statusMessage              = view.findViewById(R.id.status_message);
            ImageView glowEffect                = view.findViewById(R.id.glow_effect);

            try {
                ApplicationInfo appInfo = getPackageManager().getApplicationInfo(pkg, 0);
                appIcon.setImageDrawable(getPackageManager().getApplicationIcon(appInfo));
                gameName.setText(getPackageManager().getApplicationLabel(appInfo).toString());
                packageInfo.setText(pkg);
            } catch (PackageManager.NameNotFoundException e) {
                appIcon.setImageResource(R.drawable.ic_launcher);
                gameName.setText("Game Client");
                packageInfo.setText(pkg);
            }

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setCancelable(false).setView(view)
                   .setBackground(getResources().getDrawable(R.drawable.background_trans));
            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setDimAmount(0.8f);
            dialog.show();

            startGlowAnimation(glowEffect);
            startProgressAnimation(progress, progressPercent, statusMessage,
                loadingText, dialog, pkg);

        } catch (Exception err) {
            FLog.error(err.getMessage() != null ? err.getMessage() : "Unknown error");
            ApkEnv.getInstance().launchApk(pkg);
        }
    }

    private void startGlowAnimation(ImageView glowEffect) {
        try {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(glowEffect, "scaleX", 1f, 1.2f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(glowEffect, "scaleY", 1f, 1.2f, 1f);
            ObjectAnimator alpha  = ObjectAnimator.ofFloat(glowEffect, "alpha", 0.3f, 0.7f, 0.3f);
            for (ObjectAnimator a : new ObjectAnimator[]{scaleX, scaleY, alpha}) {
                a.setRepeatCount(ValueAnimator.INFINITE);
                a.setRepeatMode(ValueAnimator.RESTART);
                a.setDuration(2000);
            }
            AnimatorSet set = new AnimatorSet();
            set.playTogether(scaleX, scaleY, alpha);
            set.setInterpolator(new AccelerateDecelerateInterpolator());
            set.start();
        } catch (Exception ignored) {}
    }

    private void startProgressAnimation(CircularProgressIndicator progressIndicator,
                                         TextView progressPercent, TextView statusMessage,
                                         TextView loadingText, AlertDialog dialog, String pkg) {
        try {
            progressIndicator.setIndeterminate(false);
            progressIndicator.setMax(100);
            progressIndicator.setProgress(0);
            ValueAnimator animator = ValueAnimator.ofInt(0, 100);
            animator.setDuration(2000);
            animator.addUpdateListener(animation -> {
                int p = (int) animation.getAnimatedValue();
                progressIndicator.setProgress(p);
                progressPercent.setText(p + "%");
                if      (p < 25) { statusMessage.setText("Initializing..."); loadingText.setText("Preparing launch..."); }
                else if (p < 50) { statusMessage.setText("Loading resources..."); loadingText.setText("Loading..."); }
                else if (p < 75) { statusMessage.setText("Optimizing..."); loadingText.setText("Optimizing..."); }
                else             { statusMessage.setText("Launching game..."); loadingText.setText("Almost ready!"); }
            });
            animator.addListener(new android.animation.Animator.AnimatorListener() {
                public void onAnimationStart(android.animation.Animator a) {}
                public void onAnimationEnd(android.animation.Animator a)   { launchGame(dialog, pkg); }
                public void onAnimationCancel(android.animation.Animator a){ launchGame(dialog, pkg); }
                public void onAnimationRepeat(android.animation.Animator a){}
            });
            animator.start();
        } catch (Exception e) {
            launchGame(dialog, pkg);
        }
    }

    private void launchGame(AlertDialog dialog, String pkg) {
        try { dialog.dismiss(); } catch (Exception ignored) {}
        toast("Launching game...");
        new Handler().postDelayed(() -> ApkEnv.getInstance().launchApk(pkg), 300);
    }

    public void launch(AlertDialog dialog, String pkg, boolean usePremium) {
        if (usePremium) {
            launchSplash(pkg);
        } else {
            UiKit.defer().when(() -> {
                long startTime = System.currentTimeMillis();
                dialog.dismiss();
                long delta = 500L - (System.currentTimeMillis() - startTime);
                if (delta > 0) UiKit.sleep(delta);
            }).done((ree) -> ApkEnv.getInstance().launchApk(pkg));
        }
    }
}
