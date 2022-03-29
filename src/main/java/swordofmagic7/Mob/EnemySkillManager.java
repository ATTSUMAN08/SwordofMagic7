package swordofmagic7.Mob;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Mob.Skill.Griffia;
import swordofmagic7.Mob.Skill.LeeLai;
import swordofmagic7.Mob.Skill.Symmore;
import swordofmagic7.Mob.Skill.Synosas;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;

import java.util.HashMap;
import java.util.Set;

import static swordofmagic7.PlayerList.getNearLivingEntity;
import static swordofmagic7.System.random;

public class EnemySkillManager {
    public final EnemyData enemyData;
    public final ParticleData particleCasting = new ParticleData(Particle.REDSTONE, new Particle.DustOptions(Color.RED, 1));
    public final ParticleData particleActivate = new ParticleData(Particle.REDSTONE, new Particle.DustOptions(Color.PURPLE, 1));
    public final HashMap<String, Integer> CoolTime = new HashMap<>();
    public final HashMap<String, Integer> Available = new HashMap<>();
    public boolean SkillReady = true;
    public boolean setCancel = false;

    public EnemySkillManager(EnemyData enemyData) {
        this.enemyData = enemyData;
    }

    void tickSkillTrigger() {
        for (MobSkillData skill : enemyData.mobData.SkillList) {
            if (skill.maxHealth >= enemyData.Health / enemyData.MaxHealth) {
                if (skill.Available == -1 || Available.getOrDefault(skill.Skill, 0) < skill.Available) {
                    if (!CoolTime.containsKey(skill.Skill) && random.nextDouble() < skill.Percent){
                        if (SkillReady) {
                            mobSkillCast(skill);
                        }
                        /*
                        if (skill.Interrupt && !SkillReady) {
                            setCancel = true;
                            mobSkillCast(skill);
                        } else if (SkillReady) {
                            mobSkillCast(skill);
                        }
                         */
                    }
                }
            }
            else break;
        }
    }

    Symmore symmore = new Symmore(this);
    Griffia griffia = new Griffia(this);
    LeeLai leeLai = new LeeLai(this);
    Synosas synosas = new Synosas(this);

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

            case "Glitter" -> leeLai.Glitter(60);
            case "Flash" -> leeLai.Flash(60);
            case "Reflection" -> leeLai.Reflection();
            case "Glory" -> leeLai.Glory(60);
            case "Seiko" -> leeLai.Seiko();

            case "SynosasRangeAttack" -> synosas.RangeAttack(5);
            case "SynosasVerticalAttack" -> synosas.VerticalAttack(5);
            case "Forced" -> synosas.Forced(5);
            case "Fear" -> synosas.Fear(5);
            case "Despair" -> synosas.Despair(60);
            case "SynosasEffect" -> synosas.Effect(100);
            case "Quiet" -> synosas.Quiet(20);
            case "Distrust" -> synosas.Distrust(0);
        }
        if (mobSkillData.Available != -1) Available.put(mobSkillData.Skill, Available.getOrDefault(mobSkillData.Skill, 0)+1);
        MultiThread.TaskRun(() -> {
            if (!CoolTime.containsKey(mobSkillData.Skill)) {
                CoolTime.put(mobSkillData.Skill, mobSkillData.CoolTime);
                while (getCoolTime(mobSkillData.Skill) > 0) {
                    CoolTime.put(mobSkillData.Skill, getCoolTime(mobSkillData.Skill) - period);
                    MultiThread.sleepTick(period);
                }
                CoolTime.remove(mobSkillData.Skill);
            } else {
                CoolTime.put(mobSkillData.Skill, mobSkillData.CoolTime);
            }
        }, "MobSkillCoolTime: " + enemyData.mobData.Display);
    }

    public int getCoolTime(String key) {
        return CoolTime.getOrDefault(key, 0);
    }

    public void CastSkill(boolean bool) {
        //setCancel = bool;
        enemyData.entity.setAI(!bool);
        SkillReady = !bool;
    }

    public void CastSkillIgnoreAI(boolean bool) {
        //setCancel = bool;
        SkillReady = !bool;
    }

    public final int period = 5;

    void PullUpper(double radius, double angle, int CastTime) {
        if (enemyData.entity.getLocation().distance(enemyData.target.getLocation()) <= radius) {
            Location origin = enemyData.entity.getLocation().clone();
            CastSkill(true);
            MultiThread.TaskRun(() -> {
                int i = 0;
                while(enemyData.isAlive() || !setCancel) {
                    if (i < CastTime) {
                        ParticleManager.FanShapedParticle(particleCasting, origin, radius, angle, 3);
                    } else {
                        ParticleManager.FanShapedParticle(particleActivate, origin, radius, angle, 3);
                        Set<LivingEntity> Targets = getNearLivingEntity(enemyData.entity.getLocation(), radius);
                        Set<LivingEntity> victims = ParticleManager.FanShapedCollider(origin, Targets, angle);
                        Damage.makeDamage(enemyData.entity, victims, DamageCause.ATK, "PullUpper", 2, 1, 2);
                        break;
                    }
                    i += period;
                    MultiThread.sleepTick(period);
                }
                MultiThread.sleepTick(10);
                CastSkill(false);
            }, "PullUpper: " + enemyData.mobData.Display);
        }
    }
}
