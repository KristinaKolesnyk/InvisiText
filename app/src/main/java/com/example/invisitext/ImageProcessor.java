package com.example.invisitext;

import android.content.Context;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageProcessor {

    private final ImageEncryption imageEncryption;
    private final Context context;
    private final ExecutorService executorService;

    public ImageProcessor(Context context, ImageEncryption imageEncryption) {
        this.imageEncryption = imageEncryption;
        this.context = context;
        this.executorService = Executors.newFixedThreadPool(4); // Пул потоков для асинхронных задач
    }

    // Асинхронное шифрование текста в изображение
    public void encryptTextInImageAsync(Bitmap image, String text, OnImageProcessedListener listener) {
        executorService.submit(() -> {
            Bitmap encryptedImage = encryptTextInImage(image, text);
            listener.onImageProcessed(encryptedImage);
        });
    }

    // Асинхронное дешифрование текста из изображения
    public void decryptTextFromImageAsync(Bitmap image, OnTextDecryptedListener listener) {
        executorService.submit(() -> {
            String decryptedText = decryptTextFromImage(image);
            listener.onTextDecrypted(decryptedText);
        });
    }

    // Синхронное шифрование текста в изображение (может быть вызвано асинхронно)
    public Bitmap encryptTextInImage(Bitmap image, String text) {
        return imageEncryption.encryptText(image, text);
    }

    // Синхронное дешифрование текста из изображения (может быть вызвано асинхронно)
    public String decryptTextFromImage(Bitmap image) {
        try {
            return imageEncryption.decryptText(image);
        } catch (IllegalArgumentException e) {
            Toast.makeText(context, "Failed to decrypt: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return "";
        }
    }

    // Сохранение изображения в галерею
    public void saveImageToGallery(Context context, Bitmap image) {
        String savedImageURL = MediaStore.Images.Media.insertImage(
                context.getContentResolver(),
                image,
                "Encrypted Image",
                "Image with encrypted text"
        );
        if (savedImageURL == null) {
            Toast.makeText(context, "Failed to save image!", Toast.LENGTH_SHORT).show();
        }
    }

    // Интерфейс для передачи результата шифрования в UI поток
    public interface OnImageProcessedListener {
        void onImageProcessed(Bitmap image);
    }

    // Интерфейс для передачи результата дешифрования в UI поток
    public interface OnTextDecryptedListener {
        void onTextDecrypted(String text);
    }
}
