package com.ebookfrenzy.floatdraw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;

import java.io.Serializable;

public class MainActivity extends AppCompatActivity {
    private static final String STATE_PLOT = "statePlot";

    private MockDataGenerator mMockDataGenerator;
    private Plot mPlot;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null){
            mPlot = new Plot(100, -1.5f, 1.5f);
        }else{
            mPlot = (Plot) savedInstanceState.getSerializable(STATE_PLOT);
        }

        PlotView plotView = new PlotView(this);
        plotView.setPlot(mPlot);
        setContentView(plotView);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putSerializable(STATE_PLOT, mPlot);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mMockDataGenerator = new MockDataGenerator(mPlot);
        mMockDataGenerator.start();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        mMockDataGenerator.quit();
    }


    public static class MockDataGenerator
            extends Thread
    {
        private final Plot mPlot;


        public MockDataGenerator(Plot plot)
        {
            super(MockDataGenerator.class.getSimpleName());
            mPlot = plot;
        }

        @Override
        public void run()
        {
            try{
                float val = 0;
                while(!isInterrupted()){
                    mPlot.add((float) Math.sin(val += 0.16f));
                    Thread.sleep(1000 / 30);
                }
            }
            catch(InterruptedException e){
                //
            }
        }

        public void quit()
        {
            try{
                interrupt();
                join();
            }
            catch(InterruptedException e){
                //
            }
        }
    }

    public static class PlotView extends View
            implements Plot.OnPlotDataChanged
    {
        private Paint mLinePaint;
        private Plot mPlot;


        public PlotView(Context context)
        {
            this(context, null);
        }

        public PlotView(Context context, AttributeSet attrs)
        {
            super(context, attrs);
            mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mLinePaint.setStyle(Paint.Style.STROKE);
            mLinePaint.setStrokeJoin(Paint.Join.ROUND);
            mLinePaint.setStrokeCap(Paint.Cap.ROUND);
            mLinePaint.setStrokeWidth(context.getResources()
                    .getDisplayMetrics().density * 2.0f);
            mLinePaint.setColor(0xFF568607);
            setBackgroundColor(0xFF8DBF45);
        }

        public void setPlot(Plot plot)
        {
            if(mPlot != null){
                mPlot.setOnPlotDataChanged(null);
            }
            mPlot = plot;
            if(plot != null){
                plot.setOnPlotDataChanged(this);
            }
            onPlotDataChanged();
        }

        public Plot getPlot()
        {
            return mPlot;
        }

        public Paint getLinePaint()
        {
            return mLinePaint;
        }

        @Override
        public void onPlotDataChanged()
        {
            ViewCompat.postInvalidateOnAnimation(this);
        }

        @Override
        protected void onDraw(Canvas canvas)
        {
            super.onDraw(canvas);

            final Plot plot = mPlot;
            if(plot == null){
                return;
            }

            final int height = getHeight();
            final float[] data = plot.getData();
            final float unitHeight = height / plot.getRange();
            final float midHeight  = height / 2.0f;
            final float unitWidth  = (float) getWidth() / data.length;

            float lastX = -unitWidth, lastY = 0, currentX, currentY;
            for(int i = 0; i < data.length; i++){
                currentX = lastX + unitWidth;
                currentY = unitHeight * data[i] + midHeight;
                canvas.drawLine(lastX, lastY, currentX, currentY, mLinePaint);
                lastX = currentX;
                lastY = currentY;
            }
        }
    }


    public static class Plot
            implements Serializable
    {
        private final float[] mData;
        private final float   mMin;
        private final float   mMax;

        private transient OnPlotDataChanged mOnPlotDataChanged;


        public Plot(int size, float min, float max)
        {
            mData = new float[size];
            mMin  = min;
            mMax  = max;
        }

        public void setOnPlotDataChanged(OnPlotDataChanged onPlotDataChanged)
        {
            mOnPlotDataChanged = onPlotDataChanged;
        }

        public void add(float value)
        {
            System.arraycopy(mData, 1, mData, 0, mData.length - 1);
            mData[mData.length - 1] = value;
            if(mOnPlotDataChanged != null){
                mOnPlotDataChanged.onPlotDataChanged();
            }
        }

        public float[] getData()
        {
            return mData;
        }

        public float getMin()
        {
            return mMin;
        }

        public float getMax()
        {
            return mMax;
        }

        public float getRange()
        {
            return (mMax - mMin);
        }

        public interface OnPlotDataChanged
        {
            void onPlotDataChanged();
        }
    }
}