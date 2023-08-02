package com.patsofts.chatapp.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.patsofts.chatapp.Adapters.ChatAdapter;
import com.patsofts.chatapp.Models.MessageModel;
import com.patsofts.chatapp.Models.UserModel;
import com.patsofts.chatapp.Notification.Token;
import com.patsofts.chatapp.R;

import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends Fragment {

    private RecyclerView recyclerView;
    TextView textView_no_chats;
    ProgressBar progressBar;
    private ChatAdapter chatAdapter;
    private List<UserModel> userModelList; //mUser

    FirebaseUser fUser;
    DatabaseReference dReference;

    private List<String> usersList;
//    private List<ChatListModel> usersList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        progressBar=view.findViewById(R.id.progress_loading);
        textView_no_chats=view.findViewById(R.id.no_chats);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fUser = FirebaseAuth.getInstance().getCurrentUser();

        usersList = new ArrayList<>();

//        dReference = FirebaseDatabase.getInstance().getReference("chatList").child(fUser.getUid());
//        dReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                usersList.clear();
//                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                    ChatListModel chatListModel = dataSnapshot.getValue(ChatListModel.class);
//                    usersList.add(chatListModel);
//                }
//
//                display_chatList();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });

        dReference = FirebaseDatabase.getInstance().getReference("chats");
        dReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();

//                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                    MessageModel messageModel = dataSnapshot.getValue(MessageModel.class);
//
//                    assert messageModel != null;
//                    if (messageModel.getSenderId().equals(fUser.getUid())) {
//                        usersList.add(messageModel.getReceiverId());
//                    }
//                    if (messageModel.getReceiverId().equals(fUser.getUid())) {
//                        usersList.add(messageModel.getSenderId());
//                    }
//                }

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    MessageModel messageModel = dataSnapshot.getValue(MessageModel.class);

                    if (messageModel != null) {
                        String senderId = messageModel.getSenderId();
                        String receiverId = messageModel.getReceiverId();

                        if (senderId != null && senderId.equals(fUser.getUid())) {
                            usersList.add(receiverId);
                        }
                        if (receiverId != null && receiverId.equals(fUser.getUid())) {
                            usersList.add(senderId);
                        }
                    }
                }

                readChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        updateToken(FirebaseInstanceId.getInstance().getToken());
        updateToken();

        return view;
    }


//   private void updateToken(String token) {
//        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("tokens");
//       Token token1=new Token(token);
//       reference.child(fUser.getUid()).setValue(token1);
//   }

    private void updateToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String token = task.getResult();
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("tokens");
                        Token token1 = new Token(token);
                        reference.child(fUser.getUid()).setValue(token1);
                    }
                });
    }

//    private void display_chatList() {
//        userModelList = new ArrayList<>();
//        dReference = FirebaseDatabase.getInstance().getReference("users");
//        dReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                userModelList.clear();
//                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                    UserModel userModel = dataSnapshot.getValue(UserModel.class);
//                    for (ChatListModel chatListModel : usersList) {
//                        if (userModel.getUserId().equals(chatListModel.getId())) {
//                            userModelList.add(userModel);
//                        }
//                    }
//                }
//                chatAdapter = new ChatAdapter(getContext(), userModelList, true);
//                recyclerView.setAdapter(chatAdapter);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }

//    private void readChats() {
//        userModelList = new ArrayList<>();
//        dReference = FirebaseDatabase.getInstance().getReference("users");
//        dReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                userModelList.clear();
//
//                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                    UserModel userModel = dataSnapshot.getValue(UserModel.class);
//
////                    display 1 user from chats
//                    for (String id : usersList) {
//                        assert userModel != null;
//                        if (userModel.getUserId().equals(id)) {
//                            if (userModelList.size() != 0) {
//                                for (UserModel userModel1 : userModelList) {
//                                    if (!userModel.getUserId().equals(userModel1.getUserId())) {
//                                        userModelList.add(userModel);
//                                    }
//                                }
//                            } else {
//                                userModelList.add(userModel);
//                            }
//                        }
//                    }
//                }
//
//                chatAdapter = new ChatAdapter(getContext(), userModelList);
//                recyclerView.setAdapter(chatAdapter);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }


    private void readChats() {
        userModelList = new ArrayList<>();
        dReference = FirebaseDatabase.getInstance().getReference("users");
        dReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
//                textView_no_chats.setVisibility(View.VISIBLE);
                userModelList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    UserModel userModel = dataSnapshot.getValue(UserModel.class);

                    // display user from UserModel
                    for (String id : usersList) {
                        assert userModel != null;
                        if (userModel.getUserId().equals(id)) {
                            boolean shouldAddUser = true;
                            if (userModelList.size() != 0) {
                                for (UserModel userModel1 : userModelList) {
                                    if (userModel.getUserId().equals(userModel1.getUserId())) {
                                        shouldAddUser = false;
                                        break;
                                    }
                                }
                            }
                            if (shouldAddUser) {
                                userModelList.add(userModel);
                            }
                        }
                    }
                }

                chatAdapter = new ChatAdapter(getContext(), userModelList, true);
                recyclerView.setAdapter(chatAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}