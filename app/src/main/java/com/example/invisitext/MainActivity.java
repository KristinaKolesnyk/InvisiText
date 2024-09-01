package com.example.invisitext;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private final int GALLERY_PERMISSION_REQUEST_CODE = 100;

    private BottomNavigationView bottomNavigationView;
    private EncryptTextFragment encryptTextFragment;
    private DecryptTextFragment decryptTextFragment;
    private FrameLayout frameEncrypt, frameDecrypt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();
        initializeViews();
        requestGalleryPermissions();


        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.action_encrypt) {
                    frameDecrypt.setVisibility(View.INVISIBLE);
                    frameEncrypt.setVisibility(View.VISIBLE);
                    encryptTextFragment.clearData();
                    return true;
                } else if (item.getItemId() == R.id.action_decrypt) {
                    frameDecrypt.setVisibility(View.VISIBLE);
                    frameEncrypt.setVisibility(View.INVISIBLE);
                    decryptTextFragment.clearData();
                    return true;
                }
                return false;
            }
        });

    }

    private void requestGalleryPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE,
                            android.Manifest.permission.READ_MEDIA_IMAGES},
                    GALLERY_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GALLERY_PERMISSION_REQUEST_CODE) {
            // Check if the permissions were granted
            if (!(grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {

                requestGalleryPermissions();
            }
        }

    }

    private void initializeViews() {

        encryptTextFragment = new EncryptTextFragment(this);
        decryptTextFragment = new DecryptTextFragment(this);


        getSupportFragmentManager().beginTransaction().add(R.id.frame_encrypt, encryptTextFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.frame_decrypt, decryptTextFragment).commit();

        frameDecrypt.setVisibility(View.INVISIBLE);
        frameEncrypt.setVisibility(View.VISIBLE);
    }

    private void findViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        frameEncrypt = findViewById(R.id.frame_encrypt);
        frameDecrypt = findViewById(R.id.frame_decrypt);

    }
}