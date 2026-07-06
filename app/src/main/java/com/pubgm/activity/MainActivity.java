package com.pubgm.activity;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.pubgm.Config;
import com.pubgm.Login;
import com.pubgm.R;
import com.pubgm.adapter.RecyclerViewAdapter;
import com.pubgm.floating.FloatLogo;
import com.pubgm.libhelper.DownloadZip;
import com.pubgm.utils.ActivityCompat;
import com.pubgm.utils.Shell;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lsposed.lsparanoid.Obfuscate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static com.pubgm.Config.GAME_LIST_ICON;
import static com.pubgm.Config.GAME_LIST_PKG;

@Obfuscate
public class MainActivity extends ActivityCompat {

    public static MainActivity instance;
    private String daemonPath;
    public static String socket;
    public String CURRENT_PACKAGE = "";
    private LinearProgressIndicator progress;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;
    private String sFixCrash = null;
    private Dialog verifyDialog;
    private View progressBarView;
    private TextView tvStatusText, tvStatusDetail, tvKeyText, tvErrorMessage;
    private LinearLayout progressContainer, statusContainer, deviceInfoContainer,
            keyContainer, successScreen, errorScreen;
    private TextView tvDeviceInfo, tvAndroidInfo, tvSDKInfo;
    private boolean verified = false;
    private Handler handler = new Handler(Looper.getMainLooper());

    // Game data lists
    private ArrayList<String> packageValues = new ArrayList<>();
    private ArrayList<String> statusValues = new ArrayList<>();
    private ArrayList<String> titleValues = new ArrayList<>();

    public static MainActivity get() {
        return instance;
    }

