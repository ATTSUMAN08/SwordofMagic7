package swordofmagic7.Command.Player;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import swordofmagic7.Command.SomCommand;
import swordofmagic7.Command.SomTabComplete;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Mob.EnemyData;
import swordofmagic7.Mob.MobData;
import swordofmagic7.Sound.SoundList;

import java.util.ArrayList;
import java.util.List;

import static swordofmagic7.Data.DataBase.MobList;
import static swordofmagic7.Data.DataBase.getMobData;
import static swordofmagic7.Function.decoText;
import static swordofmagic7.Function.sendMessage;

public class MobInfo implements SomCommand, SomTabComplete {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        if (args.length >= 1) {
            if (MobList.containsKey(args[0])) {
                MobData mobData = getMobData(args[0]);
                List<String> message = new ArrayList<>();
                message.add(decoText(mobData.Display));
                message.addAll(playerData.Menu.mobInfo.toStringList(mobData));
                if (args.length == 2) {
                    message.addAll(EnemyData.enemyLore(mobData, Integer.parseInt(args[1])));
                }
                sendMessage(player, message, SoundList.NOPE);
            } else {
                sendMessage(player, "§a存在しない§cエネミー§aです", SoundList.NOPE);
            }
        } else {
            playerData.Menu.mobInfo.MobInfoView();
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
        List<String> complete = new ArrayList<>();
        if (args.length == 1) for (MobData mobData : MobList.values()) {
            if (!mobData.isHide) complete.add(mobData.Id);
        }
        return complete;
    }
}
