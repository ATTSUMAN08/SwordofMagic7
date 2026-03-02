@file:Suppress("unused")

package net.somrpg.swordofmagic7.commands.developer

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Syntax
import net.somrpg.swordofmagic7.extensions.getPlayerData
import org.bukkit.entity.Player
import swordofmagic7.Data.DataBase

@CommandAlias("getRune")
@CommandPermission("som7.developer")
class GetRuneCommand : BaseCommand() {
    @Default
    @Syntax("<id> [level] [quality]")
    @CommandCompletion("@allRunes * *")
    fun default(
        player: Player,
        id: String,
        @Default("1") level: Int,
        @Optional quality: Double?,
    ) {
        val playerData = player.getPlayerData()
        if (!DataBase.getRuneList().containsKey(id)) {
            DataBase.getRuneList().keys.forEach { player.sendMessage(it) }
            return
        }
        val rune = DataBase.getRuneParameter(id)
        rune.Level = level
        if (quality != null) rune.Quality = quality
        playerData.RuneInventory.addRuneParameter(rune)
        playerData.RuneInventory.viewRune()
    }
}
