package com.ccs.poketstate.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import com.ccs.poketstate.i18n.Strings;

import java.util.Locale;

/** Minimalist dual-series chart with optional dashed A/B baseline trajectory overlay. */
public final class ChartView extends View {
    /** Palette set by the host activity so charts follow the selected theme. */
    private static int inkColor = Color.parseColor("#F4F5F7");
    private static int gridColor = Color.parseColor("#20232A");
    private static int subColor = Color.parseColor("#8A909E");

    public static void setPalette(int ink, int grid, int sub) {
        inkColor = ink;
        gridColor = grid;
        subColor = sub;
    }

    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint lineA = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint lineB = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint lineBase = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillA = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path pathA = new Path();
    private final Path areaA = new Path();
    private final Path pathB = new Path();
    private final Path pathBase = new Path();

    private double[] seriesA = new double[0];
    private double[] seriesB = new double[0];
    private double[] baselineA = null;
    private String labelA = "";
    private String labelB = "";
    private int colorA = inkColor;
    private int colorB = Color.parseColor("#717785");
    private String unitA = "";
    private String unitB = "";

    public ChartView(Context context) { this(context, null); }
    public ChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        float dp = context.getResources().getDisplayMetrics().density;
        gridPaint.setColor(gridColor);
        gridPaint.setStrokeWidth(0.8f * dp);

        textPaint.setColor(subColor);
        textPaint.setTextSize(11f * dp);

        lineA.setStyle(Paint.Style.STROKE);
        lineA.setStrokeWidth(2.1f * dp);
        lineA.setStrokeCap(Paint.Cap.ROUND);
        lineA.setStrokeJoin(Paint.Join.ROUND);

        lineB.setStyle(Paint.Style.STROKE);
        lineB.setStrokeWidth(1.6f * dp);
        lineB.setStrokeCap(Paint.Cap.ROUND);
        lineB.setStrokeJoin(Paint.Join.ROUND);

        lineBase.setStyle(Paint.Style.STROKE);
        lineBase.setStrokeWidth(1.8f * dp);
        lineBase.setColor(Color.parseColor("#FBBF24"));
        lineBase.setPathEffect(new DashPathEffect(new float[]{6f * dp, 4f * dp}, 0));

