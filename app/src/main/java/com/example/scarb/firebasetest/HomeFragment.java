package com.example.scarb.firebasetest;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


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

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<PostData> posts = new ArrayList<PostData>();
                for (DataSnapshot post : dataSnapshot.child("Posts").getChildren()){

                    String username = post.child("user").getValue().toString();
                    String postContent = post.child("content").getValue().toString();

                    PostData postData = new PostData(username, postContent);

                    posts.add(postData);
                }
                if (posts.size() != 0){
                    loadMessages();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        postButton.setOnClickListener(this);
    }

    //Checks to see if any buttons on the screen have been clicked
    @Override
    public void onClick(View v) {
        if (v == postButton){
            postMessage();
            //Lowers the keyboard focus after you post the message.
            InputMethodManager inputManager = (InputMethodManager) getActivity()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager
                    .hideSoftInputFromWindow((null == getActivity().getCurrentFocus()) ? null :
                            getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void loadMessages(){
        //TODO
    }


    //Posts a message when you click the post button
    public void postMessage(){
        if (writePost.getText().toString().compareTo("") == 0){
            Toast.makeText(getActivity(), "Please write a message", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                String getCurrentUserName = dataSnapshot.child("Users")
                        .child(currentUser.getUid()).child("username")
                        .getValue().toString();

                String key = databaseReference.push().getKey();
                Map<String, Object> postDescription = new HashMap<>();
                postDescription.put("content", writePost.getText().toString());
                postDescription.put("user", getCurrentUserName);
                postDescription.put("time", ServerValue.TIMESTAMP);
                //Need to add location later
                databaseReference.child("Posts").child(key).setValue(postDescription)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getActivity(), "Succesful Post", Toast.LENGTH_SHORT)
                                        .show();
                                //Clear text after post since it looks better
                                writePost.setText("");
                            }
                        });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Say something if the post doesn't go through
            }
        });
    }
}
