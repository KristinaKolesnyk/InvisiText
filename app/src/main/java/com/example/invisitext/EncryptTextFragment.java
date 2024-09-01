package com.example.invisitext;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public class EncryptTextFragment extends Fragment {

    private MaterialButton encryptBtnEncrypt;
    private ImageView encryptImgAttached;
    private EditText encryptTxtText;
    private Bitmap selectedImageBitmap;
    private Activity parentActivity;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    public EncryptTextFragment(Activity parentActivity) {
        this.parentActivity = parentActivity;
        this.selectedImageBitmap = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_encrypt, container, false);
        initializeViews(root);
        registerActivityResult();
        setupEncryptButtonListener();
        setupImageClickListener();
        return root;
    }

    private void initializeViews(View root) {
        encryptBtnEncrypt = root.findViewById(R.id.encrypt_BTN_encrypt);
        encryptImgAttached = root.findViewById(R.id.encrypt_img_attached);
        encryptTxtText = root.findViewById(R.id.encrypt_TXT_text);
    }

    private void setupEncryptButtonListener() {
        encryptBtnEncrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInputValid()) {
                    Bitmap encryptedImage = SteganographyUtils.encryptTextIntoImage(selectedImageBitmap, encryptTxtText.getText().toString());
                    saveEncryptedImage(encryptedImage);
                    showToast("Encrypted image saved!");
                }
            }
        });
    }

    private boolean isInputValid() {
        return !encryptTxtText.getText().toString().isEmpty() && selectedImageBitmap != null;
    }

    private void setupImageClickListener() {
        encryptImgAttached.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
            @Override
            public void onClick(View v) {
                if (isPermissionGranted(android.Manifest.permission.READ_MEDIA_IMAGES)) {
                    pickImage();
                } else {
                    showToast("No permissions to access gallery!");
                }
            }
        });
    }

    private void pickImage() {
        Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        activityResultLauncher.launch(intent);
    }

    private void registerActivityResult() {
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        handleImageResult(result);
                    }
                }
        );
    }

    private void handleImageResult(ActivityResult result) {
        try (InputStream inputStream = parentActivity.getContentResolver().openInputStream(getImageUri(result))) {
            selectedImageBitmap = BitmapFactory.decodeStream(inputStream);
            encryptImgAttached.setImageBitmap(selectedImageBitmap);
        } catch (Exception e) {
            handleImageError();
        }
    }

    private Uri getImageUri(ActivityResult result) {
        return result.getData().getData();
    }

    private void handleImageError() {
        showToast("No image selected!");
        clearData();
    }

    private void saveEncryptedImage(Bitmap bitmap) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_" + System.currentTimeMillis() + ".png");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "YourAppName");

        ContentResolver contentResolver = parentActivity.getContentResolver();
        Uri uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        try (OutputStream outputStream = contentResolver.openOutputStream(uri)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            finalizeImageSave(contentResolver, uri, values);
            MediaScannerConnection.scanFile(parentActivity, new String[]{uri.toString()}, null, null);
        } catch (Exception e) {
            handleSaveImageError(e, uri, contentResolver);
        }
    }

    private void finalizeImageSave(ContentResolver contentResolver, Uri uri, ContentValues values) {
        values.clear();
        values.put(MediaStore.Images.Media.IS_PENDING, 0);
        contentResolver.update(uri, values, null, null);
    }

    private void handleSaveImageError(Exception e, Uri uri, ContentResolver contentResolver) {
        showToast("Failed to save image: " + e.getMessage());
        if (uri != null) {
            contentResolver.delete(uri, null, null);
        }
    }

    private boolean isPermissionGranted(String permission) {
        return ContextCompat.checkSelfPermission(parentActivity, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void showToast(String message) {
        Toast.makeText(parentActivity, message, Toast.LENGTH_LONG).show();
    }

    public void clearData() {
        encryptImgAttached.setImageResource(R.drawable.attach_image);
        encryptTxtText.setText("");
    }
}
