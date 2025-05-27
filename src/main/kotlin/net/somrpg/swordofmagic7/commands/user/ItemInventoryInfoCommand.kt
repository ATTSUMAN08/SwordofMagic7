@file:Suppress("unused")
package net.somrpg.swordofmagic7.commands.user

import me.attsuman08.abysslib.shade.acf.BaseCommand
import me.attsuman08.abysslib.shade.acf.annotation.CommandAlias
import me.attsuman08.abysslib.shade.acf.annotation.CommandCompletion
import me.attsuman08.abysslib.shade.acf.annotation.CommandPermission
import me.attsuman08.abysslib.shade.acf.annotation.Default
import me.attsuman08.abysslib.shade.acf.annotation.Syntax
import net.somrpg.swordofmagic7.extensions.getPlayerData
import org.bukkit.entity.Player

@CommandAlias("iteminventoryinfo|iii")
@CommandPermission("som7.user")
class ItemInventoryInfoCommand : BaseCommand() {

    @Default
    @Syntax("<item>")
    @CommandCompletion("@items")
    fun default(sender: Player, item: String) {
        val playerData = sender.getPlayerData()
        val itemSlot = playerData.ItemInventory.getItemSlot(item)
        val itemAmount = playerData.ItemInventory.getItemAmount(item)
        if (itemSlot == -1 || itemAmount == 0) {
            sender.sendMessage("§cアイテム[${item}]が見つかりませんでした。")
            return
        }
        sender.sendMessage("§a[${item}]")
        sender.sendMessage("§a・スロットID: §e${itemSlot}")
        sender.sendMessage("§a・所持数: §e${itemAmount}個")
    }

}