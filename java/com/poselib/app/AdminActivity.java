package com.poselib.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

/** 管理员入口：公开源码中的账号密码留空，本地私有构建再填入。 */
public class AdminActivity extends BaseActivity {

    private static final String ACCOUNT = "";
    private static final String PASSWORD = "";

    private boolean logged;
    private FrameLayout container;
    private boolean syncing;
    private long lastSync;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        baseInit();
        container = new FrameLayout(this);
        root.addView(container, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        showLogin();
    }

    private void showLogin() {
        container.removeAllViews();
        LinearLayout page = Ui.col(this);
        page.setPadding(Ui.dp(this, 18), 0, Ui.dp(this, 18), 0);
        container.addView(page, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        page.addView(topBar("管理员登录", true, null, "仅作者可用"));

        final EditText acc = new EditText(this);
        acc.setHint("管理员账号");
        acc.setHintTextColor(s.text2);
        acc.setTextColor(s.text);
        acc.setTextSize(13);
        acc.setSingleLine(true);
        acc.setInputType(InputType.TYPE_CLASS_NUMBER);
        LinearLayout.LayoutParams aLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        aLp.topMargin = Ui.dp(this, 30);
        acc.setLayoutParams(aLp);
        page.addView(acc);

        final EditText pwd = new EditText(this);
        pwd.setHint("密码");
        pwd.setHintTextColor(s.text2);
        pwd.setTextColor(s.text);
        pwd.setTextSize(13);
        pwd.setSingleLine(true);
        pwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        LinearLayout.LayoutParams pLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pLp.topMargin = Ui.dp(this, 10);
        pwd.setLayoutParams(pLp);
        page.addView(pwd);

        TextView btn = Ui.text(this, "登录", 13, 0xFFFFFFFF, true);
        btn.setGravity(Gravity.CENTER);
        btn.setBackground(Ui.round(this, s.accent, 10));
        LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 44));
        bLp.topMargin = Ui.dp(this, 16);
        btn.setLayoutParams(bLp);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ACCOUNT.equals(acc.getText().toString().trim())
                        && PASSWORD.equals(pwd.getText().toString())) {
                    logged = true;
                    showPanel();
                } else {
                    Toast.makeText(AdminActivity.this, "账号或密码不对",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        page.addView(btn);
    }

    private void showPanel() {
        if (!logged) {
            showLogin();
            return;
        }
        container.removeAllViews();
        LinearLayout page = Ui.col(this);
        container.addView(page, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        TextView btnAll = Ui.text(this, "一键通过", 11, s.accent, false);
        int p = Ui.dp(this, 8);
        btnAll.setPadding(p, Ui.dp(this, 5), p, Ui.dp(this, 5));
        btnAll.setBackground(Ui.roundStroke(this, s.surface2, 8, s.divider));
        btnAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(AdminActivity.this)
                        .setTitle("全部通过？")
                        .setMessage("所有待审核的反馈都会变成公开状态。")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("通过", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int which) {
                                JSONArray arr = Store.feedback();
                                java.util.List<Long> ids = new java.util.ArrayList<Long>();
                                for (int i = 0; i < arr.length(); i++) {
                                    JSONObject o = arr.optJSONObject(i);
                                    if (o != null && !"ok".equals(o.optString("ok"))) {
                                        ids.add(o.optLong("id"));
                                    }
                                }
                                Store.approveAll();
                                Thumbs.pool().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (Long id : ids) Cloud.fbStatus(id, "ok");
                                    }
                                });
                                showPanel();
                                Toast.makeText(AdminActivity.this, "已全部通过",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }).show();
            }
        });

        page.addView(topBar("审核反馈", true, btnAll,
                Store.pendingCount() + " 条待审核 · 共 " + Store.feedback().length() + " 条"));

        ScrollView sv = new ScrollView(this);
        sv.setVerticalScrollBarEnabled(false);
        page.addView(sv, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        LinearLayout list = Ui.col(this);
        list.setPadding(Ui.dp(this, 14), Ui.dp(this, 6), Ui.dp(this, 14), Ui.dp(this, 30));
        sv.addView(list);

        JSONArray arr = Store.feedback();
        if (arr.length() == 0) {
            list.addView(Ui.text(this, "还没有用户提交反馈", 12, s.text2, false));
        }
        for (int i = arr.length() - 1; i >= 0; i--) {
            JSONObject o = arr.optJSONObject(i);
            if (o != null) list.addView(card(o));
        }
        syncRemote();
    }

    /** 从服务器拉取其他手机提交的反馈；成功合并到本机后刷新列表 */
    private void syncRemote() {
        if (syncing || System.currentTimeMillis() - lastSync < 20000) return;
        syncing = true;
        Thumbs.pool().execute(new Runnable() {
            @Override
            public void run() {
                boolean ok = false;
                try {
                    JSONArray remote = Cloud.fbFetch();
                    Store.mergeRemote(remote);
                    ok = true;
                } catch (Throwable ignored) {
                }
                final boolean oknet = ok;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        syncing = false;
                        if (oknet) {
                            lastSync = System.currentTimeMillis();
                            showPanel();
                        } else {
                            Toast.makeText(AdminActivity.this,
                                    "网络不可用，暂时只能看到本机反馈", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private View card(final JSONObject o) {
        final long id = o.optLong("id");
        String status = o.optString("ok", "pending");
        LinearLayout card = Ui.col(this);
        card.setBackground(Ui.round(this, s.surface, 12));
        card.setPadding(Ui.dp(this, 12), Ui.dp(this, 11), Ui.dp(this, 12), Ui.dp(this, 11));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = Ui.dp(this, 9);
        card.setLayoutParams(lp);

        LinearLayout head = Ui.row(this);
        head.setGravity(Gravity.CENTER_VERTICAL);
        TextView st = Ui.text(this, "ok".equals(status) ? "已公开"
                : ("no".equals(status) ? "未通过" : "待审核"), 10,
                "ok".equals(status) ? 0xFF3BC25F : ("no".equals(status) ? s.text2 : s.accent),
                true);
        st.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        head.addView(st);
        TextView time = Ui.text(this, time(o.optLong("ts"))
                + (o.optString("v", "").isEmpty() ? "" : " · V" + o.optString("v", "")),
                9.5f, s.text2, false);
        head.addView(time);
        card.addView(head);

        TextView t = Ui.text(this, o.optString("t", ""), 12, s.text, false);
        t.setPadding(0, Ui.dp(this, 6), 0, 0);
        t.setLineSpacing(Ui.dp(this, 3), 1f);
        card.addView(t);

        String contact = o.optString("c", "");
        if (!contact.isEmpty()) {
            TextView c = Ui.text(this, "联系方式（仅你可见）：" + contact, 10.5f, s.accent, false);
            c.setPadding(0, Ui.dp(this, 6), 0, 0);
            card.addView(c);
        }

        String img = o.optString("img", "");
        if (!img.isEmpty() && new File(img).exists()) {
            ImageView iv = new ImageView(this);
            LinearLayout.LayoutParams iLp = new LinearLayout.LayoutParams(
                    Ui.dp(this, 110), Ui.dp(this, 110));
            iLp.topMargin = Ui.dp(this, 8);
            iv.setLayoutParams(iLp);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setImageBitmap(BitmapFactory.decodeFile(img));
            card.addView(iv);
        }

        LinearLayout ops = Ui.row(this);
        LinearLayout.LayoutParams oLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        oLp.topMargin = Ui.dp(this, 9);
        ops.setLayoutParams(oLp);
        ops.addView(op("通过", 0xFF3BC25F, 0, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Store.setStatus(id, "ok");
                postStatus(id, "ok");
                showPanel();
            }
        }));
        ops.addView(op("不通过", s.text2, Ui.dp(this, 7), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Store.setStatus(id, "no");
                postStatus(id, "no");
                showPanel();
            }
        }));
        ops.addView(op("删除", 0xFFE0483C, Ui.dp(this, 7), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Store.remove(id);
                postStatus(id, "del");
                showPanel();
            }
        }));
        card.addView(ops);
        return card;
    }

    private View op(String label, int color, int marginRight, View.OnClickListener l) {
        TextView t = Ui.text(this, label, 10.5f, color, true);
        t.setGravity(Gravity.CENTER);
        t.setBackground(Ui.roundStroke(this, s.surface2, 8, s.divider));
        t.setPadding(0, Ui.dp(this, 7), 0, Ui.dp(this, 7));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.rightMargin = marginRight;
        t.setLayoutParams(lp);
        t.setOnClickListener(l);
        return t;
    }

    private void postStatus(final long id, final String status) {
        Thumbs.pool().execute(new Runnable() {
            @Override
            public void run() {
                Cloud.fbStatus(id, status);
            }
        });
    }

    private String time(long ts) {
        if (ts <= 0) return "";
        java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("MM-dd HH:mm",
                java.util.Locale.US);
        return f.format(new java.util.Date(ts));
    }
}
