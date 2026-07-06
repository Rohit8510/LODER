// ---------- Login.java ----------
package com.pubgm;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.provider.Settings;
import android.util.Base64;

import com.pubgm.utils.FLog;

import org.json.JSONObject;
import org.lsposed.lsparanoid.Obfuscate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.NetworkInterface;
import java.net.URL;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

@Obfuscate
public class Login {

    // ── FIX 1: Native lib loaded safely — no crash if missing ────────────
    private static boolean sLibLoaded = false;

    static {
        try {
            System.loadLibrary("client");
            sLibLoaded = true;
        } catch (UnsatisfiedLinkError w) {
            sLibLoaded = false;
            FLog.error("client lib load failed: " + w.getMessage());
        }
    }

    public static native ArrayList<String> getheaders();
    public static native String getbaseurl();
    public static native void setAuth(String token, String auth);
    public static native void setExpire(String exp);
    public static native String FixCrash();

    // ── FIX 2: All globals reset before each login attempt ───────────────
    private static String  g_Token = "";
    private static String  g_Auth  = "";
    private static boolean bValid  = false;
    private static String  EXP     = "";
    private static long    rng     = 0;
    private static int     retry   = 0;

    // ── FIX 3: FixCrash() called safely — never at field level ──────────
    public static String safeFixCrash() {
        if (!sLibLoaded) return null;
        try {
            return FixCrash();
        } catch (Exception e) {
            FLog.error("FixCrash error: " + e.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    //  RESET — called at start of every check() to prevent
    //  stale bValid=true leaking into new login attempts
    // ─────────────────────────────────────────────────────────────────────
    private static void resetState() {
        bValid  = false;
        g_Token = "";
        g_Auth  = "";
        EXP     = "";
        rng     = 0;
        retry   = 0;
    }

    public static String getAndroidID(Context context) {
        try {
            return Settings.Secure.getString(
                context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) { return "unknown"; }
    }

    public static String getDeviceModel() {
        try { return Build.MODEL; } catch (Exception e) { return "unknown"; }
    }

    public static String getDeviceBrand() {
        try { return Build.BRAND; } catch (Exception e) { return "unknown"; }
    }

    public static String getUUID(String hwid) {
        try {
            return UUID.nameUUIDFromBytes(hwid.getBytes()).toString();
        } catch (Exception e) { return "unknown"; }
    }

    // ── VPN / proxy check ────────────────────────────────────────────────
    private static boolean isUnsafeNetwork(Context ctx) {
        try {
            ConnectivityManager cm = (ConnectivityManager)
                ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkCapabilities cap =
                cm.getNetworkCapabilities(cm.getActiveNetwork());
            if (cap != null && cap.hasTransport(NetworkCapabilities.TRANSPORT_VPN))
                return true;
            for (NetworkInterface ni :
                    Collections.list(NetworkInterface.getNetworkInterfaces())) {
                String n = ni.getName().toLowerCase();
                if (ni.isUp() && !ni.isLoopback() &&
                    (n.contains("tun") || n.contains("ppp") ||
                     n.contains("tap") || n.contains("vpn")))
                    return true;
            }
            if (System.getProperty("http.proxyHost") != null ||
                android.net.Proxy.getHost(ctx) != null) return true;
        } catch (Exception ignored) {}
        return false;
    }

    // ─────────────────────────────────────────────────────────────────────
    //  MAIN CHECK
    // ─────────────────────────────────────────────────────────────────────
    public static String check(Context context, String userKey) {

        // FIX 2: Always reset before new attempt
        resetState();

        // FIX 4: Guard against missing native lib
        if (!sLibLoaded) {
            return "Native library not loaded. Please reinstall the app.";
        }

        try {
            // VPN check
            while (isUnsafeNetwork(context)) {
                retry++;
                Thread.sleep(2000);
                if (retry > 15) {
                    return "Disable your VPN and Http Canary, or access will be blocked.";
                }
            }

            String androidId = getAndroidID(context);
            String model     = getDeviceModel();
            String brand     = getDeviceBrand();
            String hwid      = userKey + androidId + model + brand;
            String uuid      = getUUID(hwid);

            // FIX 5: NULL-safe native calls
            ArrayList<String> headerData = getheaders();
            if (headerData == null || headerData.size() < 12) {
                return "Initialization error. Please restart.";
            }

            String baseUrl      = getbaseurl();
            if (baseUrl == null || baseUrl.isEmpty()) {
                return "Server config error.";
            }

            String gameName    = headerData.get(8);
            String userKeyParam= headerData.get(9);
            String serialParam = headerData.get(10);
            String authSecret  = headerData.get(11);

            String postData = "game=" + gameName
                + "&" + userKeyParam + "=" + userKey
                + "&" + serialParam  + "=" + uuid;

            String response = sendHttpRequest(baseUrl, postData, headerData);
            if (response == null) {
                return "Connection failed. Check your internet.";
            }

            JSONObject result = new JSONObject(response);

            // FIX 6: NULL-safe JSON read for status
            boolean status = false;
            if (result.has("status")) {
                status = result.getBoolean("status");
            }

            if (!status) {
                // FIX 6: Try reason, message, msg in order
                if (result.has("reason"))  return result.getString("reason");
                if (result.has("message")) return result.getString("message");
                if (result.has("msg"))     return result.getString("msg");
                return "Key invalid or expired.";
            }

            // FIX 6: NULL-safe data reads
            if (!result.has("data")) return "Invalid server response.";
            JSONObject data = result.getJSONObject("data");

            g_Token = data.optString("token", "");
            EXP     = data.optString("EXP", "");
            rng     = data.optLong("rng", 0);

            if (rng + 30 > System.currentTimeMillis() / 1000) {
                String auth = gameName + "-" + userKey + "-" + uuid + "-" + authSecret;
                g_Auth = getMD5(auth);

                // FIX 7: Empty token guard
                if (!g_Token.isEmpty() && g_Token.equals(g_Auth)) {
                    bValid = true;
                    setAuth(g_Token, g_Auth);
                    setExpire(EXP);
                    return "OK";
                } else {
                    bValid = false;
                    return "Token mismatch. Contact support.";
                }
            }

            bValid = false;
            return "Session expired. Please try again.";

        } catch (Exception e) {
            bValid = false;
            FLog.error("Login.check error: " + e.getMessage());
            return "PLEASE WAIT";
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    //  HTTP REQUEST
    // ─────────────────────────────────────────────────────────────────────
    private static String sendHttpRequest(String urlStr, String postData,
                                           ArrayList<String> headerData) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setRequestProperty(headerData.get(0), headerData.get(1));
            conn.setRequestProperty(headerData.get(2), headerData.get(3));
            conn.setRequestProperty(headerData.get(4), headerData.get(5));
            conn.setRequestProperty(headerData.get(6), headerData.get(7));
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            os.write(postData.getBytes("utf-8"));
            os.close();

            if (conn.getResponseCode() == 200) {
                BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "utf-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line.trim());
                }
                br.close();
                return response.toString();
            }

        } catch (Exception e) {
            FLog.error("sendHttpRequest error: " + e.getMessage());
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────
    //  CRYPTO HELPERS
    // ─────────────────────────────────────────────────────────────────────
    private static SSLSocketFactory getPinnedFactory() throws Exception {
        TrustManager tm = new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] c, String a) {}
            public void checkServerTrusted(X509Certificate[] c, String a)
                    throws CertificateException {
                try { verifyPin(c[0]); }
                catch (Exception e) { throw new CertificateException(e); }
            }
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, new TrustManager[]{tm}, new SecureRandom());
        return ctx.getSocketFactory();
    }

    private static void verifyPin(X509Certificate cert) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(cert.getPublicKey().getEncoded());
        String pin = "sha256/" + Base64.encodeToString(digest, Base64.NO_WRAP);
    }

    private static String getMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { return ""; }
    }
}
