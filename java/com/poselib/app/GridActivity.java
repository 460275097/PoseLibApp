package com.poselib.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/** 图片宫格：默认 2 列，右上角按钮可切 2/3/4/5/6 列 */
public class GridActivity extends BaseActivity {

    private static final int TRANSPARENT = 0x00000000;

    private String source;
    private List<Img> data;
    private GridView gv;
    private int columns;
    private TextView colBtn;
    private LinearLayout emptyBox;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        baseInit();
        source = getIntent().getStringExtra("source");
        if (source == null) source = Sources.ALL;
        columns = App.p().columns();
        data = Sources.resolve(source);

        LinearLayout page = Ui.col(this);
        root.addView(page, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        page.addView(topBar(Sources.title(source), true, colBtn = colBtn(),
                data.size() + " 张 · 一行 " + columns + " 张"));

        emptyBox = Ui.col(this);
        emptyBox.setPadding(0, Ui.dp(this, 60), 0, 0);
        TextView empty = Ui.text(this, emptyText(), 13, s.text2, false);
        empty.setGravity(Gravity.CENTER);
        emptyBox.addView(empty, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        emptyBox.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
        page.addView(emptyBox);

        gv = new GridView(this);
        gv.setNumColumns(columns);
        gv.setHorizontalSpacing(Ui.dp(this, 3));
        gv.setVerticalSpacing(Ui.dp(this, 5));
        gv.setPadding(Ui.dp(this, 4), Ui.dp(this, 6), Ui.dp(this, 4), Ui.dp(this, 14));
        gv.setClipToPadding(false);
        gv.setBackgroundColor(s.bg);
        gv.setAdapter(new Adapter());
        page.addView(gv, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
    }

    private String emptyText() {
        if (Sources.FAV.equals(source)) return "还没有收藏的姿势，去图库逛逛吧";
        if (Sources.LEARNED.equals(source)) return "还没有标记「学会」的姿势";
        if (Sources.HIST.equals(source)) return "最近还没有浏览记录";
        return "这个分类下暂时没有图片";
    }

    private TextView colBtn() {
        TextView t = Ui.text(this, "▦ " + columns, 12, s.text, false);
        t.setGravity(Gravity.CENTER);
        t.setBackground(Ui.round(this, s.surface2, 9));
        t.setPadding(Ui.dp(this, 9), Ui.dp(this, 6), Ui.dp(this, 9), Ui.dp(this, 6));
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickColumns();
            }
        });
        return t;
    }

    private void pickColumns() {
        final String[] items = {"一行 2 张（默认）", "一行 3 张", "一行 4 张", "一行 5 张", "一行 6 张"};
        new AlertDialog.Builder(this)
                .setTitle("宫格列数")
                .setSingleChoiceItems(items, columns - 2, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        columns = which + 2;
                        App.p().setColumns(columns);
                        colBtn.setText("▦ " + columns);
                        gv.setNumColumns(columns);
                        ((BaseAdapter) gv.getAdapter()).notifyDataSetChanged();
                        d.dismiss();
                    }
                }).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gv != null) {
            data = Sources.resolve(source);
            ((BaseAdapter) gv.getAdapter()).notifyDataSetChanged();
            if (subView != null) {
                subView.setText(data.size() + " 张 · 一行 " + columns + " 张");
            }
            emptyBox.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private class Adapter extends BaseAdapter {
        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            FrameLayout box;
            if (convertView == null) {
                box = new FrameLayout(GridActivity.this);
                ImageView iv = new ImageView(GridActivity.this);
                iv.setId(1001);
                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                iv.setBackgroundColor(s.surface2);
                box.addView(iv, new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

                TextView badge = Ui.icon(GridActivity.this, "", 12, 0xFFFFFFFF);
                badge.setId(1002);
                FrameLayout.LayoutParams bLp = new FrameLayout.LayoutParams(
                        Ui.dp(GridActivity.this, 20), Ui.dp(GridActivity.this, 20),
                        Gravity.TOP | Gravity.END);
                badge.setLayoutParams(bLp);
                box.addView(badge);
            } else {
                box = (FrameLayout) convertView;
            }

            int gap = Ui.dp(GridActivity.this, 4);
            int w = (screenW() - gap * 2 - Ui.dp(GridActivity.this, 3) * (columns - 1)) / columns;
            int h = (int) (w * 1.30f);
            if (box.getLayoutParams() == null
                    || box.getLayoutParams().height != h
                    || box.getLayoutParams().width != w) {
                box.setLayoutParams(new GridView.LayoutParams(w, h));
            }

            Img img = data.get(position);
            ImageView iv = (ImageView) box.findViewById(1001);
            iv.setBackgroundColor(s.surface2);
            Thumbs.into(iv, img, w);

            TextView badge = (TextView) box.findViewById(1002);
            boolean learned = App.p().learned().contains(img.key());
            boolean fav = App.p().favorites().contains(img.key());
            if (learned) {
                badge.setText("✓");
                badge.setTextColor(0xFFFFFFFF);
                badge.setBackground(Ui.round(GridActivity.this, 0xFF3BC25F, 0));
            } else if (fav) {
                badge.setText("★");
                badge.setTextColor(0xFFFFFFFF);
                badge.setBackground(Ui.round(GridActivity.this, 0xFFFF9F1C, 0));
            } else {
                badge.setText("");
                badge.setBackgroundColor(TRANSPARENT);
            }

            box.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(GridActivity.this, ViewerActivity.class);
                    i.putExtra("source", source);
                    i.putExtra("index", position);
                    startActivity(i);
                }
            });
            return box;
        }
    }
}
