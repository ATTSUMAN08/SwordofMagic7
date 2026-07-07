package net.somrpg.swordofmagic7.utils

import net.somrpg.swordofmagic7.SomCore

object SomLogger {
    fun log(
        message: String,
        consoleOnly: Boolean = false,
    ) {
        SomCore.instance.logger.info(message)
    }
}
