package br.com.helpdev.facedetect;

import android.content.Context;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


import org.opencv.core.Rect;

import java.util.ArrayList;
import java.util.List;


/**
 * Copyright (C), 2013-2019, 深圳市浩瀚卓越科技有限公司
 * Author: Abraham.ai@hohem-tech.com
 * Date: 2019/9/9 17:37
 * Description:
 * History:
 */
public class FaceRectView extends View {
    private static final String TAG = "FaceRectView";

    public List<Rect> getFaceRectList() {
        return faceRectList;
    }

    public void setFaceRectList(List<Rect> faceRectList) {

        this.faceRectList = faceRectList;
    }

    private List<Rect> faceRectList = new ArrayList<>();

    public FaceRectView(Context context) {
        this(context, null);
    }

    public FaceRectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (faceRectList != null && faceRectList.size() > 0) {
            for (int i = 0; i < faceRectList.size(); i++) {
                drawFaceRect(canvas, faceRectList.get(i),getColor(i) );
            }
        }
    }
    public int getColor(int i) {
        int color;
        if(i%2==0){
            color=Color.RED;
        }
        else if(i%3==0)
        {
            color=Color.GREEN;
        }
        else if(i%4==0)
        {
            color=Color.BLACK;
        }
        else if(i%5==0)
        {
            color=Color.WHITE;
        }
        else if(i%6==0)
        {
            color=Color.MAGENTA;
        }
        else{
            color=Color.BLUE;
        }
        return color;
    }

    public  void drawFaceRect(Canvas canvas, Rect drawInfo, int color) {
        if (canvas == null || drawInfo == null) {
            return;
        }
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(color);
        Rect rect = drawInfo;

//        int xtemp=rect.width/2+10;
//        rect.x=rect.x+xtemp;
//        rect.width=rect.width+90;


       // 随着脸比例等差值  375  90
        int faceWidthRat=rect.x/2;
        rect.x=rect.x+faceWidthRat;
        Log.i(TAG,  " 当前的宽度： " + rect.width +" 当前的宽度距离   " + faceWidthRat );
       int faceHeightRat=rect.width;
        rect.width=rect.width+faceHeightRat;
        Log.i(TAG,  " 当前的宽度： " + rect.width +" 当前的高度距离   " + faceHeightRat );

        float left=rect.x;
        float top=rect.y;
        float right=rect.x+rect.width;
        float bottom=rect.y+rect.width;
        RectF rectf=new RectF(left,top,right,bottom);

        canvas.drawRect(rectf,paint );

      //  drawPathFace(canvas,rect,rectf,paint);
    }


    public void drawPathFace( Canvas canvas, Rect rect ,RectF rectf , Paint paint  )
    {
        Path mPath = new Path();
        mPath.moveTo(rectf.left, rect.y + rect.height / 4);
        mPath.lineTo(rectf.left, rect.y);
        mPath.lineTo(rectf.left + rect.width / 4, rectf.top);
        //右上
        mPath.moveTo(rectf.right - rect.width / 4, rectf.top);
        mPath.lineTo(rectf.right, rect.y);
        mPath.lineTo(rectf.right, rect.y + rect.height / 4);
        //右下
        mPath.moveTo(rectf.right, rectf.bottom - rect.height / 4);
        mPath.lineTo(rectf.right, rectf.bottom);
        mPath.lineTo(rectf.right - rect.width / 4, rectf.bottom);
        //左下
        mPath.moveTo(rectf.left + rect.width / 4, rectf.bottom);
        mPath.lineTo(rectf.left, rectf.bottom);
        mPath.lineTo(rectf.left, rectf.bottom - rect.height / 4);
        canvas.drawPath(mPath, paint);
    }
    public void clearFaceInfo() {
        faceRectList.clear();
        postInvalidate();
    }
    public void addFaceInfo(Rect faceInfo) {
        faceRectList.add(faceInfo);
        postInvalidate();
    }

    public void addFaceInfo(List<Rect> faceInfoList) {
        faceRectList.addAll(faceInfoList);
        postInvalidate();
    }
}
