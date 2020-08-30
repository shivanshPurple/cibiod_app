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
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.filters.BandPass;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.android.AndroidFFMPEGLocator;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.writer.WriterProcessor;

public class bluetoothListActivity extends AppCompatActivity implements recyclerClickInterface{
    private BluetoothAdapter bluetoothAdapter;
    private RecyclerView rv;
    private ArrayList<btDevice> btDevices = new ArrayList<btDevice>();
    private BluetoothSocket btSocket;
    private SharedPreferences prefs;
    private static UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private AudioTrack at;
    private boolean audioLoaded = false;
    private TextView connectionText, batteryText;
    private Button whoButton, getPcgButton, lungButton, heartButton, allModeButton, goodbyeButton;
    private Thread receiveDataThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(getSupportActionBar()).hide();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_list);

        rv = findViewById(R.id.btListView);
        connectionText = findViewById(R.id.connectionText);
        batteryText = findViewById(R.id.batteryText);
        whoButton = findViewById(R.id.whoButton);
        getPcgButton = findViewById(R.id.getPcgButton);
        lungButton = findViewById(R.id.lungButton);
        heartButton = findViewById(R.id.heartButton);
        allModeButton = findViewById(R.id.allModeButton);
        goodbyeButton = findViewById(R.id.goodbyeButton);

        initializeUiAndListener();

        int minBufferSize = AudioTrack.getMinBufferSize(32000,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

//        minBufferSize *= 2;

        at = new AudioTrack(AudioManager.USE_DEFAULT_STREAM_TYPE, 32000,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize, AudioTrack.MODE_STREAM);

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
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 14);
            prefs = getSharedPreferences("applicationVariables", Context.MODE_PRIVATE);
//            searchDevices();
//            openServer();
            File f = new File(Environment.getExternalStorageDirectory()+ "/cibiodLogs/audio1.wav");
            playAudio(f);
            eq2(f);
            //            searchBondedDevices();
        }

    }

    private void initializeUiAndListener() {
        batteryText.setAlpha(0);
        whoButton.setAlpha(0);
        getPcgButton.setAlpha(0);
        lungButton.setAlpha(0);
        heartButton.setAlpha(0);
        allModeButton.setAlpha(0);
        goodbyeButton.setAlpha(0);

        whoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData(connectedSocket,"who");
            }
        });

        getPcgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData(connectedSocket,"getPcg");
            }
        });
    }

    private void attachEq(String mode) {
        Equalizer eq = new Equalizer(100,at.getAudioSessionId());
        short[] freqRange = eq.getBandLevelRange();
        short minLvl = freqRange[0];

        switch(mode)
        {
            case "lung":
                eq.setBandLevel((short) 2,minLvl);
                eq.setBandLevel((short) 3,minLvl);
                eq.setBandLevel((short) 4,minLvl);
                break;

            case "heart":
                eq.setBandLevel((short) 0,minLvl);
                eq.setBandLevel((short) 1,minLvl);
                eq.setBandLevel((short) 2,minLvl);
                eq.setBandLevel((short) 3,minLvl);
                eq.setBandLevel((short) 4,minLvl);
                break;

            default:
                eq.setBandLevel((short) 0,(short)0);
                eq.setBandLevel((short) 1,(short)0);
                eq.setBandLevel((short) 2,(short)0);
                eq.setBandLevel((short) 3,(short)0);
                eq.setBandLevel((short) 4,(short)0);
        }
        eq.setEnabled(true);
    }

    private BluetoothSocket connectedSocket;

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

                while (true) {
                    try {
                        assert finalTmp != null;
                        connectedSocket = finalTmp.accept();
                    } catch (IOException e) {
                        break;
                    }

                    if (connectedSocket != null) {
                        sendData(connectedSocket,"hello");
                        receiveData(connectedSocket);
                        try {
                            finalTmp.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }
        }).start();
    }

    private boolean temp = true;
    private void receiveData(final BluetoothSocket socket) {
        receiveDataThread = new Thread() {
            @Override
            public void run() {
                InputStream inStream = null;
                try{
                    inStream = socket.getInputStream();
                } catch (IOException e) {
                    print("input stream creation failed");
                }

                byte[] inData = new byte[1024];
                while(!this.isInterrupted())
                {
                    try {
                        assert inStream != null;

                        if(temp)
                        {
                            inStream.read(inData);
                            final String data = convertByteToString(inData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    decodeDcip(data);
                                }
                            });
                        }

                        else
                        {
                            File file = new File(Environment.getExternalStorageDirectory()+ "/cibiodLogs/audio2.wav");

                            try (OutputStream output = new FileOutputStream(file)) {
                                int read;
                                while ((read = inStream.read(inData)) != -1) {
                                    output.write(inData, 0, read);
                                    if(file.length()/1024>300 && !audioLoaded)
                                        playAudio(file);
                                }
                                temp = true;
                                output.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        receiveDataThread.start();
    }

    private void decodeDcip(String data) {
        if(data.contains("02C0013D"))
        {
            print("received hi from device");
            updateUiConnected();
        }

        else if(data.contains("02C202"))
        {
            print("received device info");
            btDevices.add(new btDevice("Version",String.valueOf(getDataFromHex(data,6))));
            createList();
        }

        else if(data.contains("02C10201"))
        {
            print("pcg incoming");
            temp = false;
        }
    }

    private int getDataFromHex(String str, int i) {
        String sliced = str.substring(i,i+2);
        return Integer.parseInt(sliced,16);
    }

    private void updateUiConnected() {
        connectionText.setText("Connected!");
        batteryText.setAlpha(1);
        whoButton.setAlpha(1);
        getPcgButton.setAlpha(1);
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

            case "who":
                message = hexStringToByte("0281017C");
                break;

            case "getPcg":
                message = hexStringToByte("02820201");
                break;

            case "bye":
                message = hexStringToByte("02BF013E");
                receiveDataThread.interrupt();
                break;

            default:
                message = hexStringToByte("FF");
        }

        try {
            assert outStream != null;
            outStream.write(message);
            print(s + " is sent");
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
                        updateUiPcg();
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
                        {
                            at.stop();
                            at.release();
                            break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();
    }

    private boolean ifEqDone = false;

    private void eq2(File file) {
        new AndroidFFMPEGLocator(this);
        final AudioDispatcher adp = AudioDispatcherFactory.fromPipe(file.getAbsolutePath(), 32000, 1024, 0);
        final TarsosDSPAudioFormat format = adp.getFormat();

        adp.addAudioProcessor(new BandPass(1000,100,32000));
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(Environment.getExternalStorageDirectory()+ "/cibiodLogs/audioFiltered.wav", "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        adp.addAudioProcessor(new WriterProcessor(format,raf));
        adp.run();
        print("running filter");

        Thread audioThread = new Thread(adp, "Audio Thread");
        audioThread.start();

    }

    private void updateUiPcg() {
        lungButton.setAlpha(1);
        heartButton.setAlpha(1);
        allModeButton.setAlpha(1);
        goodbyeButton.setAlpha(1);
        lungButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attachEq("lung");
            }
        });

        heartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attachEq("heart");
            }
        });

        allModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attachEq("all");
            }
        });

        goodbyeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData(connectedSocket,"bye");
            }
        });
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