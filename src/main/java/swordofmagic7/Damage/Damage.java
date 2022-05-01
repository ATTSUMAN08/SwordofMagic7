package swordofmagic7.Damage;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.VisibilityManager;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import swordofmagic7.Attribute.AttributeType;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Data.Type.DamageLogType;
import swordofmagic7.Effect.EffectData;
import swordofmagic7.Effect.EffectManager;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Function;
import swordofmagic7.Item.RuneParameter;
import swordofmagic7.Mob.EnemyData;
import swordofmagic7.Mob.MobManager;
import swordofmagic7.Mob.MobSkillData;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Pet.PetManager;
import swordofmagic7.Pet.PetParameter;
import swordofmagic7.Sound.SoundList;

import java.util.HashSet;
import java.util.Set;

import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Function.sendMessage;
import static swordofmagic7.SomCore.createHologram;
import static swordofmagic7.SomCore.random;

public final class Damage {

    public static int OutrageResetTime;
    public static int FrenzyResetTime;
    public static double PvPDecay = 500;
    public static double PvPHealDecay = 10;

    public static void makeHeal(Player healer, Player victim, String source, double healMultiply) {
        PlayerData healerData = playerData(healer);
        PlayerData victimData = playerData(victim);
        if (victimData.EffectManager.hasEffect(EffectType.RecoveryInhibition)) {
            sendMessage(victimData.player, "§c[回復阻害]§aにより§e回復効果§aが§c無効化§aされました");
            return;
        }
        double heal = healerData.Status.HLP * healMultiply;
        if (victimData.PvPMode) heal /= PvPHealDecay;
        if (source.equals("MassHeal")) {
            RuneParameter rune = healerData.Equipment.equippedRune("過剰回復のルーン");
            if (rune != null) {
                int time  = rune.AdditionParameterValueInt(0)*20;
                double value = rune.AdditionParameterValue(1)/100;
                double shield = Math.min(heal-victimData.Status.MaxHealth, victimData.Status.MaxHealth*value);
                victimData.changeShield(shield, time);
            }
        }
        victimData.changeHealth(heal);
        String Text = "§b≫§e" + String.format("%.1f", heal);
        String M = " §f[M:" + String.format("%.0f", healMultiply*100) + "%]";
        String SPI = " §b[SPI:" + healerData.Attribute.getAttribute(AttributeType.SPI) + "]";
        String HLP = " §e[HPL:" + String.format("%.0f", healerData.Status.HLP) + "]";
        String HP = " §c[HP:" + String.format("%.0f", victimData.Status.Health) + "/" + String.format("%.0f", victimData.Status.MaxHealth) + "]";
        if (healerData.DamageLog.isDamageOnly()) {
            String healText = Text;
            if (healerData.DamageLog.isDetail()) {
                healText += M + SPI + HLP + HP;
            }
            healer.sendMessage(healText + "§e -> " + victimData.Nick);
        }
        if (healer != victim && victimData.DamageLog.isDamageOnly()) {
            String healText = Text;
            if (healerData.DamageLog.isDetail()) {
                healText += M + SPI + HLP;
            }
            victim.sendMessage(healText + "§e <- " + healerData.Nick);
        }
    }

    public static void makeDamage(LivingEntity attacker, Set<LivingEntity> victims, DamageCause damageCause, String damageSource, double damageMultiply, int count, int wait) {
        makeDamage(attacker, victims, damageCause, damageSource, damageMultiply, count, 0, false, wait);
    }

    public static void makeDamage(LivingEntity attacker, Set<LivingEntity> victims, DamageCause damageCause, String damageSource, double damageMultiply, int count, double perforate, boolean invariably, int wait) {
        MultiThread.TaskRun(() -> {
            for (LivingEntity victim : victims) {
                makeDamage(attacker, victim, damageCause, damageSource, damageMultiply, count, perforate, invariably, false);
                MultiThread.sleepTick(wait);
            }
        }, "MakeDamage");
    }

    public static void makeDamage(LivingEntity attacker, LivingEntity victim, DamageCause damageCause, String damageSource, double damageMultiply, int count) {
        makeDamage(attacker, victim, damageCause, damageSource, damageMultiply, count, 0, false, false);
    }

    public static void makeDamage(LivingEntity attacker, LivingEntity victim, DamageCause damageCause, String damageSource, double damageMultiply, int count, double perforate) {
        makeDamage(attacker, victim, damageCause, damageSource, damageMultiply, count, perforate, false, false);
    }

