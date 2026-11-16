package com.poselib.app;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Map;

/** 我的数据 + 全网排行（匿名计数） */
public class StatsActivity extends BaseActivity {

    @Override
    protected void onCreate(android.os.Bundle b) {
        super.onCreate(b);
        baseInit();

        LinearLayout page = Ui.col(this);
        root.addView(page, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        page.addView(topBar("我的数据", true, null, "坚持练，姿势就长在身上"));

        ScrollView sv = new ScrollView(this);
        sv.setVerticalScrollBarEnabled(false);
        page.addView(sv, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        LinearLayout list = Ui.col(this);
        list.setPadding(Ui.dp(this, 14), Ui.dp(this, 2), Ui.dp(this, 14), Ui.dp(this, 24));
        sv.addView(list);

        list.addView(levelCard());
        list.addView(sectionTitle("成长数据"));
        list.addView(metricGrid());
        list.addView(sectionTitle("我最常看的分类"));
        list.addView(topCategoryCard());
        list.addView(sectionTitle("全网排行"));
        list.addView(globalCard());
        list.addView(note());
    }

    private View levelCard() {
        int lv = UserStore.level();
        LinearLayout card = Ui.col(this);
        card.setBackground(Ui.round(this, s.surface, 16));
        card.setPadding(Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16));

        LinearLayout row = Ui.row(this);
        row.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout badge = Ui.col(this);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(Ui.round(this, s.accent, 12));
        badge.setLayoutParams(new LinearLayout.LayoutParams(
                Ui.dp(this, 52), Ui.dp(this, 52)));
        badge.addView(Ui.icon(this, "LV", 9, 0xFFFFFFFF));
        TextView lvText = Ui.text(this, String.valueOf(lv), 18, 0xFFFFFFFF, true);
        badge.addView(lvText);
        row.addView(badge);

        LinearLayout col = Ui.col(this);
        LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        cLp.leftMargin = Ui.dp(this, 12);
        col.setLayoutParams(cLp);
        col.addView(Ui.text(this, UserStore.levelName(lv), 16, s.text, true));
        int score = UserStore.score();
        TextView st = Ui.text(this, "活跃分 " + score + " · 已学会 " + UserStore.learnedCount()
                + " 个姿势 · 反馈 " + UserStore.feedbackCount() + " 条", 11, s.text2, false);
        st.setPadding(0, Ui.dp(this, 4), 0, 0);
        col.addView(st);
        row.addView(col);
        card.addView(row);

        LinearLayout chips = Ui.row(this);
        LinearLayout.LayoutParams chipLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        chipLp.topMargin = Ui.dp(this, 14);
        chips.setLayoutParams(chipLp);
        chips.addView(miniStat("今天", UserStore.checkedToday() ? "已打卡" : "未打卡", 0));
        chips.addView(miniStat("连续", UserStore.streak() + " 天", Ui.dp(this, 8)));
        chips.addView(miniStat("累计打卡", UserStore.checkinDays() + " 天", Ui.dp(this, 8)));
        card.addView(chips);
        return card;
    }

    private View miniStat(String label, String value, int marginRight) {
        LinearLayout box = Ui.col(this);
        box.setGravity(Gravity.CENTER);
        box.setBackground(Ui.round(this, s.surface2, 10));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(this, 48), 1f);
        lp.rightMargin = marginRight;
        box.setLayoutParams(lp);
        box.addView(Ui.text(this, value, 13, s.text, true));
        box.addView(Ui.text(this, label, 9.5f, s.text2, false));
        return box;
    }

    private View sectionTitle(String t) {
        TextView v = Ui.text(this, t, 13.5f, s.text, true);
        v.setPadding(Ui.dp(this, 2), Ui.dp(this, 18), 0, Ui.dp(this, 8));
        return v;
    }

    private View metricGrid() {
        LinearLayout wrap = Ui.col(this);
        wrap.setBackground(Ui.round(this, s.surface, 14));
        wrap.setPadding(Ui.dp(this, 4), Ui.dp(this, 6), Ui.dp(this, 4), Ui.dp(this, 6));

        String[][] items = {
                {"累计浏览姿势", UserStore.viewedCount() + " 张"},
                {"收藏姿势总数", App.p().favorites().size() + " 个"},
                {"放大查看次数", UserStore.zoomCount() + " 次"},
                {"标记学会", UserStore.learnedCount() + " 个"},
                {"累计使用时长", UserStore.fmtDuration(UserStore.durationMs())},
                {"单次最长", UserStore.fmtDuration(UserStore.longestSessionMs())},
                {"打开次数", UserStore.openCount() + " 次"},
                {"最近浏览", UserStore.history().size() + " 条记录"},
        };

        for (int i = 0; i < items.length; i += 2) {
            LinearLayout row = Ui.row(this);
            LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rLp.bottomMargin = Ui.dp(this, 2);
            row.setLayoutParams(rLp);
            row.addView(cell(items[i][0], items[i][1]));
            row.addView(cell(items[i + 1][0], items[i + 1][1]));
            wrap.addView(row);
        }
        return wrap;
    }

