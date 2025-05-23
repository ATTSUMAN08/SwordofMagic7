package swordofmagic7.Life;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Function;
import swordofmagic7.Life.Angler.AnglerData;
import swordofmagic7.Life.Angler.AnglerItemData;
import swordofmagic7.Life.Harvest.HarvestData;
import swordofmagic7.Life.Harvest.HarvestItemData;
import swordofmagic7.Life.Lumber.LumberData;
import swordofmagic7.Life.Lumber.LumberItemData;
import swordofmagic7.Life.Mine.MineData;
import swordofmagic7.Life.Mine.MineItemData;
import swordofmagic7.Map.MapData;
import swordofmagic7.MultiThread.MultiThread;
import net.somrpg.swordofmagic7.SomCore;
import swordofmagic7.Sound.SoundList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Data.Statistics.FishingComboStatisticsList;
import static swordofmagic7.Function.*;
import static swordofmagic7.Sound.CustomSound.playSound;
import static net.somrpg.swordofmagic7.SomCore.instance;
import static net.somrpg.swordofmagic7.SomCore.random;

public class Gathering {
    private final Player player;
    private final PlayerData playerData;

    public Gathering(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
    }

    public static HashMap<String, ChangeBlock> ChangeBlock = new HashMap<>();

    public static ChangeBlock ChangeBlock(Player player) {
        String uuid = player.getUniqueId().toString();
        if (!ChangeBlock.containsKey(uuid)) {
            ChangeBlock.put(uuid, new ChangeBlock());
        }
        return ChangeBlock.get(uuid);
    }

    public static void ChangeBlock(Player player, Block block, Material material, int time) {
        MultiThread.TaskRun(() -> {
            ChangeBlock(player).put(block.getLocation(), material);
            MultiThread.sleepTick(1);
            player.sendBlockChange(block.getLocation(), material.createBlockData());
            MultiThread.TaskRunLater(() -> {
                ChangeBlock(player).remove(block.getLocation());
                MultiThread.sleepTick(1);
                player.sendBlockChange(block.getLocation(), block.getBlockData());
            }, time*20, "ChangeBlock");
        }, "ChangeBlock");
    }

    public void BlockBreak(PlayerData playerData, Block block) {
        MultiThread.TaskRun(() ->{
            if (!ChangeBlock(player).checkLocation(block.getLocation())) {
                MapData mapData = playerData.Map;
                LifeStatus lifeStatus = playerData.LifeStatus;
                Material playerTool = player.getInventory().getItemInMainHand().getType();
                String keyText = block.getType().toString();
                String key = mapData.GatheringData.get(keyText);
                int CoolTime = 0;
                Material material = null;
                if (playerTool == Material.IRON_PICKAXE && MineDataList.containsKey(key)) {
                    MineData data = MineDataList.get(key);
                    playerData.Equipment.setToolEquipment(data.ReqLevel);
                    if (data.ReqLevel <= lifeStatus.getLevel(LifeType.Mine)) {
                        CoolTime = data.CoolTime;
                        if (block.getType().toString().contains(Material.DEEPSLATE.toString())) {
                            material = Material.DEEPSLATE;
                        } else {
                            material = Material.COBBLESTONE;
                        }
                        lifeStatus.addLifeExp(LifeType.Mine, data.Exp);
                        playerData.statistics.MineCount++;
                        for (MineItemData itemData : data.itemData) {
                            if (random.nextDouble() <= itemData.Percent) {
                                int amount = lifeStatus.getMultiplyAmount(LifeType.Mine);
                                playerData.ItemInventory.addItemParameter(itemData.itemParameter, amount);
                                if (playerData.DropLog.isItem()) ItemGetLog(player, itemData.itemParameter, amount);
                            }
                        }
                        if (random.nextDouble() <= 0.05) {
                            playerData.ItemInventory.addItemParameter(getItemParameter("強化石"), 1);
                            if (playerData.DropLog.isItem()) ItemGetLog(player, getItemParameter("強化石"), 1);
                        }
                    } else {
                        player.sendMessage("§e[採掘レベル]§aが§e[Lv" + data.ReqLevel + "]§a以上必要です");
                        playSound(player, SoundList.NOPE);
                    }
                } else if (playerTool == Material.IRON_AXE && LumberDataList.containsKey(key)) {
                    LumberData data = LumberDataList.get(key);
                    playerData.Equipment.setToolEquipment(data.ReqLevel);
                    if (data.ReqLevel <= lifeStatus.getLevel(LifeType.Lumber)) {
                        CoolTime = data.CoolTime;
                        material = Material.STRIPPED_OAK_WOOD;
                        lifeStatus.addLifeExp(LifeType.Lumber, data.Exp);
                        playerData.statistics.LumberCount++;
                        for (LumberItemData itemData : data.itemData) {
                            if (random.nextDouble() <= itemData.Percent) {
                                int amount = lifeStatus.getMultiplyAmount(LifeType.Lumber);
                                playerData.ItemInventory.addItemParameter(itemData.itemParameter, amount);
                                if (playerData.DropLog.isItem()) ItemGetLog(player, itemData.itemParameter, amount);
                            }
                        }
                    } else {
                        player.sendMessage("§e[伐採レベル]§aが§e[Lv" + data.ReqLevel + "]§a以上必要です");
                        playSound(player, SoundList.NOPE);
                    }
                } else if (playerTool == Material.SHEARS && HarvestDataList.containsKey(key)) {
                    HarvestData data = HarvestDataList.get(key);
                    if (data.ReqLevel <= lifeStatus.getLevel(LifeType.Harvest)) {
                        CoolTime = data.CoolTime;
                        material = Material.VOID_AIR;
                        lifeStatus.addLifeExp(LifeType.Harvest, data.Exp);
                        playerData.statistics.HarvestCount++;
                        for (HarvestItemData itemData : data.itemData) {
                            if (random.nextDouble() <= itemData.Percent) {
                                int amount = lifeStatus.getMultiplyAmount(LifeType.Harvest);
                                playerData.ItemInventory.addItemParameter(itemData.itemParameter, amount);
                                if (playerData.DropLog.isItem()) ItemGetLog(player, itemData.itemParameter, amount);
                            }
                        }
                    } else {
                        player.sendMessage("§e[採取レベル]§aが§e[Lv" + data.ReqLevel + "]§a以上必要です");
                        playSound(player, SoundList.NOPE);
                    }
                }
                if (material != null) {
                    ChangeBlock(player, block, material, CoolTime);
                    playerData.viewUpdate();
                }
            }
        }, "BlockBreak");
    }

