package swordofmagic7.Dungeon.DimensionLibrary;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Sound.SoundList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static swordofmagic7.Dungeon.Dungeon.world;
import static swordofmagic7.Function.sendMessage;

public class DimensionLibraryB1 {
    private static final Location locationB2 = new Location(world, 2395.5,66, 105.5);
    private static final Location[] locations = new Location[25];
    private static final String[] text = new String[25];
    private static final HashMap<Player, DimensionLibraryB1Data> data = new HashMap<>();

    public static void onLoad() {
        int x = 0;
        for (int i = 0; i < 5; i++) {
            for (int i2 = 0; i2 < 5; i2++) {
                locations[x] = new Location(world, 2070-(35*i), 65, 270-(35*i2));
                x++;
            }
        }
        text[0] = "原点であり虚無です";
        text[1] = "始まりであり最小の情報です";
        text[2] = "リヌスの世代であり捕手を表すものです";
        text[3] = "リチウムとアースは仲がいいです";
        text[4] = "季節・方位・苦・権・元素これらは同じ数に分けられます";
        text[5] = "大陸・大洋は同じだけあります";
        text[6] = "土星と炭素の共通点は何でしょう";
        text[7] = "戦車であり地水師です";
        text[8] = "海王と力は密接な関係にあります";
        text[9] = "将棋の大きさと義務教育は関係があります";
        text[10] = "デナリウスは運命の輪が嫌いでした";
        text[11] = "始めての大戦においてこれはとても重要です";
        text[12] = "オリュンポス神と江南神は同じ数だけいます";
        text[13] = "グレゴリウスとレオは同じ所の教皇でした";
        text[14] = "火天大有と水のイオン積に関係性があるのは意外です";
        text[15] = "悪魔と青年は契約を結びました";
        text[16] = "蜜蜂の塔は美しいです";
        text[17] = "オハイオとアンドリューは喧嘩ばかりしています";
        text[18] = "アルゴンとアルカンは似ていますね";
        text[19] = "メトンとバハーイーは時に関与しています";
        text[20] = "エジプトはファビアヌスとの審判に破れました";
        text[21] = "世界の預言者はFTPによりもたらされました";
        text[22] = "武丁と簡王は同世代だったとされています";
        text[23] = "テルネットと信者たちは似て非なるものです";
        text[24] = "景王と祖甲はクロムが好みでした";
    }

    public static void use(Player player) {
        PlayerData playerData = PlayerData.playerData(player);
        if (playerData.Map.Id.equals("DimensionLibraryB1")) {
            if (!data.containsKey(player)) {
                DimensionLibraryB1Data data = new DimensionLibraryB1Data(player);
                DimensionLibraryB1.data.put(player, data);
                List<String> message = new ArrayList<>();
                message.add("§c次元の歪みが生じました...");
                message.add("§b「全ての結果を一つ増やすことが鍵かもしれません」");
                message.add("§a本を見てみると「" + text[data.root[data.progress]] + "」と書かれてました");
                sendMessage(player, message, SoundList.TICK);
            }
            Location loc = player.getLocation();
            int i = 0;
            for (Location location : locations) {
                if (Math.abs(location.getBlockX() - loc.getBlockX()) < 15 && Math.abs(location.getBlockZ() - loc.getBlockZ()) < 15) {
                    break;
                }
                i++;
            }
            DimensionLibraryB1Data data = DimensionLibraryB1.data.get(player);
            if (data.root[data.progress] == i) {
                data.progress++;
                if (data.root.length > data.progress) {
                    List<String> message = new ArrayList<>();
                    message.add("§c次元の歪みが大きくなっています... [" + data.progress + "/" + data.root.length + "]");
                    message.add("§a本を見てみると「" + text[data.root[data.progress]] + "」と書かれてました");
                    sendMessage(player, message, SoundList.TICK);
                } else {
                    sendMessage(player, "§c次元の歪みに吸い込まれます...", SoundList.TICK);
                    MultiThread.TaskRunSynchronized(() -> {
                        player.teleportAsync(locationB2);
                        DataBase.getMapData("DimensionLibraryB2").enter(player);
                    });
                    DimensionLibraryB1.data.remove(player);
                }
            }
        }
    }
}
