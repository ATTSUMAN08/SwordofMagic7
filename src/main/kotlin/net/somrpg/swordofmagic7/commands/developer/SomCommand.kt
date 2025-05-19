@file:Suppress("unused")
package net.somrpg.swordofmagic7.commands.developer

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import me.attsuman08.abysslib.shade.acf.BaseCommand
import me.attsuman08.abysslib.shade.acf.annotation.CommandAlias
import me.attsuman08.abysslib.shade.acf.annotation.CommandPermission
import me.attsuman08.abysslib.shade.acf.annotation.Subcommand
import me.attsuman08.abysslib.shade.acf.annotation.Syntax
import net.kyori.adventure.text.Component
import net.somrpg.swordofmagic7.SomCore
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Display
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import swordofmagic7.Data.PlayerData
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

    @Subcommand("tasks")
    fun tasks(sender: CommandSender) {
        val tasks = Bukkit.getScheduler().activeWorkers.filter { bukkitWorker ->
            bukkitWorker.owner == SomCore.instance
        }

        for (task in tasks) {
            sender.sendMessage("§e- ${task.taskId} | ${task.thread.name}")
        }
        sender.sendMessage("${tasks.size} tasks")
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
        Bukkit.getServer().dispatchCommand(sender, "plugman reload swordofmagic7")
    }

    @Subcommand("test2")
    fun somTest2(sender: Player) {

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

    @Subcommand("paste")
    @Syntax("<schem>")
    fun paste(player: Player, schem: String) {
        player.performCommand("schem load $schem")
        player.performCommand("/paste")
    }

    @Subcommand("text_display")
    fun textDisplay(sender: Player) {
        val playerData = PlayerData.playerData(sender)
        val textDisplay = sender.world.spawnEntity(sender.location.clone().apply { pitch = 0F }, EntityType.TEXT_DISPLAY) as TextDisplay
        textDisplay.billboard = Display.Billboard.VERTICAL
        val text = playerData.ViewBar.nameTagText
        textDisplay.backgroundColor = Color.fromARGB(0, 0, 0, 0)
        textDisplay.text(Component.text(text).appendNewline().append(Component.empty()))
        sender.addPassenger(textDisplay)
    }

}