    public static final int MaxWaitTime = 10;
    public static final int MinWaitTime = 0;
    public static final int MissLeft = 3;
    public static final FishingCommand[] FishingCommands = FishingCommand.values();

    public FishingCommand[] inputFishingCommand;
    public int inputProgress = 0;
    public int FishingMissCount = 0;
    public int FishingComboBoost = 0;
    public boolean FishingUseCombo = true;
    public int FishingSetCombo = 0;
    public boolean FishingInProgress = false;
    public FishingCommand[] requestFishingCommand;

    public void Fishing(PlayerFishEvent event) {
        PlayerFishEvent.State state = event.getState();
        FishHook hook = event.getHook();
        event.setExpToDrop(0);
        switch (state) {
            case FISHING -> {
                hook.setMinWaitTime(MinWaitTime);
                hook.setMaxWaitTime(MaxWaitTime);
            }
            case BITE -> {
                if (!FishingInProgress) {
                    Entity caught = event.getCaught();
                    if (caught != null) caught.remove();
                    event.setCancelled(true);
                    hook.setMaxWaitTime(Integer.MAX_VALUE);
                    hook.setMinWaitTime(Integer.MAX_VALUE-1);
                    FishingHit(hook);
                }
            }
            case REEL_IN, CAUGHT_FISH, FAILED_ATTEMPT -> {
                if (FishingInProgress) {
                    event.setCancelled(true);
                }
            }
        }
    }

