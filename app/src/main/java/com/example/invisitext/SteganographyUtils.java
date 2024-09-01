package com.example.invisitext;

import android.graphics.Bitmap;
import android.graphics.Color;

public class SteganographyUtils {

    public static Bitmap encryptTextIntoImage(Bitmap image, String text) {
        String binaryText = stringToBinary(text);
        int imageCapacity = image.getWidth() * image.getHeight() * 3;
        int textLength = binaryText.length();

        int maxTextLength = imageCapacity - 32;
        if (textLength > maxTextLength) {
            throw new IllegalArgumentException("Text is too long to be encrypted within the image.");
        }

        Bitmap encryptedImage = image.copy(Bitmap.Config.ARGB_8888, true);
        String binaryLength = String.format("%32s", Integer.toBinaryString(textLength)).replace(' ', '0');

        int lengthIndex = 0;
        for (int i = 0; i < 32; i++) {
            int pixel = encryptedImage.getPixel(i, 0);
            int alpha = Color.alpha(pixel);
            int red = Color.red(pixel);
            int green = Color.green(pixel);
            int blue = Color.blue(pixel);

            if (lengthIndex < 32) {
                char bit = binaryLength.charAt(lengthIndex);
                red = modifyLeastSignificantBit(red, bit);
                lengthIndex++;
            }

            pixel = Color.argb(alpha, red, green, blue);
            encryptedImage.setPixel(i, 0, pixel);
        }

        int textIndex = 0;
        for (int y = 1; y < encryptedImage.getHeight(); y++) {
            for (int x = 0; x < encryptedImage.getWidth(); x++) {
                int pixel = encryptedImage.getPixel(x, y);
                int alpha = Color.alpha(pixel);
                int red = Color.red(pixel);
                int green = Color.green(pixel);
                int blue = Color.blue(pixel);

                if (textIndex < textLength) {
                    char bit = binaryText.charAt(textIndex);
                    red = modifyLeastSignificantBit(red, bit);
                    textIndex++;
                }

                if (textIndex < textLength) {
                    char bit = binaryText.charAt(textIndex);
                    green = modifyLeastSignificantBit(green, bit);
                    textIndex++;
                }

                if (textIndex < textLength) {
                    char bit = binaryText.charAt(textIndex);
                    blue = modifyLeastSignificantBit(blue, bit);
                    textIndex++;
                }

                pixel = Color.argb(alpha, red, green, blue);
                encryptedImage.setPixel(x, y, pixel);

                if (textIndex >= textLength) {
                    break;
                }
            }
            if (textIndex >= textLength) {
                break;
            }
        }

        return encryptedImage;
    }

    public static String decryptTextFromImage(Bitmap image) {
        StringBuilder binaryLength = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            int pixel = image.getPixel(i, 0);
            int red = Color.red(pixel);
            binaryLength.append(getLeastSignificantBit(red));
        }

        int textLength = Integer.parseInt(binaryLength.toString(), 2);

        StringBuilder binaryText = new StringBuilder();
        int textIndex = 0;
        for (int y = 1; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (textIndex >= textLength) {
                    break;
                }

                int pixel = image.getPixel(x, y);
                int red = Color.red(pixel);
                int green = Color.green(pixel);
                int blue = Color.blue(pixel);

                binaryText.append(getLeastSignificantBit(red));
                textIndex++;
                if (textIndex < textLength) {
                    binaryText.append(getLeastSignificantBit(green));
                    textIndex++;
                }
                if (textIndex < textLength) {
                    binaryText.append(getLeastSignificantBit(blue));
                    textIndex++;
                }

                if (textIndex >= textLength) {
                    break;
                }
            }
            if (textIndex >= textLength) {
                break;
            }
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
            int charCode = Integer.parseInt(binaryChar, 2);
            text.append((char) charCode);
        }
        return text.toString();
    }

    private static int modifyLeastSignificantBit(int value, char bit) {
        return (value & 0xFE) | (bit - '0');
    }

    private static int getLeastSignificantBit(int value) {
        return value & 0x01;
    }
}
