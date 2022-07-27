package br.com.andruinobluetooth;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;


public class DeviceListActivity extends ActionBarActivity {
    /* Debugging para LOGCAT */
    private static String TAG = "LOG DeviceListActivity";
    private static final boolean D = true;

    /* Send address to AndruinoActivity*/
    public static String EXTRA_DEVICE_ADDRESS = "device_addres";
    private AlertDialog.Builder alerta;
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private Toolbar mToolbar;
    private Toolbar mToolbarBotton;

    /**
     * Set up on-click listener for the list (nicked this - unsure)
     */
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        /**
         * onItemClick
         * @param av
         * @param v
         * @param arg2
         * @param arg3
         */
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {


            // Gets the MAC address of the device, which you algorithm in 17 characters in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Make the intent start the next activity, passing the MAC address through the extra.
            Intent i = new Intent(DeviceListActivity.this, AndruinoActivity.class);
            i.putExtra(EXTRA_DEVICE_ADDRESS, address);
            startActivity(i);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        mToolbar = (Toolbar) findViewById(R.id.tb_main);
        mToolbar.setTitle("Control");
        mToolbar.setSubtitle("Select your device");
        mToolbar.setLogo(R.drawable.robotic_arm_ico);
        setSupportActionBar(mToolbar);
    }


    /**
     * onResume
     */
    @Override
    public void onResume() {
        super.onResume();

        checkBTState();

        // Initialize array adapter for paired devices
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.activity_device_name);

        // Find and set up the ListView for paired devices
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // Get a set of currently paired devices and append to 'pairedDevices'
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // Add previosuly paired devices to the array
        if (pairedDevices.size() > 0) {
            //findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE); // make title viewable

            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }

        } else {
            String noDevices = "No paired device".toString();
            mPairedDevicesArrayAdapter.add(noDevices);
        }
    }


    /**
     * checkBTState - Verifica Status de conectividade bluetooth no dispositivo.
     */
    private void checkBTState() {
        /* Checks if the device has Bluetooth, is turned on or is being used by another app.. */
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBtAdapter == null) { /* Checks the existence of bluetooth technology on the device.. */
            Toast.makeText(getBaseContext(), "Device does not support bluetooth!", Toast.LENGTH_SHORT).show();
        } else {
            if (mBtAdapter.isEnabled()) { // Check if bluetooth is active..
                Toast.makeText(getBaseContext(), "Active bluetooth!", Toast.LENGTH_SHORT).show();
            } else { /* If not activated, prompts user to activate.. */
                /* Prompt user to turn on Bluetooth */
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }


}
