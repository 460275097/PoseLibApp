package com.poselib.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.FrameLayout;

import java.util.List;

/** 横向翻页：左右各预载一页，全部按原图解码；未放大时横滑换图，放大后横滑看细节 */
public class PagerView extends FrameLayout {

    private List<Img> data;
    private int index;
    private final ZoomImageView[] slots = new ZoomImageView[3];
    private float downX, downY, offset;
    private boolean dragging, decided, vertical;
    private VelocityTracker vt;
    private android.animation.ValueAnimator anim;
    private OnPageChangeListener listener;
    private boolean mirror;
    private boolean zoomFlag;
    private boolean hiLoaded;

    public interface OnPageChangeListener {
        void onPage(int index);
        void onZoom(boolean zoomed);
        void onTap();
        void onImageReady();
    }

    public PagerView(Context c) {
        super(c);
        setBackgroundColor(0xFF000000);
        setClipChildren(false);
        for (int i = 0; i < 3; i++) {
            final ZoomImageView z = new ZoomImageView(c);
            z.setLayoutParams(new FrameLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            z.setListener(new ZoomImageView.Listener() {
                @Override
                public void onZoomChanged(boolean zoomed) {
                    if (zoomed && !zoomFlag) {
                        zoomFlag = true;
                        requestHiRes();
                        if (listener != null) listener.onZoom(true);
                    } else if (!zoomed && zoomFlag) {
                        zoomFlag = false;
                        if (listener != null) listener.onZoom(false);
                    }
                }

                @Override
                public void onTap() {
                    if (listener != null) listener.onTap();
                }
            });
            slots[i] = z;
            addView(z);
        }
    }

    public void setOnPageChangeListener(OnPageChangeListener l) {
        listener = l;
    }

    public void setData(List<Img> list, int start) {
        data = list;
        index = Math.max(0, Math.min(start, list.isEmpty() ? 0 : list.size() - 1));
        post(new Runnable() {
            @Override
            public void run() {
                bind(true);
                if (listener != null) listener.onPage(index);
            }
        });
    }

    public int index() {
        return index;
    }

    public Img current() {
        if (data == null || data.isEmpty()) return null;
        return data.get(index);
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        if (w > 0 && data != null && !data.isEmpty()) {
            bind(true);
        }
    }

    private void bind(boolean force) {
        int w = getWidth();
        if (w <= 0) return;
        hiLoaded = false;
        for (int k = 0; k < 3; k++) {
            int pi = index - 1 + k;
            final ZoomImageView v = slots[k];
            if (pi < 0 || pi >= data.size()) {
                v.setVisibility(INVISIBLE);
                v.setImageDrawable(null);
                continue;
            }
            v.setVisibility(VISIBLE);
            v.setTranslationX((pi - index) * w);
            v.resetView();
            v.setScaleX(mirror ? -1f : 1f);
            v.setImageDrawable(null);
            load(v, data.get(pi), Thumbs.VIEW_TARGET);
        }
        zoomFlag = false;
    }

    /** 放大时把当前这张换成更高清的解码结果 */
    private void requestHiRes() {
        if (hiLoaded) return;
        final Img img = current();
        if (img == null) return;
        final Bitmap hit = Thumbs.viewCache().get("v" + Thumbs.HI_TARGET + "|" + img.path);
        if (hit != null) {
            slots[1].replaceBitmapKeepZoom(hit);
            hiLoaded = true;
            return;
        }
        hiLoaded = true;
        final Context ctx = getContext().getApplicationContext();
        Thumbs.pool().execute(new Runnable() {
            @Override
            public void run() {
                final Bitmap b = Thumbs.decodeView(ctx, img, Thumbs.HI_TARGET);
                if (b == null) return;
                post(new Runnable() {
                    @Override
                    public void run() {
                        Img cur = current();
                        if (cur != null && cur.path.equals(img.path)) {
                            slots[1].replaceBitmapKeepZoom(b);
                        }
                    }
                });
            }
        });
    }

    public void setMirror(boolean m) {
        mirror = m;
        for (int k = 0; k < 3; k++) slots[k].setScaleX(m ? -1f : 1f);
    }

    public boolean mirrored() {
        return mirror;
    }

    /** 当前图片在屏幕上的显示范围（给九宫格辅助线用） */
    public RectF imageRect() {
        RectF r = slots[1].imageRect();
        if (r == null) return null;
        RectF out = new RectF(r);
        out.offset(slots[1].getTranslationX(), 0f);
        return out;
    }

    public boolean isZoomed() {
        return slots[1].isZoomed();
    }

    private void load(final ZoomImageView v, final Img img, final int need) {
        final String key = "v" + need + "|" + img.path;
        Bitmap hit = Thumbs.viewCache().get(key);
        if (hit != null) {
            v.setImageBitmap(hit);
            return;
        }
        Thumbs.pool().execute(new Runnable() {
            @Override
            public void run() {
                final Bitmap bmp = Thumbs.decodeView(getContext(), img, need);
                if (bmp == null) return;
                post(new Runnable() {
                    @Override
                    public void run() {
                        v.setImageBitmap(bmp);
                        if (listener != null) listener.onImageReady();
                    }
                });
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (data == null || data.isEmpty()) return true;
        if (isZoomed()) return false;
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = ev.getX();
                downY = ev.getY();
                offset = 0f;
                dragging = false;
                decided = false;
                vertical = false;
                if (vt == null) vt = VelocityTracker.obtain();
                vt.clear();
                vt.addMovement(ev);
                return false;
            case MotionEvent.ACTION_MOVE:
                if (vt != null) vt.addMovement(ev);
                if (!decided) {
                    float dx = Math.abs(ev.getX() - downX);
                    float dy = Math.abs(ev.getY() - downY);
                    if (dx > 8 || dy > 8) {
                        decided = true;
                        vertical = dy > dx;
                    }
                }
                return decided && !vertical;
            default:
                return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (data == null || data.isEmpty()) return true;
        if (vt == null) vt = VelocityTracker.obtain();
        vt.addMovement(ev);

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = ev.getX();
                downY = ev.getY();
                offset = 0f;
                dragging = false;
                return true;
            case MotionEvent.ACTION_MOVE: {
                float dx = ev.getX() - downX;
                if (Math.abs(dx) > 6) dragging = true;
                offset = dx;
                if (isFirst() && dx > 0) offset = dx / 3f;
                if (isLast() && dx < 0) offset = dx / 3f;
                for (int k = 0; k < 3; k++) {
                    slots[k].setTranslationX((k - 1) * getWidth() + offset);
                }
                return true;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                float vx = 0;
                vt.computeCurrentVelocity(1000);
                vx = vt.getXVelocity();
                int w = getWidth();
                boolean next = false, prev = false;
                if (offset < -w * 0.22f || (dragging && vx < -700 && offset < -30)) next = true;
                if (offset > w * 0.22f || (dragging && vx > 700 && offset > 30)) prev = true;
                if (next && !isLast()) animatePage(+1);
                else if (prev && !isFirst()) animatePage(-1);
                else animateBack();
                vt.recycle();
                vt = null;
                return true;
            }
        }
        return true;
    }

    private boolean isFirst() {
        return index <= 0;
    }

    private boolean isLast() {
        return index >= data.size() - 1;
    }

    private void animatePage(final int dir) {
        cancelAnim();
        final int w = getWidth();
        final float from = offset;
        final float to = -dir * w;
        anim = android.animation.ValueAnimator.ofFloat(from, to);
        anim.setDuration(190);
        anim.addUpdateListener(new android.animation.ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(android.animation.ValueAnimator a) {
                float v = (Float) a.getAnimatedValue();
                for (int k = 0; k < 3; k++) {
                    slots[k].setTranslationX((k - 1) * w + v);
                }
            }
        });
        anim.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator a) {
                index += dir;
                for (int k = 0; k < 3; k++) slots[k].setTranslationX((k - 1) * w);
                bind(false);
                if (listener != null) listener.onPage(index);
            }
        });
        anim.start();
    }

    private void animateBack() {
        cancelAnim();
        final int w = getWidth();
        final float from = offset;
        anim = android.animation.ValueAnimator.ofFloat(from, 0f);
        anim.setDuration(150);
        anim.addUpdateListener(new android.animation.ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(android.animation.ValueAnimator a) {
                float v = (Float) a.getAnimatedValue();
                for (int k = 0; k < 3; k++) {
                    slots[k].setTranslationX((k - 1) * w + v);
                }
            }
        });
        anim.start();
    }

    private void cancelAnim() {
        if (anim != null) {
            anim.cancel();
            anim = null;
        }
    }
}
