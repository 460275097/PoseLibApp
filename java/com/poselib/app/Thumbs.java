package com.poselib.app;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** 图片解码与内存缓存：不存缩略图文件，宫格按格子尺寸降采样解码，大图按屏幕高分屏解码 */
public class Thumbs {

    public static final int PLACEHOLDER = 0xFFEFF1F5;
    public static final int VIEW_TARGET = 1400;
    public static final int HI_TARGET = 2800;

    private static ExecutorService pool;
    private static LruCache<String, Bitmap> gridCache;
    private static LruCache<String, Bitmap> viewCache;
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    public static ExecutorService pool() {
        if (pool == null) pool = Executors.newFixedThreadPool(4);
        return pool;
    }

    public static LruCache<String, Bitmap> cache() {
        if (gridCache == null) {
            int max = (int) (Runtime.getRuntime().maxMemory() / 4);
            gridCache = new LruCache<String, Bitmap>(max) {
                @Override
                protected int sizeOf(String key, Bitmap value) {
                    return value.getByteCount();
                }
            };
        }
        return gridCache;
    }

    public static LruCache<String, Bitmap> viewCache() {
        if (viewCache == null) {
            int max = (int) (Runtime.getRuntime().maxMemory() / 5);
            viewCache = new LruCache<String, Bitmap>(max) {
                @Override
                protected int sizeOf(String key, Bitmap value) {
                    return value.getByteCount();
                }
            };
        }
        return viewCache;
    }

    public static void clearViewCache() {
        if (viewCache != null) viewCache.evictAll();
    }

    private static InputStream open(Context c, String path) throws Exception {
        AssetManager am = c.getApplicationContext().getAssets();
        return am.open(path);
    }

    /**
     * 从原图解码：need = 期望的最长边像素。
     * 不生成缩略图文件，按 2 的幂降采样，保证解码结果落在 [need, 2need) 区间。
     */
    public static Bitmap decode(Context c, String path, int need) throws Exception {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        InputStream is = open(c, path);
        try {
            BitmapFactory.decodeStream(is, null, o);
        } finally {
            try { is.close(); } catch (Exception ignored) { }
        }
        int max = Math.max(o.outWidth, o.outHeight);
        int sample = 1;
        if (need > 0 && max > 0) {
            while (max / sample > need * 1.6f) sample *= 2;
        }
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = sample;
        o2.inPreferredConfig = Bitmap.Config.ARGB_8888;
        o2.inDither = false;
        is = open(c, path);
        try {
            return BitmapFactory.decodeStream(is, null, o2);
        } finally {
            try { is.close(); } catch (Exception ignored) { }
        }
    }

    /** 宫格贴图（同一格尺寸共用缓存） */
    public static void into(final ImageView iv, final Img img, final int targetPx) {
        load(iv, img, targetPx);
    }

    public static void load(final ImageView iv, final Img img, final int targetPx) {
        final Context ctx = iv.getContext().getApplicationContext();
        final String key = targetPx + "|" + img.path;
        iv.setTag(key);
        Bitmap hit = cache().get(key);
        if (hit != null) {
            iv.setImageBitmap(hit);
            return;
        }
        iv.setImageDrawable(null);
        iv.setBackgroundColor(App.app().dark() ? 0xFF1C1F24 : PLACEHOLDER);
        pool().execute(new Runnable() {
            @Override
            public void run() {
                Bitmap bmp = null;
                try {
                    bmp = decode(ctx, img.path, targetPx);
                } catch (Throwable ignored) {
                }
                final Bitmap res = bmp;
                MAIN.post(new Runnable() {
                    @Override
                    public void run() {
                        if (res == null) return;
                        cache().put(key, res);
                        Object tag = iv.getTag();
                        if (tag != null && tag.equals(key)) {
                            iv.setBackgroundColor(0);
                            iv.setImageBitmap(res);
                        }
                    }
                });
            }
        });
    }

    /** 大图解码：先按指定长边，内存不够时逐级回退 */
    public static Bitmap decodeView(Context c, Img img, int need) {
        String key = "v" + need + "|" + img.path;
        Bitmap hit = viewCache().get(key);
        if (hit != null) return hit;
        int t = need;
        for (int i = 0; i < 3; i++) {
            try {
                Bitmap b = decode(c, img.path, t);
                if (b != null) {
                    viewCache().put(key, b);
                    return b;
                }
            } catch (OutOfMemoryError e) {
                clearViewCache();
                System.gc();
            } catch (Throwable e) {
                return null;
            }
            t = (int) (t * 0.7f);
        }
        return null;
    }

    public static Bitmap decodeView(Context c, Img img) {
        return decodeView(c, img, VIEW_TARGET);
    }

    /** 是否包含该资源的原始字节（保存 / 分享用，直接拷原文件不二次压缩） */
    public static java.io.File copyRaw(Context c, Img img, java.io.File dir, String fileName) {
        try {
            if (!dir.exists()) dir.mkdirs();
            java.io.File out = new java.io.File(dir, fileName);
            InputStream is = open(c, img.path);
            java.io.FileOutputStream fos = new java.io.FileOutputStream(out);
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) > 0) fos.write(buf, 0, n);
            fos.flush();
            fos.close();
            is.close();
            return out;
        } catch (Throwable e) {
            return null;
        }
    }
}
