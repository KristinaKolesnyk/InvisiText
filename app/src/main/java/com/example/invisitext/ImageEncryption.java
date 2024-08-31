package com.example.invisitext;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

public class ImageEncryption {

    public static Bitmap encryptText(Bitmap image, String text) {
        String binaryText = stringToBinary(text);
        Log.d("ImageEncryption", "Text to encrypt: " + text + " Length: " + text.length());

        int imageCapacity = image.getWidth() * image.getHeight() * 3;
        int textLength = binaryText.length();
        int maxTextLength = imageCapacity - 32;

        if (textLength > maxTextLength) {
            throw new IllegalArgumentException("Text is too long to be encrypted within the image.");
        }

        Bitmap encryptedImage = image.copy(Bitmap.Config.ARGB_8888, true);

        String binaryLength = Integer.toBinaryString(textLength);
        binaryLength = String.format("%32s", binaryLength).replace(' ', '0');

        int lengthIndex = 0;
        for (int i = 0; i < 32; i++) {
            int pixel = encryptedImage.getPixel(i, 0);
            int alpha = Color.alpha(pixel);
            int red = Color.red(pixel);

            if (lengthIndex < 32) {
                char bit = binaryLength.charAt(lengthIndex);
                red = modifyLSB(red, bit);
                lengthIndex++;
            }

            pixel = Color.argb(alpha, red, Color.green(pixel), Color.blue(pixel));
            encryptedImage.setPixel(i, 0, pixel);
        }

        int textIndex = 0;
        for (int y = 1; y < encryptedImage.getHeight(); y++) {
            for (int x = 0; x < encryptedImage.getWidth(); x++) {
                int pixel = encryptedImage.getPixel(x, y);
                int red = Color.red(pixel);

                if (textIndex < textLength) {
                    char bit = binaryText.charAt(textIndex);

                    red = modifyLSB(red, bit);
                    pixel = Color.rgb(red, Color.green(pixel), Color.blue(pixel));
                    encryptedImage.setPixel(x, y, pixel);

                    textIndex++;
                }
            }
        }

        Log.d("ImageEncryption", "Text encrypted successfully");
        return encryptedImage;
    }

    public static String decryptText(Bitmap image) {
        StringBuilder binaryLength = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            int pixel = image.getPixel(i, 0);
            binaryLength.append(getLSB(Color.red(pixel)));
        }

        int textLength = Integer.parseInt(binaryLength.toString(), 2);
        StringBuilder binaryText = new StringBuilder();
        int textIndex = 0;
        for (int y = 1; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getPixel(x, y);
                binaryText.append(getLSB(Color.red(pixel)));

                if (++textIndex >= textLength) break;
            }
            if (textIndex >= textLength) break;
        }

        return binaryToString(binaryText.toString());
    }

    private static String stringToBinary(String text) {
        StringBuilder binaryText = new StringBuilder();
        for (char c : text.toCharArray()) {
            String binaryChar = Integer.toBinaryString(c);
            binaryText.append(String.format("%8s", binaryChar).replace(' ', '0'));
        }
        return binaryText.toString();
    }

    private static String binaryToString(String binaryText) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < binaryText.length(); i += 8) {
            String binaryChar = binaryText.substring(i, i + 8);
            text.append((char) Integer.parseInt(binaryChar, 2));
        }
        return text.toString();
    }

    private static int modifyLSB(int value, char bit) {
        return (value & 0xFE) | (bit - '0');
    }

    private static int getLSB(int value) {
        return value & 0x01;
    }
}
