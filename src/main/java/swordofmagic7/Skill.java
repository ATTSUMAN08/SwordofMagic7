package swordofmagic7;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

import static swordofmagic7.CustomSound.playSound;
import static swordofmagic7.DataBase.*;
import static swordofmagic7.Function.*;
import static swordofmagic7.MobManager.EnemyTable;
import static swordofmagic7.MobManager.isEnemy;
import static swordofmagic7.ParticleManager.ShapedParticle;
import static swordofmagic7.PetManager.PredicatePet;
import static swordofmagic7.RayTrace.rayLocationEntity;
import static swordofmagic7.SoundList.*;

enum CastType {
    Legacy("レガジー"),
    Renewed("リニュード"),
    Hold("ホールド"),
    ;

    String Display;

    CastType(String Display) {
        this.Display = Display;
    }

    boolean isLegacy() {
        return this == Legacy;
    }

    boolean isRenewed() {
        return this == Renewed;
    }

    boolean isHold() {
        return this == Hold;
    }
}

enum SkillType {
    Active("アクティブ"),
    Passive("パッシブ"),
    ;

    String Display;

    SkillType(String Display) {
        this.Display = Display;
    }

    boolean isActive() {
        return this == Active;
    }

    boolean isPassive() {
        return this == Passive;
    }
}

class SkillData {
    String Id;
    Material Icon;
    String Display;
    SkillType SkillType;
    List<String> Lore = new ArrayList<>();
    List<SkillParameter> Parameter = new ArrayList<>();
    int Mana = 0;
    int CastTime = 0;
    int RigidTime = 0;
    int CoolTime = 0;
    List<EquipmentCategory> ReqMainHand = new ArrayList<>();

    ItemStack view() {
        if (Icon == null) Icon = Material.END_CRYSTAL;
        ItemStack item = new ItemStack(Icon);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(decoText(Display));
        List<String> Lore = new ArrayList<>(this.Lore);
        Lore.add(decoText("§3§lスキルステータス"));
        for (SkillParameter param : Parameter) {
            Lore.add(decoLore(param.Display) + param.Prefix + param.valueView() + param.Suffix);
        }
        Lore.add(decoText("§3§lスキル情報"));
        Lore.add(decoLore("スキルタイプ") + SkillType.Display);
        if (SkillType.isActive()) {
            Lore.add(decoLore("消費マナ") + Mana);
            Lore.add(decoLore("詠唱時間") + (double) CastTime / 20 + "秒");
            Lore.add(decoLore("硬直時間") + (double) RigidTime / 20 + "秒");
            Lore.add(decoLore("再使用時間") + (double) CoolTime / 20 + "秒");
        }
        Lore.add(decoLore("使用可能武器種") + SkillType.Display);
        for (EquipmentCategory category : ReqMainHand) {
            Lore.add("§7・§e§l" + category.Display);
        }
        meta.setLore(Lore);
        for (ItemFlag flag : ItemFlag.values()) {
            meta.addItemFlags(flag);
        }
        item.setItemMeta(meta);
        return item;
    }
}

class SkillParameter {
    String Display;
    double Value = 0;
    double Increase = 0;
    String Prefix = "";
    String Suffix = "";
    boolean isInt;

    String valueView() {
        if (isInt) {
            return String.valueOf((int) Math.round(Value));
        } else {
            return String.valueOf(Value);
        }
    }
}

public class Skill {
    private final Plugin plugin;
    private final Player player;
    private final PlayerData playerData;
    private boolean CastReady = true;
    SkillProcess SkillProcess;
    private final HashMap<SkillData, Integer> SkillCoolTime = new HashMap<>();
    private final HashMap<SkillData, Integer> SkillLevel = new HashMap<>();
    int SkillPoint = 0;
    Skill(Player player, PlayerData playerData, Plugin plugin) {
        this.player = player;
        this.playerData = playerData;
        this.plugin = plugin;
        SkillProcess = new SkillProcess(player, playerData, plugin, this);
    }

