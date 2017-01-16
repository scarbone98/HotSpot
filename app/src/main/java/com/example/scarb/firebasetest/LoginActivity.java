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

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private Button create;
    private EditText email;
    private EditText password;
    private TextView hasAccount;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        create = (Button) findViewById(R.id.logInAccount);
        email = (EditText) findViewById(R.id.emailEnterLog);
        password = (EditText) findViewById(R.id.passwordEnterLog);
        hasAccount = (TextView) findViewById(R.id.dontHaveAccount);
        progressDialog = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();

        create.setOnClickListener(this);
        hasAccount.setOnClickListener(this);
    }

    public void logInUser(){
        String eMail = email.getText().toString().trim();
        String passWord = password.getText().toString().trim();

        if(TextUtils.isEmpty(eMail)){
            Toast.makeText(this, "Please enter an email", Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(passWord)){
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_LONG).show();
            return;
        }
        progressDialog.setMessage("Login in");
        progressDialog.show();
        firebaseAuth.signInWithEmailAndPassword(eMail, passWord)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(LoginActivity.this, "Logged in successfully!", Toast.LENGTH_SHORT).show();
                            finish();
                            startActivity(new Intent(getApplicationContext(), TabManagerActivity.class));
                        }
                        else{
                            Toast.makeText(LoginActivity.this, "Please try again.", Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    }
                });

    }

    @Override
    public void onClick(View view) {
        if(view == create){
            logInUser();
        }
        else if(view == hasAccount){
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }
    }
}
