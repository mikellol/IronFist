package br.com.andruinobluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class AndruinoActivity extends ActionBarActivity {
    /* SPP UUID service - Esto debería funcionar para la mayoría de los dispositivos. */
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String address; /* Cadena para la dirección MAC */
    final int handlerState = 0; /* Se utiliza para identificar el controlador de mensajes */
    Handler bluetoothIn;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();
    private ConnectedThread mConnectedThread;
    private Vibrator vibrator;
    private Toolbar mToolbar, mToolbarBotton;
    private SeekBar seekBarBase, seekBarG, seekBarB;
    private TextView textViewR, textViewG, textViewB;
    private boolean statusLampada = false;
    private int progressR =90, progressG = 90, progressB = 90;
    private int vibBar = 10, vibClick = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_andruino);

        mToolbar = (Toolbar) findViewById(R.id.tb_main);
        mToolbar.setTitle(" Control");
        mToolbar.setSubtitle(" Robotic Arm");
        mToolbar.setLogo(R.drawable.robotic_arm_ico);
        setSupportActionBar(mToolbar);

        // Button creation and mapping
        Button button = (Button) findViewById(R.id.buttonAbrir);

        // Listener that will listen for button events
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mConnectedThread.write("Open \n");
            }
        });

        // Button creation and mapping
        Button button2 = (Button) findViewById(R.id.buttonFechar);

        // Listener that will listen for button events
        button2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mConnectedThread.write("Close \n");
            }
        });

        seekBarBase = (SeekBar) findViewById(R.id.seekBarBase);
        seekBarG = (SeekBar) findViewById(R.id.seekBarAlcance);
        seekBarB = (SeekBar) findViewById(R.id.seekBarB);

        textViewR = (TextView) findViewById(R.id.textViewR);
        textViewG = (TextView) findViewById(R.id.textViewG);
        textViewB = (TextView) findViewById(R.id.textViewB);

        // Initialize textview's to '0'.
        textViewR.setText("Basis " + seekBarBase.getProgress() + "/" + seekBarBase.getMax());
        textViewG.setText("Range " + seekBarG.getProgress() + "/" + seekBarG.getMax());
        textViewB.setText("Height " + seekBarB.getProgress() + "/" + seekBarB.getMax());

        /**
         * seekBarBase -
         */
        seekBarBase.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBarBase, int progresValueR, boolean fromUserR) {
                progressR = progresValueR;
                textViewR.setText("Basis " + progressR + "/" + seekBarBase.getMax());
                mConnectedThread.write("servo1 " + progressR + "\n");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBarBase) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBarBase) {
                mConnectedThread.write("servo1 " + progressR + "\n");
            }
        });

        /**
         *  seekBarG
         */
        seekBarG.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBarG, int progresValueG, boolean fromUserG) {
                progressG = progresValueG;
                textViewG.setText("Range " + progressG + "/" + seekBarG.getMax());
                mConnectedThread.write("servo2 " + progressG + "\n");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBarG) {
                //Toast.makeText(getApplicationContext(), "Started tracking seekbar", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBarG) {
                mConnectedThread.write("servo2 " + progressG + "\n");
            }
        });

        /**
         * seekBarB -
         */
        seekBarB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBarB, int progresValueB, boolean fromUserB) {
                progressB = progresValueB;
                textViewB.setText("Height " + progressB + "/" + seekBarB.getMax());
                mConnectedThread.write("servo3 " + progressB + "\n");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBarB) {
                //Toast.makeText(getApplicationContext(), "Started tracking seekbar", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBarB) {
                mConnectedThread.write("servo3 " + progressB + "\n");
            }
        });

        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {
                    String readMessage = (String) msg.obj; // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage); // keep attaching the rope until ~
                    int endOfLineIndex = recDataString.indexOf("~"); // determine end-of-line

                    if (endOfLineIndex > 0) { //Make sure the data before ~
                        // extract string
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);
                        // get length of data received
                        int dataLength = dataInPrint.length();

                        // if it starts with # we know it's what we're looking for
                        if (recDataString.charAt(0) == '#') {
                        }

                        //clear all string data
                        recDataString.delete(0, recDataString.length());
                        // strIncom =" ";
                        dataInPrint = " ";
                    }
                }
            }
        };

        // get Bluetooth adapter
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        checkBTState();

    }

    /**
     * createBluetoothSocket
     *
     * @param device
     * @return
     * @throws IOException
     */
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        // Create secure outgoing connection with BT device using UUID
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }


    /**
     * onResume
     */
    @Override
    public void onResume() {
        super.onResume();

        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();

        //Get the MAC address from the DeviceListActivty via EXTRA
        address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        //create device and set the MAC address
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Socket creation error!", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                //insert code to deal with this
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("x");
    }


    /**
     * onPause
     */
    @Override
    public void onPause() {
        super.onPause();
        try {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }


    /**
     * checkBTState - Checks that the Android device Bluetooth is available and prompts to be turned on if off
     */
    private void checkBTState() {
        if (btAdapter == null) { // Check if the device has bluetooth.
            Toast.makeText(getBaseContext(), "Device does not have bluetooth technology!", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }


    /**
     * ConnectedThread - create new class for connect thread
     */
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        /**
         * ConnectedThread - creation of the connect thread
         *
         * @param socket
         */
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {

            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        /**
         * run
         */
        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer); //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);

                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /**
         * write
         *
         * @param input
         */
        public void write(String input) {
            // converts entered String into bytes
            byte[] msgBuffer = input.getBytes();
            try {
                // write bytes over BT connection via outstream
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
                // if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Connection error!", Toast.LENGTH_LONG).show();
                finish();
            }
        }

    } // FIM ConnectedThread

} // Fim class Andruino.
