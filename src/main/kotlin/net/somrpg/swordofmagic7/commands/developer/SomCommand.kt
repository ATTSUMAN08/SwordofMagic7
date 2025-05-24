@file:Suppress("unused")
package net.somrpg.swordofmagic7.commands.developer

import com.github.shynixn.mccoroutine.bukkit.launch
import kotlinx.coroutines.delay
import me.attsuman08.abysslib.shade.acf.BaseCommand
import me.attsuman08.abysslib.shade.acf.annotation.*
import net.somrpg.swordofmagic7.SomCore
import net.somrpg.swordofmagic7.extensions.asyncDispatcher
import net.somrpg.swordofmagic7.extensions.minecraftDispatcher
import net.somrpg.swordofmagic7.utils.TimeUtils
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import swordofmagic7.Data.DataBase
import swordofmagic7.Function
import swordofmagic7.Sound.SoundList
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

            SomCore.instance.launch(minecraftDispatcher) {
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

    @Subcommand("restart")
    @Syntax("<seconds>")
    fun restart(sender: CommandSender, @Conditions("limits:min=10,max=1800") seconds: Int) {
        SomCore.instance.launch(asyncDispatcher) {
            for (i in seconds downTo 1) {
                if (SomCore.restartNotifyTimes.contains(i)) {
                    for (p in Bukkit.getOnlinePlayers()) {
                        Function.sendMessage(p, "§b[${DataBase.ServerId}] §a現在接続しているチャンネルは${TimeUtils.formatSeconds(i)}後に再起動されます", SoundList.CLICK)
                    }
                }
                delay(1000)
            }
            for (p in Bukkit.getOnlinePlayers()) {
                Function.sendMessage(p, "§b[${DataBase.ServerId}] §aサーバーを再起動します", SoundList.CLICK)
            }
            delay(1000)
            SomCore.instance.launch(minecraftDispatcher) {
                Bukkit.getServer().shutdown()
            }
        }
    }

}