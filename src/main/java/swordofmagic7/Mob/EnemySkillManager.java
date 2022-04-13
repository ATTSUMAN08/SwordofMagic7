package swordofmagic7.Mob;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Mob.Skill.*;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;

import java.util.HashMap;
import java.util.Set;

import static swordofmagic7.PlayerList.getNearLivingEntity;
import static swordofmagic7.SomCore.random;

public class EnemySkillManager {
    public final EnemyData enemyData;
    public final ParticleData particleCasting = new ParticleData(Particle.REDSTONE, new Particle.DustOptions(Color.RED, 1));
    public final ParticleData particleActivate = new ParticleData(Particle.REDSTONE, new Particle.DustOptions(Color.PURPLE, 1));
    public final HashMap<String, Integer> CoolTime = new HashMap<>();
    public final HashMap<String, Integer> Available = new HashMap<>();
    public boolean SkillReady = true;
    public boolean setCancel = false;

    final BasicEnemySkills basicSkills = new BasicEnemySkills(this);
    public Symmore symmore;
    public Griffia griffia;
    public LeeLai leeLai;
    public Synosas synosas;
    public KingSlime kingSlime;
    public Exta exta;
    public Vanoset vanoset;

    public EnemySkillManager(EnemyData enemyData) {
        this.enemyData = enemyData;
        switch (enemyData.mobData.Id) {
            case "サイモア" -> symmore = new Symmore(this);
            case "グリフィア" -> griffia = new Griffia(this);
            case "リーライ" -> leeLai = new LeeLai(this);
            case "シノサス" -> synosas = new Synosas(this);
            case "キングスライム" -> kingSlime = new KingSlime(this);
            case "エクスタ" -> exta = new Exta(this);
            case "ヴァノセト" -> vanoset = new Vanoset(this);
        }
    }

    void tickSkillTrigger() {
        MultiThread.TaskRun(() -> {
            for (MobSkillData skill : enemyData.mobData.SkillList) {
                if (skill.maxHealth >= enemyData.Health / enemyData.MaxHealth) {
                    if (enemyData.Health / enemyData.MaxHealth >= skill.minHealth) {
                        if (skill.Available == -1 || Available.getOrDefault(skill.Skill, 0) < skill.Available) {
                            if (!CoolTime.containsKey(skill.Skill) && random.nextDouble() < skill.Percent) {
                                if (SkillReady) {
                                    mobSkillCast(skill);
                                }
                            }
                        }
                    }
                } else break;
            }
        }, "tickSkillTrigger");
    }

    public void mobSkillCast(MobSkillData mobSkillData) {
        if (mobSkillData.Available != -1) Available.put(mobSkillData.Skill, Available.getOrDefault(mobSkillData.Skill, 0)+1);
        switch (mobSkillData.Skill) {
            case "PullUpper" -> PullUpper(8, 90, 20);
            case "PileUpper" -> PullUpper(13, 160, 40);

            case "SkillLaser" -> basicSkills.SkillLaser();

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

            case "SlimeLaser" -> kingSlime.SlimeLaser();
            case "Crush" -> kingSlime.Crush(50);
            case "Adhesive" -> kingSlime.Adhesive(50);
            case "SummonFamiliar" -> kingSlime.SummonFamiliar();
            case "InsaneRush" -> kingSlime.InsaneRush(100);

            case "Launch" -> exta.Launch(50);
            case "Impact" -> exta.Impact(50);
            case "Starting" -> exta.Starting();
            case "Thought" -> exta.Thought(50);
            case "Acceleration" -> exta.Acceleration();

            case "Tornado" -> vanoset.Tornado(50);
            case "Squall" -> vanoset.Squall(50);
            case "WrongedFaith" -> vanoset.WrongedFaith(50);
            case "UnderTheSky" -> vanoset.UnderTheSky(70);
            case "Sacrifice" -> vanoset.Sacrifice();
            case "Vortex" -> vanoset.Vortex();
            case "Unconscious" -> vanoset.Unconscious();
            case "Decay" -> vanoset.Decay();
            case "CantLook" -> vanoset.CantLook();
            case "JustHistory" -> vanoset.JustHistory(30);


        }
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
        }, "MobSkillCoolTime");
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
            }, "PullUpper");
        }
    }
}
