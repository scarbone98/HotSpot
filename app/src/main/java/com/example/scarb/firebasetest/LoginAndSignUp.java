package com.example.scarb.firebasetest;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class LoginAndSignUp extends AppCompatActivity implements View.OnClickListener{

    private Button signUpButton;
    private Button logInButton;
    private boolean isCurrentSignUp = true;
    private boolean isCurrentLogIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_and_sign_up);

        signUpButton = (Button) findViewById(R.id.signUpButton);
        logInButton = (Button) findViewById(R.id.logInButton);

        Fragment fragment = new SignUpFragment();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragManager, fragment)
                .addToBackStack("signUp").commit();

        signUpButton.setOnClickListener(this);
        logInButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == signUpButton && !isCurrentSignUp){
            isCurrentSignUp = true;
            isCurrentLogIn = false;
            Fragment fragment = new SignUpFragment();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.animator.exit_from_right, R.animator.enter_from_left)
                    .replace(R.id.fragManager, fragment)
                    .addToBackStack("signUp").commit();
        }
        else if (v == logInButton && !isCurrentLogIn){
            isCurrentLogIn = true;
            isCurrentSignUp = false;
            Fragment fragment = new LogInFragment();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.animator.exit_from_left, R.animator.enter_from_right)
                    .replace(R.id.fragManager, fragment)
                    .addToBackStack("logIn").commit();
        }
    }
}