    public void FishingHit(FishHook hook) {
        if (!FishingInProgress) {
            MultiThread.TaskRun(() -> {
                inputProgress = 0;
                FishingMissCount = 0;
                int commandLength = 7;
                int MissLeft = Gathering.MissLeft;
                int time = 0;
                float multiply = 1;
                int combo;
                if (FishingUseCombo) {
                    combo = FishingComboBoost;
                } else {
                    combo = FishingSetCombo;
                }
                commandLength += combo;
                MissLeft += Math.floor(combo / 10f);
                multiply += combo / 20f;
                requestFishingCommand = new FishingCommand[commandLength];
                inputFishingCommand = new FishingCommand[commandLength];
                FishingInProgress = true;
                for (int i = 0; i < requestFishingCommand.length; i++) {
                    requestFishingCommand[i] = FishingCommands[random.nextInt(FishingCommands.length)];
                }
                while (playerWhileCheck(playerData) && isAlive(player) && inputProgress < requestFishingCommand.length && Function.isHoldFishingRod(player) && FishingMissCount < MissLeft) {
                    if (!playerData.isAFK()) {
                        StringBuilder title = new StringBuilder();
                        StringBuilder preview = new StringBuilder();
                        int length = Math.min(requestFishingCommand.length, inputProgress + 20);
                        for (int i = inputProgress; i < length; i++) {
                            if (requestFishingCommand[i] == null) break;
                            title.append(requestFishingCommand[i].getDisplayColored(playerData.FishingDisplayNum)).append(" ");
                            preview.append("  ");
                        }
                        preview.append(title);
                        String subTitle = "§7MissLeft " + (MissLeft - FishingMissCount);
                        if (!FishingUseCombo) {
                            subTitle = "                      " + subTitle + "    §eTime" + String.format("%.2f", time * 0.05) + "秒";
                        }
                        player.sendTitle(preview.toString(), subTitle, 0, 5, 0);
                    }
                    if (!FishingUseCombo) {
                        if (time > 3600 + combo*10) {
                            inputProgress = requestFishingCommand.length;
                            break;
                        }
                    }
                    time++;
                    MultiThread.sleepTick(1);
                }
                if (!instance.isEnabled() || !player.isOnline()) {
                    return;
                }
                FishingInProgress = false;
                if (FishingMissCount < MissLeft && inputProgress == requestFishingCommand.length) {
                    List<AnglerData> dataList = new ArrayList<>();
                    int i = 0;
                    while (playerData.Map.GatheringData.containsKey("Fishing-" + i)) {
                        dataList.add(AnglerDataList.get(playerData.Map.GatheringData.get("Fishing-" + i)));
                        i++;
                    }
                    AnglerData data = null;
                    if (dataList.size() > 1) {
                        data = dataList.get(random.nextInt(dataList.size() - 1));
                    } else if (dataList.size() == 1) {
                        data = dataList.getFirst();
                    }
                    if (data != null) {
                        AnglerItemData hitData = data.itemData.getLast();
                        for (AnglerItemData itemData : data.itemData) {
                            if (random.nextDouble() < itemData.Percent * multiply) {
                                hitData = itemData;
                                break;
                            }
                        }
                        int amount = playerData.LifeStatus.getMultiplyAmount(LifeType.Angler);
                        double percent = hitData.Percent*multiply/30f;
                        while (percent >= 1) {
                            percent--;
                            amount++;
                        }
                        if (random.nextDouble() < percent) amount++;
                        playerData.ItemInventory.addItemParameter(hitData.itemParameter, amount);
                        playerData.LifeStatus.addLifeExp(LifeType.Angler, (int) Math.round(data.Exp * hitData.expMultiply * multiply));
                        playerData.statistics.FishingCount++;
                        double timePerSecond = requestFishingCommand.length / (time * 0.05);
                        if (FishingUseCombo) FishingComboBoost++;
                        player.sendMessage("§e[" + hitData.itemParameter.Display + "§ax" + amount + "§e]§aを釣りあげました！ §b[" + combo + "Combo] §e[" + String.format("%.2f", time * 0.05) + "秒] §c[" + String.format("%.2f", timePerSecond) + "/秒] §7[" + FishingMissCount + "Miss] §b[+" + (int) ((multiply - 1) * 100) + "%]");
                        SomCore.instance.getPlayerLastLocation().remove(player);
                        MultiThread.TaskRunLater(() -> {
                            if (!hook.isDead()) FishingHit(hook);
                        }, 60, "FishingHookHit");
                        playSound(player, SoundList.LEVEL_UP);

                        if (playerData.statistics.MaxFishingCombo < FishingComboBoost) {
                            playerData.statistics.MaxFishingCombo = FishingComboBoost;
                            player.sendMessage("§b最高コンボ§aを§e更新§aしました！");
                        }
                        if (playerData.statistics.MaxFishingCPS < timePerSecond) {
                            playerData.statistics.MaxFishingCPS = timePerSecond;
                            player.sendMessage("§b最高CPS§aを§e更新§aしました！");
                        }
                        if (timePerSecond >= 5) {
                            boolean nonMiss = FishingMissCount == 0;
                            boolean cps6 = timePerSecond >= 6;
                            for (int combo2 : FishingComboStatisticsList) {
                                if (combo >= combo2) {
                                    playerData.titleManager.addTitle("釣獲コンボ" + combo2 + "CPS5");
                                    if (nonMiss) playerData.titleManager.addTitle("釣獲コンボ" + combo2 + "CPS5Miss0");
                                    if (cps6) playerData.titleManager.addTitle("釣獲コンボ" + combo2 + "CPS6");
                                }
                            }
                            if (timePerSecond >= 10) {
                                playerData.titleManager.addTitle("釣獲CPS10");
                            }
                        }
                    }
                } else {
                    if (FishingUseCombo) {
                        FishingComboBoost -= 10;
                        if (FishingComboBoost < 0) FishingComboBoost = 0;
                    }
                    player.sendMessage("§e釣獲§aに§c失敗§aしました");
                    playSound(player, SoundList.TICK);
                    MultiThread.TaskRunSynchronized(hook::remove);
                }
            }, "Fishing");
        }
    }

    public void inputFishingCommand(FishingCommand command) {
        if (FishingInProgress && inputProgress < inputFishingCommand.length) {
            if (inputFishingCommand[inputProgress] == null) {
                if (requestFishingCommand[inputProgress] == command) {
                    inputProgress++;
                    playSound(player, SoundList.TICK);
                } else {
                    FishingMissCount++;
                    playSound(player, SoundList.NOPE);
                }
            }
        }
    }

}
