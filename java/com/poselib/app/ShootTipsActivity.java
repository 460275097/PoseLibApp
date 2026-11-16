package com.poselib.app;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** 临拍技巧：拍照前快速过一遍的六步提示 */
public class ShootTipsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        baseInit();

        LinearLayout page = Ui.col(this);
        root.addView(page, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        page.addView(topBar("临拍技巧", true, null, "开拍前快速核对一套，复刻姿势更容易出片"));

        ScrollView sv = new ScrollView(this);
        sv.setVerticalScrollBarEnabled(false);
        page.addView(sv, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        LinearLayout wrap = Ui.col(this);
        wrap.setPadding(Ui.dp(this, 14), Ui.dp(this, 6), Ui.dp(this, 14), Ui.dp(this, 24));
        sv.addView(wrap);

        LinearLayout card = Ui.col(this);
        card.setBackground(Ui.round(this, s.surface, 16));
        card.setPadding(Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16));
        wrap.addView(card);

        TextView title = Ui.text(this, "拍照前 30 秒", 13.5f, s.text, true);
        card.addView(title);

        TextView body = Ui.text(this,
                "1. 擦干净镜头，打开相机九宫格辅助线。\n\n"
                        + "2. 耐心，诙谐地调节氛围，引导被拍者摆好姿势，记住肢体角度与位置。\n\n"
                        + "3. 对照参考图人物占屏比例，调整拍摄距离、机位高度与拍摄角度。（尽量和示例图一致）\n\n"
                        + "4. 稳住机身，按下快门。\n\n"
                        + "5. 拍完及时给予正向鼓励（目录分类中内置引导和鼓励话术）。此为重点\n\n"
                        + "6. 后期裁剪微调，完成成片。",
                12.5f, s.text, false);
        body.setPadding(0, Ui.dp(this, 12), 0, 0);
        body.setLineSpacing(Ui.dp(this, 6), 1f);
        card.addView(body);

        LinearLayout tip = Ui.col(this);
        tip.setBackground(Ui.round(this, s.surface2, 12));
        tip.setPadding(Ui.dp(this, 12), Ui.dp(this, 10), Ui.dp(this, 12), Ui.dp(this, 10));
        LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tLp.topMargin = Ui.dp(this, 16);
        wrap.addView(tip, tLp);

        TextView tipT = Ui.text(this, "距离怎么判断", 10.5f, s.accent, true);
        tip.addView(tipT);
        TextView tipB = Ui.text(this,
                "示例图里人物占屏幕越满，机位越近；人越小，机位越远。先对齐占屏比例，再对齐高度和角度。",
                11, s.text2, false);
        tipB.setPadding(0, Ui.dp(this, 4), 0, 0);
        tipB.setLineSpacing(Ui.dp(this, 4), 1f);
        tip.addView(tipB);
    }
}
