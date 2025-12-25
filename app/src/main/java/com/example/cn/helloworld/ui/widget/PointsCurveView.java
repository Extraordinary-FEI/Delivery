package com.example.cn.helloworld.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.cn.helloworld.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PointsCurveView extends View {
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path linePath = new Path();
    private List<Integer> points = new ArrayList<>();

    public PointsCurveView(Context context) {
        super(context);
        init();
    }

    public PointsCurveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PointsCurveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(dp(2));
        linePaint.setColor(getResources().getColor(R.color.primary_color));

        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setColor(getResources().getColor(R.color.primary_color));

        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(dp(1));
        gridPaint.setColor(getResources().getColor(R.color.profile_divider));

        textPaint.setColor(getResources().getColor(R.color.profile_muted_text));
        textPaint.setTextSize(sp(11));
    }

    public void setPoints(List<Integer> points) {
        if (points == null) {
            this.points = new ArrayList<>();
        } else {
            this.points = new ArrayList<>(points);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        float padding = dp(12);
        float chartLeft = padding;
        float chartRight = width - padding;
        float chartTop = padding;
        float chartBottom = height - padding;
        float chartWidth = chartRight - chartLeft;
        float chartHeight = chartBottom - chartTop;

        canvas.drawLine(chartLeft, chartBottom, chartRight, chartBottom, gridPaint);
        canvas.drawLine(chartLeft, chartTop, chartLeft, chartBottom, gridPaint);

        if (points.isEmpty()) {
            canvas.drawText(getResources().getString(R.string.points_curve_empty), chartLeft, chartTop + dp(12), textPaint);
            return;
        }

        List<Integer> data = points;
        if (data.size() == 1) {
            data = new ArrayList<>(data);
            data.add(data.get(0));
        }

        int min = Collections.min(data);
        int max = Collections.max(data);
        if (min == max) {
            max = min + 1;
        }

        float stepX = chartWidth / (data.size() - 1);
        linePath.reset();
        for (int i = 0; i < data.size(); i++) {
            float ratio = (data.get(i) - min) * 1f / (max - min);
            float x = chartLeft + stepX * i;
            float y = chartBottom - ratio * chartHeight;
            if (i == 0) {
                linePath.moveTo(x, y);
            } else {
                linePath.lineTo(x, y);
            }
        }
        canvas.drawPath(linePath, linePaint);
        for (int i = 0; i < data.size(); i++) {
            float ratio = (data.get(i) - min) * 1f / (max - min);
            float x = chartLeft + stepX * i;
            float y = chartBottom - ratio * chartHeight;
            canvas.drawCircle(x, y, dp(3), pointPaint);
        }
    }

    private float dp(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    private float sp(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, getResources().getDisplayMetrics());
    }
}
