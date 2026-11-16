package com.poselib.app;

import android.app.Application;

/** 全局：图库、设置、配色 */
public class App extends Application {

    public static class Style {
        public int bg, surface, surface2, text, text2, accent, divider;
        public float factor = 1f;

        Style(android.content.Context c) {
            boolean dark = App.p().dark();
            factor = Prefs.tsFactor(App.p().ts());
            bg = dark ? 0xFF0E1013 : 0xFFF7F8FA;
            surface = dark ? 0xFF171A1F : 0xFFFFFFFF;
            surface2 = dark ? 0xFF23272E : 0xFFEDF0F5;
            text = dark ? 0xFFF2F4F7 : 0xFF16181D;
            text2 = dark ? 0xFF98A0AC : 0xFF7A828E;
            accent = dark ? 0xFFFFA733 : 0xFFE86D18;
            divider = dark ? 0xFF2A2F37 : 0xFFE4E8EF;
        }
    }

    private static App inst;
    private Prefs prefs;
    public Library library;
    private Style style;

    @Override
    public void onCreate() {
        super.onCreate();
        inst = this;
        prefs = new Prefs(this);
        library = Library.load(this);
        style = new Style(this);
        UserStore.onLaunch(this);
        Cloud.reportOpen();
        Cloud.refreshUsers();
    }

    public static App app() {
        return inst;
    }

    public static Prefs p() {
        return inst.prefs;
    }

    public Style style() {
        return style;
    }

    public boolean dark() {
        return prefs.dark();
    }

    public int surface() {
        return style.surface;
    }

    public int surface2() {
        return style.surface2;
    }

    /** 设置变更后重建配色 */
    public void refreshStyle() {
        style = new Style(this);
    }
}
