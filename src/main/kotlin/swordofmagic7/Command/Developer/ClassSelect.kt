package swordofmagic7.Command.Developer

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import swordofmagic7.Command.SomCommand
import swordofmagic7.Data.PlayerData

import swordofmagic7.Data.DataBase.getClassData

class ClassSelect : SomCommand {
    override fun PlayerCommand(player: Player, playerData: PlayerData, args: Array<String>): Boolean {
        try {
            playerData.Classes.classSlot[args[0].toIntOrNull() ?: throw IllegalArgumentException()] = getClassData(args[1])
        } catch (e: Exception) {
            player.sendMessage("/classSelect <slot> <class>")
        }
        return true
    }

    override fun Command(sender: CommandSender, args: Array<String>): Boolean {
        return false
    }
}
