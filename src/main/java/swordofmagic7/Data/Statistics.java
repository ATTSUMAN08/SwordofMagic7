package swordofmagic7.Data;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import swordofmagic7.Classes.ClassData;
import swordofmagic7.Dungeon.AusMine.AusMineB2;
import swordofmagic7.Dungeon.AusMine.AusMineB4;
import swordofmagic7.Life.LifeType;
import swordofmagic7.Title.TitleManager;

import java.util.ArrayList;
import java.util.List;

import static swordofmagic7.Function.decoLore;

public class Statistics {

    private final Player player;
    private final PlayerData playerData;
    private final TitleManager titleManager;

    public Statistics(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
        titleManager = playerData.titleManager;
    }

    public int MaxFishingCombo = 0;
    public double MaxFishingCPS = 0;
    public int TotalEnemyKills = 0;

    public List<String> getStringList() {
        List<String> list = new ArrayList<>();
        list.add(decoLore("釣獲最大コンボ") + MaxFishingCombo);
        list.add(decoLore("釣獲最高CPS") + String.format("%.2f", MaxFishingCPS));
        list.add(decoLore("エネミ討伐数") + TotalEnemyKills);
        return list;
    }

    public void checkTitle() {
        if (playerData.Level >= 30) titleManager.addTitle("プレイヤーレベル30");
        if (playerData.Level >= 40) titleManager.addTitle("プレイヤーレベル40");
        if (playerData.Level >= 50) titleManager.addTitle("プレイヤーレベル50");

        if (MaxFishingCPS >= 10) titleManager.addTitle("釣獲CPS10");
        if (MaxFishingCombo >= 100) titleManager.addTitle("釣獲コンボ100");
        if (MaxFishingCombo >= 200) titleManager.addTitle("釣獲コンボ200");
        if (MaxFishingCombo >= 300) titleManager.addTitle("釣獲コンボ300");

        if (TotalEnemyKills >= 500) titleManager.addTitle("エネミー討伐500");
        if (TotalEnemyKills >= 2500) titleManager.addTitle("エネミー討伐2500");
        if (TotalEnemyKills >= 5000) titleManager.addTitle("エネミー討伐5000");
        if (TotalEnemyKills >= 10000) titleManager.addTitle("エネミー討伐10000");
        if (TotalEnemyKills >= 25000) titleManager.addTitle("エネミー討伐25000");
        if (TotalEnemyKills >= 100000) titleManager.addTitle("エネミー討伐100000");
        if (TotalEnemyKills >= 250000) titleManager.addTitle("エネミー討伐250000");
        if (TotalEnemyKills >= 1000000) titleManager.addTitle("エネミー討伐1000000");

        for (ClassData classData : DataBase.ClassList.values()) {
            if (playerData.Classes.getClassLevel(classData) >= 15) {
                titleManager.addTitle(classData.Display + "レベル15");
            }
        }

        for (LifeType lifeType : LifeType.values()) {
            if (playerData.LifeStatus.getLevel(lifeType) >= 15) {
                titleManager.addTitle(lifeType.Display + "レベル15");
            }
            if (playerData.LifeStatus.getLevel(lifeType) >= 30) {
                titleManager.addTitle(lifeType.Display + "レベル30");
            }
        }
    }

    public void enemyKill(String mobId) {
        playerData.statistics.TotalEnemyKills++;
        if (mobId.equals("サイモア")) {
            titleManager.addTitle("サイモア討伐");
            if ((AusMineB2.StartTime-AusMineB2.Time) < 60) titleManager.addTitle("サイモア討伐2");
        }
        if (mobId.equals("グリフィア")) {
            titleManager.addTitle("グリフィア討伐");
            if ((AusMineB4.StartTime-AusMineB4.Time) < 100) titleManager.addTitle("グリフィア討伐2");
        }
    }

    public void save(FileConfiguration data) {
        data.set("Statistics.MaxFishingCombo", MaxFishingCombo);
        data.set("Statistics.MaxFishingCPS", MaxFishingCPS);
        data.set("Statistics.TotalEnemyKills", TotalEnemyKills);
    }

    public void load(FileConfiguration data) {
        MaxFishingCombo = data.getInt("Statistics.MaxFishingCombo", 0);
        MaxFishingCPS = data.getDouble("Statistics.MaxFishingCPS", 0d);
        TotalEnemyKills = data.getInt("Statistics.TotalEnemyKills", 0);
    }
}
