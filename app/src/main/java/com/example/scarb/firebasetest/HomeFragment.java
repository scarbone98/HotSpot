package com.example.scarb.firebasetest;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends android.app.Fragment implements View.OnClickListener{

    private EditText writePost;
    private Button postButton;
    private DatabaseReference databaseReference;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        writePost = (EditText) view.findViewById(R.id.writePostText);
        postButton = (Button) view.findViewById(R.id.postButton);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        postButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == postButton){
            postMessage();
        }
    }

    public void postMessage(){
        if (writePost.getText().toString().compareTo("") == 0){
            Toast.makeText(getActivity(), "Please write a message", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        databaseReference.child("Posts").child("Hello")
                .setValue(writePost.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getActivity(), "Successful Post", Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }
}
