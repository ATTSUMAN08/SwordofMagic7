package swordofmagic7.Command.Developer

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import swordofmagic7.Command.SomCommand
import swordofmagic7.Data.PlayerData
import swordofmagic7.Effect.EffectType

class GetEffect : SomCommand {
    override fun PlayerCommand(player: Player, playerData: PlayerData, args: Array<String>): Boolean {
        if (args.isNotEmpty()) {
            try {
                var time = 200
                if (args.size >= 2) {
                    time = args[1].toIntOrNull() ?: throw IllegalArgumentException("Invalid time value")
                }
                playerData.EffectManager.addEffect(EffectType.valueOf(args[0]), time)
            } catch (e: Exception) {
                player.sendMessage("§c/getEffect <effect> [<time=200>]")
            }
        }
        return true
    }

    override fun Command(sender: CommandSender, args: Array<String>): Boolean {
        return false
    }
}
