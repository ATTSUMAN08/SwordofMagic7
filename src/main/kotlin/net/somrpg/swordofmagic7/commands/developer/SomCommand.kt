@file:Suppress("unused")
package net.somrpg.swordofmagic7.commands.developer

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.math.BlockVector3
import me.attsuman08.abysslib.shade.acf.BaseCommand
import me.attsuman08.abysslib.shade.acf.annotation.CommandAlias
import me.attsuman08.abysslib.shade.acf.annotation.CommandPermission
import me.attsuman08.abysslib.shade.acf.annotation.Subcommand
import me.attsuman08.abysslib.shade.acf.annotation.Syntax
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
import swordofmagic7.Data.PlayerData
import swordofmagic7.MultiThread.MultiThread
import java.io.File

@CommandAlias("som")
@CommandPermission("som7.developer")
class SomCommand : BaseCommand() {

    @Subcommand("info")
    fun version(sender: CommandSender) {
        sender.sendMessage("§eサーバー: §a${Bukkit.getServer().name} ${Bukkit.getServer().version}")
        sender.sendMessage("§eプレイヤー数: §a${Bukkit.getOnlinePlayers().size}/${Bukkit.getMaxPlayers()}")
        sender.sendMessage("§eTPS: §a${String.format("%.1f", Bukkit.getTPS()[0])}/20")
        sender.sendMessage("§eMSPT: §a${String.format("%.1f", Bukkit.getAverageTickTime())}/50")
        sender.sendMessage("§eプラグイン:")
        for (plugin in Bukkit.getPluginManager().plugins.map { it.name }) {
            sender.sendMessage("§e- $plugin §a(${getPluginVersion(plugin)})")
        }
    }

    @Suppress("UnstableApiUsage")
    private fun getPluginVersion(name: String): String {
        val plugin = if (name == "NuVotifier") {
            Bukkit.getPluginManager().getPlugin("Votifier")
        } else {
            Bukkit.getPluginManager().getPlugin(name)
        }
        return plugin?.pluginMeta?.version ?: "不明"
    }

    @Subcommand("reload")
    fun reload(sender: CommandSender) {
        for (p in Bukkit.getOnlinePlayers()) {
            PlayerData.playerData(p).saveCloseInventory()
        }
        MultiThread.TaskRunSynchronizedLater({
            Bukkit.getServer().dispatchCommand(sender, "plugman reload swordofmagic7")
        }, 5)
    }

    @Subcommand("lunachat")
    @Syntax("<message>")
    fun somLunaChat(sender: CommandSender, message: String) {
        SomCore.instance.launch(asyncDispatcher) {
            sender.sendMessage("メッセージ: $message")
            sender.sendMessage("変換済みメッセージ: ${Japanizer.japanize(message, JapanizeType.GOOGLE_IME)}")
            sender.sendMessage("変換が必要か: ${Japanizer.isNeedToJapanize(message)}")
        }
    }

    @Subcommand("test")
    fun somTest(sender: CommandSender) {
        if (sender !is Player) return
        PlayerData.playerData(sender).dead()
    }

    @Subcommand("test2")
    fun somTest2(sender: CommandSender) {
        if (sender !is Player) return

        val file = File(SomCore.instance.dataFolder, "input.txt")
        if (!file.exists()) {
            sender.sendMessage("ファイルが存在しません")
            return
        }

        file.forEachLine { line ->
            val locText = line.split(",").map { text -> text.toDouble() }
            val loc = Location(sender.world, locText[0], locText[1], locText[2])

            SomCore.instance.launch(SomCore.instance.minecraftDispatcher) {
                loc.block.type = Material.SAND
            }
        }
        sender.sendMessage("完了")
    }

    @Subcommand("test3")
    fun somTest3(sender: CommandSender) {
        if (sender !is Player) return

        sender.sendMessage("${sender.inventory.itemInMainHand.itemMeta.asComponentString}")
    }

    @Subcommand("paste")
    @Syntax("<schematic>")
    fun somPaste(sender: CommandSender, schematic: String) {
        if (sender !is Player) return
        SomCore.instance.launch(asyncDispatcher) {
            SchematicUtils.paste(schematic, BukkitAdapter.adapt(sender.world), BlockVector3.at(sender.x, sender.y, sender.z),
                ignoreAirBlocks = true,
                bypassCache = true
            )
        }
    }

}