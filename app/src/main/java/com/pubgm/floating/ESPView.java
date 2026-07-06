package com.pubgm.floating;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.SystemClock;
import android.view.Surface;
import android.view.View;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import static com.pubgm.floating.Overlay.getConfig;
import java.util.Map;
import java.util.Random;

public class ESPView extends View implements Runnable {
    Paint mStrokePaint, mFPSText;
    Paint mItemsPaint, mFilledPaint, mLootBoxPaint;
    Paint mMDText, mTextPaint;
    Paint mVehiclesPaint, mTextPainti, mNamePaint, mFillPaint;
    Thread mThread;
    
    private float mScaleX = 1;
    private float mScaleY = 1;
    
    public static long sleepTime;
    private float mFPS = 0.0f;
    private float mFPSCounter = 0.0f;
    private long mFPSTime = 0;
    Date time;
    SimpleDateFormat formatter;
    int FPS = 60;
    public static void ChangeFps(int fps) {
        sleepTime = 1000 / fps;
    }

    private int lastItemColor = Color.WHITE;

    private final HashMap<String, Integer> itemColorMap = new HashMap<>();

    public ESPView(Context context) {
        super(context, null, 0);
        InitializePaints();
        setFocusableInTouchMode(false);
        setBackgroundColor(Color.TRANSPARENT);
        time = new Date();
        formatter = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        sleepTime = 1000 / FPS;
        mThread = new Thread(this);
        mThread.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (canvas == null) return;
        try {
            int rotation = getDisplay().getRotation();
            if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) {
                return;
            }
            if (!arePaintsInitialized()) {
                InitializePaints();
            }
            ClearCanvas(canvas);
            Overlay.DrawOn(this, canvas);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        while (mThread != null && mThread.isAlive() && !mThread.isInterrupted()) {
            try {
                long t1 = System.currentTimeMillis();
                postInvalidate();
                long td = System.currentTimeMillis() - t1;
                Thread.sleep(Math.max(Math.min(0, sleepTime - td), sleepTime));
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    /**
     * Check if all paint objects are properly initialized
     */
    private boolean arePaintsInitialized() {
        return mStrokePaint != null &&
			mFilledPaint != null &&
			mTextPaint != null &&
			mFillPaint != null &&
			mNamePaint != null &&
			mMDText != null &&
			mItemsPaint != null &&
			mTextPainti != null &&
			mLootBoxPaint != null &&
			mVehiclesPaint != null &&
			mFPSText != null;
    }

    public void InitializePaints() {
        // Initialize mStrokePaint
        if (mStrokePaint == null) {
            mStrokePaint = new Paint();
            mStrokePaint.setStyle(Paint.Style.STROKE);
            mStrokePaint.setAntiAlias(true);
            mStrokePaint.setColor(Color.rgb(0, 0, 0));
        }

        // Initialize mFilledPaint
        if (mFilledPaint == null) {
            mFilledPaint = new Paint();
            mFilledPaint.setStyle(Paint.Style.FILL);
            mFilledPaint.setAntiAlias(true);
            mFilledPaint.setColor(Color.rgb(0, 0, 0));
        }

        // Initialize mTextPaint
        if (mTextPaint == null) {
            mTextPaint = new Paint();
            mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mTextPaint.setAntiAlias(true);
            mTextPaint.setColor(Color.rgb(0, 0, 0));
            mTextPaint.setTextAlign(Paint.Align.CENTER);
            mTextPaint.setStrokeWidth(1.1f);
        }

        // Initialize mFillPaint
        if (mFillPaint == null) {
            mFillPaint = new Paint();
            mFillPaint.setStyle(Paint.Style.FILL);
            mFillPaint.setAntiAlias(true);
            mFillPaint.setColor(Color.rgb(0, 0, 0));
        }

        // Initialize mNamePaint
        if (mNamePaint == null) {
            mNamePaint = new Paint();
            mNamePaint.setStyle(Paint.Style.FILL);
            mNamePaint.setAntiAlias(true);
            mNamePaint.setColor(Color.rgb(0, 0, 0));
            mNamePaint.setTextAlign(Paint.Align.CENTER);
            mNamePaint.setTypeface(Typeface.DEFAULT);
        }

        // Initialize mMDText
        if (mMDText == null) {
            mMDText = new Paint();
            mMDText.setStyle(Paint.Style.FILL_AND_STROKE);
            mMDText.setAntiAlias(true);
            mMDText.setColor(Color.rgb(0, 0, 0));
            mMDText.setStrokeWidth(0.5f);
            mMDText.setTextAlign(Paint.Align.CENTER);
            mMDText.setShadowLayer(10, 1, 1, Color.rgb(1, 1, 1));
        }

        // Initialize mItemsPaint
        if (mItemsPaint == null) {
            mItemsPaint = new Paint();
            mItemsPaint.setAntiAlias(true);
            mItemsPaint.setTextAlign(Paint.Align.CENTER);
            mItemsPaint.setTypeface(Typeface.DEFAULT);
        }

        // Initialize mTextPainti
        if (mTextPainti == null) {
            mTextPainti = new Paint();
            mTextPainti.setStyle(Paint.Style.FILL);
            mTextPainti.setAntiAlias(true);
            mTextPainti.setColor(Color.rgb(0, 0, 0));
            mTextPainti.setTextAlign(Paint.Align.CENTER);
        }

        // Initialize mLootBoxPaint
        if (mLootBoxPaint == null) {
            mLootBoxPaint = new Paint();
            mLootBoxPaint.setAntiAlias(true);
            mLootBoxPaint.setTextAlign(Paint.Align.LEFT);
            mLootBoxPaint.setColor(Color.rgb(0, 0, 0));
            mLootBoxPaint.setTypeface(Typeface.DEFAULT);
            mLootBoxPaint.setDither(true);
        }

        // Initialize mVehiclesPaint
        if (mVehiclesPaint == null) {
            mVehiclesPaint = new Paint();
            mVehiclesPaint.setAntiAlias(true);
            mVehiclesPaint.setTextAlign(Paint.Align.CENTER);
            mVehiclesPaint.setTypeface(Typeface.DEFAULT);
        }

        // Initialize mFPSText - THIS IS NOW CORRECTLY INITIALIZED
        if (mFPSText == null) {
            mFPSText = new Paint();
            mFPSText.setStyle(Paint.Style.FILL_AND_STROKE);
            mFPSText.setAntiAlias(true);
            mFPSText.setColor(Color.rgb(255, 255, 255));
            mFPSText.setTextAlign(Paint.Align.CENTER);
            mFPSText.setStrokeWidth(1.1f);
        }
    }

    public void ClearCanvas(Canvas cvs) {
        if (cvs != null) {
            cvs.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        }
    }

    public void DrawLine(Canvas cvs, int a, int r, int g, int b, float lineWidth, float fromX, float fromY, float toX, float toY) {
        mStrokePaint.setColor(Color.rgb(r, g, b));
        mStrokePaint.setAlpha(a);
        mStrokePaint.setStrokeWidth(lineWidth);
        cvs.drawLine(fromX, fromY, toX, toY, mStrokePaint);
    }

    public void DrawCurveRect(Canvas cvs, int a, int r, int g, int b, float stroke, float x, float y, float width, float height) {
        mStrokePaint.setStrokeWidth(stroke);
        mStrokePaint.setColor(Color.rgb(r, g, b));
        mStrokePaint.setAlpha(a);
        cvs.drawRoundRect(x, y, width, height, 5.0f, 5.0f, this.mStrokePaint);
    }

    public void DrawFilledRect2(Canvas cvs, int a, int r, int g, int b, float x, float y, float width, float height) {
        mFillPaint.setColor(Color.rgb(r, g, b));
        mFillPaint.setAlpha(a);
        cvs.drawRect(x, y, width, height, mFillPaint);
    }

    public void DrawFilledRect(Canvas cvs, int a, int r, int g, int b, float x, float y, float width, float height) {
        mFillPaint.setColor(Color.rgb(r, g, b));
        mFillPaint.setAlpha(a);
        cvs.drawRoundRect(x, y, width, height, 0.0f, 0.0f, mFillPaint);
    }

    public void DrawFilledRoundRect(Canvas cvs, int a, int r, int g, int b, float x, float y, float width, float height) {
        mFillPaint.setColor(Color.rgb(r, g, b));
        mFillPaint.setAlpha(a);
        cvs.drawRoundRect(x, y, width, height, 10.0f, 10.0f, mFillPaint);
    }

    public void DrawTransRoundRect(Canvas cvs, int a, int r, int g, int b, float x, float y, float width, float height) {
        mFillPaint.setColor(Color.rgb(r, g, b));
        mFillPaint.setAlpha(a);
        cvs.drawRoundRect(x, y, width, height, 1000000.0f, 1000000.0f, mFillPaint);
    }

    public void DrawEnemyCount(Canvas cvs, int a, int r, int g, int b, int x, int y, int width, int height) {
        int colors[] = {Color.TRANSPARENT, Color.rgb(r, g, b), Color.TRANSPARENT};
        GradientDrawable mDrawable = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, colors);
        mDrawable.setShape(GradientDrawable.RECTANGLE);
        mDrawable.setGradientRadius(2.0f * 60);
        Rect mRect = new Rect(x, y, width, height);
        mDrawable.setBounds(mRect);
        cvs.save();
        mDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        mDrawable.draw(cvs);
        cvs.restore();
    }

    public void DrawFilledName(Canvas cvs, int a, int r, int g, int b, float x, float y, float width, float height) {
        mStrokePaint.setColor(Color.rgb(r, g, b));
        mStrokePaint.setAlpha(a);
        cvs.drawRoundRect(x, y, width, height, 3.0f, 3.0f, this.mStrokePaint);
    }

    public void DrawFilledRect1(Canvas cvs, int a, int r, int g, int b, float x, float y, float width, float height) {
        mFilledPaint.setColor(Color.rgb(r, g, b));
        mFilledPaint.setAlpha(a);
        cvs.drawRect(x, y, width, height, mFilledPaint);
    }

    public void DrawRect(Canvas cvs, int a, int r, int g, int b, float stroke, float x, float y, float width, float height) {
        mStrokePaint.setStrokeWidth(stroke);
        mStrokePaint.setColor(Color.rgb(r, g, b));
        mStrokePaint.setAlpha(a);
        cvs.drawRoundRect(x, y, width, height, 0.0f, 0.0f, this.mStrokePaint);
    }

    public void DrawTeamID(Canvas cvs, int a, int r, int g, int b, int teamid, float posX, float posY, float size) {
        mNamePaint.setColor(Color.rgb(r, g, b));
        mNamePaint.setTextSize(size);
        cvs.drawText(teamid + "", posX, posY, mNamePaint);
    }

    public void DrawFillCircle(Canvas cvs, int a, int r, int g, int b, float posX, float posY, float radius, float stroke) {
        mFilledPaint.setARGB(a, r, g, b);
        mFilledPaint.setStrokeWidth(stroke);
        cvs.drawCircle(posX, posY, radius, mFilledPaint);
    }

    public void DrawTransCircle(Canvas cvs, int a, int r, int g, int b, float x, float y, float radius, float stroke) {
        mFilledPaint.setARGB(a, r, g, b);
        mFilledPaint.setStrokeWidth(stroke);
        cvs.drawCircle(x, y, radius, mFilledPaint);
    }

    public void DrawTranslucentRoundRect(Canvas cvs, int a, int r, int g, int b, float x, float y, float radius, float width, float height, float stroke) {
        mFilledPaint.setColor(Color.rgb(r, g, b));
        mFilledPaint.setAlpha(100);
        mFilledPaint.setStrokeWidth(stroke);
        cvs.drawCircle(x, y, radius, mFilledPaint);
    }

    public void DrawCircle(Canvas cvs, int a, int r, int g, int b, float posX, float posY, float radius, float stroke) {
        mStrokePaint.setARGB(a, r, g, b);
        mStrokePaint.setStrokeWidth(stroke);
        cvs.drawCircle(posX, posY, radius, mStrokePaint);
    }

    public void DrawFilledTriangle(Canvas cvs, int a, int r, int g, int b, float posX, float posY, float size) {
        mFilledPaint.setColor(Color.rgb(r, g, b));
        mFilledPaint.setAlpha(a);

        float halfSize = size / 2;
        float height = (float) (Math.sqrt(3) * halfSize);

        float x1 = posX;
        float y1 = posY - height / 2;

        float x2 = posX - halfSize;
        float y2 = posY + height / 2;

        float x3 = posX + halfSize;
        float y3 = posY + height / 2;

        Path path = new Path();
        path.moveTo(x1, y1);
        path.lineTo(x2, y2);
        path.lineTo(x3, y3);
        path.close();

        cvs.drawPath(path, mFilledPaint);
    }

    public void DrawTriangle(Canvas cvs, int a, int r, int g, int b, float centerX, float centerY, float size, float angle) {
        Paint fillPaint = new Paint();
        fillPaint.setColor(Color.rgb(r, g, b));
        fillPaint.setAlpha(a);
        fillPaint.setStyle(Paint.Style.FILL);
        Paint strokePaint = new Paint();
        strokePaint.setColor(Color.WHITE);
        strokePaint.setAlpha(255);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(2);
        Path path = new Path();
        float halfSize = size / 2;
        float tipSize = size * 0.8f;
        float tipX = centerX + (float) (Math.cos(Math.toRadians(angle)) * tipSize);
        float tipY = centerY + (float) (Math.sin(Math.toRadians(angle)) * tipSize);
        float leftX = centerX + (float) (Math.cos(Math.toRadians(angle + 120)) * halfSize);
        float leftY = centerY + (float) (Math.sin(Math.toRadians(angle + 120)) * halfSize);
        float rightX = centerX + (float) (Math.cos(Math.toRadians(angle - 120)) * halfSize);
        float rightY = centerY + (float) (Math.sin(Math.toRadians(angle - 120)) * halfSize);
        path.moveTo(tipX, tipY);
        path.lineTo(leftX, leftY);
        path.lineTo(rightX, rightY);
        path.close();
        cvs.drawPath(path, strokePaint);
        cvs.drawPath(path, fillPaint);
    }

    public void DrawTriangleFilled(Canvas cvs, int a, int r, int g, int b, float posX1, float posY1, float posX2, float posY2, float posX3, float posY3) {
        Path path = new Path();
        path.moveTo(posX1, posY1);
        path.lineTo(posX2, posY2);
        path.lineTo(posX3, posY3);
        path.close();
        Paint paint = new Paint();
        paint.setARGB(a, r, g, b);
        paint.setStyle(Paint.Style.FILL);
        cvs.drawPath(path, paint);
    }

    public void DrawTexture(Canvas cvs, int a, int r, int g, int b, String txt, float posX, float posY, float size) {
        mMDText.setColor(Color.rgb(r, g, b));
        mMDText.setAlpha(a);
        mMDText.setTextSize(size);
        mMDText.setTypeface(Typeface.DEFAULT);
        cvs.drawText(txt, posX, posY, mMDText);
    }

    public void DrawFilledCircle(Canvas cvs, int a, int r, int g, int b, float posX, float posY, float radius) {
        mFilledPaint.setColor(Color.rgb(r, g, b));
        mFilledPaint.setAlpha(a);
        cvs.drawCircle(posX, posY, radius, mFilledPaint);
    }

    public void DrawName(Canvas cvs, int a, int r, int g, int b, String nametxt, float posX, float posY, float size) {
        try {
            String[] namesp = nametxt.split(":");
            char[] nameint = new char[namesp.length];
            for (int i = 0; i < namesp.length; i++)
                nameint[i] = (char) Integer.parseInt(namesp[i]);
            String realname = new String(nameint);
            mTextPaint.setARGB(a, r, g, b);
            mTextPaint.setTextSize(size);
            cvs.drawText(realname, posX, posY, mTextPaint);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void DrawTextName(Canvas cvs, int a, int r, int g, int b, String txt, float posX, float posY, float size) {
        mFPSText.setARGB(a, r, g, b);
        mFPSText.setTextSize(size);
        if (SystemClock.uptimeMillis() - mFPSTime > 1000) {
            mFPSTime = SystemClock.uptimeMillis();
            mFPS = mFPSCounter;
            mFPSCounter = 0f;
        } else {
            mFPSCounter++;
        }

        int fpsInt = (int) mFPS;
        String fpsText = " - FPS : " + fpsInt;
        cvs.drawText(txt + fpsText, posX, posY, mFPSText);
    }

    public void DrawUserID(Canvas cvs, int a, int r, int g, int b, String nametxt, float posX, float posY, float size) {
        try {
            String[] namesp = nametxt.split(":");
            char[] nameint = new char[namesp.length];
            for (int i = 0; i < namesp.length; i++)
				nameint[i] = (char) Integer.parseInt(namesp[i]);
            String realname = new String(nameint);
            if (realname.length() == 4 && realname.matches("\\d{4}")) {
                realname = "Smart AI";
            }
            mTextPaint.setARGB(a, r, g, b);
            mTextPaint.setTextSize(size);
            mTextPaint.setColor(Color.parseColor("#FFFFFF"));
            cvs.drawText("ID: " + realname, posX, posY, mTextPaint);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void DrawText(Canvas cvs, int a, int r, int g, int b, String txt, float posX, float posY, float size) {
        mTextPaint.setARGB(a, r, g, b);
        mTextPaint.setTextSize(size);
        cvs.drawText(txt, posX, posY, mTextPaint);
    }

    public void DrawItems(Canvas cvs, String itemName, float distance, float posX, float posY, float size) {
        try {
            String realItemName = getItemName(itemName);
            if (realItemName != null && !realItemName.equals("")) {
                mItemsPaint.setTextSize(size);
                String displayText = realItemName + " (" + (int) distance + ")";
                if (realItemName.equals("LootBox")) {
                    if (distance < 150) {
                        mTextPainti.setTextSize(size * 1.5f);
                        mTextPainti.setColor(Color.YELLOW);
                        cvs.drawText("📦", posX - 15, posY - (15 * mScaleY), mTextPainti);
                    }
                } else if (realItemName.equals("AirDrop")) {
                    mTextPainti.setTextSize(size * 1.5f);
                    mTextPainti.setColor(Color.RED);
                    cvs.drawText("✈️", posX - 15, posY - (72 * mScaleY), mTextPainti);
                    mItemsPaint.setStyle(Paint.Style.STROKE);
                    mItemsPaint.setStrokeWidth(3);
                    mItemsPaint.setColor(Color.BLACK);
                    cvs.drawText(displayText, posX, posY - 8, mItemsPaint);
                    mItemsPaint.setStyle(Paint.Style.FILL);
                    mItemsPaint.setColor(Color.parseColor("#FF0000"));
                    cvs.drawText(displayText, posX, posY - 8, mItemsPaint);
                } else {
                    mTextPainti.setTextSize(size);
                    cvs.drawText(realItemName + " (" + Math.round(distance) + "m)", posX, posY, mTextPainti);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void DrawDeadBoxItems(Canvas cvs, int a, int r, int g, int b, String txt, float posX, float posY, float size) {
        try {
            mLootBoxPaint.setTextSize(size);

            int[] colorArray = {
				Color.parseColor("#FF5733"), Color.parseColor("#33FF57"), Color.parseColor("#3357FF"),
				Color.parseColor("#FF33A1"), Color.parseColor("#A133FF"), Color.parseColor("#33FFA1"),
				Color.parseColor("#FFA133"), Color.parseColor("#57FF33"), Color.parseColor("#FF5733"),
				Color.parseColor("#33A1FF"), Color.parseColor("#A1FF33"), Color.parseColor("#FF3366"),
				Color.parseColor("#66FF33"), Color.parseColor("#3366FF"), Color.parseColor("#FF9966"),
				Color.parseColor("#6699FF"), Color.parseColor("#9966FF"), Color.parseColor("#66FF99"),
				Color.parseColor("#996633"), Color.parseColor("#663399"), Color.parseColor("#FFCC33"),
				Color.parseColor("#CC33FF"), Color.parseColor("#33CCFF"), Color.parseColor("#FF33CC"),
				Color.parseColor("#33FFCC"), Color.parseColor("#CCFF33"), Color.parseColor("#CC6633"),
				Color.parseColor("#3366CC"), Color.parseColor("#CC3366"), Color.parseColor("#33CC66"),
				Color.parseColor("#FF6633"), Color.parseColor("#6633FF"), Color.parseColor("#3366FF"),
				Color.parseColor("#FF3366"), Color.parseColor("#66FFCC"), Color.parseColor("#CC66FF"),
				Color.parseColor("#33FF99"), Color.parseColor("#99FF33"), Color.parseColor("#FF3399"),
				Color.parseColor("#3399FF"), Color.parseColor("#FF9933"), Color.parseColor("#9933FF"),
				Color.parseColor("#FF6633"), Color.parseColor("#33FF66"), Color.parseColor("#9933CC"),
				Color.parseColor("#CC9933"), Color.parseColor("#33CC99"), Color.parseColor("#993366"),
				Color.parseColor("#669933"), Color.parseColor("#FF6699")
            };

            if (!itemColorMap.containsKey(txt)) {
                Random random = new Random();
                itemColorMap.put(txt, colorArray[random.nextInt(colorArray.length)]);
            }
            int itemColor = itemColorMap.get(txt);

            mLootBoxPaint.setStyle(Paint.Style.STROKE);
            mLootBoxPaint.setStrokeWidth(3);
            mLootBoxPaint.setColor(Color.BLACK);
            cvs.drawText(txt, posX - 60, posY - 10, mLootBoxPaint);

            mLootBoxPaint.setStyle(Paint.Style.FILL);
            mLootBoxPaint.setColor(itemColor);
            cvs.drawText(txt, posX - 60, posY - 10, mLootBoxPaint);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void DrawVehicles(Canvas cvs, String VehicleName, float distance, float health, float fuel, float posX, float posY, float size) {
        try {
            String realVehicleName = VehicleName(VehicleName);
            if (realVehicleName != null && !realVehicleName.equals("")) {
                mVehiclesPaint.setTextSize(size);
                String displayText = realVehicleName + "[" + (int) distance + "]";
                mVehiclesPaint.setStyle(Paint.Style.STROKE);
                mVehiclesPaint.setStrokeWidth(3);
                mVehiclesPaint.setColor(Color.BLACK);
                cvs.drawText(displayText, posX, posY - 0, mVehiclesPaint);
                mVehiclesPaint.setStyle(Paint.Style.FILL);
                mVehiclesPaint.setColor(Color.parseColor("#FFFFFF"));
                cvs.drawText(displayText, posX, posY - 0, mVehiclesPaint);
                handleFuelHealthText(cvs, posX, posY - 10, fuel, health, size);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleFuelHealthText(Canvas cvs, float posX, float posY, float fuel, float health, float size) {
        try {
            mStrokePaint.setARGB(50, 0, 0, 0);
            cvs.drawRoundRect(posX - 45, posY + 19, posX + 50, posY + 25, 3, 3, mStrokePaint);
            mFilledPaint.setARGB(100, 255, 255, 0);
            cvs.drawRoundRect(posX - 45, posY + 19, posX - 40 + (2 * 45) * fuel / 100, posY + 25, 3, 3, mFilledPaint);
            mStrokePaint.setARGB(50, 0, 0, 0);
            cvs.drawRoundRect(posX - 45, posY + 29, posX + 50, posY + 35, 3, 3, mStrokePaint);
            mFilledPaint.setARGB(100, 255, 45, 30);
            cvs.drawRoundRect(posX - 45, posY + 29, posX - 40 + (2 * 45) * health / 100, posY + 35, 3, 3, mFilledPaint);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void DrawNation(Canvas cvs, int a, int r, int g, int b, String nametxt, int flag, float posX, float posY, float size) {
        try {
            String[] namesp = nametxt.split(":");
            char[] nameint = new char[namesp.length];
            for (int i = 0; i < namesp.length; i++)
				nameint[i] = (char) Integer.parseInt(namesp[i]);
            String realname = new String(nameint);
            mTextPaint.setARGB(a, r, g, b);
            mTextPaint.setTextSize(size);
            cvs.drawText("", posX + 85, posY - 0, mTextPaint);
            cvs.drawText(Nation(realname), posX + 73, posY - 49, mTextPaint);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String Nation(String code) {
        if (code.equals("G1")) {
            code = "🌍️";
        } else {
            code = new String(Character.toChars((Character.codePointAt(code, 0) - 65) + 127462)) + new String(Character.toChars((Character.codePointAt(code, 1) - 65) + 127462));
        }
        return code;
    }

    public void DrawWeapon(Canvas cvs, int a, int r, int g, int b, int id, int ammo, int ammo2, float posX, float posY, float size) {
        mNamePaint.setARGB(a, r, g, b);
        mNamePaint.setTextSize(size);
        String wname = getWeapon(id);
        if (wname != null) {
            if (wname == "Sickle" || wname == "Machete" || wname == "Crowbar" || wname == "Pan") {
                cvs.drawText(wname, posX, posY, mNamePaint);
            } else {
                cvs.drawText(wname + "(" + ammo + "/" + ammo2 + ")", posX, posY, mNamePaint);
            }
        }
    }

    /**
     * Clean up method to release resources
     */
    public void cleanup() {
        if (mThread != null) {
            mThread.interrupt();
            mThread = null;
        }
    }

    private String getWeapon(int id) {
		int baseId = id / 10;
		Map<Integer, String> weaponMap = new HashMap<>();
		weaponMap.put(10100, "AKM");
		weaponMap.put(10101, "M16A4");
		weaponMap.put(10102, "SCAR-L");
		weaponMap.put(10103, "M416");
		weaponMap.put(10104, "Groza");
		weaponMap.put(10105, "AUG");
		weaponMap.put(10106, "QBZ");
		weaponMap.put(10107, "M762");
		weaponMap.put(10108, "Mk47");
		weaponMap.put(10109, "G36C");
		weaponMap.put(10111, "Honey Badger");
		weaponMap.put(10110, "FAMAS");
		weaponMap.put(10112, "ASM AR");
		weaponMap.put(10113, "ACE32");
		weaponMap.put(10200, "UZI");
		weaponMap.put(10201, "UMP");
		weaponMap.put(10202, "Vector");
		weaponMap.put(10203, "ThommyGun");
		weaponMap.put(10204, "Bizon");
		weaponMap.put(10206, "MP5K");
		weaponMap.put(10215, "P90");
		weaponMap.put(10300, "Kar98k");
		weaponMap.put(10301, "M24");
		weaponMap.put(10302, "AWM");
		weaponMap.put(10303, "SKS");
		weaponMap.put(10304, "VSS");
		weaponMap.put(10305, "Mini14");
		weaponMap.put(10306, "Mk14");
		weaponMap.put(10307, "Win94");
		weaponMap.put(10308, "SLR");
		weaponMap.put(10309, "QBU");
		weaponMap.put(10310, "Mosin");
		weaponMap.put(10311, "Lynx AMR");
		weaponMap.put(10320, "Mk12");
		return weaponMap.getOrDefault(baseId, "");
	}

    private String VehicleName(String s) {
        if (s.contains("Buggy") && getConfig("Buggy"))
            return "Buggy";
        if (s.contains("UAZ") && getConfig("UAZ"))
            return "UAZ";
        if (s.contains("MotorcycleC") && getConfig("Trike"))
            return "Trike";
        if (s.contains("Motorcycle") && getConfig("Bike"))
            return "Bike";
        if (s.contains("DAcia") && getConfig("Dacia"))
            return "Dacia";
        if (s.contains("Dacia") && getConfig("Dacia"))
            return "Dacia";
        if (s.contains("AquaRail") && getConfig("Jet"))
            return "Jet";
        if (s.contains("PG117") && getConfig("Boat"))
            return "Boat";
        if (s.contains("MiniBus") && getConfig("Bus"))
            return "Bus";
        if (s.contains("Mirado") && getConfig("Mirado"))
            return "Mirado";
        if (s.contains("Scooter") && getConfig("Scooter"))
            return "Scooter";
        if (s.contains("Rony") && getConfig("Rony"))
            return "Rony";
        if (s.contains("Snowbike") && getConfig("Snowbike"))
            return "Snowbike";
        if (s.contains("Snowmobile") && getConfig("Snowmobile"))
            return "Snowmobile";
        if (s.contains("Tuk") && getConfig("Tempo"))
            return "Tempo";
        if (s.contains("PickUp") && getConfig("Truck"))
            return "Truck";
        if (s.contains("BRDM") && getConfig("BRDM"))
            return "BRDM";
        if (s.contains("LadaNiva") && getConfig("LadaNiva"))
            return "LadaNiva";
        if (s.contains("Bigfoot") && getConfig("Monster"))
            return "Monster";
        if (s.contains("CoupeRB") && getConfig("CoupeRB"))
            return "CoupeRB";
        if (s.contains("glider") && getConfig("Motor Glider"))
            return "Motor Glider";
        if (s.contains("UTV") && getConfig("UTV"))
            return "UTV";
        if (s.contains("ATV1") && getConfig("ATV1"))
            return "ATV1";
        if (s.contains("Reindeer") && getConfig("Reindeer"))
            return "Reindeer";
        return "";
    }

    private String getItemName(String s) {
        if (s.contains("MZJ_8X") && getConfig("8x")) {
            mTextPainti.setARGB(255, 247, 99, 245);
            return "8x";
        }
        if (s.contains("MZJ_2X") && getConfig("2x")) {
            mTextPainti.setARGB(255, 230, 172, 226);
            return "2x";
        }
        if (s.contains("MZJ_HD") && getConfig("Red Dot")) {
            mTextPainti.setARGB(255, 230, 172, 226);
            return "Red Dot";
        }
        if (s.contains("MZJ_3X") && getConfig("3x")) {
            mTextPainti.setARGB(255, 247, 99, 245);
            return "3X";
        }
        if (s.contains("MZJ_QX") && getConfig("Hollow")) {
            mTextPainti.setARGB(255, 153, 75, 152);
            return "Hollow Sight";
        }
        if (s.contains("MZJ_6X") && getConfig("6x")) {
            mTextPainti.setARGB(255, 247, 99, 245);
            return "6x";
        }
        if (s.contains("MZJ_4X") && getConfig("4x")) {
            mTextPainti.setARGB(255, 247, 99, 245);
            return "4x";
        }
        if (s.contains("MZJ_SideRMR") && getConfig("Canted")) {
            mTextPainti.setARGB(255, 153, 75, 152);
            return "Canted Sight";
        }

        if (s.contains("Rifle_AUG") && getConfig("AUG")) {
            mTextPainti.setARGB(255, 52, 224, 63);
            return "AUG";
        }
        if (s.contains("Rifle_M762") && getConfig("M762")) {
            mTextPainti.setARGB(255, 43, 26, 28);
            return "M762";
        }
        if (s.contains("Rifle_SCAR") && getConfig("SCAR-L")) {
            mTextPainti.setARGB(255, 52, 224, 63);
            return "SCAR-L";
        }
        if (s.contains("Rifle_FAMAS") && getConfig("FAMAS")) {
            mTextPainti.setARGB(255, 0, 255, 0);
            return "FAMAS";
        }
        if (s.contains("Rifle_M416") && getConfig("M416")) {
            mTextPainti.setARGB(255, 115, 235, 223);
            return "M416";
        }
        if (s.contains("Rifle_M16A4") && getConfig("M16A4")) {
            mTextPainti.setARGB(255, 116, 227, 123);
            return "M16A-4";
        }
        if (s.contains("Rifle_G36") && getConfig("G36C")) {
            mTextPainti.setARGB(255, 116, 227, 123);
            return "G36C";
        }
        if (s.contains("Rifle_QBZ") && getConfig("QBZ")) {
            mTextPainti.setARGB(255, 52, 224, 63);
            return "QBZ";
        }
        if (s.contains("Rifle_AKM") && getConfig("AKM")) {
            mTextPainti.setARGB(255, 214, 99, 99);
            return "AKM";
        }
        if (s.contains("Rifle_HoneyBadger") && getConfig("Honey Badger")) {
            mTextPainti.setARGB(255, 214, 99, 99);
            return "Honey Badger";
        }
        if (s.contains("Rifle_Groza") && getConfig("Groza")) {
            mTextPainti.setARGB(255, 214, 99, 99);
            return "Groza";
        }
        if (s.contains("Rifle_ACE32") && getConfig("ACE32")) {
            mTextPainti.setARGB(255, 214, 99, 99);
            return "ACE32";
        }

        if (s.contains("SubmachineGun_UMP45") && getConfig("UMP")) {
            mTextPainti.setARGB(255, 207, 207, 207);
            return "UMP";
        }

        if (s.contains("MachineGun_PP19") && getConfig("Bizon")) {
            mTextPainti.setARGB(255, 255, 246, 0);
            return "Bizon";
        }
        if (s.contains("MachineGun_TommyGun") && getConfig("TommyGun")) {
            mTextPainti.setARGB(255, 207, 207, 207);
            return "TommyGun";
        }
        if (s.contains("MachineGun_MP5K") && getConfig("MP5K")) {
            mTextPainti.setARGB(255, 207, 207, 207);
            return "MP5K";
        }
        if (s.contains("MachineGun_UMP9") && getConfig("UMP")) {
            mTextPainti.setARGB(255, 207, 207, 207);
            return "UMP";
        }
        if (s.contains("MachineGun_Vector") && getConfig("Vector")) {
            mTextPainti.setARGB(255, 255, 246, 0);
            return "Vector";
        }
        if (s.contains("MachineGun_Uzi") && getConfig("UZI")) {
            mTextPainti.setARGB(255, 255, 246, 0);
            return "UZI";
        }
        if (s.contains("MachineGun_P90") && getConfig("P90")) {
            mTextPainti.setARGB(255, 233, 0, 207);
            return "P90";
        }

        if (s.contains("Other_DP28") && getConfig("DP28")) {
            mTextPainti.setARGB(255, 43, 26, 28);
            return "DP28";
        }
        if (s.contains("Other_M249") && getConfig("M249")) {
            mTextPainti.setARGB(255, 247, 99, 245);
            return "M249";
        }
        if (s.contains("Other_MG3") && getConfig("MG3")) {
            mTextPainti.setARGB(255, 0, 255, 0);
            return "MG3";
        }

        if (s.contains("Sniper_AWM") && getConfig("AWM")) {
            mTextPainti.setColor(Color.BLACK);
            return "AWM";
        }
        if (s.contains("Sniper_AMR") && getConfig("AMR")) {
            mTextPainti.setARGB(255, 247, 99, 245);
            return "AMR";
        }
        if (s.contains("Sniper_QBU") && getConfig("QBU")) {
            mTextPainti.setARGB(255, 207, 207, 207);
            return "QBU";
        }
        if (s.contains("Sniper_SLR") && getConfig("SLR")) {
            this.mTextPainti.setARGB(255, 214, 99, 99);
            return "SLR";
        }
        if (s.contains("Sniper_SKS") && getConfig("SKS")) {
            this.mTextPainti.setARGB(255, 214, 99, 99);
            return "SKS";
        }
        if (s.contains("Sniper_Mini14") && getConfig("Mini14")) {
            mTextPainti.setARGB(255, 247, 99, 245);
            return "Mini14";
        }
        if (s.contains("Sniper_M24") && getConfig("M24")) {
            this.mTextPainti.setARGB(255, 214, 99, 99);
            return "M24";
        }
        if (s.contains("Sniper_Kar98k") && getConfig("Kar98k")) {
            this.mTextPainti.setARGB(255, 214, 99, 99);
            return "Kar98k";
        }
        if (s.contains("Sniper_VSS") && getConfig("VSS")) {
            mTextPainti.setARGB(255, 255, 246, 0);
            return "VSS";
        }
        if (s.contains("Sniper_Win94") && getConfig("Win94")) {
            mTextPainti.setARGB(255, 207, 207, 207);
            return "Win94";
        }
        if (s.contains("Sniper_Mk14") && getConfig("MK14")) {
            this.mTextPainti.setARGB(255, 214, 99, 99);
            return "MK14";
        }
        if (s.contains("Sniper_Mosin") && getConfig("Mosin")) {
            mTextPainti.setARGB(255, 153, 0, 0);
            return "Mosin";
        }
        if (s.contains("Sniper_MK12") && getConfig("MK12")) {
            this.mTextPainti.setARGB(255, 214, 99, 99);
            return "MK12";
        }
        if (s.contains("Sniper_Mk47") && getConfig("MK47")) {
            mTextPainti.setARGB(255, 247, 99, 245);
            return "Mk47 Mutant";
        }

        if (s.contains("ShotGun_S12K") && getConfig("S12K")) {
            mTextPainti.setARGB(255, 153, 109, 109);
            return "S12K";
        }
        if (s.contains("ShotGun_DP12") && getConfig("DBS")) {
            mTextPainti.setARGB(255, 153, 109, 109);
            return "DBS";
        }
        if (s.contains("ShotGun_M1014") && getConfig("M1014")) {
            mTextPainti.setARGB(255, 153, 109, 109);
            return "M1014";
        }
        if (s.contains("ShotGun_Neostead2000") && getConfig("NS2000")) {
            mTextPainti.setARGB(255, 153, 109, 109);
            return "NS2000";
        }
        if (s.contains("ShotGun_S686") && getConfig("S686")) {
            mTextPainti.setARGB(255, 153, 109, 109);
            return "S686";
        }
        if (s.contains("ShotGun_S1897") && getConfig("S1897")) {
            mTextPainti.setARGB(255, 153, 109, 109);
            return "S1897";
        }

        if (s.contains("Sickle") && getConfig("Sickle")) {
            mTextPainti.setARGB(255, 102, 74, 74);
            return "Sickle";
        }
        if (s.contains("Machete") && getConfig("Machete")) {
            mTextPainti.setARGB(255, 102, 74, 74);
            return "Machete";
        }
        if (s.contains("Cowbar") && getConfig("Crowbar")) {
            mTextPainti.setARGB(255, 102, 74, 74);
            return "Crowbar";
        }
        if (s.contains("CrossBow") && getConfig("CrossBow")) {
            mTextPainti.setARGB(255, 102, 74, 74);
            return "CrossBow";
        }
        if (s.contains("Pan") && getConfig("Pan")) {
            mTextPainti.setARGB(255, 102, 74, 74);
            return "Pan";
        }

        if (s.contains("SawedOff") && getConfig("Sawed-Off")) {
            mTextPainti.setARGB(255, 153, 109, 109);
            return "SawedOff";
        }
        if (s.contains("R1895") && getConfig("R1895")) {
            mTextPainti.setARGB(255, 156, 113, 81);
            return "R1895";
        }
        if (s.contains("Vz61") && getConfig("Scorpion")) {
            mTextPainti.setARGB(255, 156, 113, 81);
            return "Scorpion";
        }
        if (s.contains("P92") && getConfig("P92")) {
            mTextPainti.setARGB(255, 156, 113, 81);
            return "P92";
        }
        if (s.contains("P18C") && getConfig("P18C")) {
            mTextPainti.setARGB(255, 156, 113, 81);
            return "P18C";
        }
        if (s.contains("R45") && getConfig("R45")) {
            mTextPainti.setARGB(255, 156, 113, 81);
            return "R45";
        }
        if (s.contains("P1911") && getConfig("P1911")) {
            mTextPainti.setARGB(255, 156, 113, 81);
            return "P1911";
        }
        if (s.contains("DesertEagle") && getConfig("Dessert Eagle")) {
            mTextPainti.setARGB(255, 156, 113, 81);
            return "DesertEagle";
        }

        if (s.contains("Ammo_762mm") && getConfig("7.62mm")) {
            lastItemColor = Color.argb(255, 92, 36, 28);
            mTextPainti.setARGB(255, 92, 36, 28);
            return "7.62";
        }
        if (s.contains("Ammo_45AC") && getConfig("45ACP")) {
            lastItemColor = Color.argb(255, 92, 36, 28);
            mTextPainti.setColor(Color.LTGRAY);
            return "45ACP";
        }
        if (s.contains("Ammo_556mm") && getConfig("5.56mm")) {
            lastItemColor = (Color.GREEN);
            mTextPainti.setColor(Color.GREEN);
            return "5.56";
        }
        if (s.contains("Ammo_9mm") && getConfig("9mm")) {
            mTextPainti.setColor(Color.YELLOW);
            return "9mm";
        }
        if (s.contains("Ammo_300Magnum") && getConfig("300Magnum")) {
            mTextPainti.setColor(Color.BLACK);
            return "300Magnum";
        }
        if (s.contains("Ammo_50BMG") && getConfig("50BMG")) {
            mTextPainti.setColor(Color.BLACK);
            return "50BMG";
        }
        if (s.contains("Ammo_12Guage") && getConfig("12Guage")) {
            mTextPainti.setARGB(255, 156, 91, 81);
            return "12Guage";
        }
        if (s.contains("Ammo_Bolt") && getConfig("Arrow")) {
            mTextPainti.setARGB(255, 156, 113, 81);
            return "Arrow";
        }

        if (s.contains("Bag_Lv3") && getConfig("Bag L3")) {
            mTextPainti.setARGB(255, 36, 83, 255);
            return "Bag lvl 3";
        }
        if (s.contains("Bag_Lv1") && getConfig("Bag L1")) {
            mTextPainti.setARGB(255, 127, 154, 250);
            return "Bag lvl 1";
        }
        if (s.contains("Bag_Lv2") && getConfig("Bag L2")) {
            mTextPainti.setARGB(255, 77, 115, 255);
            return "Bag lvl 2";
        }
        if (s.contains("Armor_Lv2") && getConfig("Vest L2")) {
            mTextPainti.setARGB(255, 77, 115, 255);
            return "Vest lvl 2";
        }
        if (s.contains("Armor_Lv1") && getConfig("Vest L1")) {
            mTextPainti.setARGB(255, 127, 154, 250);
            return "Vest lvl 1";
        }
        if (s.contains("Armor_Lv3") && getConfig("Vest L3")) {
            mTextPainti.setARGB(255, 36, 83, 255);
            return "Vest lvl 3";
        }
        if (s.contains("Helmet_Lv2") && getConfig("Helmet L2")) {
            mTextPainti.setARGB(255, 77, 115, 255);
            return "Helmet lvl 2";
        }
        if (s.contains("Helmet_Lv1") && getConfig("Helmet L1")) {
            mTextPainti.setARGB(255, 127, 154, 250);
            return "Helmet lvl 1";
        }
        if (s.contains("Helmet_Lv3") && getConfig("Helmet L3")) {
            mTextPainti.setARGB(255, 36, 83, 255);
            return "Helmet lvl 3";
        }

        if (s.contains("Pills") && getConfig("PainKiller")) {
            mTextPainti.setARGB(255, 227, 91, 54);
            return "PainKiller";
        }
        if (s.contains("Injection") && getConfig("Injection")) {
            mTextPainti.setARGB(255, 204, 193, 190);
            return "Injection";
        }
        if (s.contains("Drink") && getConfig("EnergyDrink")) {
            mTextPainti.setARGB(255, 54, 175, 227);
            return "Energy Drink";
        }
        if (s.contains("Firstaid") && getConfig("FirstAid")) {
            mTextPainti.setARGB(255, 194, 188, 109);
            return "FirstAid";
        }
        if (s.contains("Bandage") && getConfig("Bandage")) {
            mTextPainti.setARGB(255, 43, 189, 48);
            return "Bandage";
        }
        if (s.contains("FirstAidbox") && getConfig("MedKit")) {
            mTextPainti.setARGB(255, 0, 171, 6);
            return "Medkit";
        }

        if (s.contains("Grenade_Stun") && getConfig("Stun")) {
            mTextPainti.setARGB(255, 204, 193, 190);
            return "Stun";
        }
        if (s.contains("Grenade_Shoulei") && getConfig("Grenade")) {
            mTextPainti.setARGB(255, 2, 77, 4);
            return "Grenade";
        }
        if (s.contains("Grenade_Smoke") && getConfig("Smoke")) {
            mTextPainti.setColor(Color.WHITE);
            return "Smoke";
        }
        if (s.contains("Grenade_Burn") && getConfig("Molotov")) {
            mTextPainti.setARGB(255, 230, 175, 64);
            return "Molotov";
        }

        if (s.contains("Large_FlashHider") && getConfig("Flash Hider Ar")) {
            mTextPainti.setARGB(255, 255, 213, 130);
            return "Flash Hider Ar";
        }
        if (s.contains("QK_Large_C") && getConfig("Compensator Ar")) {
            mTextPainti.setARGB(255, 255, 213, 130);
            return "Compensator Ar";
        }
        if (s.contains("Mid_FlashHider") && getConfig("Flash Hider SMG")) {
            mTextPainti.setARGB(255, 255, 213, 130);
            return "Flash Hider SMG";
        }
        if (s.contains("QT_A_") && getConfig("Tactical Stock")) {
            mTextPainti.setARGB(255, 158, 222, 195);
            return "Tactical Stock";
        }
        if (s.contains("DuckBill") && getConfig("Duckbill")) {
            mTextPainti.setARGB(255, 158, 222, 195);
            return "DuckBill";
        }
        if (s.contains("Sniper_FlashHider") && getConfig("Flash Hider Sniper")) {
            mTextPainti.setARGB(255, 158, 222, 195);
            return "Flash Hider Sniper";
        }
        if (s.contains("Mid_Suppressor") && getConfig("Suppressor SMG")) {
            mTextPainti.setARGB(255, 158, 222, 195);
            return "Suppressor SMG";
        }
        if (s.contains("Choke") && getConfig("Choke")) {
            mTextPainti.setARGB(255, 155, 189, 222);
            return "Choke";
        }
        if (s.contains("QT_UZI") && getConfig("Stock Micro UZI")) {
            mTextPainti.setARGB(255, 155, 189, 222);
            return "Stock Micro UZI";
        }
        if (s.contains("QK_Sniper") && getConfig("Compensator Sniper")) {
            mTextPainti.setARGB(255, 60, 127, 194);
            return "Compensator Sniper";
        }
        if (s.contains("Sniper_Suppressor") && getConfig("Suppressor Sniper")) {
            mTextPainti.setARGB(255, 60, 127, 194);
            return "Suppressor Sniper";
        }
        if (s.contains("Large_Suppressor") && getConfig("Suppressor Ar")) {
            mTextPainti.setARGB(255, 60, 127, 194);
            return "Suppressor Ar";
        }
        if (s.contains("Sniper_EQ_") && getConfig("Extended QD Sniper")) {
            mTextPainti.setARGB(255, 193, 140, 222);
            return "Ex.Qd.Sniper";
        }
        if (s.contains("Sniper_E_") && getConfig("Extended Mag Sniper")) {
            mTextPainti.setARGB(255, 193, 163, 209);
            return "Ex.Sniper";
        }
        if (s.contains("Sniper_Q_") && getConfig("QuickDraw Mag Sniper")) {
            mTextPainti.setARGB(255, 193, 163, 209);
            return "Qd.Sniper";
        }
        if (s.contains("Large_EQ_") && getConfig("Extended QD Ar")) {
            mTextPainti.setARGB(255, 193, 140, 222);
            return "Extended QD Ar";
        }
        if (s.contains("Large_E_") && getConfig("Extended Mag Ar")) {
            mTextPainti.setARGB(255, 193, 163, 209);
            return "Extended Mag Ar";
        }
        if (s.contains("Large_Q_") && getConfig("QuickDraw Mag Ar")) {
            mTextPainti.setARGB(255, 193, 163, 209);
            return "QuickDraw Mag Ar";
        }
        if (s.contains("Mid_EQ_") && getConfig("Extended QD SMG")) {
            mTextPainti.setARGB(255, 193, 140, 222);
            return "Ex.Qd.SMG";
        }
        if (s.contains("Mid_E_") && getConfig("Extended Mag SMG")) {
            mTextPainti.setARGB(255, 193, 163, 209);
            return "Ex.SMG";
        }
        if (s.contains("Mid_Q_") && getConfig("QuickDraw Mag SMG")) {
            mTextPainti.setARGB(255, 193, 163, 209);
            return "Qd.SMG";
        }
        if (s.contains("Crossbow_Q") && getConfig("Quiver CrossBow")) {
            mTextPainti.setARGB(255, 148, 121, 163);
            return "Quiver CrossBow";
        }
        if (s.contains("ZDD_Sniper") && getConfig("Bullet Loop")) {
            mTextPainti.setARGB(255, 148, 121, 163);
            return "Bullet Loop";
        }
        if (s.contains("ThumbGrip") && getConfig("Thumb Grip")) {
            mTextPainti.setARGB(255, 148, 121, 163);
            return "Thumb Grip";
        }
        if (s.contains("Lasersight") && getConfig("Laser Sight")) {
            mTextPainti.setARGB(255, 148, 121, 163);
            return "Laser Sight";
        }
        if (s.contains("Angled") && getConfig("Angled Grip")) {
            mTextPainti.setARGB(255, 219, 219, 219);
            return "Angled Grip";
        }
        if (s.contains("LightGrip") && getConfig("Light Grip")) {
            mTextPainti.setARGB(255, 219, 219, 219);
            return "Light Grip";
        }
        if (s.contains("Vertical") && getConfig("Vertical Grip")) {
            mTextPainti.setARGB(255, 219, 219, 219);
            return "Vertical Grip";
        }
        if (s.contains("HalfGrip") && getConfig("Half Grip")) {
            mTextPainti.setARGB(255, 155, 189, 222);
            return "Half Grip";
        }
        if (s.contains("GasCan") && getConfig("Gas Can")) {
            mTextPainti.setARGB(255, 255, 143, 203);
            return "Gas Can";
        }
        if (s.contains("Mid_Compensator") && getConfig("Compensator SMG")) {
            mTextPainti.setARGB(255, 219, 219, 219);
            return "Compensator SMG";
        }

        if (s.contains("Flaregun") && getConfig("FlareGun")) {
            mTextPainti.setARGB(255, 242, 63, 159);
            return "Flare Gun";
        }
        if (s.contains("Ammo_Flare") && getConfig("FlareGun")) {
            mTextPainti.setARGB(255, 242, 63, 159);
            return "Flare Gun";
        }
        if (s.contains("Ghillie") && getConfig("Ghillie Suit")) {
            mTextPainti.setARGB(255, 139, 247, 67);
            return "Ghillie Suit";
        }
        if (s.contains("CheekPad") && getConfig("CheekPad")) {
            mTextPainti.setARGB(255, 112, 55, 55);
            return "CheekPad";
        }
        if (s.contains("PickUpListWrapperActor") && getConfig("LootBox")) {
            mTextPainti.setARGB(255, 255, 255, 255);
            return "LootBox";
        }
        if ((s.contains("AirDropPlane")) && getConfig("DropPlane")) {
            mTextPainti.setARGB(255, 0, 255, 255);
            return "DropPlane";
        }
        if ((s.contains("AirDropBox")) && getConfig("AirDrop")) {
            mTextPainti.setARGB(255, 0, 200, 0);
            return "AirDrop";
        }
        return null;
    }

    private String getVehicleName(String s){
        if(s.contains("Buggy") && getConfig("Buggy"))
            return "Buggy";
        if(s.contains("UAZ") && getConfig("UAZ"))
            return "UAZ";
        if(s.contains("MotorcycleC") && getConfig("Trike") )
            return "Trike";
        if(s.contains("Motorcycle") && getConfig("Bike"))
            return "Bike";
        if(s.contains("Dacia") && getConfig("Dacia"))
            return "Dacia";
        if(s.contains("AquaRail") && getConfig("Jet"))
            return "Jet";
        if(s.contains("PG117") && getConfig("Boat"))
            return "Boat";
        if(s.contains("MiniBus") && getConfig("Bus"))
            return "Bus";
        if(s.contains("Mirado") && getConfig("Mirado"))
            return "Mirado";
        if(s.contains("Scooter") && getConfig("Scooter"))
            return "Scooter";
        if(s.contains("Rony") && getConfig("Rony"))
            return "Rony";
        if(s.contains("Snowbike") && getConfig("Snowbike"))
            return "Snowbike";
        if(s.contains("Snowmobile") && getConfig("Snowmobile"))
            return "Snowmobile";
        if(s.contains("Tuk") && getConfig("Tempo"))
            return "Tempo";
        if(s.contains("PickUp") && getConfig("Truck"))
            return "Truck";
        if(s.contains("BRDM") && getConfig("BRDM"))
            return "BRDM";
        if(s.contains("LadaNiva") && getConfig("LadaNiva"))
            return "LadaNiva";
        if(s.contains("Bigfoot") && getConfig("Monster Truck"))
            return "Monster Truck";
        return "";
    }
}