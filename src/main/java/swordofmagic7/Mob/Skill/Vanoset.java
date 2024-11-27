package swordofmagic7.Mob.Skill;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import swordofmagic7.Damage.Damage;
import swordofmagic7.Damage.DamageCause;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.EffectManager;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Function;
import swordofmagic7.Mob.EnemyData;
import swordofmagic7.Mob.EnemySkillManager;
import swordofmagic7.Mob.MobManager;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.PlayerList;
import swordofmagic7.RayTrace.RayTrace;
import swordofmagic7.Sound.SoundList;

import java.util.HashSet;
import java.util.Set;

import static swordofmagic7.Dungeon.Dungeon.world;
import static swordofmagic7.Function.sendMessage;
import static swordofmagic7.SomCore.plugin;
import static swordofmagic7.SomCore.random;
import static swordofmagic7.Sound.CustomSound.playSound;

public class Vanoset {
    private final EnemySkillManager Manager;
    private LivingEntity entity;
    public EnemyData Altar;
    private final Location location = new Location(world, 5371.5, 173, 3902.5);
    private final Location[] SacrificeLocation = new Location[4];
    public Vanoset(EnemySkillManager manager) {
        this.Manager = manager;
        SacrificeLocation[0] = new Location(world, 5371.5, 172, 3858.5);
        SacrificeLocation[1] = new Location(world, 5327.5, 172, 3902.5);
        SacrificeLocation[2] = new Location(world, 5371.5, 172, 3946.5);
        SacrificeLocation[3] = new Location(world, 5415.5, 172, 3902.5);

        MultiThread.TaskRunSynchronizedLater(() -> {
            Altar = MobManager.mobSpawn(DataBase.getMobData("ノヴァハ祭壇"), Manager.enemyData.Level, location);
            MultiThread.TaskRun(() -> {
                while (plugin.isEnabled() && Manager.enemyData.isAlive() && Altar.isAlive() && !Altar.entity.isDead()) {
                    double percent = Manager.enemyData.Health/Manager.enemyData.MaxHealth;
                    double percent2 = Altar.Health/Altar.MaxHealth;
                    if (Math.abs(percent - percent2) >= 0.01) {
                        if (percent < percent2) {
                            Manager.enemyData.effectManager.addEffect(EffectType.Invincible, 20);
                        } else {
                            Altar.effectManager.addEffect(EffectType.Invincible, 20);
                        }
                    }
                    MultiThread.sleepTick(5);
                }
                if (Altar.isAlive()) {
                    ParticleData particleData = new ParticleData(Particle.EXPLOSION_EMITTER);
                    for (Player player : PlayerList.getNearNonDead(location, 96)) {
                        particleData.spawn(player.getEyeLocation());
                        PlayerData.playerData(player).dead();

                        sendMessage(player, "§c祭壇が崩壊しました...", SoundList.Explosion);
                    }
                }
            }, "SoulSyncAlterAndBoss");
            MultiThread.TaskRun(() -> {
                boolean potential = true;
                boolean pastFacts = true;
                while (plugin.isEnabled() && Manager.enemyData.isAlive()) {
                    double percent = Altar.Health / Altar.MaxHealth;
                    if (potential && percent <= 0.51) {
                        potential = false;
                        Potential();
                    }
                    if (pastFacts && percent <= 0.1) {
                        pastFacts = false;
                        PastFacts();
                    }
                    MultiThread.sleepTick(20);
                }
            }, "NovahaAlter");
        }, 2);
        MultiThread.TaskRunLater(() -> entity = Manager.enemyData.entity, 1, "Vanoset");
    }

    private void radiusMessage(String message, SoundList sound) {
        for (Player player : PlayerList.getNear(Manager.enemyData.entity.getLocation(), 64)) {
            sendMessage(player, message);
            playSound(player, sound);
        }
    }

