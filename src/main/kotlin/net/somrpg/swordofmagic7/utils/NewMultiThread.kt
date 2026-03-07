package net.somrpg.swordofmagic7.utils

import net.somrpg.swordofmagic7.extensions.runAsync

object NewMultiThread {
    fun runTaskAsync(
        runnable: Runnable,
        threadTag: String = "不明",
    ) {
        runAsync {
            runnable.run()
        }
    }
}
