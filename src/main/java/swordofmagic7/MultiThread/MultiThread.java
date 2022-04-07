package swordofmagic7.MultiThread;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

import static swordofmagic7.Function.Log;
import static swordofmagic7.SomCore.plugin;

public class MultiThread extends Thread {
    public static List<Thread> MultiThreads = new ArrayList<>();
    /*
    private static final List<MultiThreadRunnable> SynchronizedTaskList = new ArrayList<>();
    public static void SynchronizedLoopCaster() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            synchronized (SynchronizedTaskList) {
                if (SynchronizedTaskList.size() > 0) {
                    for (MultiThreadRunnable runnable : SynchronizedTaskList) {
                        try {
                            runnable.run();
                        } catch (Exception e) {
                            for (StackTraceElement stackTrace : MultiThreadRunnable.stackTrace) {
                                Log(stackTrace.getClassName() + " line" + stackTrace.getLineNumber());
                            }
                        }
                    }
                    SynchronizedTaskList.clear();
                }
            }
        }, 0 , 1);
    }
     */

    public static void closeMultiThreads() {
        for (Thread thread : MultiThreads) {
            thread.interrupt();
        }
        MultiThreads.clear();
    }

    private static final boolean log = false;

    public static Thread TaskRun(MultiThreadRunnable runnable, String ThreadTag) {
        if (plugin.isEnabled()) {
            try {
                if (log) Log("TaskRun -> " + ThreadTag);
                Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
            } catch (Exception e) {
                e.printStackTrace();
                Log("タスク実行に失敗しました Task -> " + ThreadTag);
            }
        }
        return Thread.currentThread();
    }

    public static void TaskRunSynchronized(MultiThreadRunnable runnable) {
        TaskRunSynchronized(runnable, "未定義");
    }

    public static void TaskRunSynchronized(MultiThreadRunnable runnable, String ThreadTag) {
        if (plugin.isEnabled()) {
            try {
                Bukkit.getScheduler().runTask(plugin, runnable);
            } catch (Exception e) {
                e.printStackTrace();
                Log("タスク実行に失敗しました Task -> " + ThreadTag);
            }
        }
    }

    public static BukkitTask TaskRunLater(MultiThreadRunnable runnable, int tick, String ThreadTag) {
        if (plugin.isEnabled()) {
            try {
                if (log) Log("TaskRunLater -> " + ThreadTag);
                return Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, tick);
            } catch (Exception e) {
                e.printStackTrace();
                Log("タスク実行に失敗しました Task -> " + ThreadTag);
            }
        }
        return null;
    }

    public static void TaskRunSynchronizedLater(MultiThreadRunnable runnable, int tick) {
        TaskRunSynchronizedLater(runnable, tick, "未定義");
    }

    public static void TaskRunTimer(MultiThreadRunnable runnable, int tick) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, 0, tick);
    }

    public static void TaskRunSynchronizedTimer(MultiThreadRunnable runnable, int tick, String ThreadTag) {
        if (log) Log("TaskRunSynchronizedTimer -> " + ThreadTag);
        Bukkit.getScheduler().runTaskTimer(plugin, runnable, 0, tick);
    }

    public static void TaskRunSynchronizedLater(MultiThreadRunnable runnable, int tick, String ThreadTag) {
        if (plugin.isEnabled()) {
            try {
                Bukkit.getScheduler().runTaskLater(plugin, runnable, tick);
            } catch (Exception e) {
                e.printStackTrace();
                Log("タスク実行に失敗しました Task -> " + ThreadTag);
            }
        }
    }

    public static void sleepMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {}
    }

    public static void sleepTick(long tick) {
        try {
            Thread.sleep(tick*50);
        } catch (InterruptedException ignored) {}
    }

    public static void stopTask() {
        Thread.currentThread().interrupt();
    }

    /*
    public static Thread TaskRun(MultiThreadRunnable runnable, String ThreadTag) {
        Thread thread = new Thread(runnable);
        thread.start();
        MultiThreads.add(thread);
        if (ThreadTag != null) thread.setName(ThreadTag);
        return thread;
    }
    */

    /*
    public static void TaskRunSynchronized(MultiThreadRunnable runnable) {
        synchronized (SynchronizedTaskList) {
            SynchronizedTaskList.add(runnable);
        }
    }
    */
}
