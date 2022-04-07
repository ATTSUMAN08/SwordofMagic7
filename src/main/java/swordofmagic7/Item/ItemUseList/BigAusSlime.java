package swordofmagic7.Item.ItemUseList;

import org.bukkit.Location;
import swordofmagic7.Client;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Mob.EnemyData;
import swordofmagic7.Mob.MobManager;
import swordofmagic7.Sound.SoundList;
import swordofmagic7.TextView.TextView;

import static swordofmagic7.Data.DataBase.ServerId;
import static swordofmagic7.Data.DataBase.getMobData;
import static swordofmagic7.Dungeon.Dungeon.world;
import static swordofmagic7.Function.sendMessage;

public class BigAusSlime {

    private static final Location location = new Location(world, 1431, 100, 585);
    private static EnemyData enemyData;

    public static void trigger(PlayerData playerData, ItemParameter item) {
        if (playerData.player.getLocation().distance(location) < 32) {
            if (enemyData == null || enemyData.isDead()) {
                TextView textView = new TextView("§b[" + ServerId + "] " + playerData.getNick() + "§aさんが");
                textView.addView(item.getTextView(1, playerData.ViewFormat())).addText("§aを使用しました");
                textView.setSound(SoundList.DungeonTrigger);
                Client.BroadCast(textView);
                enemyData = MobManager.mobSpawn(getMobData("巨大なオーススライム"), 50, location);
            } else {
                sendMessage(playerData.player, "§aすでに開始されています", SoundList.Nope);
            }
        } else {
            sendMessage(playerData.player, "§e[オース森林]§aの§eオーススライム§aがいる付近で使用できます", SoundList.Nope);
        }
    }
}
