package net.somrpg.swordofmagic7

import org.bukkit.scheduler.BukkitRunnable
import java.util.concurrent.atomic.AtomicLong
import java.util.function.LongFunction

object TaskUtils {

    init {
        throw UnsupportedOperationException("これはユーティリティクラスです。インスタンス化できません")
    }

    fun runTaskTimerAsync(action: LongFunction<Boolean>, delay: Long, period: Long) {
        val count = AtomicLong(0)

        object : BukkitRunnable() {
            override fun run() {
                if (action.apply(count.getAndIncrement()) == false) {
                    cancel()
                }
            }
        }.runTaskTimerAsynchronously(SomCore.instance, delay, period)
    }
}
