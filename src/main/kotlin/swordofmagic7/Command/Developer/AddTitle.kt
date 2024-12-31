package swordofmagic7.Command.Developer

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import swordofmagic7.Command.SomCommand
import swordofmagic7.Command.SomTabComplete
import swordofmagic7.Command.TabComplete.PlayerTabComplete
import swordofmagic7.Data.DataBase
import swordofmagic7.Data.PlayerData
import swordofmagic7.Data.TitleData

class AddTitle : SomCommand, SomTabComplete {
    override fun PlayerCommand(player: Player, playerData: PlayerData, args: Array<String>): Boolean {
        return false
    }

    override fun Command(sender: CommandSender, args: Array<String>): Boolean {
        var target: Player? = null
        if (sender is Player) target = sender
        if (args.size == 2) {
            target = Bukkit.getPlayer(args[1])
        }
        if (target != null && target.isOnline) {
            val targetData = PlayerData.playerData(target)
            targetData.titleManager.addTitle(args[0])
        } else {
            sender.sendMessage("§c無効なプレイヤーです")
        }
        return true
    }

    override fun PlayerTabComplete(
        player: Player,
        playerData: PlayerData,
        command: Command,
        args: Array<String>
    ): List<String>? {
        return null
    }

    override fun TabComplete(sender: CommandSender, command: Command, args: Array<String>): List<String> {
        val complete = mutableListOf<String>()
        if (args.size == 1) {
            for (titleData in DataBase.TitleDataList.values) {
                complete.add(titleData.Id)
            }
        } else if (args.size == 2) {
            return PlayerTabComplete.complete()
        }
        return complete
    }
}
