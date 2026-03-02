@file:Suppress("unused")

package net.somrpg.swordofmagic7.commands.developer

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Syntax
import net.somrpg.swordofmagic7.extensions.getPlayerData
import org.bukkit.entity.Player
import swordofmagic7.Data.DataBase
import swordofmagic7.Inventory.ItemParameterStack

@CommandAlias("getItem")
@CommandPermission("som7.developer")
class GetItemCommand : BaseCommand() {
    @Default
    @Syntax("<id> [amount] [plus]")
    @CommandCompletion("@allItems * *")
    fun default(
        player: Player,
        id: String,
        @Default("1") amount: Int,
        @Default("0") plus: Int,
    ) {
        val playerData = player.getPlayerData()
        if (!DataBase.getItemList().containsKey(id)) {
            DataBase.getItemList().keys.forEach { player.sendMessage(it) }
            return
        }
        val stack = ItemParameterStack(DataBase.getItemParameter(id))
        stack.Amount = amount
        if (plus > 0 && stack.itemParameter.Category.isEquipment()) {
            stack.itemParameter.itemEquipmentData.Plus = plus
        }
        playerData.ItemInventory.addItemParameter(stack)
        playerData.ItemInventory.viewInventory()
        val textView = stack.itemParameter.getTextView(stack.Amount, playerData.ViewFormat())
        textView.addText("§aを§e獲得§aしました")
        player.sendMessage(textView.toComponent())
    }
}
