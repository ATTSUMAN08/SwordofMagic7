package swordofmagic7.Mob;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.PlayerList;
import swordofmagic7.Sound.SoundList;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static swordofmagic7.PlayerList.getNearLivingEntity;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.System.plugin;

public class EnemySkillManager {
    private final EnemyData enemyData;
    private final ParticleData particleCasting = new ParticleData(Particle.REDSTONE, new Particle.DustOptions(Color.RED, 1));
    private final ParticleData particleActivate = new ParticleData(Particle.REDSTONE, new Particle.DustOptions(Color.PURPLE, 1));
    public final HashMap<String, Boolean> CoolTime = new HashMap<>();
    public final HashMap<String, Integer> Available = new HashMap<>();
    boolean SkillReady = true;
    private final Random random = new Random();
    public boolean setCancel = false;

    public EnemySkillManager(EnemyData enemyData) {
        this.enemyData = enemyData;
    }

    void tickSkillTrigger() {
        if (SkillReady) {
            for (MobSkillData skill : enemyData.mobData.SkillList) {
                if (SkillReady && skill.Interrupt) setCancel = true;
                if (SkillReady) mobSkillCast(skill);
                else break;
            }
        }
    }

    void mobSkillCast(MobSkillData mobSkillData) {
        if ((mobSkillData.Available == -1 || Available.getOrDefault(mobSkillData.Skill, 0) < mobSkillData.Available)
        && (!CoolTime.containsKey(mobSkillData.Skill) && random.nextDouble() < mobSkillData.Percent)
        && (mobSkillData.Health >= enemyData.Health / enemyData.MaxHealth)
        ) {
            switch (mobSkillData.Skill) {
                case "PullUpper" -> PullUpper(8, 90, 20);
                case "PileUpper" -> PullUpper(13, 160, 40);
                case "PileOut" -> PileOut(30);
                case "Howl" -> Howl(80);
                case "MagicExplosion" -> MagicExplosion(200);
            }
            if (mobSkillData.Available != -1) Available.put(mobSkillData.Skill, Available.getOrDefault(mobSkillData.Skill, 0)+1);
            CoolTime.put(mobSkillData.Skill, true);
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                CoolTime.remove(mobSkillData.Skill);
            }, mobSkillData.CoolTime);
        }
    }

    void SkillCancel() {
        setCancel = false;
        CastSkill(false);
    }

    void CastSkill(boolean bool) {
        enemyData.entity.setAI(!bool);
        SkillReady = !bool;
    }

    private final int period = 5;

    void PullUpper(double radius, double angle, int CastTime) {
        if (enemyData.entity.getLocation().distance(enemyData.target.getLocation()) <= radius) {
            Location origin = enemyData.entity.getLocation().clone();
            CastSkill(true);
            new BukkitRunnable() {
                int i = 0;

                @Override
                public void run() {
                    if (enemyData.isDead || setCancel) {
                        this.cancel();
                        SkillCancel();
                    } else if (i < CastTime) {
                        ParticleManager.FanShapedParticle(particleCasting, origin, radius, angle, 3);
                    } else {
                        this.cancel();
                        ParticleManager.FanShapedParticle(particleActivate, origin, radius, angle, 3);
                        List<LivingEntity> Targets = getNearLivingEntity(enemyData.entity.getLocation(), radius);
                        List<LivingEntity> victims = ParticleManager.FanShapedCollider(origin, Targets, angle);
                        Damage.makeDamage(enemyData.entity, victims, DamageCause.ATK, "PullUpper", 2, 1, 2);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> CastSkill(false), 10);
                    }
                    i += period;
                }
            }.runTaskTimer(plugin, 0, period);
        }
    }

    void PileOut(int CastTime) {
        CastSkill(true);
        new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                if (enemyData.isDead || setCancel) {
                    this.cancel();
                    SkillCancel();
                } else if (i < CastTime) {
                    ParticleManager.CircleParticle(particleCasting, enemyData.target.getLocation(), 1, 72);
                } else {
                    this.cancel();
                    ParticleManager.CircleParticle(particleActivate, enemyData.target.getLocation(), 1, 72);
                    Damage.makeDamage(enemyData.entity, enemyData.target, DamageCause.ATK, "PileOut", 3, 1);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> CastSkill(false), 10);
                }
                i += period;
            }
        }.runTaskTimer(plugin, 0, period);
    }

    void Howl(int CastTime) {
        CastSkill(true);
        new BukkitRunnable() {
            int i = 0;
            final List<LivingEntity> list = getNearLivingEntity(enemyData.entity.getLocation(), 32);
            @Override
            public void run() {
                if (enemyData.isDead || setCancel) {
                    this.cancel();
                    SkillCancel();
                } else if (i < list.size()*2) {
                    i++;
                    playSound(enemyData.entity.getLocation(), SoundList.Howl, 3, 1);
                    new BukkitRunnable() {
                        int i = 0;
                        final LivingEntity target = list.get(random.nextInt(list.size()));
                        @Override
                        public void run() {
                            if (enemyData.isDead || setCancel) {
                                this.cancel();
                                SkillCancel();
                            } else if (i < CastTime) {
                                ParticleManager.CircleParticle(particleCasting, target.getLocation(), 3, 36);
                            } else {
                                this.cancel();
                                ParticleManager.CircleParticle(particleActivate, target.getLocation(), 3, 36);
                                List<LivingEntity> victims = getNearLivingEntity(target.getLocation(),3);
                                Damage.makeDamage(enemyData.entity, victims, DamageCause.ATK, "Howl", 3, 1, 2);
                            }
                            i += period;
                        }
                    }.runTaskTimer(plugin, 0, period);
                } else {
                    this.cancel();
                    Bukkit.getScheduler().runTaskLater(plugin, () -> CastSkill(false), 40);
                }
            }
        }.runTaskTimer(plugin, 0, 50);
    }

    void MagicExplosion(int CastTime) {
        CastSkill(true);
        enemyData.effectManager.addEffect(EffectType.Invincible, 250);
        for (Player player : PlayerList.getNear(enemyData.entity.getLocation(), 32)) {
            player.sendMessage("§c強力な攻撃§aの準備をしています！");
            player.sendMessage("§c攻撃§aして§c阻止§aしてください！");
            playSound(player, SoundList.DungeonTrigger);
        }
        final List<LivingEntity> list = PlayerList.getNearLivingEntity(enemyData.entity.getLocation(), 32);
        ParticleData particleData = new ParticleData(Particle.SPELL_WITCH, 0.5f);
        new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                if (enemyData.isDead || setCancel) {
                    this.cancel();
                    SkillCancel();
                } else if (i < CastTime && enemyData.HitCount < list.size()*20) {
                    ParticleManager.RandomVectorParticle(particleData, enemyData.entity.getLocation(), 100);
                } else if (enemyData.HitCount > list.size()*20) {
                    this.cancel();
                    Bukkit.getScheduler().runTaskLater(plugin, () -> CastSkill(false), 10);
                } else {
                    this.cancel();
                    ParticleManager.RandomVectorParticle(particleData, enemyData.entity.getLocation(), 100);
                    ParticleManager.RandomVectorParticle(new ParticleData(Particle.EXPLOSION_NORMAL), enemyData.entity.getLocation(), 10);
                    Damage.makeDamage(enemyData.entity, list, DamageCause.MAT, "MagicExplosion", 10, 1, 1);
                    playSound(enemyData.entity.getLocation(), SoundList.Explosion, 10, 1);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> CastSkill(false), 100);
                }
                i += period;
            }
        }.runTaskTimer(plugin, 0, period);
    }
}
