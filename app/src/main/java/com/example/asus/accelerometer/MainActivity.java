package com.example.asus.accelerometer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYSeriesRenderer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener {


    private static final String TAG = "MainActivity";

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean isRunning = false;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;

    private TextView txtAX;
    private TextView txtAY;
    private TextView txtAZ;

    private int counter;

    private XYSeries seriesX;
    private XYSeries seriesY;
    private XYSeries seriesZ;

    private XYSeriesRenderer rendered;
    private XYMultipleSeriesRenderer mrendered;
    private XYMultipleSeriesDataset datasetX;
    private XYMultipleSeriesDataset datasetY;
    private XYMultipleSeriesDataset datasetZ;

    private LinearLayout chartLayoutX;
    private LinearLayout chartLayoutY;
    private LinearLayout chartLayoutZ;

    private GraphicalView chartView;

    private static final int MY_PERMISSIONS_REQUEST_WRITE = 10;
    private boolean canWriteToFile = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();

        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_FASTEST);

        seriesX = new XYSeries("X");
        seriesY = new XYSeries("Y");
        seriesZ = new XYSeries("Z");

        rendered = new XYSeriesRenderer();
        rendered.setLineWidth(2);
        rendered.setColor(Color.BLUE);
        rendered.setPointStyle(PointStyle.CIRCLE);

        mrendered = new XYMultipleSeriesRenderer();
        mrendered.addSeriesRenderer(rendered);
        mrendered.setShowGrid(true);

        datasetX = new XYMultipleSeriesDataset();
        datasetY = new XYMultipleSeriesDataset();
        datasetZ = new XYMultipleSeriesDataset();


        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "myapp:test");


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {


            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE);

            // MY_PERMISSIONS_REQUEST_WRITE is an
            // app-defined int constant. The callback method gets the
            // result of the request.

        } else {
            // Permission has already been granted
            canWriteToFile = true;
        }



    }

    public void initialize(){
        txtAX = (TextView)findViewById(R.id.txtAXValue);
        txtAY =(TextView)findViewById(R.id.txtAYValue);
        txtAZ =(TextView)findViewById(R.id.txtAZValue);

        chartLayoutX = (LinearLayout) findViewById(R.id.llv);
        chartLayoutY = (LinearLayout) findViewById(R.id.llvY);
        chartLayoutZ = (LinearLayout) findViewById(R.id.llvZ);

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (isRunning){
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                float x = sensorEvent.values[0];
                float y = sensorEvent.values[1];
                float z = sensorEvent.values[2];
                float timeStamp = sensorEvent.timestamp;

                Log.d(TAG,"przyspieszenie x: "+ Float.toString(x)+ "czas: "+Float.toString(timeStamp));
                txtAX.setText(Float.toString(x));
                txtAY.setText(Float.toString(y));
                txtAZ.setText(Float.toString(z));
                counter++;


                seriesX.add(counter, x);
                datasetX.clear();
                datasetX.addSeries(seriesX);
                chartView = ChartFactory.getLineChartView(this, datasetX, mrendered);
                chartLayoutX.removeAllViews();
                chartLayoutX.addView(chartView);

                //wykres ay
                seriesY.add(counter, y);
                datasetY.clear();
                datasetY.addSeries(seriesY);
                chartView = ChartFactory.getLineChartView(this, datasetY, mrendered);
                chartLayoutY.removeAllViews();
                chartLayoutY.addView(chartView);

                //wykres az
                seriesZ.add(counter, z);
                datasetZ.clear();
                datasetZ.addSeries(seriesZ);
                chartView = ChartFactory.getLineChartView(this, datasetZ, mrendered);
                chartLayoutZ.removeAllViews();
                chartLayoutZ.addView(chartView);



            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void btnStartPressed(View view){

        Log.d(TAG,"Button START pressed");
        isRunning  = !isRunning;
        if (isRunning) wakeLock.acquire();

        else wakeLock.release();

    }

    private void saveToFile(ArrayList<Double> data, String folder, String fileName){
        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + folder);
        dir.mkdirs();
        File file = new File(dir, fileName);

        String test = file.getAbsolutePath();
        Log.i("My", "FILE LOCATION: " + test);


        try {
            FileOutputStream f = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(f);


            for (int i = 0; i < data.size(); i++) {
                pw.println(data.get(i));
            }

            pw.flush();
            pw.close();
            f.close();


            Toast.makeText(getApplicationContext(),

                    "Data saved",

                    Toast.LENGTH_LONG).show();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i("My", "******* File not found *********");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void btnSavePressed(View view) {
        /*
        if (canWriteToFile) {
        initialize();
            EditText edStart = (EditText) findViewById(R.id.edStart);
            EditText edStop = (EditText) findViewById(R.id.edStop);
            EditText edSampFreq = (EditText) findViewById(R.id.edSampFreq);
            TextView tvFileContent = (TextView) findViewById(R.id.savedFileContent);

            double tStart = 0;
            double tStop = 0;
            double sampFreq = 0;
            boolean success = false;
            try {
                  x = Double.parseDouble(txtAX.getText().toString());

                tStart = Double.parseDouble(edStart.getText().toString());
                tStop = Double.parseDouble(edStop.getText().toString());
                sampFreq = Double.parseDouble(edSampFreq.getText().toString());
                success = true;
            } catch (NumberFormatException e) {


                Toast.makeText(getApplicationContext(),
                        "Can't  save - invalid data",
                        Toast.LENGTH_LONG).show();
            }


            if (success) {
                saveToFile(calculateSinus(tStart, tStop, sampFreq), "/TEST/", "test1.txt");

                String text = readFromFile("/TEST/", "test1.txt");
                tvFileContent.setText(text);

            }
            ;
        } else {

            Toast.makeText(getApplicationContext(),
                    "You don't have WRITE permission",
                    Toast.LENGTH_LONG).show();

        }
*/
    }

    private ArrayList<Double> calculateSinus(double t1, double t2, double fs) {

        double dt = 1. / fs;
        int n = (int) ((t2 - t1) / dt) + 1;
        double time = t1;


        ArrayList<Double> sinValues = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            sinValues.add(Math.sin(time));
            time += dt;
        }

        return sinValues;


    }


    private String readFromFile(String folder, String fileName) {

        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + folder);
        File file = new File(dir, fileName);

        //Read text from file
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i("My", "******* File not found *********");
        } catch (IOException e) {
            //You'll need to add proper error handling here
        } finally {
            return text.toString();
        }


    }


}


