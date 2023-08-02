package com.patsofts.chatapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.patsofts.chatapp.MessageActivity;
import com.patsofts.chatapp.Models.UserModel;
import com.patsofts.chatapp.R;

import java.util.Collections;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context context;
    private List<UserModel> userModelList;
    private boolean isChat;

    public UserAdapter(Context context, List<UserModel> userModelList, boolean isChat) {
        this.context = context;
        this.userModelList = userModelList;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel userModel = userModelList.get(position);
//        UserModel userModel = userModelList.get(getItemCount() - position - 1);
        holder.username.setText(userModel.getUserName());
        holder.email.setText(userModel.getUserEmail());
        if (userModel.getImageURL().equals("default")) {
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(context).load(userModel.getImageURL()).into(holder.profile_image);
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView username, email;
        public ImageView profile_image, img_on;

        public ViewHolder(View view) {
            super(view);

            username = view.findViewById(R.id.username);
            email = view.findViewById(R.id.email);
            profile_image = view.findViewById(R.id.profile_image);
            img_on = view.findViewById(R.id.img_on);
//            img_off = view.findViewById(R.id.img_off);
        }
    }
}
