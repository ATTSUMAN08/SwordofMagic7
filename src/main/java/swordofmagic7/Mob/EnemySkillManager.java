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
import swordofmagic7.Mob.Skill.Griffia;
import swordofmagic7.Mob.Skill.Symmore;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.PlayerList;
import swordofmagic7.Sound.SoundList;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static swordofmagic7.Function.Log;
import static swordofmagic7.PlayerList.getNearLivingEntity;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.System.plugin;

public class EnemySkillManager {
    public final EnemyData enemyData;
    public final ParticleData particleCasting = new ParticleData(Particle.REDSTONE, new Particle.DustOptions(Color.RED, 1));
    public final ParticleData particleActivate = new ParticleData(Particle.REDSTONE, new Particle.DustOptions(Color.PURPLE, 1));
    public final HashMap<String, Boolean> CoolTime = new HashMap<>();
    public final HashMap<String, Integer> Available = new HashMap<>();
    public boolean SkillReady = true;
    public final Random random = new Random();
    public boolean setCancel = false;

    public EnemySkillManager(EnemyData enemyData) {
        this.enemyData = enemyData;
    }

    void tickSkillTrigger() {
        for (MobSkillData skill : enemyData.mobData.SkillList) {
            if (skill.Health >= enemyData.Health / enemyData.MaxHealth) {
                if (skill.Available == -1 || Available.getOrDefault(skill.Skill, 0) < skill.Available) {
                    if (!CoolTime.containsKey(skill.Skill) && random.nextDouble() < skill.Percent){
                        if (skill.Interrupt && !SkillReady) {
                            setCancel = true;
                            mobSkillCast(skill);
                        } else if (SkillReady) {
                            mobSkillCast(skill);
                        }
                    }
                }
            }
            else break;
        }
    }

    Symmore symmore = new Symmore(this);
    Griffia griffia = new Griffia(this);

    public void mobSkillCast(MobSkillData mobSkillData) {
        switch (mobSkillData.Skill) {
            case "PullUpper" -> PullUpper(8, 90, 20);
            case "PileUpper" -> PullUpper(13, 160, 40);
            case "PileOut" -> symmore.PileOut(30);
            case "Howl" -> symmore.Howl(80);
            case "MagicExplosion" -> symmore.MagicExplosion(300);
            case "SingleFlameCircle" -> griffia.SingleFlameCircle(20);
            case "AreaFlameCircle" -> griffia.AreaFlameCircle(20);
            case "FlamePile" -> griffia.FlamePile(100);
            case "Call" -> griffia.Call(100);
            case "Loyalty" -> griffia.Loyalty(100);
            case "Fluctuation" -> griffia.Fluctuation(250);
            case "FixedStar" -> griffia.FixedStar(150);
        }
        if (mobSkillData.Available != -1) Available.put(mobSkillData.Skill, Available.getOrDefault(mobSkillData.Skill, 0)+1);
        CoolTime.put(mobSkillData.Skill, true);
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            CoolTime.remove(mobSkillData.Skill);
        }, mobSkillData.CoolTime);
    }

    public void SkillCancel() {
        setCancel = false;
        SkillReady = true;
    }

    public void CastSkill(boolean bool) {
        enemyData.entity.setAI(!bool);
        SkillReady = !bool;
    }

    public void CastSkillIgnoreAI(boolean bool) {
        SkillReady = !bool;
    }

    public final int period = 5;

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
                        Set<LivingEntity> Targets = getNearLivingEntity(enemyData.entity.getLocation(), radius);
                        Set<LivingEntity> victims = ParticleManager.FanShapedCollider(origin, Targets, angle);
                        Damage.makeDamage(enemyData.entity, victims, DamageCause.ATK, "PullUpper", 2, 1, 2);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> CastSkill(false), 10);
                    }
                    i += period;
                }
            }.runTaskTimer(plugin, 0, period);
        }
    }
}
