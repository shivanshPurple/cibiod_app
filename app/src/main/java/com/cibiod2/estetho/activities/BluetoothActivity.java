package com.cibiod2.estetho.activities;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
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

import com.cibiod2.estetho.R;
import com.cibiod2.estetho.customViews.PurpleGraph;
import com.cibiod2.estetho.fragments.BottomSheetBluetoothDevice;
import com.cibiod2.estetho.objects.PatientObject;
import com.cibiod2.estetho.objects.TestObject;
import com.cibiod2.estetho.utils.BottomAppBarCutCornersTopEdge;
import com.cibiod2.estetho.utils.Wavfile;
import com.cibiod2.estetho.utils.u;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

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
import java.util.Locale;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

@SuppressWarnings("deprecation")
@SuppressLint("SetTextI18n")
public class BluetoothActivity extends AppCompatActivity {
    final protected static char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    final File audioFile = new File(Environment.getExternalStorageDirectory() + "/Cibiod/PCG.wav"),
            dataFile = new File(Environment.getExternalStorageDirectory() + "/Cibiod/data.txt"),
            pcgFile = new File(Environment.getExternalStorageDirectory() + "/Cibiod/pcgFile.txt"),
            ecgFile = new File(Environment.getExternalStorageDirectory() + "/Cibiod/ecgFile.txt"),
            tempFile = new File(Environment.getExternalStorageDirectory() + "/Cibiod/tempFile.txt"),
            zipFile = new File(Environment.getExternalStorageDirectory() + "/Cibiod/audioZip.zip"),
            folder = new File(Environment.getExternalStorageDirectory() + "/Cibiod/");
    final Thread[] threads = new Thread[3];
    final StringBuffer dataBuffer = new StringBuffer();
    final Queue<Short> pcgQ = new LinkedList<>();
    final Queue<Short> ecgQ = new LinkedList<>();
    BluetoothAdapter bluetoothAdapter;
    PatientObject patient;
    TestObject cloudTest;
    AudioTrack at;
    long audioStartTime;
    TextView statusText, tempText, hrText, batteryText, dataLossText;
    FileOutputStream pcgOs, ecgOs, tempOs;
    PurpleGraph pcgGraph, ecgGraph;
    int i = 0, j = 0;
    boolean plotting = false;
    BluetoothSocket connectedSocket;
    boolean getPcgSent = false;
    int total = 0;
    int dataLoss = 0;
    boolean wasPrevProcessed = false;
    ScheduledExecutorService scheduler;
    boolean cloudEof;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        Toolbar toolbar = findViewById(R.id.toolbarTest);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.cutom_toolbar);

        DrawerLayout drawerLayout = findViewById(R.id.drawerLayoutTest);
        NavigationView navigationView = findViewById(R.id.navViewPatient);
        u.setupToolbar(this, drawerLayout, navigationView, toolbar, "Test", 100);

        //        region ui setup
        int minBufferSize = AudioTrack.getMinBufferSize(22050,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

//        at = new AudioTrack(
//                new AudioAttributes.Builder()
//                        .setUsage(AudioAttributes.USAGE_MEDIA)
//                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                        .build(),
//                new AudioFormat.Builder()
//                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
//                        .setSampleRate(22050)
//                        .build(),
//                minBufferSize, AudioTrack.MODE_STREAM, 69
//        );
        at = new AudioTrack(AudioManager.USE_DEFAULT_STREAM_TYPE, 44100,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize, AudioTrack.MODE_STREAM);

        at.setPlaybackRate(500);

        pcgGraph = findViewById(R.id.pcgGraph);
        pcgGraph.setMax(6000);
        pcgGraph.setColor(getColor(R.color.blue));
        ecgGraph = findViewById(R.id.ecgGraph);
        ecgGraph.setMax(1000);
        ecgGraph.setColor(getColor(R.color.blue));

        statusText = findViewById(R.id.status);
        tempText = findViewById(R.id.temp);
        hrText = findViewById(R.id.heartrate);
        batteryText = findViewById(R.id.batteryLvl);
        dataLossText = findViewById(R.id.dataLoss);
        FloatingActionButton fab = findViewById(R.id.fabTest);

        if (!folder.exists() && !folder.mkdir()) {
            Toast.makeText(this, "Unable to create a folder, Some functionality wont work!", Toast.LENGTH_LONG).show();
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

        scheduler = Executors.newScheduledThreadPool(1);
        //endregion

        Intent intent = getIntent();
        String action = intent.getStringExtra("from");

        if (action != null && action.equals("cloud")) {
            setEnterSharedElementCallback(new MaterialContainerTransformSharedElementCallback());
            getWindow().setSharedElementEnterTransition(customTransition());
            getWindow().setSharedElementReturnTransition(customTransition());
            cloudTest = (TestObject) getIntent().getSerializableExtra("testObject");
            downloadFromCloud();
            statusText.setText("DOWNLOADING FROM CLOUD");
            fab.setImageResource(R.drawable.icon_tick_45);
            fab.setOnClickListener(view -> finish());
        } else if (action != null) {
            if (action.equals("local")) {
                patient = (PatientObject) getIntent().getSerializableExtra("patientObject");
                fab.setOnClickListener(new UploadClickListener());
            } else if (action.equals("quickTest")) {
                fab.setImageResource(R.drawable.icon_tick_45);
                fab.setOnClickListener(view -> finish());
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

    private void downloadFromCloud() {
        FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference dataRef = storage.getReferenceFromUrl(cloudTest.getDataCloud());
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
        ref.getFile(file).addOnSuccessListener(taskSnapshot -> processFile()).addOnFailureListener(exception -> Toast.makeText(BluetoothActivity.this, exception.toString(), Toast.LENGTH_LONG).show());
    }

    private void processFile() {
        statusText.setOnClickListener(null);
        try {
            Scanner myReader = new Scanner(dataFile);
            threads[0] = new Thread(() -> {
                while (myReader.hasNextLine() && !threads[0].isInterrupted()) {
                    String data = myReader.nextLine();
                    String[] splited = data.split("\\s+");
                    processValues(Short.parseShort(splited[0]), Short.parseShort(splited[1]));
                    displayVal(tempText, (float) Integer.parseInt(splited[2]) / 10);
                }
                myReader.close();
                cloudEof = true;
            });

            threads[0].start();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void displayVal(final TextView textView, final Object val) {
        runOnUiThread(() -> textView.setText(String.valueOf(val)));
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
        threads[1] = new Thread(() -> {
            InputStream inStream;
            try {
                inStream = connectedSocket.getInputStream();
                runOnUiThread(() -> statusText.setText("BUFFERING DATA"));
                byte[] inData = new byte[1024];
                int read;
                while (!threads[1].isInterrupted()) {
                    read = inStream.read(inData);
                    byte[] extractedData = Arrays.copyOfRange(inData, 0, read);
                    dataBuffer.append(convertByteToString(extractedData));
                    runOnUiThread(() -> {
                        if (dataBuffer.length() >= 12) decodeDcip();
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

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
                        new Thread(() -> processValues(pcg, ecg)).start();
                    }
                }
            } else if (data.startsWith("02C503") & data.length() >= 12) {
                if (isChecksumOk(data.substring(0, 10), data.substring(10, 12))) {
                    wasPrevProcessed = true;
                    int battery = getDataFromHex(data.substring(6, 8), 2);
//                    int version = getDataFromHex(data.substring(8, 10), 2);
                    dataBuffer.delete(0, 12);
                    displayVal(batteryText, battery);
                    if (!getPcgSent) {
                        getPcgSent = true;
                        sendData("getPcg");
                    }
                }
            } else if (data.startsWith("02C403") & data.length() >= 12) {
                if (isChecksumOk(data.substring(0, 10), data.substring(10, 12))) {
                    wasPrevProcessed = true;
                    int temperature = getDataFromHex(data.substring(6, 10), 4);
                    dataBuffer.delete(0, 12);
                    displayVal(tempText, (float) temperature / 10);
                    writeToFile(tempOs, temperature);
                }
            } else {
                discardUntilNext();
            }
        }
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

    /**
     * Plots every 8th value, writes to audio track and writes to file
     *
     * @param pcg The final single processed value of pcg
     * @param ecg The final single processed value of ecg
     */
    private synchronized void processValues(final short pcg, final short ecg) {
        if (j % 8 == 0) {
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
        if (!plotting && pcgQ.size() > 100) {
            plotting = true;
            audioStartTime = System.currentTimeMillis();
            at.play();
            Runnable runnable = () -> runOnUiThread(() -> {
                if (pcgQ.size() > 0 && ecgQ.size() > 0) {
                    pcgGraph.addEntry(pcgQ.remove());
                    ecgGraph.addEntry(ecgQ.remove());

                    int elapsed = (int) (System.currentTimeMillis() - audioStartTime) / 1000;
                    int mins = Math.max(elapsed / 60, 0);
                    int secs = Math.max(elapsed % 60, 0);
                    final String minutes = mins < 10 ? "0" + mins : String.valueOf(mins);
                    final String seconds = secs < 10 ? "0" + secs : String.valueOf(secs);
                    statusText.setText(minutes + ":" + seconds);

                    i++;
                } else if (cloudEof) {
                    statusText.setText("Click me to play again!");
                    statusText.setOnClickListener((view) -> processFile());
                }
            });
            scheduler.scheduleWithFixedDelay(runnable, 20, 20, TimeUnit.MILLISECONDS);
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

            if (resultCode == RESULT_OK) startBluetoothService();
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
        ConstraintLayout layout = findViewById(R.id.loadingShower);
        ObjectAnimator oa4 = ObjectAnimator.ofFloat(layout, "alpha", 0, 1).setDuration(600);
        oa4.setStartDelay(600);
        oa4.setInterpolator(new DecelerateInterpolator());
        as.playTogether(oa, oa2, oa3, oa4);
        as.setInterpolator(new DecelerateInterpolator());
        as.start();

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
                        int temp = Integer.parseInt(Objects.requireNonNull(postSnapshot.getKey()));
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
        DatabaseReference db = database.getReference("users").child(u.getPref(this, "id")).child("patients").child(patient.getId()).child(id);

        Date date = new Date();
        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String strDate = formatter.format(date);
        LocalTime time = LocalTime.now();
        String strTime = time.format(DateTimeFormatter.ofPattern("HH:mm"));

        db.child("date").setValue(strDate);
        db.child("time").setValue(strTime);
        db.child("data").setValue(url).addOnSuccessListener(aVoid -> {
            Toast.makeText(BluetoothActivity.this, "Added to Database", Toast.LENGTH_LONG).show();
            BluetoothActivity.this.finish();
        });
    }

    private class UploadClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            ObjectAnimator.ofFloat(v, "alpha", 1, 0).setDuration(300).start();
            v.setClickable(false);
            showLoading();
            at.pause();
            if (threads[1] != null)
                threads[1].interrupt();
            if (threads[0] != null)
                threads[0].interrupt();
            if (threads[2] != null)
                threads[2].interrupt();

            new Thread(() -> {
                try {
                    ArrayList<String> lines = formatValues();
                    FileOutputStream dataOs = new FileOutputStream(dataFile);
                    for (String s : lines)
                        dataOs.write(s.getBytes());

                    dataOs.flush();
                    dataOs.close();

                    uploadToCloud();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
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
            final StorageReference dataFileRef = storageRef.child("data").child("patientId_" + patient.getId() + new Random().nextInt() + ".txt");
            UploadTask uploadTask = dataFileRef.putFile(file);

            uploadTask.addOnFailureListener(exception -> {
                Toast.makeText(BluetoothActivity.this, "Failed to upload to database " + exception.toString(), Toast.LENGTH_LONG).show();
                BluetoothActivity.this.finish();
            }).addOnSuccessListener(taskSnapshot -> dataFileRef.getDownloadUrl().addOnSuccessListener(downloadUrl -> decideIdAndAddToDb(downloadUrl.toString())));
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