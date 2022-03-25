package swordofmagic7.Npc;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Player;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Sound.SoundList;

import java.util.HashMap;

import static swordofmagic7.Data.DataBase.getNpcData;
import static swordofmagic7.Sound.CustomSound.playSound;

public class NpcMessage {
    private static final HashMap<Player, Integer> reading = new HashMap<>();
    public static void ShowMessage(Player player, NPC npc) {
        MultiThread.TaskRun(() -> {
            if (!reading.containsKey(player)) {
                reading.put(player, npc.getId());
                for (String str : getNpcData(npc.getId()).Message) {
                    player.sendMessage(npc.getFullName() + "§7: §a" + str);
                    playSound(player, SoundList.Tick);
                    MultiThread.sleepTick(20 + str.length() * 3L);
                }
                reading.remove(player);
            } else {
                player.sendMessage("§aNPCの話を聞いています");
                playSound(player, SoundList.Nope);
            }
        }, "ShowMessage: " + player.getName());
    }
}
