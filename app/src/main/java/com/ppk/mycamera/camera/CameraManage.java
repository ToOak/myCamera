package com.ppk.mycamera.camera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.ppk.mycamera.utils.LogUtil;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import rx.Observable;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.ppk.mycamera.MyApplication.dateFormat;
import static com.ppk.mycamera.MyApplication.startTime;
import static com.ppk.mycamera.utils.BitmapUtil.decodeToBitMap;
import static com.ppk.mycamera.utils.BitmapUtil.renderRGBBitmap;
import static com.ppk.mycamera.utils.BitmapUtil.rotateYUVDegree90;
import static com.ppk.mycamera.utils.BitmapUtil.saveBitmap;

public class CameraManage {
    private Camera camera;
    private Activity activity;

    public CameraManage(Activity activity) {
        this.activity = activity;
    }

    public Camera getCamera() {
        return camera;
    }

    public void initCamera(SurfaceTexture surfaceTexture) {
        camera = openCamera();
        if (camera != null) {
            try {
                camera.setPreviewTexture(surfaceTexture);
                initCamera();
            } catch (IOException e) {
                LogUtil.e("preview display exception: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void initCamera(SurfaceHolder surfaceHolder) {
        camera = openCamera();
        if (camera != null) {
            try {
                camera.setPreviewDisplay(surfaceHolder);
                initCamera();
            } catch (IOException e) {
                LogUtil.e("preview display exception: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void initCamera() {
        if (camera == null) {
            return;
        }
        camera.startPreview();
        CameraThreadPool.createAutoFocusTimerTask(new Runnable() {
            @Override
            public void run() {
                synchronized (CameraManage.class) {
                    if (camera != null) {
                        try {
                            camera.autoFocus(new Camera.AutoFocusCallback() {
                                @Override
                                public void onAutoFocus(boolean success, Camera camera) {
                                }
                            });
                        } catch (Throwable e) {
                            LogUtil.e("createAutoFocusTimerTask throwable: " + e.getMessage());
                            // startPreview是异步实现，可能在某些机器上前几次调用会autofocus failß
                        }
                    }
                }
            }
        });
        camera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(final byte[] data, Camera camera) {
//                        try {
//                            Thread.sleep(1000);
//                            Log.e("oak", "current thread: " + Thread.currentThread().getName());
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        camera.autoFocus(null);
                final int width = camera.getParameters().getPreviewSize().width;
                final int height = camera.getParameters().getPreviewSize().height;
                LogUtil.e("onPreViewFrame: " + width + " " + height);

                Observable.just(data).map(new Func1<byte[], Boolean>() {
                    @Override
                    public Boolean call(byte[] bytes) {
                        LogUtil.e("current thread: " + Thread.currentThread().getName());
                        byte[] rotateBytes = rotateYUVDegree90(data, width, height);
                        Bitmap bitmap = decodeToBitMap(rotateBytes, height, width);
                        saveBitmap(bitmap, new File(activity.getExternalCacheDir(), dateFormat.format(new Date()) + "_pic.jpg"));
                        return true;
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Boolean>() {
                            @Override
                            public void call(Boolean aBoolean) {
                                LogUtil.e("current thread: " + Thread.currentThread().getName());
                            }
                        });
            }
        });
    }

    public void setCameraOrientationAndSize(View view, int width, int height) {
        if (camera == null) {
            return;
        }
        int displayOrientation = getDisplayOrientation();
        camera.setDisplayOrientation(displayOrientation);
        List<Camera.Size> supportedPreviewSizes = camera.getParameters().getSupportedPreviewSizes();
        Camera.Size optimalPreviewSize = getOptimalPreviewSize(supportedPreviewSizes, width, height);
        camera.getParameters().setPictureSize(optimalPreviewSize.width, optimalPreviewSize.height);
        LogUtil.e("setCameraOrientationAndSize: "
                + view.getLeft() + " " + view.getTop()
                + " " + view.getRight() + " " + view.getBottom()
        );
        adjustDisplayRatio(view, displayOrientation);
        LogUtil.e("setCameraOrientationAndSize: "
                + view.getLeft() + " " + view.getTop()
                + " " + view.getRight() + " " + view.getBottom()
        );
//        camera.getParameters().setPictureSize(
//                1280, 720
//        );
    }

    public void stopCamera() {
        if (camera == null) {
            return;
        }
        CameraThreadPool.cancelAutoFocusTimer();
        camera.stopPreview();
    }

    public void resumeCamera(SurfaceHolder holder) {
        if (holder == null || holder.getSurface() == null) {
            return;
        }
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
            CameraThreadPool.createAutoFocusTimerTask(new Runnable() {
                @Override
                public void run() {
                    synchronized (CameraManage.class) {
                        if (camera != null) {
                            try {
                                camera.autoFocus(new Camera.AutoFocusCallback() {
                                    @Override
                                    public void onAutoFocus(boolean success, Camera camera) {
                                    }
                                });
                            } catch (Throwable e) {
                                LogUtil.e("createAutoFocusTimerTask throwable: " + e.getMessage());
                                // startPreview是异步实现，可能在某些机器上前几次调用会autofocus failß
                            }
                        }
                    }
                }
            });
        } catch (IOException e) {
            LogUtil.e("rePreview display exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void resumeCamera(SurfaceTexture texture) {
        if (texture == null) {
            return;
        }
        try {
            camera.setPreviewTexture(texture);
            camera.startPreview();
            CameraThreadPool.createAutoFocusTimerTask(new Runnable() {
                @Override
                public void run() {
                    synchronized (CameraManage.class) {
                        if (camera != null) {
                            try {
                                camera.autoFocus(new Camera.AutoFocusCallback() {
                                    @Override
                                    public void onAutoFocus(boolean success, Camera camera) {
                                    }
                                });
                            } catch (Throwable e) {
                                LogUtil.e("createAutoFocusTimerTask throwable: " + e.getMessage());
                                // startPreview是异步实现，可能在某些机器上前几次调用会autofocus failß
                            }
                        }
                    }
                }
            });
        } catch (IOException e) {
            LogUtil.e("rePreview display exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void releaseCamera() {
        CameraThreadPool.cancelAutoFocusTimer();
        if (camera == null) {
            return;
        }
        camera.setPreviewCallback(null);
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    private Camera openCamera() {
//        return Camera.open(1);
        return Camera.open();
    }

    /**
     * 获取最佳的分辨率 而且是16：9的
     *
     * @param sizes
     * @param w
     * @param h
     * @return
     */
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.75;
        double targetRatio = (double) w / h;
        if (sizes == null)
            return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    /**
     * 这里是一小段算法算出摄像头转多少都和屏幕方向一致
     *
     * @return
     */
    private int getDisplayOrientation() {
        WindowManager manager = activity.getWindowManager();
        Display display = manager.getDefaultDisplay();
        int orientation = display.getOrientation();
        int degress = 0;
        switch (orientation) {
            case Surface.ROTATION_0: {
                degress = 0;
                break;
            }
            case Surface.ROTATION_90:
                degress = 90;
                break;
            case Surface.ROTATION_180:
                degress = 180;
                break;
            case Surface.ROTATION_270:
                degress = 270;
                break;
        }
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, cameraInfo);
        int result = (cameraInfo.orientation - degress + 360) % 360;
        return result;
    }

    /**
     * 预览图片拉伸处理
     *
     * @param rotation
     */
    private void adjustDisplayRatio(View view, int rotation) {
        if (camera == null) {
            return;
        }
        ViewGroup parent = ((ViewGroup) view.getParent());
        Rect rect = new Rect();
        parent.getLocalVisibleRect(rect);
        int width = rect.width();
        int height = rect.height();
        Camera.Size previewSize = camera.getParameters().getPreviewSize();
        int previewWidth;
        int previewHeight;
        if (rotation == 90 || rotation == 270) {
            previewWidth = previewSize.height;
            previewHeight = previewSize.width;
        } else {
            previewWidth = previewSize.width;
            previewHeight = previewSize.height;
        }

//        https://www.polarxiong.com/archives/Android%E7%9B%B8%E6%9C%BA%E5%BC%80%E5%8F%91-%E5%9B%9B-%E6%97%8B%E8%BD%AC%E4%B8%8E%E7%BA%B5%E6%A8%AA%E6%AF%94.html
        // if camera's preview ratio of height and width is more than parent's layout container's, so that make parents's height
        // assign to camera preview's height, and make preview's width convert into: newHeight * width / oldHeight. Vice versa.
        // From (width / height) < (previewWidth / previewHeight)
//        if (CameraConfig.getInstance().getmCameraType() == CameraConfig.RK3288_LAMP) {
//            //适配特殊的设备 1080*810 显示区域
//            int surePreHeight = 1200;
//            final int scaledChildWidth = surePreHeight * previewWidth / previewHeight;
//            layout((width - scaledChildWidth)/2, (height - 1200) / 2, (scaledChildWidth + width) / 2, (height + 1200) / 2);
////            layout(0, (height - surePreHeight) / 2 + deviation, scaledChildWidth, (height + surePreHeight) / 2 + deviation);
//
//        } else {
        if (width * previewHeight < height * previewWidth) {
            // align center-vertical of parent's container
            final int scaledChildWidth = previewWidth * height / previewHeight;
            view.layout((width - scaledChildWidth) / 2, 0, (width + scaledChildWidth) / 2, height);
        } else {
            final int scaledChildHeight = previewHeight * width / previewWidth;
            view.layout(0, (height - scaledChildHeight) / 2, width, (height + scaledChildHeight) / 2);
        }
//        }
    }

}
