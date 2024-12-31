package swordofmagic7.Command.Developer

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import swordofmagic7.Command.SomCommand
import swordofmagic7.Data.PlayerData

class BukkitTasks : SomCommand {
    override fun PlayerCommand(player: Player, playerData: PlayerData, args: Array<String>): Boolean {
        return false
    }

    override fun Command(sender: CommandSender, args: Array<String>): Boolean {
        sender.sendMessage("PendingTask: " + Bukkit.scheduler.pendingTasks.size)
        sender.sendMessage("ActiveTask: " + Bukkit.scheduler.activeWorkers.size)
        return true
    }
}
