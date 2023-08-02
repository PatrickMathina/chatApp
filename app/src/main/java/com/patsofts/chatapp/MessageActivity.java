package com.patsofts.chatapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.patsofts.chatapp.Adapters.MessageAdapter;
import com.patsofts.chatapp.Fragments.API_svs;
import com.patsofts.chatapp.Models.MessageModel;
import com.patsofts.chatapp.Models.UserModel;
import com.patsofts.chatapp.Notification.Client;
import com.patsofts.chatapp.Notification.Data;
import com.patsofts.chatapp.Notification.MyResponse;
import com.patsofts.chatapp.Notification.Sender;
import com.patsofts.chatapp.Notification.Token;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity implements View.OnCreateContextMenuListener {

    CircleImageView profile_image, status;
    TextView username;
    FirebaseUser fUser;
    DatabaseReference dReference;
    ImageButton btn_send;
    EditText text_send;
    MessageAdapter messageAdapter;
    List<MessageModel> messageModelList;
    RecyclerView recyclerView;
    Intent intent;

    ValueEventListener seenListener;

    String userid;

    API_svs api_svs;

    boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(v ->
//                        finish()
                        startActivity(new Intent(MessageActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        );

        api_svs = Client.getRetrofit("https://fcm.googleapis.com/").create(API_svs.class);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.text_send);
        status = findViewById(R.id.img_on);

        intent = getIntent();
        userid = intent.getStringExtra("userid");
        fUser = FirebaseAuth.getInstance().getCurrentUser();

        text_send.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Check if the text is empty
                if (s.length() > 0) {
                    btn_send.setVisibility(View.VISIBLE);
                } else {
                    btn_send.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btn_send.setOnClickListener(v -> {
            notify = true;
            String msg = text_send.getText().toString().trim();
            if (!msg.equals("")) {
                String time = DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date());
                sendMessage(fUser.getUid(), userid, msg, time);
            } else {
                Toast.makeText(MessageActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
            }
            text_send.setText("");
        });


        dReference = FirebaseDatabase.getInstance().getReference("users").child(userid);

        dReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) {
                    // Activity is finishing or destroyed, don't proceed with Glide operation
                    return;
                }

                UserModel userModel = snapshot.getValue(UserModel.class);
                if (userModel != null) {
                    username.setText(userModel.getUserName());
                    if (userModel.getImageURL().equals("default")) {
                        profile_image.setImageResource(R.mipmap.ic_launcher);
                    } else {
                        Glide.with(MessageActivity.this).load(userModel.getImageURL()).into(profile_image);
                    }

                    if (userModel.getStatus().equals("online")) {
                        status.setVisibility(View.VISIBLE);
                    } else {
                        status.setVisibility(View.INVISIBLE);
                    }
                }

                MessageModel messageModel = snapshot.getValue(MessageModel.class);
                if (messageModel != null) {
                    readMessage(fUser.getUid(), userid, userModel.getImageURL());
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        seenMessage(userid);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) { //from MessageAdapter onCreateContextMenu
        // Handle context menu item selection
        switch (item.getItemId()) {
            case 1: //copy
                Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show();

                return true;
            case 2:
                Toast.makeText(this, "Forwarded", Toast.LENGTH_SHORT).show();


                return true;
            case 3:


                Context context = this;

                dReference = FirebaseDatabase.getInstance().getReference("users").child(userid);
                dReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (isFinishing() || isDestroyed()) {
                            // Activity is finishing or destroyed, don't proceed with Glide operation
                            return;
                        }

                        UserModel userModel = snapshot.getValue(UserModel.class);
                        assert userModel != null;
                        new AlertDialog.Builder(context)
                                .setCancelable(true)
                                .setMessage("Delete this message with " + userModel.getUserName() + "?")
                                .setPositiveButton("Delete for everyone", (dialog, which) -> {
//                                    DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("chats").child(messageId);


                                })
                                .setNegativeButton("Cancel", (dialog, which) -> {
                                    dialog.dismiss();
//                                    Intent intent = new Intent(MessageActivity.this, MessageActivity.class);
//                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                                    startActivity(intent);
//                                    finish();
                                })
                                .show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle onCancelled() method if needed
                    }
                });


                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void seenMessage(String userid) {
        dReference = FirebaseDatabase.getInstance().getReference("chats");
        seenListener = dReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    MessageModel messageModel = dataSnapshot.getValue(MessageModel.class);
                    if (messageModel != null && messageModel.getReceiverId() != null && fUser != null && fUser.getUid() != null) {
                        if (messageModel.getReceiverId().equals(fUser.getUid()) && messageModel.getSenderId().equals(userid)) {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("isseen", true);
                            dataSnapshot.getRef().updateChildren(hashMap);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void sendMessage(String senderId, String receiverId, String message, String timeStamp) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("senderId", senderId);
        hashMap.put("receiverId", receiverId);
        hashMap.put("message", message);
        hashMap.put("isseen", false);
        hashMap.put("timeStamp", timeStamp);

        databaseReference.child("chats").push().setValue(hashMap);

//       final DatabaseReference chatReference=FirebaseDatabase.getInstance().getReference("chatList")
//                .child(fUser.getUid())
//                .child(receiverId);
//
//       chatReference.addListenerForSingleValueEvent(new ValueEventListener() {
//           @Override
//           public void onDataChange(@NonNull DataSnapshot snapshot) {
//               if (!snapshot.exists()) {
//                   chatReference.child("id").setValue(receiverId);
//               }
//           }
//
//           @Override
//           public void onCancelled(@NonNull DatabaseError error) {
//
//           }
//       });

        final String msg = message;
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(fUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel userModel = snapshot.getValue(UserModel.class);
                if (notify) {
                    sendNotification(receiverId, userModel.getUserName(), msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendNotification(String receiver, String username, String message) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Token token = dataSnapshot.getValue(Token.class);
                    Data data = new Data(fUser.getUid(), username + ": " + message, "New Message",
                            userid, R.mipmap.ic_launcher);

                    Sender sender = new Sender(data, token.getToken());

                    api_svs.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success != 1) {
                                            Toast.makeText(MessageActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readMessage(final String myid, String userid, String imageurl) {
        messageModelList = new ArrayList<>();

        dReference = FirebaseDatabase.getInstance().getReference("chats");
        dReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    messageModelList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        if (dataSnapshot != null) {
                            MessageModel messageModel = dataSnapshot.getValue(MessageModel.class);
                            if (messageModel != null && (messageModel.getReceiverId().equals(myid) && messageModel.getSenderId().equals(userid)) ||
                                    (messageModel.getReceiverId().equals(userid) && messageModel.getSenderId().equals(myid))) {
                                messageModelList.add(messageModel);
                            }
                        }
                    }

                    messageAdapter = new MessageAdapter(MessageActivity.this, messageModelList, imageurl);
                    recyclerView.setAdapter(messageAdapter);
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void currentUser(String user_id) {
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentUser", user_id);
        editor.apply();
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
//        dReference.addValueEventListener(seenListener);
        status("online");
        currentUser(userid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        dReference.removeEventListener(seenListener);
        status("offline");
        currentUser("none");
    }
}