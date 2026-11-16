package com.poselib.app;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.util.LinkedHashSet;
import java.util.Set;

/** 搜索图片名称或分类 */
public class SearchActivity extends BaseActivity {

    @Override
    protected void onCreate(android.os.Bundle b) {
        super.onCreate(b);
        baseInit();

        LinearLayout page = Ui.col(this);
        root.addView(page, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        page.addView(topBar("搜索", true, null, App.app().library.total + " 张姿势"));

        EditText input = new EditText(this);
        input.setHint("试试「站姿」「街拍」「咖啡」");
        input.setHintTextColor(s.text2);
        input.setTextColor(s.text);
        input.setTextSize(13);
        input.setSingleLine(true);
        input.setBackground(Ui.round(this, s.surface2, 12));
        input.setPadding(Ui.dp(this, 12), Ui.dp(this, 8), Ui.dp(this, 12), Ui.dp(this, 8));
        LinearLayout.LayoutParams iLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 44));
        iLp.leftMargin = Ui.dp(this, 14);
        iLp.rightMargin = Ui.dp(this, 14);
        iLp.topMargin = Ui.dp(this, 6);
        input.setLayoutParams(iLp);
        page.addView(input);

        TextView btn = Ui.text(this, "搜索", 13, 0xFFFFFFFF, true);
        btn.setGravity(Gravity.CENTER);
        btn.setBackground(Ui.round(this, s.accent, 10));
        LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 42));
        bLp.leftMargin = Ui.dp(this, 14);
        bLp.rightMargin = Ui.dp(this, 14);
        bLp.topMargin = Ui.dp(this, 10);
        btn.setLayoutParams(bLp);
        page.addView(btn);

        ScrollView sv = new ScrollView(this);
        page.addView(sv, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        LinearLayout list = Ui.col(this);
        list.setPadding(Ui.dp(this, 14), Ui.dp(this, 10), Ui.dp(this, 14), Ui.dp(this, 20));
        sv.addView(list);

        TextView hot = Ui.text(this, "按分类找", 12, s.text2, true);
        hot.setPadding(0, Ui.dp(this, 10), 0, Ui.dp(this, 8));
        list.addView(hot);
        list.addView(chips(homeTitles()));

        TextView recentTitle = Ui.text(this, "最近搜索", 12, s.text2, true);
        recentTitle.setPadding(0, Ui.dp(this, 20), 0, Ui.dp(this, 8));
        list.addView(recentTitle);
        Set<String> recent = recent();
        if (recent.isEmpty()) {
            list.addView(Ui.text(this, "还没有搜索记录", 11, s.text2, false));
        } else {
            list.addView(chips(recent));
        }

        final EditText fInput = input;
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search(fInput.getText().toString());
            }
        });
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, android.view.KeyEvent e) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    search(v.getText().toString());
                    return true;
                }
                return false;
            }
        });
    }

    private void search(String q) {
        q = q.trim();
        if (q.isEmpty()) {
            Toast.makeText(this, "输入要搜的词", Toast.LENGTH_SHORT).show();
            return;
        }
        saveRecent(q);
        go(Sources.SEARCH + q);
    }

    private String[] homeTitles() {
        String[] arr = new String[App.app().library.home.size()];
        for (int i = 0; i < arr.length; i++) arr[i] = App.app().library.home.get(i).title;
        return arr;
    }

    private View chips(String[] items) {
        LinearLayout wrap = Ui.col(this);
        LinearLayout row = null;
        int perRow = 4;
        for (int i = 0; i < items.length; i++) {
            if (i % perRow == 0) {
                row = Ui.row(this);
                LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                rLp.bottomMargin = Ui.dp(this, 7);
                row.setLayoutParams(rLp);
                wrap.addView(row);
            }
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            if (i % perRow != perRow - 1) lp.rightMargin = Ui.dp(this, 6);
            TextView chip = Ui.text(this, items[i], 11, s.text, false);
            chip.setGravity(Gravity.CENTER);
            chip.setSingleLine(true);
            chip.setBackground(Ui.round(this, s.surface2, 9));
            chip.setPadding(Ui.dp(this, 4), Ui.dp(this, 9), Ui.dp(this, 4), Ui.dp(this, 9));
            chip.setLayoutParams(lp);
            final String q = items[i];
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    go(Sources.SEARCH + q);
                }
            });
            row.addView(chip);
        }
        return wrap;
    }

    private View chips(Set<String> items) {
        String[] arr = new String[items.size()];
        items.toArray(arr);
        return chips(arr);
    }

    private Set<String> recent() {
        Set<String> out = new LinkedHashSet<>();
        try {
            JSONArray arr = new JSONArray(App.p().numStr("qhist", "[]"));
            for (int i = 0; i < arr.length(); i++) out.add(arr.getString(i));
        } catch (Throwable ignored) {
        }
        return out;
    }

    private void saveRecent(String q) {
        Set<String> set = recent();
        LinkedHashSet<String> out = new LinkedHashSet<>();
        out.add(q);
        for (String s1 : set) {
            if (!s1.equals(q)) out.add(s1);
            if (out.size() >= 10) break;
        }
        JSONArray arr = new JSONArray();
        for (String s1 : out) arr.put(s1);
        App.p().setStr("qhist", arr.toString());
    }
}
