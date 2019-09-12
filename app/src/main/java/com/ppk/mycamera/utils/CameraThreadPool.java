package com.ppk.mycamera.utils;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraThreadPool {

    private static Timer timerFocus = null;

    /*
     * 对焦频率
     */
    private static final long cameraScanInterval = 1600;

    /*
     * 线程池大小
     */
    private static int poolCount = Runtime.getRuntime().availableProcessors();

    private static ExecutorService fixedThreadPool = Executors.newFixedThreadPool(poolCount);

    /**
     * 给线程池添加任务
     *
     * @param runnable 任务
     */
    public static void execute(Runnable runnable) {
        fixedThreadPool.execute(runnable);
    }

    /**
     * 创建一个定时对焦的timer任务
     *
     * @param runnable 对焦代码
     * @return Timer Timer对象，用来终止自动对焦
     */
    public static Timer createAutoFocusTimerTask(final Runnable runnable) {
        if (timerFocus != null) {
            return timerFocus;
        }
        timerFocus = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runnable.run();
            }
        };
        timerFocus.scheduleAtFixedRate(task, 0, cameraScanInterval);
        return timerFocus;
    }

    /**
     * 终止自动对焦任务，实际调用了cancel方法并且清空对象
     * 但是无法终止执行中的任务，需额外处理
     */
    public static void cancelAutoFocusTimer() {
        if (timerFocus != null) {
            timerFocus.cancel();
            timerFocus = null;
        }
    }
}
