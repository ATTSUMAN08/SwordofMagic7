package swordofmagic7;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import it.unimi.dsi.fastutil.Hash;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Item.RuneParameter;
import swordofmagic7.Map.WarpGateParameter;
import swordofmagic7.Mob.EnemyData;
import swordofmagic7.Mob.MobData;
import swordofmagic7.Mob.MobManager;
import swordofmagic7.Pet.PetParameter;
import swordofmagic7.Sound.SoundList;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Data.PlayerData.playerDataList;
import static swordofmagic7.Function.*;
import static swordofmagic7.Mob.MobManager.getEnemyTable;
import static swordofmagic7.Sound.CustomSound.playSound;

public final class System extends JavaPlugin {

    public static Plugin plugin;
    public static TagGame tagGame;

    private static HashMap<UUID, EnemyData> EnemyTable = new HashMap<>();

    @Override
    public void onEnable() {
        plugin = this;
        EnemyTable = getEnemyTable();
        tagGame = new TagGame();

        new Events(this);
        new DataBase(this);

        PlayerList.load();

        for (Player player : Bukkit.getOnlinePlayers()) {
            playerData(player).load();
        }

        for (WarpGateParameter warp : WarpGateList.values()) {
            warp.start();
        }

        World world = Bukkit.getWorld("world");
        world.setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.DO_FIRE_TICK, false);
        world.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, false);
        world.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
        world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);
        world.setGameRule(GameRule.NATURAL_REGENERATION, false);
        world.setGameRule(GameRule.MOB_GRIEFING, false);
        world.setGameRule(GameRule.DO_MOB_LOOT, false);

        BTTSet(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            BroadCast("§e[オートセーブ]§aを§b開始§aします");
            for (PlayerData playerData : PlayerData.playerDataList().values()) {
                playerData.save();
            }
            BroadCast("§e[オートセーブ]§aが§b完了§aしました");
        }, 200, 6000), "AutoSave");
    }

    @Override
    public void onDisable() {

        for (Hologram hologram : HologramsAPI.getHolograms(plugin)) {
            hologram.delete();
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            HashMap<UUID, PlayerData> list = playerDataList();
            if (list.containsKey(player.getUniqueId())) {
                PlayerData playerData = list.get(player.getUniqueId());
                playerData.PetInventory.task.cancel();
                playerData.save();
                for (PetParameter pet : playerData.PetSummon) {
                    pet.entity.remove();
                }
            }
        }

        int count = 0;
        for (EnemyData enemyData : EnemyTable.values()) {
            enemyData.entity.remove();
            count++;
        }
        for (Entity entity : Bukkit.getWorld("world").getEntities()) {
            if (!(entity instanceof Player) && ignoreEntity(entity)) {
                entity.remove();
                count++;
            }
        }
        Log("CleanEnemy: " + count);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player player) {
            PlayerData playerData = playerData(player);
            if (player.hasPermission("som7.debug")) {
                if (cmd.getName().equalsIgnoreCase("test")) {

                } else if (cmd.getName().equalsIgnoreCase("gm")) {
                    if (args.length == 0) {
                        if (player.getGameMode().equals(GameMode.CREATIVE)) {
                            player.setGameMode(GameMode.SURVIVAL);
                        } else {
                            player.setGameMode(GameMode.CREATIVE);
                        }
                    } else {
                        if (args[0].equalsIgnoreCase("0")) {
                            player.setGameMode(GameMode.SURVIVAL);
                        } else if (args[0].equalsIgnoreCase("1")) {
                            player.setGameMode(GameMode.CREATIVE);
                        } else if (args[0].equalsIgnoreCase("2")) {
                            player.setGameMode(GameMode.ADVENTURE);
                        } else if (args[0].equalsIgnoreCase("3")) {
                            player.setGameMode(GameMode.SPECTATOR);
                        }
                    }
                    return true;
                } else if (cmd.getName().equalsIgnoreCase("get")) {
                    if (args.length >= 1) {
                        if (getItemList().containsKey(args[0])) {
                            int amount = 1;
                            if (args.length == 2) amount = Integer.parseInt(args[1]);
                            playerData.ItemInventory.addItemParameter(getItemParameter(args[0]), amount);
                            playerData.ItemInventory.viewInventory();
                            return true;
                        }
                    }
                    for (Map.Entry<String, ItemParameter> str : getItemList().entrySet()) {
                        player.sendMessage(str.getKey());
                    }
                    return true;
                } else if (cmd.getName().equalsIgnoreCase("getRune")) {
                    if (args.length >= 1) {
                        if (getRuneList().containsKey(args[0])) {
                            RuneParameter rune = getRuneParameter(args[0]);
                            rune.Level = 1;
                            if (args.length >= 2) rune.Level = Integer.parseInt(args[1]);
                            if (args.length >= 3) rune.Quality = Double.parseDouble(args[2]);
                            playerData.RuneInventory.addRuneParameter(rune);
                            playerData.RuneInventory.viewRune();
                            return true;
                        }
                    }
                    for (Map.Entry<String, RuneParameter> str : DataBase.getRuneList().entrySet()) {
                        player.sendMessage(str.getKey());
                    }
                    return true;
                } else if (cmd.getName().equalsIgnoreCase("mobSpawn")) {
                    if (args.length >= 1) {
                        if (getMobList().containsKey(args[0])) {
                            int level = 1;
                            if (args.length == 2) level = Integer.parseInt(args[1]);
                            MobManager.mobSpawn(getMobData(args[0]), level, player.getLocation());
                            return true;
                        }
                    }
                    for (Map.Entry<String, MobData> str : getMobList().entrySet()) {
                        player.sendMessage(str.getKey());
                    }
                    return true;
                } else if (cmd.getName().equalsIgnoreCase("save")) {
                    Player target = player;
                    if (args.length == 1) {
                        target = Bukkit.getPlayer(args[0]);
                    }
                    if (target.isOnline()) {
                        playerData(target).save();
                    } else {
                        player.sendMessage("§c無効なプレイヤーです");
                    }
                    return true;
                } else if (cmd.getName().equalsIgnoreCase("load")) {
                    Player target = player;
                    if (args.length == 1) {
                        target = Bukkit.getPlayer(args[0]);
                    }
                    if (target.isOnline()) {
                        playerData(target).load();
                    } else {
                        player.sendMessage("§c無効なプレイヤーです");
                    }
                    return true;
                } else if (cmd.getName().equalsIgnoreCase("playMode")) {
                    playerData.PlayMode = !playerData.PlayMode;
                    if (playerData.PlayMode) {
                        player.setGameMode(GameMode.SURVIVAL);
                        player.closeInventory();
                    } else {
                        player.setGameMode(GameMode.CREATIVE);
                        player.getInventory().clear();
                    }
                    player.sendMessage("§ePlayMode: " + playerData.PlayMode);
                    return true;
                } else if (cmd.getName().equalsIgnoreCase("flySpeed")) {
                    if (args.length == 1) {
                        player.setFlySpeed(Float.parseFloat(args[0]));
                    } else {
                        player.setFlySpeed(0.2f);
                    }
                    player.sendMessage("FlySpeed: " + player.getFlySpeed());
                    return true;
                } else if (cmd.getName().equalsIgnoreCase("bukkitTasks")) {
                    player.sendMessage("TaggedTasks: ");
                    HashMap<String, Integer> TagCount = new HashMap<>();
                    if (BukkitTaskTag != null) {
                        HashMap<BukkitTask, String> tasks = (HashMap<BukkitTask, String>) BukkitTaskTag.clone();
                        for (Map.Entry<BukkitTask, String> task : tasks.entrySet()) {
                            if (!task.getKey().isCancelled()) {
                                String[] split = task.getValue().split(":");
                                TagCount.put(split[0], TagCount.getOrDefault(split[0], 0) + 1);
                            } else {
                                BukkitTaskTag.remove(task.getKey());
                            }
                        }
                    }
                    player.sendMessage("PendingTask: " + Bukkit.getScheduler().getPendingTasks().size());
                    player.sendMessage("TaggedTask: " + BukkitTaskTag.size());
                    for (Map.Entry<String, Integer> tagCount : TagCount.entrySet()) {
                        player.sendMessage(tagCount.getKey() + ": " + tagCount.getValue());
                    }
                    return true;
                } else if (cmd.getName().equalsIgnoreCase("loadedPlayer")) {
                    player.sendMessage("Loaded PlayerData: ");
                    HashMap<UUID, PlayerData> list = playerDataList();
                    for (Map.Entry<UUID, PlayerData> loopData : list.entrySet()) {
                        player.sendMessage(Bukkit.getOfflinePlayer(loopData.getKey()).getName() + ": " + loopData.getKey());
                    }
                }
            }

            if (cmd.getName().equalsIgnoreCase("menu") || cmd.getName().equalsIgnoreCase("m")) {
                playerData.Menu.UserMenuView();
                playSound(player, SoundList.MenuOpen);
                return true;
            } else if (cmd.getName().equalsIgnoreCase("skill")) {
                playerData.Skill.SkillMenuView();
                playSound(player, SoundList.MenuOpen);
                return true;
            } else if (cmd.getName().equalsIgnoreCase("attribute")) {
                playerData.Attribute.AttributeMenuView();
                playSound(player, SoundList.MenuOpen);
                return true;
            } else if (cmd.getName().equalsIgnoreCase("damageLog")) {
                playerData.DamageLog();
                return true;
            } else if (cmd.getName().equalsIgnoreCase("expLog")) {
                playerData.ExpLog();
                return true;
            } else if (cmd.getName().equalsIgnoreCase("dropLog")) {
                playerData.DropLog();
                return true;
            } else if (cmd.getName().equalsIgnoreCase("pvpMode")) {
                playerData.PvPMode();
                return true;
            } else if (cmd.getName().equalsIgnoreCase("strafeMode")) {
                playerData.StrafeMode();
                return true;
            } else if (cmd.getName().equalsIgnoreCase("castMode")) {
                playerData.CastMode();
                return true;
            } else if (cmd.getName().equalsIgnoreCase("viewFormat")) {
                if (playerData.ViewFormat < 3) playerData.setViewFormat(playerData.ViewFormat+1);
                else playerData.setViewFormat(0);
                return true;
            } else if (cmd.getName().equalsIgnoreCase("spawn")) {
                MapList.get("Alden").enter(player);
                player.teleportAsync(SpawnLocation);
                return true;
            } else if (cmd.getName().equalsIgnoreCase("info")) {
                Player target = player;
                if (args.length == 1) {
                    target = Bukkit.getPlayer(args[0]);
                }
                playerData.Menu.StatusInfo.StatusInfoView(target);
                return true;
            } else if (cmd.getName().equalsIgnoreCase("tickTime")) {
                for (World world : Bukkit.getWorlds()) {
                    player.sendMessage("§e" + world.getName() + "§7: §a" + world.getFullTime());
                }
                return true;
            } else if (cmd.getName().equalsIgnoreCase("reqExp")) {
                if (args.length == 2) {
                    try {
                        int level = Integer.parseInt(args[0]);
                        int tier = Integer.parseInt(args[1]);
                        int reqExp = playerData.Classes.ReqExp(level, tier);
                        player.sendMessage("§b[T" + tier + "] §eLv" + level + "§7: §a" + reqExp);
                    } catch (Exception ignored) {
                        player.sendMessage("§e/reqExp <Level> <Tier>");
                    }
                } else {
                    player.sendMessage("§e/reqExp <Level> <Tier>");
                }
                return true;
            } else if (cmd.getName().equalsIgnoreCase("tagGame")) {
                if (args.length >= 1) {
                    if (args[0].equalsIgnoreCase("join")) {
                        TagGame.join(player);
                    } else if (args[0].equalsIgnoreCase("leave")) {
                        TagGame.leave(player);
                    }
                } else {
                    for (String str : TagGame.info()) {
                        player.sendMessage(str);
                    }
                    player.sendMessage("§e/tagGame [join/leave]");
                }
                return true;
            }
        }
        return false;
    }

    public static HashMap<BukkitTask, String> BukkitTaskTag = new HashMap<>();
    public static void BTTSet(BukkitTask task, String tag) {
        BukkitTaskTag.put(task, tag);
    }
}