    void setCastReady(boolean bool) {
        CastReady = bool;
        SkillProcess.SkillCastTime = 0;
    }

    boolean isCastReady() {
        return CastReady;
    }

    void CastSkill(SkillData skillData) {
        if (CastReady && isAlive(player)) {
            if (CategoryCheck(EquipmentSlot.MainHand, skillData.ReqMainHand)) {
                if (!SkillCoolTime.containsKey(skillData)) {
                    new BukkitRunnable() {
                        float p = 0;
                        @Override
                        public void run() {
                            p = (float) SkillProcess.SkillCastTime/skillData.CastTime;
                            if (p >= 1) this.cancel();
                            player.sendTitle(" ", "§e" + String.format("%.0f", p*100) + "%", 0, 10, 0);
                        }
                    }.runTaskTimerAsynchronously(plugin, 0, 1);
                    switch (skillData.Id) {
                        case "Slash" -> SkillProcess.Slash(skillData);
                        case "Vertical" -> SkillProcess.Vertical(skillData);
                        case "HammerStole" -> SkillProcess.HammerStole(skillData);
                        case "Rain" -> SkillProcess.Rain(skillData);
                        case "DoubleTrigger" -> SkillProcess.DoubleTrigger(skillData);
                        case "Infall" -> SkillProcess.Infall(skillData);
                    }
                    setSkillCoolTime(skillData);
                } else {
                    player.sendMessage("§e[" + skillData.Display + "]§aを§b使用可能§aまで§c§l" + SkillCoolTime.get(skillData) / 20f + "秒§aです");
                }
            }
        }
    }

