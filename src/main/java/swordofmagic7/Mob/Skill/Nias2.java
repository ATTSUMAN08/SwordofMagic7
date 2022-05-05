package swordofmagic7.Mob.Skill;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Effect.EffectManager;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Mob.EnemySkillManager;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.PlayerList;
import swordofmagic7.Sound.SoundList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static swordofmagic7.Function.sendMessage;
import static swordofmagic7.Particle.ParticleManager.angle;

public class Nias2 extends EnemySkillBase {
    public Nias2(EnemySkillManager manager) {
        super(manager);
    }

    public void Regret2() {
        MultiThread.TaskRun(() -> {
            Manager.CastSkillIgnoreAI(true);
            LivingEntity entity = Manager.enemyData.entity;
            radiusMessage("§c「あなたは過去を振り返ったことがありますか？」", SoundList.DungeonTrigger);

            int time = 200;
            Manager.enemyData.effectManager.addEffect(EffectType.Invincible, time);
            HashMap<Player, Location> save = new HashMap<>();
            for (Player player : PlayerList.getNearNonDead(Manager.enemyData.entity.getLocation(), radius)) {
                save.put(player, player.getLocation().clone());
                EffectManager.addEffect(player, EffectType.Regret, time, null);
            }
            MultiThread.sleepTick(time);
            for (Map.Entry<Player, Location> data : save.entrySet()) {
                Player player = data.getKey();
                Location location = data.getValue();
                if (Math.abs(angle(location.getDirection()) % 360 - angle(player.getLocation().getDirection()) % 360) < 170) {
                    Damage.makeDamage(entity, player, DamageCause.ATK, "Regret", 100, 1, 0.5, true);
                    sendMessage(player, "§c「たまには過去を振り返ってみるべきです」", SoundList.Nope);
                } else sendMessage(player, "§c「過去を振り返ってみてどうでしたか？」", SoundList.Tick);
            }

            Manager.CastSkillIgnoreAI(false);
        }, "Regret");
    }

    public void Execution2() {
        MultiThread.TaskRun(() -> {
            Manager.CastSkill(true);

            radiusMessage("§c「嫌だ...」", SoundList.DungeonTrigger);
            int time = 200;
            Manager.enemyData.effectManager.addEffect(EffectType.Invincible, time);
            Set<Player> players = new HashSet<>();
            for (Player player : PlayerList.getNearNonDead(Manager.enemyData.entity.getLocation(), radius)) {
                EffectManager.addEffect(player, EffectType.Regret, time, null);
                players.add(player);
            }
            Vector vector = new Vector(0, -1, 0.7);
            for (int i = 0; i < time; i++) {
                for (Player player : players) {
                    if (player.getGameMode() == GameMode.SURVIVAL) {
                        player.setVelocity(vector);
                        if (player.isSneaking()) Damage.makeDamage(entity(), player, DamageCause.ATK, "Execution2", 1, 1, 1, true, true);
                    }
                }
                MultiThread.sleepTick(1);
            }

            Manager.CastSkill(false);
        }, "Execution");
    }
}
