package com.poselib.app;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/** 所有本地设置与用户数据 */
public class Prefs {

    private final SharedPreferences sp;

    public Prefs(Context c) {
        sp = c.getSharedPreferences("poselib", Context.MODE_PRIVATE);
    }

    // ---- 文字大小：0 极小 / 1 小 / 2 标准 / 3 大 ----

    public int ts() {
        int v = sp.getInt("ts", 2);
        return (v >= 0 && v <= 3) ? v : 2;
    }

    public void setTs(int v) {
        sp.edit().putInt("ts", v).apply();
    }

    public static String tsName(int v) {
        switch (v) {
            case 0: return "极小";
            case 1: return "小";
            case 3: return "大";
            default: return "标准";
        }
    }

    public static float tsFactor(int v) {
        switch (v) {
            case 0: return 0.82f;
            case 1: return 0.92f;
            case 3: return 1.18f;
            default: return 1.0f;
        }
    }

    // ---- 深色模式 ----

    public boolean dark() {
        return sp.getBoolean("dark", false);
    }

    public void setDark(boolean v) {
        sp.edit().putBoolean("dark", v).apply();
    }

    // ---- 默认镜像 ----

    public boolean mirror() {
        return sp.getBoolean("mirror", false);
    }

    public void setMirror(boolean v) {
        sp.edit().putBoolean("mirror", v).apply();
    }

    // ---- 宫格列数：默认 2 列，可选 2–6 列 ----

    public int columns() {
        int v = sp.getInt("cols", 2);
        return (v >= 2 && v <= 6) ? v : 2;
    }

    public void setColumns(int v) {
        sp.edit().putInt("cols", v).apply();
    }

    // ---- 收藏 ----

    public Set<String> favorites() {
        return new HashSet<>(sp.getStringSet("fav", new HashSet<>()));
    }

    public boolean isFavorite(String key) {
        return sp.getStringSet("fav", new HashSet<>()).contains(key);
    }

    public boolean toggleFavorite(String key) {
        Set<String> s = new HashSet<>(sp.getStringSet("fav", new HashSet<>()));
        boolean on;
        if (s.contains(key)) {
            s.remove(key);
            on = false;
        } else {
            s.add(key);
            on = true;
        }
        sp.edit().putStringSet("fav", s).apply();
        return on;
    }

    // ---- 已学会 ----

    public Set<String> learned() {
        return new HashSet<>(sp.getStringSet("learned", new HashSet<>()));
    }

    public boolean toggleLearned(String key) {
        Set<String> s = new HashSet<>(sp.getStringSet("learned", new HashSet<>()));
        boolean on;
        if (s.contains(key)) {
            s.remove(key);
            on = false;
        } else {
            s.add(key);
            on = true;
        }
        sp.edit().putStringSet("learned", s).apply();
        return on;
    }

    // ---- 已从我的图库移除（删除） ----

    public Set<String> hidden() {
        return new HashSet<>(sp.getStringSet("hidden", new HashSet<>()));
    }

    public void hide(String key) {
        Set<String> s = new HashSet<>(sp.getStringSet("hidden", new HashSet<>()));
        s.add(key);
        sp.edit().putStringSet("hidden", s).apply();
    }

    public void unhideAll() {
        sp.edit().remove("hidden").apply();
    }

    public boolean isHidden(String key) {
        return sp.getStringSet("hidden", new HashSet<>()).contains(key);
    }

    // ---- 首页分类速览：自定义顺序 + 隐藏 ----

    public String homeOrder() {
        return sp.getString("home_order", "");
    }

    public void setHomeOrder(String v) {
        sp.edit().putString("home_order", v).apply();
    }

    public Set<String> homeHidden() {
        return new HashSet<>(sp.getStringSet("home_hidden", new HashSet<>()));
    }

    public boolean isHomeHidden(String id) {
        return sp.getStringSet("home_hidden", new HashSet<>()).contains(id);
    }

    public void toggleHomeHidden(String id) {
        Set<String> s = new HashSet<>(sp.getStringSet("home_hidden", new HashSet<>()));
        if (s.contains(id)) s.remove(id);
        else s.add(id);
        sp.edit().putStringSet("home_hidden", s).apply();
    }

    public void unhideHomeAll() {
        sp.edit().remove("home_hidden").apply();
    }

    // ---- 目录分类：自定义顺序 + 隐藏（与快速分类独立保存） ----

    public String catalogOrder() {
        return sp.getString("cat_order", "");
    }

    public void setCatalogOrder(String v) {
        sp.edit().putString("cat_order", v).apply();
    }

    public Set<String> catalogHidden() {
        return new HashSet<>(sp.getStringSet("cat_hidden", new HashSet<>()));
    }

    public boolean isCatalogHidden(String id) {
        return sp.getStringSet("cat_hidden", new HashSet<>()).contains(id);
    }

    public void toggleCatalogHidden(String id) {
        Set<String> s = new HashSet<>(sp.getStringSet("cat_hidden", new HashSet<>()));
        if (s.contains(id)) s.remove(id);
        else s.add(id);
        sp.edit().putStringSet("cat_hidden", s).apply();
    }

    public void unhideCatalogAll() {
        sp.edit().remove("cat_hidden").apply();
    }

    /** 首页当前展示的分组：quick 快速分类 / catalog 目录分类 */
    public String homeTab() {
        String v = sp.getString("home_tab", "quick");
        return "catalog".equals(v) ? "catalog" : "quick";
    }

    public void setHomeTab(String v) {
        sp.edit().putString("home_tab", "catalog".equals(v) ? "catalog" : "quick").apply();
    }

    /** 一键恢复默认：只重置界面、布局与分组偏好，保留收藏/学会/历史/打卡 */
    public void resetDefaults() {
        sp.edit()
                .remove("ts").remove("dark").remove("mirror").remove("cols")
                .remove("home_order").remove("home_hidden")
                .remove("cat_order").remove("cat_hidden")
                .remove("home_tab")
                .apply();
    }

    // ---- 计数 / 打卡 ----

    public int num(String key, int def) {
        return sp.getInt(key, def);
    }

    public void setNum(String key, int v) {
        sp.edit().putInt(key, v).apply();
    }

    public void bump(String key) {
        sp.edit().putInt(key, num(key, 0) + 1).apply();
    }

    public String numStr(String key, String def) {
        return sp.getString(key, def);
    }

    public void setStr(String key, String v) {
        sp.edit().putString(key, v).apply();
    }

    // ---- 使用时长 ----

    public long duration() {
        return sp.getLong("dur_ms", 0L);
    }

    public long longestSession() {
        return sp.getLong("dur_max", 0L);
    }

    public void addDuration(long ms) {
        long total = duration() + ms;
        long max = Math.max(longestSession(), ms);
        sp.edit().putLong("dur_ms", total).putLong("dur_max", max).apply();
    }

    // ---- 全网统计缓存 ----

    public int cloudCount() {
        return sp.getInt("cloud_cnt", 0);
    }

    public void setCloudCount(int v) {
        sp.edit().putInt("cloud_cnt", v).apply();
    }
}
