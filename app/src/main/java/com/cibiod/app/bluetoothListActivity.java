package com.cibiod.app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.anand.brose.graphviewlibrary.GraphView;
import com.anand.brose.graphviewlibrary.WaveSample;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class bluetoothListActivity extends AppCompatActivity implements recyclerClickInterface{
    private BluetoothAdapter bluetoothAdapter;
    private RecyclerView rv;
    private ArrayList<btDevice> btDevices = new ArrayList<>();
    private SharedPreferences prefs;
    private static UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private AudioTrack at;
    private boolean audioLoaded = false;
    private TextView connectionText, batteryText;
    private Button whoButton, getPcgButton, lungButton, heartButton, allModeButton, goodbyeButton;
    private Thread receiveDataThread;


    private List<WaveSample> points = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        GraphView graphView = findViewById(R.id.graphView);
        graphView.setMaxAmplitude(Short.MAX_VALUE);
        graphView.setMasterList(points);
        graphView.startPlotting();

        initializeUiAndListener();

//        int minBufferSize = AudioTrack.getMinBufferSize(sampleRate,
//                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
//
//        minBufferSize *= 2;
//        at = new AudioTrack(AudioManager.USE_DEFAULT_STREAM_TYPE, sampleRate,
//                AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,
//                minBufferSize, AudioTrack.MODE_STREAM);

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
//            openServer();
//            searchDevices();
//            File f = new File(Environment.getExternalStorageDirectory()+ "/cibiodLogs/audio1.wav");
//            playAudio(f);
//            try {
//                eq2(f, 1024);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//            searchDevices();
            searchBondedDevices();
        }

    }
    private FileOutputStream fos;

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
                sendData("who");
            }
        });

        getPcgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData("getPcg");
            }
        });
    }

    private BluetoothSocket connectedSocket;

    private void openServer() throws IOException {
        print("starting server");
        BluetoothServerSocket tmp;

        tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("CIBIOD app", uuid);
        print("Listening to device");

        final BluetoothServerSocket finalTmp = tmp;

        new Thread(new Runnable() {
            @Override
            public void run() {

                while (true) {
                    try {
                        connectedSocket = finalTmp.accept();

                        if (connectedSocket != null) {
                            sendData("hello");
                            receiveData();
                            finalTmp.close();
                            break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void receiveData() {
        receiveDataThread = new Thread() {
            @Override
            public void run() {
                InputStream inStream;
                try{
                    inStream = connectedSocket.getInputStream();
                    print("recieving data");
                    sendData("getPcg");

                    byte[] inData = new byte[1024];
                    int read;
                    while(!this.isInterrupted()) {
                        assert inStream != null;
                        read = inStream.read(inData);
                        byte[] extractedData = Arrays.copyOfRange(inData, 0, read);
                        final String data = convertByteToString(extractedData);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    print(data);
                                    decodeDcip(data);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        receiveDataThread.start();
    }

    private void decodeDcip(final String data) throws IOException {
        if(data.contains("02C303"))
        {
//            getListOfCommands(data);
//            for(int i = 0; i < read/6;i++)
//            {
//                short s = (short)getDataFromHex(data,(i*12)+6,4);
//                points.add(new WaveSample(2,s));
////                    writeToFile(s);
////                    writeInWav(s);
//            }
        }

        else if(data.contains("02C0013D"))
        {
            print("received hi from device");
            sendData("getPcg");
            updateUiConnected();
            File f = new File(Environment.getExternalStorageDirectory()+ "/cibiodLogs/songTest.wav");
            File f2 = new File(Environment.getExternalStorageDirectory()+ "/cibiodLogs/list.txt");
            f.delete();
            f2.delete();
        }

        else if(data.contains("02C202"))
        {
            print("received device info");
            btDevices.add(new btDevice("Version",String.valueOf(getDataFromHex(data,6,2))));
            createList();
        }

        else if(data.contains("02C10201"))
        {
            print("pcg incoming");
            fos = new FileOutputStream(new File(Environment.getExternalStorageDirectory()+ "/cibiodLogs/list.txt"));
        }

    }

    private void getListOfCommands(String data)
    {
        short[] dataSize = {12,14,12};
        int beginIndex = 0, dataSizeCounter = 0;

        while(true)
        {
            int endIndex = beginIndex+dataSize[dataSizeCounter];
            String sliced = data.substring(beginIndex, endIndex);
            beginIndex = endIndex;
            dataSizeCounter++;
            if(dataSizeCounter==3)
                dataSizeCounter=0;
            if(sliced.contains("000000"))
                break;

            int pcgEcg = Integer.parseInt(sliced.substring(5,9),16);

            if(sliced.contains("02C303"))
            {
                print("pcg is "+pcgEcg);
            }

            else if(sliced.contains("02C404"))
            {
                print("ecg is "+pcgEcg);
            }

            else if(sliced.contains("02C503"))
            {
                int battery = Integer.parseInt(sliced.substring(5,7),16);
                int version = Integer.parseInt(sliced.substring(7,9),16);
                print("battery is "+battery);
                print("version is "+version);
            }
        }
        print("from "+data);
    }

    private int getDataFromHex(String str, int i, int bytes) {
        String sliced = str.substring(i,i+bytes);
        return Integer.parseInt(sliced,16);
    }

    private void writeToFile(short s) throws IOException {
        fos.write(String.valueOf(s).getBytes());
        fos.write("\n".getBytes());
    }

    private boolean ifWavMade = false;

    private void writeInWav(short s) throws IOException {
        File f = new File(Environment.getExternalStorageDirectory()+ "/cibiodLogs/songTest.wav");
        if(!ifWavMade)
        {
            byte[] header = new byte[44];
            byte[] data = get16BitPcm(s);

            long totalDataLen = data.length + 36;
            int sampleRate = 22050;
            long bitrate = sampleRate * 16;

            header[0] = 'R';
            header[1] = 'I';
            header[2] = 'F';
            header[3] = 'F';
            header[4] = (byte) (totalDataLen & 0xff);
            header[5] = (byte) ((totalDataLen >> 8) & 0xff);
            header[6] = (byte) ((totalDataLen >> 16) & 0xff);
            header[7] = (byte) ((totalDataLen >> 24) & 0xff);
            header[8] = 'W';
            header[9] = 'A';
            header[10] = 'V';
            header[11] = 'E';
            header[12] = 'f';
            header[13] = 'm';
            header[14] = 't';
            header[15] = ' ';
            header[16] = (byte) 16;
            header[17] = 0;
            header[18] = 0;
            header[19] = 0;
            header[20] = 1;
            header[21] = 0;
            header[22] = (byte) 1;
            header[23] = 0;
            header[24] = (byte) (sampleRate & 0xff);
            header[25] = (byte) ((sampleRate >> 8) & 0xff);
            header[26] = (byte) ((sampleRate >> 16) & 0xff);
            header[27] = (byte) ((sampleRate >> 24) & 0xff);
            header[28] = (byte) ((bitrate / 8) & 0xff);
            header[29] = (byte) (((bitrate / 8) >> 8) & 0xff);
            header[30] = (byte) (((bitrate / 8) >> 16) & 0xff);
            header[31] = (byte) (((bitrate / 8) >> 24) & 0xff);
            header[32] = (byte) ((16) / 8);
            header[33] = 0;
            header[34] = 16;
            header[35] = 0;
            header[36] = 'd';
            header[37] = 'a';
            header[38] = 't';
            header[39] = 'a';
            header[40] = (byte) (data.length  & 0xff);
            header[41] = (byte) ((data.length >> 8) & 0xff);
            header[42] = (byte) ((data.length >> 16) & 0xff);
            header[43] = (byte) ((data.length >> 24) & 0xff);

            FileOutputStream os = new FileOutputStream(f,false);
            os.write(header, 0, 44);
            os.write(data);
            os.close();
            ifWavMade = true;
        }
        else
        {
            byte[] wavBytes =  readFileToByteArray(f);
            int existingSize = wavBytes.length;
            byte[] newPart = get16BitPcm(s);
            int newSize = existingSize+newPart.length-44;
            int newTotal = newSize+44;
            wavBytes[4] = (byte) (newSize+36 & 0xff);
            wavBytes[5] = (byte) ((newSize+36 >> 8) & 0xff);
            wavBytes[6] = (byte) ((newSize+36 >> 16) & 0xff);
            wavBytes[7] = (byte) ((newSize+36 >> 24) & 0xff);
            wavBytes[40] = (byte) (newSize  & 0xff);
            wavBytes[41] = (byte) ((newSize >> 8) & 0xff);
            wavBytes[42] = (byte) ((newSize >> 16) & 0xff);
            wavBytes[43] = (byte) ((newSize >> 24) & 0xff);

            byte[] finalWav = new byte[newTotal];
            int cnt = 0;
            while(cnt<newTotal)
            {
                if(cnt<existingSize)
                    finalWav[cnt] = wavBytes[cnt];
                else
                    finalWav[cnt] = newPart[cnt-existingSize];
                cnt++;
            }
            FileOutputStream os = new FileOutputStream(f,false);
            os.write(finalWav);
            os.close();
        }
    }

    private byte[] readFileToByteArray(File f) throws IOException {
        int size = (int) f.length();
        byte[] bytes = new byte[size];
        BufferedInputStream buf = new BufferedInputStream(new FileInputStream(f));
        buf.read(bytes, 0, bytes.length);
        buf.close();
        return bytes;
    }

    private byte[] get16BitPcm(short s) {
        byte[] b = new byte[2];
        b[0] = (byte)(s & 0xff);
        b[1] = (byte)((s >> 8) & 0xff);
        return b;
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

    private void sendData(String s) {
        OutputStream outStream;
        try {
            outStream = connectedSocket.getOutputStream();
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
                message = hexStringToByte("0282020179");
                break;

            case "stopPcg":
                message = hexStringToByte("0282020278");
                break;

            case "bye":
                message = hexStringToByte("02BF013E");
                receiveDataThread.interrupt();
                break;

            default:
                message = hexStringToByte("FF");
        }

        outStream.write(message);
        print(s + " is sent");
        } catch (IOException e) {
            e.printStackTrace();
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

//    private boolean ifEqDone = false;

//    private void eq2(File file, int minBufferSize) throws FileNotFoundException {
//        new AndroidFFMPEGLocator(this);
//        InputStream inputStream = new FileInputStream(file);
//        TarsosDSPAudioFormat format =  new TarsosDSPAudioFormat(sampleRate,16,1,true,false);
//        AudioDispatcher adp = new AudioDispatcher(new UniversalAudioInputStream(inputStream,format),minBufferSize,0);
//
////        adp.addAudioProcessor(new RateTransposer(4.0d));
////        adp.addAudioProcessor(new RateTransposer(3.0d));
////        adp.addAudioProcessor(new LowPassFS(500,sampleRate));
////        adp.addAudioProcessor(new LowPassFS(500,sampleRate));
////        adp.addAudioProcessor(new LowPassFS(500,sampleRate));
//        adp.addAudioProcessor(new GainProcessor(6d));
//        adp.addAudioProcessor(new AndroidAudioPlayer(format,minBufferSize, AudioManager.STREAM_MUSIC));
//        RandomAccessFile raf = new RandomAccessFile(Environment.getExternalStorageDirectory()+ "/cibiodLogs/audioFiltered1.wav", "rw");
//        adp.addAudioProcessor(new WriterProcessor(format,raf));
//        new Thread(adp, "Sound Thread").start();
//    }

    private void updateUiPcg() {
        lungButton.setAlpha(1);
        heartButton.setAlpha(1);
        allModeButton.setAlpha(1);
        goodbyeButton.setAlpha(1);
        lungButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                attachEq("lung");
            }
        });

        heartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                attachEq("heart");
            }
        });

        allModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                attachEq("all");
            }
        });

        goodbyeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData("bye");
            }
        });
    }

    private void searchBondedDevices()
    {
        Set<BluetoothDevice> paired = bluetoothAdapter.getBondedDevices();
        if (paired.size() > 0)
        {
            for (BluetoothDevice device : paired) {
                btDevices.add(new btDevice(device.getName(),device.getAddress()));
            }
            createList();
        }
    }

    private void searchDevices()
    {
        bluetoothAdapter.startDiscovery();
        print("searching");
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
    }

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
        sendData("stopPcg");
        if(bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();
        unregisterReceiver(receiver);
    }

    private void print(Object s)
    {
        Log.d("customm", String.valueOf(s));
    }

    @Override
    public void OnItemClick(int position) throws IOException {
        if(bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();

        rv.setAlpha(0);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("pairedStethoName",btDevices.get(position).getName());
        editor.putString("pairedStethoAddress",btDevices.get(position).getAddress());
        editor.apply();

        BluetoothDevice toConnectBtDevice = bluetoothAdapter.getRemoteDevice(prefs.getString("pairedStethoAddress", "NA"));

        BluetoothSocket btSocket = toConnectBtDevice.createRfcommSocketToServiceRecord(uuid);
        btSocket.connect();
        print("connection successful");
        connectedSocket = btSocket;

        receiveData();
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
                try {
                    openServer();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}