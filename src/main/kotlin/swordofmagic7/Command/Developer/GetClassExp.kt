package swordofmagic7.Command.Developer

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import swordofmagic7.Command.SomCommand
import swordofmagic7.Data.PlayerData

import swordofmagic7.Data.DataBase.getClassData
import swordofmagic7.Data.DataBase.getClassList

class GetClassExp : SomCommand {
    override fun PlayerCommand(player: Player, playerData: PlayerData, args: Array<String>): Boolean {
        if (args.size == 2 && getClassList().containsKey(args[1])) {
            try {
                playerData.Classes.addClassExp(getClassData(args[1]), args[0].toIntOrNull() ?: throw IllegalArgumentException("Invalid exp value"))
            } catch (e: Exception) {
                player.sendMessage("§c/getClassExp <exp> <class>")
            }
        } else {
            player.sendMessage("§c/getClassExp <exp> <class>")
        }
        return true
    }

    override fun Command(sender: CommandSender, args: Array<String>): Boolean {
        return false
    }
}
