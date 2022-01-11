package swordofmagic7.Npc;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import swordofmagic7.Sound.SoundList;

import java.util.HashMap;

import static swordofmagic7.Data.DataBase.getNpcData;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.System.plugin;

public class NpcMessage {
    private static final HashMap<Player, Integer> reading = new HashMap<>();
    public static void ShowMessage(Player player, NPC npc) {
        if (!reading.containsKey(player)) {
            reading.put(player, npc.getId());
            int wait = 0;
            for (String str : getNpcData(npc.getId()).Message) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    player.sendMessage(npc.getFullName() + "§7: §a" + str);
                    playSound(player, SoundList.Tick);
                }, wait);
                wait += 20 + str.length()*3;
            }
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> reading.remove(player), wait);
        } else {
            player.sendMessage("§aNPCの話を聞いています");
            playSound(player, SoundList.Nope);
        }
    }
}
