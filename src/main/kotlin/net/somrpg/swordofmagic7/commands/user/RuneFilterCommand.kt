@file:Suppress("unused")

package net.somrpg.swordofmagic7.commands.user

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CatchUnknown
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import net.somrpg.swordofmagic7.extensions.getPlayerData
import org.bukkit.entity.Player
import swordofmagic7.Data.DataBase
import swordofmagic7.Function

@CommandAlias("runeFilter|rf")
@CommandPermission("som7.user")
class RuneFilterCommand : BaseCommand() {
    @Subcommand("quality|Q")
    @Syntax("<0~100>")
    fun quality(
        player: Player,
        value: Double,
    ) {
        if (value < 0 || value > 100) {
            Function.sendMessage(player, "§e/runeFilter Quality <0~100>")
            return
        }
        player.getPlayerData().RuneQualityFilter = value / 100
        Function.sendMessage(player, "§eルーンフィルター[品質] §b-> §a$value%")
    }

    @Subcommand("id|Id")
    @Syntax("<runeId>")
    @CommandCompletion("@visibleRunes")
    fun id(
        player: Player,
        runeId: String,
    ) {
        if (!DataBase.getRuneList().containsKey(runeId)) {
            Function.sendMessage(player, "§a存在しない§eルーン§aです")
            return
        }
        val playerData = player.getPlayerData()
        if (playerData.RuneIdFilter.contains(runeId)) {
            playerData.RuneIdFilter.remove(runeId)
        } else {
            playerData.RuneIdFilter.add(runeId)
        }
        Function.sendMessage(
            player,
            "§eルーンフィルター[ID:$runeId] §b-> §a${if (playerData.RuneIdFilter.contains(runeId)) "§b有効" else "§c無効"}",
        )
    }

    @Default
    @CatchUnknown
    fun default(player: Player) {
        Function.sendMessage(player, "§e/runeFilter Quality <0~100>")
        Function.sendMessage(player, "§e/runeFilter Id <RuneId>")
    }
}
