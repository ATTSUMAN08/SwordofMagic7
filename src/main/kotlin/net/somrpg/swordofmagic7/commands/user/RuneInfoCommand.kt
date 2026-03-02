@file:Suppress("unused")

package net.somrpg.swordofmagic7.commands.user

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Syntax
import net.somrpg.swordofmagic7.SomCore
import net.somrpg.swordofmagic7.extensions.getPlayerData
import org.bukkit.entity.Player
import swordofmagic7.Data.DataBase
import swordofmagic7.Function

@CommandAlias("runeInfo|ri")
@CommandPermission("som7.user")
class RuneInfoCommand : BaseCommand() {
    @Default
    @Syntax("[id] [level] [quality]")
    @CommandCompletion("@visibleRunes * *")
    fun default(
        player: Player,
        @Optional id: String?,
        @Optional levelStr: String?,
        @Optional qualityStr: String?,
    ) {
        val playerData = player.getPlayerData()
        if (id == null) {
            playerData.Menu.runeInfo.RuneInfoView()
            return
        }
        if (id.equals("Amount", ignoreCase = true)) {
            Function.sendMessage(player, "§aRuneListSize: ${DataBase.RuneList.size}")
            return
        }
        if (!DataBase.RuneList.containsKey(id)) {
            player.sendMessage("§a存在しない§eルーン§aです")
            return
        }
        val rune = DataBase.getRuneParameter(id)
        levelStr?.toIntOrNull()?.let { rune.Level = it.coerceIn(1, SomCore.PLAYER_MAX_LEVEL) }
        qualityStr?.toDoubleOrNull()?.let { rune.Quality = (it / 100.0).coerceIn(0.0, 200.0) }
        val itemStack = rune.viewRune(playerData.ViewFormat(), rune.isLoreHide)
        val list = mutableListOf<String>()
        list.add(Function.decoText(rune.Display))
        list.addAll(itemStack.lore ?: emptyList())
        Function.sendMessage(player, list)
    }
}
