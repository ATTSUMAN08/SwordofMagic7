package swordofmagic7.Mob.Skill;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.EffectManager;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Function;
import swordofmagic7.Mob.EnemySkillManager;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.PlayerList;
import swordofmagic7.Sound.SoundList;

import java.util.HashSet;
import java.util.Set;

import static swordofmagic7.Dungeon.Dungeon.world;
import static swordofmagic7.Function.sendMessage;
import static swordofmagic7.Sound.CustomSound.playSound;

public class LibraryGovernor {

    private final EnemySkillManager Manager;
    private final Location[] location = new Location[5];
    private final double radius = 64;
    public LibraryGovernor(EnemySkillManager manager) {
        this.Manager = manager;
        location[0] = new Location(world,5396, 115, 2402);
        location[1] = new Location(world,5425, 115, 2402);
        location[2] = new Location(world,5368, 115, 2402);
        location[3] = new Location(world,5396, 115, 2429);
        location[4] = new Location(world,5396, 115, 2375);
    }

    private void radiusMessage(String message, SoundList sound) {
        for (Player player : PlayerList.getNear(Manager.enemyData.entity.getLocation(), 64)) {
            sendMessage(player, message);
            playSound(player, sound);
        }
    }

    public void ToBow() {
        MultiThread.TaskRun(() -> {
            Manager.CastSkill(true);
            radiusMessage("§c「まずはお辞儀しましょう」", SoundList.DungeonTrigger);
            MultiThread.sleepTick(50);
            for (Player player : PlayerList.getNearNonDead(Manager.enemyData.entity.getLocation(), radius)) {
                if (!player.isSneaking() || player.getEyeLocation().getPitch() < 70) {
                    PlayerData.playerData(player).dead();
                    sendMessage(player, "§cルールや手順を誤ると待っているのは死です", SoundList.Nope);
                } else {
                    sendMessage(player, "§c勝利の糸口はルールや手順を守ることです", SoundList.Nope);
                }
            }
            MultiThread.sleepTick(10);
            Manager.CastSkill(false);
        }, "ToBow");
    }

    public void ItsGlory() {
        MultiThread.TaskRun(() -> {
            Manager.CastSkillIgnoreAI(true);
            radiusMessage("§c「栄光とはどのようなことを指すのでしょうか」", SoundList.DungeonTrigger);
            for (Player player : PlayerList.getNearNonDead(Manager.enemyData.entity.getLocation(), radius)) {
                EffectManager.addEffect(player, EffectType.Glory, 900, null);
            }
            MultiThread.sleepTick(10);
            Manager.CastSkillIgnoreAI(false);
        }, "ItsGlory");
    }

    public void ExcessiveTreatment() {
        MultiThread.TaskRun(() -> {
            Manager.CastSkill(true);
            radiusMessage("§c「治療は適度に行うべきです。過剰な治療は時に死ぬ招くこともあります」", SoundList.DungeonTrigger);
            MultiThread.sleepTick(50);
            Set<PlayerData> playerDataSet = new HashSet<>();
            for (Player player : PlayerList.getNearNonDead(Manager.enemyData.entity.getLocation(), radius)) {
                PlayerData playerData = PlayerData.playerData(player);
                playerDataSet.add(playerData);
                playerData.EffectManager.addEffect(EffectType.ExcessiveTreatment, 40*5);
                playerData.Status.Health = playerData.Status.MaxHealth/10;
            }
            for (int i = 0; i < 40; i++) {
                for (PlayerData playerData : playerDataSet) {
                    playerData.changeHealth(playerData.Status.MaxHealth/100);
                    if (playerData.Status.Health == playerData.Status.MaxHealth) {
                        playerData.dead();
                        sendMessage(playerData.player, "§c「過剰な治療は時に死ぬ招くこともあります」", SoundList.Nope);
                    }
                }
                playerDataSet.removeIf(playerData -> playerData.isDead);
                MultiThread.sleepTick(5);
            }
            for (PlayerData playerData : playerDataSet) {
                if (playerData.Status.Health < playerData.Status.MaxHealth*0.8) {
                    playerData.dead();
                    sendMessage(playerData.player, "§c「どうして適度な治療を行わなかったのでしょうか」", SoundList.Nope);
                } else {
                    sendMessage(playerData.player, "§c「適度な治療とはこのようなことを言うのです」", SoundList.Tick);
                }
            }
            MultiThread.sleepTick(10);
            Manager.CastSkill(false);
        }, "ExcessiveTreatment");
    }

    public void UnpleasantOmen() {
        MultiThread.TaskRun(() -> {
            Manager.CastSkill(true);
            radiusMessage("§c「どこかから嫌な気配を感じます」", SoundList.DungeonTrigger);
            ParticleData particleData = new ParticleData(Particle.FLAME);
            for (Player player : PlayerList.getNearNonDead(Manager.enemyData.entity.getLocation(), radius)) {
                PlayerData.playerData(player).EffectManager.addEffect(EffectType.Unconscious, 50);
            }
            MultiThread.sleepTick(50);
            for (Player player : PlayerList.getNearNonDead(Manager.enemyData.entity.getLocation(), radius)) {
                Location location = null;
                float yaw = player.getLocation().getYaw();
                int hashInt = Function.StringToHashInt(player.getName(), 4);
                switch (hashInt) {
                    case 0 -> {
                        if (player.getEyeLocation().getPitch() > -70) location = player.getEyeLocation().clone().add(0, 5, 0);
                    }
                    case 1 -> {
                        if (-110 > yaw || yaw > -70) location = player.getEyeLocation().clone().add(5, 0, 0);
                    }
                    case 2 -> {
                        if (70 > yaw || yaw > 110) location = player.getEyeLocation().clone().add(-5, 0, 0);
                    }
                    case 3 -> {
                        if (-20 > yaw || yaw > 20) location = player.getEyeLocation().clone().add(0, 0, 5);
                    }
                }
                if (location != null) {
                    ParticleManager.LineParticle(particleData, location, player.getEyeLocation(), 1, 2);
                    PlayerData.playerData(player).dead();
                    sendMessage(player, "§c「嫌な気配の正体は死だったようです」", SoundList.Nope);
                } else {
                    sendMessage(player, "§c「嫌な気配は無くなりました」", SoundList.Tick);
                }
            }
            MultiThread.sleepTick(10);
            Manager.CastSkill(false);
        }, "UnpleasantOmen");
    }

    public void DifferenceInInertia() {
        MultiThread.TaskRun(() -> {
            Manager.CastSkillIgnoreAI(true);
            radiusMessage("§c「寂しがりやな人、一人でいたい人、それぞれ感性の違いというものがあります」", SoundList.DungeonTrigger);
            MultiThread.sleepTick(80);
            for (Player player : PlayerList.getNearNonDead(Manager.enemyData.entity.getLocation(), radius)) {
                EffectManager.addEffect(player, EffectType.Glory, 900, null);
            }
            MultiThread.sleepTick(10);
            Manager.CastSkillIgnoreAI(false);
        }, "ItsGlory");
    }
}
