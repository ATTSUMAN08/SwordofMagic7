package swordofmagic7.Skill.SkillClass;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Function;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.Skill.BaseSkillClass;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;
import swordofmagic7.Sound.SoundList;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static swordofmagic7.Particle.ParticleManager.ShapedParticle;
import static swordofmagic7.Skill.Skill.millis;
import static swordofmagic7.Skill.SkillProcess.*;
import static swordofmagic7.Sound.CustomSound.playSound;

public class Doppelsoeldner extends BaseSkillClass {

    public Doppelsoeldner(SkillProcess skillProcess) {
        super(skillProcess);
    }

    public void DeedsOfValor(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);

            MultiThread.sleepTick(skillData.CastTime);

            playerData.EffectManager.addEffect(EffectType.DeedsOfValor, (int) skillData.Parameter.get(0).Value * 20);
            ParticleManager.CylinderParticle(new ParticleData(Particle.WITCH), player.getLocation(), 1, 2, 3, 3);
            playSound(player, SoundList.HOWL);
            skillProcess.SkillRigid(skillData);
        }, "DeedsOfValor");
    }

    public void Cyclone(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double value = skillData.ParameterValue(0)/100;
            final double radius = skillData.ParameterValue(3);
            final int hitRate = (int) Math.round(skillData.ParameterValue(2)*20);
            final int time = (int) Math.round(skillData.ParameterValue(1)*20);
            ParticleData particleData = new ParticleData(Particle.CRIT, 0.5f, Function.VectorUp);
            particleData.randomOffset = true;
            particleData.randomOffsetMultiply = (float) (radius/2);

            for (int i = 0; i < skillData.CastTime; i++) {
                ParticleManager.CircleParticle(particleCasting, player.getLocation(), radius, 15);
                MultiThread.sleepMillis(millis);
            }

            if (playerData.Equipment.isEquipRune("竜巻生成のルーン")) {
                skillProcess.SkillRigid(skillData);
                Location origin = player.getLocation().clone();
                for (int i = 0; i < time; i+=hitRate) {
                    ParticleManager.CircleParticle(particleData, origin, radius/2, 15);
                    Set<LivingEntity> victims = new HashSet<>(Function.NearLivingEntity(origin, radius, skillProcess.Predicate()));
                    Damage.makeDamage(player, victims, DamageCause.ATK, skillData.Id,  value,1, 2);
                    playSound(player, SoundList.ATTACK_WEAK);
                    MultiThread.sleepTick(hitRate);
                }
            } else {
                for (int i = 0; i < time; i+=hitRate) {
                    ParticleManager.CircleParticle(particleData, player.getLocation(), radius/2, 15);
                    Set<LivingEntity> victims = new HashSet<>(Function.NearLivingEntity(player.getLocation(), radius, skillProcess.Predicate()));
                    Damage.makeDamage(player, victims, DamageCause.ATK, skillData.Id,  value,1, 2);
                    playSound(player, SoundList.ATTACK_WEAK);
                    MultiThread.sleepTick(hitRate);
                }
                skillProcess.SkillRigid(skillData);
            }
        }, "Cyclone");
    }

    public void ComboSkill(SkillData skillDataBase, double radius, double angle, int count, EffectType reqEffect, EffectType addEffect) {
        MultiThread.TaskRun(() -> {
            SkillData skillData = skillDataBase;
            skill.setCastReady(false);
            if (reqEffect == null || playerData.EffectManager.hasEffect(reqEffect)) {
                for (int i = 0; i < skillData.CastTime; i++) {
                    ParticleManager.FanShapedParticle(particleCasting, player.getLocation(), radius, angle, 3);
                    MultiThread.sleepMillis(millis);
                }
                ParticleManager.FanShapedParticle(particleActivate, player.getLocation(), radius, angle, 3);
                Set<LivingEntity> victims = FanShapedCollider(player.getLocation(), radius, angle, skillProcess.Predicate(), false);
                Damage.makeDamage(player, victims, DamageCause.ATK, skillData.Id, skillData.Parameter.get(0).Value / 100, count, 2);
                ShapedParticle(new ParticleData(Particle.SWEEP_ATTACK), player.getLocation(), radius, angle, angle / 2, 1, true);
                if (playerData.Equipment.isEquipRune("平常運転のルーン") && Objects.equals(skillData.Id, "Zornhau")) {
                    SkillData skillDataZucken = DataBase.getSkillData("Zucken");
                    SkillData skillDataRedel = DataBase.getSkillData("Redel");

                    playerData.Skill.useStack(skillDataZucken);
                    playerData.Skill.setSkillCoolTime(skillDataZucken);
                    playerData.Skill.useStack(skillDataRedel);
                    playerData.Skill.setSkillCoolTime(skillDataRedel);

                    ComboSkill(skillDataZucken, 7, 120, 1, null, null);
                    MultiThread.sleepTick(15);
                    ComboSkill(skillDataRedel, 8, 160, 1, null, null);
                } else if (addEffect != null) playerData.EffectManager.addEffect(addEffect, 40);
            } else {
                skill.resetSkillCoolTimeWaited(skillData);
                player.sendMessage("§e[" + reqEffect.Display + "]§aが§c必要§aです");
                playSound(player, SoundList.NOPE);
            }
            skillProcess.SkillRigid(skillData);
        }, skillDataBase.Id);
    }
}
