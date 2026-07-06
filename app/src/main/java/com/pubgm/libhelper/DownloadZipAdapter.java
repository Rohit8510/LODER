package com.pubgm.libhelper;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;

import java.io.IOException;
import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.lsposed.lsparanoid.Obfuscate;

@Obfuscate
public class DownloadZipAdapter {

    public interface DownloadCallback {
        void onDownloadComplete(boolean success, String message);
        void onProgress(int progress);
    }

    private final Context context;
    private final ExecutorService executor;
    private final Handler handler;
    private String zipFileName = "Saved.zip";
    private DownloadCallback callback;
    private Dialog downloadDialog;
    private ProgressBar progressBar;
    private TextView progressText;
    private TextView statusText;
    private TextView titleText;
    private boolean downloadCancelled = false;

    public DownloadZipAdapter(Context context) {
        this.context = context;
        executor = Executors.newSingleThreadExecutor();
        handler = new Handler(Looper.getMainLooper());
    }

    public void setZipFileName(String fileName) {
        this.zipFileName = fileName;
    }

    public void startDownload(String downloadUrl, DownloadCallback callback) {
        this.callback = callback;
        
        handler.post(() -> {
            showDownloadDialog();

            executor.execute(() -> {
                boolean success = downloadFile(downloadUrl);

                handler.post(() -> {
                    if (success && !downloadCancelled) {
                        boolean processSuccess = processDownloadedFile();
                        if (processSuccess) {
                            updateUIComplete();
                        } else {
                            dismissDialog();
                            Toast.makeText(context, "Extraction failed", Toast.LENGTH_LONG).show();
                            if (callback != null) {
                                callback.onDownloadComplete(false, "Extraction failed");
                            }
                        }
                    } else if (!downloadCancelled) {
                        dismissDialog();
                        Toast.makeText(context, "Download failed", Toast.LENGTH_LONG).show();
                        if (callback != null) {
                            callback.onDownloadComplete(false, "Download failed");
                        }
                    }
                });
            });
        });
    }

