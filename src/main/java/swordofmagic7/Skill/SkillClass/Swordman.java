package swordofmagic7.Skill.SkillClass;

import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.Skill.Skill;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Skill.SkillProcess;
import swordofmagic7.Sound.SoundList;

import static swordofmagic7.Sound.CustomSound.playSound;

public class Swordman {
    private final SkillProcess skillProcess;
    private final Player player;
    private final PlayerData playerData;
    private final Skill skill;

    public Swordman(SkillProcess skillProcess) {
        this.skillProcess = skillProcess;
        skill = skillProcess.skill;
        player = skillProcess.player;
        playerData = skillProcess.playerData;
    }

    public void PainBarrier(SkillData skillData) {
        skill.setCastReady(false);
        playerData.EffectManager.addEffect(EffectType.PainBarrier, (int) skillData.Parameter.get(0).Value*20);
        ParticleManager.CylinderParticle(new ParticleData(Particle.SPELL_WITCH), player.getLocation(), 1, 2, 3, 3);
        playSound(player, SoundList.Heal);
        skillProcess.SkillRigid(skillData);
    }

    public void Feint(SkillData skillData) {
        skill.setCastReady(false);
        playerData.EffectManager.addEffect(EffectType.Invincible, (int) skillData.Parameter.get(0).Value*20);
        ParticleManager.CylinderParticle(new ParticleData(Particle.CRIT_MAGIC), player.getLocation(), 1, 2, 3, 3);
        playSound(player, SoundList.Heal);
        skillProcess.SkillRigid(skillData);
    }
}
