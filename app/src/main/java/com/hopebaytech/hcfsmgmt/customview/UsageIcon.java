package com.hopebaytech.hcfsmgmt.customview;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.utils.Logs;

/**
 * @author Aaron
 *         Created by Aaron on 2016/10/12.
 */

public class UsageIcon extends View{

    private static final String CLASSNAME = UsageIcon.class.getSimpleName();

    private final float INNER_CIRCLE_RADIUS_RATIO = 0.65f;
    private final float WHOLE_CIRCLE_RADIUS_RATIO = 0.75f;
    private final float WARNING_RIPPLE_WIDTH_RATIO = 0.025f;
    private final float WARNING_RIPPLE_GAP_RATIO = 0.1f;
    private final float START_ANGLE = 270f;

    private Context mContext;
    private Paint mWholeCirclePaint;
    private Paint mInnerCirclePaint;
    private Paint mValuePaint;
    private SurfaceHolder mSurfaceHolder;
    /**
     * <li> {@link Pair#first} is the drawable of normal icon</li>
     * <li> {@link Pair#second} is the drawable of warning icon</li>
     */
    private Pair<Drawable, Drawable> mIconSrcDrawable;
    /**
     * <li> {@link Pair#first} is the color of normal background</li>
     * <li> {@link Pair#second} is the color of warning background</li>
     */
    private Pair<Integer, Integer> mBackgroundColor;
    /**
     * <li> {@link Pair#first} is the color of normal value</li>
     * <li> {@link Pair#second} is the color of warning value</li>
     */
    private Pair<Integer, Integer> mValueColor;

    /**
     * Object animator for doing the drawing animations
     */
    private ObjectAnimator mDrawAnimator;

    /**
     * Current state of the animation
     */
    private float mPhase = 0f;

    /**
     * The animation is triggered or not
     * */
    private boolean isAnimated;

    private int mValuePercentage;
    private int mWarningPercentage = 100; // Default is 100%

    public UsageIcon(Context context) {
        super(context);
    }

    public UsageIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public UsageIcon(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, 0, 0);
    }

    public UsageIcon(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mContext = context;

        TypedArray typedArray = context.getTheme()
                .obtainStyledAttributes(attrs, R.styleable.UsageIcon, defStyleAttr, defStyleRes);

        Drawable iconSrcNormalDrawable = typedArray.getDrawable(R.styleable.UsageIcon_iconSrcNormal);
        Drawable iconSrcWarningDrawable = typedArray.getDrawable(R.styleable.UsageIcon_iconSrcWarning);
        if (iconSrcWarningDrawable == null) {
            iconSrcWarningDrawable = iconSrcNormalDrawable;
        }
        mIconSrcDrawable = new Pair<>(iconSrcNormalDrawable, iconSrcWarningDrawable);
        typedArray.recycle();

        int normalBackgroundColor = ContextCompat.getColor(getContext(), R.color.colorUserIconNormalBackground);
        int warningBackgroundColor = ContextCompat.getColor(getContext(), R.color.colorUserIconWarningBackground);
        mBackgroundColor = new Pair<>(normalBackgroundColor, warningBackgroundColor);

        int normalValueColor = ContextCompat.getColor(getContext(), R.color.colorUserIconNormalValue);
        int warningValueColor = ContextCompat.getColor(getContext(), R.color.colorUserIconWarningValue);
        mValueColor = new Pair<>(normalValueColor, warningValueColor);

        mWholeCirclePaint = new Paint();
        mWholeCirclePaint.setStyle(Paint.Style.FILL);
        mWholeCirclePaint.setAntiAlias(true);
        mWholeCirclePaint.setColor(mBackgroundColor.first);

        int innerCircleColor = ContextCompat.getColor(getContext(), R.color.colorDefaultBackground);
        mInnerCirclePaint = new Paint();
        mInnerCirclePaint.setStyle(Paint.Style.FILL);
        mInnerCirclePaint.setAntiAlias(true);
        mInnerCirclePaint.setColor(innerCircleColor);

        mValuePaint = new Paint();
        mValuePaint.setStyle(Paint.Style.FILL);
        mValuePaint.setAntiAlias(true);
        mValuePaint.setColor(mValueColor.first);

        mDrawAnimator = ObjectAnimator.ofFloat(this, "phase", 0f, 1.0f).setDuration(500);
        mDrawAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

//        mSurfaceHolder = getHolder();
//        mSurfaceHolder.addCallback(this);
    }

