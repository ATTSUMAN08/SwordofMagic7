package net.somrpg.swordofmagic7.api.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

@Suppress("unused")
class RedisMessageEvent(
    val channel: String,
    val identifier: String,
    val message: List<String>
) : Event(true) {
    companion object {
        val handler = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handler
        }
    }

    override fun getHandlers(): HandlerList {
        return handler
    }
}