    public void Tornado(int CastTime) {
        MultiThread.TaskRun(() -> {
            LivingEntity target = Manager.enemyData.target;
            if (target != null) {
                radiusMessage("§c竜巻波が来ます！避けてください！", SoundList.DungeonTrigger);
                Manager.CastSkill(true);
                Manager.enemyData.effectManager.addEffect(EffectType.Invincible, CastTime+60);
                ParticleData particleData = new ParticleData(Particle.FLAME, 0.05f, Function.VectorUp);
                ParticleData particleData2 = new ParticleData(Particle.EXPLOSION_EMITTER);
                int i = 0;
                while (Manager.enemyData.isAlive() && !Manager.setCancel && i < CastTime) {
                    ParticleManager.CircleParticle(particleData, entity.getLocation(), 1, 24);
                    i += Manager.period;
                    MultiThread.sleepTick(Manager.period);
                }

                for (i = 6; i > 0; i--) {
                    double radius = i*5;
                    double radius2 = (i+1)*5;
                    Location origin = entity.getLocation();
                    ParticleManager.CircleParticle(particleData2, origin, radius, 12);
                    ParticleManager.CircleParticle(particleData2, origin, radius2, 12);
                    Set<LivingEntity> victims = Function.NearEntityByEnemy(origin, radius2);
                    victims.removeAll(Function.NearEntityByEnemy(origin, radius));
                    for (LivingEntity victim : victims) {
                        Function.setVelocity(victim, Function.VectorUp.clone().setY(2));
                        Damage.makeDamage(entity, victim, DamageCause.ATK, "Tornado", 4, 1);
                        if (victim instanceof Player player) playSound(player, SoundList.Explosion);
                    }
                    MultiThread.sleepTick(7);
                }

                MultiThread.sleepTick(10);
                Manager.CastSkill(false);
            }
        }, "Tornado");
    }

    public void Squall(int CastTime) {
        MultiThread.TaskRun(() -> {
            Manager.CastSkill(true);
            Manager.enemyData.effectManager.addEffect(EffectType.Invincible, CastTime);
            ParticleData particleData = new ParticleData(Particle.DUST);
            ParticleData particleData2 = new ParticleData(Particle.EXPLOSION_EMITTER);
            double radius = 15;
            int i = 0;
            while (Manager.enemyData.isAlive() && !Manager.setCancel && i < CastTime) {
                ParticleManager.CircleParticle(particleData, entity.getLocation(), radius, 24);
                for (LivingEntity victim : Function.NearEntityByEnemy(entity.getLocation(), radius)) {
                    ParticleManager.LineParticle(particleData, entity.getEyeLocation(), victim.getEyeLocation(), 0, 10);
                }
                i += Manager.period;
                MultiThread.sleepTick(Manager.period);
            }

            Location origin = entity.getLocation();
            ParticleManager.CircleParticle(particleData2, origin, radius, 12);
            for (LivingEntity victim : Function.NearEntityByEnemy(origin, radius)) {
                Vector vector = victim.getLocation().toVector().subtract(origin.toVector()).setY(1);
                Function.setVelocity(victim, vector);
                Damage.makeDamage(entity, victim, DamageCause.ATK, "Squall", 6, 1);
                if (victim instanceof Player player) playSound(player, SoundList.Explosion);
            }

            MultiThread.sleepTick(10);
            Manager.CastSkill(false);
        }, "Squall");
    }

    public void WrongedFaith(int CastTime) {
        MultiThread.TaskRun(() -> {
            radiusMessage("§c「盲信的な信仰は正しいものなのでしょうか？」", SoundList.DungeonTrigger);
            Manager.CastSkill(true);
            ParticleData particleData = new ParticleData(Particle.DUST);

            int i = 0;
            while (Manager.enemyData.isAlive() && !Manager.setCancel && i < CastTime) {
                for (LivingEntity victim : Function.NearEntityByEnemy(entity.getLocation(), 96)) {
                    ParticleManager.CircleParticle(particleData, victim.getLocation(), 1, 10);
                }
                i += Manager.period;
                MultiThread.sleepTick(Manager.period);
            }

            for (LivingEntity entity : Function.NearEntityByEnemy(entity.getLocation(), 96)) {
                EffectManager.addEffect(entity, EffectType.Confusion, 300, null);
                EffectManager.addEffect(entity, EffectType.Stun, 10, null);
                if (entity instanceof  Player player) playSound(player, SoundList.Rock);
            }

            MultiThread.sleepTick(10);
            Manager.CastSkill(false);
        }, "WrongedFaith");
    }

