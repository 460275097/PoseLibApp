package com.poselib.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

/** 可双指缩放 / 双击放大 / 拖动查看的图片控件 */
public class ZoomImageView extends ImageView {

    public interface Listener {
        void onZoomChanged(boolean zoomed);
        void onTap();
    }

    private final Matrix mat = new Matrix();
    private final float[] vals = new float[9];
    private final RectF dispRect = new RectF();
    private float baseScale = 1f, minScale = 1f, maxScale = 4f;
    private float lastX, lastY;
    private boolean dragging;
    private boolean suppressFit;
    private Listener listener;

    private final ScaleGestureDetector scaleDetector;
    private final GestureDetector tapDetector;

    public ZoomImageView(Context c) {
        super(c);
        setScaleType(ScaleType.MATRIX);
        setBackgroundColor(0xFF000000);
        scaleDetector = new ScaleGestureDetector(c, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector d) {
                float factor = d.getScaleFactor();
                matrixValue();
                float cur = vals[Matrix.MSCALE_X];
                float target = cur * factor;
                if (target < minScale) factor = minScale / cur;
                if (target > maxScale) factor = maxScale / cur;
                mat.postScale(factor, factor, d.getFocusX(), d.getFocusY());
                checkBounds();
                setImageMatrix(mat);
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector d) {
                float cur = scale();
                if (cur < minScale * 1.05f) {
                    fit();
                } else {
                    checkBounds();
                    setImageMatrix(mat);
                }
                notifyZoom();
            }
        });
        tapDetector = new GestureDetector(c, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (scale() > minScale * 1.2f) {
                    fit();
                } else {
                    float t = Math.min(maxScale, minScale * 2.4f);
                    mat.postScale(t / scale(), t / scale(), e.getX(), e.getY());
                    checkBounds();
                    setImageMatrix(mat);
                }
                notifyZoom();
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (listener != null) listener.onTap();
                return true;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }
        });
    }

    public void setListener(Listener l) {
        listener = l;
    }

    private void notifyZoom() {
        if (listener != null) listener.onZoomChanged(isZoomed());
    }

    public void resetView() {
        mat.reset();
        baseScale = 1f;
        minScale = 1f;
    }

    private void matrixValue() {
        mat.getValues(vals);
    }

    public float scale() {
        matrixValue();
        return vals[Matrix.MSCALE_X];
    }

    public boolean isZoomed() {
        return getDrawable() != null && scale() > minScale * 1.03f;
    }

    @Override
    public void setImageDrawable(Drawable d) {
        super.setImageDrawable(d);
        if (suppressFit) return;
        post(new Runnable() {
            @Override
            public void run() {
                fit();
                notifyZoom();
            }
        });
    }

    /** 放大时换一张更高清的图，保持当前缩放和位置（用于放大后看细节） */
    public void replaceBitmapKeepZoom(Bitmap b) {
        if (b == null) return;
        float z = getDrawable() == null || minScale <= 0 ? 1f : scale() / minScale;
        if (z < 1f) z = 1f;
        suppressFit = true;
        setImageDrawable(new android.graphics.drawable.BitmapDrawable(getResources(), b));
        suppressFit = false;
        float iw = b.getWidth(), ih = b.getHeight();
        if (iw <= 0 || ih <= 0 || getWidth() == 0) return;
        float s = Math.min(getWidth() / iw, getHeight() / ih);
        baseScale = s;
        minScale = s;
        maxScale = s * 4f;
        mat.setScale(s * z, s * z);
        float dx = (getWidth() - iw * s * z) / 2f;
        float dy = (getHeight() - ih * s * z) / 2f;
        mat.postTranslate(dx, dy);
        setImageMatrix(mat);
        invalidate();
        notifyZoom();
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        if (w > 0 && h > 0 && getDrawable() != null) {
            fit();
        }
    }

    /** 按 FIT_CENTER 初始化 */
    public void fit() {
        Drawable d = getDrawable();
        if (d == null || getWidth() == 0) return;
        float iw = d.getIntrinsicWidth(), ih = d.getIntrinsicHeight();
        if (iw <= 0 || ih <= 0) return;
        float s = Math.min(getWidth() / iw, getHeight() / ih);
        mat.setScale(s, s);
        baseScale = s;
        minScale = s;
        maxScale = s * 4f;
        resetPosition();
        setImageMatrix(mat);
    }

    private void resetPosition() {
        Drawable d = getDrawable();
        if (d == null) return;
        float iw = d.getIntrinsicWidth(), ih = d.getIntrinsicHeight();
        float sc = baseScale;
        float dx = (getWidth() - iw * sc) / 2f;
        float dy = (getHeight() - ih * sc) / 2f;
        mat.postTranslate(dx, dy);
        setImageMatrix(mat);
    }

    /** 图片当前在控件里的矩形（未放大时就是居中显示的图片范围） */
    public RectF imageRect() {
        Drawable d = getDrawable();
        if (d == null) return null;
        RectF r = new RectF(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        mat.mapRect(r);
        return r;
    }

    private void checkBounds() {
        Drawable d = getDrawable();
        if (d == null) return;
        matrixValue();
        float sc = vals[Matrix.MSCALE_X];
        float iw = d.getIntrinsicWidth() * sc, ih = d.getIntrinsicHeight() * sc;
        dispRect.set(0, 0, iw, ih);
        mat.mapRect(dispRect);
        float dw = 0, dh = 0;
        if (dispRect.width() <= getWidth()) {
            dw = (getWidth() - dispRect.width()) / 2f - dispRect.left;
        } else if (dispRect.left > 0) {
            dw = -dispRect.left;
        } else if (dispRect.right < getWidth()) {
            dw = getWidth() - dispRect.right;
        }
        if (dispRect.height() <= getHeight()) {
            dh = (getHeight() - dispRect.height()) / 2f - dispRect.top;
        } else if (dispRect.top > 0) {
            dh = -dispRect.top;
        } else if (dispRect.bottom < getHeight()) {
            dh = getHeight() - dispRect.bottom;
        }
        if (dw != 0 || dh != 0) mat.postTranslate(dw, dh);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        scaleDetector.onTouchEvent(ev);
        tapDetector.onTouchEvent(ev);

        int pc = ev.getPointerCount();
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastX = ev.getX();
                lastY = ev.getY();
                dragging = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (pc == 1 && !scaleDetector.isInProgress()) {
                    float dx = ev.getX() - lastX;
                    float dy = ev.getY() - lastY;
                    if (isZoomed()) {
                        mat.postTranslate(dx, dy);
                        checkBounds();
                        setImageMatrix(mat);
                    }
                    dragging = Math.abs(dx) > 4 || dragging;
                    lastX = ev.getX();
                    lastY = ev.getY();
                } else if (pc >= 2) {
                    lastX = ev.getX();
                    lastY = ev.getY();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }
}
