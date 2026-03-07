package net.somrpg.swordofmagic7.extensions

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import kotlinx.coroutines.withContext
import net.somrpg.swordofmagic7.SomCore
import org.bukkit.Bukkit
import java.util.concurrent.TimeUnit

fun runAsync(block: suspend () -> Unit) {
    SomCore.instance.launch(SomCore.instance.asyncDispatcher) {
        block()
    }
}

fun runAsyncLater(
    delay: Long,
    timeUnit: TimeUnit,
    block: suspend () -> Unit,
) {
    val ticks = timeUnit.toMillis(delay) / 50L
    Bukkit.getScheduler().runTaskLater(
        SomCore.instance,
        Runnable {
            SomCore.instance.launch(SomCore.instance.asyncDispatcher) {
                block()
            }
        },
        ticks,
    )
}

fun runSync(block: suspend () -> Unit) {
    SomCore.instance.launch(SomCore.instance.minecraftDispatcher) {
        block()
    }
}

fun runSyncLater(
    delay: Long,
    timeUnit: TimeUnit,
    block: suspend () -> Unit,
) {
    val ticks = timeUnit.toMillis(delay) / 50L
    Bukkit.getScheduler().runTaskLater(
        SomCore.instance,
        Runnable {
            SomCore.instance.launch(SomCore.instance.minecraftDispatcher) {
                block()
            }
        },
        ticks,
    )
}

suspend fun <T> withMinecraftContext(block: suspend () -> T): T =
    withContext(SomCore.instance.minecraftDispatcher) {
        block()
    }

suspend fun <T> withAsyncContext(block: suspend () -> T): T =
    withContext(SomCore.instance.asyncDispatcher) {
        block()
    }

/**
 * 指定した確率でランダムにtrueを返す。
 *
 * @param chance 確率（0〜100）
 * @return 確率に基づいてtrueまたはfalseを返す。
 */
fun randomCheck(chance: Double): Boolean = Math.random() < (chance / 100.0)