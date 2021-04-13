package com.example.instaclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.instaclone.Fragments.HomeFragment;
import com.example.instaclone.Fragments.NotificationFragment;
import com.example.instaclone.Fragments.ProfileFragment;
import com.example.instaclone.Fragments.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class StartActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private Fragment selectorFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_home:
                        selectorFragment = new HomeFragment();
                        break;
                    case R.id.nav_search:
                        selectorFragment = new SearchFragment();
                        break;
                    case R.id.nav_add:
                        selectorFragment = null;
                        startActivity(new Intent(StartActivity.this,PostActivity.class));
                        break;
                    case R.id.nav_heart:
                        selectorFragment = new NotificationFragment();
                        break;
                    case R.id.nav_profile:
                        selectorFragment = new ProfileFragment();
                        getSharedPreferences("PROFILE",MODE_PRIVATE).edit().putString("profileId", FirebaseAuth.getInstance().getCurrentUser().getUid()).apply();
                        break;
                }

                if(selectorFragment!=null){
                    FragmentManager fm = getSupportFragmentManager();
                    for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                        fm.popBackStack();
                    }
                    fm.beginTransaction().replace(R.id.fragment_container,selectorFragment).commit();
                }
                return true;
            }
        });

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();

    }

}