    private View cell(String label, String value) {
        LinearLayout box = Ui.col(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        box.setLayoutParams(lp);
        box.setPadding(Ui.dp(this, 10), Ui.dp(this, 9), Ui.dp(this, 6), Ui.dp(this, 9));
        box.addView(Ui.text(this, label, 10, s.text2, false));
        TextView v = Ui.text(this, value, 14, s.text, true);
        v.setPadding(0, Ui.dp(this, 3), 0, 0);
        box.addView(v);
        return box;
    }

    private View topCategoryCard() {
        LinearLayout card = Ui.col(this);
        card.setBackground(Ui.round(this, s.surface, 14));
        card.setPadding(Ui.dp(this, 13), Ui.dp(this, 11), Ui.dp(this, 13), Ui.dp(this, 11));

        Map<String, Integer> top = UserStore.topCategories(5);
        if (top.isEmpty()) {
            card.addView(Ui.text(this, "还没看多少图，先去翻一翻分类吧", 11.5f, s.text2, false));
            return card;
        }
        int max = 1;
        for (int v : top.values()) max = Math.max(max, v);
        int i = 1;
        for (Map.Entry<String, Integer> e : top.entrySet()) {
            LinearLayout row = Ui.row(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rLp.topMargin = i == 1 ? 0 : Ui.dp(this, 9);
            row.setLayoutParams(rLp);

            TextView idx = Ui.text(this, String.valueOf(i), 11,
                    i == 1 ? s.accent : s.text2, true);
            idx.setLayoutParams(new LinearLayout.LayoutParams(
                    Ui.dp(this, 18), ViewGroup.LayoutParams.WRAP_CONTENT));
            row.addView(idx);

            TextView name = Ui.text(this, e.getKey(), 11.5f, s.text, false);
            name.setLayoutParams(new LinearLayout.LayoutParams(
                    Ui.dp(this, 74), ViewGroup.LayoutParams.WRAP_CONTENT));
            name.setSingleLine(true);
            row.addView(name);

            FrameLayout bar = new FrameLayout(this);
            bar.setLayoutParams(new LinearLayout.LayoutParams(0, Ui.dp(this, 6), 1f));
            View track = new View(this);
            track.setBackground(Ui.round(this, s.surface2, 3));
            bar.addView(track, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            View fill = new View(this);
            fill.setBackground(Ui.round(this, s.accent, 3));
            int w = (int) (((float) e.getValue() / max) * Ui.dp(this, 240));
            bar.addView(fill, new FrameLayout.LayoutParams(w,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            LinearLayout.LayoutParams bLp = (LinearLayout.LayoutParams) bar.getLayoutParams();
            bLp.leftMargin = Ui.dp(this, 6);
            bLp.rightMargin = Ui.dp(this, 8);
            row.addView(bar);

            row.addView(Ui.text(this, e.getValue() + " 次", 10, s.text2, false));
            card.addView(row);
            i++;
        }
        return card;
    }

    private View globalCard() {
        int cloud = Cloud.cached();
        LinearLayout card = Ui.col(this);
        card.setBackground(Ui.round(this, s.surface, 14));
        card.setPadding(Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 14));

        LinearLayout row = Ui.row(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout col = Ui.col(this);
        col.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        col.addView(Ui.text(this, "全网累计打开", 10.5f, s.text2, false));
        TextView v = Ui.text(this, cloud > 0 ? (cloud + " 次") : "联网后显示", 22,
                s.text, true);
        v.setPadding(0, Ui.dp(this, 4), 0, 0);
        col.addView(v);
        row.addView(col);

        TextView pos = Ui.text(this, "活跃分 " + UserStore.score(), 11, s.accent, true);
        row.addView(pos);
        card.addView(row);

        int users = Cloud.cachedUsers();
        int rank = Cloud.cachedRank();
        LinearLayout people = Ui.row(this);
        LinearLayout.LayoutParams pLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pLp.topMargin = Ui.dp(this, 12);
        people.setLayoutParams(pLp);
        people.addView(miniStat("全网使用人数", users > 0 ? users + " 人" : "联网后显示", Ui.dp(this, 8)));
        people.addView(miniStat("我的排名", rank > 0 ? "第 " + rank + " 位" : "联网后显示", 0));
        card.addView(people);

        card.addView(Ui.divider(this, s.divider));

        TextView me = Ui.text(this, "我的使用节奏：" + UserStore.streak()
                + " 天连签 · 浏览 " + UserStore.viewedCount() + " 张 · 学会 "
                + UserStore.learnedCount() + " 个", 11, s.text, false);
        me.setPadding(0, Ui.dp(this, 10), 0, 0);
        card.addView(me);

        TextView refresh = Ui.text(this, "刷新全网数据", 11, s.accent, true);
        refresh.setPadding(0, Ui.dp(this, 10), 0, 0);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cloud.reportOpen();
                Cloud.refreshUsers();
                recreate();
            }
        });
        card.addView(refresh);
        return card;
    }

    private View note() {
        TextView t = Ui.text(this, "说明：全网使用人数与排名来自本机随机生成的匿名 ID，按首次注册时间排序；"
                + "不上传照片、手机号或个人信息。网络失败时保留上次缓存，名次仅作参考。",
                10, s.text2, false);
        t.setPadding(Ui.dp(this, 4), Ui.dp(this, 16), Ui.dp(this, 4), 0);
        t.setLineSpacing(Ui.dp(this, 3), 1f);
        return t;
    }
}
