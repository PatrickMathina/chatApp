package com.patsofts.chatapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.patsofts.chatapp.Fragments.ChatsFragment;
import com.patsofts.chatapp.Fragments.FriendsFragment;
import com.patsofts.chatapp.Fragments.GroupsFragment;
import com.patsofts.chatapp.Fragments.StatusFragment;
import com.patsofts.chatapp.Fragments.UsersFragment;
import com.patsofts.chatapp.Models.MessageModel;
import com.patsofts.chatapp.Models.UserModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    CircleImageView profileImage;
    TextView userName, loading_chats;
    ProgressBar progressBar, progressBar_toolbar;
    private FirebaseUser firebaseUser;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");

        profileImage = findViewById(R.id.profile_image);
        userName = findViewById(R.id.username);
        loading_chats = findViewById(R.id.loading_chat);
        progressBar = findViewById(R.id.proB);
        progressBar_toolbar = findViewById(R.id.pro_toolbar);

        profileImage.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), ProfileActivity.class)));

        userName.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), ProfileActivity.class)));

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid());

//        databaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                UserModel userModel = snapshot.getValue(UserModel.class);
//                assert userModel != null;
//                userName.setText(userModel.getUserName());
//                if (userModel.getImageURL().equals("default")) {
//                    profileImage.setImageResource(R.mipmap.ic_launcher);
//                } else {
//                    Glide.with(MainActivity.this).load(userModel.getImageURL()).into(profileImage);
//
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) {
                    // Activity is finishing or destroyed, don't proceed with Glide operation
                    return;
                }

                progressBar.setVisibility(View.GONE);
                progressBar_toolbar.setVisibility(View.GONE);
                loading_chats.setVisibility(View.GONE);
                UserModel userModel = snapshot.getValue(UserModel.class);
                assert userModel != null;
                userName.setText(userModel.getUserName());
                if (userModel.getImageURL().equals("default")) {
                    profileImage.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(MainActivity.this).load(userModel.getImageURL()).into(profileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled() method if needed
//                Toast.makeText(MainActivity.this, "", Toast.LENGTH_LONG).show();
            }
        });

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager viewPager = findViewById(R.id.view_pager);

//        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
//
//        viewPagerAdapter.addFragment(new ChatsFragment(), "Chats");
//        viewPagerAdapter.addFragment(new GroupsFragment(), "Groups");
//        viewPagerAdapter.addFragment(new StatusFragment(), "Status");
//        viewPagerAdapter.addFragment(new FriendsFragment(), "Friends");
//        viewPagerAdapter.addFragment(new UsersFragment(), "Users");
//
//        viewPager.setAdapter(viewPagerAdapter);
//        tabLayout.setupWithViewPager(viewPager);

        databaseReference = FirebaseDatabase.getInstance().getReference("chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // count users with unread messages
                HashSet<String> uniqueSenders = new HashSet<>();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    MessageModel messageModel = dataSnapshot.getValue(MessageModel.class);
                    if (messageModel.getReceiverId().equals(firebaseUser.getUid()) && !messageModel.isIsseen()) {
                        uniqueSenders.add(messageModel.getSenderId());
                    }
                }

                int unread_msg = uniqueSenders.size();

                ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

                // Add the "Chats" fragment without the count if unread_msg is 0
                if (unread_msg == 0) {
                    viewPagerAdapter.addFragment(new ChatsFragment(), "Chats");
                } else {
                    viewPagerAdapter.addFragment(new ChatsFragment(), "Chats (" + unread_msg + ")");
                }

                viewPagerAdapter.addFragment(new GroupsFragment(), "Groups");
                viewPagerAdapter.addFragment(new StatusFragment(), "Status");
                viewPagerAdapter.addFragment(new FriendsFragment(), "Friends");
                viewPagerAdapter.addFragment(new UsersFragment(), "Users");

                viewPager.setAdapter(viewPagerAdapter);
                tabLayout.setupWithViewPager(viewPager);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled() method if needed
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu1, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.logout:
//                FirebaseAuth.getInstance().signOut();
//                FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
//                    @Override
//                    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//                        if (firebaseAuth.getCurrentUser() == null) {
//                            startActivity(new Intent(getApplicationContext(), StartActivity.class));
//                            finish();
//                            overridePendingTransition(0, 0); // remove transition animation
//                        }
//                    }
//                });


