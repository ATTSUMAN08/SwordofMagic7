package net.somrpg.swordofmagic7.mob.skill

import org.bukkit.entity.LivingEntity
import swordofmagic7.Damage.Damage
import swordofmagic7.Damage.DamageCause
import swordofmagic7.Data.DataBase
import swordofmagic7.Dungeon.AusMine.AusMineB4
import swordofmagic7.Effect.EffectManager
import swordofmagic7.Effect.EffectType
import swordofmagic7.Mob.EnemyData
import swordofmagic7.Mob.EnemySkillManager
import swordofmagic7.Mob.MobManager
import swordofmagic7.Mob.Skill.EnemySkillBase
import swordofmagic7.MultiThread.MultiThread
import swordofmagic7.Particle.ParticleManager
import swordofmagic7.PlayerList

class QueenSlime(manager: EnemySkillManager) : EnemySkillBase(manager) {

    fun stickyTrap() {
        val castTime = 60 // キャスト時間 (3秒)
        val radiusBoss = 6.0 // ボスの半径
        val radiusPlayer = 3.0 // プレイヤーの半径
        val damageMultiplier = 2.0

        val origin = entity().location.clone()
        Manager.CastSkill(true)
        val playerLocations = PlayerList.getNearNonDead(origin, radius).map { it.location.clone() }.toList()
        MultiThread.TaskRun({
            var i = 0
            while (enemyData().isAlive || !Manager.setCancel) {
                if (i < castTime) {
                    ParticleManager.CircleParticle(Manager.particleCasting, origin, radiusBoss, 3.0)
                    playerLocations.forEach { playerLoc ->
                        ParticleManager.CircleParticle(Manager.particleCasting, playerLoc, radiusPlayer, 3.0)
                    }
                } else {
                    // ボスの周囲に範囲攻撃
                    val bossCircleVictimList: Set<LivingEntity> = PlayerList.getNearNonDead(entity().location, radiusBoss)
                    ParticleManager.CircleParticle(Manager.particleActivate, origin, radiusBoss, 3.0)
                    for (player in bossCircleVictimList) {
                        Damage.makeDamage(entity(), setOf(player), DamageCause.ATK, "StickyTrap", damageMultiplier, 1, 2)
                        EffectManager.addEffect(player, EffectType.Sticky, 1200, 3, null)
                    }

                    // プレイヤーがいた位置に範囲攻撃
                    for (playerLoc in playerLocations) {
                        val victimList: Set<LivingEntity> = PlayerList.getNearNonDead(playerLoc, radiusPlayer)

                        ParticleManager.CircleParticle(Manager.particleActivate, playerLoc, radiusPlayer, 3.0)
                        for (player in victimList) {
                            Damage.makeDamage(entity(), setOf(player), DamageCause.ATK, "StickyTrap", damageMultiplier, 1, 2)
                            EffectManager.addEffect(player, EffectType.Sticky, 1200, 3, null)
                        }
                    }

                    break
                }
                i += Manager.period
                MultiThread.sleepTick(Manager.period.toLong())
            }
            MultiThread.sleepTick(10)
            Manager.CastSkill(false)
        }, "StickyTrap")
    }

    fun stickySplit() {
        Manager.CastSkill(true)
        val spawnedEntities = mutableListOf<EnemyData>()
        MultiThread.TaskRun({
            effectManager().addEffect(EffectType.Invincible, 30 * 20) // 30秒間の無敵

            repeat(4) {
                spawnedEntities.add(MobManager.mobSpawn(DataBase.getMobData("ミニクイーンスライム"), 40, entity().location))
            }
            MultiThread.sleep(30000)

            var isAlive = false
            for (entity in spawnedEntities) {
                if (!entity.isDead) {
                    isAlive = true
                    entity.delete()
                }
            }

            if (isAlive) {
                val players = PlayerList.getNearNonDead(entity().location, radius)
                for (player in players) {
                    EffectManager.addEffect(player, EffectType.Sticky, 1200, 6, null)
                }
            }
            // todo ダメージ増加とか防御力低下とかノコード

            MultiThread.sleepTick(10)
            Manager.CastSkill(false)
        }, "StickySplit")
    }

    fun stickyWave() {
        // TODO
    }

    fun stickyImpact() {
        // TODO
    }
}