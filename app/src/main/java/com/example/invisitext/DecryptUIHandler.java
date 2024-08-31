package com.example.invisitext;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.Toast;

public class DecryptUIHandler {
    private final ImageProcessor imageProcessor;
    private final Context context;

    public DecryptUIHandler(Context context, ImageProcessor imageProcessor) {
        this.context = context;
        this.imageProcessor = imageProcessor;
    }

    // Асинхронное дешифрование изображения
    public void decryptImageAsync(Bitmap selectedImage, ImageProcessor.OnTextDecryptedListener listener) {
        if (selectedImage != null) {
            imageProcessor.decryptTextFromImageAsync(selectedImage, listener);
        } else {
            Toast.makeText(context, "Please select an image.", Toast.LENGTH_SHORT).show();
        }
    }
}
