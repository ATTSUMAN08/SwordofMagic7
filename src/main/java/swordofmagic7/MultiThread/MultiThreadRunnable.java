package swordofmagic7.MultiThread;

public interface MultiThreadRunnable extends Runnable {
    StackTraceElement[] stackTrace = new Throwable().getStackTrace();;
}
