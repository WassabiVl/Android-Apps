package com.example.user.musicapp;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;



public class LiveView extends AppCompatActivity implements SensorEventListener { //www.android-graphview.org/
    private Sensor sensor; //initiate the sensors to use
    private LineGraphSeries<DataPoint> mSeries1; //the series used to display data
    private LineGraphSeries<DataPoint> mSeries2;
    private LineGraphSeries<DataPoint> mSeries3;
    private LineGraphSeries<DataPoint> mSeries4;
    private LineGraphSeries<DataPoint> mSeries5;
    private LineGraphSeries<DataPoint> mSeries6;
    int finalI = 0; //counter that keeps adding up
    int finalII = 0;
    private int progressV = 1; //set the seekbar initial positio
    private int progressVY = 2;
    double[] y3 = null; //used for the trasformed FFT data
    double[] z3 = null;
    double[] x3 = null;
    int finalIX = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_view);
        GraphView graph = (GraphView) findViewById(R.id.graph); //call the graph
        GraphView graph2 = (GraphView) findViewById(R.id.graph2);
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSeries1 = new LineGraphSeries<>();
        mSeries2 = new LineGraphSeries<>();
        mSeries3 = new LineGraphSeries<>();
        mSeries4 = new LineGraphSeries<>();
        mSeries5 = new LineGraphSeries<>();
        mSeries6 = new LineGraphSeries<>();
        graph.addSeries(mSeries1);
        graph.addSeries(mSeries2);
        graph.addSeries(mSeries3);
        graph2.addSeries(mSeries4);
        graph2.addSeries(mSeries5);
        graph2.addSeries(mSeries6);
        mSeries1.setColor(Color.RED);
        mSeries2.setColor(Color.GREEN);
        mSeries3.setColor(Color.BLUE);
        mSeries4.setColor(Color.RED);
        mSeries5.setColor(Color.GREEN);
        mSeries6.setColor(Color.BLUE);

        // activate horizontal zooming and scrolling
        graph.getViewport().setScalable(true);
        graph2.getViewport().setScalable(true);
        // activate horizontal scrolling
        graph.getViewport().setScrollable(true);
        graph2.getViewport().setScrollable(true);
        // activate horizontal and vertical zooming and scrolling
        graph.getViewport().setScalableY(true);
        graph2.getViewport().setScalableY(true);
        // activate vertical scrolling
        graph.getViewport().setScrollableY(true);
        graph2.getViewport().setScrollableY(true);
        //setting the title
        graph.setTitle("Live Data");
        graph2.setTitle("FFT Data");
        //setting the X and Y axis names
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Time/s");
        graph.getGridLabelRenderer().setVerticalAxisTitle("Force m/s");
        graph2.getGridLabelRenderer().setHorizontalAxisTitle("Time/s");
        graph2.getGridLabelRenderer().setVerticalAxisTitle("Force m/s");
        //to alter the graph size
        // set manual X bounds
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(40);
        graph.getLegendRenderer().setFixedPosition(0, 0);
        graph2.getViewport().setXAxisBoundsManual(true);
        graph2.getViewport().setMinX(0);
        graph2.getViewport().setMaxX(40);
        graph2.getLegendRenderer().setFixedPosition(0, 0);
        // set manual Y bounds
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(-20);
        graph.getViewport().setMaxY(40);
        graph2.getViewport().setYAxisBoundsManual(true);
        graph2.getViewport().setMinY(0);
        graph2.getViewport().setMaxY(40);

        //controlling the seekbar
        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        SeekBar seekBar2 = (SeekBar) findViewById(R.id.seekBar2);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {//seekbar change to live data
                progressV = progresValue;}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}});
        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {//seekbar change to FFT  Data
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress%2 == 0){//if the progress is a power of 2
                progressVY = progress;}}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}});}

    public void onSensorChanged(SensorEvent event) {
        sensor = event.sensor;
        final float x = event.values[0];
        final float y = event.values[1];
        final float z = event.values[2];
        new Thread(new Runnable() { //http://www.ssaurel.com/blog/create-a-real-time-line-graph-in-android-with-graphview/
            @Override
            public void run() { // Handles rendering the live sensor data
                for (int i = 0; i < progressV; i++) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            finalI++;
                            mSeries1.appendData(new DataPoint(finalI, x), true, 50);
                            mSeries2.appendData(new DataPoint(finalI, y), true, 50);
                            mSeries3.appendData(new DataPoint(finalI, z), true, 50);}});
                    // sleep to slow down the add of entries
                    try {Thread.sleep(600);} catch (InterruptedException e) {
                        Log.e("graph1", e.toString());}}}}).start();

        new Thread(new Runnable() { //http://www.ssaurel.com/blog/create-a-real-time-line-graph-in-android-with-graphview/
            @Override
            public void run() { //handles rendering live FFT data
                if (x>1 && y>1 && z>1){
                final int twiceTwo =((int) Math.pow(2,progressVY));
                final FFT variableName = new FFT(twiceTwo);
                for (int i = 0; i < twiceTwo; i++) {
                    final double[] x2 = new double[1000];
                    final double[] y2 = new double[1000];
                    final double[] z2 = new double[1000];
                    final double[] tTime = new double[1000];
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < twiceTwo; i++) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        finalII++;
                                        double x1 = Double.parseDouble(Float.toString(x));
                                        double y1 = Double.parseDouble(Float.toString(y));
                                        double z1 = Double.parseDouble(Float.toString(z));
                                        x2[finalII] = x1;
                                        y2[finalII] = y1;
                                        z2[finalII] = z1;
                                        tTime[finalII] = 0;}});
                                // sleep to slow down the add of entries
                                try {Thread.sleep(600);} catch (InterruptedException e) {
                                    Log.e("graph1", e.toString());}}
                                x3= variableName.fft(x2, tTime);
                                y3 = variableName.fft(y2, tTime);
                                z3 = variableName.fft(z2, tTime);
                                for (int ij = 0; ij < twiceTwo; ij++) {
                                    finalIX++;
                                    mSeries4.appendData(new DataPoint(finalIX, x3[ij]), true, 50);
                                    mSeries5.appendData(new DataPoint(finalIX, y3[ij]), true, 50);
                                    mSeries6.appendData(new DataPoint(finalIX, z3[ij]), true, 50);
                                    Log.e("FinalX", Integer.toString(finalIX));
                                }
                            }
                        });
                    try {Thread.sleep(600);} catch (InterruptedException e) {
                        Log.e("graph1", e.toString());}}}}}).start();
    }


    public void onAccuracyChanged(Sensor sensor, int accuracy) {}


}