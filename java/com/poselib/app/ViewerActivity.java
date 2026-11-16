package com.poselib.app;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/** 大图查看：顶部标题/张数 + 左右滑动换图；底部提示 + 收藏/镜像/学会/保存/分享/删除 */
public class ViewerActivity extends BaseActivity {

    private String source;
    private List<Img> data;
    private PagerView pager;
    private GridOverlay overlay;
    private TextView nameView, countView, hintView, tipView;
    private LinearLayout favBox, mirrorBox, learnBox;
    private LinearLayout topChrome, bottomChrome;
    private boolean chromeOn = true;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        baseInit();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        root.setBackgroundColor(Color.BLACK);
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);
        if (Build.VERSION.SDK_INT >= 23) {
            getWindow().getDecorView().setSystemUiVisibility(0);
        }

        source = getIntent().getStringExtra("source");
        if (source == null) source = Sources.ALL;
        data = new ArrayList<>(Sources.resolve(source));
        if (data.isEmpty()) {
            Toast.makeText(this, "这个分类下暂时没有图片", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        int start = getIntent().getIntExtra("index", 0);

        pager = new PagerView(this);
        root.addView(pager, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        pager.setMirror(App.p().mirror());

        overlay = new GridOverlay(this);
        overlay.setClickable(false);
        root.addView(overlay, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        pager.setOnPageChangeListener(new PagerView.OnPageChangeListener() {
            @Override
            public void onPage(int index) {
                if (index >= 0 && index < data.size()) {
                    Img img = data.get(index);
                    Library.Node node = App.app().library.node(img.nodeId);
                    String name = node == null ? Sources.title(source) : node.title;
                    UserStore.addHistory(img, name);
                }
                updateChrome();
            }

            @Override
            public void onZoom(boolean zoomed) {
                if (zoomed) UserStore.addZoom();
                refreshOverlay();
            }

            @Override
            public void onTap() {
                toggleChrome();
            }

            @Override
            public void onImageReady() {
                refreshOverlay();
            }
        });

        topChrome = topBarView();
        root.addView(topChrome, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.TOP));

        bottomChrome = bottomBarView();
        root.addView(bottomChrome, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM));

        pager.setData(data, start);
        updateChrome();
    }

    private LinearLayout topBarView() {
        LinearLayout bar = Ui.row(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setBackgroundColor(0x00000000);
        bar.setPadding(Ui.dp(this, 10), Ui.statusBarHeight(this) + Ui.dp(this, 10),
                Ui.dp(this, 14), Ui.dp(this, 10));

        TextView back = Ui.icon(this, "‹", 22, 0xFFFFFFFF);
        back.setPadding(Ui.dp(this, 6), Ui.dp(this, 2), Ui.dp(this, 12), Ui.dp(this, 2));
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        bar.addView(back);

        LinearLayout col = Ui.col(this);
        col.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        nameView = Ui.text(this, "", 15.5f, 0xFFFFFFFF, true);
        nameView.setSingleLine(true);
        col.addView(nameView);
        countView = Ui.text(this, "", 11, 0x99FFFFFF, false);
        LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cLp.topMargin = Ui.dp(this, 3);
        countView.setLayoutParams(cLp);
        col.addView(countView);
        bar.addView(col);

        hintView = Ui.text(this, "左右滑动换图", 10.5f, 0x88FFFFFF, false);
        bar.addView(hintView);
        return bar;
    }

    private LinearLayout bottomBarView() {
        LinearLayout wrap = Ui.col(this);
        wrap.setPadding(Ui.dp(this, 12), Ui.dp(this, 10), Ui.dp(this, 12), Ui.dp(this, 10));
        wrap.setBackgroundColor(0xF0000000);

        LinearLayout tipRow = Ui.row(this);
        tipRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView badge = Ui.text(this, "提示", 10, 0xFFFFFFFF, true);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(Ui.round(this, 0xFFE0483C, 5));
        badge.setPadding(Ui.dp(this, 6), Ui.dp(this, 3), Ui.dp(this, 6), Ui.dp(this, 3));
        tipRow.addView(badge);

        tipView = Ui.text(this, "", 11.5f, 0xEEFFFFFF, false);
        tipView.setMaxLines(2);
        tipView.setLineSpacing(Ui.dp(this, 1), 1f);
        LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        tLp.leftMargin = Ui.dp(this, 8);
        tipView.setLayoutParams(tLp);
        tipRow.addView(tipView);
        wrap.addView(tipRow);

        LinearLayout row = Ui.row(this);
        row.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rLp.topMargin = Ui.dp(this, 12);
        row.setLayoutParams(rLp);

        favBox = action("☆", "收藏", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Img img = pager.current();
                if (img == null) return;
                boolean on = App.p().toggleFavorite(img.key());
                Toast.makeText(ViewerActivity.this, on ? "已收藏" : "已取消收藏",
                        Toast.LENGTH_SHORT).show();
                updateChrome();
            }
        });
        row.addView(favBox);

        mirrorBox = action("⇋", "镜像", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pager.setMirror(!pager.mirrored());
                updateChrome();
            }
        });
        row.addView(mirrorBox);

        learnBox = action("✓", "学会", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Img img = pager.current();
                if (img == null) return;
                boolean on = App.p().toggleLearned(img.key());
                Toast.makeText(ViewerActivity.this, on ? "标记为已学会" : "已取消学习标记",
                        Toast.LENGTH_SHORT).show();
                updateChrome();
            }
        });
        row.addView(learnBox);

        row.addView(action("↓", "保存", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCurrent();
            }
        }));

        row.addView(action("↗", "分享", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareCurrent();
            }
        }));

        row.addView(action("🗑", "删除", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteCurrent();
            }
        }));

        wrap.addView(row);
        return wrap;
    }

    private LinearLayout action(String glyph, String label, View.OnClickListener l) {
        LinearLayout box = Ui.col(this);
        box.setGravity(Gravity.CENTER);
        TextView ic = Ui.icon(this, glyph, 19, 0xFFFFFFFF);
        TextView tx = Ui.text(this, label, 10, 0xAAFFFFFF, false);
        tx.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tLp.topMargin = Ui.dp(this, 4);
        tx.setLayoutParams(tLp);
        box.addView(ic);
        box.addView(tx);
        box.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        box.setOnClickListener(l);
        box.setTag(ic);
        return box;
    }

    private void toggleChrome() {
        chromeOn = !chromeOn;
        if (chromeOn) {
            topChrome.setVisibility(View.VISIBLE);
            bottomChrome.setVisibility(View.VISIBLE);
            topChrome.animate().alpha(1f).setDuration(160).start();
            bottomChrome.animate().alpha(1f).setDuration(160).start();
        } else {
            topChrome.animate().alpha(0f).setDuration(160).withEndAction(new Runnable() {
                @Override
                public void run() {
                    topChrome.setVisibility(View.GONE);
                }
            }).start();
            bottomChrome.animate().alpha(0f).setDuration(160).withEndAction(new Runnable() {
                @Override
                public void run() {
                    bottomChrome.setVisibility(View.GONE);
                }
            }).start();
        }
        refreshOverlay();
    }

    private void refreshOverlay() {
        if (overlay == null) return;
        if (!chromeOn || pager.isZoomed()) {
            overlay.setRect(null);
            return;
        }
        overlay.setRect(pager.imageRect());
    }

    private void updateChrome() {
        Img img = pager.current();
        if (img == null) return;
        nameView.setText(img.name);
        countView.setText("第 " + (pager.index() + 1) + " / 共 " + data.size() + " 张");

        String tip = App.app().library.tipOf(img.nodeId);
        tipView.setText(tip);

        boolean fav = App.p().favorites().contains(img.key());
        ((TextView) favBox.getTag()).setText(fav ? "★" : "☆");
        ((TextView) favBox.getTag()).setTextColor(fav ? 0xFFFFB020 : 0xFFFFFFFF);

        boolean learned = App.p().learned().contains(img.key());
        ((TextView) learnBox.getTag()).setText(learned ? "✓" : "✓");
        ((TextView) learnBox.getTag()).setTextColor(learned ? 0xFF3BC25F : 0xFFFFFFFF);

        ((TextView) mirrorBox.getTag()).setTextColor(
                pager.mirrored() ? 0xFFFFB020 : 0xFFFFFFFF);

        refreshOverlay();
    }

    // ---------------- 保存 / 分享 / 删除 ----------------

    private void saveCurrent() {
        final Img img = pager.current();
        if (img == null) return;
        if (Build.VERSION.SDK_INT >= 23 && Build.VERSION.SDK_INT < 29
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1001);
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean ok = false;
                try {
                    String name = "美拍姿势库_" + System.currentTimeMillis() + ".jpg";
                    if (Build.VERSION.SDK_INT >= 29) {
                        ContentValues cv = new ContentValues();
                        cv.put(MediaStore.Images.Media.DISPLAY_NAME, name);
                        cv.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                        cv.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/美拍姿势库");
                        Uri uri = getContentResolver().insert(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
                        if (uri != null) {
                            java.io.OutputStream os = getContentResolver().openOutputStream(uri);
                            copyAsset(img, os);
                            os.close();
                            ok = true;
                        }
                    } else {
                        File dir = new File(Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES), "美拍姿势库");
                        if (!dir.exists()) dir.mkdirs();
                        File f = new File(dir, name);
                        java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
                        copyAsset(img, fos);
                        fos.close();
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                Uri.fromFile(f)));
                        ok = true;
                    }
                } catch (Throwable ignored) {
                }
                final boolean res = ok;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ViewerActivity.this, res
                                ? "已保存原图到相册 Pictures/美拍姿势库"
                                : "保存失败，请检查存储权限", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }

    /** 直接把资源包里的原图字节写出去，不做二次压缩 */
    private void copyAsset(Img img, java.io.OutputStream os) throws Exception {
        InputStream is = getAssets().open(img.path);
        byte[] buf = new byte[8192];
        int n;
        while ((n = is.read(buf)) > 0) os.write(buf, 0, n);
        is.close();
    }

    private void shareCurrent() {
        final Img img = pager.current();
        if (img == null) return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File dir = new File(getCacheDir(), "share");
                    if (!dir.exists()) dir.mkdirs();
                    File f = new File(dir, "poselib_share.jpg");
                    java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
                    copyAsset(img, fos);
                    fos.close();
                    final Uri uri = ShareProvider.uriFor(f);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent i = new Intent(Intent.ACTION_SEND);
                            i.setType("image/*");
                            i.putExtra(Intent.EXTRA_STREAM, uri);
                            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(Intent.createChooser(i, "分享姿势"));
                        }
                    });
                } catch (Throwable e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ViewerActivity.this, "分享失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    private void deleteCurrent() {
        final Img img = pager.current();
        if (img == null) return;
        new AlertDialog.Builder(this)
                .setTitle("删除这张？")
                .setMessage("会从你的图库里移除这张姿势，不再出现在列表和随机里。原图仍在软件资源包里，随时可在设置里恢复全部图片。")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        App.p().hide(img.key());
                        App.app().library.invalidate();
                        for (int i = 0; i < data.size(); i++) {
                            if (data.get(i).key().equals(img.key())) {
                                data.remove(i);
                                break;
                            }
                        }
                        if (data.isEmpty()) {
                            finish();
                            return;
                        }
                        int idx = Math.min(pager.index(), data.size() - 1);
                        pager.setData(data, idx);
                        updateChrome();
                        Toast.makeText(ViewerActivity.this, "已从我的图库移除",
                                Toast.LENGTH_SHORT).show();
                    }
                }).show();
    }

    @Override
    public void onRequestPermissionsResult(int req, String[] perms, int[] res) {
        super.onRequestPermissionsResult(req, perms, res);
        if (req == 1001) {
            if (res != null && res.length > 0
                    && res[0] == PackageManager.PERMISSION_GRANTED) {
                saveCurrent();
            } else {
                Toast.makeText(this, "没有存储权限，无法保存原图", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