    void setSkillCoolTime(SkillData skillData) {
        SkillCoolTime.put(skillData, skillData.CoolTime);
        new BukkitRunnable() {
            @Override
            public void run() {
                SkillCoolTime.put(skillData, SkillCoolTime.get(skillData)-1);
                if (SkillCoolTime.get(skillData) <= 0) {
                    this.cancel();
                    SkillCoolTime.remove(skillData);
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 1);
    }

    private boolean CategoryCheck(EquipmentSlot slot, EquipmentCategory category) {
        List<EquipmentCategory> list = new ArrayList<>();
        list.add(category);
        return CategoryCheck(slot, list);
    }

    boolean hasSkill(String skill) {
        for (ClassData classData : playerData.Classes.classTier) {
            if (classData.SkillList.contains(getSkillData(skill))) {
                return true;
            }
        }
        return false;
    }

    void addSkillLevel(SkillData skillData, int add) {
        if (SkillPoint >= add) {
            SkillPoint -= add;
            SkillLevel.put(skillData, SkillLevel.get(skillData) + add);
        } else {
            player.sendMessage("§eポイント§aが足りません");
        }
    }

    void setSkillLevel(SkillData skillData, int attr) {
        SkillLevel.put(skillData, attr);
    }

    void resetSkillLevel(ClassData classData) {
        SkillPoint = playerData.Classes.getLevel(classData)-1;
        for (SkillData skillData : classData.SkillList) {
            SkillLevel.put(skillData, 0);
        }
    }

    private boolean CategoryCheck(EquipmentSlot slot, List<EquipmentCategory> categoryList) {
        if (categoryList.size() == 0) return true;
        boolean check = false;
        String Display = "";
        for (EquipmentCategory category: categoryList) {
            if (Display.equals("")) {
                Display = category.Display;
            } else {
                Display += ", " + category.Display;
            }
            if (playerData.Equipment.getEquip(slot).EquipmentCategory == category) {
                check = true;
                break;
            }
        }
        if (check) {
            return true;
        } else {
            player.sendMessage("§aこの§eスキル§aの§b発動§aには§e" + slot.Display + "§aに§e[" + Display + "]§aを§e装備§aしてる§c必要§aがあります");
            return false;
        }
    }
}

class SkillProcess {
    private final Plugin plugin;
    private final Player player;
    private final PlayerData playerData;
    private final Skill Skill;

    public SkillProcess(Player player, PlayerData playerData, Plugin plugin, Skill Skill) {
        this.player = player;
        this.playerData = playerData;
        this.plugin = plugin;
        this.Skill = Skill;
    }

    Predicate<LivingEntity> Predicate() {
        return entity -> entity != player && isEnemy(entity);
    }

    Predicate<Entity> PredicateE() {
        return entity -> entity != player && isEnemy(entity);
    }

    boolean isEnemy(Entity enemy) {
        if (PetManager.isPet((LivingEntity) enemy)) {
            PetParameter pet = PetManager.PetParameter((LivingEntity) enemy);
            enemy = pet.player;
        }
        if (enemy == player) {
            return false;
        }
        if (enemy instanceof Player target) {
            if (target.isOnline() && isAlive(target)) {
                PlayerData targetData = playerData(target);
                return playerData.PvPMode && targetData.PvPMode;
            } else return false;
        } else if (MobManager.isEnemy(enemy)) {
            EnemyTable(enemy.getUniqueId()).updateEntity();
            return true;
        } else return false;
    }

    private List<LivingEntity> FanShapedCollider(Location location, double radius, double angle, Predicate<LivingEntity> Predicate, boolean single) {
        List<LivingEntity> Targets = (List<LivingEntity>) location.getNearbyLivingEntities(radius, Predicate);
        if (Targets.size() == 0) return Targets;
        Targets = ParticleManager.FanShapedCollider(location, Targets, angle);
        if (single) Targets = Nearest(location, Targets);
        return Targets;
    }

    private List<LivingEntity> RectangleCollider(Location location, double length, double width, Predicate<LivingEntity> Predicate, boolean single) {
        List<LivingEntity> Targets = (List<LivingEntity>) location.getNearbyLivingEntities(length, Predicate);
        if (Targets.size() == 0) return Targets;
        Targets = ParticleManager.RectangleCollider(location, Targets, length, width);
        if (single) Targets = Nearest(location, Targets);
        return Targets;
    }

    List<LivingEntity> Nearest(Location location, List<LivingEntity> Entities) {
        return Nearest(location, Entities, 64);
    }

    List<LivingEntity> Nearest(Location location, List<LivingEntity> Entities, double distance) {
        if (Entities.size() == 0) return new ArrayList<>();
        LivingEntity target = null;
        for (LivingEntity entity : Entities) {
            if (location.distance(entity.getLocation()) < distance) {
                distance = location.distance(entity.getLocation());
                target = entity;
            }
        }
        List<LivingEntity> Targets = new ArrayList<>();
        if (target != null) {
            Targets.add(target);
        }
        return Targets;
    }

    private final ParticleData particleCasting = new ParticleData(Particle.REDSTONE, new Particle.DustOptions(Color.YELLOW, 1));
    private final ParticleData particleActivate = new ParticleData(Particle.REDSTONE, new Particle.DustOptions(Color.ORANGE, 1));
    private final int period = 1;
    private int normalAttackCoolTime = 0;
    int SkillCastTime = 0;

    void normalAttackTargetSelect() {
        if (playerData.Equipment.isEquip(EquipmentSlot.MainHand)) {
            if (0 >= normalAttackCoolTime) {
                EquipmentCategory category = playerData.Equipment.getEquip(EquipmentSlot.MainHand).EquipmentCategory;
                List<LivingEntity> victims = new ArrayList<>();
                switch (category) {
                    case Blade -> victims = RectangleCollider(player.getLocation(), 4, 0.75, Predicate(), true);
                    case Hammer -> victims = RectangleCollider(player.getLocation(), 6, 1.25, Predicate(), true);
                    case Rod, ActGun -> {
                        Ray ray = rayLocationEntity(player.getEyeLocation(), 20, 0.5, PredicateE());
                        if (ray.isHitEntity()) victims = List.of(ray.HitEntity);
                    }
                }
                normalAttack(victims);
            }
        }
    }

    void normalAttack(List<LivingEntity> victims) {
        final String damageSource = "attack";
        if (playerData.Equipment.isEquip(EquipmentSlot.MainHand)) {
            if (0 >= normalAttackCoolTime) {
                EquipmentCategory category = playerData.Equipment.getEquip(EquipmentSlot.MainHand).EquipmentCategory;
                switch (category) {
                    case Blade -> {
                        Damage.makeDamage(player, victims, DamageCause.ATK, damageSource, 1, 1, 2);
                        normalAttackCoolTime = 12;
                    }
                    case Hammer -> {
                        Damage.makeDamage(player, victims, DamageCause.ATK, damageSource, 1.25, 1, 2);
                        normalAttackCoolTime = 15;
                    }
                    case Rod -> {
                        ParticleManager.LineParticle(new ParticleData(Particle.CRIT_MAGIC), player.getEyeLocation(), 15, 0, 10);
                        Damage.makeDamage(player, victims, DamageCause.MAT, damageSource, 1, 1, 2);
                        playSound(player, RodAttack);
                        normalAttackCoolTime = 12;
                    }
                    case ActGun -> {
                        ParticleManager.LineParticle(new ParticleData(Particle.CRIT), player.getEyeLocation(), 15, 0, 10);
                        Damage.makeDamage(player, victims, DamageCause.MAT, damageSource, 1, 1, 2);
                        playSound(player, GunAttack);
                        normalAttackCoolTime = 10;
                    }
                    case Baton -> {
                        if (playerData.PetSummon.size() == 0) {
                            player.sendMessage("§e[ペット]§aが§e召喚§aされていません");
                            playSound(player, Nope);
                        }
                    }
                    default -> {
                        player.sendMessage("§e[武器]§aが§e装備§aされていません");
                        playSound(player, Nope);
                    }
                }
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        normalAttackCoolTime--;
                        if (normalAttackCoolTime <= 0) this.cancel();
                    }
                }.runTaskTimerAsynchronously(plugin, 0, 1);
            }
        } else {
            player.sendMessage("§e[武器]§aが§e装備§aされていません");
            playSound(player, Nope);
        }
    }

