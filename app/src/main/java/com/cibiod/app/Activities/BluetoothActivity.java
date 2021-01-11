package com.cibiod.app.Activities;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.transition.Transition;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cibiod.app.Fragments.BottomSheetBluetoothDevice;
import com.cibiod.app.Objects.PatientObject;
import com.cibiod.app.Objects.TestObject;
import com.cibiod.app.R;
import com.cibiod.app.Utils.BottomAppBarCutCornersTopEdge;
import com.cibiod.app.Utils.Wavfile;
import com.cibiod.app.Utils.u;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomappbar.BottomAppBarTopEdgeTreatment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.transition.platform.MaterialContainerTransform;
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

public class BluetoothActivity extends AppCompatActivity {
    final protected static char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final File audioFile = new File(Environment.getExternalStorageDirectory() + "/Cibiod/PCG.wav"),
            dataFile = new File(Environment.getExternalStorageDirectory() + "/Cibiod/data.txt"),
            pcgFile = new File(Environment.getExternalStorageDirectory() + "/Cibiod/pcgFile.txt"),
            ecgFile = new File(Environment.getExternalStorageDirectory() + "/Cibiod/ecgFile.txt"),
            tempFile = new File(Environment.getExternalStorageDirectory() + "/Cibiod/tempFile.txt"),
            zipFile = new File(Environment.getExternalStorageDirectory() + "/Cibiod/audioZip.zip"),
            folder = new File(Environment.getExternalStorageDirectory() + "/Cibiod/");
    private BluetoothAdapter bluetoothAdapter;
    private PatientObject patient;
    private AudioTrack at;
    private long audioStartTime;
    private LineGraphSeries<DataPoint> pcgSeries, ecgSeries;
    private TextView statusText, tempText, hrText, batteryText, dataLossText;
    private FileOutputStream pcgOs, ecgOs, tempOs;
    //   These values are used for plotting
    private int i = 0, j = 0;
    private Queue<Short> pcgQ, ecgQ;
    private boolean plotting = false;
    private final Thread[] threads = new Thread[3];
    private BluetoothSocket connectedSocket;
    private final StringBuffer dataBuffer = new StringBuffer();
    private boolean getPcgSent = false;
    private int total = 0;
    private int dataLoss = 0;
    private boolean wasPrevProcessed = false;
    //    endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        Toolbar toolbar = findViewById(R.id.toolbarTest);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.cutom_toolbar);

        DrawerLayout drawerLayout = findViewById(R.id.drawerLayoutTest);
        NavigationView navigationView = findViewById(R.id.navViewPatient);
        u.setupToolbar(this, drawerLayout, navigationView, toolbar, "Test", 100);

        //region graph setup
        GraphView pcgGraph = findViewById(R.id.graphViewPcg);
        pcgSeries = new LineGraphSeries<>();
        pcgGraph.addSeries(pcgSeries);
        pcgGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        pcgGraph.getGridLabelRenderer().setVerticalLabelsVisible(false);
        pcgGraph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.NONE);
        pcgSeries.setThickness(4);
        pcgSeries.setColor(getColor(R.color.blue));
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
        ecgSeries.setThickness(4);
        ecgSeries.setColor(getColor(R.color.blue));
        Viewport viewportEcg = ecgGraph.getViewport();
        viewportEcg.setYAxisBoundsManual(true);
        viewportEcg.setMaxY(1000);
        viewportEcg.setMinY(-600);
        viewportEcg.setXAxisBoundsManual(true);
        viewportEcg.setMaxX(5000);
        viewportEcg.setMinX(0);
        viewportEcg.setScrollable(true);
