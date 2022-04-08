package swordofmagic7;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent;
import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Dungeon.Dungeon;
import swordofmagic7.Life.FishingCommand;
import swordofmagic7.Mob.MobManager;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Npc.NpcMessage;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.Pet.PetManager;
import swordofmagic7.Pet.PetParameter;
import swordofmagic7.Skill.SkillProcess;
import swordofmagic7.Sound.SoundList;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Function.*;
import static swordofmagic7.Mob.MobManager.*;
import static swordofmagic7.SomCore.random;
import static swordofmagic7.SomCore.spawnPlayer;
import static swordofmagic7.Sound.CustomSound.playSound;

public class Events implements Listener {

    Plugin plugin;

    public Events(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (ServerId.equalsIgnoreCase("Event")) {
            List<String> ignoreList = YamlConfiguration.loadConfiguration(new File(DataBasePath, "IgnoreIPCheck.yml")).getStringList("IgnoreUUID");
            if (!ignoreList.contains(event.getUniqueId().toString())) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getAddress().toString().split(":")[0].equals(event.getAddress().toString().split(":")[0])) {
                        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "§aすでに§c別アカウント§aで§bログイン§aしています。§e別CH§aをお試しください");
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        if (Bukkit.getOnlinePlayers().size() >= 40) {
            if (!player.hasPermission("som7.OverLogin")) {
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "§bCH§aは§c満員§aです。§e別CH§aをお試しください");
            }
        }
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
        if (playerData.Party != null) {
            playerData.Party.Quit(player);
        }
        if (playerData.hologram != null) playerData.hologram.delete();
        playerData.saveCloseInventory();
        PlayerList.load();
        if (TagGame.isPlayer(player)) {
            TagGame.leave(player);
        }
        playerData.remove();
    }

    @EventHandler
    void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        PlayerData playerData = playerData(player);
        if (playerData.PlayMode) {
            playerData.Menu.MenuClick(event);
            playerData.viewUpdate();
        } else if (event.getCurrentItem() != null) {
            playerData.MapManager.TeleportGateMenuClick(event.getView(), event.getSlot());
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
        MultiThread.TaskRunSynchronizedLater(() -> {
            Player player = (Player) event.getPlayer();
            if (player.isOnline()) {
                PlayerData playerData = playerData(player);
                if (playerData.PlayMode) {
                    playerData.viewUpdate();
                    playerData.Menu.MenuClose(event);
                    playerData.Status.StatusUpdate();
                }
            }
        }, 1);
    }

    @EventHandler
    public void onEntityInteract(EntityInteractEvent event) {
        Block block = event.getBlock();
        if(block.getType() == Material.FARMLAND) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    void onPlayerLectern(PlayerTakeLecternBookEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = playerData(player);
        Block block = event.getClickedBlock();
        Action action = event.getAction();
        if (player.getGameMode() != GameMode.CREATIVE) {
            if (block != null) {
                if (block.getType() != Material.LECTERN && !playerData.Map.isGathering(block.getType())) {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }
        } else if (event.getAction() == Action.PHYSICAL) {
            if (block != null && block.getType() == Material.FARMLAND) {
                event.setCancelled(true);
            }
        }
        if (block == null && Function.isHoldFishingRod(player) && action.isRightClick()) {
            playerData.Gathering.inputFishingCommand(FishingCommand.RightClick);
            event.setCancelled(false);
        }
        if (playerData.PlayMode && player.getGameMode() != GameMode.SPECTATOR) {
            if (event.getHand() == org.bukkit.inventory.EquipmentSlot.HAND) {
                switch (action) {
                    case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> {
                        if (playerData.CastMode.isLegacy() && !Function.isHoldFishingRod(player)) {
                            if (player.isSneaking()) {
                                playerData.HotBar.use(4);
                            } else {
                                playerData.HotBar.use(0);
                            }
                        } else if (playerData.CastMode.isRenewed()) {
                            playerData.setRightClickHold();
                        } else if (playerData.CastMode.isHold()) {
                            int slot = player.getInventory().getHeldItemSlot();
                            if (slot < 8) {
                                if (player.isSneaking()) slot += 8;
                                playerData.HotBar.use(slot);
                            }
                        }
                        if (playerData.PetManager.usingBaton()) {
                            playerData.PetManager.PetAITarget();
                        }
                    }
                    case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> {
                        if (player.isSneaking()) {
                            if (playerData.CastMode.isLegacy()) {
                                playerData.HotBar.use(3);
                            }
                            if (playerData.PetManager.usingBaton()) {
                                playerData.PetManager.PetAISelect();
                            }
                        } else {
                            if (playerData.PetManager.usingBaton()) {
                                playerData.PetManager.PetSelect();
                            }
                        }
                        playerData.Skill.SkillProcess.normalAttackTargetSelect();
                    }
                }
            }

            if (block != null && block.getState() instanceof Sign sign) {
                if (sign.getLine(0).equals("訓練用ダミー")) {
                    mobSpawn(getMobData("訓練用ダミー"), 1, sign.getLocation().add(random.nextDouble(), 0, random.nextDouble()));
                }
            }
        }
    }

    @EventHandler
    void onRecipeBookClick(PlayerRecipeBookClickEvent event) {
        event.setCancelled(true);
        Player player = event.getPlayer();
        PlayerData playerData = playerData(player);
        playerData.Menu.UserMenuView();
    }

    @EventHandler
    void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = playerData(player);
        Entity entity = event.getRightClicked();
        if (player.getGameMode() != GameMode.CREATIVE && ignoreEntity(entity)) event.setCancelled(true);
        if (playerData.PlayMode && event.getHand() == org.bukkit.inventory.EquipmentSlot.HAND && entity.getCustomName() != null) {
            event.setCancelled(true);
            NPCRegistry npcRegistry = CitizensAPI.getNPCRegistry();
            if (npcRegistry.isNPC(entity)) {
                NPC npc = npcRegistry.getNPC(entity);
                String shop = unColored(entity.getCustomName());
                if (ShopList.containsKey(shop)) {
                    playerData.Shop.ShopOpen(getShopData(shop));
                    playSound(player, SoundList.MenuOpen);
                } else if (shop.equalsIgnoreCase("ペットショップ")) {
                    playerData.PetShop.PetShopOpen();
                } else if (shop.equalsIgnoreCase("ルーン職人")) {
                    playerData.RuneShop.RuneMenuView();
                } else if (shop.equalsIgnoreCase("転職神官")) {
                    playerData.Classes.ClassSelectView(true);
                } else if (shop.equalsIgnoreCase("買取屋")) {
                    playerData.Shop.ShopSellOpen();
                } else if (shop.equalsIgnoreCase("鍛冶場")) {
                    playerData.Menu.Smith.SmithMenuView();
                } else if (shop.equalsIgnoreCase("料理場")) {
                    playerData.Menu.Cook.CookMenuView();
                } else if (shop.equalsIgnoreCase("回復神官")) {
                    playerData.Status.Health = playerData.Status.MaxHealth;
                    playerData.Status.Mana = playerData.Status.MaxMana;
                    ParticleManager.CylinderParticle(new ParticleData(Particle.VILLAGER_HAPPY), player.getLocation(), 1, 2, 3, 3);
                    playSound(player, SoundList.Heal);
                } else if (shop.equalsIgnoreCase("マーケット")) {
                    playerData.Menu.Market.MarketMenuView();
                } else if (shop.equalsIgnoreCase("チュートリアル最低限編")) {
                    Tutorial.tutorialTrigger(player, 0);
                } else if (shop.equalsIgnoreCase("チュートリアルスキル編")) {
                    Tutorial.tutorialTrigger(player, 5);
                } else {
                    Dungeon.Trigger(shop);
                }
                if (NpcList.containsKey(npc.getId())) {
                    MultiThread.TaskRun(() -> {
                        NpcMessage.ShowMessage(player, npc);
                    }, "ShowMessage");
                }
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
                    MultiThread.TaskRun(() -> {
                        double y = 1;
                        while (y > -1 && !player.isSneaking()) {
                            y -= 0.08;
                            player.setVelocity(player.getLocation().getDirection().multiply(2).setY(y));
                            MultiThread.sleepTick(1);
                        }
                        player.setGravity(true);
                    }, "PressurePlate");
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
            if (Function.isHoldFishingRod(player)) {
                switch (event.getNewSlot()) {
                    case 0 -> playerData.Gathering.inputFishingCommand(FishingCommand.Shift);
                    case 1 -> playerData.Gathering.inputFishingCommand(FishingCommand.Drop);
                    case 2 -> playerData.Gathering.inputFishingCommand(FishingCommand.RightClick);
                }
                player.getInventory().setHeldItemSlot(8);
                event.setCancelled(true);
            }
            if (playerData.CastMode.isRenewed() && event.getNewSlot() < 8) {
                int x = 0;
                if (player.isSneaking()) x += 8;
                if (playerData.isRightClickHold()) x += 16;
                player.getInventory().setHeldItemSlot(8);
                int slot = event.getNewSlot() + x;
                if (!playerData.Gathering.FishingInProgress) {
                    playerData.HotBar.use(slot);
                }
            }
            if (!playerData.CastMode.isHold()) event.setCancelled(true);
        }
    }

    @EventHandler
    void onDamage(EntityDamageEvent event) {
        Entity victim = event.getEntity();
        event.setCancelled(true);
        switch (event.getCause()) {
            case FALL, HOT_FLOOR, FIRE_TICK -> {
                victim.setFireTicks(0);
                return;
            }
            case LAVA, DROWNING -> {
                if (victim instanceof Player player) {
                    PlayerData playerData = playerData(player);
                    playerData.changeHealth(-playerData.Status.MaxHealth/10);
                }
            }
            case VOID -> {
                if (victim instanceof Player player) {
                    spawnPlayer(player);
                }
            }
        }
        if (isEnemy(victim)) {
            if (event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION) {
                if (MobManager.isEnemy(victim)) {
                    MobManager.EnemyTable(victim.getUniqueId()).delete();
                }
            }
        }
    }

    @EventHandler
    void onDamageEntity(EntityDamageByEntityEvent event) {
        Entity victim = event.getEntity();
        if (event.getDamager() instanceof Player attacker && attacker.getGameMode() != GameMode.CREATIVE) {
            PlayerData attackerData = playerData(attacker);
            SkillProcess skillProcess = attackerData.Skill.SkillProcess;
            if (skillProcess.isEnemy(victim)) {
                Set<LivingEntity> victims = new HashSet<>();
                victims.add((LivingEntity) victim);
                skillProcess.normalAttack(victims);
            } else if (PetManager.isPet(victim) && attackerData.PetManager.usingBaton()) {
                if (attacker.isSneaking()) {
                    attackerData.PetManager.PetAISelect();
                } else {
                    attackerData.PetManager.PetSelect((LivingEntity) victim);
                }
            } else if (event.getEntity() instanceof Player player) {
                if (TagGame.isPlayer(attacker) || TagGame.isPlayer(player)) {
                    if (!attacker.hasPotionEffect(PotionEffectType.BLINDNESS)) {
                        TagGame.tagChange(attacker, player);
                    }
                }
            } else if (PetManager.isPet(event.getEntity())) {

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
            } else {
                CharaController.WallKick(player);
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = playerData(player);
        if (playerData.PlayMode) {
            if (isHoldFishingRod(player)) {
                playerData.Gathering.inputFishingCommand(FishingCommand.Drop);
            }
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

    @EventHandler(priority = EventPriority.HIGHEST)
    void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = playerData(player);
        if (playerData.isPTChat) {
            playerData.Party.chat(playerData, event.getMessage());
            event.setMessage("$cancel");
        }
        if (event.getMessage().contains("${")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("som7.builder") || playerData(player).PlayMode) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (!player.hasPermission("som7.builder") || playerData(player).PlayMode) {
            event.setCancelled(true);
            PlayerData playerData = playerData(player);
            playerData.Gathering.BlockBreak(playerData, block);
        }
    }

    @EventHandler
    void onBlockDamage(BlockDamageEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = playerData(player);
        Block block = event.getBlock();
        if (!playerData.Map.isGathering(block.getType())) {
            event.setCancelled(true);
        }
        if (player.getGameMode() != GameMode.CREATIVE) {
            event.setInstaBreak(false);
        }
    }

    @EventHandler
    void onTarget(EntityTargetEvent event) {
        if (MobManager.isEnemy(event.getEntity())) {
            event.setCancelled(true);
        }
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
        player.setAllowFlight(playerData.StrafeMode.isDoubleJump() || player.getGameMode() == GameMode.CREATIVE);
    }

    @EventHandler
    void onSneakToggle(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = playerData(player);
        if (!player.isSneaking()) {
            playerData.MapManager.WarpGateSelector();
            playerData.MapManager.TeleportGateSelector();
            if (Function.isHoldFishingRod(player)) playerData.Gathering.inputFishingCommand(FishingCommand.Shift);
        } else {
            CharaController.WallKick(player);
        }
        if (playerData.isDead && playerData.deadTime < 1100) {
            playerData.deadTime = 0;
        }
        MultiThread.TaskRunSynchronizedLater(() -> {
            if (playerData.CastMode.isRenewed() || playerData.CastMode.isHold()) {
                playerData.HotBar.UpdateHotBar();
            }
        }, 1);
    }

    @EventHandler
    void onSprintToggle(PlayerToggleSprintEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = playerData(player);
        if (!player.isSprinting() && playerData.StrafeMode.isAirDash()) {
            CharaController.Strafe(player);
        } else if (playerData.isDead) {

        }
    }

    @EventHandler
    void onFlightToggle(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.CREATIVE) {
            PlayerData playerData = playerData(player);
            event.setCancelled(true);
            player.setFlying(false);
            if (!player.isFlying() && playerData.StrafeMode.isDoubleJump()) {
                CharaController.Strafe(player);
            }
            if (playerData.Strafe == 0) {
                player.setAllowFlight(false);
            }
        }
    }

    @EventHandler
    void onChunkLoad(ChunkLoadEvent event) {

    }

    @EventHandler
    void onEntitiesUnload(EntitiesUnloadEvent event) {
        for (Entity entity : event.getEntities()) {
            if (entity.hasMetadata("SomEntity")) {
                entity.remove();
            } else if (MobManager.isEnemy(entity)) {
                EnemyTable.remove(entity.getUniqueId().toString());
                entity.remove();
            } else if (PetManager.isPet(entity)) {
                PetManager.PetParameter(entity).cage();
                entity.remove();
            } else if (!ignoreEntity(entity)) {
                entity.remove();
            }
        }
    }

    @EventHandler
    void onBlockExplode(BlockExplodeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    void onFishing(PlayerFishEvent event) {
        playerData(event.getPlayer()).Gathering.Fishing(event);
    }

    @EventHandler
    void onTeleport(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    void onSpectate(PlayerStartSpectatingEntityEvent event) {
        event.setCancelled(true);
    }
}
