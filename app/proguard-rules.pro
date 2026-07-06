# ==================== PUBGM FLOATING FIX - COMPLETE PROGUARD ====================
# This file contains all necessary rules to prevent crashes in release build

# ===== BASIC ANDROID RULES =====
-dontoptimize
-ignorewarnings
-dontwarn

# ===== NATIVE METHODS CRITICAL FIX =====
# Keep all native methods with exact names
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep all classes with native methods
-keepclasseswithmembers class * {
    native <methods>;
}

# ===== OVERLAY PACKAGE COMPLETE PRESERVE =====
-keep class com.pubgm.floating.** {
    *;
}

-keep interface com.pubgm.floating.** {
    *;
}

-keep enum com.pubgm.floating.** {
    *;
}

# ===== SPECIFIC OVERLAY CLASS RULES =====
-keep class com.pubgm.floating.Overlay {
    public protected private *;
    native boolean getReady();
    native void Close();
    public static native void DrawOn(com.pubgm.floating.ESPView, android.graphics.Canvas);
    void onCreate();
    void onDestroy();
    void DrawCanvas();
    void Start();
    void StartDaemon();
    void Shell(java.lang.String);
}

-keepclassmembers class com.pubgm.floating.Overlay {
    native boolean getReady();
    native void Close();
    public static native void DrawOn(com.pubgm.floating.ESPView, android.graphics.Canvas);
    *** getPref();
    *** ctx;
}

# ===== FLOATLOGO CLASS RULES =====
-keep class com.pubgm.floating.FloatLogo {
    public protected private *;
    native void SettingValue(int, boolean);
    native void SettingMemory(int, boolean);
    native void SettingAim(int, boolean);
    native void RadarSize(int);
    native void Range(int);
    native void Target(int);
    native void AimBy(int);
    native void AimWhen(int);
    native void distances(int);
    native void WideView(int);
    native void recoil(int);
    void Init();
    void createOver();
    void DrawESP();
    void StopESP();
    void Execute(java.lang.String);
    void Shell(java.lang.String);
}

-keepclassmembers class com.pubgm.floating.FloatLogo {
    native void SettingValue(int, boolean);
    native void SettingMemory(int, boolean);
    native void SettingAim(int, boolean);
    native void RadarSize(int);
    native void Range(int);
    native void Target(int);
    native void AimBy(int);
    native void AimWhen(int);
    native void distances(int);
    native void WideView(int);
    native void recoil(int);
}

# ===== ESPVIEW CLASS RULES =====
-keep class com.pubgm.floating.ESPView {
    public protected private *;
    void Draw*(...);
    void ClearCanvas(android.graphics.Canvas);
    void InitializePaints();
    void run();
    protected void onDraw(android.graphics.Canvas);
    *** getItemName(java.lang.String);
    *** getVehicleName(java.lang.String);
    *** getWeapon(int);
}

-keepclassmembers class com.pubgm.floating.ESPView {
    *** mStrokePaint;
    *** mFPSText;
    *** mItemsPaint;
    *** mFilledPaint;
    *** mLootBoxPaint;
    *** mMDText;
    *** mTextPaint;
    *** mVehiclesPaint;
    *** mTextPainti;
    *** mNamePaint;
    *** mFillPaint;
    *** mThread;
    *** FPS;
    *** sleepTime;
}

# ===== TOGGLE CLASSES RULES =====
-keep class com.pubgm.floating.ToggleAim { *; }
-keep class com.pubgm.floating.ToggleBullet { *; }
-keep class com.pubgm.floating.ToggleSimulation { *; }
-keep class com.pubgm.floating.HideRecorder { 
    public static void setFakeRecorderWindowLayoutParams(android.view.WindowManager$LayoutParams);
}

# ===== SINGLETAPCONFIRM RULES =====
-keep class com.pubgm.floating.SingleTapConfirm { *; }

# ===== JNI / NATIVE LIBRARY RULES =====
-keep class com.pubgm.** {
    native <methods>;
}

# ===== REFLECTION SAFE RULES =====
-keep class me.weishu.reflection.** { *; }
-keep class com.github.tiann.** { *; }
-keep class top.niunaijun.** { *; }
-keep class black.** { *; }

# ===== APPLICATION ENTRY =====
-keep class com.pubgm.BuildConfig { *; }
-keep class com.pubgm.BoxApplication { *; }

# ===== LOGIN METHOD SAFE =====
-keep class com.pubgm.Login {
    public *;
    private *;
    protected *;
}

-keepclassmembers class com.pubgm.Login {
    native <methods>;
    java.lang.String check(...);
    java.lang.String sendHttpRequest(...);
    java.lang.String getMD5(...);
    java.lang.String getAndroidID(...);
    java.lang.String getDeviceModel(...);
    java.lang.String getDeviceBrand(...);
    java.lang.String getUUID(...);
}

