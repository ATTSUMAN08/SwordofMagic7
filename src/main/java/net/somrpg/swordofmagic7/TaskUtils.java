package net.somrpg.swordofmagic7;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongFunction;

public class TaskUtils {

    private TaskUtils() {
        throw new UnsupportedOperationException("これはユーティリティクラスです。インスタンス化できません");
    }

    public static void runTaskTimerAsync(LongFunction<Boolean> action, long delay, long period) {
        AtomicLong count = new AtomicLong(0);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (Boolean.FALSE.equals(action.apply(count.getAndIncrement()))) {
                    cancel();
                }
            }
        }.runTaskTimerAsynchronously(SomCore.instance, delay, period);
    }
}
