package net.somrpg.swordofmagic7.utils

import com.github.shynixn.mccoroutine.bukkit.launch
import net.somrpg.swordofmagic7.SomCore
import net.somrpg.swordofmagic7.extensions.asyncDispatcher

object NewMultiThread {

    fun runTaskAsync(runnable: Runnable, threadTag: String = "不明") {
        SomCore.instance.launch(asyncDispatcher) {
            Thread.currentThread().name = "${Thread.currentThread().name} [${threadTag}]"
            try {
                runnable.run()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}