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
    private static final int GALLERY_PERMISSION_REQUEST_CODE = 100;

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
        initializeFragments();
        requestGalleryPermissions();

        bottomNavigationView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);
    }

    private void findViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        frameEncrypt = findViewById(R.id.frame_encrypt);
        frameDecrypt = findViewById(R.id.frame_decrypt);
    }

    private void initializeFragments() {
        encryptTextFragment = new EncryptTextFragment(this);
        decryptTextFragment = new DecryptTextFragment(this);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.frame_encrypt, encryptTextFragment)
                .commit();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.frame_decrypt, decryptTextFragment)
                .commit();

        frameDecrypt.setVisibility(View.INVISIBLE);
        frameEncrypt.setVisibility(View.VISIBLE);
    }

    private void requestGalleryPermissions() {
        if (!isPermissionGranted(android.Manifest.permission.READ_MEDIA_IMAGES) &&
                !isPermissionGranted(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE,
                            android.Manifest.permission.READ_MEDIA_IMAGES},
                    GALLERY_PERMISSION_REQUEST_CODE);
        }
    }

    private boolean isPermissionGranted(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GALLERY_PERMISSION_REQUEST_CODE &&
                !(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
            requestGalleryPermissions();
        }
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_encrypt) {
            showEncryptFragment();
            return true;
        } else if (item.getItemId() == R.id.action_decrypt) {
            showDecryptFragment();
            return true;
        }
        return false;
    }

    private void showEncryptFragment() {
        frameDecrypt.setVisibility(View.INVISIBLE);
        frameEncrypt.setVisibility(View.VISIBLE);
        encryptTextFragment.clearData();
    }

    private void showDecryptFragment() {
        frameDecrypt.setVisibility(View.VISIBLE);
        frameEncrypt.setVisibility(View.INVISIBLE);
        decryptTextFragment.clearData();
    }
}
