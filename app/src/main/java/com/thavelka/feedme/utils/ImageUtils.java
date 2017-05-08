package com.thavelka.feedme.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import timber.log.Timber;

public class ImageUtils {

    public static byte[] getImageBytes(Bitmap image, Bitmap.CompressFormat format, int quality) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            image.compress(format, quality, outputStream);
            byte[] imageData = outputStream.toByteArray();
            outputStream.close();
            return imageData;
        } catch (IOException e) {
            Timber.e(e);
            return null;
        }
    }

    public static Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        int r = Math.min(bitmap.getWidth(), bitmap.getHeight()) / 2;
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2, r, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }
}
