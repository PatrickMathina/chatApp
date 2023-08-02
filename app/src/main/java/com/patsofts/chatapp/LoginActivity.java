package com.patsofts.chatapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    ProgressBar progressBar;
    EditText email, password;
    Button btn_login;
    TextView forgot_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar = findViewById(R.id.progressL);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        btn_login = findViewById(R.id.btn_login);
        forgot_password = findViewById(R.id.forgot_password);

        btn_login.setOnClickListener(v -> {
            String text_email = email.getText().toString().trim();
            String text_password = password.getText().toString().trim();

            if (TextUtils.isEmpty(text_email) || TextUtils.isEmpty(text_password)) {
                Toast.makeText(LoginActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
            } else {
                progressBar.setVisibility(View.VISIBLE);
                Context context = this;
                FirebaseAuth.getInstance().signInWithEmailAndPassword(text_email.trim(), text_password.trim())
                        .addOnSuccessListener(authResult -> {
                            Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            if (!isFinishing()) {
                                progressBar.setVisibility(View.GONE);
                                new AlertDialog.Builder(context)
                                        .setCancelable(true).setTitle("Error")
                                        .setMessage(e.getMessage())
                                        .setPositiveButton("Dismiss", null)
                                        .show();
                            }
                        });
            }
        });

        forgot_password.setOnClickListener(V -> {
            startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
        });
    }
}