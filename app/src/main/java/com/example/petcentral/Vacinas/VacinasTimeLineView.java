package com.example.petcentral.Vacinas;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class VacinasTimeLineView extends View {

    public VacinasTimeLineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);

        // Dentro do método onDraw
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2);

        canvas.drawLine(0, 0, 20,20, paint);
    }
}
