package com.poselib.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/** 所有页面的基类：统一内边距、顶部条、配色、使用时长统计 */
public class BaseActivity extends Activity {

    protected FrameLayout root;
    protected App.Style s;
    protected TextView subView;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
    }

    /** 在 onCreate 里第一步调用 */
    protected void baseInit() {
        s = App.app().style();
        Window w = getWindow();
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        w.setStatusBarColor(s.bg);
        w.setNavigationBarColor(s.bg);
        if (Build.VERSION.SDK_INT >= 23) {
            View dv = w.getDecorView();
            int flags = dv.getSystemUiVisibility();
            if (s.text == 0xFFF2F4F7) {
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            dv.setSystemUiVisibility(flags);
        }
        root = new FrameLayout(this);
        root.setBackgroundColor(s.bg);
        setContentView(root, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    protected int screenW() {
        return getResources().getDisplayMetrics().widthPixels;
    }

    protected void go(Class<?> target) {
        startActivity(new Intent(this, target));
    }

    protected void go(String source) {
        Intent i = new Intent(this, GridActivity.class);
        i.putExtra("source", source);
        startActivity(i);
    }

    /** 顶部条：返回 + 标题（可带副标题）+ 右侧按钮 */
    protected View topBar(String title, boolean back, View right, String sub) {
        LinearLayout bar = Ui.row(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(Ui.dp(this, 6), Ui.statusBarHeight(this) + Ui.dp(this, 8),
                Ui.dp(this, 12), Ui.dp(this, 8));

        if (back) {
            TextView b = Ui.icon(this, "‹", 22, s.text);
            int p = Ui.dp(this, 8);
            b.setPadding(p, 0, p, 0);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            bar.addView(b);
        }

        LinearLayout col = Ui.col(this);
        LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        col.setLayoutParams(cLp);
        TextView t = Ui.text(this, title, 17, s.text, true);
        t.setSingleLine(true);
        col.addView(t);
        if (sub != null && !sub.isEmpty()) {
            TextView st = Ui.text(this, sub, 10.5f, s.text2, false);
            st.setSingleLine(true);
            LinearLayout.LayoutParams sLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            sLp.topMargin = Ui.dp(this, 2);
            st.setLayoutParams(sLp);
            col.addView(st);
            subView = st;
        }
        bar.addView(col);

        if (right != null) bar.addView(right);
        return bar;
    }

    @Override
    protected void onResume() {
        super.onResume();
        UserStore.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        UserStore.onPause();
    }
}