    public void UnderTheSky(int CastTime) {
        MultiThread.TaskRun(() -> {
            radiusMessage("§c「周囲に対抗魔法陣が生成されます」", SoundList.DungeonTrigger);
            Manager.CastSkill(true);
            ParticleData particleData = new ParticleData(Particle.FIREWORK, 0.01f, Function.VectorUp);
            ParticleData particleData2 = new ParticleData(Particle.EXPLOSION_EMITTER);
            Manager.enemyData.effectManager.addEffect(EffectType.Invincible, CastTime+40);
            double radius = 3;

            Location[] locations = new Location[3];
            for (int i = 0; i < locations.length; i ++) {
                Location origin = entity.getLocation().clone().add(random.nextDouble()*30-15, 0, random.nextDouble()*30-15);
                origin.setPitch(90);
                locations[i] = RayTrace.rayLocationBlock(origin, 16, false).HitPosition;
            }

            int i = 0;
            while (Manager.enemyData.isAlive() && !Manager.setCancel && i < CastTime) {
                for (Location location : locations) {
                    ParticleManager.CircleParticle(particleData, location, radius, 24);
                }
                i += Manager.period;
                MultiThread.sleepTick(Manager.period);
            }

            Set<LivingEntity> victims = Function.NearEntityByEnemy(location, 96);
            for (Location location : locations) {
                victims.removeAll(Function.NearEntityByEnemy(location, radius));
            }

            for (i = 0; i < 20; i++) {
                for (LivingEntity entity : victims) {
                    particleData2.spawn(entity.getLocation());
                    entity.setVelocity(Function.VectorUp.clone().setY(0.5));
                }
                MultiThread.sleepTick(1);
            }

            for (i = 0; i < 5; i++) {
                for (LivingEntity entity : victims) {
                    particleData2.spawn(entity.getLocation());
                    entity.setVelocity(Function.VectorDown.clone().setY(-2));
                }
                MultiThread.sleepTick(1);
            }

            for (LivingEntity entity : victims) {
                Damage.makeDamage(this.entity, entity, DamageCause.ATK, "UnderTheSky", 100, 1, 0.5);
                ParticleManager.CircleParticle(particleData2, entity.getLocation(), radius, 24);
                for (LivingEntity entity2 : Function.NearEntityByEnemy(entity.getLocation(), radius)) {
                    Damage.makeDamage(this.entity, entity2, DamageCause.ATK, "UnderTheSky", 7, 1);
                    if (entity2 instanceof  Player player) playSound(player, SoundList.Explosion);
                }
            }

            MultiThread.sleepTick(10);
            Manager.CastSkill(false);
        }, "UnderTheSky");
    }

    public int SacrificeCount = -1;
    public void Sacrifice() {
        MultiThread.TaskRun(() -> {
            radiusMessage("§c「祭壇が生贄を吸収しようとしています」", SoundList.DungeonTrigger);
            Manager.CastSkill(true);
            ParticleData particleData = new ParticleData(Particle.EXPLOSION_EMITTER);

            Set<EnemyData> enemyList = new HashSet<>();
            MultiThread.TaskRunSynchronized(() -> {
                for (Location location : SacrificeLocation) {
                    for (int i = 0; i < 5; i++) {
                        EnemyData enemyData = MobManager.mobSpawn(DataBase.getMobData("ノヴァハ生贄"), Manager.enemyData.Level, location);
                        enemyData.overrideTargetLocation = Altar.entity.getLocation();
                        enemyList.add(enemyData);
                    }
                }
                MultiThread.TaskRun(() -> {
                    boolean isAlive = true;
                    SacrificeCount = 0;
                    while (plugin.isEnabled() && enemyList.size() > 0) {
                        Manager.enemyData.effectManager.addEffect(EffectType.Invincible, 25);
                        enemyList.removeIf(EnemyData::isDead);
                        for (EnemyData enemyData : enemyList) {
                            if (Altar.entity.getLocation().distance(enemyData.entity.getLocation()) < 8) {
                                SacrificeCount++;
                                particleData.spawn(enemyData.entity.getEyeLocation());
                                playSound(enemyData.entity.getEyeLocation(), SoundList.Explosion);
                                enemyData.delete();
                            }
                        }
                        if (SacrificeCount >= 10) {
                            for (LivingEntity entity : Function.NearEntityByEnemy(entity.getLocation(), 96)) {
                                if (entity instanceof  Player player) PlayerData.playerData(player).dead();
                            }
                            isAlive = false;
                            radiusMessage("§c「祭壇が起動しました」", SoundList.Explosion);
                            break;
                        }
                        MultiThread.sleepTick(20);
                    }
                    SacrificeCount = -1;
                    if (isAlive) radiusMessage("§c「祭壇の起動を阻止しました」", SoundList.Tick);
                    MultiThread.sleepTick(10);
                    Manager.CastSkill(false);
                }, "Sacrifice");
            });
        }, "Sacrifice");
    }

