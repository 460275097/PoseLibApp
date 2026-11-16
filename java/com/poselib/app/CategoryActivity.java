package com.poselib.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;

/** 首页：顶部条 + 四个快捷卡 + 分类速览（一行三宫格，竖版主图） */
public class CategoryActivity extends BaseActivity {

    private LinearLayout grid;
    private SideNav nav;
    private String tab = "quick";

    @Override
    protected void onCreate(android.os.Bundle b) {
        super.onCreate(b);
        baseInit();

        ScrollView sv = new ScrollView(this);
        sv.setVerticalScrollBarEnabled(false);
        FrameLayout.LayoutParams svLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        root.addView(sv, svLp);

        tab = App.p().homeTab();

        LinearLayout page = Ui.col(this);
        page.setPadding(0, 0, 0, Ui.dp(this, 86));
        sv.addView(page, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        page.addView(header());

        LinearLayout quick = quickRow();
        LinearLayout.LayoutParams qLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        qLp.leftMargin = Ui.dp(this, 14);
        qLp.rightMargin = Ui.dp(this, 14);
        qLp.topMargin = Ui.dp(this, 8);
        page.addView(quick, qLp);

        page.addView(sectionHeader());

        grid = Ui.col(this);
        grid.setPadding(Ui.dp(this, 12), 0, Ui.dp(this, 12), 0);
        page.addView(grid);

        buildCategoryGrid();

        nav = new SideNav(this);
        root.addView(nav, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        root.addView(bottomBar(), new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.END | Gravity.BOTTOM));
    }

    /** 顶部：☰ 左侧、标题与统计、右侧 技巧/搜索/我的/设置 */
    private View header() {
        LinearLayout bar = Ui.row(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        int top = Ui.statusBarHeight(this);
        bar.setPadding(Ui.dp(this, 6), top + Ui.dp(this, 8), Ui.dp(this, 4), Ui.dp(this, 4));

        TextView menu = Ui.icon(this, "☰", 18, s.text);
        int p = Ui.dp(this, 8);
        menu.setPadding(p, p, p, p);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nav.open();
            }
        });
        bar.addView(menu);

        LinearLayout titleCol = Ui.col(this);
        LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        tLp.leftMargin = Ui.dp(this, 4);
        titleCol.setLayoutParams(tLp);

        TextView t = Ui.text(this, "美拍姿势库", 19, s.text, true);
        titleCol.addView(t);

        TextView sl = Ui.text(this, "告别拍照尴尬，记录每一次美好！", 9.5f, 0xFFFF5D6D, false);
        LinearLayout.LayoutParams slLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        slLp.topMargin = Ui.dp(this, 1);
        sl.setLayoutParams(slLp);
        titleCol.addView(sl);

        String sub = App.app().library.total + " 张姿势 · 已学会 " + UserStore.learnedCount()
                + " · 连签 " + UserStore.streak() + " 天";
        TextView st = Ui.text(this, sub, 10.5f, s.text2, false);
        st.setSingleLine(true);
        LinearLayout.LayoutParams sLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sLp.topMargin = Ui.dp(this, 2);
        st.setLayoutParams(sLp);
        titleCol.addView(st);

        bar.addView(titleCol);

