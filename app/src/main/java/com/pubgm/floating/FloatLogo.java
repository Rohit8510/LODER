package com.pubgm.floating;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;

import com.pubgm.R;
import com.pubgm.utils.Animations;
import com.pubgm.utils.FLog;
import com.pubgm.utils.FPrefs;

import static java.lang.System.exit;

public class FloatLogo extends Service implements View.OnClickListener {

    private WindowManager mWindowManager;
    private View mFloatingView;
    private View espView, logoView;
    private boolean isDragging = false;

    // ============= AUTO COLOR CHANGE =============
    private Handler colorHandler = new Handler(Looper.getMainLooper());
    private Runnable colorRunnable;
    private int currentColorIndex = 0;
    private boolean isColorAnimating = false;

    // Color array for cycling
    private int[] colorPalette = {
        Color.parseColor("#C9A84C"),  // Gold
        Color.parseColor("#FF4444"),  // Red
        Color.parseColor("#4CAF50"),  // Green
        Color.parseColor("#2196F3"),  // Blue
        Color.parseColor("#FF9800"),  // Orange
        Color.parseColor("#9C27B0"),  // Purple
        Color.parseColor("#00BCD4"),  // Cyan
        Color.parseColor("#E91E63"),  // Pink
        Color.parseColor("#FFEB3B"),  // Yellow
        Color.parseColor("#FFFFFF"),  // White
    };

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

    @SuppressLint("CutPasteId")
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            createOver();
            logoView = mFloatingView.findViewById(R.id.relativeLayoutParent);
            espView = mFloatingView.findViewById(R.id.espView);
            Init();
            
            // Start auto color animation
            startAutoColorCycle();
            
