package com.hopebaytech.hcfsmgmt.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.utils.Logs;

/**
 * @author Aaron
 *         Created by Aaron on 2016/11/9.
 */

public class BoosterSeekBar extends View {

    private final String CLASSNAME = BoosterSeekBar.class.getSimpleName();

    private int mThumbNum = 4;
    private int mPaddingLeft = (int) dipToPixels(getContext(), 30);
    private int mPaddingRight = mPaddingLeft;
    private int mPaddingTop = (int) dipToPixels(getContext(), 10);
    private int mPaddingBottom = mPaddingTop;
    private int mBackgroundLineHeight = (int) dipToPixels(getContext(), 8);
    private int mBackgroundThumbRadius = (int) dipToPixels(getContext(), 8);
    private int mThumbIntervalNum = mThumbNum - 1;
    private int mSelectedIndex;

    private float mEndX;
    private float mSeekBarY;
    private float mTouchDownX;
    private float mValueTextY;
    private float mSeekBarWidth;
    private float mThumbInterval;
    private double mMinValue = 0d;
    private double mMaxValue = 100d;
    private double mValue = mMinValue;
    private float mStartX = mPaddingLeft;
    private int mProgress;

    private Drawable mThumb;
    private Rect mTextRect = new Rect();
    private Paint mValuePaint = new Paint();
    private Paint mBackgroundPaint = new Paint();
    private String mValueText = String.valueOf(mValue);

    private boolean isFirstTime = true;

    public BoosterSeekBar(Context context) {
        super(context);
        init();
    }

    public BoosterSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BoosterSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mThumb = ContextCompat.getDrawable(getContext(), R.drawable.icon_btn_selected);

        mValuePaint.setColor(Color.BLACK);
        mValuePaint.setTextAlign(Paint.Align.CENTER);
        mValuePaint.setAntiAlias(true);
        mValuePaint.setTextSize(36);

