package com.patsofts.chatapp.Fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;

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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.patsofts.chatapp.Adapters.UserAdapter;
import com.patsofts.chatapp.Models.UserModel;
import com.patsofts.chatapp.R;

import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<UserModel> userModelList;
    EditText search_users;

    ProgressBar progressBar_search;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        progressBar_search = view.findViewById(R.id.progress_s);

        userModelList = new ArrayList<>();

        readUsers();

        search_users = view.findViewById(R.id.search_users);

        search_users.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString().toLowerCase());

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return view;
    }

    private void searchUsers(String s) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Query query = FirebaseDatabase.getInstance().getReference("users").orderByChild("search")
                .startAt(s)
                .endAt(s + "\uf8ff");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userModelList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    UserModel userModel = dataSnapshot.getValue(UserModel.class);

                    assert userModel != null;
                    assert firebaseUser != null;
                    if (!userModel.getUserId().equals(firebaseUser.getUid())) {
                        userModelList.add(userModel);
                    }
                }

                userAdapter = new UserAdapter(getContext(), userModelList, false);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readUsers() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");

        databaseReference
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (search_users.getText().toString().equals("")) {
                            progressBar_search.setVisibility(View.GONE);
                            userModelList.clear();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                UserModel userModel = dataSnapshot.getValue(UserModel.class);

                                assert userModel != null;
                                assert firebaseUser != null;
                                if (!userModel.getUserId().equals(firebaseUser.getUid())) {
                                    userModelList.add(userModel);
                                }
                            }

                            userAdapter = new UserAdapter(getContext(), userModelList, true);
                            recyclerView.setAdapter(userAdapter);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}