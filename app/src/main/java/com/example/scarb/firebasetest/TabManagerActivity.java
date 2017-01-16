package com.example.scarb.firebasetest;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import java.util.ArrayList;


public class TabManagerActivity extends AppCompatActivity {
    ArrayList<Integer> stackNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        final BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        stackNumber = new ArrayList<>();
        stackNumber.add(bottomBar.getCurrentTabId());
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if (tabId == R.id.profile) {
                    if(stackNumber.get(stackNumber.size() - 1) != bottomBar.getCurrentTabId()){
                        stackNumber.add(bottomBar.getCurrentTabId());
                    }
                    Fragment fragment = new ProfileFragment();
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.newFrame, fragment)
                            .addToBackStack(null).commit();
                } else if (tabId == R.id.friends) {
                    if(stackNumber.get(stackNumber.size() - 1) != bottomBar.getCurrentTabId()){
                        stackNumber.add(bottomBar.getCurrentTabId());
                    }
                    Fragment fragment = new FriendsFragment();
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.newFrame, fragment)
                    .addToBackStack(null).commit();
                } else if (tabId == R.id.home) {
                    if(stackNumber.get(stackNumber.size() - 1) != bottomBar.getCurrentTabId()){
                        stackNumber.add(bottomBar.getCurrentTabId());
                    }
                    Fragment fragment = new HomeFragment();
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.newFrame, fragment).
                            addToBackStack(null).commit();
                } else if (tabId == R.id.hotSpot) {
                    if(stackNumber.get(stackNumber.size() - 1) != bottomBar.getCurrentTabId()){
                        stackNumber.add(bottomBar.getCurrentTabId());
                    }
                    Fragment fragment = new HotSpotFragment();
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.newFrame, fragment)
                            .addToBackStack(null).commit();
                }
            }
        });
    }
    @Override
    public void onBackPressed() {

        int count = getFragmentManager().getBackStackEntryCount();

        if (count == 1) {
            super.onBackPressed();
            finish();
        } else {
            BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);
            getFragmentManager().popBackStack();
            if(stackNumber.size()  >= 0) {
                bottomBar.selectTabWithId(stackNumber.get(stackNumber.size() - 1));
                //Log.e("HErhehar", stackNumber.toString());
                stackNumber.remove(stackNumber.size() - 1);
            }
        }

    }
}
