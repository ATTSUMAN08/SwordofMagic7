@file:Suppress("unused")

package net.somrpg.swordofmagic7.commands.user

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Syntax
import org.bukkit.entity.Player
import swordofmagic7.Data.DataBase
import swordofmagic7.Function

@CommandAlias("itemInfo|ii")
@CommandPermission("som7.user")
class ItemInfoCommand : BaseCommand() {
    @Default
    @Syntax("<id>")
    @CommandCompletion("@visibleItems @nothing")
    fun default(
        player: Player,
        id: String,
    ) {
        if (id.equals("Amount", ignoreCase = true)) {
            Function.sendMessage(player, "§aItemListSize: ${DataBase.ItemList.size}")
            return
        }
        if (!DataBase.ItemList.containsKey(id)) {
            player.sendMessage("§a存在しない§eアイテム§aです")
            return
        }
        val item = DataBase.getItemParameter(id)
        val list = mutableListOf<String>()
        list.add(Function.decoText(item.Display))
        list.addAll(DataBase.ItemInfoData[id] ?: emptyList())
        Function.sendMessage(player, list)
    }
}