    public static void makeDamage(LivingEntity attacker, LivingEntity victim, DamageCause damageCause, String damageSource, double damageMultiply, int count, double perforate, boolean invariably) {
        makeDamage(attacker, victim, damageCause, damageSource, damageMultiply, count, perforate, invariably, false);
    }

    public static void makeDamage(LivingEntity attacker, LivingEntity victim, DamageCause damageCause, String damageSource, double damageMultiply, int count, double perforate, boolean invariably, boolean ignoreInvincible) {
        if (victim.isDead()) return;

        double ATK;
        double DEF;
        double ACC;
        double EVA;
        double CriticalRate;
        double CriticalResist;
        double baseDamage;
        double hitRate;
        double criRate;
        double damage = 0;

        double Attack;
        double Defence;
        double victimMaxHealth = 0;
        double victimHealth = 0;
        double Multiply = 1;
        double CriticalMultiply = 1.2;
        double Resistance = 1;
        EffectManager attackerEffectManager;
        EffectManager victimEffectManager;
        int attackerLevel;
        int victimLevel;

        Set<Player> HoloView = new HashSet<>();
        if (attacker instanceof Player player) {
            HoloView.add(player);
            if (!Function.isAlive(player)) return;
            PlayerData playerData = playerData(player);
            ATK = playerData.Status.ATK;
            ACC = playerData.Status.ACC;
            CriticalRate = playerData.Status.CriticalRate;
            CriticalMultiply = playerData.Status.CriticalMultiply;
            Multiply = playerData.Status.DamageCauseMultiply.getOrDefault(damageCause, Resistance);
            attackerLevel = playerData.Level;
            attackerEffectManager = playerData.EffectManager;
            attackerEffectManager.removeEffect(EffectType.Covert);
            if (playerData.Skill.hasSkill("Outrage")) {
                attackerEffectManager.addEffect(EffectType.Outrage, OutrageResetTime);
            }
            if (attackerEffectManager.hasEffect(EffectType.JollyRogerCombo)) {
                PlayerData JollyRogerPlayerData = (PlayerData) attackerEffectManager.getData(EffectType.JollyRogerCombo).getObject(0);
                JollyRogerPlayerData.Skill.corsair.JollyRogerCombo++;
            }
            if (attackerEffectManager.hasEffect(EffectType.Nachash)) {
                playerData.changeHealth(attackerEffectManager.getData(EffectType.Nachash).getDouble(0));
            }
            playerData.setTargetEntity(victim);
        } else if (MobManager.isEnemy(attacker)) {
            EnemyData enemyData = MobManager.EnemyTable(attacker.getUniqueId());
            if (enemyData == null) return;
            ATK = enemyData.ATK;
            ACC = enemyData.ACC;
            CriticalRate = enemyData.CriticalRate;
            Multiply = enemyData.DamageCauseMultiply.getOrDefault(damageCause, Multiply);
            attackerLevel = enemyData.Level;
            attackerEffectManager = enemyData.effectManager;
        } else if (PetManager.isPet(attacker)) {
            PetParameter petParameter = PetManager.PetParameter(attacker);
            ATK = petParameter.ATK;
            ACC = petParameter.ACC;
            CriticalRate = petParameter.CriticalRate;
            Multiply = petParameter.DamageCauseMultiply.getOrDefault(damageCause, Multiply);
            attackerLevel = petParameter.Level;
            petParameter.DecreaseStamina(1, 1);
            attackerEffectManager = petParameter.getEffectManager();
        } else return;

        if (attackerEffectManager.hasEffect(EffectType.AttackProhibited)) {
            if (attacker instanceof Player player) sendMessage(player, "§c[" + EffectType.AttackProhibited.Display + "]§aの効果により§cダメージ§aが§c無効化§aされました");
            return;
        }

        if (victim instanceof Player player) {
            HoloView.add(player);
            if (!Function.isAlive(player)) return;
            PlayerData playerData = playerData(player);
            DEF = playerData.Status.DEF;
            EVA = playerData.Status.EVA;
            CriticalResist = playerData.Status.CriticalResist;
            victimLevel = playerData.Level;
            Resistance = playerData.Status.DamageCauseResistance.getOrDefault(damageCause, Resistance);
            if (player.isInsideVehicle()) {
                DEF = 0;
                EVA = 0;
                CriticalResist = 0;
                Resistance = 1;
            }
            victimEffectManager = playerData.EffectManager;
            if (playerData.Skill.hasSkill("Frenzy")) {
                victimEffectManager.addEffect(EffectType.Frenzy, FrenzyResetTime);
            }
            if (victimEffectManager.hasEffect(EffectType.Sevenfold)) {
                victimEffectManager.removeEffect(EffectType.Sevenfold);
            }
            if (victimEffectManager.hasEffect(EffectType.SubzeroShield)) {
                EffectData effectData = victimEffectManager.getData(EffectType.SubzeroShield);
                if (random.nextDouble() < effectData.getDouble(0)) {
                    attackerEffectManager.addEffect(EffectType.Freeze, effectData.getInt(1));
                }
            }
        } else if (MobManager.isEnemy(victim)) {
            EnemyData enemyData = MobManager.EnemyTable(victim.getUniqueId());
            if (enemyData == null) return;
            DEF = enemyData.DEF;
            EVA = enemyData.EVA;
            CriticalResist = enemyData.CriticalResist;
            Resistance = enemyData.DamageCauseResistance.getOrDefault(damageCause, Resistance);
            victimLevel = enemyData.Level;
            victimEffectManager = enemyData.effectManager;
            enemyData.HitCount++;
        } else if (PetManager.isPet(victim)) {
            PetParameter petParameter = PetManager.PetParameter(victim);
            DEF = petParameter.DEF;
            EVA = petParameter.EVA;
            CriticalResist = petParameter.CriticalResist;
            Resistance = petParameter.DamageCauseResistance.getOrDefault(damageCause, Resistance);
            victimLevel = petParameter.Level;
            petParameter.DecreaseStamina(3, 1);
            victimEffectManager = petParameter.getEffectManager();
        } else return;

        victim.playEffect(EntityEffect.HURT);
        if (victimEffectManager.isInvincible() && !ignoreInvincible) {
            String log = "§b§l" + EffectType.Invincible.Display;
            randomHologram(log, victim.getEyeLocation());
            if (attacker instanceof Player player) {
                DamageLogType DamageLog = playerData(player).DamageLog;
                if (DamageLog.isDamageOnly()) {
                    player.sendMessage("§a≫" + log);
                }
            }
            if (victim instanceof Player player) {
                DamageLogType DamageLog = playerData(player).DamageLog;
                if (DamageLog.isDamageOnly()) {
                    player.sendMessage("§c≪" + log);
                }
            }
            return;
        }

        baseDamage = (Math.pow(ATK, 2) / (ATK + DEF * 2)) * (1-perforate);
        baseDamage += ATK*perforate;
        baseDamage *= damageMultiply;
        baseDamage *= Multiply;
        baseDamage /= Resistance;
        if (!invariably) {
            hitRate = Math.min(1, Math.pow(ACC, 1.6) / Math.pow(EVA, 1.6));
            if (victimEffectManager.hasEffect(EffectType.AbsolutelyEVA)) hitRate = 0;
        } else hitRate = 1;
        if (CriticalRate > CriticalResist) {
            criRate = (CriticalRate-CriticalResist/2)/((CriticalRate*2+CriticalResist)/3);
        } else {
            criRate = 1-(CriticalResist-CriticalRate/2)/((CriticalResist*2+CriticalRate)/3);
        }
        criRate = Math.min(Math.max(criRate, 0.01), 0.95);
        Attack = ATK;
        Defence = DEF;

        if ((attacker instanceof Player || PetManager.isPet(attacker)) && (victim instanceof Player || PetManager.isPet(victim))) {
            baseDamage /= PvPDecay;
        }

        if (victimLevel - attackerLevel > 30) {
            baseDamage /= 1+(victimLevel - attackerLevel)/10f;
        }

        if (victimEffectManager.hasEffect(EffectType.Glory)) baseDamage *= 2;
        if (victimEffectManager.hasEffect(EffectType.Seiko)) baseDamage /= 2;

        int hitCount = 0;
        int criCount = 0;
        int missCount = 0;
        for (int i = 0; i < count; i++) {
            if (random.nextDouble() <= hitRate) {
                if (random.nextDouble() <= criRate) {
                    criCount++;
                    damage += baseDamage * CriticalMultiply;
                    randomHologram("§b§l❤" + String.format("%.1f", baseDamage * CriticalMultiply), victim.getEyeLocation(), HoloView);
                } else {
                    hitCount++;
                    damage += baseDamage;
                    randomHologram("§c§l❤" + String.format("%.1f", baseDamage), victim.getEyeLocation(), HoloView);
                }
            } else {
                missCount++;
                randomHologram("§7§lMiss [" + String.format("%.0f", hitRate * 100) + "%]", victim.getEyeLocation(), HoloView);
                if (victimEffectManager.hasEffect(EffectType.Bully)) victimEffectManager.addEffect(EffectType.Bully, DataBase.getSkillData("Bully").ParameterValueInt(0)*20, null, 1);
            }
        }

        if (victimEffectManager.hasEffect(EffectType.Reflection)) {
            double ReflectionDamage = -damage/10;
            if (attacker instanceof Player player) {
                playerData(player).changeHealth(ReflectionDamage);
            }
        }

        if (attackerEffectManager.hasEffect(EffectType.EnchantSlow)) {
            EffectData effectData = attackerEffectManager.getData(EffectType.EnchantSlow);
            double percent = effectData.getDouble(0);
            int time = effectData.getInt(1);
            if (random.nextDouble() < percent) victimEffectManager.addEffect(EffectType.Slow, time);
        }

        boolean victimDead = false;
        if (victim instanceof Player player) {
            PlayerData playerData = playerData(player);
            if (victimEffectManager.hasEffect(EffectType.CrossGuard)) {
                int time = victimEffectManager.getData(EffectType.CrossGuard).getInt(0);
                victimEffectManager.addEffect(EffectType.CrossGuardCounter, time);
                victimEffectManager.removeEffect(EffectType.CrossGuard);
                sendMessage(player,"§e[" + EffectType.CrossGuardCounter.Display + "]§aが発動しました", SoundList.Counter);
                RuneParameter rune = playerData.Equipment.equippedRune("反転切りのルーン");
                if (rune != null) makeDamage(victim, attacker, DamageCause.ATK, rune.Id, rune.AdditionParameterValue(0)/100, 1);
            }
            playerData.HealthRegenDelay = 40;
            double finalDamage = damage;
            if (playerData.Status.Shield > 0) {
                if (playerData.Status.Shield > finalDamage) {
                    playerData.Status.Shield -= finalDamage;
                    finalDamage = 0;
                } else {
                    finalDamage -= playerData.Status.Shield;
                    playerData.Status.Shield = 0;
                    playerData.stopShieldTask();
                }
            }
            if (playerData.Status.Health - finalDamage > 0) {
                playerData.Status.Health -= finalDamage;
            } else {
                boolean isRevive = false;
                for (EffectType effectType : new EffectType[]{EffectType.Revive, EffectType.LastChance}) {
                    if (playerData.EffectManager.hasEffect(effectType)) {
                        playerData.Status.Health = playerData.Status.MaxHealth / 2;
                        if (effectType == EffectType.Revive) {
                            int time = playerData.EffectManager.getData(EffectType.Revive).getInt(0);
                            if (time > 0) playerData.EffectManager.addEffect(EffectType.Invincible, time);
                        }
                        playerData.EffectManager.removeEffect(effectType);
                        player.sendMessage("§e[" + effectType.Display + "]§aが発動しました");
                        isRevive = true;
                        break;
                    }
                }
                if (!isRevive) {
                    victimDead = true;
                    playerData.dead();
                    if (attacker instanceof Player player2) {
                        PlayerData playerData2 = playerData(player2);
                        playerData2.Status.Health += playerData.Status.MaxHealth / 5;
                    }
                }
            }

            victimMaxHealth = playerData.Status.MaxHealth;
            victimHealth = playerData.Status.Health;
        } else if (MobManager.isEnemy(victim)) {
            EnemyData enemyData = MobManager.EnemyTable(victim.getUniqueId());

            boolean isStop = false;
            for (double HPStop : enemyData.mobData.HPStop) {
                if (enemyData.Health / enemyData.MaxHealth > HPStop && HPStop >= (enemyData.Health-damage) / enemyData.MaxHealth) {
                    enemyData.Health = enemyData.MaxHealth * (HPStop-0.0001);
                    for (MobSkillData skillData : enemyData.mobData.SkillList) {
                        if (skillData.maxHealth == HPStop) {
                            enemyData.skillManager.mobSkillCast(skillData);
                        }
                    }
                    isStop = true;
                    break;
                }
            }
            if (isStop) {
                enemyData.effectManager.addEffect(EffectType.Invincible, 10);
            } else {
                enemyData.Health -= damage;
            }
            double priority = attackerEffectManager.hasEffect(EffectType.SwashBaring) ? damage*10 : damage;
            enemyData.addPriority(attacker, priority);
            enemyData.addDamage(attacker, damage);
            if (enemyData.Health > 0) {
                victim.setHealth(enemyData.Health);
            } else {
                victimDead = true;
                enemyData.dead();
            }
            victimMaxHealth = enemyData.MaxHealth;
            victimHealth = enemyData.Health;
        } else if (PetManager.isPet(victim)) {
            PetParameter petStatus = PetManager.PetParameter(victim);
            if (petStatus.Health - damage > 0) {
                petStatus.Health -= damage;
            } else {
                victimDead = true;
                petStatus.dead();
            }
            victimMaxHealth = petStatus.MaxHealth;
            victimHealth = petStatus.Health;
        }

        if (PetManager.isPet(attacker)) {
            PetParameter pet = PetManager.PetParameter(attacker);
            attacker = pet.player;
            if (victimDead) {
                pet.target = null;
            }
        }
        if (PetManager.isPet(victim)) {
            victim = PetManager.PetParameter(victim).player;
        }

        String damageText = "";
        if (hitCount > 0) damageText += "§e" + String.format("%.1f", baseDamage) + "§ax" + hitCount + " ";
        if (criCount > 0) damageText += "§b" + String.format("%.1f", baseDamage * CriticalMultiply) + "§ax" + criCount + " ";
        if (missCount > 0) damageText += "§7Miss§ax" + missCount + " ";
        if (hitCount + criCount > 0) {

        } else {
            if (victimEffectManager.hasEffect(EffectType.Muleta)) {
                Damage.makeDamage(victim, attacker, DamageCause.ATK, EffectType.Muleta.toString(), victimEffectManager.getData(EffectType.Muleta).getDouble(0), 1);
                victimEffectManager.removeEffect(EffectType.Muleta);
                if (victim instanceof Player player) sendMessage(player,"§e[" + EffectType.Muleta.Display + "]§aが発動しました", SoundList.Counter);
            }
        }

        final String M = "§f[M:" + String.format("%.0f", damageMultiply * 100) + "%]";
        final String HP = " §c[HP:" + String.format("%.0f", victimHealth) + "/" + String.format("%.0f", victimMaxHealth) + "]";
        final String AD = " §e[AD:" + String.format("%.1f", Attack) + "/" + String.format("%.1f", Defence) + "]";
        final String HR = " §a[HR:" + String.format("%.0f", hitRate * 100) + "%]";
        final String CR = " §b[CR:" + String.format("%.0f", criRate * 100) + "%]";
        final String R = " §b[R:" + String.format("%.1f", (1 - (1 / Resistance)) * 100) + "%]";
        if (attacker instanceof Player player) {
            PlayerData playerData = playerData(player);
            playerData.addDPS(damage);
            DamageLogType DamageLog = playerData.DamageLog;
            if (DamageLog.isDamageOnly()) {
                String damageLog = "§a≫" + damageText;
                if (DamageLog.isDetail()) {
                    damageLog += M + HP;
                    if (DamageLog.isAll()) {
                        damageLog += AD + HR + CR + R;
                    }
                }
                player.sendMessage(damageLog);
            }
        }

        if (victim instanceof Player player) {
            DamageLogType DamageLog = playerData(player).DamageLog;
            if (DamageLog.isDamageOnly()) {
                String damageLog = "§c≪" + damageText;
                if (DamageLog.isDetail()) {
                    damageLog += M + HP;
                    if (DamageLog.isAll()) {
                        damageLog += AD + HR + CR + R;
                    }
                }
                player.sendMessage(damageLog);
            }
        }
    }

    static void randomHologram(String string, Location loc) {
        randomHologram(string, loc, new HashSet<>());
    }

    static void randomHologram(String string, Location loc, Set<Player> players) {
        double x = random.nextDouble() * 2 - 1;
        double y = random.nextDouble() + 1;
        double z = random.nextDouble() * 2 - 1;
        MultiThread.TaskRunSynchronized(() -> {
            loc.add(x, y, z);
            Hologram hologram = createHologram(loc);
            VisibilityManager manager = hologram.getVisibilityManager();
            manager.setVisibleByDefault(false);
            for (Player player : players) {
                manager.showTo(player);
            }
            hologram.appendTextLine(string);
            MultiThread.TaskRunSynchronizedLater(hologram::delete, 20);
        });
    }
}
