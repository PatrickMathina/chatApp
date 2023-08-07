package com.patsofts.chatapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.patsofts.chatapp.Models.UserModel;

import java.util.HashMap;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    ImageView image_profile, image_pencil, image_pencil_bio;
    ProgressBar progress_profile, progress_load_p_Image;
    EditText username_edit, bio_edit;
    TextView email, cant_be_empty;

    DatabaseReference dReference;
    FirebaseUser fUser;

    StorageReference storageReference;
    private static final int IMAGE_REQUEST = 1;
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 2;
    private Uri image_uri;
    private StorageTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });

        image_profile = findViewById(R.id.profile_image);
        progress_profile = findViewById(R.id.progress_profile);
        progress_load_p_Image = findViewById(R.id.progress_load_p_Image);
        cant_be_empty = findViewById(R.id.not_be_empty);
        image_pencil = findViewById(R.id.pencil);
        image_pencil_bio = findViewById(R.id.pencil_bio);
        username_edit = findViewById(R.id.username_edit);
        bio_edit = findViewById(R.id.bio_edit);
        email = findViewById(R.id.email);

        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        fUser = FirebaseAuth.getInstance().getCurrentUser();
        dReference = FirebaseDatabase.getInstance().getReference("users").child(fUser.getUid());

        dReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) {
                    // Activity is finishing or destroyed, don't proceed with Glide operation
                    return;
                }
                UserModel userModel = snapshot.getValue(UserModel.class);

                assert userModel != null;
                progress_profile.setVisibility(View.GONE);
                username_edit.setText(userModel.getUserName());
                email.setText(userModel.getUserEmail());
                if (userModel.getImageURL().equals("default")) {
                    image_profile.setImageResource(R.mipmap.ic_launcher);
                    progress_load_p_Image.setVisibility(View.GONE);
                } else {
                    Glide.with(ProfileActivity.this).load(userModel.getImageURL()).into(image_profile);
                    progress_load_p_Image.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        username_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String name = username_edit.getText().toString().trim();
                if (s.length() > 0 && s.length() <= 30 && !name.equals("")) {
                    image_pencil.setVisibility(View.VISIBLE);
                    cant_be_empty.setVisibility(View.GONE);
                }  else {
                    image_pencil.setVisibility(View.GONE);
                    cant_be_empty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        bio_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                image_pencil_bio.setVisibility(View.VISIBLE);

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        image_profile.setOnClickListener(v -> {
            checkPermission();
        });
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            selectImage();
        } else {
            ActivityCompat.requestPermissions(ProfileActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
        }
    }

    private void selectImage() {
        Toast.makeText(getApplicationContext(), "Select profile image", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Permission allowed", Toast.LENGTH_SHORT).show();
            selectImage();
        } else {
            Toast.makeText(getApplicationContext(), "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }


    private void uploadImage(Uri url) {

        if (image_uri != null) {
            final ProgressDialog pd = new ProgressDialog(ProfileActivity.this);
            pd.setTitle("Updating your dp");
            pd.show();

            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    + "." + getFileExtension(image_uri));

            uploadTask = fileReference.putFile(url)
                    .addOnSuccessListener(taskSnapshot -> {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isComplete()) ;
                        Uri uri = uriTask.getResult();
                        String mUri = uri.toString();

                        dReference = FirebaseDatabase.getInstance().getReference("users").child(fUser.getUid());
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("imageURL", mUri);
                        dReference.updateChildren(map);

                        pd.dismiss();


                    })
                    .addOnProgressListener(snapshot -> {
                        double percent = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        pd.setMessage("Updated: " + (int) percent + "%");
                    })
                    .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show());

        } else {
            Toast.makeText(getApplicationContext(), "No image selected", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            image_uri = data.getData();

            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(getApplicationContext(), "Update in progress", Toast.LENGTH_SHORT).show();
            } else {
                uploadImage(image_uri);
            }
        }
    }


    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void status(String status) {
        dReference = FirebaseDatabase.getInstance().getReference("users").child(fUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        dReference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }
}