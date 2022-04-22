package swordofmagic7.Command.Player;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import swordofmagic7.Command.SomCommand;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Life.LifeStatus;

public class ReqLifeExp implements SomCommand {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        return false;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        if (args.length == 1) {
            try {
                int level = Integer.parseInt(args[0]);
                int reqExp = LifeStatus.LifeReqExp(level);
                sender.sendMessage("§eLv" + level + "§7: §a" + reqExp);
            } catch (Exception ignored) {
                sender.sendMessage("§e/reqLifeExp <Level>");
            }
        } else {
            sender.sendMessage("§e/reqLifeExp <Level>");
        }
        return true;
    }
}
