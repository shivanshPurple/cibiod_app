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
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
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
    private static final UUID uuid = UUID.fromString("00001102-0000-1000-8000-00805F9B34FB");
    private MediaPlayer player = new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
        Objects.requireNonNull(getSupportActionBar()).hide(); //hide the title bar

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_list);

        rv = findViewById(R.id.btListView);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            show("Bluetooth Required");
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
            try {
                openServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
//            searchBondedDevices();
        }

    }

    private void openServer() throws IOException {
        show("starting server");
        BluetoothServerSocket tmp = null;

        try {
            tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("CIBIOD app", uuid);
            show("Listening to device");
        } catch (IOException e) {
            show("Socket's listen() method failed " + e);
        }
        BluetoothServerSocket btServerSocket = tmp;

        BluetoothSocket socket;

        while (true) {
            try {
                assert btServerSocket != null;
                socket = btServerSocket.accept();
                show("found socket");
            } catch (IOException e) {
                show("Socket's accept() method failed " + e);
                break;
            }

            if (socket != null) {
                doInAndOut(socket);
                btServerSocket.close();
                break;
            }
        }
    }

    private void doInAndOut(BluetoothSocket socket) {
        show("data exchange");
        InputStream inStream = null;
        try{
            inStream = socket.getInputStream();
        } catch (IOException e) {
            show("input stream creation failed");
        }

        byte[] inData = new byte[1024];
        File file = new File(Environment.getExternalStorageDirectory()+ "/cibiodLogs/audio1.mp3");

        try (OutputStream output = new FileOutputStream(file)) {
            int read;

            assert inStream != null;
            while ((read = inStream.read(inData)) != -1) {
                output.write(inData, 0, read);
                show(String.valueOf(file.length()/(1024)));
                if(file.length()/1024 > 50)
                    playAudio(file, read);
            }

            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        show("done");

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

    private void playAudio(File file, final int read) throws IOException {
        if(!player.isPlaying())
        {
            final FileInputStream fis = new FileInputStream(file);
            player.prepareAsync();
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    try {
                        player.setDataSource(fis.getFD(),0,read);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mp.start();
                }
            });

        }
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

    private void show(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
        Log.d("customm",s);
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
            show("socket successful");
        } catch (IOException e) {
            show("socket create failed: " + e.getMessage() + ".");
        }

        try {
            btSocket.connect();
            show("connection successful");
        } catch (IOException connectException) {
            show("connection failed");
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
                show("Bluetooth Required");
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