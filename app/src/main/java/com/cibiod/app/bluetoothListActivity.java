package com.cibiod.app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.audiofx.Equalizer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Window;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class bluetoothListActivity extends AppCompatActivity implements recyclerClickInterface{
    private BluetoothAdapter bluetoothAdapter;
    private RecyclerView rv;
    private ArrayList<btDevice> btDevices = new ArrayList<btDevice>();
    private BluetoothSocket btSocket;
    private SharedPreferences prefs;
    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private AudioTrack at;
    private boolean audioLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
        Objects.requireNonNull(getSupportActionBar()).hide(); //hide the title bar

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_list);

        rv = findViewById(R.id.btListView);
        int minBufferSize = AudioTrack.getMinBufferSize(44100,
                AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);

        minBufferSize *= 2;

        at = new AudioTrack(AudioManager.USE_DEFAULT_STREAM_TYPE, 44100,
            AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,
            minBufferSize, AudioTrack.MODE_STREAM);

        attachEq(at.getAudioSessionId());

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            print("Bluetooth Required");
            finish();
        }

        else if(!bluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 11);
        }

        else
        {
            prefs = getSharedPreferences("applicationVariables", Context.MODE_PRIVATE);
//            searchDevices();
            //                openServer();
            File file = new File(Environment.getExternalStorageDirectory()+ "/cibiodLogs/audio1.wav");
            at.play();
            playAudio(file);
            //            searchBondedDevices();
        }

    }

    private void attachEq(int audioSessionId) {
        Equalizer eq = new Equalizer(100,audioSessionId);
        short[] freqRange = eq.getBandLevelRange();
        short minLvl = freqRange[0];
        short maxLvl = freqRange[1];

        eq.setBandLevel((short) 4,minLvl);
        eq.setBandLevel((short) 3,minLvl);

        eq.setEnabled(true);
    }

    private void openServer() {
        print("starting server");
        BluetoothServerSocket tmp = null;

        try {
            tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("CIBIOD app", uuid);
            print("Listening to device");
        } catch (IOException e) {
            print("Socket's listen() method failed " + e);
        }

        final BluetoothServerSocket finalTmp = tmp;
        new Thread(new Runnable() {
            @Override
            public void run() {
                BluetoothServerSocket btServerSocket = finalTmp;

                BluetoothSocket socket;

                while (true) {
                    try {
                        assert btServerSocket != null;
                        socket = btServerSocket.accept();
                        Log.d("customm","found socket");
                    } catch (IOException e) {
                        Log.d("customm","Socket's accept() method failed " + e);
                        break;
                    }

                    if (socket != null) {
                        doInAndOut(socket);
                        try {
                            btServerSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }
        }).start();
    }

    private void doInAndOut(final BluetoothSocket socket) {
        Log.d("customm","data exchange");

        new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream inStream = null;
                try{
                    inStream = socket.getInputStream();
                } catch (IOException e) {
                    print("input stream creation failed");
                }

                byte[] inData = new byte[1024];
                File file = new File(Environment.getExternalStorageDirectory()+ "/cibiodLogs/audio1.wav");

                try (OutputStream output = new FileOutputStream(file)) {
                    int read;
                    at.play();
                    assert inStream != null;
                    while ((read = inStream.read(inData)) != -1) {
                        output.write(inData, 0, read);
                        if(file.length()/1024>500 && !audioLoaded)
                            playAudio(file);
                        Log.d("customm",String.valueOf(file.length()/1024));
                    }
                    Log.d("customm","yeet");
                    at.stop();
                    at.release();
                    output.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

//        OutputStream outStream = null;
//        try {
//            outStream = socket.getOutputStream();
//        } catch (IOException e) {
//            makeToast("output stream creation failed:" + e.getMessage() + ".");
//        }
//
//        String message = "Hello from Android.\n";
//        byte[] msgBuffer = message.getBytes();
//        try {
//            assert outStream != null;
//            outStream.write(msgBuffer);
//        } catch (IOException e) {
//            makeToast("output stream disconnected");
//        }
    }

    private InputStream inStreamAudio = null;
    private byte[] inDataAudio = new byte[1024];

    private void playAudio(final File file) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true)
                {
                    int readAudio;
                    if(!audioLoaded)
                    {
                        audioLoaded = true;
                        try {
                            inStreamAudio = new FileInputStream(file);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        if((readAudio = inStreamAudio.read(inDataAudio)) != -1)
                        {
                            at.write(inDataAudio,0, readAudio);
                        }
                        else
                            break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();
    }

//    private void searchBondedDevices()
//    {
//        Set<BluetoothDevice> paired = bluetoothAdapter.getBondedDevices();
//        if (paired.size() > 0)
//        {
//            for (BluetoothDevice device : paired) {
//                btDevices.add(new btDevice(device.getName(),device.getAddress()));
//            }
//            createList();
//        }
//    }

//    private void searchDevices()
//    {
//        bluetoothAdapter.startDiscovery();
//        show("searching");
//        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//        registerReceiver(receiver, filter);
//    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                btDevices.add(new btDevice(device.getName(),device.getAddress()));
                createList();
            }
        }
    };

    private void createList()
    {
        btDeviceAdapter adapter = new btDeviceAdapter(btDevices, this);

        rv.setAdapter(adapter);

        rv.setLayoutManager(new LinearLayoutManager(bluetoothListActivity.this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();
        unregisterReceiver(receiver);
    }

    private void print(String s)
    {
//        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
        Log.d("customm",s);
    }

    private void print(int i)
    {
//        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
        Log.d("customm",String.valueOf(i));
    }

    @Override
    public void OnItemClick(int position) {
        if(bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("pairedStethoName",btDevices.get(position).getName());
        editor.putString("pairedStethoAddress",btDevices.get(position).getAddress());
        editor.apply();

        BluetoothDevice toConnectBtDevice = bluetoothAdapter.getRemoteDevice(prefs.getString("pairedStethoAddress", "NA"));

        try {
            btSocket = toConnectBtDevice.createRfcommSocketToServiceRecord(uuid);
            print("socket successful");
        } catch (IOException e) {
            print("socket create failed: " + e.getMessage() + ".");
        }

        try {
            btSocket.connect();
            print("connection successful");
        } catch (IOException connectException) {
            print("connection failed");
            try {
                btSocket.close();
            } catch (IOException closeException) {
                Log.e("customm", "Could not close the client socket", closeException);
            }
        }

        doInAndOut(btSocket);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==11)
        {
            if(resultCode==RESULT_CANCELED)
            {
                print("Bluetooth Required");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 11);
            }
            else if(resultCode==RESULT_OK) {
//                searchDevices();
                openServer();
            }
        }
    }
}