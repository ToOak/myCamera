package com.ppk.mycamera.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.ExifInterface;
import android.view.View;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.ppk.mycamera.MyApplication.startTime;

public class BitmapUtil {

    private BitmapUtil() {
    }

    /**
     * bitmap 转换成二进制数据流
     *
     * @param bitmap
     * @return
     */
    public static byte[] getBitmapByte(Bitmap bitmap) {
        startTime = System.currentTimeMillis();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new BufferedOutputStream(out));
        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LogUtil.e("getBitmapByte: " + (System.currentTimeMillis() - startTime));
        return out.toByteArray();
    }

    /**
     * 二进制数据流转换成 bitmap
     *
     * @param temp
     * @return
     */
    public static Bitmap getBitmapFromByte(byte[] temp) {
        if (temp != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(temp, 0, temp.length);
            return bitmap;
        } else {
            return null;
        }
    }


    /**
     * 保存bitmap
     *
     * @param bitmap     bitmap
     * @param outputFile like new File(Environment.getExternalStorageDirectory(), "ocr1.jpg")
     */
    public static void saveBitmap(Bitmap bitmap, File outputFile) {
        startTime = System.currentTimeMillis();
        try {
            if (!outputFile.exists()) {
                outputFile.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            LogUtil.e("saveBitmap: " + (System.currentTimeMillis() - startTime));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * 获取位图的YUV数据
     */
    public static byte[] getYUVByBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int size = width * height;

        int pixels[] = new int[size];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        // byte[] data = convertColorToByte(pixels);
        byte[] data = rgb2YCbCr420(pixels, width, height);

        return data;
    }

    private static byte[] rgb2YCbCr420(int[] pixels, int width, int height) {
        int len = width * height;
        // yuv格式数组大小，y亮度占len长度，u,v各占len/4长度。
        byte[] yuv = new byte[len * 3 / 2];
        int y, u, v;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                // 屏蔽ARGB的透明度值
                int rgb = pixels[i * width + j] & 0x00FFFFFF;
                // 像素的颜色顺序为bgr，移位运算。
                int r = rgb & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = (rgb >> 16) & 0xFF;
                // 套用公式
                y = ((66 * r + 129 * g + 25 * b + 128) >> 8) + 16;
                u = ((-38 * r - 74 * g + 112 * b + 128) >> 8) + 128;
                v = ((112 * r - 94 * g - 18 * b + 128) >> 8) + 128;
                // rgb2yuv
                // y = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                // u = (int) (-0.147 * r - 0.289 * g + 0.437 * b);
                // v = (int) (0.615 * r - 0.515 * g - 0.1 * b);
                // RGB转换YCbCr
                // y = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                // u = (int) (-0.1687 * r - 0.3313 * g + 0.5 * b + 128);
                // if (u > 255)
                // u = 255;
                // v = (int) (0.5 * r - 0.4187 * g - 0.0813 * b + 128);
                // if (v > 255)
                // v = 255;
                // 调整
                y = y < 16 ? 16 : (y > 255 ? 255 : y);
                u = u < 0 ? 0 : (u > 255 ? 255 : u);
                v = v < 0 ? 0 : (v > 255 ? 255 : v);
                // 赋值
                yuv[i * width + j] = (byte) y;
                yuv[len + (i >> 1) * width + (j & ~1) + 0] = (byte) u;
                yuv[len + +(i >> 1) * width + (j & ~1) + 1] = (byte) v;
            }
        }
        return yuv;
    }

    /**
     * 质量压缩法
     *
     * @param image bitmap
     * @return bitmap
     */
    public static byte[] compressImageByQuality(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        // 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        //循环判断如果压缩后图片是否大于100kb,大于继续压缩
        while (baos.toByteArray().length / 1024 > 120) {
            //重置baos即清空baos
            baos.reset();
            //每次都减少10
            options -= 10;
            //这里压缩options%，把压缩后的数据存放到baos中
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
        }
//        //把压缩后的数据baos存放到ByteArrayInputStream中
//        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
//        //把ByteArrayInputStream数据生成图片
//        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        return baos.toByteArray();
    }

    /**
     * 先比例压缩再质量压缩
     *
     * @param srcPath
     * @return
     */
    public static byte[] getImageByBoth(String srcPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);//此时返回bm为空

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //现在主流手机比较多是1920*1080分辨率，所以高和宽我们设置为
        float hh = 800f;//这里设置高度为1920f
        float ww = 800f;//这里设置宽度为1080f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
