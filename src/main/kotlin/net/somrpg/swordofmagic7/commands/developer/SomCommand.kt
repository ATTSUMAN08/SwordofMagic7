@file:Suppress("unused")
package net.somrpg.swordofmagic7.commands.developer

import net.somrpg.swordofmagic7.translater.JapanizeType
import net.somrpg.swordofmagic7.translater.Japanizer
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission
import swordofmagic7.Data.PlayerData.playerData
import swordofmagic7.MultiThread.MultiThread

@Suppress("UnstableApiUsage")
class SomCommand {

    @Command("som info")
    @Permission("som7.developer")
    fun somVersion(sender: CommandSender) {
        sender.sendMessage("§e現在のスレッド: §a${Thread.currentThread().name}")
        sender.sendMessage("§eサーバー: §a${Bukkit.getServer().name} ${Bukkit.getServer().version}")
        sender.sendMessage("§eプレイヤー数: §a${Bukkit.getOnlinePlayers().size}/${Bukkit.getMaxPlayers()}")
        sender.sendMessage("§eTPS: §a${String.format("%.1f", Bukkit.getTPS()[0])}")
        sender.sendMessage("§eMSPT: §a${String.format("%.1f", Bukkit.getAverageTickTime())}")
        sender.sendMessage("§eプラグイン:")
        for (depend in Bukkit.getPluginManager().plugins.map { it.name }) {
            sender.sendMessage("§e- $depend §a(${getVersion(depend)})")
        }
    }

    private fun getVersion(name: String): String {
        val plugin = if (name == "NuVotifier") {
            Bukkit.getPluginManager().getPlugin("Votifier")
        } else {
            Bukkit.getPluginManager().getPlugin(name)
        }
        return plugin?.pluginMeta?.version ?: "不明"
    }

    @Command("som reload")
    @Permission("som7.developer")
    fun somReload(sender: CommandSender) {
        for (p in Bukkit.getOnlinePlayers()) {
            playerData(p).saveCloseInventory()
        }
        MultiThread.TaskRunSynchronizedLater({
            Bukkit.getServer().dispatchCommand(sender, "plugman reload swordofmagic7")
        }, 5)
    }

    @Command("som lunachat <message>")
    @Permission("som7.developer")
    fun somLunaChat(sender: CommandSender, @Argument("message") message: String) {
        sender.sendMessage("メッセージ: $message")
        sender.sendMessage("変換済みメッセージ: ${Japanizer.japanize(message, JapanizeType.GOOGLE_IME, emptyMap())}")
        sender.sendMessage("変換が必要か: ${Japanizer.isNeedToJapanize(message)}")
    }
}