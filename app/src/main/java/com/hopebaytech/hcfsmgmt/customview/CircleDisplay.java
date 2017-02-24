package com.hopebaytech.hcfsmgmt.customview;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.utils.UnitConverter;

/**
 * @author Aaron
 *         Created by Aaron on 2016/5/13.
 */
public class CircleDisplay extends View {

    private Paint mArcPaint;
    private Paint mWholeCirclePaint;
    private Paint mInnerCirclePaint;
    private Paint mCapacityTextPaint;
    private Paint mPercentageTextPaint;

    /**
     * object animator for doing the drawing animations
     */
    private ObjectAnimator mDrawAnimator;

    /**
     * The width of the arc takes, in pixels.
     */
    private float mArcStokeWidth = 20f;

    /**
     * the currently displayed value, can be percent or actual value
     */
    private float mValue = 0f;

    /**
     * the maximum displayable value, depends on the set value
     */
    private float mMaxValue = 0f;

    /**
     * angle that represents the displayed value
     */
    private float mAngle = 0f;

    /**
     * current state of the animation
     */
    private float mPhase = 0f;

    /**
     * the unit that is represented by the circle-display
     */
    private String mUnit = "%";

    /**
     * the decimal format responsible for formatting the values in the view
     */
    private String mCapacityValue = "0B";

    /**
     * startangle of the view
     */
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
        int arcBackground = a.getColor(R.styleable.CircleDisplay_arcBackground, Color.WHITE);
        int valueColor = a.getColor(R.styleable.CircleDisplay_valueColor, Color.WHITE);
        int percentageTextColor = a.getColor(R.styleable.CircleDisplay_percentageTextColor, Color.WHITE);
        int capacityTextColor = a.getColor(R.styleable.CircleDisplay_capacityTextColor, Color.WHITE);
        float percentageTextSize = a.getDimensionPixelSize(R.styleable.CircleDisplay_percentageTextSize, 0);
        float capacityTextSize = a.getDimensionPixelSize(R.styleable.CircleDisplay_capacityTextSize, 0);
        mArcStokeWidth = a.getDimension(R.styleable.CircleDisplay_arcStokeWidth, 0);
        a.recycle();

        mArcPaint = new Paint();
        mArcPaint.setStyle(Paint.Style.FILL);
        mArcPaint.setAntiAlias(true);
        mArcPaint.setColor(valueColor);

        mWholeCirclePaint = new Paint();
        mWholeCirclePaint.setStyle(Paint.Style.FILL);
        mWholeCirclePaint.setAntiAlias(true);
        mWholeCirclePaint.setColor(arcBackground);

        mInnerCirclePaint = new Paint();
        mInnerCirclePaint.setStyle(Paint.Style.FILL);
        mInnerCirclePaint.setAntiAlias(true);

        if (Build.VERSION.SDK_INT > 23) {
            mInnerCirclePaint.setColor(getContext().getColor(R.color.colorDefaultBackground));
        } else {
            mInnerCirclePaint.setColor(getContext().getResources().getColor(R.color.colorDefaultBackground));
        }

        mCapacityTextPaint = new Paint();
        mCapacityTextPaint.setStyle(Paint.Style.STROKE);
        mCapacityTextPaint.setAntiAlias(true);
        mCapacityTextPaint.setTextAlign(Paint.Align.CENTER);
        mCapacityTextPaint.setColor(capacityTextColor);
        mCapacityTextPaint.setTextSize(capacityTextSize);

        mPercentageTextPaint = new Paint();
        mPercentageTextPaint.setStyle(Paint.Style.STROKE);
        mPercentageTextPaint.setTextAlign(Paint.Align.CENTER);
        mPercentageTextPaint.setColor(percentageTextColor);
        mPercentageTextPaint.setTextSize(percentageTextSize);
        mPercentageTextPaint.setAntiAlias(true);
        mPercentageTextPaint.setFakeBoldText(true);

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
        if (Build.VERSION.SDK_INT > 20) {
            canvas.drawArc(0, 0, getHeight(), getWidth(), mStartAngle, angle, true, mArcPaint);
        } else {
            RectF rectF = new RectF(0, 0, getHeight(), getWidth());
            canvas.drawArc(rectF, mStartAngle, angle, true, mArcPaint);
        }
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

        Rect percentageBound = new Rect();
        String percentage = UnitConverter.formatPercentage(number) + mUnit;
        mPercentageTextPaint.getTextBounds(percentage, 0, percentage.length(), percentageBound);

        Rect capacityBound = new Rect();
        mCapacityTextPaint.getTextBounds(mCapacityValue, 0, mCapacityValue.length(), capacityBound);

        float percentageTextX = getWidth() >> 1;
        float percentageTextY = getHeight() >> 1;
        canvas.drawText(percentage, percentageTextX, percentageTextY, mPercentageTextPaint);

        int spaceBtwRatioCapacity = (int) UnitConverter.convertDpToPixel(getResources(), 5);
        float capacityTextX = percentageTextX;
        float capacityTextY = percentageTextY + (percentageBound.height() >> 1) + spaceBtwRatioCapacity + (capacityBound.height() >> 1);
        canvas.drawText(mCapacityValue, capacityTextX, capacityTextY, mCapacityTextPaint);
//        float capacityTextY = ratioTextY + spaceBtwRatioCapacity + mCapacityTextPaint.descent();
    }

    /**
     * draws the inner circle of the view
     *
     * @param c
     */
    private void drawInnerCircle(Canvas c) {
        c.drawCircle(getWidth() / 2, getHeight() / 2, getRadius() - mArcStokeWidth, mInnerCirclePaint);
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
     * @return the radius of the drawn circle, in pixels.
     */
    public float getRadius() {
        return getDiameter() / 2f;
    }

    /**
     * @return the diameter of the drawn circle, in pixels.
     */
    public float getDiameter() {
        return Math.min(getWidth(), getHeight());
    }

}
