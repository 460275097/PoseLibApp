package com.poselib.app;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** 打开次数、连续打卡、浏览历史、使用时长、分类偏好 */
public class UserStore {

    public static final String P_OPEN = "open_cnt";
    public static final String P_VIEW = "view_cnt";
    public static final String P_ZOOM = "zoom_cnt";
    public static final String P_STREAK = "streak";
    public static final String P_LASTDAY = "last_day";
    public static final String P_DAYS = "checkin_days";
    public static final String P_FB = "fb_cnt";

    private static final int HISTORY_MAX = 20;
    private static final SimpleDateFormat DAY = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static long sessionStart = 0L;

    private static String today() {
        return DAY.format(new Date());
    }

    private static String yesterday() {
        long t = System.currentTimeMillis() - 24L * 3600 * 1000;
        return DAY.format(new Date(t));
    }

    /** 每次启动：打开次数 +1；当天首次打开算一次打卡 */
    public static void onLaunch(Context c) {
        App.p().bump(P_OPEN);
        String last = App.p().numStr(P_LASTDAY, "");
        String now = today();
        if (!now.equals(last)) {
            if (now.equals(yesterday())) {
                App.p().setNum(P_STREAK, App.p().num(P_STREAK, 0) + 1);
            } else {
                App.p().setNum(P_STREAK, 1);
            }
            App.p().setNum(P_DAYS, App.p().num(P_DAYS, 0) + 1);
            App.p().setStr(P_LASTDAY, now);
        } else if (App.p().num(P_STREAK, 0) == 0) {
            App.p().setNum(P_STREAK, 1);
            App.p().setNum(P_DAYS, Math.max(1, App.p().num(P_DAYS, 0)));
        }
    }

    // ---- 使用时长（前后台切换累计） ----

    public static void onResume() {
        if (sessionStart == 0) sessionStart = System.currentTimeMillis();
    }

    public static void onPause() {
        if (sessionStart == 0) return;
        long d = System.currentTimeMillis() - sessionStart;
        sessionStart = 0;
        if (d > 0 && d < 6L * 3600 * 1000) App.p().addDuration(d);
    }

    public static long durationMs() {
        return App.p().duration();
    }

    public static long longestSessionMs() {
        return App.p().longestSession();
    }

    public static String fmtDuration(long ms) {
        if (ms <= 0) return "0 分钟";
        long min = ms / 60000L;
        if (min < 1) return "不到 1 分钟";
        if (min < 60) return min + " 分钟";
        long h = min / 60;
        long m = min % 60;
        return m == 0 ? h + " 小时" : h + " 小时 " + m + " 分钟";
    }

    public static int openCount() {
        return App.p().num(P_OPEN, 0);
    }

    public static int streak() {
        String last = App.p().numStr(P_LASTDAY, "");
        if (!last.equals(today()) && !last.equals(yesterday())) return 0;
        return App.p().num(P_STREAK, 0);
    }

    public static int checkinDays() {
        return App.p().num(P_DAYS, 0);
    }

    public static boolean checkedToday() {
        return today().equals(App.p().numStr(P_LASTDAY, ""));
    }

    public static int learnedCount() {
        return App.p().learned().size();
    }

    public static int viewedCount() {
        return App.p().num(P_VIEW, 0);
    }

    public static int zoomCount() {
        return App.p().num(P_ZOOM, 0);
    }

    public static void addZoom() {
        App.p().bump(P_ZOOM);
    }

    /** 成长等级：浏览、学会、连签、使用时长、反馈加权，50 分一级，最高 30 级 */
    public static int score() {
        return viewedCount() + learnedCount() * 3 + streak() * 2
                + (int) (durationMs() / 600000L) + feedbackCount() * 5;
    }

    public static int feedbackCount() {
        return App.p().num(P_FB, 0);
    }

    public static void bumpFeedback() {
        App.p().bump(P_FB);
    }

    public static int level() {
        int score = score();
        int lv = 1 + score / 50;
        return Math.min(30, lv);
    }

    public static String levelName(int lv) {
        if (lv >= 24) return "拍照大师";
        if (lv >= 16) return "构图达人";
        if (lv >= 10) return "姿势熟练";
        if (lv >= 5) return "进阶新手";
        return "刚入门";
    }

    // ---- 最近浏览（最多 120 条） ----

    public static void addHistory(Img img, String nodeTitle) {
        try {
            JSONArray arr = rawHistory();
            JSONObject o = new JSONObject();
            o.put("p", img.path);
            o.put("n", img.name);
            o.put("c", nodeTitle == null ? "" : nodeTitle);
            o.put("k", img.nodeId);
            o.put("ts", System.currentTimeMillis());
            JSONArray out = new JSONArray();
            out.put(o);
            for (int i = 0; i < arr.length() && out.length() < HISTORY_MAX; i++) {
                JSONObject old = arr.optJSONObject(i);
                if (old == null) continue;
                if (o.optString("p").equals(old.optString("p"))) continue;
                out.put(old);
            }
            App.p().setStr("history", out.toString());
            App.p().bump(P_VIEW);
            bumpCategory(nodeTitle);
        } catch (Throwable ignored) {
        }
    }

    private static void bumpCategory(String name) {
        if (name == null || name.isEmpty()) return;
        try {
            JSONObject map = new JSONObject(App.p().numStr("cat_cnt", "{}"));
            map.put(name, map.optInt(name, 0) + 1);
            App.p().setStr("cat_cnt", map.toString());
        } catch (Throwable ignored) {
        }
    }

    private static JSONArray rawHistory() {
        try {
            JSONArray arr = new JSONArray(App.p().numStr("history", "[]"));
            if (arr.length() > HISTORY_MAX) {
                JSONArray out = new JSONArray();
                for (int i = 0; i < HISTORY_MAX; i++) out.put(arr.get(i));
                App.p().setStr("history", out.toString());
                return out;
            }
            return arr;
        } catch (Throwable e) {
            return new JSONArray();
        }
    }

    public static List<Img> history() {
        List<Img> out = new ArrayList<>();
        JSONArray arr = rawHistory();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.optJSONObject(i);
            if (o == null) continue;
            out.add(new Img(o.optString("p"), o.optString("n"), o.optString("k")));
        }
        return out;
    }

    /** 看得最多的分类 */
    public static String topCategory() {
        String best = "—";
        int max = 0;
        try {
            JSONObject map = new JSONObject(App.p().numStr("cat_cnt", "{}"));
            java.util.Iterator<String> it = map.keys();
            while (it.hasNext()) {
                String k = it.next();
                int v = map.optInt(k, 0);
                if (v > max) {
                    max = v;
                    best = k;
                }
            }
        } catch (Throwable ignored) {
        }
        return best;
    }

    /** 分类浏览排行（前 n 名） */
    public static Map<String, Integer> topCategories(int n) {
        Map<String, Integer> all = new HashMap<>();
        try {
            JSONObject map = new JSONObject(App.p().numStr("cat_cnt", "{}"));
            java.util.Iterator<String> it = map.keys();
            while (it.hasNext()) {
                String k = it.next();
                all.put(k, map.optInt(k, 0));
            }
        } catch (Throwable ignored) {
        }
        Map<String, Integer> out = new java.util.LinkedHashMap<>();
        while (out.size() < n && !all.isEmpty()) {
            String best = null;
            int max = -1;
            for (Map.Entry<String, Integer> e : all.entrySet()) {
                if (e.getValue() > max) {
                    max = e.getValue();
                    best = e.getKey();
                }
            }
            if (best == null) break;
            out.put(best, max);
            all.remove(best);
        }
        return out;
    }
}
