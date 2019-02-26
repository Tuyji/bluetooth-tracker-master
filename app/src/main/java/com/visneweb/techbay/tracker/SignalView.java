package com.visneweb.techbay.tracker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Dimension;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by praween on 9/9/16.
 */
public class SignalView extends LinearLayout {
    @Dimension
    int mSpacing = 2;
    @Dimension
    int mStepHeight = 0;
    //    int[] heights;
    ImageView[] bars;
    private int mLineCount = 5;
    @Dimension
    private int mWidth = 48;
    @Dimension
    private int mHeight = 48;
    @Dimension
    private int mRadius = 2;
    @Dimension
    private int mLineWidth = 6;
    private Paint mPaint;
    private Paint mProgressPaint;
    private int mProgress = 0;
    private int colorOff = com.visneweb.techbay.tracker.R.color.colorAccentDark;
    private int colorOn = com.visneweb.techbay.tracker.R.color.colorAccent;

    public SignalView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAttributes(attrs);
        init();
    }

    /**
     * set the default attributes for the view
     *
     * @param attrs
     */
    private void setAttributes(AttributeSet attrs) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, com.visneweb.techbay.tracker.R.styleable.SignalView, 0, 0);
        mLineCount = typedArray.getInt(com.visneweb.techbay.tracker.R.styleable.SignalView_line_count, mLineCount);
        colorOff = typedArray.getInt(com.visneweb.techbay.tracker.R.styleable.SignalView_color_on, com.visneweb.techbay.tracker.R.color.colorAccentDark);
        colorOn = typedArray.getInt(com.visneweb.techbay.tracker.R.styleable.SignalView_color_off, com.visneweb.techbay.tracker.R.color.colorAccent);
        mProgress = typedArray.getInt(com.visneweb.techbay.tracker.R.styleable.SignalView_progress, mProgress);
        mRadius = typedArray.getDimensionPixelSize(com.visneweb.techbay.tracker.R.styleable.SignalView_corner_radius, mRadius);
        mSpacing = typedArray.getDimensionPixelSize(com.visneweb.techbay.tracker.R.styleable.SignalView_line_spacing, mSpacing);
        mLineWidth = typedArray.getDimensionPixelSize(com.visneweb.techbay.tracker.R.styleable.SignalView_line_width, mLineWidth);
    }

    @SuppressWarnings("ResourceAsColor")
    private void init() {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.BOTTOM);
        mStepHeight = (getHeight() - getPaddingTop() - getPaddingBottom()) / mLineCount;
        bars = new ImageView[mLineCount];
//        for(int i = 0; i < mLineCount; i++){
//            LayoutParams params = new LayoutParams(0,mStepHeight*(i+1),1);
////            heights[i] = mStepHeight*(i+1);
//            ImageView bar = new ImageView(mContext);
//            bar.setLayoutParams(params);
//            bar.setImageResource(R.drawable.bar);
//            bars[i] = bar;
//            addView(bars[i]);
//        }
//        mPaint = new Paint();
//        mPaint.setColor(colorOff);
//        mPaint.setDither(false);
//        mPaint.setStyle(Paint.Style.FILL);
//        mPaint.setAntiAlias(true);
//        mPaint.setStrokeJoin(Paint.Join.ROUND);
//
        mProgressPaint = new Paint();
        mProgressPaint.setColor(Color.BLACK);
        mProgressPaint.setDither(false);
        mProgressPaint.setStyle(Paint.Style.FILL);
        mProgressPaint.setAntiAlias(true);
        mProgressPaint.setStrokeJoin(Paint.Join.ROUND);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.i("BLT", "çizmeye çalışıyor");
        for (int i = 0; i < mLineCount; i++) {
            if (i < mProgress) {
                bars[i].setImageState(new int[]{android.R.attr.state_checked}, true);
            } else {
                bars[i].setImageState(new int[]{android.R.attr.state_checked}, false);
            }
        }
    }


//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    @Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//        Log.i("BLT","çizmeye çalışıyor");
//        RectF rect = new RectF(100, 100, 200, 200);
//        canvas.drawRoundRect(rect, mRadius, mRadius, mProgressPaint);
//        for (int i = 0; i < mLineCount; i++) {
//            if (i < mProgress) {
//                bars[i].setImageState(new int[]{android.R.attr.state_checked}, true);
////                canvas.drawRoundRect(rect, mRadius, mRadius, mProgressPaint);
//            } else {
//                bars[i].setImageState(new int[]{android.R.attr.state_checked}, false);
////                canvas.drawRoundRect(rect, mRadius, mRadius, mPaint);
//            }
////            bar.setImageState(new int[]{android.R.attr.state_enabled}, false);
////            int left = getPaddingLeft() + (mSpacing+mLineWidth)*i;
////            int top = getPaddingTop();
////            int right = left + mLineWidth;
////            int bottom = top + mStepHeight*(i+1);
////            Log.i("BLT","left:"+left+" right:"+right+" top:"+top+" bottom"+bottom);
////            RectF rect = new RectF(left, right, top, bottom);
////            if (i < mProgress) {
////                bar.setImageState(new int[]{android.R.attr.state_checked}, false);
////                canvas.drawRoundRect(rect, mRadius, mRadius, mProgressPaint);
////            } else {
////                bar.setImageState(new int[]{android.R.attr.state_empty}, false);
////                canvas.drawRoundRect(rect, mRadius, mRadius, mPaint);
////            }
//        }
//    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int totHPad = getPaddingEnd()+getPaddingStart();
//        int totVPad = getPaddingBottom() + getPaddingTop();
//        int minWidth = mLineCount*mLineWidth+(mLineCount-1)*mSpacing + totHPad;
//        int minHeight = totVPad + 5;
//        mStepHeight = (heightMeasureSpec - totVPad)/mLineCount;
//
//
//        int mode = MeasureSpec.getMode(widthMeasureSpec);
//        int size = MeasureSpec.getSize(widthMeasureSpec);
//
//
//
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        mWidth = getWidth();
//        mHeight = getHeight();
//        mLineWidth = mWidth / mLineCount - mSpacing;
//        mTop = mHeight / mLineCount;
//        mPadding = mSpacing / mLineCount;
//    }

    /**
     * change selected color
     *
     * @param colorResId color resource id
     */
    public void setProgressColor(int colorResId) {
        mProgressPaint.setColor(colorResId);
        invalidate();
    }

    /**
     * change selected color
     *
     * @param progress number of line need to show filled
     */
    public void setProgress(int progress) {
        mProgress = progress;
        invalidate();
    }

    /**
     * change line count of view
     *
     * @param lineCount number of lines which need to display
     */
    public void setLineCount(int lineCount) {
        mLineCount = lineCount;
        invalidate();
    }

    /**
     * change width of view
     *
     * @param width width of view
     */
    public void setWidth(int width) {
        mWidth = width;
    }

    public void setHeight(int height) {
        mHeight = height;
    }

}
