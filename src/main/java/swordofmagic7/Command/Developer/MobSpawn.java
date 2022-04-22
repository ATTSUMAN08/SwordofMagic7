package swordofmagic7.Command.Developer;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import swordofmagic7.Command.SomCommand;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Mob.MobData;
import swordofmagic7.Mob.MobManager;

import java.util.Map;

import static swordofmagic7.Data.DataBase.getMobData;
import static swordofmagic7.Data.DataBase.getMobList;

public class MobSpawn implements SomCommand {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        if (args.length >= 1) {
            if (getMobList().containsKey(args[0])) {
                int level = 1;
                int perSpawn = 1;
                if (args.length == 2) level = Integer.parseInt(args[1]);
                if (args.length == 3) perSpawn = Integer.parseInt(args[2]);
                for (int i = 0; i < perSpawn; i++){
                    MobManager.mobSpawn(getMobData(args[0]), level, player.getLocation());
                }
                return true;
            }
        }
        for (Map.Entry<String, MobData> str : getMobList().entrySet()) {
            player.sendMessage(str.getKey());
        }
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return false;
    }
}
