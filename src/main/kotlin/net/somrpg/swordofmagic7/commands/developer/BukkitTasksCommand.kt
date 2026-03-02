@file:Suppress("unused")

package net.somrpg.swordofmagic7.commands.developer

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

@CommandAlias("bukkitTasks")
@CommandPermission("som7.developer")
class BukkitTasksCommand : BaseCommand() {
    @Default
    fun default(sender: CommandSender) {
        sender.sendMessage("PendingTask: ${Bukkit.getScheduler().pendingTasks.size}")
        sender.sendMessage("ActiveTask: ${Bukkit.getScheduler().activeWorkers.size}")
    }
}
