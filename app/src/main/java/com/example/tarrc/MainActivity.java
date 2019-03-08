/*
  MIT License 2019
  ---
  TARRC Android application
  version: 0.1
  Purpose: Control robot and present collected data.
  ---
  @author: Krzysztof Stezala
  ---
  Provided by CybAiR Science Club at
  Institute of Control, Robotics and Information Engineering of
  Poznan University of Technology
*/

package com.example.tarrc;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    /* Elements of UI */
    TextView deviceName;
    CheckBox enableButton, visibleButton;
    Button searchButton, connectButton;
    ListView devicesList;

    /* Bluetooth components */
    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;

    static final int BLUETOOTH_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Get all elements from the View linked to the Controller */
        deviceName = findViewById(R.id.deviceName);
        enableButton = findViewById(R.id.enableBtn);
        visibleButton = findViewById(R.id.visibleBtn);
        searchButton = findViewById(R.id.searchBtn);
        devicesList = findViewById(R.id.devicesLIst);
        connectButton = findViewById(R.id.connectBtn);

        /* Update the user's device name in the main view */
        deviceName.setText(getLocalBluetoothName());

        /* Connect to the default BT adapter on the device*/
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        /* If no BT adapter found, terminate the app */
        if(bluetoothAdapter==null){
            Toast.makeText(this,"Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        /* If Bt was enabled before, mark checkbox as true*/
        if(bluetoothAdapter.isEnabled()){
            enableButton.setChecked(true);
        }

        enableButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked){
                    bluetoothAdapter.disable();
                    Toast.makeText(MainActivity.this, "Turned off", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intentOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intentOn,BLUETOOTH_REQUEST_CODE);

                }

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    // Check which request we're responding to
        if (requestCode == BLUETOOTH_REQUEST_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Toast.makeText(MainActivity.this, "Turned on", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Operation aborted", Toast.LENGTH_SHORT).show();
                enableButton.setChecked(false);
            }

        }
    }

    private String getLocalBluetoothName() {
        if(bluetoothAdapter==null){
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        String name = bluetoothAdapter.getName();
        if(name==null){
            name = bluetoothAdapter.getAddress();
        }
        return name;
    }
}
