package com.hy.greenbuilding.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class VerticalBrightnessSlider extends View {
    public interface OnProgressChangeListener {
        void onStartTrackingTouch(VerticalBrightnessSlider slider);

        void onStopTrackingTouch(VerticalBrightnessSlider slider);

        void onProgressChanged(VerticalBrightnessSlider slider, int progress, boolean fromUser);
    }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private int progress = 80;
    private int max = 100;
    private int min = 1;
    private OnProgressChangeListener listener;

    private final int trackColor = 0xFFE8DAC8;
    private final int activeColor = 0xFFC6A184;
    private final int thumbColor = 0xFF9B704F;

    public VerticalBrightnessSlider(Context context) {
        super(context);
    }

    public VerticalBrightnessSlider(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setMax(int max) {
        this.max = Math.max(1, max);
        setProgress(progress);
    }

    public void setProgress(int progress) {
        this.progress = clamp(progress);
        invalidate();
    }

    public int getProgress() {
        return progress;
    }

    public void setOnProgressChangeListener(OnProgressChangeListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float thumbRadius = dp(28);
        float trackWidth = dp(32);
        float centerX = getWidth() / 2f;
        float top = thumbRadius + dp(1);
        float bottom = getHeight() - thumbRadius - dp(1);
        float trackRadius = trackWidth / 2f;
        float thumbCenterY = progressToY(top, bottom);

        rect.set(centerX - trackRadius, top, centerX + trackRadius, bottom);
        paint.setColor(trackColor);
        canvas.drawRoundRect(rect, trackRadius, trackRadius, paint);

        rect.set(centerX - trackRadius, thumbCenterY, centerX + trackRadius, bottom);
        paint.setColor(activeColor);
        canvas.drawRoundRect(rect, trackRadius, trackRadius, paint);

        paint.setColor(thumbColor);
        canvas.drawCircle(centerX, thumbCenterY, thumbRadius, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (listener != null) {
                    listener.onStartTrackingTouch(this);
                }
                updateFromTouch(event.getY());
                return true;
            case MotionEvent.ACTION_MOVE:
                updateFromTouch(event.getY());
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                updateFromTouch(event.getY());
                if (listener != null) {
                    listener.onStopTrackingTouch(this);
                }
                return true;
            default:
                return true;
        }
    }

    private void updateFromTouch(float y) {
        float thumbRadius = dp(28);
        float top = thumbRadius + dp(1);
        float bottom = getHeight() - thumbRadius - dp(1);
        float ratio = 1f - ((clamp(y, top, bottom) - top) / (bottom - top));
        int value = Math.round(min + ratio * (max - min));
        setProgress(value);
        if (listener != null) {
            listener.onProgressChanged(this, progress, true);
        }
    }

    private float progressToY(float top, float bottom) {
        float ratio = (progress - min) / (float) (max - min);
        return bottom - ratio * (bottom - top);
    }

    private int clamp(int value) {
        return Math.max(min, Math.min(max, value));
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
