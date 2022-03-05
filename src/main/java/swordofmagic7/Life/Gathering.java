package swordofmagic7.Life;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Life.Harvest.HarvestData;
import swordofmagic7.Life.Harvest.HarvestItemData;
import swordofmagic7.Life.Lumber.LumberData;
import swordofmagic7.Life.Lumber.LumberItemData;
import swordofmagic7.Life.Mine.MineData;
import swordofmagic7.Life.Mine.MineItemData;
import swordofmagic7.Map.MapData;
import swordofmagic7.Sound.SoundList;

import java.util.HashMap;
import java.util.Random;

import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.System.BTTSet;
import static swordofmagic7.System.plugin;

public class Gathering {
    private final Random random = new Random();
    private final Player player;
    private final PlayerData playerData;

    public Gathering(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
    }

    public HashMap<Location, Material> ChangeBlock = new HashMap<>();
    public void ChangeBlock(Location location, Material material, int time) {
        ChangeBlock.put(location, material);
        BTTSet(new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                if (i > time*20) {
                    this.cancel();
                    ChangeBlock.remove(location);
                    player.sendBlockChange(location, location.getBlock().getType().createBlockData());
                } else {
                    player.sendBlockChange(location, material.createBlockData());
                    i++;
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 1), "ChangeBlock");
    }

    public void BlockBreak(PlayerData playerData, Block block) {
        if (!playerData.Gathering.ChangeBlock.containsKey(block.getLocation())) {
            MapData mapData = playerData.Map;
            LifeStatus lifeStatus = playerData.LifeStatus;
            Material playerTool = player.getInventory().getItemInMainHand().getType();
            String key = mapData.GatheringData.get(block.getType());
            int CoolTime = 0;
            Material material = null;
            if (playerTool == Material.IRON_PICKAXE && MineDataList.containsKey(key)) {
                MineData data = MineDataList.get(key);
                if (data.ReqLevel <= lifeStatus.getLevel(LifeType.Mine)) {
                    CoolTime = data.CoolTime;
                    if (block.getType().toString().contains(Material.DEEPSLATE.toString())) {
                        material = Material.DEEPSLATE;
                    } else {
                        material = Material.COBBLESTONE;
                    }
                    lifeStatus.addLifeExp(LifeType.Mine, data.Exp);
                    for (MineItemData itemData : data.itemData) {
                        if (random.nextDouble() <= itemData.Percent) {
                            playerData.ItemInventory.addItemParameter(itemData.itemParameter, 1);
                        }
                    }
                } else {
                    player.sendMessage("§e[採掘レベル]§aが§e[Lv" + data.ReqLevel + "]§a以上必要です");
                    playSound(player, SoundList.Nope);
                }
            } else if (playerTool == Material.IRON_AXE && LumberDataList.containsKey(key)) {
                LumberData data = LumberDataList.get(key);
                if (data.ReqLevel <= lifeStatus.getLevel(LifeType.Lumber)) {
                    CoolTime = data.CoolTime;
                    material = Material.STRIPPED_OAK_WOOD;
                    lifeStatus.addLifeExp(LifeType.Lumber, data.Exp);
                    for (LumberItemData itemData : data.itemData) {
                        if (random.nextDouble() <= itemData.Percent) {
                            playerData.ItemInventory.addItemParameter(itemData.itemParameter, 1);
                        }
                    }
                } else {
                    player.sendMessage("§e[伐採レベル]§aが§e[Lv" + data.ReqLevel + "]§a以上必要です");
                    playSound(player, SoundList.Nope);
                }
            } else if (playerTool == Material.SHEARS && HarvestDataList.containsKey(key)) {
                HarvestData data = HarvestDataList.get(key);
                if (data.ReqLevel <= lifeStatus.getLevel(LifeType.Harvest)) {
                    CoolTime = data.CoolTime;
                    material = Material.VOID_AIR;
                    lifeStatus.addLifeExp(LifeType.Harvest, data.Exp);
                    for (HarvestItemData itemData : data.itemData) {
                        if (random.nextDouble() <= itemData.Percent) {
                            playerData.ItemInventory.addItemParameter(itemData.itemParameter, 1);
                        }
                    }
                } else {
                    player.sendMessage("§e[採取レベル]§aが§e[Lv" + data.ReqLevel + "]§a以上必要です");
                    playSound(player, SoundList.Nope);
                }
            }
            if (material != null) {
                ChangeBlock(block.getLocation(), material, CoolTime);
                playerData.viewUpdate();
            }
        }
    }
}