        fillA.setStyle(Paint.Style.FILL);
        dotPaint.setStyle(Paint.Style.FILL);
    }

    public void setData(double[] a, String nameA, String suffixA, int colA,
                        double[] b, String nameB, String suffixB, int colB) {
        this.seriesA = a != null ? a : new double[0];
        this.seriesB = b != null ? b : new double[0];
        this.labelA = nameA;
        this.unitA = suffixA;
        this.colorA = colA;
        this.labelB = nameB;
        this.unitB = suffixB;
        this.colorB = colB;
        invalidate();
    }

    public void setBaseline(double[] baseA) {
        this.baselineA = (baseA != null && baseA.length >= 2) ? baseA : null;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        if (w <= 0 || h <= 0) return;

        float dp = getResources().getDisplayMetrics().density;
        float padL = 12 * dp, padR = 12 * dp, padT = 28 * dp, padB = 18 * dp;
        float plotW = Math.max(1f, w - padL - padR);
        float plotH = Math.max(1f, h - padT - padB);

        for (int i = 0; i <= 3; i++) {
            float y = padT + plotH * (i / 3f);
            canvas.drawLine(padL, y, w - padR, y, gridPaint);
        }

        float lastA = seriesA.length > 0 ? (float) seriesA[seriesA.length - 1] : 0f;
        float lastB = seriesB.length > 0 ? (float) seriesB[seriesB.length - 1] : 0f;

        dotPaint.setColor(colorA);
        canvas.drawCircle(padL + 5 * dp, 13 * dp, 4f * dp, dotPaint);
        textPaint.setColor(colorA);
        String legA = String.format(Locale.ROOT, "%s: %.1f%s", labelA, lastA, unitA);
        canvas.drawText(legA, padL + 13 * dp, 17 * dp, textPaint);

        if (baselineA != null) {
            float xBase = w * 0.52f;
            float baseLast = (float) baselineA[Math.min(baselineA.length - 1, Math.max(0, seriesA.length - 1))];
            dotPaint.setColor(Color.parseColor("#FBBF24"));
            canvas.drawCircle(xBase, 13 * dp, 4f * dp, dotPaint);
            textPaint.setColor(Color.parseColor("#FBBF24"));
            canvas.drawText(Strings.f("База A: %.1f%s", baseLast, unitA), xBase + 9 * dp, 17 * dp, textPaint);
        } else if (seriesB.length > 0 && !labelB.isEmpty()) {
            float xB = w * 0.52f;
            dotPaint.setColor(colorB);
            canvas.drawCircle(xB, 13 * dp, 4f * dp, dotPaint);
            textPaint.setColor(colorB);
            canvas.drawText(String.format(Locale.ROOT, "%s: %.1f%s", labelB, lastB, unitB), xB + 9 * dp, 17 * dp, textPaint);
        }

        if (seriesA.length < 2) {
            textPaint.setColor(subColor);
            canvas.drawText(Strings.t("Зробіть ще 1 хід, щоб побачити динаміку..."), padL + 8 * dp, padT + plotH * 0.5f, textPaint);
            return;
        }

        if (baselineA != null) {
            // Shared scale for apples-to-apples A vs B comparison
            double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
            for (double v : seriesA) { if (v < min) min = v; if (v > max) max = v; }
            for (double v : baselineA) { if (v < min) min = v; if (v > max) max = v; }
            if (Math.abs(max - min) < 0.01) { min -= 1; max += 1; }
            else { double m = (max - min) * 0.12; min -= m; max += m; }
            drawOnRange(canvas, baselineA, pathBase, lineBase, false, Color.parseColor("#FBBF24"),
                    min, max, Math.max(seriesA.length, baselineA.length), padL, padT, plotW, plotH, dp);
            lineA.setColor(colorA);
            drawOnRange(canvas, seriesA, pathA, lineA, true, colorA,
                    min, max, Math.max(seriesA.length, baselineA.length), padL, padT, plotW, plotH, dp);
        } else {
            drawAutoScaled(canvas, seriesA, colorA, true, padL, padT, plotW, plotH, dp);
            if (seriesB.length >= 2) {
                drawAutoScaled(canvas, seriesB, colorB, false, padL, padT, plotW, plotH, dp);
            }
        }
    }

    private void drawAutoScaled(Canvas canvas, double[] data, int color, boolean fill,
                                float padL, float padT, float plotW, float plotH, float dp) {
        double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
        for (double v : data) { if (v < min) min = v; if (v > max) max = v; }
        if (Math.abs(max - min) < 0.01) { min -= 1.0; max += 1.0; }
        else { double margin = (max - min) * 0.12; min -= margin; max += margin; }
        Paint p = fill ? lineA : lineB;
        p.setColor(color);
        drawOnRange(canvas, data, fill ? pathA : pathB, p, fill, color, min, max, data.length, padL, padT, plotW, plotH, dp);
    }

    private void drawOnRange(Canvas canvas, double[] data, Path line, Paint paint, boolean fill, int color,
                             double min, double max, int totalSteps,
                             float padL, float padT, float plotW, float plotH, float dp) {
        line.reset();
        areaA.reset();
        int denom = Math.max(1, totalSteps - 1);
        float lastX = padL, lastY = padT + plotH;
        for (int i = 0; i < data.length; i++) {
            float x = padL + plotW * (i / (float) denom);
            float norm = (float) ((data[i] - min) / (max - min));
            float y = padT + plotH * (1f - norm);
            if (i == 0) {
                line.moveTo(x, y);
                if (fill) { areaA.moveTo(x, padT + plotH); areaA.lineTo(x, y); }
            } else {
                line.lineTo(x, y);
                if (fill) areaA.lineTo(x, y);
            }
            lastX = x;
            lastY = y;
        }
        if (fill) {
            areaA.lineTo(lastX, padT + plotH);
            areaA.close();
            int topAlpha = Color.argb(54, Color.red(color), Color.green(color), Color.blue(color));
            fillA.setShader(new LinearGradient(0, padT, 0, padT + plotH, topAlpha, Color.TRANSPARENT, Shader.TileMode.CLAMP));
            canvas.drawPath(areaA, fillA);
        }
        canvas.drawPath(line, paint);
        dotPaint.setColor(color);
        canvas.drawCircle(lastX, lastY, 3.6f * dp, dotPaint);
    }
}
