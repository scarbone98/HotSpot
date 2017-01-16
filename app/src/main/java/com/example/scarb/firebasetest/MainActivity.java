package com.example.scarb.firebasetest;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button create;
    private EditText username;
    private EditText email;
    private EditText password;
    private TextView hasAccount;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*
            Checks to see if current user is already signed in. If he is then
            open the TabManagerActivity instead.
         */
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    finish();
                    startActivity(new Intent(getApplicationContext(),
                            TabManagerActivity.class));
                }
            }
        });

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        create = (Button) findViewById(R.id.createAccount);
        username = (EditText) findViewById(R.id.usernameEnter);
        email = (EditText) findViewById(R.id.emailEnter);
        password = (EditText) findViewById(R.id.passwordEnter);
        hasAccount = (TextView) findViewById(R.id.haveAccount);
        progressDialog = new ProgressDialog(this);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        create.setOnClickListener(this);
        hasAccount.setOnClickListener(this);
    }

    public void registerUser(){
        String eMail = email.getText().toString().trim();
        String passWord = password.getText().toString().trim();
        final String userName = username.getText().toString().trim();
        final String photoURL = "";

        if(TextUtils.isEmpty(userName)){
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(eMail)){
            Toast.makeText(this, "Please enter an email", Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(passWord)){
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_LONG).show();
            return;
        }
        progressDialog.setMessage("Registering");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(eMail, passWord)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(MainActivity.this, "Created Successfully!", Toast.LENGTH_SHORT).show();
                            User user = new User(userName, photoURL);
                            FirebaseUser temp = firebaseAuth.getCurrentUser();
                            databaseReference.child("Users").child(temp.getUid()).setValue(user);
                            databaseReference.child("HashMap").child(userName.toLowerCase().trim()).setValue(temp.getUid());
                            finish();
                            startActivity(new Intent(getApplicationContext(), TabManagerActivity.class));
                        }
                        else{
                            Toast.makeText(MainActivity.this, "Please try again.", Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    }
                });

    }

    @Override
    public void onClick(View view) {
        if(view == create){
            registerUser();
        }
        else if(view == hasAccount){
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
    }
}
