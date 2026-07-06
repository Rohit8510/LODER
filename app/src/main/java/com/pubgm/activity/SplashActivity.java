package com.pubgm.activity;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import com.pubgm.R;
import com.pubgm.utils.ActivityCompat;
import com.pubgm.utils.FLog;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import org.lsposed.lsparanoid.Obfuscate;

@Obfuscate
public class SplashActivity extends ActivityCompat {

    private static final long SPLASH_DELAY_MS = 2500;

    private CircularProgressIndicator progressIndicator;
    private View dot1, dot2, dot3;
    private Handler loadingHandler;
    private Runnable loadingRunnable;
    private boolean isActivityDestroyed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_splash);
        } catch (Exception e) {
            FLog.error("Splash layout inflate failed: " + e.getMessage());
            // Go directly to login if splash layout fails
            LoginActivity.goLogin(this);
            finish();
            return;
        }

        initializeViews();
        hideSystemUI();
        startLogoAnimation();
        startDotAnimation();
        startLoadingProcess();
    }

    private void initializeViews() {
        try {
            progressIndicator = findViewById(R.id.animationView);
            dot1 = findViewById(R.id.dot1);
            dot2 = findViewById(R.id.dot2);
            dot3 = findViewById(R.id.dot3);
        } catch (Exception e) {
            FLog.error("initializeViews error: " + e.getMessage());
        }
    }

    private void startLogoAnimation() {
        if (progressIndicator == null) return;
        try {
            ScaleAnimation scaleAnimation = new ScaleAnimation(
                0.85f, 1.0f, 0.85f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            );
            scaleAnimation.setDuration(1500);
            scaleAnimation.setRepeatCount(Animation.INFINITE);
            scaleAnimation.setRepeatMode(Animation.REVERSE);
            progressIndicator.startAnimation(scaleAnimation);
        } catch (Exception e) {
            FLog.error("Logo animation error: " + e.getMessage());
        }
    }

    private void startDotAnimation() {
        if (dot1 == null || dot2 == null || dot3 == null) return;
        try {
            final Handler handler = new Handler(Looper.getMainLooper());
            final int[] dotState = {0};
            final float ALPHA_HIGH = 1.0f;
            final float ALPHA_LOW = 0.3f;
            
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (isActivityDestroyed) return;
                    dot1.setAlpha(ALPHA_LOW);
                    dot2.setAlpha(ALPHA_LOW);
                    dot3.setAlpha(ALPHA_LOW);
                    switch (dotState[0] % 3) {
                        case 0: dot1.setAlpha(ALPHA_HIGH); break;
                        case 1: dot2.setAlpha(ALPHA_HIGH); break;
                        case 2: dot3.setAlpha(ALPHA_HIGH); break;
                    }
                    dotState[0]++;
                    if (!isActivityDestroyed) {
                        handler.postDelayed(this, 300);
                    }
                }
            };
            handler.post(runnable);
        } catch (Exception e) {
            FLog.error("Dot animation error: " + e.getMessage());
        }
    }

    private void startLoadingProcess() {
        loadingHandler = new Handler(Looper.getMainLooper());
        loadingRunnable = () -> {
            if (!isActivityDestroyed && !isFinishing()) {
                try {
                    LoginActivity.goLogin(SplashActivity.this);
                    // Finish with a small delay to allow transition
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (!isActivityDestroyed && !isFinishing()) {
                            finish();
                        }
                    }, 200);
                } catch (Exception e) {
                    FLog.error("Start login error: " + e.getMessage());
                    finish();
                }
            }
        };
        loadingHandler.postDelayed(loadingRunnable, SPLASH_DELAY_MS);
    }

    @Override
    protected void onDestroy() {
        isActivityDestroyed = true;
        if (loadingHandler != null && loadingRunnable != null) {
            loadingHandler.removeCallbacks(loadingRunnable);
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() { /* block back */ }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && !isActivityDestroyed) hideSystemUI();
    }

    private void hideSystemUI() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WindowInsetsController controller = getWindow().getInsetsController();
                if (controller != null) {
                    controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                    controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                }
            } else {
                getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN);
            }
        } catch (Exception e) {
            FLog.error("hideSystemUI error: " + e.getMessage());
        }
    }
}