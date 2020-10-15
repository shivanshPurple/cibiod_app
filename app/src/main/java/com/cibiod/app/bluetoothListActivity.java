package com.cibiod.app;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

//import com.anand.brose.graphviewlibrary.GraphView;
//import com.anand.brose.graphviewlibrary.WaveSample;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.GainProcessor;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.UniversalAudioInputStream;
import be.tarsos.dsp.io.android.AndroidAudioPlayer;
import be.tarsos.dsp.io.android.AndroidFFMPEGLocator;
import be.tarsos.dsp.resample.RateTransposer;
import be.tarsos.dsp.writer.WriterProcessor;

public class bluetoothListActivity extends AppCompatActivity implements recyclerClickInterface{
    private BluetoothAdapter bluetoothAdapter;
    private RecyclerView rv;
    private ArrayList<btDevice> btDevices = new ArrayList<>();
    private SharedPreferences prefs;
    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private AudioTrack at;
    private boolean audioLoaded = false;
    private Thread receiveDataThread;
    private TextView connectionText, batteryText, temperatureText, hrText, dataLossText;
    private LineGraphSeries<DataPoint> pcgSeries, ecgSeries;
    private int sampleRate = 4000, minBufferSize;

    private String audio500path = Environment.getExternalStorageDirectory()+ "/cibiodLogs/songTest.wav",
            audio4000path = Environment.getExternalStorageDirectory()+ "/cibiodLogs/audio4000hz.wav",
            listPath = Environment.getExternalStorageDirectory()+ "/cibiodLogs/list1.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_list);

        rv = findViewById(R.id.btListView);
        connectionText = findViewById(R.id.connectionText);
        batteryText = findViewById(R.id.batteryText);
        temperatureText = findViewById(R.id.temperatureText);
        hrText = findViewById(R.id.hrText);
        dataLossText = findViewById(R.id.dataLossText);

