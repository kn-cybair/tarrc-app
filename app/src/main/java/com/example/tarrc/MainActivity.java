/*
  MIT License 2019
  ---
  TARRC Android application
  version: 0.2
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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    /* Elements of UI */
    Button searchButton;
    ListView deviceList;

    /* Bluetooth components */
    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;

    /* Unique extra data identifier for new activity */
    static final String EXTRA_ADDRESS = "com.example.tarrc.extra_address";

    /*  */
    static final int BLUETOOTH_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Get all elements from the View linked to the Controller */
        searchButton = findViewById(R.id.searchButton);
        deviceList = findViewById(R.id.deviceList);

        /* Connect to the default BT adapter on the device*/
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        /* If no BT adapter found, terminate the app */
        if(bluetoothAdapter == null){
            Toast.makeText(getApplicationContext(),"Bluetooth Device Not Available", Toast.LENGTH_SHORT).show();
            finish();
        }
        else{
            if(bluetoothAdapter.isEnabled()){
                Toast.makeText(getApplicationContext(),"Turned On", Toast.LENGTH_SHORT).show();
            }
            else{
                Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnBTon,1);
            }
        }

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pairedDevicesList();
            }
        });
    }

    private void pairedDevicesList() {
        pairedDevices = bluetoothAdapter.getBondedDevices();
        ArrayList list = new ArrayList();

        if(pairedDevices.size()>0){
            for (BluetoothDevice bt : pairedDevices){
                list.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,list);
        deviceList.setAdapter(adapter);
        deviceList.setOnItemClickListener(myListClickListener);
    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // get mac address
            String info = ((TextView)view).getText().toString();
            String address = info.substring(info.length()-17);
            Intent i = new Intent(MainActivity.this, CarControl.class);
            i.putExtra(EXTRA_ADDRESS, address);
            startActivity(i);
        }
    };

}
