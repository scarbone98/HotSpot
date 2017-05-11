package com.example.scarb.firebasetest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.Layout;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.List;

public class FriendAdapter extends ArrayAdapter<FriendData>{

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

    public FriendAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<FriendData> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final FriendData friendData = getItem(position);

        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.friend_row,
                    parent, false);
        }
        //Make an accept button if the button doesn't already exist
        //AND if the user is currently pending
        if (convertView.findViewById(R.id.linearLayout).findViewWithTag("created") == null &&
                friendData.getPeding()) {
            //create a button
            Button button = new Button(getContext());
            button.setText("Accept");
            button.setTag("created");
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateDataBase(friendData.getUsername());
                }
            });
            LinearLayout ll = (LinearLayout) convertView.findViewById(R.id.linearLayout);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            ll.addView(button, lp);
        }


        //If the user is already a friend then remove the ADD button
        else if(!friendData.getPeding() && convertView.findViewById(R.id.linearLayout)
                .findViewWithTag("created") != null){
            try {
                convertView.findViewWithTag(R.id.linearLayout).findViewWithTag("created")
                        .setVisibility(View.GONE);
            } catch (Exception e){
                Log.e("Something", "Went Wrong");
            }
        }

        //Load the friend's picture into their imageView
        ImageView imageView = (ImageView) convertView.findViewById(R.id.friendPicture);
        TextView textView = (TextView) convertView.findViewById(R.id.friendUserName);
        /*Gets user name */
        textView.setText(friendData.getUsername());
        /*Decodes the image from dataBase to bitmap*/
        InputStream stream = new ByteArrayInputStream(
                Base64.decode(friendData.getPhotoID().getBytes(), Base64.DEFAULT));
        Bitmap bitmap = BitmapFactory.decodeStream(stream);
        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.
                create(getContext().getResources(), bitmap);
        roundedBitmapDrawable.setCircular(true);
        imageView.setImageDrawable(roundedBitmapDrawable);

        return convertView;
    }
    private void updateDataBase(final String username){
        databaseReference.child("Users").child(firebaseUser.getUid())
                .child("Friends").child(username).setValue("friend");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String friendID = dataSnapshot.child("HashMap").child(username.toLowerCase().trim())
                        .getValue().toString();
                String currentUserName = dataSnapshot.child("Users").child(firebaseUser.getUid()).
                        child("username").getValue().toString();
                updateFriend(friendID, currentUserName);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Error, please refresh.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void updateFriend(String friendUserName, String currentUserName){

        databaseReference.child("Users").child(friendUserName)
                .child("Friends").child(currentUserName).setValue("friend");
    }
}
