@file:Suppress("unused")
package net.somrpg.swordofmagic7.commands.user

import me.attsuman08.abysslib.shade.acf.BaseCommand
import me.attsuman08.abysslib.shade.acf.annotation.CommandAlias
import me.attsuman08.abysslib.shade.acf.annotation.CommandCompletion
import me.attsuman08.abysslib.shade.acf.annotation.CommandPermission
import me.attsuman08.abysslib.shade.acf.annotation.Default
import me.attsuman08.abysslib.shade.acf.annotation.HelpCommand
import me.attsuman08.abysslib.shade.acf.annotation.Syntax
import net.somrpg.swordofmagic7.extensions.getPlayerData
import org.bukkit.entity.Player
import swordofmagic7.Function.sendMessage
import swordofmagic7.Sound.SoundList

@CommandAlias("channel|ch")
@CommandPermission("som7.user")
class ChannelCommand : BaseCommand() {

    @Default
    @Syntax("<channel>")
    @CommandCompletion("@channels")
    fun default(player: Player, channel: String) {
        val playerData = player.getPlayerData()
        if (playerData.isPlayDungeonQuest) {
            sendMessage(player, "§cダンジョンクエスト§a中は§eチャンネル§aを変更できません", SoundList.NOPE)
            return
        }
        val teleportServer = when (channel.lowercase()) {
            "1" -> "CH1"
            "2" -> "CH2"
            "3" -> "CH3"
            "4" -> "CH4"
            "5" -> "CH5"
            "ev", "event" -> "Event"
            "dev" -> "Dev"
            else -> {
                player.sendMessage("存在しないチャンネルです")
                return
            }
        }
        playerData.saveTeleportServer = "SOM7$teleportServer"
        playerData.save()
    }

    @HelpCommand
    fun onHelp(player: Player) {
        player.sendMessage("/ch <channel>")
    }
}