//        endregion
        //        region audio track setup
        int minBufferSize = AudioTrack.getMinBufferSize(44100,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        at = new AudioTrack(AudioManager.USE_DEFAULT_STREAM_TYPE, 44100,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize, AudioTrack.MODE_STREAM);
        at.setPlaybackRate(500);
//        endregion
        //region intialization of views and other vars
        statusText = findViewById(R.id.status);
        tempText = findViewById(R.id.temp);
        hrText = findViewById(R.id.heartrate);
        batteryText = findViewById(R.id.batteryLvl);
        dataLossText = findViewById(R.id.dataLoss);
        FloatingActionButton fab = findViewById(R.id.fabTest);

        if(!folder.exists()){
            u.print("folder does not exists, making the folder");
            folder.mkdir();
        }

        try {
            pcgOs = new FileOutputStream(pcgFile);
            ecgOs = new FileOutputStream(ecgFile);
            tempOs = new FileOutputStream(tempFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        BottomAppBar bar = findViewById(R.id.bottomBarTest);

        BottomAppBarTopEdgeTreatment topEdge = new BottomAppBarCutCornersTopEdge(
                bar.getFabCradleMargin(),
                bar.getFabCradleRoundedCornerRadius(),
                bar.getCradleVerticalOffset());

        MaterialShapeDrawable bottomBarBackground = (MaterialShapeDrawable) bar.getBackground();
        bottomBarBackground.setShapeAppearanceModel(
                bottomBarBackground.getShapeAppearanceModel()
                        .toBuilder()
                        .setTopRightCorner(CornerFamily.ROUNDED, 75)
                        .setTopLeftCorner(CornerFamily.ROUNDED, 75)
                        .setTopEdge(topEdge)
                        .build());
        //endregion

        Intent intent = getIntent();
        String action = intent.getStringExtra("from");

        if (action != null && action.equals("cloud")) {
            setEnterSharedElementCallback(new MaterialContainerTransformSharedElementCallback());
            getWindow().setSharedElementEnterTransition(customTransition());
            getWindow().setSharedElementReturnTransition(customTransition());
            TestObject test = (TestObject) getIntent().getSerializableExtra("testObject");
            downloadFromCloud(test);
            statusText.setText("DOWNLOADING FROM CLOUD");
            fab.setImageResource(R.drawable.icon_tick_45);
        } else if (action != null) {
            if (action.equals("local")) {
                patient = (PatientObject) getIntent().getSerializableExtra("patientObject");
                fab.setOnClickListener(new UploadClickListener());
            } else if (action.equals("quickTest")) {
                fab.setImageResource(R.drawable.icon_tick_45);
            }
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                Toast.makeText(this, "Bluetooth Required", Toast.LENGTH_LONG).show();
                finish();
            } else if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 11);
            } else {
                statusText.setText("CONNECTING TO DEVICE");
                startBluetoothService();
            }
        }
    }

    private Transition customTransition() {
        MaterialContainerTransform mct = new MaterialContainerTransform();
        mct.setFadeMode(MaterialContainerTransform.FADE_MODE_OUT);
        mct.setScrimColor(Color.DKGRAY);
        mct.setAllContainerColors(Color.WHITE);
        mct.setElevationShadowEnabled(true);
        mct.setStartElevation(8);
        mct.setEndElevation(16);
        return mct.addTarget(R.id.drawerLayoutTest).setDuration(600).setInterpolator(new AccelerateDecelerateInterpolator());
    }

    //region Top right corner menu setup
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.export_menu, menu);
        return true;
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        changeScrimState(true);
        return true;
    }

    @Override
    public void onPanelClosed(int featureId, @NonNull Menu menu) {
        changeScrimState(false);
    }

    private void changeScrimState(boolean toOpen) {
        View view = findViewById(R.id.scrimBluetooth);
        ObjectAnimator oa;
        if (toOpen) oa = ObjectAnimator.ofArgb(view, "alpha", 0, 1);
        else oa = ObjectAnimator.ofArgb(view, "alpha", 1, 0);
        oa.setDuration(300);
        oa.start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.export) {
            Wavfile.makeZip(pcgFile, audioFile, zipFile);
            Uri uri = Uri.parse(zipFile.getAbsolutePath());
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("application/zip");
            share.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(share, "Share Via"));
        }
        return true;
    }

    private void downloadFromCloud(TestObject patient) {
        FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference dataRef = storage.getReferenceFromUrl(patient.getDataCloud());
        try {
            Files.createDirectories(Paths.get(Environment.getExternalStorageDirectory() + "/Cibiod"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        TextView dataLossText = findViewById(R.id.dataLoss);
        dataLossText.setText("0");
        processDownload(dataRef, dataFile);
    }

    private void processDownload(StorageReference ref, File file) {
        ref.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                try {
                    processFile();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(BluetoothActivity.this, exception.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void processFile() throws FileNotFoundException {
        final Scanner myReader = new Scanner(dataFile);
        threads[0] = new Thread(new Runnable() {
            @Override
            public void run() {
                while (myReader.hasNextLine()) {
                    if (threads[0].isInterrupted())
                        break;
                    String data = myReader.nextLine();
                    String[] splited = data.split("\\s+");
                    processValues(Short.parseShort(splited[0]), Short.parseShort(splited[1]));
                    displayVal(tempText, Integer.parseInt(splited[2]));
                }
                myReader.close();
                try {
                    processFile();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        threads[0].start();
    }

    private void displayVal(final TextView textView, final Object val) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(String.valueOf(val));
            }
        });
    }

    /**
     * Plots every 8th value, writes to audio track and writes to file
     *
     * @param pcg The final single processed value of pcg
     * @param ecg The final single processed value of ecg
     */
    private void processValues(final short pcg, final short ecg) {
        if (pcgQ == null) {
            pcgQ = new LinkedList<>();
            ecgQ = new LinkedList<>();
        }
        if (j % 8 == 0) {
            if (pcgQ.size() > 100000) {
                pcgQ = new LinkedList<>();
                ecgQ = new LinkedList<>();
            }
            pcgQ.offer(pcg);
            ecgQ.offer(ecg);
        }
        j++;

        writeToFile(pcgOs, pcg);
        writeToFile(ecgOs, ecg);

//        writing to audiotrack

        byte[] ret = new byte[2];
        ret[0] = (byte) (pcg & 0xff);
        ret[1] = (byte) ((pcg >> 8) & 0xff);
        if (at.getPlayState() == AudioTrack.PLAYSTATE_PLAYING)
            at.write(ret, 0, 2);

//        plotting graph thread, plots graph from queue of loaded data

        if (!plotting & pcgQ.size() > 200) {
            plotting = true;
            threads[2] = new Thread() {
                @Override
                public void run() {
                    super.run();
                    at.play();
                    audioStartTime = System.currentTimeMillis();
                    while (true) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int elapsed = (int) (System.currentTimeMillis() - audioStartTime) / 100;
                                int mins = Math.max(elapsed / 60, 0);
                                int secs = Math.max(elapsed % 60, 0);
                                String minutes = mins < 10 ? "0" + mins : String.valueOf(mins);
                                String seconds = secs < 10 ? "0" + secs : String.valueOf(secs);
                                statusText.setText(minutes + ":" + seconds);
                                if (pcgQ.size() > 0)
                                    pcgSeries.appendData(new DataPoint(i * 16, pcgQ.remove()), true, 100000);
                                if (ecgQ.size() > 0)
                                    ecgSeries.appendData(new DataPoint(i * 16, ecgQ.remove()), true, 100000);
                            }
                        });
                        i++;
                        if (i > 10000)
                            break;
                        try {
                            Thread.sleep(16);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };

            threads[2].start();
        }
    }

    public void startBluetoothService() {
        String deviceAddress = u.getPref(this, "pairedStethoAddress");
        if (deviceAddress.equals("NA")) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 69);
            else {
                BottomSheetBluetoothDevice bottomSheet = new BottomSheetBluetoothDevice(bluetoothAdapter);
                bottomSheet.show(getSupportFragmentManager(), "bottomSheet");
            }
            return;
        }

        try {
            BluetoothDevice toConnectBtDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);
            BluetoothSocket btSocket;
            btSocket = toConnectBtDevice.createRfcommSocketToServiceRecord(uuid);
            btSocket.connect();
            connectedSocket = btSocket;
            sendData("hello");
            receiveData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveData() {
        threads[1] = new Thread() {
            @Override
            public void run() {
                InputStream inStream;
                try {
                    inStream = connectedSocket.getInputStream();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            statusText.setText("BUFFERING DATA");
                        }
                    });
                    byte[] inData = new byte[1024];
                    int read;
                    while (!this.isInterrupted()) {
                        assert inStream != null;
                        read = inStream.read(inData);
                        byte[] extractedData = Arrays.copyOfRange(inData, 0, read);
                        dataBuffer.append(convertByteToString(extractedData));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (dataBuffer.length() >= 12)
                                    decodeDcip();
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        threads[1].start();
    }

    private void decodeDcip() {
        while (dataBuffer.length() >= 12) {
            String data = dataBuffer.toString();

            if (data.startsWith("02C306")) {
                if (data.length() >= 18) {
                    if (isChecksumOk(data.substring(0, 16), data.substring(16, 18))) {
                        wasPrevProcessed = true;
                        short pcg = (short) getDataFromHex(data.substring(6, 10), 4);
                        short ecg = (short) getDataFromHex(data.substring(10, 14), 4);
                        int hr = getDataFromHex(data.substring(14, 16), 2);
                        dataBuffer.delete(0, 18);
                        displayVal(hrText, hr);
                        processValues(pcg, ecg);
                    }
                }
            } else if (data.startsWith("02C503") & data.length() >= 12) {
                if (isChecksumOk(data.substring(0, 10), data.substring(10, 12))) {
                    wasPrevProcessed = true;
                    int battery = getDataFromHex(data.substring(6, 8), 2);
                    int version = getDataFromHex(data.substring(8, 10), 2);
                    dataBuffer.delete(0, 12);
                    displayVal(batteryText, battery);
                    if (!getPcgSent) {
                        getPcgSent = true;
                        sendData("getPcg");
                    }
//                    display battery
                }
            } else if (data.startsWith("02C403") & data.length() >= 12) {
                if (isChecksumOk(data.substring(0, 10), data.substring(10, 12))) {
                    wasPrevProcessed = true;
                    int temperature = getDataFromHex(data.substring(6, 10), 4);
                    dataBuffer.delete(0, 12);
                    displayVal(tempText, temperature / 10);
                    writeToFile(tempOs, temperature);
                }
            } else {
                discardUntilNext();
            }
        }
    }

    private void discardUntilNext() {
        if (wasPrevProcessed) {
            dataLoss++;
            total++;
        }
        wasPrevProcessed = false;
        float percent = (float) dataLoss / total * 100;
        displayVal(dataLossText, (Math.round(percent * 100.0) / 100.0) + "%");
        dataBuffer.delete(0, 2);
    }

    private boolean isChecksumOk(String hex, String cs) {
        List<String> ret = new ArrayList<>((hex.length() + 1) / 2);
        for (int start = 0; start < hex.length(); start += 2) {
            ret.add(hex.substring(start, Math.min(hex.length(), start + 2)));
        }
        String[] splitArr = ret.toArray(new String[0]);
        int sumInt = 0;
        for (String p : splitArr) {
            sumInt += Integer.parseInt(p, 16);
        }
        sumInt = ~sumInt;
        sumInt++;
        String calcChecksum = Integer.toHexString(sumInt);
        calcChecksum = calcChecksum.substring(calcChecksum.length() - 2).toUpperCase();

        if (calcChecksum.equals(cs)) {
            total++;
            return true;
        } else {
            discardUntilNext();
            return false;
        }
    }

    private int getDataFromHex(String str, int chars) {
        String sliced = str.substring(0, chars);
        return Integer.parseInt(sliced, 16);
    }

    private void writeToFile(FileOutputStream os, Object s) {
        try {
            os.write(String.valueOf(s).getBytes());
            os.write("\n".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String convertByteToString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;

        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }

        return new String(hexChars);
    }

    private void sendData(String s) {
        OutputStream outStream;
        try {
            outStream = connectedSocket.getOutputStream();
            byte[] message;
            switch (s) {
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
                    threads[1].interrupt();
                    threads[2].interrupt();
                    break;

                default:
                    message = hexStringToByte("FF");
            }

            outStream.write(message);
            u.print(s + " is sent");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] hexStringToByte(String s) {
        s = s.length() % 2 != 0 ? "0" + s : s;

        byte[] b = new byte[s.length() / 2];

        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(s.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }

    @Override
    protected void onPause() {
        super.onPause();
        at.pause();
    }

    @Override
    protected void onResume() {
        at.play();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (threads[0] != null)
            threads[0].interrupt();
        if (threads[1] != null)
            threads[1].interrupt();
        if (threads[2] != null)
            threads[2].interrupt();
        at.stop();
        at.flush();
        at.release();
        if (pcgQ != null) {
            pcgQ.clear();
            ecgQ.clear();
        }
        try {
            if (tempOs != null) tempOs.close();
            if (pcgOs != null) pcgOs.close();
            if (ecgOs != null) ecgOs.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 11) {
            if (resultCode == RESULT_OK) {
                startBluetoothService();
            } else {
                Toast.makeText(this, "Bluetooth is required to connect", Toast.LENGTH_LONG).show();
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 11);
            }
        }

        if (requestCode == 69) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Permissions required fpr app to work!", Toast.LENGTH_LONG).show();
                requestPermissions(new String[]{ACCESS_COARSE_LOCATION}, 69);
            }

            if (resultCode == RESULT_OK) {
                u.alert();
                startBluetoothService();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showLoading() {
        ImageView bg = findViewById(R.id.fabBgBluetooth);
        ConstraintLayout content = findViewById(R.id.contentBluetooth);
        AnimatorSet as = new AnimatorSet();
        ObjectAnimator oa = ObjectAnimator.ofFloat(content, "alpha", 1, 0).setDuration(200);
        ObjectAnimator oa2 = ObjectAnimator.ofFloat(bg, "scaleX", 0, 30).setDuration(600);
        ObjectAnimator oa3 = ObjectAnimator.ofFloat(bg, "scaleY", 0, 30).setDuration(600);
        as.playTogether(oa, oa2, oa3);
        as.setInterpolator(new DecelerateInterpolator());
        as.start();

        ConstraintLayout layout = findViewById(R.id.loadingShower);
        ObjectAnimator oa4 = ObjectAnimator.ofFloat(layout, "alpha", 0, 1).setDuration(600);
        oa4.setStartDelay(600);
        oa4.setInterpolator(new DecelerateInterpolator());
        oa4.start();
        getWindow().setStatusBarColor(getColor(R.color.orangeDark));
    }

    private void decideIdAndAddToDb(final String url) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        final DatabaseReference dbRef = database.getReference("users");
        final Query idGetter = dbRef.child(u.getPref(this, "id")).child("patients").child(patient.getId()).orderByKey();

        idGetter.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                idGetter.removeEventListener(this);
                dbRef.removeEventListener(this);
                String id = null;
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    try {
                        int temp = Integer.parseInt(postSnapshot.getKey());
                        id = Integer.toString(++temp);
                    } catch (Exception e) {
                        if (id == null)
                            id = Integer.toString(1);
                        addToDb(url, id, database);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Firebase Connection Error", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addToDb(String url, String id, FirebaseDatabase database) {
        DatabaseReference db = database.getReference("users").child(id).child("patients").child(patient.getId()).child(id);

        Date date = new Date();
        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        String strDate = formatter.format(date);
        LocalTime time = LocalTime.now();
        String strTime = time.format(DateTimeFormatter.ofPattern("HH:mm"));


        db.child("date").setValue(strDate);
        db.child("time").setValue(strTime);
        db.child("data").setValue(url).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(BluetoothActivity.this, "Added to Database", Toast.LENGTH_LONG).show();
                BluetoothActivity.this.finish();
            }
        });
    }

    private class UploadClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            showLoading();
            at.pause();
//            if(pcgQ!=null)
//                pcgQ.clear();
//            if(receiveDataThread!=null)
//                receiveDataThread.interrupt();
//            try {
//                ArrayList<String> lines = formatValues();
//
//                FileOutputStream dataOs = new FileOutputStream(dataFile);
//                for (String s : lines)
//                    dataOs.write(s.getBytes());
//
//                dataOs.close();

            uploadToCloud();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

        }

        private ArrayList<String> formatValues() throws FileNotFoundException {
            at.stop();
            at.flush();
            pcgQ.clear();
            ecgQ.clear();
            Scanner pcgReader = new Scanner(pcgFile);
            Scanner ecgReader = new Scanner(ecgFile);
            Scanner tempReader = new Scanner(tempFile);

            ArrayList<String> lines = new ArrayList<>();
            while (pcgReader.hasNextLine() & ecgReader.hasNextLine() & tempReader.hasNextLine())
                lines.add(pcgReader.nextLine() + " " + ecgReader.nextLine() + " " + tempReader.nextLine() + "\n");

            pcgReader.close();
            ecgReader.close();
            tempReader.close();

            return lines;
        }

        private void uploadToCloud() {
            FirebaseStorage storage = FirebaseStorage.getInstance();

            StorageReference storageRef = storage.getReference();

            Uri file = Uri.fromFile(dataFile);
            final StorageReference dataFileRef = storageRef.child("data/patientId_" + patient.getId() + ".txt");
            UploadTask uploadTask = dataFileRef.putFile(file);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(BluetoothActivity.this, "Failed to upload to database", Toast.LENGTH_LONG).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    dataFileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri downloadUrl) {
                            decideIdAndAddToDb(downloadUrl.toString());
                        }
                    });
                }
            });

        }
    }

    //    private boolean ifEqDone = false;

