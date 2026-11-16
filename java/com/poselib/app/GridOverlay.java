package com.poselib.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

/** 大图上的九宫格构图辅助线（只在未放大时显示） */
public class GridOverlay extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF rect;

    public GridOverlay(Context c) {
        super(c);
        paint.setColor(0x33FFFFFF);
        paint.setStrokeWidth(Math.max(1f, c.getResources().getDisplayMetrics().density * 0.8f));
        setWillNotDraw(false);
    }

    public void setRect(RectF r) {
        rect = r == null ? null : new RectF(r);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (rect == null || rect.width() <= 0 || rect.height() <= 0) return;
        float x1 = rect.left + rect.width() / 3f;
        float x2 = rect.left + rect.width() * 2f / 3f;
        float y1 = rect.top + rect.height() / 3f;
        float y2 = rect.top + rect.height() * 2f / 3f;
        canvas.drawLine(x1, rect.top, x1, rect.bottom, paint);
        canvas.drawLine(x2, rect.top, x2, rect.bottom, paint);
        canvas.drawLine(rect.left, y1, rect.right, y1, paint);
        canvas.drawLine(rect.left, y2, rect.right, y2, paint);
    }
}
