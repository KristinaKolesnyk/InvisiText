package com.example.invisitext;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.Toast;

public class EncryptUIHandler {
    public final ImageProcessor imageProcessor; // Изменение доступа на public
    private final Context context;

    public EncryptUIHandler(Context context, ImageProcessor imageProcessor) {
        this.context = context;
        this.imageProcessor = imageProcessor;
    }

    public void encryptImageAsync(Bitmap selectedImage, String secretText, ImageProcessor.OnImageProcessedListener listener) {
        if (selectedImage != null && !secretText.isEmpty()) {
            imageProcessor.encryptTextInImageAsync(selectedImage, secretText, listener);
        } else {
            Toast.makeText(context, "Please select an image and enter the text.", Toast.LENGTH_SHORT).show();
        }
    }
}
