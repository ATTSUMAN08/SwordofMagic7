@file:Suppress("unused")
package net.somrpg.swordofmagic7.commands.developer

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.math.BlockVector3
import net.somrpg.swordofmagic7.SomCore
import net.somrpg.swordofmagic7.extensions.asyncDispatcher
import net.somrpg.swordofmagic7.translater.JapanizeType
import net.somrpg.swordofmagic7.translater.Japanizer
import net.somrpg.swordofmagic7.utils.SchematicUtils
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission
import swordofmagic7.Data.DataBase
import swordofmagic7.Data.PlayerData
import swordofmagic7.Data.PlayerData.playerData
import swordofmagic7.MultiThread.MultiThread
import java.io.File

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
        sender.sendMessage("変換済みメッセージ: ${Japanizer.japanize(message, JapanizeType.GOOGLE_IME)}")
        sender.sendMessage("変換が必要か: ${Japanizer.isNeedToJapanize(message)}")
    }

    @Command("som test")
    @Permission("som7.developer")
    fun somTest(sender: CommandSender) {
        if (sender !is Player) return
        PlayerData.playerData(sender).dead()
    }

    @Command("som test2")
    @Permission("som7.developer")
    fun somTest2(sender: CommandSender) {
        if (sender !is Player) return

        val file = File(SomCore.instance.dataFolder, "input.txt")
        if (!file.exists()) {
            sender.sendMessage("ファイルが存在しません")
            return
        }

        file.forEachLine {
            val locText = it.split(",").map { it.toDouble() }
            val loc = Location(sender.world, locText[0], locText[1], locText[2])

            SomCore.instance.launch(SomCore.instance.minecraftDispatcher) {
                loc.block.type = Material.SAND
            }
        }
        sender.sendMessage("完了")
    }

    @Command("som test3")
    @Permission("som7.developer")
    fun somTest3(sender: CommandSender) {
        if (sender !is Player) return

        sender.sendMessage("${sender.inventory.itemInMainHand.itemMeta.asComponentString}")
    }

    @Command("som paste <schematic>")
    @Permission("som7.developer")
    fun somPaste(sender: CommandSender, @Argument("schematic") schematic: String) {
        if (sender !is Player) return
        SomCore.instance.launch(asyncDispatcher) {
            SchematicUtils.paste(schematic, BukkitAdapter.adapt(sender.world), BlockVector3.at(sender.x, sender.y, sender.z), true)
        }
    }
}