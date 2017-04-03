package com.example.administrator.gua_gua_ka.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.example.administrator.gua_gua_ka.R;

/**
 * Created by Administrator on 2017/4/3 0003.
 */

public class GuaGuaKa extends View {

    private Paint mOut;//画笔
    private Path mPath;//绘制路径
    private Canvas mCanvas;//画布
    private Bitmap mBitmap;
    private int mX, mY;

    //绘制一个图层
    private Bitmap mOutBitmap;
    private String mText;//信息
    private Paint mIn;
    private int mTextSize, mTextColor;
    private Rect mTextBound;//记录文本信息的宽和高
    private volatile boolean isComplete = false;//判断遮盖层区域

    // TODO: 2017/4/3 0003 volatile 属性的作用（可见性问题）
    public GuaGuaKa(Context context) {
        this(context, null);
    }

    public GuaGuaKa(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
//        context.obtainStyledAttributes()
    }

    public GuaGuaKa(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        //自定义属性
        TypedArray a = null;
        try {
            a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.GuaGuaKa,
                    defStyleAttr, 0);

            int n = a.getIndexCount();
            for (int i = 0; i < n; i++) {
                int attr = a.getIndex(i);
                switch (attr) {
                    case R.styleable.GuaGuaKa_text:
                        mText = a.getString(attr);
                        break;
                    case R.styleable.GuaGuaKa_textSize:
                        mTextSize = (int) a.getDimension(attr,
                                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20,
                                        getResources().getDisplayMetrics()));
                        break;
                    case R.styleable.GuaGuaKa_textColor:
                        mTextColor = a.getColor(attr, 0x000000);
                        break;
                }
            }
        } finally {
            if (a != null) {
                a.recycle();
            }
        }


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);//少了这句话会咋样

        int width = getMeasuredWidth();
        int hight = getMeasuredHeight();
        //初始化图片
        mBitmap = Bitmap.createBitmap(width, hight, Bitmap.Config.ARGB_8888);
        //初始化画布
        mCanvas = new Canvas(mBitmap);//在哪里画图

        //设置绘制path画笔的一些属性
        setOutPaint();
        //获奖信息画笔属性
        setInPaint();

//        mCanvas.drawColor(Color.parseColor("#c0c0c0"));//假的

        mCanvas.drawRoundRect(new RectF(0, 0, width, hight), 30, 30, mOut);//是你
        mCanvas.drawBitmap(mOutBitmap, null, new RectF(0, 0, width, hight), null);//是你
    }

    private void setInPaint() {
//        mIn.setColor(Color.DKGRAY);
        mIn.setColor(mTextColor);

        mIn.setStyle(Paint.Style.FILL);
        mIn.setTextSize(mTextSize);
        //获取获奖信息绘制的宽和高
        mIn.getTextBounds(mText, 0, mText.length(), mTextBound);
    }

    //设置绘制path画笔的一些属性
    private void setOutPaint() {
        mOut.setColor(Color.parseColor("#c0c0c0"));

        mOut.setAntiAlias(true);
        mOut.setDither(true);
        mOut.setStrokeCap(Paint.Cap.ROUND);
        mOut.setStrokeJoin(Paint.Join.ROUND);

        mOut.setStyle(Paint.Style.FILL);
        mOut.setStrokeWidth(20);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mX = x;
                mY = y;
                mPath.moveTo(mX, mY);
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = Math.abs(x - mX);//绝对值
                int dy = Math.abs(y - mY);
//               Log.e("@@@@@@@@@",dx+"");
//               Log.e("@@@@@@@@@",dy+"");
                if (dx > 3 || dy > 3) {
                    mPath.lineTo(x, y);//不做操作
                }
                mX = x;
                mY = y;

                break;
            case MotionEvent.ACTION_UP://计算挂过的面积
                new Thread(mRunnable).start();//并发
                break;
            default:
                break;
        }

        invalidate();//开始绘画
        return true;
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            int w = getWidth();
            int h = getHeight();

            float wipeArea = 0;
            float totalArea = w * h;

            Bitmap bitmap = mBitmap;

            int[] mPixels = new int[w * h];
            //获取Bitmap上所有像素信息
            bitmap.getPixels(mPixels, 0, w, 0, 0, w, h);
            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    int index = i + j * w;
                    if (mPixels[index] == 0) {
                        wipeArea++;
                    }
                }
            }
            if (wipeArea > 0 && totalArea > 0) {
                int percent = (int) (wipeArea * 100 / totalArea);

                if (percent > 60) {
                    //清除图层区域
                    isComplete = true;
                    postInvalidate();//重回区域


                }
            }

        }
    };

    @Override
    protected void onDraw(Canvas canvas) {
//        canvas.drawBitmap(bitmap, 0, 0, null);//1
        //绘画高度
        canvas.drawText(mText, getWidth() / 2 - mTextBound.width() / 2,
                getHeight() / 2 + mTextBound.height() / 2, mIn);
        //为什么要放在这呢
        if (isComplete) {
            if (listener != null) {
                listener.complete();
            }
        }

        if (!isComplete) {
            drawPath();

            canvas.drawBitmap(mBitmap, 0, 0, null);
        }

    }

    private void drawPath() {
        mOut.setStyle(Paint.Style.STROKE);//最后加的作用是什么？？？
        mOut.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));//假的
        mCanvas.drawPath(mPath, mOut);
    }

    private void init() {
        mOut = new Paint();

        mPath = new Path();

//        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);//1
        mOutBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round);//3
        mText = "谢谢惠顾";
        mTextBound = new Rect();
        mIn = new Paint();
        mTextSize = (int)
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20,
                        getResources().getDisplayMetrics());

    }

    public void setText(String mText) {
        this.mText = mText;
        //获取获奖信息绘制的宽和高
        mIn.getTextBounds(mText, 0, mText.length(), mTextBound);
    }

    private OnGuaGuaKaCompleteListener listener;

    public interface OnGuaGuaKaCompleteListener {
        void complete();
    }

    public void setOnGuaGuaKaCompleteListener(OnGuaGuaKaCompleteListener listener) {
        this.listener = listener;
    }
}
