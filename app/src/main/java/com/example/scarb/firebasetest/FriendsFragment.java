package com.example.scarb.firebasetest;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;


public class FriendsFragment extends Fragment implements View.OnClickListener{
    private Button addButton;
    private TextView addTextView;
    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;
    private ListView listView;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friends,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        addButton = (Button) getActivity().findViewById(R.id.add);
        addTextView = (TextView) getActivity().findViewById(R.id.addFriendText);
        listView = (ListView) getActivity().findViewById(R.id.friendsList);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        loadFriendsList();

        addButton.setOnClickListener(this);
    }
    public void setValues(String userName, String userID, String currentUserName){
        databaseReference.child("Users").child(currentUser.getUid()).child("Friends").
                child(userName).setValue("sent");
        databaseReference.child("Users").child(userID).child("Friends").
                child(currentUserName).setValue("pending");
    }
    public void loadFriendsList(){
        try {
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            ArrayList<FriendData> friendsTemp = new ArrayList<>();

                            for (DataSnapshot user : dataSnapshot.child("Users")
                                    .child(currentUser.getUid()).child("Friends").getChildren()) {

                                String userName = user.getKey();
                                boolean pending = false;
                                String userUID = dataSnapshot.child("HashMap").
                                        child(userName.toLowerCase().trim()).getValue().toString();
                                String photoID = dataSnapshot.child("Users").child(userUID).
                                        child("profileURL").getValue().toString();

                                if (user.getValue().toString().equals("pending")){
                                    pending = true;
                                }

                                FriendData friendData = new FriendData(userName, photoID, pending);

                                friendsTemp.add(friendData);
                            }
                            if (friendsTemp.size() != 0) {
                                loadArray(friendsTemp);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(getActivity(), "Error, try refreshing", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
        }catch (Exception e){
            //ADD SOMETHING ELSE HERE TO TELL USER HOW TO ADD FRIEND
            Log.e("No friends", "User has no friends yet.");
        }
    }
    public void loadArray(ArrayList<FriendData> friendsTemp){
        FriendAdapter friendAdapter = new FriendAdapter(getActivity(),
                R.layout.friend_row,
                friendsTemp);
        listView.setAdapter(friendAdapter);
    }
    @Override
    public void onClick(View view) {
        if (view == addButton){
            String username = addTextView.getText().toString();
            if(username.equals("")){
                Toast.makeText(getActivity(), "Please enter a username.", Toast.LENGTH_SHORT).show();
                return;
            }
            addFriend(username);
        }
    }

    public void addFriend(final String username){
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    String currentUserName = dataSnapshot.child("Users").child(currentUser.getUid()).child("username").
                            getValue().toString();
                    String userId = dataSnapshot.child("HashMap").child(username.toLowerCase().trim()).getValue().toString();
                    String realUserName = dataSnapshot.child("Users").child(userId).child("username").getValue().toString();
                    setValues(realUserName, userId, currentUserName);
                    Toast.makeText(getActivity(), "Friend added", Toast.LENGTH_SHORT)
                            .show();
                    loadFriendsList();
                } catch (Exception e){
                    Toast.makeText(getActivity().getApplicationContext(), "User does not exist", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity().getApplicationContext(), "Please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