    public void Vortex() {
        MultiThread.TaskRun(() -> {
            radiusMessage("§c「ときに人は、何かに引き込まれてしまうものです」", SoundList.DungeonTrigger);
            Manager.CastSkill(true);
            ParticleData particleData = new ParticleData(Particle.CRIT);

            MultiThread.sleepTick(10);
            Manager.CastSkill(false);

            for (int i = 0; i < 15*4; i++) {
                Set<LivingEntity> victims = Function.NearEntityByEnemy(entity.getLocation(), 96);
                victims.remove(Manager.enemyData.target);
                for (LivingEntity victim : victims) {
                    ParticleManager.LineParticle(particleData, victim.getEyeLocation(), entity.getEyeLocation(), 1, 10);
                    Function.setVelocity(victim,entity.getLocation().toVector().subtract(victim.getLocation().toVector()).multiply(0.3));
                }
                MultiThread.sleepTick(5);
            }
        }, "Vortex");
    }

    public void Unconscious() {
        MultiThread.TaskRun(() -> {
            radiusMessage("§c「自分では、自分が狂っていることはわからないものです」", SoundList.DungeonTrigger);
            Manager.CastSkill(true);
            for (Player player : PlayerList.getNearNonDead(entity.getLocation(), 96)) {
                PlayerData.playerData(player).EffectManager.addEffect(EffectType.Unconscious, 400);
            }
            MultiThread.sleepTick(10);
            Manager.CastSkill(false);

            for (int i = 0; i < 10; i++) {
                MultiThread.TaskRunSynchronized(() -> {
                    for (Player player : PlayerList.getNearNonDead(location, 96)) {
                        Location location = player.getLocation();
                        location.setDirection(Vector.getRandom());
                        player.teleportAsync(location);
                    }
                });
                MultiThread.sleepTick(40);
            }
        }, "Unconscious");
    }

    public void Decay() {
        MultiThread.TaskRun(() -> {
            radiusMessage("§c「一度なにかに縋ってしまうと、それ以外何も見えなくなってしまうのは良くないところです」", SoundList.DungeonTrigger);
            Manager.CastSkill(true);
            MultiThread.sleepTick(30);
            Manager.CastSkill(false);

            for (Player player : PlayerList.getNearNonDead(entity.getLocation(), 96)) {
                PlayerData.playerData(player).EffectManager.addEffect(EffectType.Decay, 320);
            }

            for (int i = 0; i < 8; i++) {
                for (Player player : PlayerList.getNearNonDead(entity.getLocation(), 96)) {
                    if (!RayTrace.rayLocationEntity(player.getEyeLocation(), 100, 0, entity -> entity == this.entity).isHitEntity()) {
                        Damage.makeDamage(this.entity, player, DamageCause.MAT, "Decay", 100, 1, 0.5, true);
                        sendMessage(player, "§c[崩壊]により致死ダメージを受けました", SoundList.Nope);
                    }
                }
                MultiThread.sleepTick(40);
            }
        }, "Decay");
    }

    public void CantLook() {
        MultiThread.TaskRun(() -> {
            radiusMessage("§c「ときに同調圧力は、文明を滅ぼします」", SoundList.DungeonTrigger);
            Manager.CastSkill(true);
            MultiThread.sleepTick(30);
            Manager.CastSkill(false);

            for (Player player : PlayerList.getNearNonDead(entity.getLocation(), 96)) {
                PlayerData.playerData(player).EffectManager.addEffect(EffectType.CantLook, 320);
            }

            for (int i = 0; i < 8; i++) {
                for (Player player : PlayerList.getNearNonDead(entity.getLocation(), 96)) {
                    if (RayTrace.rayLocationEntity(player.getEyeLocation(), 100, 2, entity -> entity == this.entity).isHitEntity()) {
                        Damage.makeDamage(this.entity, player, DamageCause.MAT, "CantLook", 100, 1, 0.5, true);
                        sendMessage(player, "§c[見堪]により致死ダメージを受けました", SoundList.Nope);
                    }
                }
                MultiThread.sleepTick(40);
            }
        }, "CantLook");
    }

