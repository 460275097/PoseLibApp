package com.poselib.app;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
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
import java.io.FileOutputStream;
import java.io.InputStream;

/** 反馈：文字 1–300 字 + 可选配图 + 联系方式（仅作者可见）；真实联网上传，管理员可见 */
public class FeedbackActivity extends BaseActivity {

    private static final int PICK_IMAGE = 2001;

    private EditText input, contact;
    private TextView counter;
    private ImageView thumb;
    private String imagePath = "";
    private String ver = "";
    private LinearLayout mineBox, publicBox;
    private boolean syncing;

    @Override
    protected void onCreate(android.os.Bundle b) {
        super.onCreate(b);
        baseInit();
        ver = versionName();

        LinearLayout page = Ui.col(this);
        root.addView(page, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        page.addView(topBar("反馈", true, adminLink(), "说点什么都行，作者会看"));

        ScrollView sv = new ScrollView(this);
        sv.setVerticalScrollBarEnabled(false);
        page.addView(sv, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        LinearLayout list = Ui.col(this);
        list.setPadding(Ui.dp(this, 14), Ui.dp(this, 2), Ui.dp(this, 14), Ui.dp(this, 30));
        sv.addView(list);

        list.addView(formCard());

        TextView t1 = Ui.text(this, "我的反馈", 13.5f, s.text, true);
        t1.setPadding(Ui.dp(this, 2), Ui.dp(this, 18), 0, Ui.dp(this, 8));
        list.addView(t1);
        mineBox = Ui.col(this);
        list.addView(mineBox);

        TextView t2 = Ui.text(this, "公开反馈", 13.5f, s.text, true);
        t2.setPadding(Ui.dp(this, 2), Ui.dp(this, 18), 0, Ui.dp(this, 8));
        list.addView(t2);
        publicBox = Ui.col(this);
        list.addView(publicBox);

        refreshLists();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mineBox != null) refreshLists();
    }

    private String versionName() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Throwable e) {
            return "";
        }
    }

    private View adminLink() {
        TextView t = Ui.text(this, "管理员", 11, s.accent, false);
        t.setPadding(Ui.dp(this, 8), Ui.dp(this, 6), Ui.dp(this, 6), Ui.dp(this, 6));
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                go(AdminActivity.class);
            }
        });
        return t;
    }

    private View formCard() {
        LinearLayout card = Ui.col(this);
        card.setBackground(Ui.round(this, s.surface, 14));
        card.setPadding(Ui.dp(this, 13), Ui.dp(this, 12), Ui.dp(this, 13), Ui.dp(this, 13));

        input = new EditText(this);
        input.setBackground(null);
        input.setHint("想加什么分类、哪里不好用、想要什么功能……");
        input.setHintTextColor(s.text2);
        input.setTextColor(s.text);
        input.setTextSize(12.5f);
        input.setGravity(Gravity.TOP);
        input.setMinLines(4);
        input.setMaxLines(8);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(300)});
        card.addView(input, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        counter = Ui.text(this, "0 / 300", 10, s.text2, false);
        counter.setGravity(Gravity.END);
        card.addView(counter);

        TextView verHint = Ui.text(this, "提交后自动带上当前版本号 V" + ver, 9, s.text2, false);
        verHint.setGravity(Gravity.END);
        card.addView(verHint);

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s1, int st, int c, int a) { }

            @Override
            public void onTextChanged(CharSequence s1, int st, int b, int c) {
                counter.setText(s1.length() + " / 300");
            }

            @Override
            public void afterTextChanged(Editable e) { }
        });

        LinearLayout tools = Ui.row(this);
        tools.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams tlLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tlLp.topMargin = Ui.dp(this, 8);
        tools.setLayoutParams(tlLp);

        TextView pick = Ui.text(this, "＋ 加一张图", 11, s.accent, false);
        pick.setBackground(Ui.roundStroke(this, s.surface2, 8, s.divider));
        int p = Ui.dp(this, 8);
        pick.setPadding(p, Ui.dp(this, 5), p, Ui.dp(this, 5));
        pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("image/*");
                startActivityForResult(i, PICK_IMAGE);
            }
        });
        tools.addView(pick);

        thumb = new ImageView(this);
        LinearLayout.LayoutParams thLp = new LinearLayout.LayoutParams(
                Ui.dp(this, 46), Ui.dp(this, 46));
        thLp.leftMargin = Ui.dp(this, 8);
        thumb.setLayoutParams(thLp);
        thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
        thumb.setVisibility(View.GONE);
        thumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePath = "";
                thumb.setVisibility(View.GONE);
                Toast.makeText(FeedbackActivity.this, "已移除配图", Toast.LENGTH_SHORT).show();
            }
        });
        tools.addView(thumb);
        card.addView(tools);

        contact = new EditText(this);
        contact.setBackground(null);
        contact.setHint("联系方式（微信 / 手机号，仅作者可见，可不填）");
        contact.setHintTextColor(s.text2);
        contact.setTextColor(s.text);
        contact.setTextSize(11.5f);
        contact.setSingleLine(true);
        LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cLp.topMargin = Ui.dp(this, 10);
        contact.setLayoutParams(cLp);
        card.addView(contact);

        TextView submit = Ui.text(this, "提交反馈", 13, 0xFFFFFFFF, true);
        submit.setGravity(Gravity.CENTER);
        submit.setBackground(Ui.round(this, s.accent, 10));
        LinearLayout.LayoutParams sLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 42));
        sLp.topMargin = Ui.dp(this, 12);
        submit.setLayoutParams(sLp);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
        card.addView(submit);
        return card;
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == PICK_IMAGE && res == RESULT_OK && data != null && data.getData() != null) {
            try {
                Uri uri = data.getData();
                File out = new File(getFilesDir(), "fb_" + System.currentTimeMillis() + ".jpg");
                InputStream is = getContentResolver().openInputStream(uri);
                FileOutputStream fos = new FileOutputStream(out);
                byte[] buf = new byte[8192];
                int n;
                while ((n = is.read(buf)) > 0) fos.write(buf, 0, n);
                fos.close();
                is.close();
                imagePath = out.getAbsolutePath();
                thumb.setVisibility(View.VISIBLE);
                thumb.setImageBitmap(BitmapFactory.decodeFile(imagePath));
            } catch (Throwable e) {
                Toast.makeText(this, "读取图片失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void submit() {
        final String text = input.getText().toString().trim();
        if (text.length() < 1) {
            Toast.makeText(this, "写两句吧，1–300 字都行", Toast.LENGTH_SHORT).show();
            return;
        }
        if (text.length() > 300) {
            Toast.makeText(this, "最多 300 字", Toast.LENGTH_SHORT).show();
            return;
        }
        final long id = System.currentTimeMillis();
        final String contactText = contact.getText().toString().trim();
        final String img = imagePath;
        try {
            JSONObject o = new JSONObject();
            o.put("k", "fb");
            o.put("id", id);
            o.put("t", text);
            o.put("c", contactText);
            o.put("img", img);
            o.put("v", ver);
            o.put("ts", id);
            o.put("ok", "pending");
            o.put("mine", true);
            o.put("up", false);
            JSONArray arr = Store.feedback();
            arr.put(o);
            Store.saveFeedback(arr);
            UserStore.bumpFeedback();

            input.setText("");
            contact.setText("");
            imagePath = "";
            thumb.setVisibility(View.GONE);
            refreshLists();
            Toast.makeText(this, "正在上传到服务器…", Toast.LENGTH_SHORT).show();
            upload(o);
        } catch (Throwable e) {
            Toast.makeText(this, "提交失败，再试一次", Toast.LENGTH_SHORT).show();
        }
    }

    /** 后台真实上传；失败保留本机副本并提示重试 */
    private void upload(final JSONObject o) {
        Thumbs.pool().execute(new Runnable() {
            @Override
            public void run() {
                final boolean sent = Cloud.fbPush(o);
                Store.setUploaded(o.optLong("id"), sent);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshLists();
                        Toast.makeText(FeedbackActivity.this,
                                sent ? "已上传，作者那边能看到了" : "上传失败，点状态可重试",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void refreshLists() {
        renderMine();
        syncCloud();
    }

    private void renderMine() {
        mineBox.removeAllViews();
        try {
            JSONArray arr = Store.feedback();
            int mine = 0;
            for (int i = arr.length() - 1; i >= 0; i--) {
                JSONObject o = arr.getJSONObject(i);
                if (o.optBoolean("mine", false)) {
                    mineBox.addView(item(o, true));
                    mine++;
                }
            }
            if (mine == 0) {
                mineBox.addView(Ui.text(this, "还没有提交过反馈", 11, s.text2, false));
            }
        } catch (Throwable ignored) {
        }
    }

    private void syncCloud() {
        if (syncing) return;
        syncing = true;
        Thumbs.pool().execute(new Runnable() {
            @Override
            public void run() {
                boolean online;
                try {
                    JSONArray remote = Cloud.fbFetch();
                    JSONArray keep = new JSONArray();
                    for (int i = 0; i < remote.length(); i++) {
                        JSONObject r = remote.optJSONObject(i);
                        if (r == null) continue;
                        // 联系方式只留给作者，普通用户不缓存别人的联系方式
                        r.remove("c");
                        r.remove("img");
                        if ("ok".equals(r.optString("ok")) || isMine(r.optLong("id"))) {
                            keep.put(r);
                        }
                    }
                    Store.mergeRemote(keep);
                    online = true;
                } catch (Throwable e) {
                    online = false;
                }
                final boolean oknet = online;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        syncing = false;
                        renderMine();
                        renderPublic(oknet);
                    }
                });
            }
        });
    }

    private boolean isMine(long id) {
        try {
            JSONArray arr = Store.feedback();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                if (o.optLong("id") == id && o.optBoolean("mine", false)) return true;
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    private void renderPublic(boolean online) {
        publicBox.removeAllViews();
        int pub = 0;
        try {
            JSONArray arr = Store.feedback();
            for (int i = arr.length() - 1; i >= 0; i--) {
                JSONObject o = arr.getJSONObject(i);
                if ("ok".equals(o.optString("ok"))) {
                    publicBox.addView(item(o, false));
                    pub++;
                }
            }
        } catch (Throwable ignored) {
        }
        if (pub == 0) {
            publicBox.addView(Ui.text(this, online ? "还没有公开反馈"
                    : "网络不可用，暂时拉不到公开反馈", 11, s.text2, false));
        }
    }

    private String time(long ts) {
        if (ts <= 0) return "";
        java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("MM-dd HH:mm",
                java.util.Locale.US);
        return f.format(new java.util.Date(ts));
    }

    private View item(final JSONObject o, boolean showStatus) {
        LinearLayout card = Ui.col(this);
        card.setBackground(Ui.round(this, s.surface, 12));
        card.setPadding(Ui.dp(this, 12), Ui.dp(this, 11), Ui.dp(this, 12), Ui.dp(this, 11));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = Ui.dp(this, 8);
        card.setLayoutParams(lp);

        TextView t = Ui.text(this, o.optString("t", ""), 12, s.text, false);
        t.setLineSpacing(Ui.dp(this, 3), 1f);
        card.addView(t);

        TextView meta = Ui.text(this, metaText(o), 9, s.text2, false);
        meta.setPadding(0, Ui.dp(this, 4), 0, 0);
        card.addView(meta);

        String img = o.optString("img", "");
        if (!img.isEmpty() && new File(img).exists()) {
            ImageView iv = new ImageView(this);
            LinearLayout.LayoutParams iLp = new LinearLayout.LayoutParams(
                    Ui.dp(this, 96), Ui.dp(this, 96));
            iLp.topMargin = Ui.dp(this, 8);
            iv.setLayoutParams(iLp);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setImageBitmap(BitmapFactory.decodeFile(img));
            card.addView(iv);
        }

        if (showStatus) {
            String ok = o.optString("ok", "pending");
            boolean up = o.optBoolean("up", false);
            String status;
            int color;
            if ("ok".equals(ok)) {
                status = "已公开";
                color = 0xFF3BC25F;
            } else if ("no".equals(ok)) {
                status = "未通过";
                color = s.text2;
            } else {
                status = "待审核";
                color = s.accent;
            }
            status += up ? " · 已上传" : " · 未上传（点此重试）";
            TextView st = Ui.text(this, status, 10, color, false);
            st.setPadding(0, Ui.dp(this, 8), 0, 0);
            if (!up) {
                st.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(FeedbackActivity.this, "正在重试上传…",
                                Toast.LENGTH_SHORT).show();
                        upload(o);
                    }
                });
            }
            card.addView(st);
        }
        return card;
    }

    private String metaText(JSONObject o) {
        String v = o.optString("v", "");
        String time = time(o.optLong("ts"));
        if (v.isEmpty()) return time;
        return "V" + v + (time.isEmpty() ? "" : " · " + time);
    }
}
