package swordofmagic7.Skill.SkillClass;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.EffectManager;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Function;
import swordofmagic7.Item.RuneParameter;
import swordofmagic7.Map.MapData;
import swordofmagic7.Mob.EnemyData;
import swordofmagic7.Mob.EnemySkillManager;
import swordofmagic7.Mob.MobManager;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.RayTrace.Ray;
import swordofmagic7.RayTrace.RayTrace;
import swordofmagic7.Skill.BaseSkillClass;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;
import swordofmagic7.Sound.SoundList;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Function.playerHandLocation;
import static swordofmagic7.Function.sendMessage;
import static swordofmagic7.RayTrace.RayTrace.rayLocationEntity;
import static swordofmagic7.Skill.Skill.millis;
import static swordofmagic7.Skill.SkillProcess.particleCasting;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.Sound.SoundList.RodAttack;

public class Chronomancer extends BaseSkillClass {
    
    public Chronomancer(SkillProcess skillProcess) {
        super(skillProcess);
    }

    public void Slow(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            RuneParameter rune = playerData.Equipment.equippedRune("エンチャントスローのルーン");
            boolean bool = rune != null;
            double radius = skillData.ParameterValue(1);
            int time = skillData.ParameterValueInt(0) * 20;
            Location origin = player.getLocation().clone().add(player.getLocation().getDirection().multiply(radius));

            for (int i = 0; i < skillData.CastTime; i++) {
                ParticleManager.CircleParticle(particleCasting, origin, radius, 30);
                MultiThread.sleepMillis(millis);
            }
            if (bool) {
                int time2 = rune.AdditionParameterValueInt(0)*20;
                double percent = rune.AdditionParameterValue(1)/100;
                playerData.EffectManager.addEffect(EffectType.EnchantSlow, time2, new Object[]{percent,time});
                playSound(player, SoundList.Heal);
            } else {
                ParticleManager.CircleParticle(new ParticleData(Particle.SMOKE_LARGE, 0.2f, Function.VectorUp), origin, radius, 20);
                Set<LivingEntity> Targets = Function.NearLivingEntity(origin, radius, skillProcess.Predicate());
                for (LivingEntity target : Targets) {
                    EffectManager.addEffect(target, EffectType.Slow, time, player);
                }
                playSound(player, SoundList.DeBuff);
            }
            skillProcess.SkillRigid(skillData);
        }, "Slow");
    }

    public void Stop(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            int time = skillData.ParameterValueInt(0) * 20;
            ParticleData particleData = new ParticleData(Particle.CAMPFIRE_COSY_SMOKE, 0.05f);

            MultiThread.sleepTick(skillData.CastTime);

            Ray ray = rayLocationEntity(player.getEyeLocation(), 20, 0.5, skillProcess.Predicate());
            RuneParameter rune = playerData.Equipment.equippedRune("休憩時間のルーン");
            if (rune != null) {
                time = rune.AdditionParameterValueInt(0)*20;
                playerData.EffectManager.addEffect(EffectType.Stop, time, player.getLocation());
                playSound(player, SoundList.Heal);
            } else if (ray.isHitEntity()) {
                ParticleManager.LineParticle(particleData, playerHandLocation(player), ray.HitPosition, 0, 10);
                ParticleManager.RandomVectorParticle(particleData, ray.HitPosition, 30);
                EffectManager.addEffect(ray.HitEntity, EffectType.Stop, time, player, ray.HitEntity.getLocation());
                playSound(player, RodAttack);
            } else {
                player.sendMessage("§e対象§aがいません");
                playSound(player, SoundList.Nope);
                skill.resetSkillCoolTimeWaited(skillData);
            }
            skillProcess.SkillRigid(skillData);
        }, "Stop");
    }

    public void Path(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            double multiply = 1-(skillData.ParameterValue(0)/100);
            int time = skillData.ParameterValueInt(1) * 20;
            ParticleData particleData = new ParticleData(Particle.ENCHANTMENT_TABLE);
            Set<Player> Targets = new HashSet<>();
            skill.setCastReady(false);

            MultiThread.sleepTick(skillData.CastTime);

            Targets.add(player);
            RuneParameter rune = playerData.Equipment.equippedRune("時間集中のルーン");
            if (rune != null) {
                multiply = 1-(rune.AdditionParameterValue(0)/100);
            } else if (playerData.Party != null) Targets.addAll(playerData.Party.Members);
            for (Player target : Targets) {
                if (skillProcess.isAllies(target) || target == player) {
                    PlayerData targetData = playerData(target);
                    if (!targetData.EffectManager.hasEffect(EffectType.TimeTravelSequelae)) {
                        ParticleManager.CylinderParticle(particleData, target.getLocation(), 1, 2, 3, 3);
                        targetData.EffectManager.addEffect(EffectType.TimeTravelSequelae, time);
                        EffectManager.addEffectMessage(player, target, skillData.Display, "§e");
                        for (Map.Entry<String, Integer> data : targetData.Skill.SkillCoolTime.entrySet()) {
                            if (!data.getKey().equals(skillData.Id)) data.setValue((int) Math.floor(data.getValue() * multiply));
                        }
                    }
                }
            }
            playSound(player, SoundList.Heal);
            skillProcess.SkillRigid(skillData);
        }, "Path");
    }

    public void TimeForward(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            double radius = skillData.ParameterValue(1);
            int time = skillData.ParameterValueInt(0)*20;
            Location origin = RayTrace.rayLocationBlock(player.getEyeLocation(), 20, true).HitPosition;

            for (int i = 0; i < skillData.CastTime; i++) {
                ParticleManager.CircleParticle(particleCasting, origin, radius, 30);
                MultiThread.sleepMillis(millis);
            }

            ParticleManager.CircleParticle(new ParticleData(Particle.SMOKE_LARGE, 0.2f, Function.VectorUp), origin, radius, 20);
            Set<LivingEntity> Targets = new HashSet<>(Function.NearLivingEntity(origin, radius, skillProcess.Predicate()));
            Set<LivingEntity> forwards = new HashSet<>();
            RuneParameter rune = playerData.Equipment.equippedRune("タイムアウトのルーン");
            int maxCount = rune != null ? rune.AdditionParameterValueInt(0) * rune.AdditionParameterValueInt(1) : 3;
            for (LivingEntity target : Targets) {
                int count = 0;
                if (target instanceof Player player) {
                    PlayerData playerData = playerData(player);
                    for (Map.Entry<String, Integer> data : playerData.Skill.SkillCoolTime.entrySet()) {
                        if (data.getValue() > 0) {
                            playerData.Skill.SkillCoolTime.put(data.getKey(), data.getValue()+time);
                            sendMessage(player, "§c" + playerData.Nick + "§aの§b" + data.getKey() + "§aを§c延長§aしました");
                            forwards.add(target);
                            count++;
                            if (rune == null || count >= 3) break;
                        }
                    }
                } else if (MobManager.isEnemy(target)) {
                    EnemyData enemyData = MobManager.EnemyTable(target.getUniqueId());
                    if (enemyData != null && !enemyData.mobData.enemyType.isRaidBoss()) {
                        EnemySkillManager manager = enemyData.skillManager;
                        if (manager != null) {
                            for (Map.Entry<String, Integer> data : manager.CoolTime.entrySet()) {
                                int cooltime = manager.getCoolTime(data.getKey());
                                if (cooltime > 0) {
                                    manager.CoolTime.put(data.getKey(), cooltime + time);
                                    forwards.add(target);
                                    sendMessage(player, "§c" + manager.enemyData.mobData.Display + "§aの§b" + data.getKey() + "§aを§c延長§aしました");
                                    count++;
                                    if (rune == null || count >= 3) break;
                                }
                            }
                        }
                    }
                }
                if (forwards.size() >= maxCount) break;
            }
            if (forwards.size() > 0) {
                playSound(player, SoundList.DeBuff);
                for (LivingEntity entity : forwards) {
                    EffectManager.addEffectMessage(player, entity, skillData.Display, "§c");
                    EffectManager.addEffect(entity, EffectType.TimeForward, skillData.CoolTime, player);
                }
            } else {
                player.sendMessage("§e対象§aがいません");
                playSound(player, SoundList.Nope);
                skill.resetSkillCoolTimeWaited(skillData);
            }
            skillProcess.SkillRigid(skillData);
        }, "TimeForward");
    }

    public double BackMaskingHealth = 0;
    public double BackMaskingMana = 0;
    public Location BackMaskingLocation = null;
    public MapData BackMaskingMapData = null;

    public void BackMasking(SkillData skillData) {
        MultiThread.TaskRun(() -> {
            skill.setCastReady(false);
            if (BackMaskingHealth > 0) {
                ParticleData particleData = new ParticleData(Particle.FIREWORKS_SPARK, 0.1f, Function.VectorUp);
                for (int i = 0; i < skillData.CastTime; i++) {
                    ParticleManager.CircleParticle(particleData, player.getLocation().clone().add(0, skill.SkillCastProgress*2, 0), 1, 10);
                    MultiThread.sleepMillis(millis);
                }

                MultiThread.TaskRunSynchronized(() -> {
                    if (player != null) {
                        playerData.setHealth(BackMaskingHealth);
                        playerData.setMana(BackMaskingMana);
                        player.teleportAsync(BackMaskingLocation);
                        BackMaskingMapData.enter(player);
                        player.sendMessage("§a情報を巻き戻しました");
                        playSound(player, SoundList.Warp);
                        MultiThread.TaskRunSynchronizedLater(this::BackMaskingReset, 1);
                    }
                });
            } else {
                BackMaskingSet();
                player.sendMessage("§a現在の情報を記録しました");
                playSound(player, SoundList.Tick);
                skillProcess.SkillCastTime = skillData.CastTime;
                skill.resetSkillCoolTimeWaited(skillData);
            }
            skillProcess.SkillRigid(skillData);
        }, "TimeForward");
    }

    public void BackMaskingReset() {
        BackMaskingHealth = 0;
        BackMaskingMana = 0;
        BackMaskingLocation = null;
        BackMaskingMapData = null;
    }
    public void BackMaskingSet() {
        BackMaskingHealth = playerData.Status.Health;
        BackMaskingMana = playerData.Status.Mana;
        BackMaskingLocation = player.getLocation().clone();
        BackMaskingMapData = playerData.Map.clone();
    }

}
