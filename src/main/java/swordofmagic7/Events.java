package swordofmagic7;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Gate;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.material.Lever;
import org.bukkit.plugin.Plugin;

import java.util.Random;

import static swordofmagic7.DataBase.*;
import static swordofmagic7.Function.colored;
import static swordofmagic7.Function.unColored;
import static swordofmagic7.MobManager.getEnemyTable;
import static swordofmagic7.MobManager.mobSpawn;

public class Events implements Listener {

    Plugin plugin;

    public Events(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        playerData(player).load();
        PlayerList.load();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        playerData(player).save();
        playerData(player).remove();
        PlayerList.load();
    }

    @EventHandler
    void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        PlayerData playerData = playerData(player);
        if (playerData.PlayMode) {
            playerData.Menu.MenuClick(event);
            playerData.viewUpdate();
        }
    }

    @EventHandler
    void onInventoryOpen(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        PlayerData playerData = playerData(player);
        if (playerData.PlayMode) {
            playerData.viewUpdate();
        }
    }

    @EventHandler
    void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        PlayerData playerData = playerData(player);
        if (playerData.PlayMode) {
            playerData.viewUpdate();
            playerData.Menu.MenuClose(event);
            playerData.Status.StatusUpdate();
        }
    }

    @EventHandler
    void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() instanceof Door) event.setCancelled(true);
        if (event.getClickedBlock() instanceof TrapDoor) event.setCancelled(true);
        if (event.getClickedBlock() instanceof Gate) event.setCancelled(true);
        if (event.getClickedBlock() instanceof Lever) event.setCancelled(true);
        if (event.getClickedBlock() instanceof ItemFrame) event.setCancelled(true);

        Action action = event.getAction();
        Player player = event.getPlayer();
        PlayerData playerData = playerData(player);
        if (playerData.PlayMode) {
            Block block = event.getClickedBlock();
            if (event.getHand() == org.bukkit.inventory.EquipmentSlot.HAND && playerData.Equipment.isEquip(EquipmentSlot.MainHand)) {
                if ((action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
                    if (playerData.CastType.isLegacy()) {
                        if (player.isSneaking()) {
                            playerData.HotBar.use(4);
                        } else {
                            playerData.HotBar.use(0);
                        }
                    } else if (playerData.CastType.isRenewed()) {
                        playerData.setRightClickHold();
                    }
                } else if ((action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)) {
                    if (player.isSneaking()) {
                        playerData.HotBar.use(3);
                    } else {
                        playerData.Skill.SkillProcess.normalAttack();
                    }
                }
            }

            if (block != null && block.getState() instanceof Sign sign) {
                if (sign.getLine(0).equals("訓練用ダミー")) {
                    Random random = new Random();
                    mobSpawn(getMobData("訓練用ダミー"), 1, sign.getLocation().add(random.nextDouble(), 0, random.nextDouble()));
                }

                if (sign.getLine(0).equals("モジュラーブレード")) {
                    playerData.ItemInventory.addItemParameter(getItemParameter("モジュラーブレード"), 1);
                    playerData.viewUpdate();
                }
            }
        }
    }

    @EventHandler
    void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = playerData(player);
        Entity entity = event.getRightClicked();
        if (entity.getCustomName() != null) {
            String shop = unColored(entity.getCustomName());
            if (ShopList.containsKey(shop)) {
                playerData.Menu.ShopOpen(getShopData(shop));
            }
        }
    }

    @EventHandler
    void onToolChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = playerData(player);
        if (playerData.PlayMode) {
            if (playerData.CastType.isRenewed() && event.getNewSlot() < 8) {
                int x = 0;
                if (player.isSneaking()) x += 8;
                if (playerData.isRightClickHold()) x += 16;
                player.getInventory().setHeldItemSlot(8);
                playerData.HotBar.use(event.getNewSlot() + x);
            }
            if (!playerData.CastType.isHold()) event.setCancelled(true);
        }
    }

    @EventHandler
    void onDamage(EntityDamageEvent event) {
        Entity victim = event.getEntity();
        if (victim instanceof Player) {
            event.setDamage(0.01);
        } else if (getEnemyTable().containsKey(victim.getUniqueId())) {
            event.setDamage(0.01);
        }
    }

    @EventHandler
    void onDamageEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker) {
            if (getEnemyTable().containsKey(event.getEntity().getUniqueId())) {
                PlayerData playerData = playerData(attacker);
                playerData.Skill.SkillProcess.normalAttack();
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    void onExplosionPrime(ExplosionPrimeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    void onOffHandSwitch(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = playerData(player);
        if (playerData.PlayMode) {
            if (playerData.CastType.isLegacy()) {
                if (player.isSneaking()) {
                    playerData.HotBar.use(5);
                } else {
                    playerData.HotBar.use(1);
                }
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = playerData(player);
        if (playerData.PlayMode) {
            if (playerData.CastType.isLegacy()) {
                if (player.isSneaking()) {
                    playerData.HotBar.use(6);
                } else {
                    playerData.HotBar.use(2);
                }
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    void onChat(AsyncPlayerChatEvent event) {
        if (event.getMessage().contains("${")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("Trigger");
        }
    }

    @EventHandler
    void onBlockPlace(BlockPlaceEvent event) {
        if (!event.getPlayer().isOp()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    void onBlockBreak(BlockBreakEvent event) {
        if (!event.getPlayer().isOp()) {
            event.setCancelled(true);
        }
    }
}
