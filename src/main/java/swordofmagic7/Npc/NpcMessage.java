package swordofmagic7.Npc;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.somrpg.swordofmagic7.npc.NPCData;
import org.bukkit.entity.Player;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Sound.SoundList;

import java.util.HashMap;

import static swordofmagic7.Data.DataBase.getNpcData;
import static swordofmagic7.Sound.CustomSound.playSound;

public class NpcMessage {
    private static final HashMap<Player, Integer> reading = new HashMap<>();
    public static void ShowMessage(Player player, NPCData.NPC npc) {
        MultiThread.TaskRun(() -> {
            if (!reading.containsKey(player)) {
                reading.put(player, npc.getMessageId());
                for (String str : getNpcData(npc.getMessageId()).Message) {
                    player.sendMessage(MiniMessage.miniMessage().deserialize(npc.getName() + "<gray>: <green>" + str));
                    playSound(player, SoundList.TICK);
                    MultiThread.sleepTick(20 + str.length() * 3L);
                }
                reading.remove(player);
            } else {
                player.sendMessage("§aNPCの話を聞いています");
                playSound(player, SoundList.NOPE);
            }
        }, "ShowMessage");
    }
}
