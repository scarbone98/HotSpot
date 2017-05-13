package com.example.scarb.firebasetest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import static android.app.Activity.RESULT_OK;


public class ProfileFragment extends android.app.Fragment implements View.OnClickListener{

    private static final int RESULT_LOAD_IMAGE = 5;
    private ImageView imageView, friendImageView;
    private FirebaseAuth firebaseAuth;
    private TextView textView;
    private EditText friendText;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private Button uploadButton, friendButton, signOutButton;
    private Uri selectedImage;
    private FirebaseUser user;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        /****************************************************************************/
        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        uploadButton = (Button) view.findViewById(R.id.uploadButton);
        friendButton = (Button) view.findViewById(R.id.friendSearchButton);
        signOutButton = (Button) view.findViewById(R.id.signOut);
        textView = (TextView) view.findViewById(R.id.usernameView);
        friendText = (EditText) view.findViewById(R.id.friendTextSearch);
        imageView = (ImageView) view.findViewById(R.id.profilePicture);
        friendImageView = (ImageView) view.findViewById(R.id.friendImageView);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        user = firebaseAuth.getCurrentUser();
        /**************************************************************************/
        databaseReference.child("Users").child(firebaseAuth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String imageEncoded = dataSnapshot.child("profileURL").getValue().toString();
                        if(!imageEncoded.equals("")) {
                            InputStream stream = new ByteArrayInputStream(
                                    Base64.decode(imageEncoded.getBytes(), Base64.DEFAULT));
                            Bitmap bitmap = BitmapFactory.decodeStream(stream);
                            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.
                                    create(getResources(), bitmap);
                            roundedBitmapDrawable.setCircular(true);
                            imageView.setImageDrawable(roundedBitmapDrawable);
                            //imageView.setImageBitmap(bitmap);
                        }
                        String uName = dataSnapshot.child("username").getValue().toString();
                        uName = uName + "!";
                        String welcome = "Welcome, " + uName;
                        textView.setText(welcome);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        imageView.setOnClickListener(this);
        signOutButton.setOnClickListener(this);
        uploadButton.setOnClickListener(this);
        friendButton.setOnClickListener(this);
    }

    public void searchFriend(final String username){
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    String userId = dataSnapshot.child("HashMap").child(username.toLowerCase().trim()).getValue().toString();
                    String photo = dataSnapshot.child("Users").child(userId).child("profileURL").getValue().toString();
                    if (!photo.equals("")) {
                        InputStream stream = new ByteArrayInputStream(
                                Base64.decode(photo.getBytes(), Base64.DEFAULT));
                        Bitmap bitmap = BitmapFactory.decodeStream(stream);
                        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.
                                create(getResources(), bitmap);
                        roundedBitmapDrawable.setCircular(true);
                        friendImageView.setImageDrawable(roundedBitmapDrawable);
                    }
                } catch (Exception e){
                    Toast.makeText(getActivity().getApplicationContext(), "User does not exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity().getApplicationContext(), "Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View view) {
        /*
            Checks to see if the user clicked on the image if so then it loads up
            the image gallery. The user must pick an Image. Once the image is picked
            it is loaded into the imageView.
         */
        if(view == imageView){
            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
        }
        /*
            Button to sign the user out.
         */
        else if(view == signOutButton){
            firebaseAuth.signOut();
            getActivity().finish();
            startActivity(new Intent(getActivity(), LoginAndSignUp.class));
        }
        /*
            Checks if the button clicked is the search button.
            If it is the program calls a function to see if the user that was typed in
            exists.
         */
        else if (view == friendButton){
            String username = friendText.getText().toString();
            searchFriend(username);
        }
        /*
            Loads the Image, if the user has picked an image.
            If the user has not picked an image it sends a
            message saying to load a new Image. The image is then
            encoded to Base64 and sent to the users database location.
         */
        else if(view == uploadButton && selectedImage != null){
            StorageReference filePath = storageReference.child("Users").child(user.getUid()).child("Photo");

            filePath.putFile(selectedImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    imageView.buildDrawingCache();
                    Bitmap bitmap = imageView.getDrawingCache();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 35, stream);
                    byte[] data = stream.toByteArray();
                    String imString = Base64.encodeToString(data, 0);
                    databaseReference.child("Users").child(user.getUid()).
                            child("profileURL").setValue(imString);


                    Toast.makeText(getActivity().getApplicationContext(), "Looking good!", Toast.LENGTH_SHORT)
                            .show();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getActivity().getApplicationContext(), "Please try again", Toast.LENGTH_SHORT)
                            .show();
                }
            });
        }

        else{
            Toast.makeText(getActivity(), "Please enter a new image.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_LOAD_IMAGE && data != null && resultCode == RESULT_OK){
            try {
                selectedImage = data.getData();
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver()
                        , selectedImage);
                RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.
                        create(getResources(), bitmap);
                roundedBitmapDrawable.setCircular(true);
                imageView.setImageDrawable(roundedBitmapDrawable);
            } catch (IOException e){
                Log.e("broken", "Done");
            }
            //imageView.setImageURI(selectedImage);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