    public void JustHistory(int CastTime) {
        MultiThread.TaskRun(() -> {
            radiusMessage("§c「本当にこれが、ただの歴史だと言えるのでしょうか？」", SoundList.DungeonTrigger);
            Manager.CastSkill(true);
            ParticleData particleData = new ParticleData(Particle.FIREWORK);
            double radius = 15;

            int i = 0;
            while (plugin.isEnabled() && Manager.enemyData.isAlive() && !Manager.setCancel && i < CastTime) {
                ParticleManager.CircleParticle(particleData, location, radius, 48);
                i += Manager.period;
                MultiThread.sleepTick(Manager.period);
            }

            Set<Player> victims = PlayerList.getNearNonDead(location, 96);
            for (Player player : PlayerList.getNearNonDead(location, radius)) {
                sendMessage(player, "§c[ただの歴史]による死を回避しました", SoundList.Tick);
                victims.remove(player);
            }
            victims.removeAll(PlayerList.getNearNonDead(location, radius));
            for (Player player : victims) {
                sendMessage(player, "§c[ただの歴史]により死を迎えました", SoundList.Nope);
                PlayerData.playerData(player).dead();
            }

            MultiThread.sleepTick(10);
            Manager.CastSkill(false);
        }, "JustHistory");
    }

    private void Potential() {
        MultiThread.TaskRun(() -> {
            radiusMessage("§c「引き込まれてしまうものほど、危険なものなことが多いです」", SoundList.DungeonTrigger);
            Manager.CastSkill(true);
            ParticleData particleData = new ParticleData(Particle.LAVA);
            ParticleData particleData2 = new ParticleData(Particle.EXPLOSION_EMITTER);
            double radius = 8;

            for (int i = 0; i < 15; i++) {
                Set<LivingEntity> victims = Function.NearEntityByEnemy(entity.getLocation(), radius);
                victims.remove(Manager.enemyData.target);
                for (LivingEntity victim : victims) {
                    particleData2.spawn(victim.getEyeLocation());
                    Damage.makeDamage(entity, victim, DamageCause.MAT, "Potential", 7, 1);
                }
                for (int i2 = 0; i2 < 4; i2++) {
                    ParticleManager.CircleParticle(particleData, entity.getLocation(), radius, 24);
                    MultiThread.sleepTick(5);
                }
            }

            Manager.CastSkill(false);
        }, "Potential");
    }

    private void PastFacts() {
        MultiThread.TaskRun(() -> {
            radiusMessage("§c「結局のところ、全てはただの歴史です」", SoundList.DungeonTrigger);
            Manager.CastSkill(true);
            ParticleData particleData = new ParticleData(Particle.DUST);
            ParticleData particleData2 = new ParticleData(Particle.EXPLOSION_EMITTER);
            double radius = 15;

            int i = 0;
            while (plugin.isEnabled() && Manager.enemyData.isAlive() && !Manager.setCancel && i < 30) {
                ParticleManager.CircleParticle(particleData, location, radius, 48);
                i += Manager.period;
                MultiThread.sleepTick(Manager.period);
            }

            ParticleManager.CircleParticle(particleData2, location, radius, 48);
            Set<Player> victims = PlayerList.getNearNonDead(location, 96);
            Set<Player> victims2 = PlayerList.getNearNonDead(location, radius);
            victims.removeAll(victims2);
            for (Player player : victims) {
                sendMessage(player, "§c[過去の事実]による死を回避しました", SoundList.Tick);
            }
            for (Player player : victims2) {
                sendMessage(player, "§c[過去の事実]により死を迎えました", SoundList.Nope);
                PlayerData.playerData(player).dead();
            }

            MultiThread.sleepTick(10);
            Manager.CastSkill(false);
        }, "PastFacts");
    }
}
