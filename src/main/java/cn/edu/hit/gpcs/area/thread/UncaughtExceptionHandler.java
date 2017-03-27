package cn.edu.hit.gpcs.area.thread;

/**
 * 线程默认异常处理
 */
public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
    }
}