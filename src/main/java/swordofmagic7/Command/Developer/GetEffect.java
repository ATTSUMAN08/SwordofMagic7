package swordofmagic7.Command.Developer;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import swordofmagic7.Command.SomCommand;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.EffectType;

public class GetEffect implements SomCommand {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        if (args.length >= 1) {
            try {
                int time = 200;
                if (args.length >= 2) {
                    time = Integer.parseInt(args[1]);
                }
                playerData.EffectManager.addEffect(EffectType.valueOf(args[0]), time);
            } catch (Exception e) {
                player.sendMessage("§c" + "/getEffect <effect> [<time=200>]");
            }
        }
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return false;
    }
}
