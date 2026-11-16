package com.poselib.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/** 关于 + 设置（含软件更新）+ 打赏（收款码可放大、长按保存） */
public class AboutActivity extends BaseActivity {

    private static final String UPDATE_URL = "https://wwbmz.lanzoul.com/b0fqtcmid";
    private static final String UPDATE_PWD = "5ylj";
    private static final String HOT_WEB =
            "https://hotboard-mobile.app.workbuddy.host/?sharecode=5FPPVW7FTM";
    private static final String HOT_BD =
            "https://pan.baidu.com/s/1SxP7FeB85dEaFKwT7SqHQw?pwd=2640";
    private static final String HOT_QUARK = "https://pan.quark.cn/s/94df9d49591f";

    @Override
    protected void onCreate(android.os.Bundle b) {
        super.onCreate(b);
        baseInit();

        LinearLayout page = Ui.col(this);
        root.addView(page, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        page.addView(topBar("关于", true, null, "美拍姿势库 " + version()));

        ScrollView sv = new ScrollView(this);
        sv.setVerticalScrollBarEnabled(false);
        page.addView(sv, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        LinearLayout list = Ui.col(this);
        list.setPadding(Ui.dp(this, 14), Ui.dp(this, 2), Ui.dp(this, 14), Ui.dp(this, 30));
        sv.addView(list);

        list.addView(brandCard());
        list.addView(title("设置"));
        list.addView(settingsCard());
        list.addView(title("开发者其他应用"));
        list.addView(devAppCard());
        list.addView(title("支持作者"));
        list.addView(donateCard());
        list.addView(title("说明与许可"));
        list.addView(licenseCard());
    }

    /** 当前安装的版本名（每次改版自动变化） */
    private String version() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Throwable e) {
            return "V1.0";
        }
    }

    private View title(String t) {
        TextView v = Ui.text(this, t, 13.5f, s.text, true);
        v.setPadding(Ui.dp(this, 2), Ui.dp(this, 20), 0, Ui.dp(this, 8));
        return v;
    }

    private View brandCard() {
        LinearLayout card = Ui.col(this);
        card.setBackground(Ui.round(this, s.surface, 16));
        card.setPadding(Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16));

        LinearLayout row = Ui.row(this);
        row.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout col = Ui.col(this);
        col.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        col.addView(Ui.text(this, "美拍姿势库", 16, s.text, true));
        TextView ver = Ui.text(this, version() + " · 内置 " + App.app().library.total
                + " 张原图姿势", 10.5f, s.text2, false);
        ver.setPadding(0, Ui.dp(this, 3), 0, 0);
        col.addView(ver);
        row.addView(col);
        card.addView(row);

        TextView h1 = Ui.text(this, "开发初衷", 12.5f, s.text, true);
        h1.setPadding(0, Ui.dp(this, 14), 0, Ui.dp(this, 7));
        card.addView(h1);

        TextView desc = Ui.text(this, "自己拍照时最怕两件事：不知道手往哪放，一站到镜头前不知道怎么摆姿势。网上姿势图不少，但要么散在各处，要么被压得模糊，翻两张就得等加载。索性把整理的素材做成一个能离线看的姿势库：挑一张，照着画面里的人物站位、手部动作、机位角度摆，根据人物占屏幕大小来判断镜头远近,按下快门就行。内容还有拍照话术，未来还想加进去专业的参数技巧，摄影公式等，目前还有很多分类但没加进来不然软件大小超标了。前期整理的都是20k到80K大小的图片，后来想不能为了大小而不清晰，后边的目录清晰度几乎都还是可以的。这个可以调。但是现在只有个人在用，目前没选定方案。欢迎反馈意见。", 12, s.text, false);
        desc.setLineSpacing(Ui.dp(this, 4), 1f);
        card.addView(desc);

        TextView h2 = Ui.text(this, "主要功能", 12.5f, s.text, true);
        h2.setPadding(0, Ui.dp(this, 16), 0, Ui.dp(this, 7));
        card.addView(h2);

