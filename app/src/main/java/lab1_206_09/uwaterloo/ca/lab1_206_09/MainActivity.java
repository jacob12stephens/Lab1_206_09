package lab1_206_09.uwaterloo.ca.lab1_206_09;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedList;
import ca.uwaterloo.sensortoy.LineGraphView;

public class MainActivity extends AppCompatActivity{
    LinkedList<String> lastHundred;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setting linear layout.
        LinearLayout ll = (LinearLayout) findViewById(R.id.layout);
        ll.setOrientation(LinearLayout.VERTICAL);
        LineGraphView graph = new LineGraphView(getApplicationContext(), 100, Arrays.asList("x", "y", "z"));
        ll.addView(graph);
        graph.setVisibility((View.VISIBLE));

        //Add button for writing/clear max
        Button clearMaxButton = new Button(getApplicationContext());
        clearMaxButton.setText("Clear historical max readings: ");
        ll.addView(clearMaxButton);
        //Button to create CSV file for accelerometer
        Button generateCSVButton = new Button(getApplicationContext());
        generateCSVButton.setText("Generate CSV file of Values");
        ll.addView(generateCSVButton);

        //Creating new textview object for light sensor.
        TextView lightSense = new TextView(getApplicationContext());
        TextView maxLightSense = new TextView(getApplicationContext());

        //Core of event driven programming.  Asks OS for access to the sensor information.
        //SINGLETON Pattern... limited to one instance only, can only be defined once or will crash.
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //Requesting access to the light sensors.
        Sensor lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        //Create instance of sensor event listener and register listener to the sensor event.
        final LightSensorEventListener lightSEL = new LightSensorEventListener(lightSense, maxLightSense);
        sensorManager.registerListener(lightSEL, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);

        //Add View for light sensor to linear layout.
        ll.addView(lightSense);
        ll.addView(maxLightSense);

        lastHundred = new LinkedList<String>();
        //Add Accelerometer sensor handler and display values
        final TextView accelerometer = new TextView(getApplicationContext());
        TextView maxAccelerometer = new TextView(getApplicationContext());
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        final AccelerometerEventListener accelerometerSEL = new AccelerometerEventListener(accelerometer, maxAccelerometer, graph);
        sensorManager.registerListener(accelerometerSEL, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
        ll.addView(accelerometer);
        ll.addView(maxAccelerometer);

        //Add Magnetic Field sensor handler and display values
        TextView magField = new TextView(getApplicationContext());
        TextView magFieldMax = new TextView(getApplicationContext());
        Sensor magFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        final MagneticFieldEventListener magFieldSEL = new MagneticFieldEventListener(magField, magFieldMax);
        sensorManager.registerListener(magFieldSEL, magFieldSensor, SensorManager.SENSOR_DELAY_NORMAL);
        ll.addView(magField);
        ll.addView(magFieldMax);

        //Add Rotation Vector sensor handler and display values
        TextView rotVector = new TextView(getApplicationContext());
        TextView rotVectorMax = new TextView(getApplicationContext());
        Sensor rotVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        final RotationVectorEventListener rotVectorSEL = new RotationVectorEventListener(rotVector, rotVectorMax);
        sensorManager.registerListener(rotVectorSEL, rotVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
        ll.addView(rotVector);
        ll.addView(rotVectorMax);

        clearMaxButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                lightSEL.clearMax();
                accelerometerSEL.clearMax();
                magFieldSEL.clearMax();
                rotVectorSEL.clearMax();
            }
        });

        generateCSVButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                File outputFile = null;
                PrintWriter writer = null;
                //Initialize print writer and open new File.
                try{
                    outputFile = new File(getExternalFilesDir("Lab1_206_09 Files"), "AccelerometerRecord.csv");
                    writer = new PrintWriter(outputFile);
                }catch(IOException e){
                    Log.d("FileIO", "error writing file");
                }

                //Get access to accelerometer array
                lastHundred = accelerometerSEL.maxArray();
                //Print out all the values to the file
                for (int i = 0; i < lastHundred.size(); i++){
                    writer.println(lastHundred.get(i));
                }
                writer.flush();
                writer.close();
                Log.d("Lab1_206_09 File: ", "Accelerometer values recorded in " + outputFile);

            }
        });

//        TextView test = new TextView(getApplicationContext());
//        test.setText("TESTING BITCHES" + String.format(maxAccelArray.get(0)));
//        ll.addView(test);
    }

}


class LightSensorEventListener implements SensorEventListener{
    private TextView lightOutput, maxValue;
    private float lightSensor, maxLightValue = 0;

    LightSensorEventListener(TextView outputView, TextView maxValue){
        lightOutput = outputView;
        this.maxValue =  maxValue;
    }

    //Tells us accuracy Changes.
    public void onAccuracyChanged(Sensor s, int i){
        //Blank because we don't care about accuracy.
    }

    //Tells us reading changes
    public void onSensorChanged(SensorEvent se){
        if (se.sensor.getType() == Sensor.TYPE_LIGHT) {
            //se.values[0] is x, [1] is y, [2] is z readings
            lightSensor = se.values[0];
            lightOutput.setText("Light sensor current reading: " + Float.toString(lightSensor));
            if (lightSensor > maxLightValue){
                maxLightValue = lightSensor;
                maxValue.setText("Light sensor maximum reading " + Float.toString(maxLightValue));
            }
        }
    }

    public void clearMax(){
        maxLightValue = 0;
        maxValue.setText("Light sensor maximum reading " + Float.toString(maxLightValue));
    }

}

class AccelerometerEventListener implements SensorEventListener{
    private TextView output, max;
    private float[] accelerometer = new float[3];
    private float[] maxValue = new float[3];
    private LinkedList<String> lastMax100 = new LinkedList<String>();
    private LineGraphView graph;

