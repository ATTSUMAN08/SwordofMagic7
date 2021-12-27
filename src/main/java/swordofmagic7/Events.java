package swordofmagic7;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Equipment.EquipmentSlot;
import swordofmagic7.Mob.MobManager;
import swordofmagic7.Pet.PetManager;
import swordofmagic7.Pet.PetParameter;
import swordofmagic7.Skill.SkillProcess;
import swordofmagic7.Sound.SoundList;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Function.unColored;
import static swordofmagic7.Function.unDecoText;
import static swordofmagic7.Map.MapManager.TeleportGateSelector;
import static swordofmagic7.Mob.MobManager.*;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.System.tagGame;

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
        PlayerData playerData = playerData(player);
        List<PetParameter> PetSummon = new ArrayList<>(playerData.PetSummon);
        for (PetParameter pet : PetSummon) {
            pet.cage();
        }
        playerData.save();
        playerData.remove();
        PlayerList.load();
        if (tagGame.isPlayer(player)) {
            tagGame.leave(player);
        }
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
    public void onEntityInteract(EntityInteractEvent event) {
        Block block = event.getBlock();
        if(block.getType() == Material.FARMLAND) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        } else if (event.getAction() == Action.PHYSICAL) {
            if(event.getClickedBlock().getType() == Material.FARMLAND) {
                event.setCancelled(true);
            }
        }

        Action action = event.getAction();
        PlayerData playerData = playerData(player);
        if (playerData.PlayMode) {
            Block block = event.getClickedBlock();
            if (event.getHand() == org.bukkit.inventory.EquipmentSlot.HAND && playerData.Equipment.isEquip(EquipmentSlot.MainHand)) {
                if ((action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
                    if (playerData.CastMode.isLegacy()) {
                        if (player.isSneaking()) {
                            playerData.HotBar.use(4);
                        } else {
                            playerData.HotBar.use(0);
                        }
                    } else if (playerData.CastMode.isRenewed()) {
                        playerData.setRightClickHold();
                    } else if (playerData.CastMode.isHold()) {
                        int slot = player.getInventory().getHeldItemSlot();
                        if (slot < 9) {
                            if (player.isSneaking()) slot += 8;
                            playerData.HotBar.use(slot);
                        }
                    }
                    if (playerData.PetManager.usingBaton()) {
                        playerData.PetManager.PetAITarget();
                    }
                } else if ((action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)) {
                    if (player.isSneaking()) {
                        if (playerData.CastMode.isLegacy()) {
                            playerData.HotBar.use(3);
                        }
                        if (playerData.PetManager.usingBaton()) {
                            playerData.PetManager.PetAISelect();
                        }
                    } else {
                        playerData.Skill.SkillProcess.normalAttackTargetSelect();
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
        if (event.getHand() == org.bukkit.inventory.EquipmentSlot.HAND && entity.getCustomName() != null) {
            String shop = unColored(entity.getCustomName());
            if (ShopList.containsKey(shop)) {
                playerData.Shop.ShopOpen(getShopData(shop));
            } else if (shop.equalsIgnoreCase("ペットショップ")) {
                playerData.PetManager.PetShop();
            } else if (shop.equalsIgnoreCase("ルーン職人")) {
                playerData.RuneInventory.RuneMenuView();
            }
        }
    }

    @EventHandler
    public void onPressurePlate(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction().equals(Action.PHYSICAL)) {
            if (event.getClickedBlock().getType() == Material.STONE_PRESSURE_PLATE) {
                Material Under1 = event.getClickedBlock().getLocation().add(0, -1,0).getBlock().getType();
                Material Under2 = event.getClickedBlock().getLocation().add(0, -2,0).getBlock().getType();
                if (Under1 == Material.IRON_BLOCK) {
                    playSound(player, SoundList.Shoot);
                    player.setVelocity(player.getLocation().getDirection().multiply(0.4).setY(0.5));
                } else if (Under1 == Material.GOLD_BLOCK || Under2 == Material.GOLD_BLOCK) {
                    event.setCancelled(true);
                    player.setGravity(false);
                    new BukkitRunnable() {
                        double y = 1;
                        @Override
                        public void run() {
                            y -= 0.08;
                            player.setVelocity(player.getLocation().getDirection().multiply(2).setY(y));
                            if (y < -1 || player.isSneaking()) {
                                this.cancel();
                                player.setGravity(true);
                            }
                        }
                    }.runTaskTimerAsynchronously(plugin, 0, 1);
                    playSound(player, SoundList.Shoot);

                } else if (Under1 == Material.EMERALD_BLOCK || Under2 == Material.EMERALD_BLOCK) {
                    event.setCancelled(true);
                    Vector vec = new Vector(0, 2.8, 0);
                    player.setVelocity(vec);
                    playSound(player, SoundList.Shoot);

                }
            }
        }
    }

    @EventHandler
    void onToolChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = playerData(player);
        if (playerData.PlayMode) {
            if (playerData.CastMode.isRenewed() && event.getNewSlot() < 8) {
                int x = 0;
                if (player.isSneaking()) x += 8;
                if (playerData.isRightClickHold()) x += 16;
                player.getInventory().setHeldItemSlot(8);
                playerData.HotBar.use(event.getNewSlot() + x);
            }
            if (!playerData.CastMode.isHold()) event.setCancelled(true);
        }
    }

    @EventHandler
    void onDamage(EntityDamageEvent event) {
        Entity victim = event.getEntity();
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
            return;
        }
        if (victim instanceof Player) {
            event.setDamage(0.01);
        } else if (isEnemy(victim)) {
            if (event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION) {
                if (MobManager.isEnemy(victim)) {
                    MobManager.EnemyTable(victim.getUniqueId()).delete();
                    return;
                }
            }
            event.setDamage(0.01);
        }
    }

    @EventHandler
    void onDamageEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker && attacker.getGameMode() != GameMode.CREATIVE) {
            SkillProcess skillProcess = playerData(attacker).Skill.SkillProcess;
            if (skillProcess.isEnemy(event.getEntity())) {
                List<LivingEntity> victims = new ArrayList<>();
                victims.add((LivingEntity) event.getEntity());
                skillProcess.normalAttack(victims);
            }
            if (event.getEntity() instanceof Player victim) {
                if (tagGame.isPlayer(attacker) || tagGame.isPlayer(victim)) {
                    if (!attacker.hasPotionEffect(PotionEffectType.BLINDNESS)) {
                        tagGame.tagChange(attacker, victim);
                    }
                }
            } else if (event.getEntity() instanceof LivingEntity victim) {
                PlayerData playerData = playerData(attacker);
                if (playerData.PetManager.usingBaton()) {
                    playerData.PetManager.PetSelect(victim);
                }
            }
            event.setCancelled(true);
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
            if (playerData.CastMode.isLegacy()) {
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
            if (playerData.CastMode.isLegacy()) {
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
        Player player = event.getPlayer();
        if (event.getMessage().contains("${")) {
            event.setCancelled(true);
        }
        String message = event.getMessage();
        if (message.contains("%item%")) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType() != Material.AIR && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasDisplayName() && meta.hasLore()) {
                    String Display = unDecoText(meta.getDisplayName());
                    StringBuilder Lore = new StringBuilder(meta.getDisplayName());
                    for (String str : meta.getLore()) {
                        Lore.append("<nl>").append(str);
                    }
                    event.setMessage(message.replace("%item%", "§e[" + Display + "]<tag>" + Lore + "<end>"));
                }
            }
        }
    }

    @EventHandler
    void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!player.isOp() || playerData(player).PlayMode) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!player.isOp() || playerData(player).PlayMode) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    void onTarget(EntityTargetEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    void onSlimeSplit(SlimeSplitEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    void onJump(PlayerJumpEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = playerData(player);
        playerData.Strafe = 2;
        if (playerData(player).StrafeMode.isDoubleJump() || player.getGameMode() == GameMode.CREATIVE) {
            player.setAllowFlight(true);
        } else {
            player.setAllowFlight(false);
        }
    }

    @EventHandler
    void onSneakToggle(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (!player.isSneaking()) {
            TeleportGateSelector(player);
        }
        CharaController.WallKick(player);
    }

    @EventHandler
    void onSprintToggle(PlayerToggleSprintEvent event) {
        Player player = event.getPlayer();
        if (!player.isSprinting() && playerData(player).StrafeMode.isAirDash()) {
            CharaController.Strafe(player);
        }
    }

    @EventHandler
    void onFlightToggle(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
            player.setFlying(false);
            player.setAllowFlight(false);
            if (!player.isFlying() && playerData(player).StrafeMode.isDoubleJump()) {
                CharaController.Strafe(player);
            }
        }
    }

    @EventHandler
    void onChunkLoad(ChunkLoadEvent event) {
        Bukkit.getScheduler().runTaskLater(System.plugin, () -> {
            for (Entity entity : event.getChunk().getEntities()) {
                if (isEnemy(entity)) {
                    EnemyTable(entity.getUniqueId()).updateEntity();
                } else if (entity.getName().contains("§c§l《")) {
                    entity.remove();
                }
            }
        }, 5);
    }


}
