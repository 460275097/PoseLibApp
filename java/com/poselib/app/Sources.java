package com.poselib.app;

import java.util.ArrayList;
import java.util.List;

/** 图片集合来源：全部 / 收藏 / 已学会 / 历史 / 某分类 / 随机 / 搜索 */
public class Sources {

    public static final String ALL = "all";
    public static final String FAV = "fav";
    public static final String LEARNED = "learned";
    public static final String HIST = "history";
    public static final String RANDOM = "random";
    public static final String NODE = "node:";
    public static final String SEARCH = "search:";

    public static final String TITLE_ALL = "全部图片";
    public static final String TITLE_FAV = "我的收藏";
    public static final String TITLE_LEARNED = "已学会";
    public static final String TITLE_HIST = "最近浏览";

    public static String title(String src) {
        if (src == null) return "";
        if (src.startsWith(NODE)) {
            Library.Node n = App.app().library.node(src.substring(NODE.length()));
            return n == null ? "分类" : n.title;
        }
        if (src.startsWith(SEARCH)) return "搜索：" + src.substring(SEARCH.length());
        if (ALL.equals(src)) return TITLE_ALL;
        if (FAV.equals(src)) return TITLE_FAV;
        if (LEARNED.equals(src)) return TITLE_LEARNED;
        if (HIST.equals(src)) return TITLE_HIST;
        if (RANDOM.equals(src)) return "随机一张";
        return "";
    }

    public static List<Img> resolve(String src) {
        Library lib = App.app().library;
        if (src == null) return new ArrayList<>();
        if (src.startsWith(NODE)) {
            return lib.imagesOf(src.substring(NODE.length()));
        }
        if (src.startsWith(SEARCH)) {
            String q = src.substring(SEARCH.length()).trim().toLowerCase();
            List<Img> out = new ArrayList<>();
            if (q.isEmpty()) return out;
            for (Img img : lib.all()) {
                if (img.name.toLowerCase().contains(q)
                        || img.nodeId.toLowerCase().contains(q)) {
                    out.add(img);
                }
            }
            return out;
        }
        if (ALL.equals(src)) return lib.all();
        if (FAV.equals(src)) return byKeys(App.p().favorites());
        if (LEARNED.equals(src)) return byKeys(App.p().learned());
        if (HIST.equals(src)) {
            List<Img> out = new ArrayList<>();
            for (Img img : UserStore.history()) {
                if (!App.p().isHidden(img.key())) out.add(img);
            }
            return out;
        }
        if (RANDOM.equals(src)) return lib.randomPool();
        return new ArrayList<>();
    }

    private static List<Img> byKeys(java.util.Set<String> keys) {
        List<Img> out = new ArrayList<>();
        if (keys.isEmpty()) return out;
        for (Img img : App.app().library.all()) {
            if (keys.contains(img.key())) out.add(img);
        }
        return out;
    }

    /** 分类下所有图片（含子分类），给分类页用 */
    public static int count(String src) {
        return resolve(src).size();
    }
}
