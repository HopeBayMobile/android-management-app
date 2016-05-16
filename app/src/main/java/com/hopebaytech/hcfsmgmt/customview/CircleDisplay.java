package com.hopebaytech.hcfsmgmt.customview;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.UnitConverter;

/**
 * Created by Aaron on 2016/5/13.
 */
public class CircleDisplay extends View {

    private final String CLASSNAME = CircleDisplay.class.getSimpleName();

    private Paint mArcPaint;
    private Paint mWholeCirclePaint;
    private Paint mInnerCirclePaint;
    private Paint mCapacityTextPaint;
    private Paint mRatioTextPaint;

    /** object animator for doing the drawing animations */
    private ObjectAnimator mDrawAnimator;

    /** percent of the maximum width the arc takes */
    private float mValueWidthPercent = 25f;

    /** the currently displayed value, can be percent or actual value */
    private float mValue = 0f;

    /** the maximum displayable value, depends on the set value */
    private float mMaxValue = 0f;

    /** angle that represents the displayed value */
    private float mAngle = 0f;

    /** current state of the animation */
    private float mPhase = 0f;

    /** the unit that is represented by the circle-display */
    private String mUnit = "%";

    /** the decimal format responsible for formatting the values in the view */
    private String mCapacityValue = "0B";

    /** startangle of the view */
    private float mStartAngle = 270f;

    public CircleDisplay(Context context) {
        super(context);
    }

    public CircleDisplay(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CircleDisplay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CircleDisplay, 0, 0);
        int circleBackgroundColor = a.getColor(R.styleable.CircleDisplay_circleBackground, Color.WHITE);
        int valueColor = a.getColor(R.styleable.CircleDisplay_valueColor, Color.WHITE);
        int ratioColor = a.getColor(R.styleable.CircleDisplay_ratioColor, Color.WHITE);
        int capacityColor = a.getColor(R.styleable.CircleDisplay_capacityColor, Color.WHITE);
        float ratioTextSize = a.getDimensionPixelSize(R.styleable.CircleDisplay_ratioTextSize, 0);
        float capacityTextSize = a.getDimensionPixelSize(R.styleable.CircleDisplay_capacityTextSize, 0);
        a.recycle();

        mArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mArcPaint.setStyle(Paint.Style.FILL);
        mArcPaint.setColor(valueColor);

        mWholeCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mWholeCirclePaint.setStyle(Paint.Style.FILL);
        mWholeCirclePaint.setColor(circleBackgroundColor);

        mInnerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mInnerCirclePaint.setStyle(Paint.Style.FILL);
        mInnerCirclePaint.setColor(Color.WHITE);

        mCapacityTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCapacityTextPaint.setStyle(Paint.Style.STROKE);
        mCapacityTextPaint.setTextAlign(Paint.Align.CENTER);
        mCapacityTextPaint.setColor(capacityColor);
        mCapacityTextPaint.setTextSize(capacityTextSize);

        mRatioTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRatioTextPaint.setStyle(Paint.Style.STROKE);
        mRatioTextPaint.setTextAlign(Paint.Align.CENTER);
        mRatioTextPaint.setColor(ratioColor);
        mRatioTextPaint.setTextSize(ratioTextSize);

        mDrawAnimator = ObjectAnimator.ofFloat(this, "phase", 0f, 1.0f).setDuration(500);
        mDrawAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawWholeCircle(canvas);
        drawValue(canvas);
        drawInnerCircle(canvas);
        drawText(canvas);
    }

    /**
     * draws the actual value slice/arc
     *
     * @param canvas
     */
    private void drawValue(Canvas canvas) {
        float angle = mAngle * mPhase;
        canvas.drawArc(0, 0, getHeight(), getWidth(), mStartAngle, angle, true, mArcPaint);
    }

    /**
     * shows the given value in the circle view
     *
     * @param toShow
     * @param total
     * @param capacity
     * @param animated
     */
    public void showValue(float toShow, float total, long capacity, boolean animated) {
        mAngle = calcAngle(toShow / total * 100f);
        mValue = toShow;
        mMaxValue = total;
        mCapacityValue = UnitConverter.convertByteToProperUnit(capacity);

        if (animated)
            startAnim();
        else {
            mPhase = 1f;
            invalidate();
        }
    }

    public void startAnim() {
        mPhase = 0f;
        mDrawAnimator.start();
    }

    /**
     * returns the current animation status of the view
     *
     * @return
     */
    public float getPhase() {
        return mPhase;
    }

    /**
     * DONT USE THIS METHOD
     *
     * @param phase
     */
    public void setPhase(float phase) {
        mPhase = phase;
        invalidate();
    }

    /**
     * calculates the needed angle for a given value
     *
     * @param percent
     * @return
     */
    private float calcAngle(float percent) {
        return percent / 100f * 360f;
    }

    /**
     * draws the text in the center of the view
     *
     * @param canvas
     */
    private void drawText(Canvas canvas) {
        float number = mValue * mPhase;

        String formatValue = UnitConverter.formatPercentage(number);

        float ratioTextX = getWidth() >> 1;
        float ratioTextY = getHeight() >> 1;
        canvas.drawText(formatValue + "" + mUnit, ratioTextX, ratioTextY, mRatioTextPaint);
        HCFSMgmtUtils.log(Log.WARN, CLASSNAME, "onReceive", "ratioTextY=" + ratioTextY);

        int spaceBtwRatioCapacity = (int) Utils.convertDpToPixel(getResources(), 8);
        float capacityTextX = ratioTextX;
        float capacityTextY = ratioTextY + spaceBtwRatioCapacity + mCapacityTextPaint.descent();
        canvas.drawText(mCapacityValue, capacityTextX, capacityTextY, mCapacityTextPaint);
    }

    /**
     * draws the inner circle of the view
     *
     * @param c
     */
    private void drawInnerCircle(Canvas c) {
        c.drawCircle(getWidth() / 2, getHeight() / 2, getRadius() / 100f * (100f - mValueWidthPercent), mInnerCirclePaint);
    }

    /**
     * draws the background circle with less alpha
     *
     * @param c Canvas
     */
    private void drawWholeCircle(Canvas c) {
        float r = getRadius();
        c.drawCircle(getWidth() / 2, getHeight() / 2, r, mWholeCirclePaint);
    }

    /**
     * returns the radius of the drawn circle
     *
     * @return
     */
    public float getRadius() {
        return getDiameter() / 2f;
    }

    public float getDiameter() {
        return Math.min(getWidth(), getHeight());
    }

    public static abstract class Utils {

        /**
         * This method converts dp unit to equivalent pixels, depending on device density.
         *
         * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
         * @return A float value to represent px equivalent to dp depending on device density
         */
        public static float convertDpToPixel(Resources r, float dp) {
            DisplayMetrics metrics = r.getDisplayMetrics();
            float px = dp * (metrics.densityDpi / 160f);
            return px;
        }
    }

}
