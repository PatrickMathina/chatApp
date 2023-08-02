package com.patsofts.chatapp.Adapters;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.patsofts.chatapp.Models.MessageModel;
import com.patsofts.chatapp.R;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_LEFT = 0;
    public static final int MSG_RIGHT = 1;
    private Context context;
    private List<MessageModel> messageModelList; //MessageModel-Chat, messageModelList-mChat
    private String image_url; //imageurl

    FirebaseUser fUser;

    public MessageAdapter(Context context, List<MessageModel> messageModelList, String image_url) {
        this.context = context;
        this.messageModelList = messageModelList;
        this.image_url = image_url;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == MSG_RIGHT) {
            view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false);
        }
        return new ViewHolder(view);

//        i.e
//                if (viewType == MSG_RIGHT) {
//            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false);
//            return new MessageAdapter.ViewHolder(view);
//        } else {
//            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false);
//            return new MessageAdapter.ViewHolder(view);
//        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        MessageModel messageModel = messageModelList.get(position);

        holder.show_msg.setText(messageModel.getMessage());

        if (image_url.equals("default")) {
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(context).load(image_url).into(holder.profile_image);
        }

//        if (position==messageModelList.size()-1) { //-1 for the last message
        if (messageModel.isIsseen()) {
            holder.txt_seen.setText("| Seen");
        } else {
            holder.txt_seen.setText("| Delivered");
        }
        holder.txt_time.setText(messageModel.getTimeStamp());

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
//                Toast.makeText(context, "Long press", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return messageModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        public TextView show_msg, txt_seen, txt_time;
        public ImageView profile_image;

        public ViewHolder(View view) {
            super(view);

            show_msg = view.findViewById(R.id.show_msg);
            profile_image = view.findViewById(R.id.profile_image);
            txt_seen = view.findViewById(R.id.txt_seen);
            txt_time = view.findViewById(R.id.txt_time);

            show_msg.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            // Add menu items
            menu.add(Menu.NONE, 1, Menu.NONE, "Copy").setIcon(R.drawable.baseline_content_copy_24);
            menu.add(Menu.NONE, 2, Menu.NONE, "Forward").setIcon(R.drawable.baseline_arrow_forward_24);
            menu.add(Menu.NONE, 3, Menu.NONE, "Delete").setIcon(R.drawable.baseline_delete_24);
        }
    }

    //    ctr+o
    @Override
    public int getItemViewType(int position) {
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (messageModelList.get(position).getSenderId().equals(fUser.getUid())) {
            return MSG_RIGHT;
        } else {
            return MSG_LEFT;
        }
    }
}