        TextView list = Ui.text(this, "1. 内置 1700+ 张真人姿势原图，按 21 个一级分类整理：单人、双人、闺蜜、情侣、男生、亲子、手部动作、镜头角度、身材适配、艾寿花甲、照片风格、多人、室内场景、户外场景、引导话术、泳装、儿童亲子、场景用途、运动、录像技巧、摄像参数。\n\n"
                + "2. 首页分「快速分类」「目录分类」两组，一行三宫格；长按可调顺序、隐藏分类，标题栏可恢复显示。\n\n"
                + "3. 分类页宫格列数可切 2/3/4/5/6，默认一行 2 张、一页 4 图。\n\n"
                + "4. 图片不压缩、不生成缩略图，网格按需采样解码，点开大图再换高清，滑动时预加载相邻几张。\n\n"
                + "5. 大图页：左右横滑换图、双指缩放、双击放大还原；只有未放大时横滑才切图，放大状态横滑是拖动看细节。\n\n"
                + "6. 大图底部 6 个键：收藏、镜像、学会、保存、分享、删除。镜像可左右翻转，方便对着镜子摆。\n\n"
                + "7. 每张图下方只有「提示」+ 一句话拍摄技巧，没有难度星级、没有倒计时。\n\n"
                + "8. 收藏、历史、已学会三个清单，加上每日打卡和连续天数。\n\n"
                + "9. 「我的」数据页：累计浏览张数、收藏总数、最常看的分类、放大查看次数、累计使用时长、单次最长浏览、已学会数量；反馈意见计入活跃分，全网卡片展示使用人数与大致排名。\n\n"
                + "10. 随机一张只从单人、户外场景、室内场景、照片风格、镜头角度、场景用途 6 个分类里抽，不会蹦出偏门的图。\n\n"
                + "11. 拍照技巧 8 个图文主题：数码相机、安卓手机、AI 修图、参数设置、苹果手机、华为、OPPO、荣耀。\n\n"
                + "12. 侧栏还有「AI 修图软件推荐」，默认折叠，点标题才展开；点条目只复制官网或下载链接，不自动跳转。", 12, s.text, false);
        list.setLineSpacing(Ui.dp(this, 4), 1f);
        card.addView(list);
        return card;
    }

    private View settingsCard() {
        LinearLayout card = Ui.col(this);
        card.setBackground(Ui.round(this, s.surface, 14));

        card.addView(settingBlock("文字大小",
                new String[]{"极小", "小", "标准", "大"}, App.p().ts(), 0));
        card.addView(Ui.divider(this, s.divider));

        card.addView(settingBlock("深色模式",
                new String[]{"关", "开"}, App.p().dark() ? 1 : 0, 1));
        card.addView(Ui.divider(this, s.divider));

        card.addView(settingBlock("默认镜像",
                new String[]{"关", "开"}, App.p().mirror() ? 1 : 0, 2));
        card.addView(Ui.divider(this, s.divider));

        card.addView(settingBlock("宫格列数",
                new String[]{"2 列", "3 列", "4 列", "5 列", "6 列"},
                App.p().columns() - 2, 3));
        card.addView(Ui.divider(this, s.divider));

        card.addView(settingRow("恢复被删除的图片", App.p().hidden().size() + " 张已移除",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (App.p().hidden().isEmpty()) {
                            Toast.makeText(AboutActivity.this, "没有移除过的图片",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        new AlertDialog.Builder(AboutActivity.this)
                                .setTitle("恢复全部图片？")
                                .setMessage("会把之前从图库移除的 " + App.p().hidden().size()
                                        + " 张图全部放回来。")
                                .setNegativeButton("取消", null)
                                .setPositiveButton("恢复", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface d, int which) {
                                        App.p().unhideAll();
                                        App.app().library.invalidate();
                                        recreate();
                                    }
                                }).show();
                    }
                }));

        card.addView(settingRowAccent("一键恢复默认设置", "重置界面与首页布局",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(AboutActivity.this)
                                .setTitle("一键恢复默认设置")
                                .setMessage("文字大小、深色模式、默认镜像、宫格列数、首页排序与隐藏将恢复默认；"
                                        + "收藏、学会、历史和打卡记录保留。")
                                .setNegativeButton("取消", null)
                                .setPositiveButton("恢复默认", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface d, int which) {
                                        App.p().resetDefaults();
                                        App.app().refreshStyle();
                                        recreate();
                                    }
                                }).show();
                    }
                }));

        card.addView(settingRowAccent("软件更新", "蓝奏云 · 密码 " + UPDATE_PWD,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        update();
                    }
                }));

        return card;
    }

    /** 设置项：标题 + 一行可直接点选的选项，不再弹二次选择框 */
    private View settingBlock(String label, String[] names, int selected, final int kind) {
        LinearLayout box = Ui.col(this);
        box.setPadding(Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 10));

        box.addView(Ui.text(this, label, 13, s.text, false));

        LinearLayout chips = Ui.row(this);
        LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cLp.topMargin = Ui.dp(this, 10);
        chips.setLayoutParams(cLp);

        for (int i = 0; i < names.length; i++) {
            final int idx = i;
            boolean on = i == selected;
            TextView chip = Ui.text(this, names[i], 11, on ? 0xFFFFFFFF : s.text2, on);
            chip.setGravity(Gravity.CENTER);
            chip.setBackground(Ui.round(this, on ? s.accent : s.surface2, 10));
            chip.setPadding(Ui.dp(this, 12), Ui.dp(this, 7), Ui.dp(this, 12), Ui.dp(this, 7));
            LinearLayout.LayoutParams chLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            chLp.rightMargin = Ui.dp(this, 8);
            chip.setLayoutParams(chLp);
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (kind) {
                        case 0:
                            App.p().setTs(idx);
                            break;
                        case 1:
                            App.p().setDark(idx == 1);
                            break;
                        case 2:
                            App.p().setMirror(idx == 1);
                            break;
                        case 3:
                            App.p().setColumns(idx + 2);
                            break;
                    }
                    App.app().refreshStyle();
                    recreate();
                }
            });
            chips.addView(chip);
        }
        box.addView(chips);
        return box;
    }

    private View settingRow(String label, String value, View.OnClickListener l) {
        return setting(label, value, l, s.text, false);
    }

    private View settingRowAccent(String label, String value, View.OnClickListener l) {
        return setting(label, value, l, s.accent, true);
    }

    private View setting(String label, String value, View.OnClickListener l,
                         int labelColor, boolean hasArrow) {
        LinearLayout row = Ui.row(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 14));
        row.setOnClickListener(l);

        TextView t = Ui.text(this, label, 13, labelColor, false);
        t.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(t);

        TextView v = Ui.text(this, value, 11, s.text2, false);
        v.setPadding(0, 0, Ui.dp(this, 4), 0);
        row.addView(v);
        if (hasArrow) row.addView(Ui.text(this, "›", 14, s.text2, false));
        return row;
    }

    private void update() {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(UPDATE_URL));
            startActivity(i);
            Toast.makeText(this, "已打开蓝奏云，提取密码 " + UPDATE_PWD,
                    Toast.LENGTH_LONG).show();
        } catch (Throwable e) {
            Toast.makeText(this, "没有可用的浏览器，请手动访问 " + UPDATE_URL,
                    Toast.LENGTH_LONG).show();
        }
    }

    private View donateCard() {
        LinearLayout card = Ui.col(this);
        card.setBackground(Ui.round(this, s.surface, 16));
        card.setPadding(Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16));
        card.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView t = Ui.text(this, "如果这个姿势库帮你拍出了满意的照片", 12, s.text, false);
        t.setGravity(Gravity.CENTER);
        card.addView(t);

        ImageView qr = new ImageView(this);
        int size = Math.min((int) (screenW() * 0.62f), Ui.dp(this, 260));
        LinearLayout.LayoutParams qLp = new LinearLayout.LayoutParams(size, size);
        qLp.topMargin = Ui.dp(this, 12);
        qr.setLayoutParams(qLp);
        qr.setScaleType(ImageView.ScaleType.FIT_CENTER);
        qr.setBackgroundColor(0xFFFFFFFF);
        card.addView(qr);
        Thumbs.into(qr, new Img("donate.png", "打赏码", "about"), size);
        qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQr();
            }
        });

        TextView tip = Ui.text(this, "微信扫码打赏 · 点一下可以放大，长按图片保存到相册", 10.5f,
                s.text2, false);
        tip.setGravity(Gravity.CENTER);
        tip.setPadding(0, Ui.dp(this, 10), 0, 0);
        card.addView(tip);

        TextView thx = Ui.text(this, "谢谢你的支持，我会继续往里加姿势", 10.5f, s.text2, false);
        thx.setGravity(Gravity.CENTER);
        thx.setPadding(0, Ui.dp(this, 4), 0, 0);
        card.addView(thx);
        return card;
    }

    /** 全屏放大收款码，长按保存 */
    private void showQr() {
        final Dialog d = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        FrameLayout box = new FrameLayout(this);
        box.setBackgroundColor(Color.BLACK);

        final ImageView iv = new ImageView(this);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        iv.setLayoutParams(lp);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        box.addView(iv);
        Thumbs.into(iv, new Img("donate.png", "打赏码", "about"), 1400);

        TextView hint = Ui.text(this, "长按图片保存到相册 · 点空白处关闭", 11, 0xCCFFFFFF, false);
        hint.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams hLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM);
        hLp.bottomMargin = Ui.dp(this, 40);
        hint.setLayoutParams(hLp);
        box.addView(hint);

        box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 放大图不关闭
            }
        });
        iv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                saveDonate();
                return true;
            }
        });

        d.setContentView(box);
        Window w = d.getWindow();
        if (w != null) {
            w.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
        }
        d.show();
    }

    private void saveDonate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean ok = false;
                try {
                    String name = "美拍姿势库_打赏码.png";
                    InputStream is = getAssets().open("donate.png");
                    if (Build.VERSION.SDK_INT >= 29) {
                        ContentValues cv = new ContentValues();
                        cv.put(MediaStore.Images.Media.DISPLAY_NAME, name);
                        cv.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                        cv.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/美拍姿势库");
                        Uri uri = getContentResolver().insert(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
                        if (uri != null) {
                            OutputStream os = getContentResolver().openOutputStream(uri);
                            copy(is, os);
                            os.close();
                            ok = true;
                        }
                    } else {
                        File dir = new File(Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES), "美拍姿势库");
                        if (!dir.exists()) dir.mkdirs();
                        File f = new File(dir, name);
                        java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
                        copy(is, fos);
                        fos.close();
                        ok = true;
                    }
                    is.close();
                } catch (Throwable ignored) {
                }
                final boolean res = ok;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AboutActivity.this, res ? "已保存到相册"
                                : "保存失败，请检查存储权限", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }

    private void copy(InputStream is, OutputStream os) throws Exception {
        byte[] buf = new byte[8192];
        int n;
        while ((n = is.read(buf)) > 0) os.write(buf, 0, n);
    }

    /** 开发者另一款作品：热点助手（点击链接自动用浏览器打开） */
    private View devAppCard() {
        LinearLayout card = Ui.col(this);
        card.setBackground(Ui.round(this, s.surface, 14));
        card.setPadding(Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 10));

        TextView name = Ui.text(this, "热点助手", 15, s.text, true);
        card.addView(name);

        TextView tag = Ui.text(this, "200+ 热榜源 · 15 大分类 · 多线程抓取秒开", 9.5f, s.accent, false);
        tag.setPadding(0, Ui.dp(this, 2), 0, Ui.dp(this, 8));
        card.addView(tag);

        String[] lines = {
                "· 200+ 热榜源：微博、抖音、知乎、B 站、百度等各大平台热搜榜",
                "· 15 大分类：综合 / 财经 / 娱乐 / 科技 / 社区 / 国际新闻 等",
                "· 多线程并发抓取 + 缓存，打开秒开",
                "· 关键词搜索、语音朗读（三重引擎）、收藏夹与浏览记录",
                "· 字号 / 字体调节，载入默认源、锁定新闻源、新增自定义源",
                "· 电脑端 exe、手机端 apk、网页端均可下载",
        };
        for (String line : lines) {
            TextView t = Ui.text(this, line, 11, s.text, false);
            t.setLineSpacing(Ui.dp(this, 3), 1f);
            t.setPadding(0, Ui.dp(this, 1), 0, Ui.dp(this, 1));
            card.addView(t);
        }

        card.addView(Ui.space(this, 8));
        card.addView(devLink("网页端（手机可直接打开）", HOT_WEB));
        card.addView(devLink("百度网盘（提取码 2640）", HOT_BD));
        card.addView(devLink("夸克网盘", HOT_QUARK));

        TextView hint = Ui.text(this, "点链接会自动用浏览器打开", 9, s.text2, false);
        hint.setPadding(0, Ui.dp(this, 7), 0, Ui.dp(this, 2));
        card.addView(hint);
        return card;
    }

    private View devLink(String label, final String url) {
        LinearLayout row = Ui.row(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackground(Ui.roundStroke(this, s.surface2, 10, s.divider));
        row.setPadding(Ui.dp(this, 10), Ui.dp(this, 9), Ui.dp(this, 10), Ui.dp(this, 9));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = Ui.dp(this, 5);
        row.setLayoutParams(lp);

        TextView t = Ui.text(this, label, 11.5f, s.accent, false);
        t.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(t);
        row.addView(Ui.text(this, "↗", 12, s.accent, false));
        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUrl(url);
            }
        });
        return row;
    }

    private void openUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Throwable e) {
            Toast.makeText(this, "没有可用的浏览器，请手动访问 " + url,
                    Toast.LENGTH_LONG).show();
        }
    }

    /** 说明与许可：侵权 / 交流 / 打赏入口 / 使用许可 */
    private View licenseCard() {
        LinearLayout card = Ui.col(this);
        card.setBackground(Ui.round(this, s.surface, 14));
        card.setPadding(Ui.dp(this, 14), Ui.dp(this, 12), Ui.dp(this, 14), Ui.dp(this, 12));

        String[] items = {
                "如有侵权问题，请联系作者会第一时间删除图片。",
                "想加新站点、报 BUG、用着不顺手，也欢迎添加交流。",
                "觉得好用的话，软件里有个请喝咖啡的入口。",
                "许可：供个人免费使用，请勿用于商业用途或二次打包牟利。",
        };
        for (String item : items) {
            TextView t = Ui.text(this, "· " + item, 11.5f, s.text, false);
            t.setLineSpacing(Ui.dp(this, 3), 1f);
            t.setPadding(0, Ui.dp(this, 4), 0, Ui.dp(this, 4));
            card.addView(t);
        }
        return card;
    }
}
