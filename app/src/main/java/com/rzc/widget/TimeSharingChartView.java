package com.rzc.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.rzc.stockdemo.data.TimeSharingData;

import java.util.List;

/**
 * Created by rzc on 17/11/3.
 */

public class TimeSharingChartView extends View {
    private Paint linePaint;
    private Paint solidLinePaint;
    private Paint dottedLinePaint;
    private Path linePath;

    private float maxValue = 100;
    private float minValue = -100;

    private int secondWidth = 4 * 3600;//9:30~11:30 & 1:00~3:00 4hour

    private List<TimeSharingData> timeSharingDataList;

    public TimeSharingChartView(Context context) {
        super(context);
        init();
    }

    public TimeSharingChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setData(float basePrice, float maxPrice, float minPrice, List<TimeSharingData> list) {
        if ((maxPrice - basePrice) * (minPrice - basePrice) > 0) {
            if (maxPrice > basePrice) {
                maxValue = maxPrice;
                minValue = basePrice - (maxPrice - basePrice);
            } else {
                maxValue = basePrice + (basePrice - minPrice);
                minValue = minPrice;
            }
        } else {
            if (maxPrice - basePrice > basePrice - minPrice) {
                maxValue = maxPrice;
                minValue = basePrice - (maxPrice - basePrice);
            } else {
                maxValue = basePrice + (basePrice - minPrice);
                minValue = minPrice;
            }
        }
        timeSharingDataList = list;
    }

    private void init() {
        linePaint = new Paint();
        linePaint.setColor(0xff333333);
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(1 * getResources().getDisplayMetrics().density);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        solidLinePaint = new Paint();
        solidLinePaint.setColor(0xffdddddd);
        solidLinePaint.setAntiAlias(true);
        solidLinePaint.setStyle(Paint.Style.STROKE);
        solidLinePaint.setStrokeWidth(1 * getResources().getDisplayMetrics().density);
        solidLinePaint.setStrokeJoin(Paint.Join.ROUND);
        solidLinePaint.setStrokeCap(Paint.Cap.ROUND);

        dottedLinePaint = new Paint();
        dottedLinePaint.setColor(0xffdddddd);
        dottedLinePaint.setAntiAlias(true);
        dottedLinePaint.setStyle(Paint.Style.STROKE);
        dottedLinePaint.setStrokeWidth(1 * getResources().getDisplayMetrics().density);
        dottedLinePaint.setStrokeJoin(Paint.Join.ROUND);
        dottedLinePaint.setStrokeCap(Paint.Cap.ROUND);
        PathEffect effects = new DashPathEffect(new float[]{
                dottedLinePaint.getStrokeWidth() * 2, dottedLinePaint.getStrokeWidth() * 2}, 0);
        dottedLinePaint.setPathEffect(effects);

        linePath = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (timeSharingDataList == null || timeSharingDataList.size() == 0) {
            return;
        }

        int viewWidth = getWidth();
        int viewHeight = getHeight();

        linePath.reset();
        float lineShift = solidLinePaint.getStrokeWidth() / 2;
        linePath.moveTo(lineShift, lineShift);
        linePath.lineTo(viewWidth - lineShift, lineShift);
        linePath.lineTo(viewWidth - lineShift, viewHeight - lineShift);
        linePath.lineTo(lineShift, viewHeight - lineShift);
        linePath.lineTo(lineShift, lineShift);
        canvas.drawPath(linePath, solidLinePaint);

        float dottedRowSize = viewHeight / 4f;
        float dottedColumnSize = viewWidth / 4f;

        linePath.reset();
        linePath.moveTo(lineShift, dottedRowSize - lineShift);
        linePath.lineTo(viewWidth - lineShift, dottedRowSize - lineShift);
        canvas.drawPath(linePath, dottedLinePaint);

        linePath.reset();
        linePath.moveTo(lineShift, dottedRowSize * 2 - lineShift);
        linePath.lineTo(viewWidth - lineShift, dottedRowSize * 2 - lineShift);
        canvas.drawPath(linePath, dottedLinePaint);

        linePath.reset();
        linePath.moveTo(lineShift, dottedRowSize * 3 - lineShift);
        linePath.lineTo(viewWidth - lineShift, dottedRowSize * 3 - lineShift);
        canvas.drawPath(linePath, dottedLinePaint);


        linePath.reset();
        linePath.moveTo(dottedColumnSize - lineShift, lineShift);
        linePath.lineTo(dottedColumnSize - lineShift, viewHeight - lineShift);
        canvas.drawPath(linePath, dottedLinePaint);

        linePath.reset();
        linePath.moveTo(dottedColumnSize * 2 - lineShift, lineShift);
        linePath.lineTo(dottedColumnSize * 2 - lineShift, viewHeight - lineShift);
        canvas.drawPath(linePath, dottedLinePaint);

        linePath.reset();
        linePath.moveTo(dottedColumnSize * 3 - lineShift, lineShift);
        linePath.lineTo(dottedColumnSize * 3 - lineShift, viewHeight - lineShift);
        canvas.drawPath(linePath, dottedLinePaint);

        linePath.reset();
        TimeSharingData first = timeSharingDataList.get(0);
        linePath.moveTo(getSecondPos(first.second) / secondWidth * viewWidth,
                viewHeight - (first.value - minValue) / (maxValue - minValue) * viewHeight);
        for (int i = 1; i < timeSharingDataList.size(); i++) {
            TimeSharingData data = timeSharingDataList.get(i);
            linePath.lineTo(getSecondPos(data.second) / secondWidth * viewWidth,
                    viewHeight - (data.value - minValue) / (maxValue - minValue) * viewHeight);
        }
        canvas.drawPath(linePath, linePaint);
    }

    public float getMaxValue() {
        return maxValue;
    }

    public float getMinValue() {
        return minValue;
    }

    private float getSecondPos(int second) {
        if (second > 12 * 3600) {//代表下午
            return (float) (second - 1.5 * 3600 - 9.5 * 3600);
        } else {
            return (float) (second - 9.5 * 3600);
        }
    }
}
