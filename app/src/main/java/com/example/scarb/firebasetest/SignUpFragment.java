package com.example.scarb.firebasetest;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * A simple {@link Fragment} subclass.
 */
public class SignUpFragment extends Fragment implements View.OnClickListener {

    private Button create;
    private Button admin;
    private EditText username;
    private EditText email;
    private EditText password;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    //getActivity().finish();
                    startActivity(new Intent(getActivity().getApplicationContext(),
                            MainTabsManager.class));
                }
            }
        });
        admin = (Button) view.findViewById(R.id.adminButton);
        create = (Button) view.findViewById(R.id.createAccountEnter);
        username = (EditText) view.findViewById(R.id.usernameEnter);
        email = (EditText) view.findViewById(R.id.emailEnter);
        password = (EditText) view.findViewById(R.id.passwordEnter);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        admin.setOnClickListener(this);
        create.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == create){
            registerUser();
        }
        //Temporary
        else if (v == admin){
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            firebaseAuth.signInWithEmailAndPassword("admin@gmail.com", "1234567").
                    addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(getActivity(), "Logged in successfully!", Toast.LENGTH_SHORT).show();
                                getActivity().finish();
                                startActivity(new Intent(getActivity().getApplicationContext(), MainTabsManager.class));
                            }
                            else{
                                Toast.makeText(getActivity(), "Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
    public void registerUser(){
        String eMail = email.getText().toString().trim();
        String passWord = password.getText().toString().trim();
        final String userName = username.getText().toString().trim();
        final String photoURL = "";

        if(TextUtils.isEmpty(eMail)){
            Toast.makeText(getActivity(), "Please enter an email", Toast.LENGTH_LONG).show();
            return;
        }
        else if(TextUtils.isEmpty(passWord)){
            Toast.makeText(getActivity(), "Please enter a password", Toast.LENGTH_LONG).show();
            return;
        }
        else if(TextUtils.isEmpty(userName)){
            Toast.makeText(getActivity(), "Please enter a username", Toast.LENGTH_LONG).show();
            return;
        }

        firebaseAuth.createUserWithEmailAndPassword(eMail, passWord)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(getActivity(), "Created Successfully!", Toast.LENGTH_SHORT).show();
                            User user = new User(userName, photoURL);
                            FirebaseUser temp = firebaseAuth.getCurrentUser();
                            databaseReference.child("Users").child(temp.getUid()).setValue(user);
                            databaseReference.child("HashMap").child(userName.toLowerCase().trim()).setValue(temp.getUid());
                            getActivity().finish();
                            startActivity(new Intent(getActivity().getApplicationContext(), MainTabsManager.class));
                        }
                        else{
                            Toast.makeText(getActivity(), "Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }
}
