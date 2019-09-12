package com.ppk.mycamera.utils;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    //    private  File CACHE_ROOT_DIR = new File(Environment.getExternalStorageDirectory(), "ppk");
    private File crash_root_dir;
    private volatile static CrashHandler crashHandler;
    private Context context;
    // 系统默认的UncaughtExceptionHandler处理类
    private Thread.UncaughtExceptionHandler defaultHandler;
    // 用于格式化日期,作为日志文件名的一部分
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    private CrashHandler() {
    }

    public void init(Context context) {
        this.context = context;
        this.crash_root_dir = context.getExternalFilesDir(null);
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public static CrashHandler getInstance() {
        if (crashHandler == null) {
            synchronized (CrashHandler.class) {
                if (crashHandler == null) {
                    crashHandler = new CrashHandler();
                }
            }
        }
        return crashHandler;
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     *
     * @param t
     * @param e
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
//        MobclickAgent.reportError(context, e);
        //将异常信息保存到sd卡中
        dumpExceptionToSDCard(e);

        //将异常信息上传到服务器
        uploadExceptionToServer();

        //将异常信息打印出来
        e.printStackTrace();

        //如果系统提供了默认的异常处理器，则交给系统去结束程序，系统可能是弹出对话框或者是直接闪退
        if (defaultHandler != null) {
            defaultHandler.uncaughtException(t, e);
        } else {
            //杀死进程来结束程序
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }

    /**
     * 将异常信息保存到sd卡中
     *
     * @param ex
     */
    private void dumpExceptionToSDCard(Throwable ex) {

        try {
            String time = dateFormat.format(new Date());
            String fileName = "crash-" + time + ".log";
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File dir = new File(crash_root_dir, "crash");
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File crashFile = new File(dir, fileName);
                if (!crashFile.exists()) {
                    crashFile.createNewFile();
                }

                PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(crashFile)));

                //将手机设备信息也写入异常信息
                dumpPhoneInfo(printWriter);

                ex.printStackTrace(printWriter);

                //关闭流，如果没有关闭流可能写入文件中会失败
                printWriter.close();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 将手机设备信息也写入异常信息
     *
     * @param printWriter
     */
    private void dumpPhoneInfo(PrintWriter printWriter) {


        //printWriter.print("");


    }

    /**
     * 将异常信息上传到服务器
     */
    private void uploadExceptionToServer() {


    }
}