//                FirebaseAuth.getInstance().signOut();
//                startActivity(new Intent(getApplicationContext(), StartActivity.class));
//                finish();


                if (firebaseUser != null) {
                    Context context = this;

                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (isFinishing() || isDestroyed()) {
                                // Activity is finishing or destroyed, don't proceed with Glide operation
                                return;
                            }

                            UserModel userModel = snapshot.getValue(UserModel.class);
                            assert userModel != null;
                            new AlertDialog.Builder(context)
                                    .setCancelable(false)
//                                    .setTitle("Logout")
                                    .setMessage(userModel.getUserName() + ", are your sure to logout?")
                                    .setPositiveButton("Logout", (dialog, which) -> {

                                        status("offline");
                                        FirebaseAuth.getInstance().signOut();
                                        FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
                                            @Override
                                            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                                                if (firebaseAuth.getCurrentUser() == null) {
                                                    startActivity(new Intent(getApplicationContext(), StartActivity.class));
                                                    finish();
//                                                    overridePendingTransition(0, 0); // remove transition animation
                                                }
                                            }
                                        });

                                    })
                                    .setNegativeButton("Cancel", (dialog, which) -> {
                                        dialog.dismiss();
                                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle onCancelled() method if needed
                            Toast.makeText(MainActivity.this, "_-_", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                break;

            case R.id.delete_account:
                if (firebaseUser != null) {
                    Context context = this;

                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (isFinishing() || isDestroyed()) {
                                // Activity is finishing or destroyed, don't proceed with Glide operation
                                return;
                            }

                            UserModel userModel = snapshot.getValue(UserModel.class);
                            assert userModel != null;
                            new AlertDialog.Builder(context)
                                    .setCancelable(false)
                                    .setTitle("Delete Account")
                                    .setMessage(userModel.getUserName() + ", are your sure to delete account " + userModel.getUserEmail() + "?\nThis action cannot be undone.")
                                    .setPositiveButton("Delete", (dialog, which) -> {
                                        status("offline");
                                        firebaseUser.delete()
                                                .addOnCompleteListener(task -> {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(MainActivity.this, "Account deleted", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(getApplicationContext(), StartActivity.class));
                                                        finish();
                                                    } else {
//                                                    Toast.makeText(MainActivity.this, "Failed to delete account", Toast.LENGTH_SHORT).show();
                                                        String error = Objects.requireNonNull(task.getException()).getMessage();
                                                        new AlertDialog.Builder(context)
                                                                .setCancelable(true)
                                                                .setTitle("Error")
                                                                .setMessage(error)
                                                                .setPositiveButton("Dismiss", (dialog1, which1) -> {
                                                                    dialog1.dismiss();
                                                                })
                                                                .show();
                                                    }
                                                });
                                    })
                                    .setNegativeButton("Cancel", (dialog, which) -> {
                                        dialog.dismiss();
                                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle onCancelled() method if needed
                        }
                    });


//                    new AlertDialog.Builder(context)
//                            .setCancelable(true)
//                            .setTitle("Delete Account")
//                            .setMessage(userModel.getUserName() + ", are your sure to delete account " + userModel.getUserEmail() + "? This action cannot be undone.")
//                            .setPositiveButton("Delete", (dialog, which) -> {
//                                firebaseUser.delete()
//                                        .addOnCompleteListener(task -> {
//                                            if (task.isSuccessful()) {
//                                                Toast.makeText(MainActivity.this, "Account deleted", Toast.LENGTH_SHORT).show();
//                                                startActivity(new Intent(getApplicationContext(), StartActivity.class));
//                                                finish();
//                                            } else {
////                                                    Toast.makeText(MainActivity.this, "Failed to delete account", Toast.LENGTH_SHORT).show();
//                                                String error = Objects.requireNonNull(task.getException()).getMessage();
//                                                new AlertDialog.Builder(context)
//                                                        .setCancelable(true)
//                                                        .setTitle("Error")
//                                                        .setMessage(error)
//                                                        .setPositiveButton("Dismiss", (dialog1, which1) -> {
//                                                            dialog1.dismiss();
//                                                        })
//                                                        .show();
//                                            }
//                                        });
//                            })
//                            .setNegativeButton("Cancel", (dialog, which) -> {
//                                dialog.dismiss();
//                            })
//                            .show();


                }
//                else {
//                    // User is not currently signed in
//                    Toast.makeText(MainActivity.this, "User is not signed in", Toast.LENGTH_SHORT).show();
//                }

                break;

            case R.id.restart:
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
        }
        return false;
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        ViewPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }

        //        ctr+o
        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

    private void status(String status) {
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        databaseReference.updateChildren(hashMap);
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