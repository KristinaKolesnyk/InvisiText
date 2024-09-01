package com.example.invisitext;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.material.snackbar.Snackbar;

import java.io.InputStream;

public class DecryptTextFragment extends Fragment {

    private final Activity parentActivity;
    private ImageView decryptImgAttached;
    private TextView decryptTxtEncryptedData;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    public DecryptTextFragment(Activity parentActivity) {
        this.parentActivity = parentActivity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_decrypt, container, false);
        initializeViews(root);
        registerActivityResult();
        setupImageClickListener();
        return root;
    }

    private void initializeViews(View root) {
        decryptImgAttached = root.findViewById(R.id.decrypt_img_attached);
        decryptTxtEncryptedData = root.findViewById(R.id.decrypt_TXT_encryptedData);
    }

    private void setupImageClickListener() {
        decryptImgAttached.setOnClickListener(v -> {
            if (isPermissionGranted(android.Manifest.permission.READ_MEDIA_IMAGES)) {
                pickImage();
            } else {
                showSnackbar(v, "No permissions to access gallery!");
            }
        });
    }

    private boolean isPermissionGranted(String permission) {
        return ContextCompat.checkSelfPermission(parentActivity, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void pickImage() {
        Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        activityResultLauncher.launch(intent);
    }

    private void registerActivityResult() {
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::handleImageResult
        );
    }

    private void handleImageResult(ActivityResult result) {
        try (InputStream inputStream = parentActivity.getContentResolver().openInputStream(getImageUri(result))) {
            Bitmap selectedImage = BitmapFactory.decodeStream(inputStream);
            decryptImgAttached.setImageBitmap(selectedImage);
            displayDecryptedText(selectedImage);
        } catch (Exception e) {
            handleImageError();
        }
    }

    private Uri getImageUri(ActivityResult result) {
        return result.getData().getData();
    }

    private void displayDecryptedText(Bitmap selectedImage) {
        try {
            String text = SteganographyUtils.decryptTextFromImage(selectedImage);
            decryptTxtEncryptedData.setText(text);
        } catch (Exception err) {
            decryptTxtEncryptedData.setText("No data encrypted!");
        }
    }

    private void handleImageError() {
        clearData();
        showSnackbar(decryptImgAttached, "No image selected!");
    }

    private void showSnackbar(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }

    public void clearData() {
        decryptImgAttached.setImageResource(R.drawable.download);
        decryptTxtEncryptedData.setText("");
    }
}
