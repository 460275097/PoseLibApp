package com.poselib.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 左侧边栏：快捷入口（2 字标签）+ 单开手风琴分类树 + AI 修图推荐（点按复制链接）
 * + 关于 / 打赏 / 反馈
 */
public class SideNav extends FrameLayout {

    private final View scrim;
    private final LinearLayout panel;
    private final int panelW;
    private boolean open;
    private final Map<String, Boolean> expanded = new HashMap<>();
    private LinearLayout treeBox;
    private ScrollView scroll;
    private boolean aiOpen;
    private LinearLayout aiBody;
    private TextView aiArrow;
    private TextView aiHint;

    public SideNav(Context c) {
        super(c);
        setVisibility(GONE);

        scrim = new View(c);
        scrim.setBackgroundColor(0x99000000);
        scrim.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });
        addView(scrim, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        panelW = (int) (c.getResources().getDisplayMetrics().widthPixels * 0.58f);

        panel = Ui.col(c);
        panel.setBackgroundColor(App.app().surface());
        FrameLayout.LayoutParams pLp = new FrameLayout.LayoutParams(panelW,
                ViewGroup.LayoutParams.MATCH_PARENT, Gravity.START);
        addView(panel, pLp);

        build();
    }

    private void build() {
        App.Style s = App.app().style();
        Context c = getContext();

        scroll = new ScrollView(c);
        scroll.setVerticalScrollBarEnabled(false);
        panel.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        LinearLayout content = Ui.col(c);
        content.setPadding(Ui.dp(c, 12), Ui.statusBarHeight(c) + Ui.dp(c, 10),
                Ui.dp(c, 10), Ui.dp(c, 16));
        scroll.addView(content);

        TextView brand = Ui.text(c, "美拍姿势库", 18, s.text, true);
        content.addView(brand);
        TextView sub = Ui.text(c, App.app().library.total + " 张原图姿势 · 连签 "
                + UserStore.streak() + " 天", 10, s.text2, false);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subLp.topMargin = Ui.dp(c, 3);
        sub.setLayoutParams(subLp);
        content.addView(sub);

        TextView tagline = Ui.text(c, "告别拍照尴尬，记录每一份美好。", 10, Ui.mix(s.text2, s.accent, 0.55f), false);
        LinearLayout.LayoutParams tagLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tagLp.topMargin = Ui.dp(c, 4);
        tagline.setLayoutParams(tagLp);
        content.addView(tagline);

        // 顶部三/四个快捷图标：收藏 / 常看 / 打卡 / 学会（标签固定 2 字，触控统一大小）
        LinearLayout quick = Ui.row(c);
        LinearLayout.LayoutParams qLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        qLp.topMargin = Ui.dp(c, 12);
        quick.setLayoutParams(qLp);
        int gap = Ui.dp(c, 5);
        quick.addView(navChip("★", "收藏", Sources.FAV, gap));
        quick.addView(navChip("⟲", "常看", Sources.HIST, gap));
        quick.addView(navChip("▤", "打卡", null, gap));
        quick.addView(navChip("✓", "学会", Sources.LEARNED, 0));
        content.addView(quick);

        content.addView(Ui.space(c, 8));
        content.addView(menuRow("💡", "拍照技巧", TipsActivity.class));
        content.addView(menuRow("▦", "全部图片", null));
        content.addView(Ui.space(c, 6));

        content.addView(aiSection());

        content.addView(Ui.space(c, 8));
        content.addView(Ui.divider(c, s.divider));
        content.addView(Ui.space(c, 6));

        TextView catalogTitle = Ui.text(c, "全部目录", 11, s.text2, true);
        catalogTitle.setPadding(Ui.dp(c, 2), 0, 0, Ui.dp(c, 4));
        content.addView(catalogTitle);

        treeBox = Ui.col(c);
        content.addView(treeBox);
        buildTree();

        // 底部固定区：数据 / 反馈 / 打赏
        panel.addView(Ui.divider(c, s.divider));
        LinearLayout footer = Ui.row(c);
        footer.setPadding(Ui.dp(c, 8), Ui.dp(c, 7), Ui.dp(c, 8), Ui.dp(c, 7));
        footer.addView(footerCell("▦", "数据", StatsActivity.class, gap));
        footer.addView(footerCell("💬", "反馈", FeedbackActivity.class, gap));
        footer.addView(footerCell("♥", "打赏", AboutActivity.class, 0));
        panel.addView(footer);
    }

    // ---------------- 分类树（同一层只展开一个，打开自动收起其他） -------------

    private void buildTree() {
        treeBox.removeAllViews();
        Context c = getContext();
        List<String> roots = App.app().library.root;
        for (String id : roots) {
            Library.Node n = App.app().library.node(id);
            if (n == null || n.count == 0) continue;
            treeBox.addView(treeRow(n, 0));
        }
        final int y = scroll == null ? 0 : scroll.getScrollY();
        if (scroll != null) {
            scroll.post(new Runnable() {
                @Override
                public void run() {
                    scroll.scrollTo(0, y);
                }
            });
        }
    }

    private View treeRow(final Library.Node n, int level) {
        final Context c = getContext();
        App.Style s = App.app().style();
        final LinearLayout holder = Ui.col(c);

        LinearLayout row = Ui.row(c);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(Ui.dp(c, level == 0 ? 2 : (level == 1 ? 12 : 22)), Ui.dp(c, 7),
                Ui.dp(c, 2), Ui.dp(c, 7));

        TextView title = Ui.text(c, n.title, level == 0 ? 13.5f : (level == 1 ? 12.5f : 12f),
                s.text, level == 0);
        title.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(title);

        TextView cnt = Ui.text(c, String.valueOf(n.count), 9.5f, s.text2, false);
        cnt.setPadding(Ui.dp(c, 3), 0, Ui.dp(c, 4), 0);
        row.addView(cnt);

        TextView arrow = null;
        if (!n.children.isEmpty()) {
            arrow = Ui.icon(c, expanded(n.id) ? "▾" : "▸", 10, s.text2);
            arrow.setPadding(Ui.dp(c, 3), Ui.dp(c, 1), Ui.dp(c, 4), Ui.dp(c, 1));
            final Library.Node self = n;
            arrow.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggle(self);
                }
            });
            row.addView(arrow);
        }

        final LinearLayout kids = Ui.col(c);
        boolean isOpen = expanded(n.id);
        kids.setVisibility(isOpen ? VISIBLE : GONE);
        if (isOpen) {
            for (String cid : n.children) {
                Library.Node cn = App.app().library.node(cid);
                if (cn == null || cn.count == 0) continue;
                kids.addView(treeRow(cn, level + 1));
            }
        }

        row.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!n.children.isEmpty() && n.images.isEmpty()) {
                    toggle(n);
                } else {
                    openNode(n);
                }
            }
        });

        holder.addView(row);
        holder.addView(kids);
        return holder;
    }

    private boolean expanded(String id) {
        Boolean b = expanded.get(id);
        return b != null && b;
    }

    /** 单开手风琴：只展开当前这条路径，同级其他分类自动收起 */
    private void toggle(Library.Node n) {
        boolean now = !expanded(n.id);
        expanded.clear();
        if (now) {
            String id = n.id;
            while (true) {
                expanded.put(id, true);
                int slash = id.lastIndexOf('/');
                if (slash < 0) break;
                id = id.substring(0, slash);
            }
        }
        buildTree();
    }

    private void openNode(Library.Node n) {
        close();
        Intent i = new Intent(getContext(), GridActivity.class);
        i.putExtra("source", Sources.NODE + n.id);
        getContext().startActivity(i);
    }

    // ---------------- AI 修图推荐 -------------

    private View aiSection() {
        App.Style s = App.app().style();
        Context c = getContext();
        LinearLayout box = Ui.col(c);

        final LinearLayout head = Ui.row(c);
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.setPadding(Ui.dp(c, 2), Ui.dp(c, 2), Ui.dp(c, 2), Ui.dp(c, 2));

        TextView caseTitle = Ui.text(c, "AI修图软件推荐", 11, s.text, true);
        caseTitle.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        head.addView(caseTitle);

        aiHint = Ui.text(c, "点击展开", 9, s.accent, false);
        head.addView(aiHint);

        aiArrow = Ui.text(c, "▸", 11, s.accent, false);
        aiArrow.setPadding(Ui.dp(c, 4), 0, 0, 0);
        head.addView(aiArrow);

        box.addView(head);

        aiBody = Ui.col(c);
        aiBody.setVisibility(aiOpen ? VISIBLE : GONE);
        box.addView(aiBody);

        TextView note = Ui.text(c, "点一下自动复制下载链接 · 网络收集，作者未验证", 9, s.text2, false);
        note.setPadding(Ui.dp(c, 2), 0, 0, Ui.dp(c, 4));
        aiBody.addView(note);

        String[][] apps = {
                {"美图秀秀", "人像美容、拼图、AI 消除路人", "https://xiuxiu.meitu.com/"},
                {"醒图", "全能修图，AI 消除路人和换发型实用，会员次数受限", "https://www.retouchpics.com/"},
                {"像素蛋糕", "商业级 AI 人像精修，批量出片", "https://www.pixcakes.com/"},
                {"Nano Banana", "Google Gemini 图片生成与改图", "https://gemini.google.com/"},
                {"豆包", "对话式改图、消除、扩图", "https://www.doubao.com/"},
                {"即梦 AI", "图片生成与风格化", "https://jimeng.jianying.com/"},
        };
        for (String[] a : apps) aiBody.addView(linkRow(a[0], a[1], a[2], false));

        TextView extra = Ui.text(c, "网络收集（会员 / 便携版）", 9.5f, s.text2, true);
        extra.setPadding(Ui.dp(c, 2), Ui.dp(c, 6), 0, Ui.dp(c, 2));
        aiBody.addView(extra);

        String[][] more = {
                {"国际版剪映", "海外版剪映 · 夸克网盘",
                        "https://pan.quark.cn/s/776332c8567f"},
                {"Capptivo 剪辑软件", "海外剪辑 App（未给直链，商店搜索）",
                        "https://play.google.com/store/search?q=Capptivo&c=apps"},
                {"GIF 工具箱 v3.4.2", "GIF 制作、分解、压缩",
                        "https://www.cr173.com/soft/1115345.html"},
                {"GIF 制作动图助手 v1.3", "动图制作与导出",
                        "https://www.cr173.com/soft/1606777.html"},
                {"GIF 动图制作 v4.3.0", "第三方修改版，功能全但非官方",
                        "https://www.aiweibk.com/431388.html"},
                {"醒图国际版", "海外版醒图，修图与拼图",
                        "https://www.cr173.com/soft/1712265.html"},
                {"拼图酱 v2.6.2", "会员修改版，拼图模板多",
                        "https://www.cr173.com/soft/1078330.html"},
                {"拼图大神 v3.5.6", "手机拼图工具",
                        "https://www.cr173.com/soft/1711697.html"},
                {"醒图会员版 v8.7.1", "第三方改版，功能全但非官方",
                        "https://423down.lanzouo.com/b0f1czyjc"},
                {"Luminar Neo v1.29.0", "绿色便携版 · 夸克网盘",
                        "https://pan.quark.cn/s/f5d1a75117a3"},
                {"Luminar Neo 备用", "123 网盘",
                        "https://www.123pan.com/s/A6cA-EmBJh"},
                {"佐糖 Android v2.0.7", "专业版 · 夸克网盘",
                        "https://pan.quark.cn/s/16d3f54c4b08"},
        };
        for (String[] a : more) aiBody.addView(linkRow(a[0], a[1], a[2], true));

        head.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                aiOpen = !aiOpen;
                aiBody.setVisibility(aiOpen ? VISIBLE : GONE);
                aiArrow.setText(aiOpen ? "▾" : "▸");
                aiHint.setText(aiOpen ? "点击收起" : "点击展开");
            }
        });

        return box;
    }

    private View linkRow(String name, String desc, final String url, boolean thirdParty) {
        App.Style s = App.app().style();
        Context c = getContext();
        LinearLayout row = Ui.col(c);
        row.setPadding(Ui.dp(c, 2), Ui.dp(c, 4), Ui.dp(c, 2), Ui.dp(c, 4));
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout head = Ui.row(c);
        head.setGravity(Gravity.CENTER_VERTICAL);
        TextView n = Ui.text(c, name, 11, s.text, true);
        n.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        head.addView(n);
        TextView tag = Ui.text(c, thirdParty ? "复制" : "官网", 9, s.accent, false);
        head.addView(tag);
        row.addView(head);

        TextView d = Ui.text(c, desc, 9, s.text2, false);
        d.setPadding(0, Ui.dp(c, 1), 0, Ui.dp(c, 1));
        row.addView(d);

        TextView u = Ui.text(c, url, 8.5f, Ui.mix(s.text2, s.accent, 0.5f), false);
        u.setSingleLine(true);
        u.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        u.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        row.addView(u);

        row.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                copyLink(url);
            }
        });
        return row;
    }

    private void copyLink(String url) {
        try {
            ClipboardManager cm = (ClipboardManager) getContext()
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("下载链接", url));
            Toast.makeText(getContext(), "已复制下载链接（网络收集，作者未验证）",
                    Toast.LENGTH_SHORT).show();
        } catch (Throwable e) {
            Toast.makeText(getContext(), "复制失败，请手动访问 " + url,
                    Toast.LENGTH_LONG).show();
        }
    }

    // ---------------- 小零件 -------------

    private View navChip(String glyph, String label, final String source, int marginRight) {
        App.Style s = App.app().style();
        Context c = getContext();
        LinearLayout chip = Ui.col(c);
        chip.setGravity(Gravity.CENTER);
        chip.setBackground(Ui.round(c, s.surface2, 10));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(c, 46), 1f);
        lp.rightMargin = marginRight;
        chip.setLayoutParams(lp);
        chip.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
                Intent i = new Intent(getContext(),
                        source == null ? StatsActivity.class : GridActivity.class);
                if (source != null) i.putExtra("source", source);
                getContext().startActivity(i);
            }
        });
        TextView ic = Ui.icon(c, glyph, 10, s.accent);
        ic.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        chip.addView(ic);
        TextView lab = Ui.text(c, label, 9.5f, s.text, false);
        lab.setGravity(Gravity.CENTER);
        lab.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        chip.addView(lab);
        return chip;
    }

    private View menuRow(String glyph, String title, final Class<?> target) {
        App.Style s = App.app().style();
        Context c = getContext();
        LinearLayout row = Ui.row(c);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(Ui.dp(c, 2), Ui.dp(c, 8), Ui.dp(c, 4), Ui.dp(c, 8));
        TextView ic = Ui.icon(c, glyph, 12, s.accent);
        ic.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(c, 19),
                ViewGroup.LayoutParams.WRAP_CONTENT));
        ic.setGravity(Gravity.CENTER);
        row.addView(ic);
        TextView t = Ui.text(c, title, 12, s.text, false);
        t.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(t);
        row.addView(Ui.text(c, "›", 13, s.text2, false));
        row.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
                if (target != null) {
                    getContext().startActivity(new Intent(getContext(), target));
                } else if (title.startsWith("随机")) {
                    Intent i = new Intent(getContext(), GridActivity.class);
                    i.putExtra("source", Sources.RANDOM);
                    getContext().startActivity(i);
                } else {
                    Intent i = new Intent(getContext(), GridActivity.class);
                    i.putExtra("source", Sources.ALL);
                    getContext().startActivity(i);
                }
            }
        });
        return row;
    }

    private View footerCell(String glyph, String label, final Class<?> target,
                            int marginRight) {
        App.Style s = App.app().style();
        Context c = getContext();
        LinearLayout cell = Ui.col(c);
        cell.setGravity(Gravity.CENTER);
        cell.setBackground(Ui.round(c, s.surface2, 10));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(c, 44), 1f);
        lp.rightMargin = marginRight;
        cell.setLayoutParams(lp);
        cell.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
                getContext().startActivity(new Intent(getContext(), target));
            }
        });
        TextView ic = Ui.icon(c, glyph, 10, s.text);
        ic.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        cell.addView(ic);
        TextView lab = Ui.text(c, label, 9.5f, s.text, false);
        lab.setGravity(Gravity.CENTER);
        lab.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        cell.addView(lab);
        return cell;
    }

    public void open() {
        if (open) return;
        open = true;
        setVisibility(VISIBLE);
        panel.setTranslationX(-panelW);
        panel.animate().translationX(0).setDuration(210).start();
        scrim.setAlpha(0f);
        scrim.animate().alpha(1f).setDuration(210).start();
    }

    public void close() {
        if (!open) return;
        open = false;
        panel.animate().translationX(-panelW).setDuration(180).start();
        scrim.animate().alpha(0f).setDuration(180).withEndAction(new Runnable() {
            @Override
            public void run() {
                setVisibility(GONE);
            }
        }).start();
    }

    public boolean isOpen() {
        return open;
    }
}
