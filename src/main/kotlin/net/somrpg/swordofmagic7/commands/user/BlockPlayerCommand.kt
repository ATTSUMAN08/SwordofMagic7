@file:Suppress("unused")

package net.somrpg.swordofmagic7.commands.user

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CatchUnknown
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import net.somrpg.swordofmagic7.extensions.getPlayerData
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import swordofmagic7.Data.DataBase
import swordofmagic7.Data.PlayerData
import swordofmagic7.Function
import swordofmagic7.Sound.SoundList

@CommandAlias("blockPlayer")
@CommandPermission("som7.user")
class BlockPlayerCommand : BaseCommand() {
    @Subcommand("list")
    fun list(player: Player) {
        val playerData = player.getPlayerData()
        val message = mutableListOf<String>()
        message.add(Function.decoText("§cBlockList"))
        for (uuid in playerData.BlockList) {
            message.add("§7・§e$uuid")
        }
        if (message.size == 1) message.add("§7・§7なし")
        Function.sendMessage(player, message)
    }

    @Default
    @CatchUnknown
    @Syntax("[player]")
    @CommandCompletion("@players")
    fun default(
        player: Player,
        @Optional targetName: String?,
    ) {
        if (targetName == null) {
            Function.sendMessage(player, "§c無効なプレイヤーです", SoundList.NOPE)
            return
        }
        val target =
            Bukkit.getPlayer(targetName) ?: run {
                Function.sendMessage(player, "§c無効なプレイヤーです", SoundList.NOPE)
                return
            }
        if (target == player) {
            Function.sendMessage(player, "§c無効なプレイヤーです", SoundList.NOPE)
            return
        }
        val playerData = player.getPlayerData()
        val uuid = target.uniqueId.toString()
        val targetData = PlayerData.playerData(target)
        val limit =
            when {
                player.hasPermission(DataBase.Som7Premium) -> 50
                player.hasPermission(DataBase.Som7VIP) -> 25
                else -> 10
            }
        if (!playerData.BlockList.contains(uuid)) {
            if (playerData.BlockList.size < limit) {
                playerData.BlockList.add(uuid)
                Function.sendMessage(player, "§c${targetData.Nick}§aを§4Block§aしました", SoundList.TICK)
            } else {
                Function.sendMessage(player, "§c${limit}人§aまでしか§4Block§a出来ません", SoundList.NOPE)
            }
        } else {
            playerData.BlockList.remove(uuid)
            Function.sendMessage(player, "§e${targetData.Nick}§aの§4Block§aを§b解除§aしました", SoundList.TICK)
        }
        playerData.updateBlockPlayer()
        targetData.updateBlockPlayer()
    }
}