-keepnames class com.pubgm.Login

# ===== RESET PAGE =====
-keep class com.pubgm.license.EliteResetPage { *; }
-keep class com.pubgm.license.EliteResetPage$* { *; }

# ===== ACTIVITIES / LIFECYCLE =====
-keep class * extends android.app.Activity { *; }
-keep class * extends android.app.Service { *; }
-keep class * extends android.content.BroadcastReceiver { *; }
-keep class * extends android.app.Application { *; }

# ===== ANDROIDX AND MATERIAL =====
-keep class androidx.** { *; }
-keep class com.google.android.material.** { *; }
-keep class androidx.appcompat.widget.SwitchCompat { *; }

# ===== UTILS PACKAGE =====
-keep class com.pubgm.utils.** { *; }
-keep class com.pubgm.utils.**$* { *; }
-keep class com.pubgm.utils.Animations {
    public static *** flash(...);
    public static *** animateClick(...);
    public static *** pulse(...);
    public static *** animateTextColor(...);
    public static *** successAnimation(...);
    public static *** buttonPressEffect(...);
    public static *** buttonReleaseEffect(...);
    public static int getColor(java.lang.String);
}

-keep class com.pubgm.utils.FLog { *; }
-keep class com.pubgm.utils.FPrefs { *; }

# ===== ADAPTER PACKAGE =====
-keep class com.pubgm.adapter.** { *; }
-keep class com.pubgm.adapter.**$* { *; }
-keep class com.pubgm.adapter.RecyclerViewAdapter { *; }
-keep class com.pubgm.adapter.RecyclerViewAdapter$* { *; }
-keepnames class com.pubgm.adapter.RecyclerViewAdapter

# ===== OTHER PROJECT PACKAGES =====
-keep class com.pubgm.activity.** { *; }
-keep class com.pubgm.libhelper.** { *; }
-keep class com.pubgm.ifc.** { *; }

# ===== THIRD PARTY LIBS =====
-keep class org.jdeferred.** { *; }
-keep class com.topjohnwu.** { *; }
-keep class net.lingala.zip4j.** { *; }
-keep class org.slf4j.** { *; }

# ===== NETWORK SAFE =====
-keep class java.net.HttpURLConnection { *; }
-keep class java.net.URL { *; }
-keep class javax.net.ssl.** { *; }
-dontwarn java.net.**
-dontwarn javax.net.ssl.**

# ===== PARCELABLE =====
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# ===== SERIALIZABLE =====
-keep class * implements java.io.Serializable { *; }

# ===== VIEWS AND WIDGETS =====
-keep class android.widget.** { *; }
-keep class android.view.** { *; }
-keep class android.graphics.** { *; }

# ===== WINDOW MANAGER =====
-keep class android.view.WindowManager { *; }
-keep class android.view.WindowManager$LayoutParams { *; }

# ===== CANVAS DRAWING =====
-keep class android.graphics.Canvas { *; }
-keep class android.graphics.Paint { *; }
-keep class android.graphics.Path { *; }
-keep class android.graphics.Rect { *; }
-keep class android.graphics.PorterDuff { *; }
-keep class android.graphics.PorterDuff$Mode { *; }
-keep class android.graphics.drawable.GradientDrawable { *; }
-keep class android.graphics.Typeface { *; }

# ===== THREADING =====
-keep class java.lang.Thread { *; }
-keep class java.lang.Runnable { *; }

# ===== DATE/TIME =====
-keep class java.text.SimpleDateFormat { *; }
-keep class java.util.Date { *; }
-keep class java.util.Locale { *; }

# ===== COLLECTIONS =====
-keep class java.util.HashMap { *; }
-keep class java.util.Map { *; }
-keep class java.util.Random { *; }

# ===== SYSTEM =====
-keep class java.lang.System { *; }
-keep class android.os.SystemClock { *; }
-keep class android.os.Process { *; }

# ===== SHARED PREFERENCES =====
-keep class android.content.SharedPreferences { *; }
-keep class android.content.SharedPreferences$Editor { *; }

# ===== GESTURE DETECTOR =====
-keep class android.view.GestureDetector { *; }
-keep class android.view.GestureDetector$SimpleOnGestureListener { *; }

# ===== KEEP ANNOTATIONS =====
-keep @androidx.annotation.Keep class * { *; }
-keep @interface androidx.annotation.Keep
-keep @interface com.pubgm.utils.Keep

# ===== ATTRIBUTES =====
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes SourceFile,LineNumberTable

# ===== RESOURCE KEEP =====
-keep class **.R$* { *; }
-keep class **.R

# ===== CONSTRUCTORS =====
-keepclasseswithmembers class * {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}