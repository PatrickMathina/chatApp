package com.patsofts.chatapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.patsofts.chatapp.MessageActivity;
import com.patsofts.chatapp.Models.MessageModel;
import com.patsofts.chatapp.Models.UserModel;
import com.patsofts.chatapp.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private Context context;
    private List<UserModel> userModelList;
    private boolean isChat;
    String theLastMsg;

    public ChatAdapter(Context context, List<UserModel> userModelList, boolean isChat) {
        this.context = context;
        this.userModelList = userModelList;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_item, parent, false);
        return new ChatAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        UserModel userModel = userModelList.get(getItemCount() - position - 1);
        UserModel userModel = userModelList.get(position);
        holder.username.setText(userModel.getUserName());
        if (userModel.getImageURL().equals("default")) {
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(context).load(userModel.getImageURL()).into(holder.profile_image);
        }

        if (isChat) {
            lastMsg(userModel.getUserId(), holder.last_msg, holder.textView_time, holder.textView_unread_msgs);
        } else {
            holder.last_msg.setVisibility(View.GONE);
        }

        if (isChat) {
            if (userModel.getStatus().equals("online")) {
                holder.img_on.setVisibility(View.VISIBLE);
//                holder.img_off.setVisibility(View.GONE);
            } else {
//                holder.img_off.setVisibility(View.VISIBLE);
                holder.img_on.setVisibility(View.GONE);
            }
        } else {
            holder.img_on.setVisibility(View.GONE);
//            holder.img_off.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MessageActivity.class);
            intent.putExtra("userid", userModel.getUserId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView username, last_msg, textView_time, textView_unread_msgs;
        private ImageView profile_image, img_on;

        public ViewHolder(View view) {
            super(view);

            username = view.findViewById(R.id.username);
            profile_image = view.findViewById(R.id.profile_image);
            img_on = view.findViewById(R.id.img_on);
//            img_off = view.findViewById(R.id.img_off);
            last_msg = view.findViewById(R.id.last_msg);
            textView_time = view.findViewById(R.id.time);
            textView_unread_msgs = view.findViewById(R.id.unread_msg);
        }
    }

    private void lastMsg(String userid, TextView last_msg, TextView textView_time, TextView textView_unread_msgs) {
        theLastMsg = "default";
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("chats");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int unreadCount = 0;

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    MessageModel messageModel = dataSnapshot.getValue(MessageModel.class);
                    assert messageModel != null;
                    assert firebaseUser != null;

                    if (messageModel.getReceiverId().equals(firebaseUser.getUid()) && messageModel.getSenderId().equals(userid) &&
                            !messageModel.isIsseen()) {
                        unreadCount++;
                    }

                    if (messageModel.getReceiverId().equals(firebaseUser.getUid()) && messageModel.getSenderId().equals(userid) ||
                            messageModel.getReceiverId().equals(userid) && messageModel.getSenderId().equals(firebaseUser.getUid())) {
                        theLastMsg = messageModel.getMessage();
                        textView_time.setText(messageModel.getTimeStamp());

                        if (unreadCount > 0) {
                            String unread_messages=String.valueOf(unreadCount);
                            textView_unread_msgs.setText(" "+unread_messages+" ");
                        }
                    }
                }

                switch (theLastMsg) {
                    case "default":
                        last_msg.setText("No new message");
                        break;

                    default:
                        last_msg.setText(theLastMsg);
                        break;
                }

//                String lastMsgText;
//                if (theLastMsg.equals("default")) {
//                    lastMsgText = "No new message";
//                } else {
//                    lastMsgText = theLastMsg;
//                }

//                // Truncate the lastMsgText if it is too long
//                int maxLength = 25; // Define the maximum length of the lastMsgText
//                if (lastMsgText.length() > maxLength) {
//                    lastMsgText = lastMsgText.substring(0, maxLength) + "...";
//                }

                // Create a SpannableString to customize the color of the unreadCount
//                SpannableString spannableLastMsgText = new SpannableString(lastMsgText);
//                if (unreadCount > 0) {
//                    String countText = " (" + unreadCount + ")";
//                    if (countText.length() <= spannableLastMsgText.length()) {
//                        spannableLastMsgText.setSpan(new ForegroundColorSpan(Color.RED), spannableLastMsgText.length() - countText.length(), spannableLastMsgText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                        spannableLastMsgText = new SpannableString(spannableLastMsgText + countText);
//                    }
//                }

//                last_msg.setText(spannableLastMsgText);
                theLastMsg = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


//    private void lastMsg(String userid, TextView last_msg, TextView textView_time) {
//        theLastMsg = "default";
//        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("chats");
//
//        databaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                int unreadCount = 0;  // Counter for unread messages
//
//                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                    MessageModel messageModel = dataSnapshot.getValue(MessageModel.class);
//                    assert messageModel != null;
//                    assert firebaseUser != null;
//
//                    if (messageModel.getReceiverId().equals(firebaseUser.getUid()) && messageModel.getSenderId().equals(userid) &&
//                            !messageModel.isIsseen()) {
//                        unreadCount++;  // Increment the count for each unread message
//                    }
//
//                    if (messageModel.getReceiverId().equals(firebaseUser.getUid()) && messageModel.getSenderId().equals(userid) ||
//                            messageModel.getReceiverId().equals(userid) && messageModel.getSenderId().equals(firebaseUser.getUid())) {
//                        theLastMsg = messageModel.getMessage();
//                        textView_time.setText(messageModel.getTimeStamp());
//                    }
//                }
//
//                String lastMsgText;
//                if (theLastMsg.equals("default")) {
//                    lastMsgText = "No new message";
//                } else {
//                    lastMsgText = theLastMsg;
//                }
//
//                // Truncate the lastMsgText if it is too long
//                int maxLength = 25; // Define the maximum length of the lastMsgText
//                if (lastMsgText.length() > maxLength) {
//                    lastMsgText = lastMsgText.substring(0, maxLength) + "...";
//                }
//
//                // Add the unreadCount at the far end of the lastMsgText
//                if (unreadCount > 0) {
//                    String countText = " (" + unreadCount + ")";
//                    lastMsgText += countText;
//                }
//
//                last_msg.setText(lastMsgText);
//                theLastMsg = "default";
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }


//    private void lastMsg(String userid, TextView last_msg, TextView textView_time) {
//        theLastMsg = "default";
//        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("chats");
//
//        databaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                int unreadCount = 0;  // Counter for unread messages
//
//                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                    MessageModel messageModel = dataSnapshot.getValue(MessageModel.class);
//                    assert messageModel != null;
//                    assert firebaseUser != null;
//
//                    if (messageModel.getReceiverId().equals(firebaseUser.getUid()) && messageModel.getSenderId().equals(userid) &&
//                            !messageModel.isIsseen()) {
//                        unreadCount++;  // Increment the count for each unread message
//                    }
//
//                    if (messageModel.getReceiverId().equals(firebaseUser.getUid()) && messageModel.getSenderId().equals(userid) ||
//                            messageModel.getReceiverId().equals(userid) && messageModel.getSenderId().equals(firebaseUser.getUid())) {
//                        theLastMsg = messageModel.getMessage();
//                        textView_time.setText(messageModel.getTimeStamp());
//                    }
//                }
//
//                SpannableString lastMsgText;
//                if (theLastMsg.equals("default")) {
//                    lastMsgText = new SpannableString("No new message");
//                } else {
//                    lastMsgText = new SpannableString(theLastMsg);
//                }
//
//                // Add the unreadCount at the far end of the lastMsgText
//                if (unreadCount > 0) {
//                    String countText = " (" + unreadCount + ")";
//                    lastMsgText = new SpannableString(lastMsgText + countText);
//                    lastMsgText.setSpan(new ForegroundColorSpan(Color.RED), lastMsgText.length() - countText.length(), lastMsgText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                }
//
//                last_msg.setText(lastMsgText);
//                theLastMsg = "default";
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }


//    private void lastMsg(String userid, TextView last_msg, TextView textView_time) {
//        theLastMsg = "default";
//        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("chats");
//
//        databaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                int unreadCount = 0;  // Counter for unread messages
//
//                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                    MessageModel messageModel = dataSnapshot.getValue(MessageModel.class);
//                    assert messageModel != null;
//                    assert firebaseUser != null;
//
//                    if (messageModel.getReceiverId().equals(firebaseUser.getUid()) && messageModel.getSenderId().equals(userid) &&
//                            !messageModel.isIsseen()) {
//                        unreadCount++;  // Increment the count for each unread message
//                    }
//
//                    if (messageModel.getReceiverId().equals(firebaseUser.getUid()) && messageModel.getSenderId().equals(userid) ||
//                            messageModel.getReceiverId().equals(userid) && messageModel.getSenderId().equals(firebaseUser.getUid())) {
//                        theLastMsg = messageModel.getMessage();
//                        textView_time.setText(messageModel.getTimeStamp());
//                    }
//                }
//
//                switch (theLastMsg) {
//                    case "default":
//                        last_msg.setText("No new message");
//                        break;
//
//                    default:
//                        String lastMsgText = theLastMsg;
//                        if (unreadCount > 0) {
//                            lastMsgText += " (" + unreadCount + ")";
//                        }
//                        last_msg.setText(lastMsgText);
//                        break;
//                }
//
//                theLastMsg = "default";
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }


    //check for last msg
//    private void lastMsg(String userid, TextView last_msg, TextView textView_time) {
//        theLastMsg = "default";
//        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("chats");
//
//        databaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                    MessageModel messageModel = dataSnapshot.getValue(MessageModel.class);
//                    assert messageModel != null;
//                    assert firebaseUser != null;
//
//                    if (messageModel.getReceiverId().equals(firebaseUser.getUid()) && messageModel.getSenderId().equals(userid) ||
//                            messageModel.getReceiverId().equals(userid) && messageModel.getSenderId().equals(firebaseUser.getUid())) {
////                        String time = DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date()); // Get current time
//                        theLastMsg = messageModel.getMessage();
//                        textView_time.setText(messageModel.getTimeStamp());
//                    }
//                }
//
//                switch (theLastMsg) {
//                    case "default":
//                        last_msg.setText("No new message");
//                        break;
//
//                    default:
//                        last_msg.setText(theLastMsg);
//                        break;
//                }
//
//                theLastMsg = "default";
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }


//    private void lastMsg(String userid, TextView last_msg) {
//        theLastMsg = "default";
//        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("chats");
//
//        databaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                    MessageModel messageModel = dataSnapshot.getValue(MessageModel.class);
//                    assert messageModel != null;
//                    assert firebaseUser != null;
//                    if (messageModel.getReceiverId().equals(firebaseUser.getUid()) && messageModel.getSenderId().equals(userid) ||
//                            messageModel.getReceiverId().equals(userid) && messageModel.getSenderId().equals(firebaseUser.getUid())) {
//                        theLastMsg = messageModel.getMessage();
//                        // Get the timestamp of the message and format it
//                        long timestamp = messageModel.getTimestamp();
//                        String time = formatTimestamp(timestamp, context); // Call the method to format the timestamp
//                        theLastMsg = theLastMsg + " - " + time; // Concatenate the message and time
//                    }
//                }
//
//                switch (theLastMsg) {
//                    case "default":
//                        last_msg.setText("No new message");
//                        break;
//
//                    default:
//                        last_msg.setText(theLastMsg);
//                        break;
//                }
//
//                theLastMsg = "default";
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }

//    private String formatTimestamp(long timestamp, Context context) {
//        DateFormat dateFormat = android.text.format.DateFormat.getTimeFormat(context);
//        return dateFormat.format(new Date(timestamp));
//    }


//    private String formatTimestamp(long timestamp) {
//        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
//        return dateFormat.format(new Date(timestamp));
//    }


}
