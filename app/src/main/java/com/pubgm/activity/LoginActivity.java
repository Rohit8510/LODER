package com.pubgm.activity;

import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;        // ✅ ADD THIS
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.pubgm.Login;
import com.pubgm.R;
import com.pubgm.license.EliteResetPage;
import com.pubgm.utils.ActivityCompat;
import com.pubgm.utils.FLog;
import org.lsposed.lsparanoid.Obfuscate;

@Obfuscate
public class LoginActivity extends ActivityCompat {

    private static final String USER = "USER";
    public static String USERKEY;

    private Button btnSignIn;
    private TextView versionNameTv;
    private RelativeLayout webViewLayout;
    private EditText textUsername;
    private ImageView showPwd, hidePwd;
    private ImageView pasteBtn;
    private ImageView telegramBtn;
    private ScrollView layLogin;

    private EliteResetPage sboxResetPage;
    private Dialog customProgressDialog;
    private Dialog customErrorDialog;

    public static void goLogin(Context context) {
        try {
            Intent intent = new Intent(context, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            FLog.error("goLogin failed: " + e.getMessage());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_login);
        } catch (Exception e) {
            FLog.error("Login layout inflate failed: " + e.getMessage());
            Toast.makeText(this, "Layout error", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        isLogin = true;
        initializeUI();
        loadSavedKey();
        initializeEliteResetPage();
    }

    private void initializeUI() {
        try {
            layLogin = findViewById(R.id.lay_login);
            btnSignIn = findViewById(R.id.btnSignIn);
            textUsername = findViewById(R.id.textUsername);
            showPwd = findViewById(R.id.show_pwd);
            hidePwd = findViewById(R.id.vis_pwd);
            pasteBtn = findViewById(R.id.paste);
            telegramBtn = findViewById(R.id.telegram_opne);
            versionNameTv = findViewById(R.id.VERSION_NAME);
            webViewLayout = findViewById(R.id.webViewLayout);

            setVersionName();
            setupButtonListeners();
            applyAnimations();
        } catch (Exception e) {
            FLog.error("initializeUI error: " + e.getMessage());
            Toast.makeText(this, "UI init error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupButtonListeners() {
        try {
            if (btnSignIn != null) {
                btnSignIn.setOnClickListener(v -> {
                    Toast.makeText(this, "Sign In clicked!", Toast.LENGTH_SHORT).show();
                    handleLoginClick();
                });
            }
            
            if (pasteBtn != null) {
                pasteBtn.setOnClickListener(v -> {
                    Toast.makeText(this, "Paste clicked!", Toast.LENGTH_SHORT).show();
                    pasteFromClipboard();
                });
            }
            
            if (hidePwd != null) {
                hidePwd.setOnClickListener(v -> togglePasswordVisibility(true));
            }
            
            if (showPwd != null) {
                showPwd.setOnClickListener(v -> togglePasswordVisibility(false));
            }
            
            if (telegramBtn != null) {
                telegramBtn.setOnClickListener(v -> openTelegram());
            }
            
            View showWebView = findViewById(R.id.ShowWebView);
            if (showWebView != null) {
                showWebView.setOnClickListener(v -> {
                    Toast.makeText(this, "Reset License clicked!", Toast.LENGTH_SHORT).show();
                    showResetLicensePage();
                });
            }
            
            View closeButton = findViewById(R.id.close_button);
            if (closeButton != null) {
                closeButton.setOnClickListener(v -> hideWebView());
            }
        } catch (Exception e) {
            FLog.error("setupButtonListeners error: " + e.getMessage());
        }
    }

    private void initializeEliteResetPage() {
        try {
            sboxResetPage = new EliteResetPage(this, new EliteResetPage.Callback() {
                @Override
                public void onResetDone(String key) {
                    runOnUiThread(() -> {
                        if (textUsername != null) textUsername.setText(key);
                        prefs.write(USER, key);
                        USERKEY = key;
                        showToast("✅ License reset successful!");
                        hideWebView();
                    });
                }
                @Override
                public void onClose() { runOnUiThread(() -> hideWebView()); }
                @Override
                public void onError(String message) {
                    runOnUiThread(() -> {
                        showToast("Error: " + message);
                        showMaterialErrorDialog("Reset Error", message);
                    });
                }
            });
        } catch (Exception e) {
            FLog.error("initializeEliteResetPage error: " + e.getMessage());
        }
    }

    private void setVersionName() {
        try {
            if (versionNameTv == null) return;
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionNameTv.setText("v" + pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            if (versionNameTv != null) versionNameTv.setText("v1.0.0");
        }
    }

    private void applyAnimations() {
        try {
            LinearLayout loginLayout = findViewById(R.id.loginbtn);
            if (loginLayout != null) {
                Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
                loginLayout.startAnimation(slideUp);
            }

            LinearLayout inputLayout = findViewById(R.id.animeson_enter_key);
            if (inputLayout != null) {
                Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
                inputLayout.startAnimation(fadeIn);
            }

            ImageView logo = findViewById(R.id.jkpapa_ka_loda);
            if (logo != null) {
                Animation scaleIn = AnimationUtils.loadAnimation(this, R.anim.scale_in);
                logo.startAnimation(scaleIn);
            }
        } catch (Exception ignored) { }
    }

    private void loadSavedKey() {
        try {
            if (textUsername == null) return;
            String savedKey = prefs.read(USER, "");
            if (!savedKey.isEmpty()) {
                textUsername.setText(savedKey);
            }
        } catch (Exception e) {
            FLog.error("loadSavedKey error: " + e.getMessage());
        }
    }

    private void handleLoginClick() {
        if (textUsername == null) {
            showToast("UI not initialized");
            return;
        }
        
        String userKey = textUsername.getText().toString().trim();
        
        if (userKey.isEmpty()) {
            textUsername.setError("Please enter license key");
            showToast("Please enter license key");
            return;
        }
        
        prefs.write(USER, userKey);
        USERKEY = userKey;
        performLogin(userKey);
    }

    private void pasteFromClipboard() {
        try {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (clipboard != null && clipboard.hasPrimaryClip()) {
                String copiedText = clipboard.getPrimaryClip().getItemAt(0).getText().toString();
                if (textUsername != null) {
                    textUsername.setText(copiedText);
                    showToast("Key pasted");
                }
            } else {
                showToast("Clipboard is empty");
            }
        } catch (Exception e) {
            showToast("Failed to paste");
        }
    }

    private void togglePasswordVisibility(boolean show) {
        if (textUsername == null) return;
        try {
            if (show) {
                textUsername.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                if (showPwd != null) showPwd.setVisibility(View.VISIBLE);
                if (hidePwd != null) hidePwd.setVisibility(View.GONE);
            } else {
                textUsername.setTransformationMethod(PasswordTransformationMethod.getInstance());
                if (showPwd != null) showPwd.setVisibility(View.GONE);
                if (hidePwd != null) hidePwd.setVisibility(View.VISIBLE);
            }
            textUsername.setSelection(textUsername.getText().length());
        } catch (Exception e) {
            FLog.error("togglePasswordVisibility error: " + e.getMessage());
        }
    }

    private void openTelegram() {
        try {
            showToast("Opening Telegram...");
        } catch (Exception e) {
            showToast("Cannot open Telegram");
        }
    }

    private void showResetLicensePage() {
        if (layLogin != null) layLogin.setVisibility(View.GONE);
        if (webViewLayout != null) webViewLayout.setVisibility(View.VISIBLE);
        if (sboxResetPage != null && textUsername != null) {
            sboxResetPage.open(textUsername.getText().toString().trim());
        }
    }

    private void hideWebView() {
        if (webViewLayout != null) webViewLayout.setVisibility(View.GONE);
        if (layLogin != null) layLogin.setVisibility(View.VISIBLE);
    }

    private void showReset() {
        if (layLogin != null) layLogin.setVisibility(View.GONE);
        if (webViewLayout != null) webViewLayout.setVisibility(View.VISIBLE);
        if (sboxResetPage != null && textUsername != null) {
            sboxResetPage.open(textUsername.getText().toString());
        }
    }

    private void hideReset() {
        if (webViewLayout != null) webViewLayout.setVisibility(View.GONE);
        if (layLogin != null) layLogin.setVisibility(View.VISIBLE);
    }

    private void showMaterialErrorDialog(String errorMessage) {
        showMaterialErrorDialog("Error", errorMessage);
    }

    private void showMaterialErrorDialog(String title, String errorMessage) {
        dismissErrorDialog();

        try {
            LayoutInflater inflater = LayoutInflater.from(this);
            View customView = inflater.inflate(R.layout.dialog_progress, null);

            CircularProgressIndicator progressBar = customView.findViewById(R.id.progress_circular);
            if (progressBar != null) progressBar.setVisibility(View.GONE);

            TextView progressText = customView.findViewById(R.id.progress_text);
            if (progressText != null) {
                progressText.setTextColor(Color.parseColor("#FF4444"));
                progressText.setText(errorMessage != null ? errorMessage : "Unknown error");
            }

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setTitle(title);
            builder.setView(customView);
            builder.setCancelable(true);
            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

            customErrorDialog = builder.show();
            if (customErrorDialog.getWindow() != null) {
                customErrorDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        } catch (Exception e) {
            FLog.error("Error dialog inflation failed: " + e.getMessage());
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    private void dismissErrorDialog() {
        if (customErrorDialog != null && customErrorDialog.isShowing()) {
            customErrorDialog.dismiss();
            customErrorDialog = null;
        }
    }

    private void performLogin(String userKey) {
        try {
            View progressView = LayoutInflater.from(this).inflate(R.layout.dialog_progress, null);
            CircularProgressIndicator progressBar = progressView.findViewById(R.id.progress_circular);
            if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
            TextView progressText = progressView.findViewById(R.id.progress_text);
            if (progressText != null) progressText.setText("Checking license...\nPlease wait...");

            customProgressDialog = new MaterialAlertDialogBuilder(this)
                    .setView(progressView)
                    .setCancelable(false)
                    .create();
            if (customProgressDialog.getWindow() != null) {
                customProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
            customProgressDialog.show();
        } catch (Exception e) {
            FLog.error("Progress dialog failed: " + e.getMessage());
            Toast.makeText(this, "Please wait...", Toast.LENGTH_SHORT).show();
        }

        final Handler mainHandler = new Handler(Looper.getMainLooper());

        new Thread(() -> {
            if (isFinishing()) {
                mainHandler.post(this::dismissProgressDialog);
                return;
            }

            String result;
            try {
                result = Login.check(LoginActivity.this, userKey);
            } catch (Exception e) {
                FLog.error("Login.check threw exception: " + e.getMessage());
                result = "Login check failed: " + e.getMessage();
            }

            final String finalResult = result;
            mainHandler.post(() -> {
                dismissProgressDialog();
                if (isFinishing()) return;

                if ("OK".equals(finalResult)) {
                    MainActivity.goMain(LoginActivity.this);
                    toast("Login Success");
                    finishActivity(0);
                } else {
                    showMaterialErrorDialog(finalResult);
                    toast("Login Failed");
                }
            });
        }).start();
    }

    private void dismissProgressDialog() {
        if (customProgressDialog != null && customProgressDialog.isShowing()) {
            customProgressDialog.dismiss();
            customProgressDialog = null;
        }
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (webViewLayout != null && webViewLayout.getVisibility() == View.VISIBLE) {
            hideReset();
        } else {
            super.onBackPressed();
        }
    }
}