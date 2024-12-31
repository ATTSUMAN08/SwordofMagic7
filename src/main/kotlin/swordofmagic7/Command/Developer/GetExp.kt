package swordofmagic7.Command.Developer

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import swordofmagic7.Command.SomCommand
import swordofmagic7.Data.PlayerData

class GetExp : SomCommand {
    override fun PlayerCommand(player: Player, playerData: PlayerData, args: Array<String>): Boolean {
        if (args.isNotEmpty()) {
            try {
                playerData.addPlayerExp(args[0].toIntOrNull() ?: throw IllegalArgumentException("Invalid exp value"))
            } catch (e: Exception) {
                player.sendMessage("§c/getExp <exp>")
            }
        }
        return true
    }

    override fun Command(sender: CommandSender, args: Array<String>): Boolean {
        return false
    }
}
