package com.patsofts.chatapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.patsofts.chatapp.Models.UserModel;
import com.patsofts.chatapp.databinding.ActivityRegisterBinding;

import java.util.Objects;


public class RegisterActivity extends AppCompatActivity {
    ProgressBar progressBar;
    Button btn_Register;
    EditText user_name, user_email, user_password;
    String name, email, password;
    ActivityRegisterBinding binding;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar = findViewById(R.id.progressR);
        user_name = findViewById(R.id.registerName);
        user_email = findViewById(R.id.registerEmail);
        user_password = findViewById(R.id.registerPassword);
        btn_Register = findViewById(R.id.btnRegister);

        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        binding.btnRegister.setOnClickListener(view -> {
            name = Objects.requireNonNull(binding.registerName.getText()).toString().trim();
            email = Objects.requireNonNull(binding.registerEmail.getText()).toString().trim();
            password = Objects.requireNonNull(binding.registerPassword.getText()).toString().trim();
            signUp();
        });
    }

    private void signUp() {
        if (TextUtils.isEmpty(binding.registerName.getText().toString().trim()) ||
                TextUtils.isEmpty(binding.registerEmail.getText().toString().trim()) ||
                TextUtils.isEmpty(binding.registerPassword.getText().toString().trim())) {
            Toast.makeText(getApplicationContext(), "All fields are required", Toast.LENGTH_LONG).show();
        } else if (user_password.length() < 6) {
            Toast.makeText(getApplicationContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
        } else {
            progressBar.setVisibility(View.VISIBLE);
            Context context = this;
            FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(email.trim(), password)
                    .addOnSuccessListener(authResult -> {
                        UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        assert firebaseUser != null;
                        String userId = firebaseUser.getUid();
                        firebaseUser.updateProfile(userProfileChangeRequest);
                        UserModel userModel = new UserModel(userId, name, email, password, "default", "offline", name.toLowerCase());
                        databaseReference.child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).setValue(userModel);
                        Toast.makeText(RegisterActivity.this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        new AlertDialog.Builder(context)
                                .setCancelable(true).setTitle("Error")
                                .setMessage(e.getMessage())
                                .setPositiveButton("Dismiss", (dialog, which) -> {
                                    dialog.dismiss();
                                }).show();
                    });
        }
    }

}


//public class RegisterActivity extends AppCompatActivity {
//
//    ProgressBar progressBar;
//    EditText userName, email, password;
//    Button btn_Register;
//
//    FirebaseAuth firebaseAuth;
//    DatabaseReference databaseReference;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_register);
//
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        Objects.requireNonNull(getSupportActionBar()).setTitle("Register");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//
//        progressBar = findViewById(R.id.progressR);
//        userName = findViewById(R.id.username);
//        email = findViewById(R.id.email);
//        password = findViewById(R.id.password);
//        btn_Register = findViewById(R.id.btn_register);
//
////        firebaseAuth = firebaseAuth.getInstance();
//
//        btn_Register.setOnClickListener(v -> {
//            String text_username = userName.getText().toString();
//            String text_email = email.getText().toString();
//            String text_password = password.getText().toString();
//
//            if (TextUtils.isEmpty(text_username) || TextUtils.isEmpty(text_email) || TextUtils.isEmpty(text_password)) {
//                Toast.makeText(RegisterActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
//            } else if (text_password.length() < 6) {
//                Toast.makeText(RegisterActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
//            } else {
//                register(text_username, text_email, text_password);
//            }
//        });
//    }
//
//    private void register(String username, String email, String password) {
//        progressBar.setVisibility(View.VISIBLE);
//        Context context = this;
//        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
//                .addOnSuccessListener(authResult -> {
//                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//                    assert firebaseUser != null;
//                    String userId = firebaseUser.getUid();
//                    databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
//
//                    HashMap<String, String> hashMap = new HashMap<>();
//                    hashMap.put("id", userId);
//                    hashMap.put("userName", username);
//                    hashMap.put("email", email);
//                    hashMap.put("password", password);
//                    hashMap.put("imageURL", "default");
//                    Toast.makeText(RegisterActivity.this, "Registered Successfully", Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);
//                    finish();
//
//                }).addOnFailureListener(e -> {
//                    progressBar.setVisibility(View.GONE);
//                    new AlertDialog.Builder(context)
//                            .setCancelable(true).setTitle("ERROR")
//                            .setMessage(e.getMessage())
//                            .setPositiveButton("Dismiss", (dialog, which) -> {
//                                dialog.dismiss();
//                            }).show();
//                });
//
//
//    }
//
//
//}