package com.k1ngdr3w.supercontacts;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.EnumMap;
import java.util.Map;

/**
 *
 * Created by k1ngdr3w on 6/28/16.
 */
public class BarcodeWriter {
    // Given a Content String create a PDF417 bitmap
    public static Bitmap encodeStringToBitmap(String contents) throws WriterException {
        //Null check, just b/c
        if (contents == null) {
            return null;
        }
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result = writer.encode(contents, BarcodeFormat.PDF_417, 700, 900, hints);
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? 0xFF000000 : 0xFFFFFFFF;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }
}
