package com.example.stepappv3.ui.home;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.stepappv3.R;
import com.example.stepappv3.databinding.FragmentHomeBinding;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private TextView stepCountsView;
    private CircularProgressIndicator progressBar;

    private MaterialButtonToggleGroup toggleButtonGroup;

    // TODO 1: Create an object from Sensor class, to be used for Acc. sensor
    private Sensor accSensor;

    // TODO 2: Create an object from SensorManager class
    private SensorManager sensorManager;

    private StepCounterListener sensorListener;

    // TODO 17.1 (YOUR TURN): Create an object from Sensor class,  to be used for STEP_DETECTOR sensor
    private Sensor stepDetectorSensor;

    public Integer progressMultiplier = 100000;
    public Button buttonStart;
    public Button buttonStop;
    public int stepDetectorCounter = 0;
    public int accStepCounter = 0;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        stepCountsView = (TextView) root.findViewById(R.id.counter);
        stepCountsView.setText("0");

        progressBar = (CircularProgressIndicator) root.findViewById(R.id.progressBar);
        progressBar.setMax(50*progressMultiplier);
        progressBar.setProgress(0);

        // TODO 3: Get an instance of sensor manager
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        // TODO 4: Assign sensor object to ACC. sensor
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        // TODO 17.2 (YOUR TURN): Assign sensor object to STEP_DETECTOR sensor
//        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);


        buttonStop = (Button) root.findViewById(R.id.stop_button);
        buttonStart = (Button) root.findViewById(R.id.start_button);
        buttonStart.setEnabled(true);
        buttonStop.setEnabled(false);

        buttonStart.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // TODO 6: Check if ACC. sensor exists and register the sensor event listener to it when use press start button
                if (accSensor != null)
                {
                    // TODO 15: Pass the TextView variable from the HomeFragment class to the StepCounterListener class
                    sensorListener = new StepCounterListener(stepCountsView,progressBar);
                    sensorManager.registerListener(sensorListener, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
                }
                else
                {
                    Toast.makeText(getContext(), R.string.acc_sensor_not_available, Toast.LENGTH_LONG).show();
                }
                buttonStart.setEnabled(false);
                buttonStop.setEnabled(true);
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sensorManager.unregisterListener(sensorListener);
                buttonStart.setEnabled(true);
                buttonStop.setEnabled(false);
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public class ProgressBarAnimation extends Animation {
        private CircularProgressIndicator progressBar;
        private float from;
        private float to;



        public ProgressBarAnimation(CircularProgressIndicator progressBar, float from, float to) {
            super();
            this.progressBar = progressBar;
            this.from = from;
            this.to = to;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            float value = from + (to - from) * interpolatedTime;
            progressBar.setProgress((int) value);
        }

        public void animateProgressBarStep(Integer step){
            ProgressBarAnimation anim = new ProgressBarAnimation(progressBar, step*progressMultiplier, 0);
            stepCountsView.setText(Integer.toString(step));
            anim.setDuration(200);
            progressBar.startAnimation(anim);
        }
    }


    // TODO 5: Create a class that implements SensorEventListener interface
    class  StepCounterListener implements SensorEventListener{

        private long lastSensorUpdate = 0;
        ArrayList<Integer> accSeries = new ArrayList<Integer>();
        private double accMag = 0;
        private int lastAddedIndex = 1;
        int stepThreshold = 6;

        public Integer progressMultiplier = 100000;

        //TODO 13: Declare the TextView in the listener class
        TextView stepCountsView;

        //TODO 18.1: Declare a counter for STEP_DETECTOR
        public CircularProgressIndicator progressBar;


        //TODO 14: Pass the TextView to the listener class using the constructor
        public StepCounterListener(TextView stepCountsView, CircularProgressIndicator progressBar)
        {
            this.stepCountsView = stepCountsView;
            this.progressBar = progressBar;
        }


        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            // TODO 8: Check the type of the sensor, this is helpful in case of multiple sensors (you will need for the next assignment)
            switch (sensorEvent.sensor.getType())
            {
                case Sensor.TYPE_LINEAR_ACCELERATION:

                    // TODO 9: Get the raw acc. sensor data
                    float x = sensorEvent.values[0];
                    float y = sensorEvent.values[1];
                    float z = sensorEvent.values[2];

                    // TODO 10: Log the raw acc. sensor data and the event timestamp

                    long currentTimeInMilliSecond = System.currentTimeMillis();

                    long timeUntilSensorEvent = (sensorEvent.timestamp - SystemClock.elapsedRealtimeNanos())/1000000;

                    long SensorEventTimestampInMilliSecond =  currentTimeInMilliSecond + timeUntilSensorEvent;

                    SimpleDateFormat sensorEventTimestamp = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss");
                    sensorEventTimestamp.setTimeZone(TimeZone.getTimeZone("GMT+2"));

                    String sensorEventDate = sensorEventTimestamp.format(SensorEventTimestampInMilliSecond);


                    if ((currentTimeInMilliSecond - lastSensorUpdate) > 1000)
                    {
                        lastSensorUpdate = currentTimeInMilliSecond;
                        String sensorRawValues = "  x = "+ String.valueOf(x) +"  y = "+ String.valueOf(y) +"  z = "+ String.valueOf(z);
                        Log.d("Acc. Event", "last sensor update at " + String.valueOf(sensorEventDate) + sensorRawValues);
                    }

                    // TODO 11 (YOUR TURN): Compute the magnitude for the acceleration

                    accMag = Math.sqrt(x*x+y*y+z*z);

                    // TODO 12 (YOUR TURN): Store the magnitude for the acceleration in accSeries

                    accSeries.add((int) accMag);


                    peakDetection();

//                break;

                    // TODO 18.3 (YOUR TURN): Add new case for STEP_DETECTOR sensor
                case Sensor.TYPE_STEP_DETECTOR:
                    // TODO 18.4 (YOUR TURN): Call countSteps() function to count the number of steps using STEP_DETECTOR sensor
                    countSteps(sensorEvent.values[0]);

//                break;
            }


        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }

        private void peakDetection() {

            int windowSize = 20;
            /* Peak detection algorithm derived from: A Step Counter Service for Java-Enabled Devices Using a Built-In Accelerometer Mladenov et al.
             */
            int currentSize = accSeries.size(); // get the length of the series
            if (currentSize - lastAddedIndex < windowSize) { // if the segment is smaller than the processing window size skip it
                return;
            }

            List<Integer> valuesInWindow = accSeries.subList(lastAddedIndex,currentSize);
            lastAddedIndex = currentSize;

            for (int i = 1; i < valuesInWindow.size()-1; i++) {
                int forwardSlope = valuesInWindow.get(i + 1) - valuesInWindow.get(i);
                int downwardSlope = valuesInWindow.get(i) - valuesInWindow.get(i - 1);

                if (forwardSlope < 0 && downwardSlope > 0 && valuesInWindow.get(i) > stepThreshold) {
                    accStepCounter += 1;
                    Log.d("ACC STEPS: ", String.valueOf(accStepCounter));

                    //TODO 16: Update the TextView with the number of steps calculated using ACC. sensor
//                    stepCountsView.setText(String.valueOf(accStepCounter));
                    stepCountsView.setText(Integer.toString(accStepCounter));
                    ProgressBarAnimation anim = new ProgressBarAnimation((CircularProgressIndicator) progressBar, (accStepCounter-1)*progressMultiplier, accStepCounter*progressMultiplier);
                    anim.setDuration(300);
                    progressBar.startAnimation(anim);
                }
            }
        }

        // TODO 18.2 (YOUR TURN): Implement countSteps() function to count the number of steps from the STEP_DETECTOR events and print them in the Logcat
        private void countSteps(float step)
        {
            stepDetectorCounter += step;

//        Log.d("STEP_DETECTOR STEPS: ", String.valueOf(stepDetectorCounter));
        }
    }
}