//        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        bitmap = rotateBitmap(BitmapFactory.decodeFile(srcPath, newOpts), srcPath);
        return compressImageByQuality(bitmap);//压缩好比例大小后再进行质量压缩
    }

    /**
     * 获取图片旋转角度
     */
    //判断图片的旋转角度
    public static int getExifOrientation(String filepath) {
        int degree = 0;
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filepath);
        } catch (IOException ex) {
            //Log.e("---->", ex.getMessage());
        }
        if (exif != null) {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                // We only recognize a subset of orientation tag values.
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                    default:
                        break;
                }
            }
        }
        return degree;
    }


    /**
     * 旋转图片
     */
    public static Bitmap rotateBitmap(Bitmap b, String filepath) {
        int degrees = getExifOrientation(filepath);
        if (degrees != 0 && b != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) b.getWidth() / 2, (float) b.getHeight() / 2);
            try {
                Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), m, true);
                if (b != b2) {
                    b.recycle();
                    b = b2;
                }
            } catch (OutOfMemoryError ex) {

            }
        }
        return b;
    }


    public static Bitmap convertViewToBitmap(View view) {
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }

    /**
     * 质量压缩方法
     *
     * @param image
     * @return
     */
    public static Bitmap compressImage(Bitmap image, int max) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 90;
        LogUtil.e("compressImage: " + image.getByteCount() + "\t" + baos.toByteArray().length);
        while (baos.toByteArray().length >= max) { // 循环判断如果压缩后图片是否大于max b,大于继续压缩
            baos.reset(); // 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;// 每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    /**
     * 裁剪后的rgb变成bitmap
     *
     * @param rgb
     * @param w
     * @param h
     * @param noScale
     * @return
     */
    public static Bitmap renderRGBBitmap(byte[] rgb, int w, int h, boolean noScale) {
        startTime = System.currentTimeMillis();
        try {
            int[] pixels = new int[w * h];
            int out = 0;
            int in = 0;
            for (int i = 0; i < h; i++) {
                for (int j = 0; j < w; j++) {
                    int r = rgb[in] & 0xff;
                    int g = rgb[in + 1] & 0xff;
                    int b = rgb[in + 2] & 0xff;
                    pixels[out] = (0xff000000) | (r << 16) | (g << 8) | b;
                    in += 3;
                    out++;
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
            pixels = null;

            Bitmap newBmp = null;
            if (noScale)
                newBmp = Bitmap.createScaledBitmap(bitmap, w, h, true);
            else
                newBmp = Bitmap.createScaledBitmap(bitmap, w * 4, h, true);
            bitmap.recycle();
            LogUtil.e("renderRGBBitmap: " + (System.currentTimeMillis() - startTime));

            return newBmp;
        } catch (Exception e) {
            LogUtil.e("renderRGBBitmap exception: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 对preview的一帧的图片进行90°顺时针旋转
     *
     * @param data
     * @param imageWidth
     * @param imageHeight
     * @return
     */
    public static byte[] rotateYUVDegree90(byte[] data, int imageWidth, int imageHeight) {
        startTime = System.currentTimeMillis();
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
// Rotate the Y luma
        int i = 0;
        for (int x = 0; x < imageWidth; x++) {
            for (int y = imageHeight - 1; y >= 0; y--) {
                yuv[i] = data[y * imageWidth + x];
                i++;
            }
        }
// Rotate the U and V color components
        i = imageWidth * imageHeight * 3 / 2 - 1;
        for (int x = imageWidth - 1; x > 0; x = x - 2) {
            for (int y = 0; y < imageHeight / 2; y++) {
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
                i--;
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + (x - 1)];
                i--;
            }
        }
        LogUtil.e("rotateYUVDegree90: " + (System.currentTimeMillis() - startTime));
        return yuv;
    }


    /**
     * 对preview的一帧的图片变成bitmap
     *
     * @param data
     * @param width
     * @param height
     * @return
     */
    public static Bitmap decodeToBitMap(byte[] data, int width, int height) {
        if (data == null) {
            return null;
        }
        startTime = System.currentTimeMillis();
        try {
            YuvImage image = new YuvImage(data, ImageFormat.NV21, width,
                    height, null);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compressToJpeg(new Rect(0, 0, width, height),
                    100, stream);
            Bitmap bmp = BitmapFactory.decodeByteArray(
                    stream.toByteArray(), 0, stream.size());

            stream.close();
            LogUtil.e("decodeToBitMap: " + (System.currentTimeMillis() - startTime));
            return bmp;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