//    @Override
//    public void surfaceCreated(SurfaceHolder holder) {
//    }
//
//    @Override
//    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//
//    }
//
//    @Override
//    public void surfaceDestroyed(SurfaceHolder holder) {
//
//    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawWholeCircle(canvas);
        drawValue(canvas);
        drawInnerCircle(canvas);
        drawIcon(canvas);

        if (isWarning()) {
            drawWarningRipple(canvas);
        }
    }

    private void drawWarningRipple(Canvas canvas) {
        float radius = getRadius();
        float centerX = getWidth() / 2;
        float centerY = getHeight() / 2;

        float rippleGap = radius * WARNING_RIPPLE_GAP_RATIO;
        float rippleThickness = radius * WARNING_RIPPLE_WIDTH_RATIO;
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(rippleThickness);
        paint.setColor(mBackgroundColor.second);

        float firstRippleRadius = radius * WHOLE_CIRCLE_RADIUS_RATIO + rippleGap;
        float secondRippleRadius = firstRippleRadius + rippleGap;
        canvas.drawCircle(centerX, centerY, firstRippleRadius, paint);
        canvas.drawCircle(centerX, centerY, secondRippleRadius, paint);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();

        Bitmap.Config config = drawable.getOpacity() == PixelFormat.OPAQUE ?
                Bitmap.Config.RGB_565 : Bitmap.Config.ARGB_8888;
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    private void drawValue(Canvas canvas) {
        if (isWarning()) {
            mValuePaint.setColor(mValueColor.second);
        } else {
            mValuePaint.setColor(mValueColor.first);
        }

        float radius = getRadius();
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float wholeCircleRadius = radius * WHOLE_CIRCLE_RADIUS_RATIO;
        float left = centerX - wholeCircleRadius;
        float right = centerX + wholeCircleRadius;
        float top = centerY - wholeCircleRadius;
        float bottom = centerY + wholeCircleRadius;
        float sweepAngle = 360f * (mValuePercentage / 100f) * mPhase;
        canvas.drawArc(left, top, right, bottom, START_ANGLE, sweepAngle, true, mValuePaint);
    }

    private void drawIcon(Canvas canvas) {
        Drawable drawable;
        if (isWarning()) {
            drawable = mIconSrcDrawable.second;
        } else {
            drawable = mIconSrcDrawable.first;
        }
        Bitmap bitmap = drawableToBitmap(drawable);
        int left = (getWidth() / 2) - (bitmap.getWidth() / 2);
        int top = (getHeight() / 2) - (bitmap.getHeight() / 2);
        canvas.drawBitmap(bitmap, left, top, null);
    }

    private void drawInnerCircle(Canvas canvas) {
        float radius = getRadius() * INNER_CIRCLE_RADIUS_RATIO;
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, radius, mInnerCirclePaint);
    }

    private void drawWholeCircle(Canvas canvas) {
        if (isWarning()) {
            mWholeCirclePaint.setColor(mBackgroundColor.second);
        } else {
            mWholeCirclePaint.setColor(mBackgroundColor.first);
        }
        float radius = getRadius() * WHOLE_CIRCLE_RADIUS_RATIO;
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, radius, mWholeCirclePaint);
    }

    private float getRadius() {
        return getDiameter() / 2f;
    }

    private float getDiameter() {
        return Math.min(getWidth(), getHeight());
    }

    /**
     * @param iconSrcDrawable the drawable of normal icon and warning icon
     *                        <li> {@link Pair#first} is the drawable of normal icon</li>
     *                        <li> {@link Pair#second} is the drawable of warning icon</li>
     */
    public void setIconSrcDrawable(Pair<Drawable, Drawable> iconSrcDrawable) {
        this.mIconSrcDrawable = iconSrcDrawable;
        invalidate();
    }

    public void showPercentage(int percentage) {
        mValuePercentage = percentage;

        if (isAnimated) {
            invalidate();
        } else {
            isAnimated = true;

            mPhase = 0f;
            mDrawAnimator.start();
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    myDraw();
//                }
//            }).start();
        }
    }

    public void myDraw() {
        Canvas canvas = mSurfaceHolder.lockCanvas();
        canvas.drawColor(ContextCompat.getColor(mContext, R.color.colorDefaultBackground));

        drawWholeCircle(canvas);
        drawValue(canvas);
        drawInnerCircle(canvas);
        drawIcon(canvas);

        if (isWarning()) {
            drawWarningRipple(canvas);
        }

        mSurfaceHolder.unlockCanvasAndPost(canvas);
    }

    public void setWarningPercentage(int percentage) {
        mWarningPercentage = percentage;
    }

    public boolean isWarning() {
        Logs.w(CLASSNAME, "isWarning", "mValuePercentage=" + mValuePercentage + ", mWarningPercentage=" + mWarningPercentage);
        return mValuePercentage >= mWarningPercentage;
    }

    public float getPhase() {
        return mPhase;
    }

    /**
     * The setter function for mDrawAnimator
     * */
    public void setPhase(float mPhase) {
        this.mPhase = mPhase;
        invalidate();
    }

}
