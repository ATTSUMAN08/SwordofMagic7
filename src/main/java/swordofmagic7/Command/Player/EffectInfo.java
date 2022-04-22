package swordofmagic7.Command.Player;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import swordofmagic7.Command.SomCommand;
import swordofmagic7.Command.SomTabComplete;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.EffectType;

import java.util.ArrayList;
import java.util.List;

import static swordofmagic7.Function.*;

public class EffectInfo implements SomCommand, SomTabComplete {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        if (args.length == 1) {
            List<String> message = new ArrayList<>();
            for (EffectType effectType : EffectType.values()) {
                if (effectType.Display.equals(args[0])) {
                    message.add(decoText(effectType.Display));
                    message.add(decoLore("効果タイプ") + (effectType.Buff ? "§b§lバフ" : "§c§lデバフ"));
                    message.add(decoLore("最大スタック") + effectType.MaxStack);
                    message.add(decoLore("解除等級") + effectType.effectRank.Display);
                    message.add(decoLore("干渉系影響") + (effectType.isStatic ? "§b§lする" : "§c§lしない"));
                    message.add(decoLore("ステータス更新") + (effectType.isUpdateStatus ? "§b§lする" : "§c§lしない"));
                    message.add(decoText("効果説明"));
                    for (String str : effectType.Lore) {
                        message.add("§a" + str);
                    }
                    sendMessage(player, message);
                    return true;
                }
            }
        }
        sendMessage(player, "§e/effectInfo <効果名>");
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
        for (EffectType effectType : EffectType.values()) {
            complete.add(effectType.Display);
        }
        return complete;
    }
}
