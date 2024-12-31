package swordofmagic7.Command.Developer

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import swordofmagic7.Command.SomCommand
import swordofmagic7.Command.SomTabComplete
import swordofmagic7.Data.PlayerData
import swordofmagic7.Inventory.ItemParameterStack
import swordofmagic7.Item.ItemParameter
import swordofmagic7.TextView.TextView

import swordofmagic7.Data.DataBase.getItemList
import swordofmagic7.Data.DataBase.getItemParameter
import swordofmagic7.Data.PlayerData.playerData

class GetItem : SomCommand, SomTabComplete {
    override fun PlayerCommand(player: Player, playerData: PlayerData, args: Array<String>): Boolean {
        return false
    }

    override fun Command(sender: CommandSender, args: Array<String>): Boolean {
        var targetData: PlayerData? = if (sender is Player) playerData(sender) else null
        if (args.size >= 3) {
            val target = Bukkit.getPlayer(args[0])
            if (target != null && target.isOnline) targetData = playerData(target)
        }
        if (targetData != null) {
            if (getItemList().containsKey(args[0])) {
                val stack = ItemParameterStack(getItemParameter(args[0]))
                var amount = 1
                if (args.size >= 2) amount = args[1].toIntOrNull() ?: 1
                if (args.size >= 3 && stack.itemParameter.Category.isEquipment) {
                    stack.itemParameter.itemEquipmentData.Plus = args[2].toIntOrNull() ?: 0
                }
                stack.Amount = amount
                targetData.ItemInventory.addItemParameter(stack)
                targetData.ItemInventory.viewInventory()
                val textView = stack.itemParameter.getTextView(stack.Amount, targetData.ViewFormat())
                textView.addText("§aを§e獲得§aしました")
                targetData.player.sendMessage(textView.toComponent())
                return true
            }
        } else {
            sender.sendMessage("§c無効なプレイヤーです")
        }
        for (str in getItemList().entries) {
            sender.sendMessage(str.key)
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
        if (args.size == 1) for (item in getItemList().values) {
            complete.add(item.Id)
        }
        return complete
    }
}
