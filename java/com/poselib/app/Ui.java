package com.poselib.app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/** 统一的界面零件 */
public class Ui {

    public static int dp(Context c, float v) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                c.getResources().getDisplayMetrics()) + 0.5f);
    }

    public static int statusBarHeight(Context c) {
        int id = c.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (id > 0) return c.getResources().getDimensionPixelSize(id);
        return dp(c, 24);
    }

    public static LinearLayout col(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    public static LinearLayout row(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.HORIZONTAL);
        return l;
    }

    public static TextView text(Context c, String v, float sp, int color, boolean bold) {
        TextView t = new TextView(c);
        t.setText(v);
        t.setTextSize(sp * App.app().style().factor);
        t.setTextColor(color);
        if (bold) t.setTypeface(t.getTypeface(), android.graphics.Typeface.BOLD);
        return t;
    }

    public static TextView icon(Context c, String glyph, float sp, int color) {
        TextView t = new TextView(c);
        t.setText(glyph);
        t.setTextSize(sp);
        t.setTextColor(color);
        t.setGravity(android.view.Gravity.CENTER);
        return t;
    }

    public static GradientDrawable round(Context c, int color, float radiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(c, radiusDp));
        return d;
    }

    public static GradientDrawable roundStroke(Context c, int color, float radiusDp, int stroke) {
        GradientDrawable d = round(c, color, radiusDp);
        d.setStroke(dp(c, 1), stroke);
        return d;
    }

    public static View space(Context c, float h) {
        View v = new View(c);
        v.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(c, h)));
        return v;
    }

    public static View divider(Context c, int color) {
        View v = new View(c);
        v.setBackgroundColor(color);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Math.max(1, dp(c, 0.7f)));
        v.setLayoutParams(lp);
        return v;
    }

    public static int mix(int a, int b, float t) {
        int ra = Color.red(a), ga = Color.green(a), ba = Color.blue(a);
        int rb = Color.red(b), gb = Color.green(b), bb = Color.blue(b);
        return Color.rgb((int) (ra + (rb - ra) * t), (int) (ga + (gb - ga) * t),
                (int) (ba + (bb - ba) * t));
    }
}
