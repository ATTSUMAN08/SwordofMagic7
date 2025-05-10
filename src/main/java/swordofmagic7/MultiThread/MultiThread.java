package swordofmagic7.MultiThread;

import net.somrpg.swordofmagic7.utils.NewMultiThread;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import static swordofmagic7.Function.Log;
import static net.somrpg.swordofmagic7.SomCore.instance;

public class MultiThread extends Thread {
    /*
    private static final List<MultiThreadRunnable> SynchronizedTaskList = new ArrayList<>();
    public static void SynchronizedLoopCaster() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            synchronized (SynchronizedTaskList) {
                if (SynchronizedTasklist.isEmpty()) {
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

    private static final boolean log = false;

    public static void TaskRun(MultiThreadRunnable runnable, String ThreadTag) {
        if (instance.isEnabled()) {
            if (log) Log("TaskRun -> " + ThreadTag);
            NewMultiThread.INSTANCE.runTaskAsync(runnable, ThreadTag);
        }
    }

    public static void TaskRunSynchronized(MultiThreadRunnable runnable) {
        TaskRunSynchronized(runnable, "未定義");
    }

    public static void TaskRunSynchronized(MultiThreadRunnable runnable, String ThreadTag) {
        if (instance.isEnabled()) {
            try {
                BukkitTask task = Bukkit.getScheduler().runTask(instance, runnable);
            } catch (Exception e) {
                e.printStackTrace();
                Log("タスク実行に失敗しました Task -> " + ThreadTag);
            }
        }
    }

    public static BukkitTask TaskRunLater(MultiThreadRunnable runnable, int tick, String ThreadTag) {
        if (instance.isEnabled()) {
            try {
                if (log) Log("TaskRunLater -> " + ThreadTag);
                BukkitTask task = Bukkit.getScheduler().runTaskLaterAsynchronously(instance, runnable, tick);
                return task;
            } catch (Exception e) {
                e.printStackTrace();
                Log("タスク実行に失敗しました Task -> " + ThreadTag);
            }
        }
        return null;
    }

    public static BukkitTask TaskRunSynchronizedLater(MultiThreadRunnable runnable, int tick) {
        return TaskRunSynchronizedLater(runnable, tick, "未定義");
    }

    public static BukkitTask TaskRunTimer(MultiThreadRunnable runnable, int tick) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(instance, runnable, 0, tick);
    }

    public static BukkitTask TaskRunSynchronizedTimer(MultiThreadRunnable runnable, int tick, String ThreadTag) {
        if (log) Log("TaskRunSynchronizedTimer -> " + ThreadTag);
        return Bukkit.getScheduler().runTaskTimer(instance, runnable, 0, tick);
    }

    public static BukkitTask TaskRunSynchronizedLater(MultiThreadRunnable runnable, int tick, String ThreadTag) {
        if (instance.isEnabled()) {
            try {
                return Bukkit.getScheduler().runTaskLater(instance, runnable, tick);
            } catch (Exception e) {
                e.printStackTrace();
                Log("タスク実行に失敗しました Task -> " + ThreadTag);
            }
        }
        return null;
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
