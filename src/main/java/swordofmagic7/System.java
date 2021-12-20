package swordofmagic7;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static swordofmagic7.DataBase.*;
import static swordofmagic7.Function.Log;
import static swordofmagic7.Function.colored;

public final class System extends JavaPlugin {

    static Plugin plugin;

    @Override
    public void onEnable() {
        plugin = this;

        new Events(this);
        new DataBase(this);
        PlayerList.load();

        for (Player player : Bukkit.getOnlinePlayers()) {
            playerData(player).load();
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
            playerData(player).save();
        }

        int count = 0;
        for (Entity entity : Bukkit.getWorld("world").getEntities()) {
            if (!(entity instanceof Player)) {
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
                if (cmd.getName().equalsIgnoreCase("gm")) {
                    if (args.length == 0) {
                        if (player.getGameMode().equals(GameMode.CREATIVE)) {
                            player.setGameMode(GameMode.ADVENTURE);
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
                    playerData(target).save();
                    return true;
                } else if (cmd.getName().equalsIgnoreCase("load")) {
                    Player target = player;
                    if (args.length == 1) {
                        target = Bukkit.getPlayer(args[0]);
                    }
                    playerData(target).load();
                    return true;
                } else if (cmd.getName().equalsIgnoreCase("playMode")) {
                    playerData.PlayMode = !playerData.PlayMode;
                    player.sendMessage(colored("&ePlayMode: " + playerData.PlayMode));
                    return true;
                } else if (cmd.getName().equalsIgnoreCase("spawn")) {
                    player.teleportAsync(Bukkit.getWorld("world").getSpawnLocation());
                    return true;
                }
            }

            if (cmd.getName().equalsIgnoreCase("menu")) {
                playerData.Menu.UserMenuView();
                return true;
            } else if (cmd.getName().equalsIgnoreCase("skill")) {
                playerData.Menu.SkillMenuView();
                return true;
            } else if (cmd.getName().equalsIgnoreCase("attribute")) {
                playerData.Menu.AttributeMenuView();
                return true;
            } else if (cmd.getName().equalsIgnoreCase("damageLog")) {
                playerData.DamageLog(!playerData.DamageLog);
                return true;
            } else if (cmd.getName().equalsIgnoreCase("expLog")) {
                playerData.ExpLog(!playerData.ExpLog);
                return true;
            } else if (cmd.getName().equalsIgnoreCase("pvpMode")) {
                playerData.PvPMode(!playerData.PvPMode);
                return true;
            } else if (cmd.getName().equalsIgnoreCase("viewFormat")) {
                if (playerData.ViewFormat < 3) playerData.setViewFormat(playerData.ViewFormat+1);
                else playerData.setViewFormat(0);
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