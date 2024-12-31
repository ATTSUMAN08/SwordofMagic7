package swordofmagic7

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.entity.Player
import swordofmagic7.Data.PlayerData
import swordofmagic7.Sound.SoundList
import swordofmagic7.TextView.TextView
import swordofmagic7.redis.RedisManager

import kotlin.collections.List

import swordofmagic7.Data.DataBase.ServerId
import swordofmagic7.Function.Log
import swordofmagic7.Function.sendMessage

object Client {

    fun sendBroadCast(textView: TextView) {
        send("BroadCast," + textView.toString())
    }

    fun sendPlayerChat(player: Player, textView: TextView) {
        val displayName = PlainTextComponentSerializer.plainText().serialize(player.displayName())
        send("Chat," + TextView()
            .addView(textView)
            .setSender(Function.unColored(displayName))
            .setUUID(player.uniqueId)
            .setSender(player.name)
            .setDisplay(displayName)
            .setMute(!player.hasPermission("snc.chat"))
            .setFrom(ServerId)
            .toString())
    }

    fun sendDisplay(player: Player, textView: TextView) {
        val displayName = PlainTextComponentSerializer.plainText().serialize(player.displayName())
        send("Display," + TextView()
            .addView(textView)
            .setSender(Function.unColored(displayName))
            .setUUID(player.uniqueId)
            .setSender(player.name)
            .setDisplay(displayName)
            .setMute(!player.hasPermission("snc.chat"))
            .setFrom(ServerId)
            .toString())
    }

    fun send(str: String) {
        RedisManager.publishObject("SNC", str)
    }

    fun Trigger(packet: String) {
        val data = packet.split(",").toTypedArray()
        when (data[0]) {
            "BroadCast" -> {
                var sound: SoundList? = null
                var isNatural = false
                for (i in 1 until data.size) {
                    val split = data[i].split(":", limit = 2).toTypedArray()
                    if (split[0].equals("Sound", ignoreCase = true)) {
                        sound = SoundList.valueOf(split[1])
                    } else if (split[0].equals("isNatural", ignoreCase = true)) {
                        isNatural = true
                    }
                }
                Function.BroadCast(textComponentFromPacket(data), sound, isNatural)
            }
            "Chat", "Display" -> {
                var text: TextComponent = Component.empty()
                var from = ""
                var display = ""
                var uuid = ""
                var isMute = false
                for (i in 1 until data.size) {
                    val split = data[i].split(":", limit = 2).toTypedArray()
                    if (split[0].equals("UUID", ignoreCase = true)) {
                        uuid = split[1]
                    } else if (split[0].equals("Display", ignoreCase = true)) {
                        display = split[1]
                    } else if (split[0].equals("From", ignoreCase = true)) {
                        from = split[1]
                    } else if (split[0].equals("isMute", ignoreCase = true)) {
                        isMute = true
                    }
                }
                if (data[0] == "Chat") text = text.append(Component.text("§b[$from] §r$display§a: §r"))
                if (data[0] == "Display") text = text.append(Component.text("§b[$from] §r"))
                text = text.append(textComponentFromPacket(data))
                for (player in PlayerList.get()) {
                    if (player.isOnline) {
                        if (isMute) {
                            if (player.isOp) {
                                var textMuted: TextComponent = Component.empty()
                                textMuted = textMuted.append(Component.text("§4[M]"))
                                textMuted = textMuted.append(text)
                                sendMessage(player, textMuted)
                            } else if (player.uniqueId.toString() == uuid) {
                                sendMessage(player, text)
                            }
                        } else {
                            val playerData = PlayerData.playerData(player)
                            if (uuid == null || !playerData.BlockListAtString().contains(uuid)) sendMessage(player, text)
                        }
                    }
                }
            }
            "Check" -> {}
            else -> Log("§c無効なパケット -> $packet")
        }
    }

    fun textComponentFromPacket(data: Array<String>): Component {
        var finalText: Component = Component.empty()
        var text: Component = Component.empty()
        var hover: Component = Component.empty()
        for (i in 1 until data.size) {
            val split = data[i].split(":", limit = 2).toTypedArray()
            when {
                split[0].equals("Reset", ignoreCase = true) -> {
                    finalText = finalText.append(text)
                    text = Component.empty()
                }
                split[0].equals("Text", ignoreCase = true) -> {
                    text = text.append(Component.text(split[1]))
                }
                split[0].equals("Hover", ignoreCase = true) -> {
                    var first = true
                    for (str in split[1].split("\n").toTypedArray()) {
                        if (!first) hover = hover.appendNewline()
                        hover = hover.append(Component.text(str))
                        first = false
                    }
                    text = text.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, hover))
                    hover = Component.empty()
                }
            }
        }
        finalText = finalText.append(text)
        return finalText
    }
}