    public static void goMain(Context context) {
        Intent i = new Intent(context, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.activity_main);

        try {
            sFixCrash = Login.FixCrash();
        } catch (Exception e) {
            sFixCrash = null;
        }

        progress = findViewById(R.id.progress);
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
        hideSystemUI();

        View searchBox = findViewById(R.id.searchBox);
        if (searchBox != null) {
            searchBox.setOnClickListener(v -> {});
        }

        View profileContainer = findViewById(R.id.profileContainer);
        if (profileContainer != null) {
            profileContainer.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ProfileActivity.class)));
        }

        SharedPreferences prefs = getSharedPreferences("auth_pref", MODE_PRIVATE);
        boolean isVerified = prefs.getBoolean("verified", false);
        long expiry = prefs.getLong("expiry", 0);

        if (isVerified && expiry > System.currentTimeMillis() / 1000) {
            verified = true;
            recyclerView.setVisibility(View.VISIBLE);
            doInitRecycler();
        } else {
            prefs.edit().putBoolean("verified", false).apply();
            createVerifyDialog();
        }
    }

    // ── Recycler ─────────────────────────────────────────────
    public void doInitRecycler() {
        progress.setVisibility(View.VISIBLE);
        handler.postDelayed(() -> {
            ArrayList<Integer> imageValues   = new ArrayList<>();
            ArrayList<String>  titleValues   = new ArrayList<>();
            ArrayList<String>  versionValues = new ArrayList<>();
            ArrayList<String>  statusValues  = new ArrayList<>();
            ArrayList<String>  packageValues = new ArrayList<>();
            ArrayList<String>  genreValues   = new ArrayList<>();
            ArrayList<String>  regionValues  = new ArrayList<>();

            // Store for click listeners
            this.packageValues = packageValues;
            this.statusValues = statusValues;
            this.titleValues = titleValues;

            String jsonString = loadJson("games.json");
            if (jsonString != null) {
                try {
                    JSONArray jsonArray = new JSONArray(jsonString);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject game = jsonArray.getJSONObject(i);
                        imageValues.add(GAME_LIST_ICON[game.getInt("imageIndex")]);
                        titleValues.add(game.getString("title"));
                        versionValues.add(game.getString("version"));
                        statusValues.add(game.getString("status"));
                        packageValues.add(game.getString("package"));
                        genreValues.add(game.getString("genre"));
                        regionValues.add(game.getString("region"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            adapter = new RecyclerViewAdapter(this, imageValues, titleValues,
                    versionValues, statusValues, packageValues, genreValues, regionValues);

            // ✅ FIX: Set click listeners for game actions
            adapter.setOnGameActionListener(new RecyclerViewAdapter.OnGameActionListener() {
                @Override
                public void onLaunch(String packageName) {
                    launchGame(packageName);
                }

                @Override
                public void onWipe(String packageName) {
                    wipeGameData(packageName);
                }

                @Override
                public void onInstall(String packageName) {
                    installGame(packageName);
                }
            });

            recyclerView.setAdapter(adapter);
            progress.setVisibility(View.GONE);
        }, 300);
    }

    // ============= GAME ACTIONS =============
    
    private void launchGame(String packageName) {
        try {
            PackageManager pm = getPackageManager();
            try {
                pm.getPackageInfo(packageName, 0);
                // App is installed - launch it
                Intent intent = pm.getLaunchIntentForPackage(packageName);
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    Toast.makeText(this, "Launching " + packageName, Toast.LENGTH_SHORT).show();
                }
            } catch (PackageManager.NameNotFoundException e) {
                // App not installed - open Play Store
                Toast.makeText(this, "App not installed. Opening Play Store...", Toast.LENGTH_LONG).show();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, 
                        Uri.parse("market://details?id=" + packageName)));
                } catch (Exception ex) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to launch: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void wipeGameData(String packageName) {
        try {
            Process process = Runtime.getRuntime().exec("pm clear " + packageName);
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                Toast.makeText(this, "Data wiped for " + packageName, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to wipe data", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to wipe data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void installGame(String packageName) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, 
                Uri.parse("market://details?id=" + packageName));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
            } catch (Exception ex) {
                Toast.makeText(this, "Failed to open Play Store", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public String loadJson(String fileName) {
        try {
            File file = new File(getFilesDir(), fileName);
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                byte[] buffer = new byte[fis.available()];
                fis.read(buffer);
                fis.close();
                return new String(buffer, "UTF-8");
            }
        } catch (Exception e) { e.printStackTrace(); }

        try {
            File file = new File(getExternalFilesDir(null), fileName);
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                byte[] buffer = new byte[fis.available()];
                fis.read(buffer);
                fis.close();
                return new String(buffer, "UTF-8");
            }
        } catch (Exception e) { e.printStackTrace(); }

        try {
            InputStream is = getAssets().open(fileName);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            return new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void loadAssets() {
        String filepath = Environment.getExternalStorageDirectory() + "/Android/data/.tyb";
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filepath);
            fos.write("DO NOT DELETE".getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) try { fos.close(); } catch (IOException ignored) {}
        }

        daemonPath = getFilesDir().toString() + "/sock64";
        socket = Shell.rootAccess() ? "su -c " + daemonPath : daemonPath;
        try {
            Runtime.getRuntime().exec("chmod 777 " + daemonPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Verify Dialog ─────────────────────────────────────────
    private void createVerifyDialog() {
        try {
            verifyDialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
            verifyDialog.setCancelable(false);

            View dialogView = getLayoutInflater().inflate(R.layout.dialog_verify, null);
            verifyDialog.setContentView(dialogView);

            progressBarView      = dialogView.findViewById(R.id.progressBar);
            tvStatusText         = dialogView.findViewById(R.id.tvStatusText);
            tvStatusDetail       = dialogView.findViewById(R.id.tvStatusDetail);
            tvKeyText            = dialogView.findViewById(R.id.tvKeyText);
            tvErrorMessage       = dialogView.findViewById(R.id.tvErrorMessage);
            progressContainer    = dialogView.findViewById(R.id.progressContainer);
            statusContainer      = dialogView.findViewById(R.id.statusContainer);
            deviceInfoContainer  = dialogView.findViewById(R.id.deviceInfoContainer);
            keyContainer         = dialogView.findViewById(R.id.keyContainer);
            successScreen        = dialogView.findViewById(R.id.successScreen);
            errorScreen          = dialogView.findViewById(R.id.errorScreen);
            tvDeviceInfo         = dialogView.findViewById(R.id.tvDeviceInfo);
            tvAndroidInfo        = dialogView.findViewById(R.id.tvAndroidInfo);
            tvSDKInfo            = dialogView.findViewById(R.id.tvSDKInfo);

            Window window = verifyDialog.getWindow();
            if (window != null) {
                int w = (int) (180 * getResources().getDisplayMetrics().density);
                int h = (int) (220 * getResources().getDisplayMetrics().density);
                window.setLayout(w, h);
                window.setGravity(Gravity.CENTER);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }

            verifyDialog.show();
            startVerification();
        } catch (Exception e) {
            LoginActivity.goLogin(this);
            finish();
        }
    }

    private void startVerification() {
        if (tvStatusText != null) tvStatusText.setText("VERIFYING");
        if (tvStatusDetail != null) tvStatusDetail.setText("Please wait...");
        loadDeviceInfo();

        String key = getUserKey();
        if (tvKeyText != null) tvKeyText.setText(key != null ? key : "");

        if (key == null || key.equals("INVALID") || key.trim().isEmpty()) {
            showError("INVALID LICENCE KEY");
            return;
        }

        new Thread(() -> {
            try {
                String result = Login.check(MainActivity.this, key);
                runOnUiThread(() -> {
                    if (result.equals("OK")) {
                        SharedPreferences prefs2 = getSharedPreferences("auth_pref", MODE_PRIVATE);
                        prefs2.edit()
                            .putBoolean("verified", true)
                            .putLong("expiry", (System.currentTimeMillis() / 1000) + 86400)
                            .apply();

                        if (tvStatusText != null) tvStatusText.setText("VERIFIED");
                        if (tvStatusDetail != null) tvStatusDetail.setText("Licence confirmed");

                        handler.postDelayed(() -> {
                            safeHide(progressContainer);
                            safeHide(statusContainer);
                            safeHide(deviceInfoContainer);
                            safeHide(keyContainer);
                            safeShow(successScreen);
                            handler.postDelayed(() -> {
                                try { if (verifyDialog != null) verifyDialog.dismiss(); } catch (Exception ignored) {}
                                onVerified();
                            }, 800);
                        }, 500);
                    } else {
                        showError(result);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> showError(e.getMessage() != null ? e.getMessage() : "Error"));
            }
        }).start();
    }

    private void onVerified() {
        if (verified) return;
        verified = true;
        recyclerView.setVisibility(View.VISIBLE);
        doInitRecycler();
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            safeHide(progressContainer);
            safeHide(statusContainer);
            safeHide(deviceInfoContainer);
            safeHide(keyContainer);
            if (tvErrorMessage != null) tvErrorMessage.setText(message != null ? message : "Error");
            safeShow(errorScreen);
            handler.postDelayed(() -> {
                try { if (verifyDialog != null) verifyDialog.dismiss(); } catch (Exception ignored) {}
                toast("Licence Error: " + message);
                LoginActivity.goLogin(MainActivity.this);
                finish();
            }, 2000);
        });
    }

    private void safeHide(View v) { if (v != null) v.setVisibility(View.GONE); }
    private void safeShow(View v) { if (v != null) v.setVisibility(View.VISIBLE); }

    private void loadDeviceInfo() {
        runOnUiThread(() -> {
            if (tvDeviceInfo != null)  tvDeviceInfo.setText("Device: " + Build.MANUFACTURER + " " + Build.MODEL);
            if (tvAndroidInfo != null) tvAndroidInfo.setText("Android: " + Build.VERSION.RELEASE);
            if (tvSDKInfo != null)     tvSDKInfo.setText("SDK: " + Build.VERSION.SDK_INT);
        });
    }

    private String getUserKey() {
        if (LoginActivity.USERKEY != null && !LoginActivity.USERKEY.isEmpty())
            return LoginActivity.USERKEY;
        try {
            SharedPreferences p = getSharedPreferences("FPrefs", MODE_PRIVATE);
            String key = p.getString("USER", "");
            if (!key.isEmpty()) {
                LoginActivity.USERKEY = key;
                return key;
            }
        } catch (Exception ignored) {}
        return "INVALID";
    }

    public LinearProgressIndicator getProgresBar() { return progress; }

    public void doShowProgress(boolean indeterminate) {
        if (progress == null) return;
        runOnUiThread(() -> {
            progress.setVisibility(View.VISIBLE);
            progress.setIndeterminate(indeterminate);
        });
    }

    public void doHideProgress() {
        if (progress == null) return;
        runOnUiThread(() -> progress.setVisibility(View.GONE));
    }

    private void hideSystemUI() {
        View dv = getWindow().getDecorView();
        dv.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    public void startPatcher() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 123);
            } else {
                startService(new Intent(MainActivity.get(), FloatLogo.class));
                loadAssets();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) handler.removeCallbacksAndMessages(null);
        try { if (verifyDialog != null && verifyDialog.isShowing()) verifyDialog.dismiss(); }
        catch (Exception ignored) {}
    }
}