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
import android.util.Log;
import android.view.Window;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
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
    private static UUID uuid = UUID.fromString("00001102-0000-1000-8000-00805F9B34FB");
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
            openServer();
//            playAudio(file);
            //            searchBondedDevices();
        }

    }

    private void attachEq(int audioSessionId) {
        Equalizer eq = new Equalizer(100,audioSessionId);
        short[] freqRange = eq.getBandLevelRange();
        short minLvl = freqRange[0];

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
                        sendData(socket,"hello");
                        receiveData(socket);
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

    private void receiveData(final BluetoothSocket socket) {
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
                while(true)
                {
                    try {
                        inStream.read(inData);
                        String data = convertByteToString(inData);
                        decodeDcip(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

//                File file = new File(Environment.getExternalStorageDirectory()+ "/cibiodLogs/audio1.wav");
//
//                try (OutputStream output = new FileOutputStream(file)) {
//                    int read;
//                    assert inStream != null;
//                    while ((read = inStream.read(inData)) != -1) {
//                        output.write(inData, 0, read);
//                        if(file.length()/1024>500 && !audioLoaded)
//                            playAudio(file);
//                    }
//                    at.stop();
//                    at.release();
//                    output.flush();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
        }).start();
    }

    private boolean decodeDcip(String data) {
        if(data.contains("02C0013D"))
        {
            print("recieved hi from device");
            return true;
        }
        return false;
    }

    final protected static char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
    private String convertByteToString(byte[] bytes) {
        char[] hexChars = new char[bytes.length*2];
        int v;

        for(int j=0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j*2] = hexArray[v>>>4];
            hexChars[j*2 + 1] = hexArray[v & 0x0F];
        }

        return new String(hexChars);
    }

    private void sendData(BluetoothSocket socket, String s) {
        print("trying to send data");
        OutputStream outStream = null;
        try {
            outStream = socket.getOutputStream();
        } catch (IOException e) {
            print("output stream creation failed:" + e.getMessage() + ".");
        }
        byte[] message;
        switch(s)
        {
            case "hello":
                message = hexStringToByte("0280017D");
                break;

            default:
                message = hexStringToByte("FF");
        }

        try {
            assert outStream != null;
            outStream.write(message);
            print("data is sent");
        } catch (IOException e) {
            print("output stream disconnected");
        }
    }

    private byte[] hexStringToByte(String s) {
        s = s.length()%2 != 0?"0"+s:s;

        byte[] b = new byte[s.length() / 2];

        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(s.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
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
                            at.play();
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

        receiveData(btSocket);
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