package com.poselib.app;

import android.graphics.Bitmap;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/** 拍照技巧图文：数码相机 / 安卓 / 苹果 / AI 修图 / 参数 / 华为 / OPPO / 荣耀 */
public class TipsActivity extends BaseActivity {

    @Override
    protected void onCreate(android.os.Bundle b) {
        super.onCreate(b);
        baseInit();

        JSONArray arr = null;
        try {
            arr = read();
        } catch (Throwable e) {
            arr = null;
        }
        int n = arr == null ? 0 : arr.length();

        LinearLayout page = Ui.col(this);
        root.addView(page, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        page.addView(topBar("拍照技巧", true, null, n + " 个主题 · 看完就能上手"));

        ScrollView sv = new ScrollView(this);
        sv.setVerticalScrollBarEnabled(false);
        page.addView(sv, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        LinearLayout list = Ui.col(this);
        list.setPadding(Ui.dp(this, 14), Ui.dp(this, 4), Ui.dp(this, 14), Ui.dp(this, 20));
        sv.addView(list);

        if (arr != null) {
            try {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    list.addView(card(o, i));
                }
            } catch (Throwable e) {
                list.addView(Ui.text(this, "技巧内容加载失败", 13, s.text2, false));
            }
        } else {
            list.addView(Ui.text(this, "技巧内容加载失败", 13, s.text2, false));
        }
    }

    private JSONArray read() throws Exception {
        InputStream is = getAssets().open("tips.json");
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();
        JSONObject o = new JSONObject(sb.toString());
        JSONArray arr = o.optJSONArray("topics");
        if (arr == null) arr = o.optJSONArray("items");
        return arr;
    }

    private View card(JSONObject o, int i) {
        LinearLayout card = Ui.col(this);
        card.setBackground(Ui.round(this, s.surface, 14));
        card.setClipToOutline(true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = Ui.dp(this, 12);
        card.setLayoutParams(lp);

        String img = o.optString("image", "");
        if (!img.isEmpty()) {
            ImageView iv = new ImageView(this);
            int h = (int) (screenW() * 0.44f);
            iv.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, h));
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setBackgroundColor(s.surface2);
            card.addView(iv);
            Thumbs.into(iv, new Img(img, o.optString("title", ""), "tips"), h);
        }

        LinearLayout body = Ui.col(this);
        body.setPadding(Ui.dp(this, 13), Ui.dp(this, 11), Ui.dp(this, 13), Ui.dp(this, 13));
        card.addView(body);

        LinearLayout head = Ui.row(this);
        head.setGravity(Gravity.CENTER_VERTICAL);
        TextView num = Ui.text(this, String.format("%02d", i + 1), 10, s.accent, true);
        num.setPadding(0, 0, Ui.dp(this, 6), 0);
        head.addView(num);
        TextView title = Ui.text(this, o.optString("title", ""), 15, s.text, true);
        head.addView(title);
        body.addView(head);

        String sub = o.optString("sub", "");
        if (!sub.isEmpty()) {
            TextView st = Ui.text(this, sub, 11, s.text2, false);
            st.setPadding(0, Ui.dp(this, 4), 0, 0);
            st.setLineSpacing(Ui.dp(this, 2), 1f);
            body.addView(st);
        }

        JSONArray pts = o.optJSONArray("points");
        if (pts != null) {
            for (int j = 0; j < pts.length(); j++) {
                LinearLayout row = Ui.row(this);
                LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                rLp.topMargin = Ui.dp(this, 7);
                row.setLayoutParams(rLp);
                row.setGravity(Gravity.TOP);
                TextView dot = Ui.text(this, "·", 12, s.accent, true);
                dot.setPadding(0, 0, Ui.dp(this, 5), 0);
                row.addView(dot);
                TextView t = Ui.text(this, pts.optString(j, ""), 11.8f, s.text, false);
                t.setLayoutParams(new LinearLayout.LayoutParams(0,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                t.setLineSpacing(Ui.dp(this, 3), 1f);
                row.addView(t);
                body.addView(row);
            }
        }
        return card;
    }
}