        GraphView pcgGraph = findViewById(R.id.graphViewPcg);
        pcgSeries = new LineGraphSeries<>();
        pcgGraph.addSeries(pcgSeries);
        pcgGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        pcgGraph.getGridLabelRenderer().setVerticalLabelsVisible(false);
        pcgGraph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.NONE);
        pcgSeries.setThickness(2);

        Viewport viewportPcg = pcgGraph.getViewport();
        viewportPcg.setYAxisBoundsManual(true);
        viewportPcg.setMaxY(6000);
        viewportPcg.setMinY(-6000);
        viewportPcg.setXAxisBoundsManual(true);
        viewportPcg.setMaxX(5000);
        viewportPcg.setMinX(0);
        viewportPcg.setScrollable(true);

        GraphView ecgGraph = findViewById(R.id.graphViewEcg);
        ecgSeries = new LineGraphSeries<>();
        ecgGraph.addSeries(ecgSeries);
        ecgGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        ecgGraph.getGridLabelRenderer().setVerticalLabelsVisible(false);
        ecgGraph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.NONE);
        ecgSeries.setThickness(2);

        Viewport viewportEcg = ecgGraph.getViewport();
        viewportEcg.setYAxisBoundsManual(true);
        viewportEcg.setMaxY(750);
        viewportEcg.setMinY(-750);
        viewportEcg.setXAxisBoundsManual(true);
        viewportEcg.setMaxX(5000);
        viewportEcg.setMinX(0);

        initializeUiAndListener();

        minBufferSize = AudioTrack.getMinBufferSize(sampleRate,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

        minBufferSize *= 2;

//        at = new AudioTrack(AudioManager.USE_DEFAULT_STREAM_TYPE, sampleRate,
//                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
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
            startBluetoothService();
        }
    }

    private void playFile(String filePath, int position) {
        try {
            final MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );
            mediaPlayer.prepare();
            mediaPlayer.start();
            if(position!=0)
                mediaPlayer.seekTo(position);
            Thread stateChangeListener = new Thread(new Runnable() {
                @Override
                public void run() {
                    int prevPosition = 0;
                    while(true)
                    {
                        if(!mediaPlayer.isPlaying())
                        {
                            mediaPlayer.reset();
                            playFile(audio500path,prevPosition);
                            break;
                        }
                        else
                            prevPosition = mediaPlayer.getCurrentPosition();
                    }
                }
            });

            stateChangeListener.setPriority(Thread.NORM_PRIORITY);
            stateChangeListener.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startBluetoothService() {
//            openServer();
//            searchDevices();
//            File f = new File(Environment.getExternalStorageDirectory()+ "/cibiodLogs/audio1.wav");
//            playAudio(f);
//            searchDevices();
//            searchBondedDevices();

        BluetoothDevice toConnectBtDevice = bluetoothAdapter.getRemoteDevice("00:01:95:4A:46:7A");

        BluetoothSocket btSocket = null;
        try {
            btSocket = toConnectBtDevice.createRfcommSocketToServiceRecord(uuid);
            btSocket.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connectedSocket = btSocket;
        connectionText.setText("Connected!");

        try {
            fos = new FileOutputStream(new File(Environment.getExternalStorageDirectory()+ "/cibiodLogs/list1.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        receiveData();
    }

    private FileOutputStream fos;

    private void initializeUiAndListener() {
//        batteryText.setAlpha(0);
//        whoButton.setAlpha(0);
//        getPcgButton.setAlpha(0);
//        lungButton.setAlpha(0);
//        heartButton.setAlpha(0);
//        allModeButton.setAlpha(0);
//        goodbyeButton.setAlpha(0);
//
//        whoButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                sendData("who");
//            }
//        });
//
//        getPcgButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                sendData("getPcg");
//            }
//        });
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

    private StringBuffer dataBuffer = new StringBuffer();
    private void receiveData() {
        receiveDataThread = new Thread() {
            @Override
            public void run() {
                InputStream inStream;
                try{
                    inStream = connectedSocket.getInputStream();
                    print("recieving data");

                    byte[] inData = new byte[1024];
                    int read;
                    while(!this.isInterrupted()) {
                        assert inStream != null;
                        read = inStream.read(inData);
                        byte[] extractedData = Arrays.copyOfRange(inData, 0, read);
                        dataBuffer.append(convertByteToString(extractedData));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                            if(dataBuffer.length()>=12)
                                decodeDcip();
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

    private boolean getPcgSent = false;
    private String lastUsed = "not used still";

    private void decodeDcip() {
        while(dataBuffer.length()>=12)
        {
//            print("///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
            String data = dataBuffer.toString();

            if(data.startsWith("02C306"))
            {
                if(data.length()>=18)
                {
                    if(checksumOk(data.substring(0,16),data.substring(16,18)))
                    {
                        lastUsed=data;
                        wasPrevProcessed = true;
                        short pcg = (short) getDataFromHex(data.substring(6,10),0,4);
                        short ecg = (short) getDataFromHex(data.substring(10,14),0,4);
                        int hr = getDataFromHex(data.substring(14,16),0,2);
                        dataBuffer.delete(0,18);
                        hrText.setText("Heart Rate is "+hr);
                        plot(pcg,ecg);
                    }
                }
            }

            else if(data.startsWith("02C503") & data.length()>=12)
            {
                if(checksumOk(data.substring(0,10),data.substring(10,12)))
                {
                    lastUsed=data;
                    wasPrevProcessed = true;
                    int battery = getDataFromHex(data.substring(6,8),0,2);
                    int version = getDataFromHex(data.substring(8,10),0,2);
                    dataBuffer.delete(0,12);
                    if(!getPcgSent)
                    {
                        getPcgSent = true;
                        sendData("getPcg");
                    }
                    batteryText.setText("Battery is "+battery);
                }
            }
            else if(data.startsWith("02C403") & data.length()>=12)
            {
                if(checksumOk(data.substring(0,10),data.substring(10,12)))
                {
                    lastUsed=data;
                    wasPrevProcessed = true;
                    int temperature = getDataFromHex(data.substring(6,10),0,4);
                    dataBuffer.delete(0,12);
                    temperatureText.setText("Temperature is "+(float)temperature/10);
                }
            }
            else
            {
                discardUnitllNext();
            }
        }
    }

    private boolean plotting = false;
    private Short[] pcgArr = new Short[100000], ecgArr = new Short[100000];
    private int i = 0, j = 0;

    private void plot(short pcg, short ecg) {
        pcgArr[j] = pcg;
//        ecgArr[j] = ecg;
        j++;
//        writeToFile(pcg);
        writeInWav(pcg);

        //plotting the graph, called once only
        if(!plotting & j>4000)
        {
            plotting = true;
            Thread plottingThread = new Thread() {
                @Override
                public void run() {
                    super.run();
                    print("plotting and playing started");
                    playFile(audio500path, 0);
                    while (true) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (pcgArr[i] != null) {
                                    pcgSeries.appendData(new DataPoint(i * 2, pcgArr[i]), true, 7500);
                                }
//                                ecgSeries.appendData(new DataPoint(i*2,ecgArr[i]),true,7500);
                            }
                        });
                        i++;
                        try {
                            Thread.sleep(2);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };

            plottingThread.setPriority(Thread.MAX_PRIORITY);
            plottingThread.start();
        }
    }

    private int total = 0;
    private int dataLoss = 0;
    private boolean wasPrevProcessed = false;

    @SuppressLint("SetTextI18n")
    private void discardUnitllNext() {
        if(wasPrevProcessed)
        {
            dataLoss++;
            total++;
        }
        wasPrevProcessed = false;
        dataLossText.setText("Data loss is "+((float)dataLoss/total*100)+"%");
        dataBuffer.delete(0,2);
    }

    private boolean checksumOk(String hex, String cs) {
        List<String> ret = new ArrayList<>((hex.length() + 1) / 2);
        for (int start = 0; start < hex.length(); start += 2) {
            ret.add(hex.substring(start, Math.min(hex.length(), start + 2)));
        }
        String[] splitArr = ret.toArray(new String[0]);
        int sumInt = 0;
        for(String p:splitArr)
        {
            sumInt+=Integer.parseInt(p,16);
        }
        sumInt=~sumInt;
        sumInt++;
        String calcChecksum = Integer.toHexString(sumInt);
        calcChecksum = calcChecksum.substring(calcChecksum.length()-2).toUpperCase();

        if(calcChecksum.equals(cs))
        {
            total++;
            return true;
        }

        else
        {
            discardUnitllNext();
            return false;
        }
    }

    private int getDataFromHex(String str, int i, int chars) {
        String sliced = str.substring(i,i+chars);
        return Integer.parseInt(sliced,16);
    }

    private void writeToFile(short s) {
        try {
            fos.write(String.valueOf(s).getBytes());
            fos.write("\n".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean ifWavMade = false;

    private void writeInWav(short s) {
        try
        {
            File f = new File(audio500path);
            if(!ifWavMade)
            {
                byte[] header = new byte[44];
                byte[] data = get16BitPcm(s);

                long totalDataLen = data.length + 36;
                int sampleRate = 500;
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
            }
        } catch (IOException e) {
            e.printStackTrace();
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
                        try {
                            inStreamAudio = new FileInputStream(file);
                            at.play();
                            print("audio started!");
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
                            print("audio stopped!");
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

//    private void dsp(FileInputStream inputStream) throws FileNotFoundException{
//        new AndroidFFMPEGLocator(this);
//        TarsosDSPAudioFormat format =  new TarsosDSPAudioFormat(sampleRate,16,1,true,false);
//        AudioDispatcher adp = new AudioDispatcher(new UniversalAudioInputStream(inputStream,format),minBufferSize,0);
//
//        adp.addAudioProcessor(new RateTransposer(4.0d));
//        adp.addAudioProcessor(new RateTransposer(1.6203d));
////        adp.addAudioProcessor(new LowPassFS(500,sampleRate));
////        adp.addAudioProcessor(new LowPassFS(500,sampleRate));
////        adp.addAudioProcessor(new LowPassFS(500,sampleRate));
////        adp.addAudioProcessor(new AndroidAudioPlayer(format,minBufferSize, AudioManager.STREAM_MUSIC));
//        RandomAccessFile raf = new RandomAccessFile(audio4000path, "rw");
//        adp.addAudioProcessor(new WriterProcessor(format,raf));
//        new Thread(adp, "Sound Thread").start();
//    }

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
        print(btDevices.get(position).getAddress());
        editor.apply();

        BluetoothDevice toConnectBtDevice = bluetoothAdapter.getRemoteDevice(prefs.getString("pairedStethoAddress", "NA"));

        BluetoothSocket btSocket = toConnectBtDevice.createRfcommSocketToServiceRecord(uuid);
        btSocket.connect();
        connectedSocket = btSocket;
        connectionText.setText("Connected!");

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
                startBluetoothService();
            }
        }
    }
}