        bar.addView(actionIcon("💡", 15, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                go(TipsActivity.class);
            }
        }));
        bar.addView(actionIcon("🔍", 15, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                go(SearchActivity.class);
            }
        }));
        bar.addView(actionIcon("👤", 15, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                go(StatsActivity.class);
            }
        }));
        bar.addView(actionIcon("⚙", 16, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                go(AboutActivity.class);
            }
        }));
        bar.addView(redCameraIcon(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                go(ShootTipsActivity.class);
            }
        }));
        return bar;
    }

    private View actionIcon(String glyph, float sp, View.OnClickListener l) {
        TextView t = Ui.icon(this, glyph, sp, s.text);
        int p = Ui.dp(this, 7);
        t.setPadding(p, p, p, p);
        t.setOnClickListener(l);
        return t;
    }

    /** 右上角红色摄像机图标：红底白机身的临拍技巧入口 */
    private View redCameraIcon(View.OnClickListener l) {
        View v = new View(this) {
            @Override
            protected void onDraw(android.graphics.Canvas c) {
                super.onDraw(c);
                float w = getWidth(), h = getHeight();
                float l0 = getPaddingLeft(), t0 = getPaddingTop();
                float r0 = w - getPaddingRight(), b0 = h - getPaddingBottom();
                android.graphics.Paint p = new android.graphics.Paint(
                        android.graphics.Paint.ANTI_ALIAS_FLAG);
                p.setColor(0xFFFF3B30);
                c.drawRoundRect(l0, t0, r0, b0, Ui.dp(getContext(), 8), Ui.dp(getContext(), 8), p);

                p.setColor(android.graphics.Color.WHITE);
                c.drawRoundRect(w * 0.20f, h * 0.38f, w * 0.80f, h * 0.74f,
                        Ui.dp(getContext(), 3), Ui.dp(getContext(), 3), p);
                c.drawRoundRect(w * 0.34f, h * 0.28f, w * 0.56f, h * 0.42f,
                        Ui.dp(getContext(), 2), Ui.dp(getContext(), 2), p);

                p.setColor(0xFFFF3B30);
                c.drawCircle(w * 0.50f, h * 0.56f, w * 0.115f, p);
                p.setColor(android.graphics.Color.WHITE);
                c.drawCircle(w * 0.50f, h * 0.56f, w * 0.045f, p);
            }
        };
        int size = Ui.dp(this, 34);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
        lp.leftMargin = Ui.dp(this, 1);
        lp.rightMargin = Ui.dp(this, 1);
        v.setLayoutParams(lp);
        v.setPadding(Ui.dp(this, 4), Ui.dp(this, 4), Ui.dp(this, 4), Ui.dp(this, 4));
        v.setOnClickListener(l);
        return v;
    }

    /** 四个快捷卡：收藏 / 学会 / 随机 / 打卡（固定 2 字，统一大小，向右靠齐） */
    private LinearLayout quickRow() {
        LinearLayout row = Ui.row(this);
        row.setGravity(Gravity.END);
        int w = Ui.dp(this, 66);
        int h = Ui.dp(this, 40);
        int gap = Ui.dp(this, 6);

        row.addView(quickCard("★", "收藏", w, h, gap, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                go(Sources.FAV);
            }
        }));
        row.addView(quickCard("✓", "学会", w, h, gap, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                go(Sources.LEARNED);
            }
        }));
        row.addView(quickCard("⟳", "随机", w, h, gap, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRandom();
            }
        }));
        row.addView(quickCard("▤", "打卡", w, h, 0, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                go(StatsActivity.class);
            }
        }));
        return row;
    }

    private View quickCard(String glyph, String label, int w, int h, int marginRight,
                           View.OnClickListener l) {
        LinearLayout card = Ui.row(this);
        card.setGravity(Gravity.CENTER);
        card.setBackground(Ui.round(this, s.surface, 12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(w, h);
        lp.rightMargin = marginRight;
        card.setLayoutParams(lp);
        card.setOnClickListener(l);

        TextView ic = Ui.icon(this, glyph, 13, s.accent);
        card.addView(ic);

        TextView lab = Ui.text(this, label, 10.5f, s.text, false);
        LinearLayout.LayoutParams labLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        labLp.leftMargin = Ui.dp(this, 3);
        lab.setLayoutParams(labLp);
        card.addView(lab);
        return card;
    }

    private boolean isCatalog() {
        return "catalog".equals(tab);
    }

    private View sectionHeader() {
        LinearLayout row = Ui.row(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(Ui.dp(this, 16), Ui.dp(this, 18), Ui.dp(this, 16), Ui.dp(this, 10));

        TextView quickTab = tabView("快速分类", !isCatalog());
        quickTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchTab("quick");
            }
        });
        row.addView(quickTab);

        TextView catTab = tabView("目录分类", isCatalog());
        LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cLp.leftMargin = Ui.dp(this, 16);
        catTab.setLayoutParams(cLp);
        catTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchTab("catalog");
            }
        });
        row.addView(catTab);

        TextView spacer = new TextView(this);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(spacer);

        boolean hasHidden = isCatalog()
                ? !App.p().catalogHidden().isEmpty()
                : !App.p().homeHidden().isEmpty();
        if (hasHidden) {
            TextView restore = Ui.text(this, "恢复显示", 10, s.accent, false);
            restore.setPadding(0, 0, Ui.dp(this, 8), 0);
            restore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isCatalog()) App.p().unhideCatalogAll();
                    else App.p().unhideHomeAll();
                    recreate();
                }
            });
            row.addView(restore);
        } else {
            TextView hint = Ui.text(this, "长按排序 / 隐藏", 9.5f, s.text2, false);
            hint.setPadding(0, 0, Ui.dp(this, 8), 0);
            row.addView(hint);
        }

        TextView more = Ui.text(this, "全部图片 ›", 12, s.accent, false);
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                go(Sources.ALL);
            }
        });
        row.addView(more);
        return row;
    }

    private TextView tabView(String label, boolean active) {
        TextView t = Ui.text(this, label, 15, active ? s.accent : s.text2, active);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = Ui.dp(this, 2);
        t.setLayoutParams(lp);
        return t;
    }

    private void switchTab(String target) {
        if (tab.equals(target)) return;
        tab = target;
        App.p().setHomeTab(target);
        recreate();
    }

    /** 一行三宫格，竖版主图；长按可调顺序或隐藏 */
    private void buildCategoryGrid() {
        grid.removeAllViews();
        List<Library.HomeItem> items = isCatalog()
                ? App.app().library.catalogList()
                : App.app().library.homeList();
        int cols = 3;
        int gap = Ui.dp(this, 9);
        int avail = screenW() - Ui.dp(this, 24);
        int tileW = (avail - gap * (cols - 1)) / cols;
        int imgH = (int) (tileW * 1.34f);

        LinearLayout row = null;
        int inRow = 0;
        for (Library.HomeItem item : items) {
            if (inRow == 0) {
                row = Ui.row(this);
                LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                rLp.bottomMargin = gap;
                row.setLayoutParams(rLp);
                grid.addView(row);
            }
            View tile = tile(item, tileW, imgH);
            LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(tileW,
                    imgH + Ui.dp(this, 30));
            if (inRow < cols - 1) tLp.rightMargin = gap;
            tile.setLayoutParams(tLp);
            row.addView(tile);
            inRow++;
            if (inRow == cols) inRow = 0;
        }
        if (row != null && inRow != 0) {
            for (int i = inRow; i < cols; i++) {
                View filler = new View(this);
                LinearLayout.LayoutParams fLp = new LinearLayout.LayoutParams(tileW, 1);
                if (i < cols - 1) fLp.rightMargin = gap;
                filler.setLayoutParams(fLp);
                row.addView(filler);
            }
        }
    }

    private View tile(final Library.HomeItem item, int w, int imgH) {
        LinearLayout card = Ui.col(this);
        card.setBackground(Ui.round(this, s.surface, 13));
        card.setClipToOutline(true);
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                go(Sources.NODE + item.nodeId);
            }
        });
        card.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                tileMenu(item);
                return true;
            }
        });

        ImageView iv = new ImageView(this);
        iv.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, imgH));
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv.setBackgroundColor(s.surface2);
        card.addView(iv);

        String cover = item.cover;
        if (cover == null || cover.isEmpty()) {
            Library.Node node = App.app().library.node(item.nodeId);
            if (node != null) cover = node.cover;
        }
        if (cover != null && !cover.isEmpty()) {
            Thumbs.into(iv, new Img(cover, item.title, item.nodeId), Math.max(w, imgH));
        }

        TextView label = Ui.text(this, item.title, 12, s.text, true);
        label.setGravity(Gravity.CENTER);
        label.setSingleLine(true);
        LinearLayout.LayoutParams lLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 30));
        label.setLayoutParams(lLp);
        card.addView(label);
        return card;
    }

    /** 长按分类：上移 / 下移 / 隐藏；快速与目录分组独立保存 */
    private void tileMenu(final Library.HomeItem item) {
        final String[] opts = {"上移一位", "下移一位", "隐藏此分类"};
        new AlertDialog.Builder(this)
                .setTitle(item.title)
                .setItems(opts, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        if (which == 2) {
                            if (isCatalog()) App.p().toggleCatalogHidden(item.nodeId);
                            else App.p().toggleHomeHidden(item.nodeId);
                            Toast.makeText(CategoryActivity.this, "已隐藏「" + item.title
                                    + "」，标题栏「恢复显示」可放回来", Toast.LENGTH_SHORT).show();
                            recreate();
                            return;
                        }
                        List<String> ids = isCatalog()
                                ? App.app().library.catalogAllIds()
                                : App.app().library.homeAllIds();
                        int idx = ids.indexOf(item.nodeId);
                        int target = which == 0 ? idx - 1 : idx + 1;
                        if (idx < 0 || target < 0 || target >= ids.size()) {
                            Toast.makeText(CategoryActivity.this,
                                    which == 0 ? "已经在最前了" : "已经在最后了",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Collections.swap(ids, idx, target);
                        String order = join(ids);
                        if (isCatalog()) App.p().setCatalogOrder(order);
                        else App.p().setHomeOrder(order);
                        buildCategoryGrid();
                    }
                }).show();
    }

    private String join(List<String> ids) {
        StringBuilder sb = new StringBuilder();
        for (String id : ids) {
            if (sb.length() > 0) sb.append('\n');
            sb.append(id);
        }
        return sb.toString();
    }

    /** 首页右下角悬浮功能栏：侧边栏 / 收藏 / 最近浏览 / 设置 */
    private View bottomBar() {
        LinearLayout bar = Ui.row(this);
        bar.setBackground(Ui.round(this, s.surface, 18));
        int pad = Ui.dp(this, 8);
        bar.setPadding(pad, Ui.dp(this, 8), pad, Ui.dp(this, 8));

        bar.addView(barItem("☰", "侧边栏", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nav.open();
            }
        }));
        bar.addView(barItem("★", "收藏", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                go(Sources.FAV);
            }
        }));
        bar.addView(barItem("⟲", "最近浏览", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                go(Sources.HIST);
            }
        }));
        bar.addView(barItem("⚙", "设置", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                go(AboutActivity.class);
            }
        }));

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.END | Gravity.BOTTOM);
        lp.rightMargin = Ui.dp(this, 12);
        lp.bottomMargin = Ui.dp(this, 14);
        bar.setLayoutParams(lp);
        return bar;
    }

    private View barItem(String glyph, String label, View.OnClickListener l) {
        LinearLayout box = Ui.col(this);
        box.setGravity(Gravity.CENTER);
        int w = Ui.dp(this, 54);
        LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(
                w, ViewGroup.LayoutParams.WRAP_CONTENT);
        box.setLayoutParams(bLp);
        box.setOnClickListener(l);

        TextView ic = Ui.icon(this, glyph, 16, s.text);
        box.addView(ic);
        TextView lab = Ui.text(this, label, 9, s.text2, false);
        lab.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lLp.topMargin = Ui.dp(this, 2);
        lab.setLayoutParams(lLp);
        box.addView(lab);
        return box;
    }

    private void openRandom() {
        List<Img> pool = App.app().library.randomPool();
        if (pool.isEmpty()) return;
        int idx = (int) (Math.random() * pool.size());
        Intent i = new Intent(this, ViewerActivity.class);
        i.putExtra("source", Sources.RANDOM);
        i.putExtra("index", idx);
        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        if (nav != null && nav.isOpen()) {
            nav.close();
            return;
        }
        super.onBackPressed();
    }
}