    private boolean downloadFile(String downloadUrl) {
        File outputFile = new File(context.getFilesDir(), zipFileName);

        try {
            URL url = new URL(downloadUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return false;
            }

            int fileLength = connection.getContentLength();
            try (InputStream input = connection.getInputStream();
                 FileOutputStream output = new FileOutputStream(outputFile)) {

                byte[] data = new byte[4096];
                long total = 0;
                int count;

                while ((count = input.read(data)) != -1) {
                    if (downloadCancelled) {
                        return false;
                    }
                    total += count;
                    if (fileLength > 0) {
                        final int progress = (int) (total * 100 / fileLength);
                        updateProgress(progress);
                        if (callback != null) {
                            callback.onProgress(progress);
                        }
                    }
                    output.write(data, 0, count);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void updateProgress(final int progress) {
        handler.post(() -> {
            if (downloadDialog != null && downloadDialog.isShowing()) {
                try {
                    if (progressBar != null) {
                        progressBar.setProgress(progress);
                    }
                    if (progressText != null) {
                        progressText.setText(progress + "%");
                    }
                    if (statusText != null) {
                        statusText.setText(getStatusMessage(progress));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private String getStatusMessage(int progress) {
        if (progress < 10) return "Starting download...";
        else if (progress < 30) return "Connecting to server...";
        else if (progress < 60) return "Downloading files...";
        else if (progress < 85) return "Almost there...";
        else if (progress < 100) return "Finishing download...";
        return "Processing...";
    }

    private void updateUIComplete() {
        handler.post(() -> {
            if (downloadDialog != null && downloadDialog.isShowing()) {
                try {
                    if (statusText != null) {
                        statusText.setText("Download Complete!");
                    }
                    if (progressBar != null) {
                        progressBar.setProgress(100);
                    }
                    if (progressText != null) {
                        progressText.setText("100%");
                    }
                    if (titleText != null) {
                        titleText.setText("SUCCESS!");
                        titleText.setTextColor(Color.GREEN);
                    }
                    
                    handler.postDelayed(() -> {
                        dismissDialog();
                        Toast.makeText(context, "Download successful!", Toast.LENGTH_LONG).show();
                        if (callback != null) {
                            callback.onDownloadComplete(true, "Download successful");
                        }
                    }, 2000);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onDownloadComplete(false, e.getMessage());
                    }
                }
            }
        });
    }

    private void showDownloadDialog() {
        try {
            downloadDialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
            downloadDialog.setCancelable(false);
            downloadDialog.setCanceledOnTouchOutside(false);

            if (downloadDialog.getWindow() != null) {
                downloadDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }

            // Create custom layout programmatically
            LinearLayout mainLayout = new LinearLayout(context);
            mainLayout.setOrientation(LinearLayout.VERTICAL);
            mainLayout.setBackgroundResource(android.R.drawable.screen_background_light_transparent);
            mainLayout.setBackgroundColor(Color.parseColor("#99000000"));
            mainLayout.setPadding(dp(20), dp(20), dp(20), dp(20));

            // Card Layout
            LinearLayout cardLayout = new LinearLayout(context);
            cardLayout.setOrientation(LinearLayout.VERTICAL);
            cardLayout.setBackgroundColor(Color.BLACK);
            cardLayout.setPadding(dp(16), dp(16), dp(16), dp(16));
            
            // LayoutParams for card
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                dp(250), 
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardLayout.setLayoutParams(cardParams);

            // Title
            titleText = new TextView(context);
            titleText.setText("DOWNLOADING");
            titleText.setTextColor(Color.parseColor("#4CAF50"));
            titleText.setTextSize(16);
            titleText.setGravity(Gravity.CENTER);
            titleText.setPadding(0, 0, 0, dp(10));
            cardLayout.addView(titleText);

            // Divider
            View divider = new View(context);
            divider.setBackgroundColor(Color.parseColor("#4CAF50"));
            LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(1)
            );
            dividerParams.setMargins(0, 0, 0, dp(15));
            divider.setLayoutParams(dividerParams);
            cardLayout.addView(divider);

            // Device Info
            TextView deviceInfo = new TextView(context);
            deviceInfo.setText(getDeviceInfo());
            deviceInfo.setTextColor(Color.WHITE);
            deviceInfo.setTextSize(11);
            deviceInfo.setBackgroundColor(Color.parseColor("#1A4CAF50"));
            deviceInfo.setPadding(dp(8), dp(8), dp(8), dp(8));
            LinearLayout.LayoutParams deviceParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            deviceParams.setMargins(0, 0, 0, dp(15));
            deviceInfo.setLayoutParams(deviceParams);
            cardLayout.addView(deviceInfo);

            // Progress Container
            LinearLayout progressContainer = new LinearLayout(context);
            progressContainer.setOrientation(LinearLayout.VERTICAL);
            progressContainer.setBackgroundColor(Color.parseColor("#1A4CAF50"));
            progressContainer.setPadding(dp(12), dp(12), dp(12), dp(12));
            
            // Progress Bar
            progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
            progressBar.setMax(100);
            progressBar.setProgress(0);
            progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50")));
            LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(20)
            );
            progressBar.setLayoutParams(progressParams);
            progressContainer.addView(progressBar);

            // Progress Text
            progressText = new TextView(context);
            progressText.setText("0%");
            progressText.setTextColor(Color.parseColor("#4CAF50"));
            progressText.setTextSize(18);
            progressText.setGravity(Gravity.CENTER);
            progressText.setPadding(0, dp(5), 0, 0);
            progressContainer.addView(progressText);

            // Status Text
            statusText = new TextView(context);
            statusText.setText("Initializing...");
            statusText.setTextColor(Color.WHITE);
            statusText.setTextSize(12);
            statusText.setGravity(Gravity.CENTER);
            statusText.setPadding(0, dp(5), 0, 0);
            progressContainer.addView(statusText);

            cardLayout.addView(progressContainer);

            mainLayout.addView(cardLayout);
            downloadDialog.setContentView(mainLayout);
            downloadDialog.show();

            Window window = downloadDialog.getWindow();
            if (window != null) {
                WindowManager.LayoutParams layoutParams = window.getAttributes();
                layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                layoutParams.gravity = Gravity.CENTER;
                layoutParams.dimAmount = 0.6f;
                window.setAttributes(layoutParams);
                window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error showing dialog", Toast.LENGTH_SHORT).show();
        }
    }

    private String getDeviceInfo() {
        return "Device: " + Build.MANUFACTURER + " " + Build.MODEL + "\n" + 
               "Android: " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")";
    }

    private int dp(int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density);
    }

    private void dismissDialog() {
        try {
            if (downloadDialog != null && downloadDialog.isShowing()) {
                downloadDialog.dismiss();
                downloadDialog = null;
                progressBar = null;
                progressText = null;
                statusText = null;
                titleText = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean processDownloadedFile() {
        File zipFile = new File(context.getFilesDir(), zipFileName);
        String zipPath = zipFile.getAbsolutePath();
        String outputDir = context.getFilesDir().getAbsolutePath();
        String password = "0000";

        if (unzipEncrypted(zipPath, outputDir, password)) {
            moveSoFiles(new File(outputDir, "loader"));
            zipFile.delete();
            return true;
        } else {
            return false;
        }
    }

    private boolean unzipEncrypted(String zipPath, String outputDir, String password) {
        try {
            new ZipFile(zipPath, password.toCharArray()).extractAll(outputDir);
            setPermissions(new File(outputDir));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void moveSoFiles(File loaderFolder) {
        File outputDir = context.getFilesDir();
        if (!loaderFolder.exists()) loaderFolder.mkdirs();
        File[] files = outputDir.listFiles((dir, name) -> name.endsWith(".so"));
        if (files != null) {
            for (File soFile : files) {
                try {
                    Files.move(soFile.toPath(), new File(loaderFolder, soFile.getName()).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setPermissions(File fileOrDir) {
        try {
            fileOrDir.setExecutable(true, false);
            fileOrDir.setReadable(true, false);
            fileOrDir.setWritable(true, false);
            if (fileOrDir.isDirectory()) {
                File[] children = fileOrDir.listFiles();
                if (children != null) {
                    for (File child : children) {
                        setPermissions(child);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cancelDownload() {
        downloadCancelled = true;
        dismissDialog();
    }
}