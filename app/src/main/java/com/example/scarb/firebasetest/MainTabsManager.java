package com.example.scarb.firebasetest;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import java.util.Stack;



public class MainTabsManager extends AppCompatActivity {
    private FragmentManager fragmentManager;
    private Stack<Integer> stackCheckOrder;
    private BottomBar bottomBar;
    private MenuItem userFriends;
    private MenuItem userMessages;
    private MenuItem userSettings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tabmanager);

        userFriends = (MenuItem) findViewById(R.id.userFriends);
        userMessages = (MenuItem) findViewById(R.id.userMessages);
        userSettings = (MenuItem) findViewById(R.id.userSettings);

        fragmentManager = getFragmentManager();
        bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        stackCheckOrder = new Stack<>();

        stackCheckOrder.push(bottomBar.getCurrentTabId());

        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if (tabId == R.id.profile) {
                    if (stackCheckOrder.peek() != bottomBar.getCurrentTabId()){
                        stackCheckOrder.push(bottomBar.getCurrentTabId());
                    }
                    Fragment fragment = new ProfileFragment();
                    fragmentManager.beginTransaction().replace(R.id.newFrame, fragment)
                            .addToBackStack("profile").commit();

                }
                /*
                else if (tabId == R.id.friends) {
                    if (stackCheckOrder.peek() != bottomBar.getCurrentTabId()){
                        stackCheckOrder.push(bottomBar.getCurrentTabId());
                    }
                    Fragment fragment = new FriendsFragment();
                    fragmentManager.beginTransaction().replace(R.id.newFrame, fragment)
                    .addToBackStack("friends").commit();

                } */
                else if (tabId == R.id.home) {
                    if (stackCheckOrder.peek() != bottomBar.getCurrentTabId()){
                        stackCheckOrder.push(bottomBar.getCurrentTabId());
                    }
                    Fragment fragment = new HomeFragment();
                    fragmentManager.beginTransaction().replace(R.id.newFrame, fragment).
                            addToBackStack("home").commit();
                } else if (tabId == R.id.hotSpot) {
                    if (stackCheckOrder.peek() != bottomBar.getCurrentTabId()){
                        stackCheckOrder.push(bottomBar.getCurrentTabId());
                    }
                    Fragment fragment = new HotSpotFragment();
                    fragmentManager.beginTransaction().replace(R.id.newFrame, fragment)
                            .addToBackStack("hotSpot").commit();
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.action_bar, menu);
        return true;
    }

    //Show messages
    public void showMessages(MenuItem item){
        Toast.makeText(this, "Show messages", Toast.LENGTH_LONG).show();
    }

    //Show friends
    public void showFriends(MenuItem item){
        if (stackCheckOrder.peek() != bottomBar.getCurrentTabId()){
            stackCheckOrder.push(bottomBar.getCurrentTabId());
        }
        Fragment fragment = new FriendsFragment();
        fragmentManager.beginTransaction().replace(R.id.newFrame, fragment)
                .addToBackStack("friends").commit();
    }

    //Show settings
    public void showSettings(MenuItem item){
        Toast.makeText(this, "Show settings", Toast.LENGTH_LONG).show();
    }


    //Need to make custom on backPressed, to only work for fragments.
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
