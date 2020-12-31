package com.cibiod.app.Activities;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.cibiod.app.CustomViews.CustomSpinner;
import com.cibiod.app.R;
import com.cibiod.app.Utils.u;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class ForumActivity extends AppCompatActivity {

    private EditText nameText, ageText;
    private CustomSpinner genderDropdown;
    private ImageView closeIcon;
    private String pictureImagePath;
    private Button addButton;

    private int rotationToBeDone = 0;
    private boolean rotating;
    private volatile boolean rotationDone = false;
    private CountDownLatch latch = new CountDownLatch(1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum);
        u.sharedTransFix(getWindow(), R.color.transparent);
        Window window = getWindow();
        window.setSharedElementEnterTransition(enterTransition());
        window.setSharedElementReturnTransition(returnTransition());
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getColor(R.color.orangeDark));

        nameText = findViewById(R.id.addNameEdit);
        final ImageView nameRect = findViewById(R.id.addNameRect);

        ageText = findViewById(R.id.addAgeEdit);
        final ImageView ageRect = findViewById(R.id.addAgeRect);

        u.setupEditText(this, nameText, nameRect, "NAME", false);
        u.setupEditText(this, ageText, ageRect, "AGE", false);

        genderDropdown = findViewById(R.id.addGenderSpinner);
        final ImageView genderPopupBg = findViewById(R.id.addGenderPopupBg);
        final ImageView genderDropdownArrow = findViewById(R.id.addGenderDropdownArrow);

        u.setupDropdown(this, genderDropdown, R.array.Gender, genderPopupBg, genderDropdownArrow);

        ObjectAnimator oa = ObjectAnimator.ofFloat(findViewById(R.id.addRoot), "alpha", 0, 1).setDuration(300);
        oa.setStartDelay(1000);
        oa.setInterpolator(new AccelerateInterpolator());
        oa.start();

        closeIcon = findViewById(R.id.closeAddButton);
        final RotateAnimation aRotate = new RotateAnimation(0, 135,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        aRotate.setStartOffset(1300);
        aRotate.setDuration(600);
        aRotate.setFillAfter(true);
        aRotate.setInterpolator(new AnticipateOvershootInterpolator());
        closeIcon.startAnimation(aRotate);

        closeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });

        Button photoButton = findViewById(R.id.photoButton);
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPhoto();
            }
        });

        addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String nameVal = nameText.getText().toString().toLowerCase();
                final String ageVal = ageText.getText().toString();
                final String genderVal = genderDropdown.getSelectedItem().toString();
                if (nameVal.isEmpty()) {
                    nameText.setError("Name is Empty");
                    return;
                }
                if (ageVal.isEmpty()) {
                    ageText.setError("Age is Empty");
                    return;
                }
                if (genderVal.equals("GENDER")) {
                    Toast.makeText(ForumActivity.this, "Select a gender", Toast.LENGTH_LONG).show();
                    return;
                }
                if (pictureImagePath == null) {
                    Toast.makeText(ForumActivity.this, "Add a photo", Toast.LENGTH_LONG).show();
                    return;
                } else {
                    if (!new File(pictureImagePath).exists()) {
                        Toast.makeText(ForumActivity.this, "Add a photo", Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference dbRef = database.getReference("users");
                final Query idGetter = dbRef.child(u.getPref(ForumActivity.this, "id")).child("patients").limitToLast(1).orderByKey();

                addButton.setClickable(false);
                ObjectAnimator.ofFloat(addButton, "alpha", 1, 0).setDuration(300).start();

                idGetter.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        idGetter.removeEventListener(this);
                        dbRef.removeEventListener(this);
                        String id = null;
                        if (snapshot.hasChildren()) {
                            for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                int temp = Integer.parseInt(Objects.requireNonNull(postSnapshot.getKey()));
                                id = Integer.toString(++temp);
                            }
                        } else
                            id = Integer.toString(1);

                        try {
                            addToDb(dbRef, nameVal, ageVal, genderVal, id);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(), "Firebase Connection Error", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void goBack() {
        RotateAnimation aRotate = new RotateAnimation(135, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        aRotate.setDuration(600);
        aRotate.setFillAfter(true);
        aRotate.setInterpolator(new AnticipateOvershootInterpolator());
        closeIcon.startAnimation(aRotate);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                ForumActivity.this.onBackPressed();
            }
        };
        new Handler().postDelayed(r, 800);
    }

    void addToDb(final DatabaseReference dbRef, String name, final String age, final String gender, final String id) throws InterruptedException {
        String[] split = name.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String s : split) {
            String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
            builder.append(cap).append(" ");
        }
        builder.deleteCharAt(builder.length() - 1);
        name = builder.toString();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        Uri file = Uri.fromFile(new File(pictureImagePath));
        final StorageReference photoRef = storageRef.child("patientPics/" + file.getLastPathSegment());
        UploadTask uploadTask = photoRef.putFile(file);

        final String finalName = name;

        latch.await();
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(ForumActivity.this, "Failed, Try again later!", Toast.LENGTH_LONG).show();
                addButton.setClickable(true);
                ObjectAnimator.ofFloat(addButton, "alpha", 0, 1).setDuration(300).start();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri downloadUrl) {
                        final String userId = u.getPref(ForumActivity.this, "id");
                        dbRef.child(userId).child("patients").child(id).child("photo").setValue(downloadUrl.toString());
                        dbRef.child(userId).child("patients").child(id).child("name").setValue(finalName);
                        dbRef.child(userId).child("patients").child(id).child("age").setValue(age);
                        dbRef.child(userId).child("patients").child(id).child("gender").setValue(gender);
                        dbRef.child(userId).child("patients").child(id).child("lowercase").setValue(finalName.toLowerCase()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(ForumActivity.this);
                                Bundle bundle = new Bundle();
                                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, userId);
                                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, finalName);
                                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                                Toast.makeText(ForumActivity.this, "Added to Database", Toast.LENGTH_LONG).show();
                                goBack();

                            }
                        });
                    }
                });
            }
        });
    }

    private void getPhoto() {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + ".jpg";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        pictureImagePath = storageDir.getAbsolutePath() + "/" + imageFileName;
        File file = new File(pictureImagePath);
        Uri outputFileUri = Uri.fromFile(file);
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(takePictureIntent, 90);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 90 && resultCode == RESULT_OK) {
            File imgFile = new File(pictureImagePath);
            if (imgFile.exists()) {
                final Bitmap imageBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                final ImageView image = findViewById(R.id.patientPhoto);
                image.setImageBitmap(imageBitmap);

                final View layout = findViewById(R.id.photoLayout);
                layout.setAlpha(1);

                rotating = false;

                ImageButton rotateLeft = findViewById(R.id.rotateLeftButton);
                rotateLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!rotating) {
                            rotating = true;
                            ObjectAnimator oa;
                            oa = ObjectAnimator.ofFloat(image, "rotation", image.getRotation(), image.getRotation() - 90).setDuration(300);
                            oa.start();
                            oa.addListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    rotating = false;
                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {

                                }

                                @Override
                                public void onAnimationRepeat(Animator animation) {

                                }
                            });
                            rotationToBeDone -= 90;
                        }
                    }
                });

                ImageButton rotateRight = findViewById(R.id.rotateRightButton);

                rotateRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!rotating) {
                            rotating = true;
                            ObjectAnimator oa;
                            oa = ObjectAnimator.ofFloat(image, "rotation", image.getRotation(), image.getRotation() + 90).setDuration(300);
                            oa.start();
                            oa.addListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    rotating = false;
                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {

                                }

                                @Override
                                public void onAnimationRepeat(Animator animation) {

                                }
                            });
                            rotationToBeDone += 90;
                        }
                    }
                });

                ImageButton confirmPhoto = findViewById(R.id.confirmPhotoButton);
                confirmPhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ObjectAnimator.ofFloat(layout, "alpha", 1, 0).setDuration(300).start();

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Bitmap finalImg = RotateBitmap(imageBitmap, rotationToBeDone);
                                try (FileOutputStream out = new FileOutputStream(pictureImagePath)) {
                                    finalImg.compress(Bitmap.CompressFormat.PNG, 100, out);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                rotationDone = true;
                                latch.countDown();
                            }
                        }).start();
                    }
                });

            } else {
                Toast.makeText(ForumActivity.this, "Can not detect photo!", Toast.LENGTH_LONG).show();
            }
        }
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private Transition enterTransition() {
        ChangeBounds bounds = new ChangeBounds();
        bounds.setInterpolator(new AccelerateDecelerateInterpolator());
        bounds.setDuration(800);

        return bounds;
    }

    private Transition returnTransition() {
        ChangeBounds bounds = new ChangeBounds();
        bounds.setInterpolator(new AccelerateDecelerateInterpolator());
        bounds.setDuration(800);

        return bounds;
    }
}