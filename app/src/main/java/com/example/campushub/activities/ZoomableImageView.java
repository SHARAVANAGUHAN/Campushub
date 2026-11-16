package com.example.campushub.activities;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

/** Image view with fit-to-page, pinch zoom, double-tap zoom, and drag panning. */
public class ZoomableImageView extends AppCompatImageView {
    private static final float MAX_SCALE = 5f;
    private final Matrix imageMatrix = new Matrix();
    private final float[] matrixValues = new float[9];
    private final ScaleGestureDetector scaleDetector;
    private final GestureDetector gestureDetector;
    private float minimumScale = 1f;
    private float lastX;
    private float lastY;
    private boolean dragging;

    public ZoomableImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setScaleType(ScaleType.MATRIX);
        scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override public boolean onScale(ScaleGestureDetector detector) {
                imageMatrix.getValues(matrixValues);
                float currentScale = matrixValues[Matrix.MSCALE_X];
                float nextScale = Math.max(minimumScale,
                        Math.min(MAX_SCALE, currentScale * detector.getScaleFactor()));
                float factor = nextScale / currentScale;
                imageMatrix.postScale(factor, factor, detector.getFocusX(), detector.getFocusY());
                setImageMatrix(imageMatrix);
                return true;
            }
        });
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onDoubleTap(MotionEvent event) {
                imageMatrix.getValues(matrixValues);
                float currentScale = matrixValues[Matrix.MSCALE_X];
                float targetScale = currentScale > minimumScale * 1.1f
                        ? minimumScale : Math.min(MAX_SCALE, minimumScale * 2.5f);
                float factor = targetScale / currentScale;
                imageMatrix.postScale(factor, factor, event.getX(), event.getY());
                setImageMatrix(imageMatrix);
                return true;
            }
        });
        addOnLayoutChangeListener((view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (right - left != oldRight - oldLeft || bottom - top != oldBottom - oldTop) fitImage();
        });
    }

    @Override public void setImageBitmap(android.graphics.Bitmap bitmap) {
        super.setImageBitmap(bitmap);
        post(this::fitImage);
    }

    private void fitImage() {
        Drawable drawable = getDrawable();
        int availableWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int availableHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        if (drawable == null || availableWidth <= 0 || availableHeight <= 0
                || drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) return;

        float scale = Math.min((float) availableWidth / drawable.getIntrinsicWidth(),
                (float) availableHeight / drawable.getIntrinsicHeight());
        float x = getPaddingLeft() + (availableWidth - drawable.getIntrinsicWidth() * scale) / 2f;
        float y = getPaddingTop() + (availableHeight - drawable.getIntrinsicHeight() * scale) / 2f;
        minimumScale = scale;
        imageMatrix.setScale(scale, scale);
        imageMatrix.postTranslate(x, y);
        setImageMatrix(imageMatrix);
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastX = event.getX();
                lastY = event.getY();
                dragging = true;
                return true;
            case MotionEvent.ACTION_MOVE:
                if (dragging && !scaleDetector.isInProgress()) {
                    imageMatrix.getValues(matrixValues);
                    float currentScale = matrixValues[Matrix.MSCALE_X];
                    if (currentScale > minimumScale * 1.01f) {
                        imageMatrix.postTranslate(event.getX() - lastX, event.getY() - lastY);
                        setImageMatrix(imageMatrix);
                    }
                }
                lastX = event.getX();
                lastY = event.getY();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                dragging = false;
                performClick();
                return true;
            default:
                return true;
        }
    }

    @Override public boolean performClick() {
        super.performClick();
        return true;
    }
}
