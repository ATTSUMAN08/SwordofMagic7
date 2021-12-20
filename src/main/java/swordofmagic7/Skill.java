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
import static swordofmagic7.DataBase.playerData;
import static swordofmagic7.Function.*;
import static swordofmagic7.SoundList.*;

enum CastType {
    Legacy,
    Renewed,
    Hold,
    ;

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
        meta.setDisplayName(decoText(colored(Display)));
        List<String> Lore = new ArrayList<>(this.Lore);
        Lore.add(decoText("&3&lスキルステータス"));
        for (SkillParameter param : Parameter) {
            Lore.add(decoLore(param.Display) + param.Prefix + param.valueView() + param.Suffix);
        }
        Lore.add(decoLore("スキルタイプ") + SkillType.Display);
        if (SkillType.isActive()) {
            Lore.add(decoLore("消費マナ") + Mana);
            Lore.add(decoLore("詠唱時間") + (double) CastTime / 20 + "秒");
            Lore.add(decoLore("硬直時間") + (double) RigidTime / 20 + "秒");
            Lore.add(decoLore("再使用時間") + (double) CoolTime / 20 + "秒");
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
    private final HashMap<String, Integer> SkillCoolTime = new HashMap<>();
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

    void CastSkill(SkillData skillData) {
        if (CastReady) {
            if (CategoryCheck(EquipmentSlot.MainHand, skillData.ReqMainHand)) {
                if (!SkillCoolTime.containsKey(skillData.Id)) {
                    new BukkitRunnable() {
                        float p = 0;
                        @Override
                        public void run() {
                            p = (float) SkillProcess.SkillCastTime/skillData.CastTime;
                            if (p >= 1) this.cancel();
                            player.sendTitle(" ", colored("&e" + String.format("%.0f", p*100) + "%"), 0, 10, 0);
                        }
                    }.runTaskTimerAsynchronously(plugin, 0, 1);
                    switch (skillData.Id) {
                        case "Slash" -> SkillProcess.Slash(skillData);
                        case "Vertical" -> SkillProcess.Vertical(skillData);
                        case "Rain" -> SkillProcess.Rain(skillData);
                    }
                    setSkillCoolTime(skillData);
                } else {
                    player.sendMessage(colored("&e[" + skillData.Display + "]&aを&b使用可能&aまで&c&l" + SkillCoolTime.get(skillData.Id) / 20f + "秒&aです"));
                }
            }
        }
    }

    void setSkillCoolTime(SkillData skillData) {
        SkillCoolTime.put(skillData.Id, skillData.CoolTime);
        new BukkitRunnable() {
            @Override
            public void run() {
                SkillCoolTime.put(skillData.Id, SkillCoolTime.get(skillData.Id)-1);
                if (SkillCoolTime.get(skillData.Id) <= 0) {
                    this.cancel();
                    SkillCoolTime.remove(skillData.Id);
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 1);
    }

    private boolean CategoryCheck(EquipmentSlot slot, EquipmentCategory category) {
        List<EquipmentCategory> list = new ArrayList<>();
        list.add(category);
        return CategoryCheck(slot, list);
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
            player.sendMessage(colored("&aこの&eスキル&aの&b発動&aには&e" + slot.Display + "&aに&e[" + Display + "]&aを&e装備&aしてる&c必要&aがあります"));
            return false;
        }
    }
}

class SkillProcess {
    private final Plugin plugin;
    private final ParticleManager particleManager = new ParticleManager();
    private final Player player;
    private final PlayerData playerData;
    private final Skill Skill;
    private final RayTrace RayTrace = new RayTrace();

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

    private boolean isEnemy(Entity enemy) {
        if (enemy == player) {
            return false;
        } else if (enemy instanceof Player target) {
            if (target.isOnline()) {
                PlayerData targetData = playerData(target);
                return (playerData.PvPMode && targetData.PvPMode);
            } else return false;
        } else {
            return MobManager.getEnemyTable().containsKey(enemy.getUniqueId());
        }
    }

    private List<LivingEntity> FanShapedCollider(Location location, double radius, double angle, Predicate<LivingEntity> Predicate, boolean single) {
        List<LivingEntity> Targets = (List<LivingEntity>) location.getNearbyLivingEntities(radius, Predicate);
        if (Targets.size() == 0) return Targets;
        Targets = particleManager.FanShapedCollider(location, Targets, angle);
        if (single) Targets = Nearest(location, Targets);
        return Targets;
    }

    private List<LivingEntity> RectangleCollider(Location location, double length, double width, Predicate<LivingEntity> Predicate, boolean single) {
        List<LivingEntity> Targets = (List<LivingEntity>) location.getNearbyLivingEntities(length, Predicate);
        if (Targets.size() == 0) return Targets;
        Targets = particleManager.RectangleCollider(location, Targets, length, width);
        if (single) Targets = Nearest(location, Targets);
        return Targets;
    }

    private List<LivingEntity> Nearest(Location location, List<LivingEntity> Entities) {
        if (Entities.size() == 0) return new ArrayList<>();
        double distance = 64;
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

    void normalAttack() {
        if (playerData.Equipment.isEquip(EquipmentSlot.MainHand)) {
            if (0 >= normalAttackCoolTime) {
                EquipmentCategory category = playerData.Equipment.getEquip(EquipmentSlot.MainHand).EquipmentCategory;
                switch (category) {
                    case Blade -> {
                        List<LivingEntity> victims = RectangleCollider(player.getLocation(), 4, 1, Predicate(), true);
                        Damage.makeDamage(player, victims, DamageCause.ATK, 1, 1, 2);
                        normalAttackCoolTime = 15;
                    }
                    case Rapier -> {
                        List<LivingEntity> victims = RectangleCollider(player.getLocation(), 6, 0.75, Predicate(), true);
                        Damage.makeDamage(player, victims, DamageCause.ATK, 1, 1, 2);
                        normalAttackCoolTime = 15;
                    }
                    case Rod -> {
                        List<LivingEntity> victims = RectangleCollider(player.getLocation(), 15, 0.5, Predicate(), true);
                        particleManager.LineParticle(new ParticleData(Particle.CRIT_MAGIC), player.getEyeLocation(), 15, 0, 10);
                        Damage.makeDamage(player, victims, DamageCause.MAT, 1, 1, 2);
                        playSound(player, RodAttack);
                        normalAttackCoolTime = 15;
                    }
                    case ActGun -> {
                        List<LivingEntity> victims = RectangleCollider(player.getLocation(), 15, 0.5, Predicate(), true);
                        particleManager.LineParticle(new ParticleData(Particle.CRIT), player.getEyeLocation(), 15, 0, 10);
                        Damage.makeDamage(player, victims, DamageCause.MAT, 0.5, 1, 2);
                        playSound(player, GunAttack);
                        normalAttackCoolTime = 7;
                    }
                    default -> {
                        player.sendMessage(colored("&e[武器]&aが&e装備&aされていません"));
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
            player.sendMessage(colored("&e[武器]&aが&e装備&aされていません"));
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
                    particleManager.FanShapedParticle(particleCasting, player.getLocation(), radius, angle, 3);
                } else {
                    this.cancel();
                    particleManager.FanShapedParticle(particleActivate, player.getLocation(), radius, angle, 3);
                    List<LivingEntity> victims = FanShapedCollider(player.getLocation(), radius, angle, Predicate(), false);
                    Damage.makeDamage(player, victims, DamageCause.ATK, skillData.Parameter.get(0).Value / 100, 1, 2);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> Skill.setCastReady(true), skillData.RigidTime);
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
                    particleManager.RectangleParticle(particleCasting, player.getLocation(), length, width, 3);
                } else {
                    this.cancel();
                    particleManager.RectangleParticle(particleActivate, player.getLocation(), length, width, 3);
                    List<LivingEntity> victims = RectangleCollider(player.getLocation(), length, width, Predicate(), false);
                    Damage.makeDamage(player, victims, DamageCause.ATK, skillData.Parameter.get(0).Value / 100, 1, 2);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> Skill.setCastReady(true), skillData.RigidTime);
                }
                SkillCastTime += period;
            }
        }.runTaskTimer(plugin, 0, period);
    }

    void Rain(SkillData skillData) {
        Skill.setCastReady(false);
        final double radius = 5;
        final Location origin;
        final Location loc = RayTrace.rayLocation(player.getEyeLocation(),32, 0.1, false, Predicate()).HitPosition;
        loc.setPitch(90);
        origin = RayTrace.rayLocationBlock(loc, 32, false).HitPosition;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (SkillCastTime < skillData.CastTime) {
                    particleManager.CircleParticle(particleCasting, origin, radius, 30);
                } else {
                    this.cancel();
                    particleManager.CircleParticle(particleActivate, origin, radius, 30);
                    List<LivingEntity> victims = (List<LivingEntity>) origin.getNearbyLivingEntities(radius, Predicate());
                    Damage.makeDamage(player, victims, DamageCause.MAT, skillData.Parameter.get(0).Value/100, 1, 2);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> Skill.setCastReady(true), skillData.RigidTime);
                }
                SkillCastTime += period;
            }
        }.runTaskTimer(plugin, 0, period);
    }
}
