package widget;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.dk.monitoryourgilrs.R;

public class HeartProgressBar extends View {

    private final static int UNREACHEDCOLOR_DEFAULT = 0xFF69B4;
    private final static int REACHEDCOLOR_DEFAULT = 0xFF1493;
    private final static int INNERTEXTCOLOR_DEFAULT = 0xDC143C;
    private final static int INNERTEXTSIZE_DEFAULT = 10;
    private static final int PROGRESS_DEFAULT = 0;
    private int unReachedColor;
    private int reachedColor;
    private int innerTextColor;
    private int innerTextSize;
    private int progress;
    private int realWidth;
    private int realHeight;
    private Paint underPaint;
    private Paint textPaint;
    private Path path;
    private int paddingTop;
    private int paddingBottom;
    private int paddingLeft;
    private int paddingRight;
    private ArgbEvaluator argbEvaluator;
    public HeartProgressBar(Context context) {
        this(context,null);
    }

    public HeartProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        argbEvaluator = new ArgbEvaluator();
        TypedArray ta = getResources().obtainAttributes(attrs,R.styleable.HeartProgressBar);
        unReachedColor = Color.GRAY; //ta.getColor(R.styleable.HeartProgressBar_UnReachedColor,UNREACHEDCOLOR_DEFAULT);
        reachedColor = ta.getColor(R.styleable.HeartProgressBar_ReachedColor,REACHEDCOLOR_DEFAULT);
        innerTextColor = ta.getColor(R.styleable.HeartProgressBar_InnerTextColor,INNERTEXTCOLOR_DEFAULT);
        innerTextSize = (int) ta.getDimension(R.styleable.HeartProgressBar_InnerTextSize,INNERTEXTSIZE_DEFAULT);
        progress = ta.getInt(R.styleable.HeartProgressBar_Progress,PROGRESS_DEFAULT);
        ta.recycle();
        Log.i("nowColor",progress+"");
        //声明区
        underPaint = new Paint();
        textPaint = new Paint();
        path = new Path();
        //构造画笔区
        underPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        underPaint.setStrokeWidth(5.0f);
        textPaint.setColor(innerTextColor);
        textPaint.setTextSize(innerTextSize);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int usedHeight = getRealHeight(heightMeasureSpec);
        int usedWidth = getRealWidth(widthMeasureSpec);
        setMeasuredDimension(usedWidth,usedHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        realWidth = w;
        realHeight = h;
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paddingBottom = getPaddingBottom();
        paddingTop = getPaddingTop();
        paddingLeft = getPaddingLeft();
        paddingRight = getPaddingRight();
        float pro = ((float)progress)/100.0f;
        Log.i("nowColor","pro"+pro+"");
        int nowColor = (int) argbEvaluator.evaluate(pro,unReachedColor,reachedColor);
        underPaint.setColor(nowColor);
        path.moveTo((float) (0.5*realWidth), (float) (0.17*realHeight));
        path.cubicTo((float) (0.15*realWidth), (float) (-0.35*realHeight), (float) (-0.4*realWidth), (float) (0.45*realHeight), (float) (0.5*realWidth),realHeight);
        path.moveTo((float) (0.5*realWidth),realHeight);
        path.cubicTo((float) (realWidth+0.4*realWidth), (float) (0.45*realHeight),(float) (realWidth-0.15*realWidth), (float) (-0.35*realHeight),(float) (0.5*realWidth), (float) (0.17*realHeight));
        path.close();
        canvas.drawPath(path,underPaint);
        canvas.drawText(String.valueOf(progress),realWidth/2,realHeight/2,textPaint);
    }


    public int getRealHeight(int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightVal = MeasureSpec.getSize(heightMeasureSpec);
        paddingTop = getPaddingTop();
        paddingBottom = getPaddingBottom();
        if(heightMode == MeasureSpec.EXACTLY){
            return paddingTop + paddingBottom + heightVal;
        }else if(heightMode == MeasureSpec.UNSPECIFIED){
            return (int) (Math.abs(underPaint.ascent()-underPaint.descent()) + paddingTop + paddingBottom);
        }else{
            return (int) Math.min((Math.abs(underPaint.ascent()-underPaint.descent()) + paddingTop + paddingBottom),heightVal);
        }
    }

    public int getRealWidth(int widthMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthVal = MeasureSpec.getSize(widthMeasureSpec);
        paddingLeft = getPaddingLeft();
        paddingRight = getPaddingRight();
        if(widthMode == MeasureSpec.EXACTLY){
            return paddingLeft+paddingRight+widthVal;
        }else if(widthMode == MeasureSpec.UNSPECIFIED){
            return (int) (Math.abs(underPaint.ascent()-underPaint.descent()) + paddingLeft + paddingRight);
        }else{
            return (int) Math.min((Math.abs(underPaint.ascent()-underPaint.descent()) + paddingLeft + paddingRight),widthVal);
        }
    }

    public void setProgress(int progress) {
        this.progress = progress;
        invalidate();
    }
}