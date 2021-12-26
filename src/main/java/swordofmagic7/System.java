package swordofmagic7;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

import static swordofmagic7.CustomSound.playSound;
import static swordofmagic7.DataBase.*;
import static swordofmagic7.Function.*;
import static swordofmagic7.MobManager.getEnemyTable;
import static swordofmagic7.ParticleManager.WarpGateParticle;

public final class System extends JavaPlugin {

    static Plugin plugin;
    static HashMap<UUID, EnemyData> EnemyTable = new HashMap<>();
    static TagGame tagGame;

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
            WarpGateParticle(warp.Location, Particle.SPELL_WITCH);
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
    }

    @Override
    public void onDisable() {

        for (Hologram hologram : HologramsAPI.getHolograms(plugin)) {
            hologram.delete();
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (playerData.containsKey(player)) {
                PlayerData data = playerData.get(player);
                data.PetInventory.task.cancel();
                data.save();
                for (PetParameter pet : data.PetSummon) {
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
            if (!(entity instanceof Player || entity instanceof ItemFrame || entity instanceof ArmorStand || entity instanceof Minecart)) {
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
                            if (args.length == 2) amount = Integer.getInteger(args[1]);
                            playerData.ItemInventory.addItemParameter(getItemParameter(args[0]), amount);
                            playerData.ItemInventory.viewInventory();
                            return true;
                        }
                    }
                    for (Map.Entry<String, ItemParameter> str : getItemList().entrySet()) {
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
                }
            }

            if (cmd.getName().equalsIgnoreCase("menu") || cmd.getName().equalsIgnoreCase("m")) {
                playerData.Menu.UserMenuView();
                playSound(player, SoundList.MenuOpen);
                return true;
            } else if (cmd.getName().equalsIgnoreCase("skill")) {
                playerData.Menu.SkillMenuView();
                playSound(player, SoundList.MenuOpen);
                return true;
            } else if (cmd.getName().equalsIgnoreCase("attribute")) {
                playerData.Menu.AttributeMenuView();
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
                playerData.Menu.StatusInfoView(target);
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
                        tagGame.join(player);
                    } else if (args[0].equalsIgnoreCase("leave")) {
                        tagGame.leave(player);
                    }
                } else {
                    for (String str : tagGame.info()) {
                        player.sendMessage(str);
                    }
                    player.sendMessage("§e/tagGame [join/leave]");
                }
                return true;
            }
        }
        return false;
    }
}

final class PlayerList {
    private static final List<Player> PlayerList = new ArrayList<>();

    static void load() {
        PlayerList.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOnline()) {
                PlayerList.add(player);
            }
        }
    }

    static List<Player> get() {
        return PlayerList;
    }

    static List<Player> getNear(Location loc, double radius) {
        List<Player> List = new ArrayList<>();
        for (Player player : PlayerList) {
            if (player.getLocation().distance(loc) <= radius) List.add(player);
        }
        return List;
    }

    static List<LivingEntity> getNearLivingEntity(Location loc, double radius) {
        List<LivingEntity> List = new ArrayList<>();
        for (Player player : PlayerList) {
            if (player.getLocation().distance(loc) <= radius) List.add(player);
        }
        return List;
    }
}

class ReturnPackage {
    boolean bool;
    String string;

    ReturnPackage(boolean bool) {
        this.bool = bool;
    }

    ReturnPackage(boolean bool, String string) {
        this.bool = bool;
        this.string = string;
    }
}