package si.virag.bicikelj.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class CircleLetterView extends View {

    private String text;
    private Paint circlePaint;
    private Paint textPaint;

    public CircleLetterView(Context context) {
        super(context);
        init(context);
    }

    public CircleLetterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CircleLetterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(Color.GRAY);
        circlePaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        textPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15.0f, context.getResources().getDisplayMetrics()));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.WHITE);

        text = "AA";
    }


    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth() - getPaddingLeft() - getPaddingRight();
        int h = getWidth() - getPaddingTop() - getPaddingBottom();

        int cx = getPaddingLeft() + (w / 2);
        int cy = getPaddingTop() + (h / 2);
        int radius = Math.min(w / 2, h / 2);

        canvas.drawCircle(cx, cy, radius, circlePaint);

        int tx = cx;
        int ty = cy - (int) ((textPaint.descent() + textPaint.ascent()) / 2);
        canvas.drawText(text, tx, ty, textPaint);
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setColor(int color) {
        circlePaint.setColor(color);
    }

}
