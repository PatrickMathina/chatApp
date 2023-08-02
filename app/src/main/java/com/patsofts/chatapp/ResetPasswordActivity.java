package com.patsofts.chatapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class ResetPasswordActivity extends AppCompatActivity {

    ProgressBar progressBar;
    EditText send_email;
    Button btn_reset;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Reset Password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar = findViewById(R.id.progressL);
        send_email = findViewById(R.id.send_email);
        btn_reset = findViewById(R.id.btn_reset);

        firebaseAuth = FirebaseAuth.getInstance();

        btn_reset.setOnClickListener(V -> {
            String email = send_email.getText().toString().trim();

            if (email.isEmpty()) {  //email.equals("")
                Toast.makeText(ResetPasswordActivity.this, "Please enter an email address", Toast.LENGTH_SHORT).show();
            } else {
                progressBar.setVisibility(View.VISIBLE);
                Context context = this;
                firebaseAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                progressBar.setVisibility(View.GONE);
                                btn_reset.setEnabled(false);
                                new AlertDialog.Builder(context)
                                        .setCancelable(true)
                                        .setTitle("Password reset email sent successfully!")
                                        .setMessage("Please check your email to set up a new password")
                                        .setPositiveButton("Check Email", (dialog, which) -> {
                                            openGmail();
                                        })
                                        .setNegativeButton("Dismiss", (dialog, which) -> {
                                            dialog.dismiss();
//                                            startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class));
//                                            finish();
                                        })
                                        .show();

                            } else {
                                progressBar.setVisibility(View.GONE);
                                String error = Objects.requireNonNull(task.getException()).getMessage();
                                new AlertDialog.Builder(context)
                                        .setCancelable(true)
                                        .setTitle("Error")
                                        .setMessage(error)
                                        .setPositiveButton("Dismiss", (dialog, which) -> {
                                            dialog.dismiss();
                                        })
                                        .show();
                            }
                        });
            }


        });
    }

    private void openGmail() {
        // Get the package manager
        PackageManager packageManager = getPackageManager();

        // Create an intent with the Gmail package name
        Intent intent = packageManager.getLaunchIntentForPackage("com.google.android.gm");

        if (intent != null) {
            // Gmail app exists, open it
            startActivity(intent);
        } else {
            // Gmail app is not installed, open Gmail website in browser
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://mail.google.com"));
            startActivity(intent);
        }
    }
}