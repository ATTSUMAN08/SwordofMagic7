package swordofmagic7;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.VisibilityManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.w3c.dom.css.ViewCSS;

import java.util.ArrayList;
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

    static void makeDamage(LivingEntity attacker, List<LivingEntity> victims, DamageCause damageCause, double damageMultiply, int count, int wait) {
        new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                if (i < victims.size()) {
                    makeDamage(attacker, victims.get(i), damageCause, damageMultiply, count);
                }
                i++;
            }
        }.runTaskTimerAsynchronously(plugin, 0, wait);
    }

    static void makeDamage(LivingEntity attacker, LivingEntity victim, DamageCause damageCause, double damageMultiply, int count) {
        if (victim.isDead()) {
            return;
        }

        double ATK;
        double DEF;
        double ACC;
        double EVA;
        double baseDamage;
        double hitRate;
        double damage = 0;

        double Attack;
        double Defence;
        double victimMaxHealth;
        double victimHealth;

        Bukkit.getScheduler().runTask(plugin, () -> victim.damage(0));

        if (attacker instanceof Player player) {
            PlayerData playerData = playerData(player);
            ATK = playerData.Status.ATK;
            ACC = playerData.Status.ACC;
        } else if (MobManager.getEnemyTable().containsKey(attacker.getUniqueId())) {
            EnemyData enemyData = MobManager.EnemyTable(attacker.getUniqueId());
            ATK = enemyData.ATK;
            ACC = enemyData.ACC;
        } else return;

        if (victim instanceof Player player) {
            PlayerData playerData = playerData(player);
            DEF = playerData.Status.DEF;
            EVA = playerData.Status.EVA;
        } else if (MobManager.getEnemyTable().containsKey(victim.getUniqueId())) {
            EnemyData enemyData = MobManager.EnemyTable(victim.getUniqueId());
            DEF = enemyData.DEF;
            EVA = enemyData.EVA;
        } else {
            Log("A: " + attacker.getName() + " V: NoData");
            return;
        }

        baseDamage = (Math.pow(ATK, 2) / (ATK + DEF*2));
        baseDamage *= damageMultiply;
        hitRate = ACC / EVA;
        Attack = ATK;
        Defence = DEF;

        if (attacker instanceof Player player) {
            PlayerData playerData = playerData(player);
            Attribute attr = playerData.Attribute;
            double Multiply = 1;
            if (damageCause == DamageCause.ATK) {
                Multiply += attr.getAttribute(AttributeType.STR)*0.005;
            } else if (damageCause == DamageCause.MAT) {
                Multiply += attr.getAttribute(AttributeType.INT)*0.004;
            }
            baseDamage *= Multiply;
        }

        if (victim instanceof Player player) {
            PlayerData playerData = playerData(player);
            Attribute attr = playerData.Attribute;
            double Resistance = 1;
            if (damageCause == DamageCause.ATK) {
                Resistance += attr.getAttribute(AttributeType.VIT)*0.003;
            } else if (damageCause == DamageCause.MAT) {
                Resistance += attr.getAttribute(AttributeType.INT)*0.001;
                Resistance += attr.getAttribute(AttributeType.SPI)*0.001;
                Resistance += attr.getAttribute(AttributeType.VIT)*0.001;
            }
            baseDamage /= Resistance;
        }

        int hitCount = 0;
        for (int i = 0; i < count; i++) {
            if (random.nextDouble() <= hitRate) {
                damage += baseDamage;
                hitCount++;
            }
        }

        if (victim instanceof Player player) {
            PlayerData playerData = playerData(player);
            if (playerData.Status.Health - damage > 0) {
                playerData.Status.Health -= damage;
            } else {
                playerData.dead();
            }

            victimMaxHealth = playerData.Status.MaxHealth;
            victimHealth = playerData.Status.Health;
        } else {
            EnemyData enemyData = MobManager.EnemyTable(victim.getUniqueId());
            enemyData.Health -= damage;
            if (attacker instanceof Player player) enemyData.addPriority(player, damage);
            if (enemyData.Health > 0) {
                victim.setHealth(enemyData.Health);
            } else {
                enemyData.dead();
            }
            victimMaxHealth = enemyData.MaxHealth;
            victimHealth = enemyData.Health;
        }

        randomHologram(colored("&c&l❤" + String.format("%.1f", damage)), victim.getEyeLocation());

        if (attacker instanceof Player player) {
            PlayerData playerData = playerData(player);
            if (playerData.DamageLog) player.sendMessage(colored("&a≫&e" + String.format("%.1f", baseDamage) + "&ax" + hitCount
                    + " &c[HP: " + String.format("%.0f", victimHealth) + "/" + String.format("%.0f", victimMaxHealth) + "]"
                    + " &e[AD: " + String.format("%.1f", Attack) + "/" + String.format("%.1f", Defence) + "]"
            ));
        }
    }

    static void randomHologram(String string, Location loc) {
        randomHologram(string, loc, new ArrayList<>());
    }
    static void randomHologram(String string, Location loc, List<Player> players) {
        Random random = new Random();
        double x = random.nextInt(200) - 100;
        double y = random.nextInt(30);
        double z = random.nextInt(200) - 100;
        Bukkit.getScheduler().runTask(plugin, () -> {
            Hologram hologram = HologramsAPI.createHologram(plugin, loc);
            loc.add(x / 100, y / 100, z / 100);
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
