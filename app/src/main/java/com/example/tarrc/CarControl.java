/*
  MIT License 2019
  ---
  TARRC Android application
  version: 0.3
  Purpose: TARRC is an educational project created by CybAiR to teach students about basics of PCB
           board design, 3D modeling, Android and Arduino programming.
  File: CarControl.java
  ---
  @author: Krzysztof Stezala
  ---
  Provided by CybAiR Science Club at
  Institute of Control, Robotics and Information Engineering of
  Poznan University of Technology
*/

package com.example.tarrc;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class CarControl extends AppCompatActivity {

    private boolean isRobotOn = false;
    private boolean isAutonomous = false;

    Button driveForward, driveBackward, driveRight, driveLeft, switchMode, emergencyStop, onButton, sensorButton;
    TextView currentStatus;
    String address = null;
    private ProgressDialog progressDialog;
    BluetoothAdapter bluetoothAdapter = null;
    BluetoothSocket bluetoothSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent newIntent = getIntent();
        address = newIntent.getStringExtra(MainActivity.EXTRA_ADDRESS);
        setContentView(R.layout.activity_car_control);

        onButton = (Button) findViewById(R.id.onButton);
        emergencyStop = (Button) findViewById(R.id.emergencyStop);
        switchMode = (Button) findViewById(R.id.switchMode);
        sensorButton = (Button) findViewById(R.id.sensorButton);
        driveBackward = (Button) findViewById(R.id.driveBackward);
        driveForward = (Button) findViewById(R.id.driveForward);
        driveLeft = (Button) findViewById(R.id.driveLeft);
        driveRight = (Button) findViewById(R.id.driveRight);

        currentStatus = (TextView) findViewById(R.id.currentStatus);
        currentStatus.setText("booting...");

        // Call the class to connect
        new ConnectBT().execute();

        // set event listeners
        onButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // turn on robot or turn off robot
                changeRobotState();
                // change state of boolean variable
            }
        });

        emergencyStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // cut power from motors
                cutPower();
            }
        });

        switchMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // change mode from autonomous to manual and the other way around
                changeMode();
            }
        });

        sensorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnectDevice();
            }
        });


        driveBackward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        moveBackward();

                        break;
                    case MotionEvent.ACTION_UP:
                        //terminate transmission
                        doNothing();

                        v.performClick();
                        break;
                }
                return true;
            }
        });

        driveForward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        moveForward();
                        break;
                    case MotionEvent.ACTION_UP:
                        //terminate transmission
                        doNothing();
                        v.performClick();
                        break;
                }
                return true;
            }
        });

        driveLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        turnLeft();
                        break;
                    case MotionEvent.ACTION_UP:
                        //terminate transmission
                        doNothing();
                        v.performClick();
                        break;
                }
                return true;
            }
        });

        driveRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        turnRight();
                        break;
                    case MotionEvent.ACTION_UP:
                        //terminate transmission
                        doNothing();
                        v.performClick();
                        break;
                }
                return true;
            }
        });


    }

    private void changeRobotState() {
        isRobotOn = !isRobotOn;
        if (isRobotOn) {
            Toast.makeText(this, "ON", Toast.LENGTH_SHORT).show();
            onButton.setText("ON");
            driveBackward.setEnabled(true);
            driveForward.setEnabled(true);
            driveLeft.setEnabled(true);
            driveRight.setEnabled(true);
            if(bluetoothSocket!=null){
                try{
                    bluetoothSocket.getOutputStream().write("1".toString().getBytes());
                }
                catch (IOException e){
                    Toast.makeText(CarControl.this,"MF #08", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "OFF", Toast.LENGTH_SHORT).show();
            onButton.setText("OFF");
            driveBackward.setEnabled(false);
            driveForward.setEnabled(false);
            driveLeft.setEnabled(false);
            driveRight.setEnabled(false);
            if(bluetoothSocket!=null){
                try{
                    bluetoothSocket.getOutputStream().write("2".toString().getBytes());
                }
                catch (IOException e){
                    Toast.makeText(CarControl.this,"MF #09", Toast.LENGTH_SHORT).show();
                }
            }
        }
        doNothing();
    }

    private void cutPower() {
        if(bluetoothSocket!=null){
            try{
                bluetoothSocket.getOutputStream().write("C".toString().getBytes());
            }
            catch (IOException e){
                Toast.makeText(CarControl.this,"MF #07", Toast.LENGTH_SHORT).show();
            }
        }
        doNothing();

    }

    private void changeMode() {
        isAutonomous = !isAutonomous;
        if (isAutonomous) {
            Toast.makeText(this, "Autonomous mode", Toast.LENGTH_SHORT).show();
            driveBackward.setEnabled(false);
            driveForward.setEnabled(false);
            driveLeft.setEnabled(false);
            driveRight.setEnabled(false);
            if(bluetoothSocket!=null){
                try{
                    bluetoothSocket.getOutputStream().write("A".toString().getBytes());
                }
                catch (IOException e){
                    Toast.makeText(CarControl.this,"MF #05", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "Manual mode", Toast.LENGTH_SHORT).show();
            driveBackward.setEnabled(true);
            driveForward.setEnabled(true);
            driveLeft.setEnabled(true);
            driveRight.setEnabled(true);
            if(bluetoothSocket!=null){
                try{
                    bluetoothSocket.getOutputStream().write("M".toString().getBytes());
                }
                catch (IOException e){
                    Toast.makeText(CarControl.this,"MF #06", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    private void disconnectDevice(){
        if(bluetoothSocket!=null){
            try{
                bluetoothSocket.close();
                Toast.makeText(CarControl.this, "Connection closed",Toast.LENGTH_SHORT).show();
            }
            catch (IOException e){
                Toast.makeText(CarControl.this, "Error LOL",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void turnRight() {
        if(bluetoothSocket!=null){
            try{
                currentStatus.setText("right");
                bluetoothSocket.getOutputStream().write("R".toString().getBytes());
            }
            catch (IOException e){
                Toast.makeText(CarControl.this,"MF #04", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void turnLeft() {
        if(bluetoothSocket!=null){
            try{
                currentStatus.setText("left");
                bluetoothSocket.getOutputStream().write("L".toString().getBytes());
            }
            catch (IOException e){
                Toast.makeText(CarControl.this,"MF #03", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void moveForward() {
        if(bluetoothSocket!=null){
            try{
                currentStatus.setText("forward");
                bluetoothSocket.getOutputStream().write("F".toString().getBytes());
            }
            catch (IOException e){
                Toast.makeText(CarControl.this,"MF #02", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void moveBackward() {
        if(bluetoothSocket!=null){
            try{
                currentStatus.setText("backward");
                bluetoothSocket.getOutputStream().write("B".toString().getBytes());
            }
            catch (IOException e){
                Toast.makeText(CarControl.this,"MF #01", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void doNothing() {
        if(bluetoothSocket!=null){
            try{
                currentStatus.setText("nothing");
                bluetoothSocket.getOutputStream().write("N".toString().getBytes());
            }
            catch (IOException e){
                Toast.makeText(CarControl.this,"MF #00", Toast.LENGTH_SHORT).show();
            }
        }
    }




    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean connectSuccess = true;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(CarControl.this, "Connecting...", "Please wait.");
        }

        @Override
        protected Void doInBackground(Void... devices){
            try{
                if(bluetoothSocket == null || !isBtConnected){
                    // get the mobile bt device
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    // connect to the devices's address and checks if it's available
                    BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(address);
                    // create a RFCOMM (SPP) connection
                    bluetoothSocket = remoteDevice.createInsecureRfcommSocketToServiceRecord(myUUID);
                    // cancel discovery process
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    // start connection to the remote device
                    bluetoothSocket.connect();
                }
            }
            catch(IOException e){
                // if connection failed, exception is catched
                connectSuccess = false;
            }
            return null;
        }

        // after doInBackground, it checks if everything went fine
        @Override
        protected void onPostExecute(Void result){
            super.onPostExecute(result);

            if(!connectSuccess){
                Toast.makeText(CarControl.this,"Connection failed :(", Toast.LENGTH_SHORT).show();
                finish();
            }
            else{
                Toast.makeText(CarControl.this,"Connected :)", Toast.LENGTH_SHORT).show();
                isBtConnected = true;
            }
            progressDialog.dismiss();
        }
    }

}