    void Slash(SkillData skillData) {
        Skill.setCastReady(false);
        final double radius = 5;
        final double angle = 70;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (SkillCastTime < skillData.CastTime) {
                    ParticleManager.FanShapedParticle(particleCasting, player.getLocation(), radius, angle, 3);
                } else {
                    this.cancel();
                    ParticleManager.FanShapedParticle(particleActivate, player.getLocation(), radius, angle, 3);
                    List<LivingEntity> victims = FanShapedCollider(player.getLocation(), radius, angle, Predicate(), false);
                    Damage.makeDamage(player, victims, DamageCause.ATK, skillData.Id, skillData.Parameter.get(0).Value / 100, 1, 2);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> Skill.setCastReady(true), skillData.RigidTime);
                    ShapedParticle(new ParticleData(Particle.SWEEP_ATTACK), player.getLocation(), radius, angle, angle/2, 1, true);
                }
                SkillCastTime += period;
            }
        }.runTaskTimer(plugin, 0, period);
    }

    void Vertical(SkillData skillData) {
        Skill.setCastReady(false);
        final double length = 10;
        final double width = 2.5;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (SkillCastTime < skillData.CastTime) {
                    ParticleManager.RectangleParticle(particleCasting, player.getLocation(), length, width, 3);
                } else {
                    this.cancel();
                    ParticleManager.RectangleParticle(particleActivate, player.getLocation(), length, width, 3);
                    List<LivingEntity> victims = RectangleCollider(player.getLocation(), length, width, Predicate(), false);
                    Damage.makeDamage(player, victims, DamageCause.ATK, skillData.Id, skillData.Parameter.get(0).Value / 100, 1, 2);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> Skill.setCastReady(true), skillData.RigidTime);
                }
                SkillCastTime += period;
            }
        }.runTaskTimer(plugin, 0, period);
    }

    void Rain(SkillData skillData) {
        Skill.setCastReady(false);
        final double radius = 5;
        final Location loc = RayTrace.rayLocation(player.getEyeLocation(),32, 0.1, false, Predicate()).HitPosition;
        loc.setPitch(90);
        final Location origin = RayTrace.rayLocationBlock(loc, 32, false).HitPosition;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (SkillCastTime < skillData.CastTime) {
                    ParticleManager.CircleParticle(particleCasting, origin, radius, 30);
                } else {
                    this.cancel();
                    ParticleManager.CircleParticle(particleActivate, origin, radius, 30);
                    List<LivingEntity> victims = (List<LivingEntity>) origin.getNearbyLivingEntities(radius, Predicate());
                    Damage.makeDamage(player, victims, DamageCause.MAT, skillData.Id, skillData.Parameter.get(0).Value/100, 1, 2);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> Skill.setCastReady(true), skillData.RigidTime);
                }
                SkillCastTime += period;
            }
        }.runTaskTimer(plugin, 0, period);
    }

    void DoubleTrigger(SkillData skillData) {
        Skill.setCastReady(false);
        final Location loc = RayTrace.rayLocation(player.getEyeLocation(),32, 0.1, false, Predicate()).HitPosition;
        loc.setPitch(90);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (SkillCastTime > skillData.CastTime) {
                    this.cancel();
                    ParticleManager.LineParticle(new ParticleData(Particle.CRIT), player.getEyeLocation(), 15, 0, 10);
                    Ray ray = rayLocationEntity(player.getEyeLocation(), 20, 0.5, entity -> entity != player);
                    if (ray.isHitEntity()) Damage.makeDamage(player, ray.HitEntity, DamageCause.MAT, skillData.Id, skillData.Parameter.get(0).Value/100, 2);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> Skill.setCastReady(true), skillData.RigidTime);
                    playSound(player, GunAttack, 2, 2);
                }
                SkillCastTime += period;
            }
        }.runTaskTimer(plugin, 0, period);
    }

    void HammerStole(SkillData skillData) {
        Skill.setCastReady(false);
        final double radius = 8;
        final double angle = 110;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (SkillCastTime < skillData.CastTime) {
                    ParticleManager.FanShapedParticle(particleCasting, player.getLocation(), radius, angle, 3);
                } else {
                    this.cancel();
                    ParticleManager.FanShapedParticle(particleActivate, player.getLocation(), radius, angle, 3);
                    List<LivingEntity> victims = FanShapedCollider(player.getLocation(), radius, angle, Predicate(), false);
                    Damage.makeDamage(player, victims, DamageCause.ATK, skillData.Id, skillData.Parameter.get(0).Value / 100, 1, 2);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> Skill.setCastReady(true), skillData.RigidTime);
                    ShapedParticle(new ParticleData(Particle.SWEEP_ATTACK), player.getLocation(), radius, angle, angle/2, 1, true);
                }
                SkillCastTime += period;
            }
        }.runTaskTimer(plugin, 0, period);
    }

    void Infall(SkillData skillData) {
        Skill.setCastReady(false);
        final double radius = 10;
        final Location origin = player.getLocation().clone();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (SkillCastTime < skillData.CastTime) {
                    ParticleManager.CircleParticle(particleCasting, origin, radius, 30);
                } else {
                    this.cancel();
                    ParticleManager.CircleParticle(particleActivate, origin, radius, 30);
                    List<LivingEntity> victims = (List<LivingEntity>) origin.getNearbyLivingEntities(radius, Predicate());
                    Damage.makeDamage(player, victims, DamageCause.MAT, skillData.Id, skillData.Parameter.get(0).Value/100, 1, 2);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> Skill.setCastReady(true), skillData.RigidTime);
                }
                SkillCastTime += period;
            }
        }.runTaskTimer(plugin, 0, period);
    }
}
