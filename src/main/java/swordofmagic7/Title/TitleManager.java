package swordofmagic7.Title;

import org.bukkit.entity.Player;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Data.TitleData;
import swordofmagic7.Sound.SoundList;

import java.util.HashSet;
import java.util.Set;

import static swordofmagic7.Data.DataBase.TitleDataList;
import static swordofmagic7.Sound.CustomSound.playSound;

public class TitleManager {

    public static final TitleData DefaultTitle = TitleDataList.get("称号無し");
    private final Player player;
    private final PlayerData playerData;

    public TitleManager(PlayerData playerData) {
        this.playerData = playerData;
        player = playerData.player;
    }

    public Set<String> TitleList = new HashSet<>();
    public TitleData Title = DefaultTitle;

    public void setTitle(TitleData title) {
        playerData.HoloAnim = 0;
        playerData.HoloWait = 0;
        if (TitleList.contains(title.Id) || player.hasPermission("som7.title.editor")) {
            Title = title;
            player.sendMessage("§a称号を§e[" + title.Id + "§e]§aに変更しました");
            playSound(player, SoundList.LEVEL_UP);
        } else {
            player.sendMessage("§a所持していない称号です");
            playSound(player, SoundList.NOPE);
        }
    }

    public void addTitle(String title) {
        if (TitleDataList.containsKey(title)) {
            addTitle(TitleDataList.get(title));
        }
    }
    public void addTitle(TitleData title) {
        if (title != null && !TitleList.contains(title.Id)) {
            if (title.attributePoint > 0) playerData.Attribute.addPoint(title.attributePoint);
            TitleList.add(title.Id);
            player.sendMessage("§e称号[" + title.Id + "§e]§aを獲得しました");
            playSound(player, SoundList.TICK);
        }
    }

}
