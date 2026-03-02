@file:Suppress("unused")

package net.somrpg.swordofmagic7.commands.user

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Syntax
import net.somrpg.swordofmagic7.extensions.getPlayerData
import org.bukkit.entity.Player

@CommandAlias("iteminventoryinfo|iii")
@CommandPermission("som7.user")
class ItemInventoryInfoCommand : BaseCommand() {
    @Default
    @Syntax("<item>")
    @CommandCompletion("@items")
    fun default(
        sender: Player,
        item: String,
    ) {
        val playerData = sender.getPlayerData()
        val itemSlot = playerData.ItemInventory.getItemSlot(item)
        val itemAmount = playerData.ItemInventory.getItemAmount(item)
        if (itemSlot == -1 || itemAmount == 0) {
            sender.sendMessage("§cアイテム[$item]が見つかりませんでした。")
            return
        }
        sender.sendMessage("§a[$item]")
        sender.sendMessage("§a・スロットID: §e$itemSlot")
        sender.sendMessage("§a・所持数: §e${itemAmount}個")
    }
}
