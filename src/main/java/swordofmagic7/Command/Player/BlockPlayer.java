package swordofmagic7.Command.Player;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import swordofmagic7.Command.SomCommand;
import swordofmagic7.Command.SomTabComplete;
import swordofmagic7.Command.TabComplete.PlayerTabComplete;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Sound.SoundList;

import java.util.ArrayList;
import java.util.List;

import static swordofmagic7.Data.DataBase.Som7Premium;
import static swordofmagic7.Data.DataBase.Som7VIP;
import static swordofmagic7.Function.decoText;
import static swordofmagic7.Function.sendMessage;

public class BlockPlayer implements SomCommand, SomTabComplete {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        Player target = null;
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                List<String> message = new ArrayList<>();
                message.add(decoText("§cBlockList"));
                for (String uuid : playerData.BlockList) {
                    message.add("§7・§e" + uuid);
                }
                if (message.size() == 1) message.add("§7・§7なし");
                sendMessage(player, message);
            } else target = Bukkit.getPlayer(args[0]);
        }
        if (target != null && target.isOnline() && target != player) {
            String uuid = target.getUniqueId().toString();
            PlayerData targetData = PlayerData.playerData(target);
            int limit = 10;
            if (player.hasPermission(Som7VIP)) limit = 25;
            if (player.hasPermission(Som7Premium)) limit = 50;
            if (!playerData.BlockList.contains(uuid)) {
                if (playerData.BlockList.size() < limit) {
                    playerData.BlockList.add(uuid);
                    sendMessage(player, "§c" + targetData.Nick + "§aを§4Block§aしました", SoundList.Tick);
                } else {
                    sendMessage(player, "§c" + limit + "人§aまでしか§4Block§a出来ません", SoundList.Nope);
                }
            } else {
                playerData.BlockList.remove(uuid);
                sendMessage(player, "§e" + targetData.Nick + "§aの§4Block§aを§b解除§aしました", SoundList.Tick);
            }
            playerData.updateBlockPlayer();
            targetData.updateBlockPlayer();
        } else {
            sendMessage(player, "§c無効なプレイヤーです", SoundList.Nope);
        }
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return false;
    }

    @Override
    public List<String> PlayerTabComplete(Player player, PlayerData playerData, Command command, String[] args) {
        return null;
    }

    @Override
    public List<String> TabComplete(CommandSender sender, Command command, String[] args) {
        return PlayerTabComplete.complete();
    }
}
