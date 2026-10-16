package com.game.education.model;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.game.education.R;
import com.game.education.activity.Game;
import java.util.ArrayList;
import java.util.List;

public class LineView extends View {
    public Activity activity;
    public LottieAnimationView gift;
    public List<Item> array_item = new ArrayList<>();
    public boolean start;
    private float mX, mY, Mx1, My1;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mBitmapPaint;
    private final Paint mPaint = new Paint();
    private int count = 1;

    public LineView(Context c) {
        super(c);
    }

    public LineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        SetPaint();
    }

    public LineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawLine(mX, mY, Mx1, My1, mPaint);
        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, 0, 0, mPaint);
        }
        if (mPath != null) {
            canvas.drawPath(mPath, mPaint);
        }
    }

    public void SetPaint() {
        new Handler().postDelayed(() -> {
            mPaint.setAntiAlias(true);
            mPaint.setDither(true);
            if (activity instanceof Game && Game.color >= 0 && ((Game) activity).array_color.size() > Game.color) {
                mPaint.setColor(((Game) activity).array_color.get(Game.color));
            } else {
                mPaint.setColor(ContextCompat.getColor(getContext(), R.color.orang_400));
            }
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(20);
            mPath = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        }, 300);
    }

    private void Touch_start(float x, float y) {
        if (mPath == null) mPath = new Path();
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
        if (gift != null && !gift.isAnimating()) {
            gift.resumeAnimation();
        }
    }

    private void Touch_move(float x, float y) {
        Mx1 = x;
        My1 = y;
        int i = CheckDistans(x, y);
        if (i != -1 && array_item != null && i < array_item.size()) {
            SetLine(array_item.get(i).x, array_item.get(i).y, i);
        }
    }

    private void Touch_up() {
        if (mBitmap != null) {
            mBitmap.eraseColor(Color.TRANSPARENT);
        }
        mX = 0f;
        mY = 0f;
        Mx1 = 0f;
        My1 = 0f;
        count = 1;
        start = false;
        if (array_item != null) {
            for (int i = 0; i < array_item.size(); i++) {
                if (array_item.get(i).active || array_item.get(i).getMagnet()) {
                    array_item.get(i).btn.setBackgroundResource(R.drawable.word_un_select);
                    array_item.get(i).btn.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                    scaleView(array_item.get(i).btn, 1.05f, 1f);
                    array_item.get(i).active = false;
                    array_item.get(i).setMagnet(false);
                }
            }
        }
        if (activity instanceof Game) {
            ((Game) activity).CheckAnswer();
            ((Game) activity).magnetIndex = 0;
            ((Game) activity).checkIndex = 0;
        }
        if (mPath != null) {
            mPath.reset();
        }
        if (gift != null) {
            gift.pauseAnimation();
        }
    }

    public void SetLine(float x, float y, int i) {
        if (mPath != null && mCanvas != null) {
            mPath.lineTo(x, y);
            mCanvas.drawPath(mPath, mPaint);
            mPath.reset();
        }
        Touch_start(array_item.get(i).x, array_item.get(i).y);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (start) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_MOVE:
                    Touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    Touch_up();
                    invalidate();
                    break;
            }
        } else {
            CheckTouch(x, y);
        }
        return true;
    }

    Boolean CheckTouch(float x, float y) {
        if (array_item == null) return false;
        for (int i = 0; i < array_item.size(); i++) {
            float X = Math.abs(x - array_item.get(i).x);
            float Y = Math.abs(y - array_item.get(i).y);
            if (X < 100 && Y < 100) {
                Touch_start(array_item.get(i).x, array_item.get(i).y);
                array_item.get(i).active = true;
                if (activity instanceof Game && Game.color >= 0 && ((Game) activity).array_drawble.size() > Game.color) {
                    array_item.get(i).btn.setBackgroundResource(((Game) activity).array_drawble.get(Game.color));
                }
                array_item.get(i).btn.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                scaleView(array_item.get(i).btn, 1f, 1.05f);
                if (activity instanceof Game) {
                    ((Game) activity).SetText(array_item.get(i).word);
                    ((Game) activity).OnWordSelected(array_item.get(i).word, i);
                    ((Game) activity).sound.PlaySelect(count);
                }
                count++;
                start = true;
                return true;
            }
        }
        return false;
    }

    Integer CheckDistans(float x, float y) {
        if (array_item == null) return -1;
        for (int i = 0; i < array_item.size(); i++) {
            float X = Math.abs(x - array_item.get(i).x);
            float Y = Math.abs(y - array_item.get(i).y);
            if (X < 100 && Y < 100 && !array_item.get(i).active) {
                array_item.get(i).active = true;
                if (activity instanceof Game && Game.color >= 0 && ((Game) activity).array_drawble.size() > Game.color) {
                    array_item.get(i).btn.setBackgroundResource(((Game) activity).array_drawble.get(Game.color));
                }
                array_item.get(i).btn.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                scaleView(array_item.get(i).btn, 1f, 1.05f);
                if (activity instanceof Game) {
                    ((Game) activity).SetText(array_item.get(i).word);
                    ((Game) activity).OnWordSelected(array_item.get(i).word, i);
                    ((Game) activity).sound.PlaySelect(count);
                }
                count++;
                return i;
            }
        }
        return -1;
    }

    public void scaleView(View v, float start, float end) {
        Animation anim = new ScaleAnimation(
                start, end,
                start, end,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f);
        anim.setDuration(100);
        v.startAnimation(anim);
    }

    public void ShowMagnet(String word) {
        if (array_item == null) return;
        for (int i = 0; i < array_item.size(); i++) {
            if (array_item.get(i).word.equals(word)) {
                array_item.get(i).setMagnet(true);
                array_item.get(i).btn.setBackgroundResource(R.drawable.word_select_1);
                array_item.get(i).btn.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                break;
            }
        }
    }
}
