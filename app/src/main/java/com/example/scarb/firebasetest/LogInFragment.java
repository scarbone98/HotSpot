package com.example.scarb.firebasetest;


import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class LogInFragment extends Fragment implements View.OnClickListener{

    private EditText email;
    private EditText password;
    private Button logInButton;
    private FirebaseAuth firebaseAuth;

    public LogInFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (savedInstanceState != null){
            email.setText(savedInstanceState.get("Email").toString());
            password.setText(savedInstanceState.get("Password").toString());
        }
        return inflater.inflate(R.layout.fragment_log_in, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        email = (EditText) view.findViewById(R.id.emailEnter);
        password = (EditText) view.findViewById(R.id.passwordEnter);
        logInButton = (Button) view.findViewById(R.id.logInButton);
        firebaseAuth = FirebaseAuth.getInstance();

        logInButton.setOnClickListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("Email", email.toString());
        outState.putString("Password", password.toString());
    }

    @Override
    public void onClick(View v) {
        if (v == logInButton){
            logInUser();
        }
    }
    public void logInUser(){
        String eMail = email.getText().toString().trim();
        String passWord = password.getText().toString().trim();

        if(TextUtils.isEmpty(eMail)){
            Toast.makeText(getActivity(), "Please enter an email", Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(passWord)){
            Toast.makeText(getActivity(), "Please enter a password", Toast.LENGTH_LONG).show();
            return;
        }
        firebaseAuth.signInWithEmailAndPassword(eMail, passWord)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(getActivity(), "Logged in successfully!", Toast.LENGTH_SHORT).show();
                            getActivity().finish();
                            startActivity(new Intent(getActivity(), MainTabsManager.class));
                        }
                        else{
                            Toast.makeText(getActivity(), "Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }
}