        mBackgroundPaint.setColor(Color.parseColor("#ededed"));
        mBackgroundPaint.setAntiAlias(true);
    }

    public static float dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mValuePaint.getTextBounds(mValueText, 0, mValueText.length(), mTextRect);

        int defaultWidthSize = mThumb.getIntrinsicWidth() + mPaddingLeft + mPaddingRight;
        int defaultHeightSize = mThumb.getIntrinsicHeight() + mTextRect.height() + mPaddingTop + mPaddingBottom;

        int width = measureDimension(defaultWidthSize, widthMeasureSpec);
        int height = measureDimension(defaultHeightSize, heightMeasureSpec);

        setMeasuredDimension(width, height);
    }

    protected int measureDimension(int defaultSize, int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        int result;
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            result = Math.min(defaultSize, specSize);
        } else {
            result = defaultSize;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        initViewInfo();
        drawBackgroundLine(canvas);
        drawBackgroundThumb(canvas);
        drawForegroundThumb(canvas);
        drawSeekValueText(canvas);
    }

    private void initViewInfo() {
        mValuePaint.getTextBounds(mValueText, 0, mValueText.length(), mTextRect);

        mSeekBarWidth = calcSeekBarWidth();
        mSeekBarY = getHeight() / 2f + Math.max(mThumb.getIntrinsicHeight(), mBackgroundLineHeight) / 2f;
        mValueTextY = getHeight() / 2f - mTextRect.height();
        mStartX = mPaddingLeft;
        mEndX = mStartX + mSeekBarWidth;
        mThumbInterval = calcThumbInterval(mSeekBarWidth);
        mValue = calcValue(mProgress);

        if (isFirstTime) {
            mTouchDownX = mStartX + mThumbInterval * mSelectedIndex;
            isFirstTime = false;
        }
    }

    private void drawBackgroundLine(Canvas canvas) {
        float halfLineHeight = mBackgroundLineHeight / 2f;
        float left = mStartX;
        float top = mSeekBarY - halfLineHeight;
        float bottom = mSeekBarY + halfLineHeight;
        float right = mEndX;
        canvas.drawRect(left, top, right, bottom, mBackgroundPaint);
    }

    private void drawBackgroundThumb(Canvas canvas) {
        for (int i = 0; i < mThumbNum; i++) {
            canvas.save();
            canvas.translate(mThumbInterval * i, 0);
            canvas.drawCircle(mStartX, mSeekBarY, mBackgroundThumbRadius, mBackgroundPaint);
            canvas.restore();
        }
    }

    private float closestThumbX(float touchDownX) {
        float thumbOffset = touchDownX - mStartX;
        int intervalIndex = (int) (thumbOffset / mThumbInterval);

        float closestThumbX;
        if (touchDownX > mStartX + mThumbInterval * (intervalIndex + 0.5f)) {
            mSelectedIndex = intervalIndex + 1;
        } else {
            mSelectedIndex = intervalIndex;
        }
        closestThumbX = mStartX + mThumbInterval * mSelectedIndex;
        return closestThumbX;
    }

    private void drawForegroundThumb(Canvas canvas) {
        float mForegroundThumbOffset = mTouchDownX - mStartX;
        float halfThumbWidth = mThumb.getIntrinsicWidth() / 2f;
        float halfThumbHeight = mThumb.getIntrinsicHeight() / 2f;
        float dx = mStartX - halfThumbWidth + mForegroundThumbOffset;
        float dy = mSeekBarY - halfThumbHeight;

        canvas.save();
        canvas.translate(dx, dy);
        mThumb.setBounds(0, 0, mThumb.getIntrinsicWidth(), mThumb.getIntrinsicHeight());
        mThumb.draw(canvas);
        canvas.restore();
    }

    private OnProgressChangedListener mProgressChangedListener;
    private ValueFormatter mValueFormatter;

    public OnProgressChangedListener getProgressChangedListener() {
        return mProgressChangedListener;
    }

    public void setProgressChangedListener(OnProgressChangedListener mProgressChangedListener) {
        this.mProgressChangedListener = mProgressChangedListener;

        invalidate();
    }

    public ValueFormatter getValueFormatter() {
        return mValueFormatter;
    }

    public void setValueFormatter(ValueFormatter valueFormatter) {
        this.mValueFormatter = valueFormatter;

        invalidate();
    }

    public interface ValueFormatter {

        String getFormatValue(double value);

    }

    public interface OnProgressChangedListener {

        /**
         * @param progress the current progress which range is from 0 to 100.
         */
        void onProcessChanged(int progress);

    }

    private void drawSeekValueText(Canvas canvas) {
        float mForegroundThumbOffset = mTouchDownX - mStartX;
        float x = mStartX + mForegroundThumbOffset;
        float y = mValueTextY;
        mValueText = String.valueOf(mProgress);
        if (mValueFormatter != null) {
            mValueText = mValueFormatter.getFormatValue(mValue);
        }
        canvas.drawText(mValueText, x, y, mValuePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchDownX = limitSeekRange(event);
                mProgress = calcProgress();
                if (mProgressChangedListener != null) {
                    mProgressChangedListener.onProcessChanged(mProgress);
                }
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                mTouchDownX = limitSeekRange(event);
                mProgress = calcProgress();
                if (mProgressChangedListener != null) {
                    mProgressChangedListener.onProcessChanged(mProgress);
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                mTouchDownX = closestThumbX(limitSeekRange(event));
                mProgress = calcProgress();
                if (mProgressChangedListener != null) {
                    mProgressChangedListener.onProcessChanged(mProgress);

                }
                invalidate();
                break;
        }

        return true;
    }

    /**
     * <p>Calculate the progress of the booster seek bar. The range is from 0 to 100.</p>
     */
    private int calcProgress() {
        float thumbOffset = mTouchDownX - mStartX;
        return (int) ((thumbOffset / mSeekBarWidth) * 100);
    }

    private double calcValue(int progress) {
        return mMinValue + ((mMaxValue - mMinValue) * progress / 100f);
    }

    private float limitSeekRange(MotionEvent event) {
        float touchDownX = event.getX();
        if (touchDownX < mStartX) {
            touchDownX = mStartX;
        } else if (touchDownX > mEndX) {
            touchDownX = mEndX;
        }
        return touchDownX;
    }

    public String getValueText() {
        return mValueText;
    }

    public int getThumbNum() {
        return mThumbNum;
    }

    public void setThumbNum(int thumbNum) {
        this.mThumbNum = thumbNum;
        mThumbIntervalNum = thumbNum - 1;

        invalidate();
    }

    /**
     * @param index the index of thumb should to be selected which range is from 0 to
     *              {@link #getThumbNum()} - 1.
     */
    public void setSelectedThumb(int index) {
        if (index + 1 > mThumbNum) {
            index = mThumbNum - 1;
        } else if (index < 0) {
            index = 0;
        }
        mSelectedIndex = index;

        invalidate();
    }

    public int getSelectedIndex() {
        return mSelectedIndex;
    }

    private float calcSeekBarWidth() {
        return getWidth() - (mPaddingLeft + mPaddingRight);
    }

    private float calcThumbInterval(float seekBarWidth) {
        return seekBarWidth / mThumbIntervalNum;
    }

    public double getMinValue() {
        return mMinValue;
    }

    public void setMinValue(double min) {
        this.mMinValue = min;

        invalidate();
    }

    public double getMaxValue() {
        return mMaxValue;
    }

    public void setMaxValue(double max) {
        this.mMaxValue = max;

        invalidate();
    }

    public void setValueRange(double min, double max) {
        this.mMinValue = min;
        this.mMaxValue = max;

        invalidate();
    }

    public double getValue() {
        return mValue;
    }
}