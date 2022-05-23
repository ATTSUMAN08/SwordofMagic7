package swordofmagic7.Data;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import swordofmagic7.Classes.ClassData;
import swordofmagic7.Dungeon.Ashark.AsharkB2;
import swordofmagic7.Dungeon.Ashark.AsharkB4;
import swordofmagic7.Dungeon.AusMine.AusMineB2;
import swordofmagic7.Dungeon.AusMine.AusMineB4;
import swordofmagic7.Dungeon.Novaha.Novaha2;
import swordofmagic7.Dungeon.Novaha.Novaha4;
import swordofmagic7.Dungeon.Tarnet.TarnetB1;
import swordofmagic7.Dungeon.Tarnet.TarnetB3;
import swordofmagic7.Life.LifeType;
import swordofmagic7.Mob.MobData;
import swordofmagic7.MultiThread.MultiThread;
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
        MultiThread.TaskRunTimer(() -> {
            playTime++;
        }, 20);
    }

    public int playTime = 0;
    public int AFKTime = 0;
    public int MaxFishingCombo = 0;
    public double MaxFishingCPS = 0;
    public int TotalEnemyKills = 0;
    public int TotalBossEnemyKills = 0;
    public int DownCount = 0;
    public int DeathCount = 0;
    public int RevivalCount = 0;
    public int MineCount = 0;
    public int FishingCount = 0;
    public int HarvestCount = 0;
    public int LumberCount = 0;
    public int CookCount = 0;
    public int UpgradeUseCostCount = 0;
    public int MakeEquipmentCount = 0;
    public int SmeltCount = 0;
    public int MakePotionCount = 0;
    public int StrafeCount = 0;
    public int WallJumpCount = 0;

    public static final int[] FishingComboStatisticsList = new int[]{100,200,300,400,500,700,1000};
    public static final int[] UpgradeStoneStrafeStatisticsList = new int[]{1000,5000,10000,30000,50000,100000,200000,300000};

    public List<String> getStringList() {
        List<String> list = new ArrayList<>();
        list.add(decoLore("プレイ時間") + String.format("%.2f", playTime/3600f) + "時間");
        list.add(decoLore("放置時間") + String.format("%.2f", AFKTime/3600f) + "時間");
        list.add(decoLore("釣獲最大コンボ") + MaxFishingCombo);
        list.add(decoLore("釣獲最高CPS") + String.format("%.2f", MaxFishingCPS));
        list.add(decoLore("エネミ討伐数") + TotalEnemyKills);
        list.add(decoLore("ボスエネミ討伐数") + TotalBossEnemyKills);
        list.add(decoLore("ダウン回数") + DownCount);
        list.add(decoLore("死亡回数") + DeathCount);
        list.add(decoLore("復活回数") + RevivalCount);
        list.add(decoLore("採掘数") + MineCount);
        list.add(decoLore("釣獲数") + FishingCount);
        list.add(decoLore("採取数") + HarvestCount);
        list.add(decoLore("伐採数") + LumberCount);
        list.add(decoLore("料理数") + CookCount);
        list.add(decoLore("精錬数") + SmeltCount);
        list.add(decoLore("消費強化石数") + UpgradeUseCostCount);
        list.add(decoLore("鍛冶装備作成数") + MakeEquipmentCount);
        list.add(decoLore("ポーション作成数") + MakePotionCount);
        list.add(decoLore("ストレイフ回数") + StrafeCount);
        list.add(decoLore("壁ジャンプ数") + WallJumpCount);
        return list;
    }

    public void checkTitle() {
        if (playerData.Level >= 30) titleManager.addTitle("プレイヤーレベル30");
        if (playerData.Level >= 40) titleManager.addTitle("プレイヤーレベル40");
        if (playerData.Level >= 50) titleManager.addTitle("プレイヤーレベル50");
        if (playerData.Level >= 60) titleManager.addTitle("プレイヤーレベル60");

        if (MaxFishingCPS >= 10) titleManager.addTitle("釣獲CPS10");
        for (int combo : FishingComboStatisticsList) {
            if (MaxFishingCombo >= combo) titleManager.addTitle("釣獲コンボ" + combo);
        }

        for (int count : UpgradeStoneStrafeStatisticsList) {
            if (UpgradeUseCostCount >= count) titleManager.addTitle("強化石消費" + count);
            if (StrafeCount >= count) titleManager.addTitle("ストレイフ回数" + count);
        }

        for (ClassData classData : DataBase.ClassList.values()) {
            if (playerData.Classes.getClassLevel(classData) >= 15) {
                titleManager.addTitle(classData.Display + "レベル15");
            }
            if (playerData.Classes.getClassLevel(classData) >= 25) {
                titleManager.addTitle(classData.Display + "レベル25");
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

        checkTitleEnemyKill();
    }

    public void checkTitleEnemyKill() {
        if (TotalEnemyKills >= 500) titleManager.addTitle("エネミー討伐500");
        if (TotalEnemyKills >= 2500) titleManager.addTitle("エネミー討伐2500");
        if (TotalEnemyKills >= 5000) titleManager.addTitle("エネミー討伐5000");
        if (TotalEnemyKills >= 10000) titleManager.addTitle("エネミー討伐10000");
        if (TotalEnemyKills >= 25000) titleManager.addTitle("エネミー討伐25000");
        if (TotalEnemyKills >= 50000) titleManager.addTitle("エネミー討伐50000");
        if (TotalEnemyKills >= 100000) titleManager.addTitle("エネミー討伐100000");
        if (TotalEnemyKills >= 250000) titleManager.addTitle("エネミー討伐250000");
        if (TotalEnemyKills >= 1000000) titleManager.addTitle("エネミー討伐1000000");

        if (TotalBossEnemyKills >= 500) titleManager.addTitle("ボスエネミー討伐500");
        if (TotalBossEnemyKills >= 1000) titleManager.addTitle("ボスエネミー討伐1000");
        if (TotalBossEnemyKills >= 2500) titleManager.addTitle("ボスエネミー討伐2500");
        if (TotalBossEnemyKills >= 5000) titleManager.addTitle("ボスエネミー討伐5000");
        if (TotalBossEnemyKills >= 10000) titleManager.addTitle("ボスエネミー討伐10000");
    }

    public void enemyKill(MobData mobData) {
        playerData.statistics.TotalEnemyKills++;
        if (mobData.enemyType.isBoss()) TotalBossEnemyKills++;
        switch (mobData.Id) {
            case "サイモア" -> {
                titleManager.addTitle("サイモア討伐");
                if ((AusMineB2.StartTime - AusMineB2.Time) < 60) titleManager.addTitle("サイモア討伐2");
            }
            case "グリフィア" -> {
                titleManager.addTitle("グリフィア討伐");
                if ((AusMineB4.StartTime - AusMineB4.Time) < 100) titleManager.addTitle("グリフィア討伐2");
            }
            case "リーライ" -> {
                titleManager.addTitle("リーライ討伐");
                if ((TarnetB1.StartTime - TarnetB1.Time) < 60) titleManager.addTitle("リーライ討伐2");
            }
            case "シノサス" -> {
                titleManager.addTitle("シノサス討伐");
                if ((TarnetB3.StartTime - TarnetB3.Time) < 100) titleManager.addTitle("シノサス討伐2");
            }
            case "エクスタ" -> {
                titleManager.addTitle("エクスタ討伐");
                if ((Novaha2.StartTime - Novaha2.Time) < 100) titleManager.addTitle("エクスタ討伐2");
            }
            case "ヴァノセト" -> {
                titleManager.addTitle("ヴァノセト討伐");
                if ((Novaha4.StartTime - Novaha4.Time) < 300) titleManager.addTitle("ヴァノセト討伐2");
                if ((Novaha4.StartTime - Novaha4.Time) < 125) titleManager.addTitle("ヴァノセト討伐ThePeriod");
            }
            case "ナイアス討伐" -> {
                titleManager.addTitle("ナイアス討伐");
                if ((AsharkB2.StartTime - AsharkB2.Time) < 150) titleManager.addTitle("ナイアス討伐2");
            }
            case "ハインド" -> {
                titleManager.addTitle("ハインド討伐");
                if ((AsharkB4.StartTime - AsharkB4.Time) < 1200) titleManager.addTitle("ハインド討伐2");
                if ((AsharkB4.StartTime - AsharkB4.Time) < 600) titleManager.addTitle("ハインド討伐PickYouUp");
            }
            case "訓練用ダミー" -> titleManager.addTitle("訓練用ダミー討伐");
        }
        checkTitleEnemyKill();
    }

    public void save(FileConfiguration data) {
        data.set("Statistics.PlayTime", playTime);
        data.set("Statistics.AFKTime", AFKTime);
        data.set("Statistics.MaxFishingCombo", MaxFishingCombo);
        data.set("Statistics.MaxFishingCPS", MaxFishingCPS);
        data.set("Statistics.TotalEnemyKills", TotalEnemyKills);
        data.set("Statistics.TotalBossEnemyKills", TotalBossEnemyKills);
        data.set("Statistics.DownCount", DownCount);
        data.set("Statistics.DeathCount", DeathCount);
        data.set("Statistics.RevivalCount", RevivalCount);
        data.set("Statistics.MineCount", MineCount);
        data.set("Statistics.FishingCount", FishingCount);
        data.set("Statistics.HarvestCount", HarvestCount);
        data.set("Statistics.LumberCount", LumberCount);
        data.set("Statistics.CookCount", CookCount);
        data.set("Statistics.SmeltCount", SmeltCount);
        data.set("Statistics.UpgradeUseCostCount", UpgradeUseCostCount);
        data.set("Statistics.MakeEquipmentCount", MakeEquipmentCount);
        data.set("Statistics.MakePotionCount", MakePotionCount);
        data.set("Statistics.StrafeCount", StrafeCount);
        data.set("Statistics.WallJumpCount", WallJumpCount);
    }

    public void load(FileConfiguration data) {
        playTime = data.getInt("Statistics.PlayTime", 0);
        AFKTime = data.getInt("Statistics.AFKTime", 0);
        MaxFishingCombo = data.getInt("Statistics.MaxFishingCombo", 0);
        MaxFishingCPS = data.getDouble("Statistics.MaxFishingCPS", 0d);
        TotalEnemyKills = data.getInt("Statistics.TotalEnemyKills", 0);
        TotalBossEnemyKills = data.getInt("Statistics.TotalBossEnemyKills", 0);
        DownCount = data.getInt("Statistics.DownCount", 0);
        DeathCount = data.getInt("Statistics.DeathCount", 0);
        RevivalCount = data.getInt("Statistics.RevivalCount", 0);
        MineCount = data.getInt("Statistics.MineCount", 0);
        FishingCount = data.getInt("Statistics.FishingCount", 0);
        HarvestCount = data.getInt("Statistics.HarvestCount", 0);
        LumberCount = data.getInt("Statistics.LumberCount", 0);
        CookCount = data.getInt("Statistics.CookCount", 0);
        SmeltCount = data.getInt("Statistics.SmeltCount", 0);
        UpgradeUseCostCount = data.getInt("Statistics.UpgradeUseCostCount", 0);
        MakeEquipmentCount = data.getInt("Statistics.MakeEquipmentCount", 0);
        MakePotionCount = data.getInt("Statistics.MakePotionCount", 0);
        StrafeCount = data.getInt("Statistics.StrafeCount", 0);
        WallJumpCount = data.getInt("Statistics.WallJumpCount", 0);
    }
}