    AccelerometerEventListener(TextView outputView, TextView max, LineGraphView graph){
        output = outputView;
        this.max = max;
        this.graph = graph;
    }

    public void onSensorChanged(SensorEvent se){
        if (se.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            accelerometer[0] = se.values[0];
            accelerometer[1] = se.values[1];
            accelerometer[2] = se.values[2];
            graph.addPoint(se.values);
            //Record values to an array, first out, first out principle.
            lastMax100.add(String.format("%f,%f,%f", accelerometer[0], accelerometer[1], accelerometer[2]));
            if(lastMax100.size() > 100){
                lastMax100.removeFirst();
            }
            output.setText("Accelerometer current reading (x, y, z): (" +  Math.round(accelerometer[0] * 100.0)/100.0 + ", " + Math.round(accelerometer[1] * 100.0)/100.0 + ", " + Math.round(accelerometer[2] * 100.0)/100.0 + ")");
            if (maxValue[0] < accelerometer[0]){
                maxValue[0] = accelerometer[0];
            }
            if (maxValue[1] < accelerometer[1]){
                maxValue[1] = accelerometer[1];
            }
            if (maxValue[2] < accelerometer[2]){
                maxValue[2] = accelerometer[2];
            }
            max.setText("Accelerometer maximum reading (x, y, z): (" +  Math.round(maxValue[0] * 100.0)/100.0 + ", " + Math.round(maxValue[1] * 100.0)/100.0 + ", " + Math.round(maxValue[2] * 100.0)/100.0 + ")");
        }
    }

    public void onAccuracyChanged(Sensor s, int i){

    }

    public void clearMax(){
        maxValue[0] = 0;
        maxValue[1] = 0;
        maxValue[2] = 0;
        max.setText("Accelerometer maximum reading (x, y, z): (" +  Math.round(maxValue[0] * 100.0)/100.0 + ", " + Math.round(maxValue[1] * 100.0)/100.0 + ", " + Math.round(maxValue[2] * 100.0)/100.0 + ")");
    }

    public LinkedList<String> maxArray(){
        return lastMax100;
    }

}

class MagneticFieldEventListener implements SensorEventListener{
    TextView output, max;
    float[] magField = new float[3];
    float[] maxValue = new float[3];

    MagneticFieldEventListener(TextView outputView, TextView maxReading){
        output = outputView;
        max = maxReading;
    }

    public void onSensorChanged(SensorEvent se){
        if (se.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            magField[0] = se.values[0];
            magField[1] = se.values[1];
            magField[2] = se.values[2];
            output.setText("Magnetic Field current reading (x, y, z): (" +  Math.round(magField[0] * 100.0)/100.0 + ", " + Math.round(magField[1] * 100.0)/100.0 + ", " + Math.round(magField[2] * 100.0)/100.0 + ")");
        }
        if (maxValue[0] < magField[0]){
            maxValue[0] = magField[0];
        }
        if (maxValue[1] < magField[1]){
            maxValue[1] = magField[1];
        }
        if (maxValue[2] < magField[2]){
            maxValue[2] = magField[2];
        }
        max.setText("Magnetic Field maximum reading (x, y, z): (" +  Math.round(maxValue[0] * 100.0)/100.0 + ", " + Math.round(maxValue[1] * 100.0)/100.0 + ", " + Math.round(maxValue[2] * 100.0)/100.0 + ")");


    }

    public void onAccuracyChanged(Sensor s, int i){

    }

    public void clearMax(){
        maxValue[0] = 0;
        maxValue[1] = 0;
        maxValue[2] = 0;
        max.setText("Magnetic Field maximum reading (x, y, z): (" +  Math.round(maxValue[0] * 100.0)/100.0 + ", " + Math.round(maxValue[1] * 100.0)/100.0 + ", " + Math.round(maxValue[2] * 100.0)/100.0 + ")");
    }
}

class RotationVectorEventListener implements SensorEventListener{
    TextView output, max;
    float[] rotVector = new float[3];
    float[] maxValue = new float[3];

    RotationVectorEventListener(TextView outputView, TextView max){
        output = outputView;
        this.max = max;
    }

    public void onSensorChanged(SensorEvent se){
        if (se.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
            rotVector[0] = se.values[0];
            rotVector[1] = se.values[1];
            rotVector[2] = se.values[2];
            output.setText("Rotational vector current reading (x, y, z): (" + Math.round(rotVector[0] * 100.0)/100.0 + ", " + Math.round(rotVector[1] * 100.0)/100.0 + ", " + Math.round(rotVector[2] * 100.0)/100.0 + ")");
        }
        if (maxValue[0] < rotVector[0]){
            maxValue[0] = rotVector[0];
        }
        if (maxValue[1] < rotVector[1]){
            maxValue[1] = rotVector[1];
        }
        if (maxValue[2] < rotVector[2]){
            maxValue[2] = rotVector[2];
        }
        max.setText("Rotational Vector maximum reading (x, y, z): (" +  Math.round(maxValue[0] * 100.0)/100.0 + ", " + Math.round(maxValue[1] * 100.0)/100.0 + ", " + Math.round(maxValue[2] * 100.0)/100.0 + ")");

    }

    public void onAccuracyChanged(Sensor s, int i){

    }

    public void clearMax(){
        maxValue[0] = 0;
        maxValue[1] = 0;
        maxValue[2] = 0;
        max.setText("Rotational Vector maximum reading (x, y, z): (" +  Math.round(maxValue[0] * 100.0)/100.0 + ", " + Math.round(maxValue[1] * 100.0)/100.0 + ", " + Math.round(maxValue[2] * 100.0)/100.0 + ")");
    }
}