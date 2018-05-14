package me.pqpo.smartcropper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.Collections;

/**
 * Created by JebySun on 2018/5/10.
 * email:jebysun@126.com
 */
public class CropView extends View {

    private Context context;
    private Paint paint;

    private Point[] points;


    public CropView(Context context) {
        this(context, null);
    }

    public CropView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CropView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(AndroidUtil.dp2px(1));
        paint.setColor(Color.GREEN);
        //连接处更加平滑
        paint.setStrokeJoin(Paint.Join.ROUND);

    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (points == null) {
            return;
        }
        canvas.drawLine(points[0].x, points[0].y, points[1].x, points[1].y, paint);
        canvas.drawLine(points[1].x, points[1].y, points[2].x, points[2].y, paint);
        canvas.drawLine(points[2].x, points[2].y, points[3].x, points[3].y, paint);
        canvas.drawLine(points[3].x, points[3].y, points[0].x, points[0].y, paint);
    }

    public void setRectPoints(Point[] points) {
        this.points = points;
        invalidate();
    }




}