            Toast.makeText(this, "⚡ Floating Started", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            FLog.error("FloatLogo onCreate error: " + e.getMessage());
            stopSelf();
        }
    }

    // ============= SERVICE METHODS =============
    private void DrawESP() {
        try {
            startService(new Intent(this, Overlay.class));
        } catch (Exception e) {
            FLog.error("DrawESP error: " + e.getMessage());
        }
    }

    private void StopESP() {
        try {
            stopService(new Intent(this, Overlay.class));
        } catch (Exception e) {
            FLog.error("StopESP error: " + e.getMessage());
        }
    }

    private void StartAimTouch() {
        try {
            startService(new Intent(getApplicationContext(), ToggleSimulation.class));
        } catch (Exception e) {
            FLog.error("StartAimTouch error: " + e.getMessage());
        }
    }

    private void StopAimTouch() {
        try {
            stopService(new Intent(getApplicationContext(), ToggleSimulation.class));
        } catch (Exception e) {
            FLog.error("StopAimTouch error: " + e.getMessage());
        }
    }

    private void StartAimFloat() {
        try {
            startService(new Intent(getApplicationContext(), ToggleAim.class));
        } catch (Exception e) {
            FLog.error("StartAimFloat error: " + e.getMessage());
        }
    }

    private void StopAimFloat() {
        try {
            stopService(new Intent(getApplicationContext(), ToggleAim.class));
        } catch (Exception e) {
            FLog.error("StopAimFloat error: " + e.getMessage());
        }
    }

    private void StartAimBulletFloat() {
        try {
            startService(new Intent(getApplicationContext(), ToggleBullet.class));
        } catch (Exception e) {
            FLog.error("StartAimBulletFloat error: " + e.getMessage());
        }
    }

    private void StopAimBulletFloat() {
        try {
            stopService(new Intent(getApplicationContext(), ToggleBullet.class));
        } catch (Exception e) {
            FLog.error("StopAimBulletFloat error: " + e.getMessage());
        }
    }

    // ============= CREATE OVERLAY =============
    @SuppressLint("InflateParams")
    void createOver() {
        try {
            mFloatingView = LayoutInflater.from(this).inflate(R.layout.float_logo, null);

            int LAYOUT_FLAG;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
            }

            final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.RGBA_8888
            );

            if (getPref().readBoolean("anti_recorder")) {
                HideRecorder.setFakeRecorderWindowLayoutParams(params);
            }

            mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            mWindowManager.addView(mFloatingView, params);

            final GestureDetector gestureDetector = new GestureDetector(this, new SingleTapConfirm());

            // Close button
            TextView closeBtn = mFloatingView.findViewById(R.id.closeBtn);
            if (closeBtn != null) {
                closeBtn.setOnClickListener(v -> {
                    if (espView != null) espView.setVisibility(View.GONE);
                    if (logoView != null) logoView.setVisibility(View.VISIBLE);
                    Animations.flash(closeBtn);
                });
            }

            // ✅ FIXED: Proper Touch Handling - Drag and Tap
            View logoContainer = mFloatingView.findViewById(R.id.relativeLayoutParent);
            if (logoContainer != null) {
                logoContainer.setOnTouchListener(new View.OnTouchListener() {
                    private float startX, startY;
                    private int initialX, initialY;
                    private boolean isSwiping = false;

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                startX = event.getRawX();
                                startY = event.getRawY();
                                initialX = params.x;
                                initialY = params.y;
                                isSwiping = false;
                                isDragging = false;
                                return true;

                            case MotionEvent.ACTION_MOVE:
                                float deltaX = event.getRawX() - startX;
                                float deltaY = event.getRawY() - startY;
                                
                                if (Math.abs(deltaX) > 15 || Math.abs(deltaY) > 15) {
                                    isSwiping = true;
                                    isDragging = true;
                                    params.x = initialX + (int) deltaX;
                                    params.y = initialY + (int) deltaY;
                                    mWindowManager.updateViewLayout(mFloatingView, params);
                                }
                                return true;

                            case MotionEvent.ACTION_UP:
                                if (!isSwiping) {
                                    // It's a tap - toggle ESP panel
                                    if (espView != null && logoView != null) {
                                        if (espView.getVisibility() == View.VISIBLE) {
                                            espView.setVisibility(View.GONE);
                                            logoView.setVisibility(View.VISIBLE);
                                        } else {
                                            espView.setVisibility(View.VISIBLE);
                                            logoView.setVisibility(View.GONE);
                                        }
                                    }
                                }
                                return true;

                            default:
                                return false;
                        }
                    }
                });
            }

            // Setup tab buttons
            setupTabButtons();

        } catch (Exception e) {
            FLog.error("createOver error: " + e.getMessage());
            stopSelf();
        }
    }

    private void setupTabButtons() {
        try {
            final LinearLayout players = mFloatingView.findViewById(R.id.players);
            final LinearLayout aimbots = mFloatingView.findViewById(R.id.aimbots);
            final LinearLayout memory = mFloatingView.findViewById(R.id.memory);
            final TextView playerBtn = mFloatingView.findViewById(R.id.playerBtn);
            final TextView aimbotBtn = mFloatingView.findViewById(R.id.aimbotBtn);
            final TextView otherBtn = mFloatingView.findViewById(R.id.otherBtn);

            GradientDrawable gradientDrawable1 = new GradientDrawable();
            gradientDrawable1.setColor(Color.parseColor("#FF732DC4"));
            gradientDrawable1.setStroke(0, Color.WHITE);
            gradientDrawable1.setCornerRadius(20);

            GradientDrawable gradientDrawable2 = new GradientDrawable();
            gradientDrawable2.setColor(Color.TRANSPARENT);
            gradientDrawable2.setStroke(0, Color.TRANSPARENT);
            gradientDrawable2.setCornerRadius(20);

            // Player Tab (ESP)
            playerBtn.setOnClickListener(v -> {
                Animations.animateClick(v);
                playerBtn.setBackground(gradientDrawable1);
                aimbotBtn.setBackground(gradientDrawable2);
                otherBtn.setBackground(gradientDrawable2);
                playerBtn.setTextColor(Color.parseColor("#ff5ee863"));
                aimbotBtn.setTextColor(Color.parseColor("#BDBDBD"));
                otherBtn.setTextColor(Color.parseColor("#BDBDBD"));
                players.setVisibility(View.VISIBLE);
                aimbots.setVisibility(View.GONE);
                memory.setVisibility(View.GONE);
            });

            // AIM Tab
            aimbotBtn.setOnClickListener(v -> {
                Animations.animateClick(v);
                playerBtn.setBackground(gradientDrawable2);
                aimbotBtn.setBackground(gradientDrawable1);
                otherBtn.setBackground(gradientDrawable2);
                playerBtn.setTextColor(Color.parseColor("#BDBDBD"));
                aimbotBtn.setTextColor(Color.parseColor("#ff5ee863"));
                otherBtn.setTextColor(Color.parseColor("#BDBDBD"));
                players.setVisibility(View.GONE);
                aimbots.setVisibility(View.VISIBLE);
                memory.setVisibility(View.GONE);
            });

            // MISC Tab (if visible)
            if (otherBtn != null && otherBtn.getVisibility() == View.VISIBLE) {
                otherBtn.setOnClickListener(v -> {
                    Animations.animateClick(v);
                    playerBtn.setBackground(gradientDrawable2);
                    aimbotBtn.setBackground(gradientDrawable2);
                    otherBtn.setBackground(gradientDrawable1);
                    playerBtn.setTextColor(Color.parseColor("#BDBDBD"));
                    aimbotBtn.setTextColor(Color.parseColor("#BDBDBD"));
                    otherBtn.setTextColor(Color.parseColor("#ff5ee863"));
                    players.setVisibility(View.GONE);
                    aimbots.setVisibility(View.GONE);
                    memory.setVisibility(View.VISIBLE);
                });
            }

        } catch (Exception e) {
            FLog.error("setupTabButtons error: " + e.getMessage());
        }
    }

    // ============= AUTO COLOR CYCLE =============

    private void startAutoColorCycle() {
        if (isColorAnimating) return;
        isColorAnimating = true;
        
        colorRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isColorAnimating) return;
                
                // Get next color
                int color = colorPalette[currentColorIndex % colorPalette.length];
                currentColorIndex++;
                
                // Apply color to floating UI
                applyColorToUI(color);
                
                // Update AIM circle color
                setAimCircleColor(color);
                
                // Update SeekBar colors
                changeSeekBarColors(color);
                
                // Schedule next color change (every 2 seconds)
                colorHandler.postDelayed(this, 2000);
            }
        };
        
        colorHandler.post(colorRunnable);
    }

    private void stopAutoColorCycle() {
        isColorAnimating = false;
        if (colorHandler != null && colorRunnable != null) {
            colorHandler.removeCallbacks(colorRunnable);
        }
    }

    private void applyColorToUI(int color) {
        try {
            // Change bottom text color
            TextView textView4 = mFloatingView.findViewById(R.id.textView4);
            if (textView4 != null) {
                textView4.setTextColor(color);
            }
            
            // Change close button color
            TextView closeBtn = mFloatingView.findViewById(R.id.closeBtn);
            if (closeBtn != null) {
                closeBtn.setTextColor(color);
            }
            
            // Change Range text colors
            TextView rangeText = mFloatingView.findViewById(R.id.rangeText);
            if (rangeText != null) {
                rangeText.setTextColor(color);
            }
            
            TextView distText = mFloatingView.findViewById(R.id.distancetext);
            if (distText != null) {
                distText.setTextColor(color);
            }
            
            TextView recoilText = mFloatingView.findViewById(R.id.AimRecoilCompetext);
            if (recoilText != null) {
                recoilText.setTextColor(color);
            }
            
            TextView wideText = mFloatingView.findViewById(R.id.wideviewtext);
            if (wideText != null) {
                wideText.setTextColor(color);
            }
            
        } catch (Exception e) {
            FLog.error("applyColorToUI error: " + e.getMessage());
        }
    }

    private void setAimCircleColor(int color) {
        try {
            // Save to preferences
            SharedPreferences sp = getSharedPreferences("espValue", Context.MODE_PRIVATE);
            sp.edit().putInt("aimColor", color).apply();
            
            // If your native lib supports color change, uncomment:
            // SetAimColor(color);
            
            FLog.debug("AIM Circle color changed to: " + color);
            
        } catch (Exception e) {
            FLog.error("setAimCircleColor error: " + e.getMessage());
        }
    }

    private void changeSeekBarColors(int color) {
        try {
            // Change Range SeekBar
            SeekBar rangeSeek = mFloatingView.findViewById(R.id.range);
            if (rangeSeek != null) {
                rangeSeek.setThumbTintList(ColorStateList.valueOf(color));
                rangeSeek.setProgressTintList(ColorStateList.valueOf(color));
            }
            
            // Change Distance SeekBar
            SeekBar distSeek = mFloatingView.findViewById(R.id.distances);
            if (distSeek != null) {
                distSeek.setThumbTintList(ColorStateList.valueOf(color));
                distSeek.setProgressTintList(ColorStateList.valueOf(color));
            }
            
            // Change Recoil SeekBar
            SeekBar recoilSeek = mFloatingView.findViewById(R.id.AimRecoilCompe);
            if (recoilSeek != null) {
                recoilSeek.setThumbTintList(ColorStateList.valueOf(color));
                recoilSeek.setProgressTintList(ColorStateList.valueOf(color));
            }
            
            // Change WideView SeekBar
            SeekBar wideSeek = mFloatingView.findViewById(R.id.wideview);
            if (wideSeek != null) {
                wideSeek.setThumbTintList(ColorStateList.valueOf(color));
                wideSeek.setProgressTintList(ColorStateList.valueOf(color));
            }
            
        } catch (Exception e) {
            FLog.error("changeSeekBarColors error: " + e.getMessage());
        }
    }

    private void toggleColorAnimation() {
        if (isColorAnimating) {
            stopAutoColorCycle();
            Toast.makeText(this, "⏹️ Color Animation Stopped", Toast.LENGTH_SHORT).show();
        } else {
            startAutoColorCycle();
            Toast.makeText(this, "🌈 Color Animation Started", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        
        // Stop color animation
        stopAutoColorCycle();
        
        if (mFloatingView != null && mWindowManager != null) {
            try {
                mWindowManager.removeView(mFloatingView);
            } catch (Exception ignored) {}
        }
    }

    public void Execute(String path) {}

    @Override
    public void onClick(View v) {}

    // ============= PREFERENCES =============
    boolean getConfig(String key) {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        return sp.getBoolean(key, false);
    }

    private void setValue(String key, boolean b) {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        sp.edit().putBoolean(key, b).apply();
    }

    private int getrangeAim() {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        return sp.getInt("getrangeAim", 50);
    }

    private void getrangeAim(int getrangeAim) {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        sp.edit().putInt("getrangeAim", getrangeAim).apply();
    }

    private int getDistances() {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        return sp.getInt("Distances", 100);
    }

    private void setDistances(int Distances) {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        sp.edit().putInt("Distances", Distances).apply();
    }

    private int getwideview() {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        return sp.getInt("getwideview", 100);
    }

    private void getwideview(int getwideview) {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        sp.edit().putInt("getwideview", getwideview).apply();
    }

    private int getrecoilAim() {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        return sp.getInt("getrecoilAim", 50);
    }

    private void getrecoilAim(int getrecoilAim) {
        SharedPreferences sp = this.getSharedPreferences("espValue", Context.MODE_PRIVATE);
        sp.edit().putInt("getrecoilAim", getrecoilAim).apply();
    }

    // ============= SEEK BAR SETUP =============
    void setupSeekBar(final SeekBar seekBar, final TextView textView, final int initialValue, final Runnable onChangeFunction) {
        if (seekBar == null || textView == null) return;
        seekBar.setProgress(initialValue);
        textView.setText(String.valueOf(initialValue));
        onChangeFunction.run();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView.setText(String.valueOf(progress));
                onChangeFunction.run();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    // ============= ESP VISUAL TOGGLES =============
    private void EspVisual(int id, int index) {
        try {
            SwitchCompat sw = mFloatingView.findViewById(id);
            if (sw == null) return;
            sw.setChecked(getConfig(sw.getText().toString()));
            SettingValue(index, sw.isChecked());
            sw.setOnCheckedChangeListener((btn, isChecked) -> {
                setValue(sw.getText().toString(), isChecked);
                SettingValue(index, isChecked);
            });
        } catch (Exception e) {
            FLog.error("EspVisual error: " + e.getMessage());
        }
    }

    public void Hide() {
        stopSelf();
        exit(-1);
    }

    // ============= INIT =============
    @SuppressLint("CutPasteId")
    void Init() {
        try {
            // ✅ ESP Toggle
            final TextView EnableEsp = mFloatingView.findViewById(R.id.EnableEsp);
            if (EnableEsp != null) {
                final boolean[] isChecked = {false};
                EnableEsp.setOnClickListener(v -> {
                    isChecked[0] = !isChecked[0];
                    if (isChecked[0]) {
                        DrawESP();
                        EnableEsp.setText("✓ ESP Enabled");
                        EnableEsp.setTextColor(Color.GREEN);
                    } else {
                        StopESP();
                        EnableEsp.setText("✗ ESP Disabled");
                        EnableEsp.setTextColor(Color.RED);
                    }
                });
            }

            // ✅ Logo Bypass
            final TextView isLogoBypass = mFloatingView.findViewById(R.id.isLogoBypass);
            if (isLogoBypass != null) {
                isLogoBypass.setOnClickListener(v -> {
                    Execute("/LogoBypass 1216");
                    Execute("/LogoBypass 1520");
                    Execute("/LogoBypass 1728");
                    Animations.pulse(isLogoBypass);
                    Toast.makeText(this, "Logo Bypass Applied", Toast.LENGTH_SHORT).show();
                });
            }

            // ✅ Lobby Bypass
            final TextView isLobbyBypass = mFloatingView.findViewById(R.id.isLobbyBypass);
            if (isLobbyBypass != null) {
                isLobbyBypass.setOnClickListener(v -> {
                    Execute("/LobbyBypass 1216");
                    Execute("/LobbyBypass 1520");
                    Execute("/LobbyBypass 1728");
                    Animations.pulse(isLobbyBypass);
                    Toast.makeText(this, "Lobby Bypass Applied", Toast.LENGTH_SHORT).show();
                });
            }

            // ✅ Color Animation Toggle
            TextView toggleColor = mFloatingView.findViewById(R.id.toggleColor);
            if (toggleColor != null) {
                toggleColor.setOnClickListener(v -> {
                    toggleColorAnimation();
                    Animations.pulse(toggleColor);
                    if (isColorAnimating) {
                        toggleColor.setText("⏹️ Stop Color Animation");
                        toggleColor.setTextColor(Color.GREEN);
                    } else {
                        toggleColor.setText("🌈 Start Color Animation");
                        toggleColor.setTextColor(Color.parseColor("#C9A84C"));
                    }
                });
            }

            // ✅ ESP Visuals
            EspVisual(R.id.isBox, 1);
            EspVisual(R.id.isLine, 2);
            EspVisual(R.id.isDist, 3);
            EspVisual(R.id.isHealth, 4);
            EspVisual(R.id.isName, 5);
            EspVisual(R.id.isHead, 6);
            EspVisual(R.id.is360Alert, 7);
            EspVisual(R.id.isSkeleton, 8);
            EspVisual(R.id.isGrenadeWarning, 9);
            EspVisual(R.id.isPlayerWeapon, 10);
            EspVisual(R.id.isLootItems, 11);
            EspVisual(R.id.isPlayerNation, 12);
            EspVisual(R.id.isPlayerTeamID, 13);
            EspVisual(R.id.isVehicles, 14);
            EspVisual(R.id.isHideBot, 15);
            EspVisual(R.id.isItems, 16);

            // ✅ Memory Features
            MemoryHack(R.id.isRecoil, 1);
            MemoryHack(R.id.isInstant, 2);
            MemoryHack(R.id.isSmallCross, 3);
            MemoryHack(R.id.isSpeed, 4);
            MemoryHack(R.id.isNightMode, 5);

            // ✅ AIM Group
            RadioGroup aimgrup = mFloatingView.findViewById(R.id.grupaim);
            if (aimgrup != null) {
                aimgrup.setOnCheckedChangeListener((group, checkedId) -> {
                    if (checkedId == R.id.disableaim) {
                        StopAimBulletFloat();
                        StopAimFloat();
                        StopAimTouch();
                        Toast.makeText(this, "AIM Disabled", Toast.LENGTH_SHORT).show();
                    } else if (checkedId == R.id.aimbot) {
                        StartAimFloat();
                        StopAimBulletFloat();
                        StopAimTouch();
                        Toast.makeText(this, "AIMBOT Enabled", Toast.LENGTH_SHORT).show();
                    } else if (checkedId == R.id.bullettrack) {
                        StartAimBulletFloat();
                        StopAimFloat();
                        StopAimTouch();
                        Toast.makeText(this, "Bullet Track Enabled", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // ✅ Range SeekBar
            final SeekBar rangeSeekBar = mFloatingView.findViewById(R.id.range);
            final TextView rangeText = mFloatingView.findViewById(R.id.rangeText);
            if (rangeSeekBar != null && rangeText != null) {
                setupSeekBar(rangeSeekBar, rangeText, getrangeAim(), () -> {
                    Range(rangeSeekBar.getProgress());
                    getrangeAim(rangeSeekBar.getProgress());
                });
            }

            // ✅ WideView SeekBar
            final SeekBar distancesSeekBar = mFloatingView.findViewById(R.id.wideview);
            final TextView distancesText = mFloatingView.findViewById(R.id.wideviewtext);
            if (distancesSeekBar != null && distancesText != null) {
                setupSeekBar(distancesSeekBar, distancesText, getDistances(), () -> {
                    WideView(distancesSeekBar.getProgress());
                    getwideview(distancesSeekBar.getProgress());
                });
            }

            // ✅ Recoil SeekBar
            final SeekBar RecoilCompeSeekBar = mFloatingView.findViewById(R.id.AimRecoilCompe);
            final TextView RecoilCompeText = mFloatingView.findViewById(R.id.AimRecoilCompetext);
            if (RecoilCompeSeekBar != null && RecoilCompeText != null) {
                setupSeekBar(RecoilCompeSeekBar, RecoilCompeText, getDistances(), () -> {
                    recoil(RecoilCompeSeekBar.getProgress());
                    getrecoilAim(RecoilCompeSeekBar.getProgress());
                });
            }

            // ✅ Distances SeekBar
            final SeekBar wideviewSeekBar = mFloatingView.findViewById(R.id.distances);
            final TextView wideviewText = mFloatingView.findViewById(R.id.distancetext);
            if (wideviewSeekBar != null && wideviewText != null) {
                setupSeekBar(wideviewSeekBar, wideviewText, getDistances(), () -> {
                    distances(wideviewSeekBar.getProgress());
                    setDistances(wideviewSeekBar.getProgress());
                });
            }

            // ✅ Aim By RadioGroup
            final RadioGroup aimby = mFloatingView.findViewById(R.id.aimby);
            if (aimby != null) {
                aimby.setOnCheckedChangeListener((radioGroup, i) -> {
                    int chkdId = aimby.getCheckedRadioButtonId();
                    RadioButton btn = mFloatingView.findViewById(chkdId);
                    if (btn != null && btn.getTag() != null) {
                        AimBy(Integer.parseInt(btn.getTag().toString()));
                    }
                });
            }

            // ✅ Aim When RadioGroup
            final RadioGroup aimwhen = mFloatingView.findViewById(R.id.aimwhen);
            if (aimwhen != null) {
                aimwhen.setOnCheckedChangeListener((radioGroup, i) -> {
                    int chkdId = aimwhen.getCheckedRadioButtonId();
                    RadioButton btn = mFloatingView.findViewById(chkdId);
                    if (btn != null && btn.getTag() != null) {
                        AimWhen(Integer.parseInt(btn.getTag().toString()));
                    }
                });
            }

            // ✅ Aim Mode RadioGroup
            final RadioGroup aimbotmode = mFloatingView.findViewById(R.id.aimbotmode);
            if (aimbotmode != null) {
                aimbotmode.setOnCheckedChangeListener((radioGroup, i) -> {
                    int chkdId = aimbotmode.getCheckedRadioButtonId();
                    RadioButton btn = mFloatingView.findViewById(chkdId);
                    if (btn != null && btn.getTag() != null) {
                        Target(Integer.parseInt(btn.getTag().toString()));
                    }
                });
            }

        } catch (Exception e) {
            FLog.error("Init error: " + e.getMessage());
        }
    }

    private void MemoryHack(int id, int code) {
        try {
            SwitchCompat sw = mFloatingView.findViewById(id);
            if (sw == null) return;
            boolean val = getConfig(sw.getText().toString());
            sw.setChecked(val);
            SettingMemory(code, val);
            sw.setOnCheckedChangeListener((buttonView, isChecked) -> {
                SettingMemory(code, isChecked);
            });
        } catch (Exception e) {
            FLog.error("MemoryHack error: " + e.getMessage());
        }
    }

    // ============= NATIVE METHODS =============
    public native void SettingValue(int setting_code, boolean value);
    public native void SettingMemory(int setting_code, boolean value);
    public native void SettingAim(int setting_code, boolean value);
    public native void RadarSize(int size);
    public native void Range(int range);
    public native void Target(int target);
    public native void AimBy(int aimby);
    public native void AimWhen(int aimwhen);
    public native void distances(int distances);
    public native void WideView(int wideview);
    public native void recoil(int recoil);
}

class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {
    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        return true;
    }
}