package swordofmagic7;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.VisibilityManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.w3c.dom.css.ViewCSS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static swordofmagic7.DataBase.playerData;
import static swordofmagic7.Function.Log;
import static swordofmagic7.Function.colored;
import static swordofmagic7.System.plugin;

enum DamageCause {
    ATK,
    MAT,
}

public final class Damage {

    private static final Random random = new Random();

    static void makeDamage(LivingEntity attacker, List<LivingEntity> victims, DamageCause damageCause, String damageSource, double damageMultiply, int count, int wait) {
        new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                if (i < victims.size()) {
                    makeDamage(attacker, victims.get(i), damageCause, damageSource, damageMultiply, count);
                }
                i++;
            }
        }.runTaskTimerAsynchronously(plugin, 0, wait);
    }

    static void makeDamage(LivingEntity attacker, LivingEntity victim, DamageCause damageCause, String damageSource, double damageMultiply, int count) {
        if (victim.isDead()) {
            return;
        }

        double ATK;
        double DEF;
        double ACC;
        double EVA;
        double CriticalRate;
        double CriticalResist;
        double baseDamage;
        double hitRate;
        double criRate;
        HashMap<AttributeType, Integer> attackerAttribute = null;
        HashMap<AttributeType, Integer> victimAttribute = null;
        double damage = 0;

        double Attack;
        double Defence;
        double victimMaxHealth = 0;
        double victimHealth = 0;
        double Multiply = 1;
        double CriticalMultiply = 1.2;
        double Resistance = 1;

        LivingEntity finalVictim = victim;
        Bukkit.getScheduler().runTask(plugin, () -> {
            finalVictim.damage(0);
        });

        if (attacker instanceof Player player) {
            PlayerData playerData = playerData(player);
            Attribute attr = playerData.Attribute;
            ATK = playerData.Status.ATK;
            ACC = playerData.Status.ACC;
            CriticalRate = playerData.Status.CriticalRate;
            attackerAttribute = attr.getAttribute();
        } else if (MobManager.isEnemy(attacker)) {
            EnemyData enemyData = MobManager.EnemyTable(attacker.getUniqueId());
            ATK = enemyData.ATK;
            ACC = enemyData.ACC;
            CriticalRate = enemyData.CriticalRate;
        } else if (PetManager.isPet(attacker)) {
            PetParameter petParameter = PetManager.PetParameter(attacker);
            ATK = petParameter.ATK;
            ACC = petParameter.ACC;
            CriticalRate = petParameter.CriticalRate;
            attackerAttribute = petParameter.Attribute;
            petParameter.DecreaseStamina(1, 0.1);
        } else return;

        if (victim instanceof Player player) {
            if (!player.isOnline()) return;
            PlayerData playerData = playerData(player);
            DEF = playerData.Status.DEF;
            EVA = playerData.Status.EVA;
            CriticalResist = playerData.Status.CriticalResist;
            victimAttribute = playerData.Attribute.getAttribute();
        } else if (MobManager.isEnemy(victim)) {
            EnemyData enemyData = MobManager.EnemyTable(victim.getUniqueId());
            DEF = enemyData.DEF;
            EVA = enemyData.EVA;
            CriticalResist = enemyData.CriticalResist;
        } else if (PetManager.isPet(victim)) {
            PetParameter petParameter = PetManager.PetParameter(victim);
            DEF = petParameter.DEF;
            EVA = petParameter.EVA;
            CriticalResist = petParameter.CriticalResist;
            victimAttribute = petParameter.Attribute;
            petParameter.DecreaseStamina(1, 0.7);
        } else {
            return;
        }

        if (attackerAttribute != null) {
            CriticalMultiply += attackerAttribute.get(AttributeType.DEX) * 0.008;
            if (damageCause == DamageCause.ATK) {
                Multiply += attackerAttribute.get(AttributeType.STR) * 0.005;
            } else if (damageCause == DamageCause.MAT) {
                Multiply += attackerAttribute.get(AttributeType.INT) * 0.004;
            }
        }

        if (victimAttribute != null) {
            if (damageCause == DamageCause.ATK) {
                Resistance += victimAttribute.get(AttributeType.VIT)*0.003;
            } else if (damageCause == DamageCause.MAT) {
                Resistance += victimAttribute.get(AttributeType.INT)*0.001;
                Resistance += victimAttribute.get(AttributeType.SPI)*0.001;
                Resistance += victimAttribute.get(AttributeType.VIT)*0.001;
            }
        }

        baseDamage = (Math.pow(ATK, 2) / (ATK + DEF*2));
        baseDamage *= damageMultiply;
        baseDamage *= Multiply;
        baseDamage /= Resistance;
        hitRate = ACC / (EVA*2);
        criRate = (Math.pow(CriticalRate, 2) / (CriticalRate + CriticalResist*2)) / CriticalRate;
        Attack = ATK;
        Defence = DEF;

        if ((attacker instanceof Player || PetManager.isPet(victim)) && victim instanceof Player) {
            baseDamage /= 2;
        }

        int hitCount = 0;
        int criCount = 0;
        for (int i = 0; i < count; i++) {
            if (random.nextDouble() <= hitRate) {
                if (random.nextDouble() <= criRate) {
                    criCount++;
                    damage += baseDamage * CriticalMultiply;
                    randomHologram("§b§l❤" + String.format("%.1f", baseDamage * CriticalMultiply), victim.getEyeLocation());
                } else {
                    hitCount++;
                    damage += baseDamage;
                    randomHologram("§c§l❤" + String.format("%.1f", baseDamage), victim.getEyeLocation());
                }
            } else {
                randomHologram("§7§lMiss [" + String.format("%.0f", hitRate*100) + "%]", victim.getEyeLocation());
            }
        }

        String damageText;
        if (hitCount > 0) {
            damageText = "§e" + String.format("%.1f", baseDamage) + "§ax" + hitCount + " §b" + String.format("%.1f", baseDamage*CriticalMultiply) + "§ax" + criCount;
        } else damageText = "§7Miss";

        if (victim instanceof Player player) {
            PlayerData playerData = playerData(player);
            if (playerData.Status.Health - damage > 0) {
                playerData.Status.Health -= damage;
            } else {
                playerData.dead();
            }

            victimMaxHealth = playerData.Status.MaxHealth;
            victimHealth = playerData.Status.Health;
        } else if (MobManager.isEnemy(victim)) {
            EnemyData enemyData = MobManager.EnemyTable(victim.getUniqueId());
            enemyData.Health -= damage;
            enemyData.addPriority(attacker, damage);
            if (enemyData.Health > 0) {
                victim.setHealth(enemyData.Health);
            } else {
                enemyData.dead();
            }
            victimMaxHealth = enemyData.MaxHealth;
            victimHealth = enemyData.Health;
        } else if (PetManager.isPet(victim)) {
            PetParameter petStatus = PetManager.PetParameter(victim);
            if (petStatus.Health - damage > 0) {
                petStatus.Health -= damage;
            } else {
                petStatus.dead();
            }
            victimMaxHealth = petStatus.MaxHealth;
            victimHealth = petStatus.Health;
        }

        if (PetManager.isPet(attacker)) {
            attacker = PetManager.PetParameter(attacker).player;
        }
        if (PetManager.isPet(victim)) {
            victim = PetManager.PetParameter(victim).player;
        }

        final String M = " §f[M:" + String.format("%.0f", damageMultiply*100) + "%]";
        final String HP = " §c[HP:" + String.format("%.0f", victimHealth) + "/" + String.format("%.0f", victimMaxHealth) + "]";
        final String AD = " §e[AD:" + String.format("%.1f", Attack) + "/" + String.format("%.1f", Defence) + "]";
        final String HR = " §e[HR:" + String.format("%.0f", hitRate*100) + "%]";
        final String CR = " §b[CR:" + String.format("%.0f", criRate*100) + "%]";
        final String R = " §b[R:" + String.format("%.1f", (1 - (1 / Resistance)) * 100) + "%]";
        if (attacker instanceof Player player) {
            DamageLogType DamageLog = playerData(player).DamageLog;
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
        randomHologram(string, loc, new ArrayList<>());
    }
    static void randomHologram(String string, Location loc, List<Player> players) {
        Random random = new Random();
        double x = random.nextDouble()*2 - 1;
        double y = random.nextDouble() + 1;
        double z = random.nextDouble()*2 - 1;
        Bukkit.getScheduler().runTask(plugin, () -> {
            Hologram hologram = HologramsAPI.createHologram(plugin, loc);
            loc.add(x, y, z);
            VisibilityManager visibilityManager = hologram.getVisibilityManager();
            if (players.size() > 0) {
                visibilityManager.setVisibleByDefault(false);
                for (Player player : players) {
                    visibilityManager.showTo(player);
                }
            }
            hologram.appendTextLine(string);
            hologram.teleport(loc);
            Bukkit.getScheduler().runTaskLater(plugin, hologram::delete, 20);
        });
    }
}
