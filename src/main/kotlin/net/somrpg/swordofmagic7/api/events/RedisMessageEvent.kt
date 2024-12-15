@file:Suppress("unused")
package net.somrpg.swordofmagic7.api.events

import net.somrpg.swordofmagic7.SomCore.Companion.gson
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class RedisMessageEvent(
    val channel: String,
    val identifier: String,
    private val message: String
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

    /**
     * メッセージをオブジェクトとして取得します
     *
     * @param clazz オブジェクトのクラス
     * @return オブジェクト
     */
    fun <T> get(clazz: Class<T>): T {
        return gson.fromJson(message, clazz)
    }
}