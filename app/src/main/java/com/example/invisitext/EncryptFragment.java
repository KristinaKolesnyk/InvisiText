package com.example.invisitext;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.button.MaterialButton;

public class EncryptFragment extends Fragment {

    private EncryptUIHandler encryptUIHandler;
    private ImageView encryptImageAttached;
    private EditText encryptText;
    private Bitmap selectedImage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImageEncryption imageEncryption = new ImageEncryption();
        ImageProcessor imageProcessor = new ImageProcessor(requireContext(), imageEncryption);
        encryptUIHandler = new EncryptUIHandler(requireContext(), imageProcessor);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_encrypt, container, false);
        setupUI(root);
        return root;
    }

    private void setupUI(View root) {
        encryptImageAttached = root.findViewById(R.id.encrypt_img_attached);
        encryptText = root.findViewById(R.id.encrypt_TXT_text);
        MaterialButton encryptButton = root.findViewById(R.id.encrypt_BTN_encrypt);

        encryptImageAttached.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                pickImageFromGallery();
            } else {
                Toast.makeText(requireContext(), "No permissions to access gallery!", Toast.LENGTH_LONG).show();
            }
        });

        encryptButton.setOnClickListener(v -> {
            if (selectedImage != null) {
                String secretText = encryptText.getText().toString();
                encryptUIHandler.encryptImageAsync(selectedImage, secretText, encryptedImage -> {
                    requireActivity().runOnUiThread(() -> {
                        // Вызов saveImageToGallery напрямую из imageProcessor
                        encryptUIHandler.imageProcessor.saveImageToGallery(requireContext(), encryptedImage);
                        Toast.makeText(requireContext(), "Encrypted image saved!", Toast.LENGTH_SHORT).show();
                    });
                });
            } else {
                Toast.makeText(requireContext(), "Please select an image first.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 100);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            Glide.with(this)
                    .asBitmap()
                    .load(imageUri)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            selectedImage = resource;
                            encryptImageAttached.setImageBitmap(selectedImage);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            // Очищение ресурсов, если требуется
                        }
                    });
        }
    }
}