//    private void dsp(FileInputStream inputStream) throws FileNotFoundException{
//        new AndroidFFMPEGLocator(this);
//        TarsosDSPAudioFormat format =  new TarsosDSPAudioFormat(sampleRate,16,1,true,false);
//        AudioDispatcher adp = new AudioDispatcher(new UniversalAudioInputStream(inputStream,format),minBufferSize,0);
//
//        adp.addAudioProcessor(new RateTransposer(4.0d));
//        adp.addAudioProcessor(new RateTransposer(1.6203d));
//        adp.addAudioProcessor(new LowPassFS(500,sampleRate));
//        adp.addAudioProcessor(new LowPassFS(500,sampleRate));
//        adp.addAudioProcessor(new LowPassFS(500,sampleRate));
//        adp.addAudioProcessor(new AndroidAudioPlayer(format,minBufferSize, AudioManager.STREAM_MUSIC));
//        RandomAccessFile raf = new RandomAccessFile(audio4000path, "rw");
//        adp.addAudioProcessor(new WriterProcessor(format,raf));
//        new Thread(adp, "Sound Thread").start();
//    }
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        sendData("stopPcg");
//        if (bluetoothAdapter.isDiscovering())
//            bluetoothAdapter.cancelDiscovery();
//        unregisterReceiver(receiver);
//    }

}