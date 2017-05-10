package com.example.scarb.firebasetest;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import java.util.Stack;



public class MainTabsManager extends AppCompatActivity {
    private FragmentManager fragmentManager;
    Stack<Integer> stack;
    BottomBar bottomBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        fragmentManager = getFragmentManager();
        bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        stack = new Stack<>();
        stack.push(bottomBar.getCurrentTabId());
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if (tabId == R.id.profile) {


                    if (stack != null && stack.peek() != R.id.profile) {
                        stack.push(bottomBar.getCurrentTabId());
                    }


                    Fragment fragment = new ProfileFragment();
                    //FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.newFrame, fragment)
                            .addToBackStack("profile").commit();

                } else if (tabId == R.id.friends) {


                    if (stack != null && stack.peek() != R.id.friends) {
                        stack.push(bottomBar.getCurrentTabId());
                    }


                    Fragment fragment = new FriendsFragment();
                    //FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.newFrame, fragment)
                    .addToBackStack("friends").commit();
                } else if (tabId == R.id.home) {


                    if (stack != null && stack.peek() != R.id.home) {
                        stack.push(bottomBar.getCurrentTabId());
                    }


                    Fragment fragment = new HomeFragment();
                    //FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.newFrame, fragment).
                            addToBackStack("home").commit();
                } else if (tabId == R.id.hotSpot) {


                    if (stack != null && stack.peek() != R.id.profile) {
                        stack.push(bottomBar.getCurrentTabId());
                    }



                    Fragment fragment = new HotSpotFragment();
                    //FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.newFrame, fragment)
                            .addToBackStack("hotSpot").commit();
                }
            }
        });
    }
    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0){
            super.onBackPressed();
            //finish();
        }
        else {
            getSupportFragmentManager().popBackStack();
            bottomBar.selectTabWithId(stack.pop());
        }
    }
}
