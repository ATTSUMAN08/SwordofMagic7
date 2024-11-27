package swordofmagic7.Skill;

import net.kyori.adventure.text.Component;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;
import swordofmagic7.Classes.ClassData;
import swordofmagic7.Classes.Classes;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Equipment.EquipmentCategory;
import swordofmagic7.Equipment.EquipmentSlot;
import swordofmagic7.Item.RuneParameter;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Pet.PetAIState;
import swordofmagic7.Skill.SkillClass.Alchemist.Alchemist;
import swordofmagic7.Skill.SkillClass.*;
import swordofmagic7.Sound.SoundList;
import swordofmagic7.Tutorial;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static swordofmagic7.Data.DataBase.getSkillData;
import static swordofmagic7.Function.*;
import static swordofmagic7.Menu.Data.SkillMenuDisplay;
import static swordofmagic7.Pet.PetManager.ReqAttackTarget;
import static swordofmagic7.Sound.CustomSound.playSound;

public class Skill {
    private final Plugin plugin;
    public final Player player;
    public final PlayerData playerData;
    private boolean CastReady = true;
    public SkillProcess SkillProcess;
    public float SkillCastProgress = 0f;
    public final HashMap<String, Integer> SkillCoolTime = new HashMap<>();
    private final HashMap<String, Integer> SkillLevel = new HashMap<>();
    public final HashMap<String, Integer> SkillStack = new HashMap<>();
    int SkillPoint = 0;
    public static final int millis = 50;

    public final Novice novice;
    public final Swordman swordman;
    public final Mage mage;
    public final Gunner gunner;
    public final Cleric cleric;
    public final Tamer tamer;
    public final Priest priest;
    public final Peltast peltast;
    public final BulletMarker bulletMarker;
    public final Elementalist elementalist;
    public final Doppelsoeldner doppelsoeldner;
    public final Pardoner pardoner;
    public final Chronomancer chronomancer;
    public final Alchemist alchemist;
    public final Sheriff sheriff;
    public final Ranger ranger;
    public final DualStar dualStar;
    public final Assassin assassin;
    public final Sage sage;
    public final Highlander highlander;
    public final Oracle oracle;
    public final Corsair corsair;
    public final OutLaw outLaw;
    public final Kabbalist kabbalist;
    public final Cryomancer cryomancer;
    public final PlagueDoctor plagueDoctor;
    public final Barbarian barbarian;
    public final Matador matador;
    public final Shadowmancer shadowmancer;

    public Alchemist getAlchemist() {
        return alchemist;
    }

    public Skill(Player player, PlayerData playerData, Plugin plugin) {
        this.player = player;
        this.playerData = playerData;
        this.plugin = plugin;
        SkillProcess = new SkillProcess(this);

        novice = new Novice(SkillProcess);
        swordman = new Swordman(SkillProcess);
        mage = new Mage(SkillProcess);
        gunner = new Gunner(SkillProcess);
        cleric = new Cleric(SkillProcess);
        tamer = new Tamer(SkillProcess);
        priest = new Priest(SkillProcess);
        peltast = new Peltast(SkillProcess);
        bulletMarker = new BulletMarker(SkillProcess);
        elementalist = new Elementalist(SkillProcess);
        doppelsoeldner = new Doppelsoeldner(SkillProcess);
        pardoner = new Pardoner(SkillProcess);
        chronomancer = new Chronomancer(SkillProcess);
        sheriff = new Sheriff(SkillProcess);
        ranger = new Ranger(SkillProcess);
        dualStar = new DualStar(SkillProcess);
        assassin = new Assassin(SkillProcess);
        sage = new Sage(SkillProcess);
        highlander = new Highlander(SkillProcess);
        oracle = new Oracle(SkillProcess);
        corsair = new Corsair(SkillProcess);
        outLaw = new OutLaw(SkillProcess);
        kabbalist = new Kabbalist(SkillProcess);
        cryomancer = new Cryomancer(SkillProcess);
        plagueDoctor = new PlagueDoctor(SkillProcess);
        barbarian = new Barbarian(SkillProcess);
        matador = new Matador(SkillProcess);
        shadowmancer = new Shadowmancer(SkillProcess);

        alchemist = new Alchemist(SkillProcess);

        MultiThread.TaskRun(() -> {
            while (playerWhileCheck(playerData)) {
                for (Map.Entry<String, Integer> data : SkillCoolTime.entrySet()) {
                    String key = data.getKey();
                    int cooltime = data.getValue()-1;
                    SkillCoolTime.put(key, cooltime);
                    if (cooltime <= 0) SkillStack.put(key, getSkillData(key).Stack);
                }
                SkillCoolTime.entrySet().removeIf(entry -> entry.getValue() <= 0);
                if (SkillProcess.normalAttackCoolTime > 0) SkillProcess.normalAttackCoolTime--;
                MultiThread.sleepTick(1);
            }
        }, "SkillCoolTimeTask");
    }

    public void setCastReady(boolean bool) {
        CastReady = bool;
        SkillProcess.SkillCastTime = 0;
    }

    public boolean isCastReady() {
        return CastReady && !playerData.EffectManager.hasEffect(EffectType.Rigidity);
    }

    public void CastSkill(SkillData skillDataBase) {
        MultiThread.TaskRun(() -> {
            if (!SkillStack.containsKey(skillDataBase.Id)) {
                SkillStack.put(skillDataBase.Id, skillDataBase.Stack);
            }
            if (CastReady && isAlive(player) && !player.isInsideVehicle() && !playerData.isAFK()) {
                SkillData skillData = skillDataBase.clone();
                if (CategoryCheck(skillData)) {
                    if (hasSkill(skillData.Id)) {
                        if (SkillStack(skillData) > 0) {
                            if (playerData.Status.Mana >= skillData.Mana) {
                                if (!playerData.EffectManager.isSkillsNotAvailable) {
                                    Tutorial.tutorialTrigger(player, 7);
                                    if (skillData.SkillType.isPetSkill()) {
                                        if (playerData.getPetSelect() == null) {
                                            player.sendMessage("§a指揮する§e[ペット]§aを選択してください");
                                            playSound(player, SoundList.Nope);
                                            return;
                                        } else if (skillData.SkillType.isPetAttack() && playerData.getPetSelect().target == null) {
                                            player.sendMessage(ReqAttackTarget);
                                            playSound(player, SoundList.Nope);
                                            return;
                                        }
                                    }
                                    skillData.Mana = IncreasedConsumptionMana(skillData.Mana, playerData.Level);
                                    if (playerData.EffectManager.hasEffect(EffectType.ArcaneEnergy)) skillData.Mana = 0;
                                    if (hasSkill("MagicEfficiently")) {
                                        SkillData MagicEfficiently = getSkillData("MagicEfficiently");
                                        skillData.Mana = (int) Math.floor(skillData.Mana * (1 - MagicEfficiently.ParameterValue(0) / 100));
                                    }
                                    if (playerData.EffectManager.hasEffect(EffectType.Inexhaustible)) {
                                        skillData.Mana = (int) Math.floor(skillData.Mana * (1 - playerData.EffectManager.getData(EffectType.Inexhaustible).getDouble(0)));
                                    }
                                    skillData.CastTime = (int) Math.floor(skillData.CastTime * (1 / playerData.Status.SkillCastTime));
                                    skillData.RigidTime = (int) Math.floor(skillData.RigidTime * (1 / playerData.Status.SkillRigidTime));
                                    skillData.CoolTime = (int) Math.floor(skillData.CoolTime * (1 / playerData.Status.SkillCooltime));
                                    if (playerData.Map.isRaid) {
                                        switch (skillData.Id) {
                                            case "Revive" -> skillData.CoolTime = 20 * 60 * 5;
                                            case "BackMasking", "Stop" -> {
                                                sendMessage(player, "§a現在の§eマップ§aでは使用できません", SoundList.Nope);
                                                return;
                                            }
                                        }
                                    }
                                    try {
                                        switch (skillData.Id) {
                                            //ノービス
                                            case "Slash" -> novice.Slash(skillData, 5, 70);
                                            case "Vertical" -> novice.Vertical(skillData, 10, 2.5);
                                            case "Smite" -> novice.Smite(skillData, 4);
                                            case "Rain" -> novice.Rain(skillData, 5);
                                            case "DoubleTrigger" -> novice.TriggerShot(skillData, 2);
                                            case "FireBall" -> novice.FireBall(skillData);
                                            //ソードマン
                                            case "Bash" -> novice.Slash(skillData, 6, 90);
                                            case "Thrust" -> novice.Vertical(skillData, 10, 3);
                                            case "PainBarrier" -> swordman.PainBarrier(skillData);
                                            case "Feint" -> swordman.Feint(skillData);
                                            //ガンナー
                                            case "TripleTrigger" -> novice.TriggerShot(skillData, 3);
                                            case "ChargeShot" -> novice.TriggerShot(skillData, 1);
                                            case "Aiming" -> gunner.Aiming(skillData);
                                            case "Rolling" -> gunner.Rolling(skillData);
                                            //メイジ
                                            case "Infall" -> mage.Infall(skillData);
                                            case "Teleportation" -> mage.Teleportation(skillData);
                                            case "MagicMissile" -> mage.MagicMissile(skillData);
                                            //クレシック
                                            case "Heal" -> {
                                                RuneParameter rune = playerData.Equipment.equippedRune("気配り上手のルーン");
                                                if (rune != null) {
                                                    double value = rune.AdditionParameterValue(0)/100;
                                                    skillData.Parameter.get(0).Value *= value;
                                                    skillData.Mana = Math.toIntExact(Math.round(skillData.CastTime * value));
                                                    skillData.CastTime = Math.toIntExact(Math.round(skillData.CastTime * value));
                                                    skillData.RigidTime = Math.toIntExact(Math.round(skillData.RigidTime * value));
                                                    skillData.CoolTime  = Math.toIntExact(Math.round(skillData.CoolTime * value));
                                                }
                                                cleric.Heal(skillData, 30);
                                            }
                                            case "Cure" -> cleric.Cure(skillData, 30);
                                            case "Fade" -> cleric.Fade(skillData);
                                            case "Resurrection" -> cleric.Resurrection(skillData, 30);
                                            //テイマー
                                            case "PetAttack" -> tamer.PetAttack(skillData);
                                            case "PetHeal" -> tamer.PetHeal(skillData);
                                            case "PetBoost" -> tamer.PetBoost(skillData);
                                            //プリースト
                                            case "MassHeal" -> priest.MassHeal(skillData);
                                            case "Monstrance" -> priest.Monstrance(skillData);
                                            case "HolyDefense" -> priest.HolyBuff(skillData, new ParticleData(Particle.FIREWORK), EffectType.HolyDefense);
                                            case "HolyAttack" -> priest.HolyBuff(skillData, new ParticleData(Particle.DUST), EffectType.HolyAttack);
                                            case "Revive" -> priest.Revive(skillData);
                                            //ペルタスト
                                            case "RimBlow" -> novice.Slash(skillData, 4, 160);
                                            case "ShieldBash" -> peltast.ShieldBash(skillData, 12, 5);
                                            case "SwashBaring" -> peltast.SwashBaring(skillData);
                                            case "HighGuard" -> peltast.HighGuard(skillData);
                                            //エレメンタリスト
                                            case "ElementalBurst" -> elementalist.ElementalBurst(skillData);
                                            case "Heil" -> elementalist.Heil(skillData);
                                            case "FireClaw" -> elementalist.FireClaw(skillData);
                                            case "Electrocute" -> elementalist.Electrocute(skillData);
                                            case "StormDust" -> elementalist.StormDust(skillData);
                                            //バレットマーカー
                                            case "TracerBullet" -> SkillProcess.BuffApply(skillData, EffectType.TracerBullet, new ParticleData(Particle.DUST), skillData.ParameterValueInt(0) * 20);
                                            case "DoubleGunStance" -> SkillProcess.BuffApply(skillData, EffectType.DoubleGunStance, new ParticleData(Particle.DUST), skillData.ParameterValueInt(0) * 20);
                                            case "FreezeBullet" -> bulletMarker.FreezeBullet(skillData, SkillProcess);
                                            case "RestInPeace" -> bulletMarker.RestInPeace(skillData, SkillProcess);
                                            //ドッペルゾルドナー
                                            case "DeedsOfValor" -> doppelsoeldner.DeedsOfValor(skillData);
                                            case "Cyclone" -> doppelsoeldner.Cyclone(skillData);
                                            case "Zornhau" -> doppelsoeldner.ComboSkill(skillData, 6, 90, 1, null, EffectType.Zornhau);
                                            case "Zucken" -> doppelsoeldner.ComboSkill(skillData, 7, 120, 1, EffectType.Zornhau, EffectType.Zucken);
                                            case "Redel" -> doppelsoeldner.ComboSkill(skillData, 8, 160, 1, EffectType.Zucken, null);
                                            //パードナー
                                            case "Indulgence" -> pardoner.Indulgence(skillData);
                                            case "Indulgendia" -> pardoner.Indulgendia(skillData);
                                            case "Forgiveness" -> pardoner.Forgiveness(skillData, 20);
                                            case "DiscernEvil" -> pardoner.DiscernEvil(skillData);
                                            case "IncreaseMagicDef" -> priest.HolyBuff(skillData, new ParticleData(Particle.WITCH), EffectType.IncreaseMagicDef);
                                            //クロノマンサー
                                            case "Slow" -> chronomancer.Slow(skillData);
                                            case "Stop" -> chronomancer.Stop(skillData);
                                            case "Path" -> chronomancer.Path(skillData);
                                            case "TimeForward" -> chronomancer.TimeForward(skillData);
                                            case "BackMasking" -> chronomancer.BackMasking(skillData);
                                            //アルケミスト
                                            case "Alchemy" -> alchemist.AlchemyView();
                                            //シェリフ
                                            case "QuickDraw" -> novice.TriggerShot(skillData, 1);
                                            case "Fanning" -> sheriff.Fanning(skillData);
                                            case "HeadShot" -> sheriff.HeadShot(skillData);
                                            case "PeaceMaker" -> sheriff.PeaceMaker(skillData);
                                            case "Redemption" -> {
                                                RuneParameter rune = playerData.Equipment.equippedRune("屈服のルーン");
                                                if (rune != null) {
                                                    skillData.CoolTime = getSkillData("PeaceMaker").CoolTime;
                                                    resetSkillCoolTime("PeaceMaker");
                                                } else sheriff.Redemption(skillData);
                                            }
                                            //アサシン
                                            case "InstantAccel" -> assassin.InstantAccel(skillData);
                                            case "HallucinationSmoke" -> assassin.HallucinationSmoke(skillData);
                                            case "PiercingHeart" -> assassin.PiercingHeart(skillData);
                                            case "Cloaking" -> assassin.Cloaking(skillData);
                                            case "Annihilation" -> assassin.Annihilation(skillData);
                                            //デュアルスター
                                            case "ExtraAttack" -> dualStar.ExtraBuff(skillData, EffectType.ExtraAttack);
                                            case "ExtraDefense" -> dualStar.ExtraBuff(skillData, EffectType.ExtraDefense);
                                            case "ExtraCritical" -> dualStar.ExtraBuff(skillData, EffectType.ExtraCritical);
                                            case "EyeSight" -> dualStar.ExtraBuff(skillData, EffectType.EyeSight);
                                            case "AttackOrder" -> dualStar.Order(skillData, PetAIState.Attack);
                                            case "SupportOrder" -> dualStar.Order(skillData, PetAIState.Support);
                                            case "FollowOrder" -> dualStar.Order(skillData, PetAIState.Follow);
                                            //レンジャー
                                            case "ChainAttack" -> ranger.ChainAttack(skillData);
                                            //セージ
                                            case "Blink" -> sage.Blink(skillData);
                                            case "DimensionCompression" -> sage.DimensionCompression(skillData);
                                            case "MicroDimension" -> sage.MicroDimension(skillData);
                                            case "UltimateDimension" -> sage.UltimateDimension(skillData);
                                            case "MissileHole" -> sage.MissileHole(skillData);
                                            //ハイランダー
                                            case "CartarStroke" -> highlander.CartarStroke(skillData);
                                            case "CrossCut" -> highlander.CrossCut(skillData);
                                            case "Crown" -> highlander.Crown(skillData);
                                            case "WagonWheel" -> highlander.WagonWheel(skillData);
                                            case "CrossGuard" -> {
                                                if (playerData.Equipment.isEquipRune("反転切りのルーン")) skillData.CoolTime = skillData.ParameterValueInt(1)*20;
                                                highlander.CrossGuard(skillData);
                                            }
                                            //オラクル
                                            case "CounterSpell" -> oracle.CounterSpell(skillData);
                                            case "ArcaneEnergy" -> oracle.ArcaneEnergy(skillData);
                                            case "DeathVerdict" -> oracle.DeathVerdict(skillData);
                                            case "Foretell" -> oracle.Foretell(skillData);
                                            case "DivineMight" -> oracle.DivineMight(skillData);
                                            //コルセア
                                            case "Brutality" -> corsair.Brutality(skillData);
                                            case "CoveringFire" -> corsair.CoveringFire(skillData);
                                            case "JollyRoger" -> corsair.JollyRoger(skillData);
                                            case "IronHook" -> corsair.IronHook(skillData);
                                            case "Keelhauling" -> corsair.Keelhauling(skillData);
                                            //アウトロー
                                            case "SprinkleSand" -> outLaw.SprinkleSand(skillData);
                                            case "BreakBrick" -> outLaw.BreakBrick(skillData);
                                            case "Bully" -> outLaw.Bully(skillData);
                                            case "FireBlindly" -> outLaw.FireBlindly(skillData);
                                            case "Rampage" -> outLaw.Rampage(skillData);
                                            //カバリスト
                                            case "Ayinsof" -> {
                                                ParticleData particleData = new ParticleData(Particle.WITCH);
                                                int time = skillData.ParameterValueInt(0) * 20;
                                                if (playerData.Equipment.isEquipRune("単体魔法陣のルーン")) {
                                                    SkillProcess.PartyBuffApply(skillData, EffectType.Ayinsof, particleData, time);
                                                } else {
                                                    SkillProcess.BuffApply(skillData, EffectType.Ayinsof, particleData, time * 2);
                                                }
                                            }
                                            case "Sevenfold" -> {
                                                EffectType effectType = playerData.Equipment.isEquipRune("ラストチャンスのルーン") ? EffectType.LastChance : EffectType.Sevenfold;
                                                SkillProcess.PartyBuffApply(skillData, effectType, new ParticleData(Particle.WITCH), skillData.ParameterValueInt(0)*20);
                                                skillData.CoolTime = 120*20;
                                            }
                                            case "Gevura" -> kabbalist.Gevura(skillData);
                                            case "Nachash" -> kabbalist.Nachash(skillData);
                                            case "TreeOfSepiroth" -> kabbalist.TreeOfSepiroth(skillData);
                                            //クリオマンサー
                                            case "FrostPillar" -> cryomancer.FrostPillar(skillData);
                                            case "IceBlast" -> cryomancer.IceBlast(skillData);
                                            case "IcePike" -> cryomancer.IcePike(skillData);
                                            case "SubzeroShield" -> cryomancer.SubzeroShield(skillData);
                                            case "SnowRolling" -> cryomancer.SnowRolling(skillData);
                                            //プレイグドクター
                                            case "BeakMask" -> SkillProcess.BuffApply(skillData, EffectType.BeakMask, new ParticleData(Particle.WITCH), skillData.ParameterValueInt(0)*20);
                                            case "Modafinil" -> plagueDoctor.Modafinil(skillData);
                                            case "HealingFactor" -> plagueDoctor.HealingFactor(skillData);
                                            case "FumiGate" -> plagueDoctor.FumiGate(skillData);
                                            case "Pandemic" -> plagueDoctor.Pandemic(skillData);
                                            //バーバリアン
                                            case "Cleave" -> barbarian.Cleave(skillData);
                                            case "Embowel" -> barbarian.Embowel(skillData);
                                            case "StompingKick" -> barbarian.StompingKick(skillData);
                                            case "Warcry" -> barbarian.Warcry(skillData);
                                            //マタドール
                                            case "Capote" -> matador.Capote(skillData);
                                            case "Faena" -> matador.Faena(skillData);
                                            case "Ole" -> matador.Ole(skillData);
                                            case "CorridaFinale" -> matador.CorridaFinale(skillData);
                                            case "Muleta" -> matador.Muleta(skillData);
                                            //シャドウマンサー
                                            case "ShadowPool" -> shadowmancer.ShadowPool(skillData);
                                            case "Hallucination" -> shadowmancer.Hallucination(skillData);
                                            case "ShadowFatter" -> shadowmancer.ShadowFatter(skillData);
                                            case "ShadowThorn" -> shadowmancer.ShadowThorn(skillData);
                                            case "ShadowCondensation" -> shadowmancer.ShadowCondensation(skillData);
                                        }
                                        MultiThread.TaskRun(() -> {
                                            player.showBossBar(playerData.BossBarSkillProgress);
                                            if (skillData.CastTime > 0) {
                                                for (int i = 0; i < skillData.CastTime; i++) {
                                                    SkillProcess.SkillCastTime++;
                                                    SkillCastProgress = (float) SkillProcess.SkillCastTime / skillData.CastTime;
                                                    playerData.BossBarSkillProgress.name(Component.text("§e" + String.format("%.0f", SkillCastProgress * 100) + "%"));
                                                    playerData.BossBarSkillProgress.progress(Math.min(Math.max(SkillCastProgress, 0), 1));
                                                    MultiThread.sleepTick(1);
                                                }
                                            } else {
                                                MultiThread.sleepTick(1);
                                                SkillCastProgress = 1f;
                                            }
                                            player.hideBossBar(playerData.BossBarSkillProgress);
                                            playerData.BossBarSkillProgress.progress(0);
                                        }, "CastTime");
                                        playerData.changeMana(-skillData.Mana);
                                        useStack(skillData);
                                        setSkillCoolTime(skillData, skillData.CoolTime);
                                    }  catch (NoClassDefFoundError e) {
                                        e.printStackTrace();
                                        sendMessage(player, "§cNoClassDefFoundErrorが発生しました。別CHへ移動してください", SoundList.Nope);
                                        resetSkillCoolTimeWaited(skillData);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        sendMessage(player, "§cスキル発動中にエラーが発生しました", SoundList.Nope);
                                        resetSkillCoolTimeWaited(skillData);
                                    }
                                } else {
                                    sendMessage(player, "§c[デバフ効果]§aによりスキルを発動できません", SoundList.Nope);
                                }
                            } else {
                                sendMessage(player, "§b[マナ]§aが足りません", SoundList.Nope);
                            }
                        } else if (playerData.NaturalMessage) {
                            sendMessage(player, "§e[" + skillData.Display + "]§aを§b[使用可能]§aまで§c[" + getSkillCoolTime(skillData) / 20f + "秒]§aです", SoundList.Nope);
                        }
                    } else {
                        sendMessage(player, "§a現在の§eクラス§aでは使用出来ないか§eクラスレベル§aが足りません", SoundList.Nope);
                    }
                }
            }
        }, "SkillCast");
    }

    void setSkillCoolTime(SkillData skillData, int time) {
        SkillCoolTime.put(skillData.Id, time);
    }

    void setSkillCoolTime(SkillData skillData) {
        SkillCoolTime.put(skillData.Id, skillData.CoolTime);
    }

    public int SkillStack(SkillData skillData) {
        return SkillStack.getOrDefault(skillData.Id, skillData.Stack);
    }

    void useStack(SkillData skillData) {
        SkillStack.put(skillData.Id, SkillStack(skillData)-1);
    }

    public void resetSkillCoolTime(String skill) {
        SkillCoolTime.remove(skill);
        SkillStack.put(skill, getSkillData(skill).Stack);
    }

    public void resetSkillCoolTime(SkillData skillData) {
        SkillCoolTime.remove(skillData.Id);
        SkillStack.put(skillData.Id, skillData.Stack);
    }

    public void resetSkillCoolTimeWaited(SkillData skillData) {
        MultiThread.TaskRunSynchronizedLater(() -> resetSkillCoolTime(skillData), 5);
    }

    public int getSkillCoolTime(SkillData skillData) {
        return SkillCoolTime.getOrDefault(skillData.Id, 0);
    }

    public boolean hasSkill(String skill) {
        SkillData skillData = DataBase.getSkillData(skill);
        for (ClassData classData : playerData.Classes.classSlot) {
            if (classData != null) {
                if (classData.SkillList.contains(skillData)) {
                    return playerData.Classes.getClassLevel(classData) >= skillData.ReqLevel;
                }
            }
        }
        return false;
    }

    void setSkillLevel(SkillData skillData, int attr) {
        SkillLevel.put(skillData.Id, attr);
    }

    void resetSkillLevel(ClassData classData) {
        SkillPoint = playerData.Classes.getClassLevel(classData) - 1;
        for (SkillData skillData : classData.SkillList) {
            SkillLevel.put(skillData.Id, 0);
        }
    }

    int getSkillLevel(SkillData skillData) {
        return SkillLevel.getOrDefault(skillData.Id, 1);
    }

    public boolean CategoryCheck(SkillData skillData) {
        boolean ReqMainHand = true;
        boolean ReqOffHand = true;
        if (skillData.ReqMainHand.size() > 0) {
            ReqMainHand = CategoryCheck(EquipmentSlot.MainHand, skillData.ReqMainHand, skillData.SkillType.isPassive());
        }
        if (skillData.ReqOffHand.size() > 0) {
            ReqOffHand = CategoryCheck(EquipmentSlot.OffHand, skillData.ReqOffHand, skillData.SkillType.isPassive());
        }
        return ReqMainHand && ReqOffHand;
    }

    public boolean CategoryCheck(EquipmentSlot slot, List<EquipmentCategory> categoryList, boolean isPassive) {
        if (categoryList.size() == 0) return true;
        boolean check = false;
        StringBuilder Display = new StringBuilder();
        for (EquipmentCategory category : categoryList) {
            if (Display.toString().equals("")) {
                Display = new StringBuilder(category.Display);
            } else {
                Display.append(", ").append(category.Display);
            }
            if (playerData.Equipment.getEquip(slot).itemEquipmentData.equipmentCategory == category) {
                check = true;
                break;
            }
        }
        if (check) {
            return true;
        } else {
            if (!isPassive) sendMessage(player, "§aこの§e[スキル]§aの§b発動§aには§e" + slot.Display + "§aに§e[" + Display + "]§aを§e装備§aしてる§c必要§aがあります", SoundList.Nope);
            return false;
        }
    }

    private final HashMap<Integer, String> SkillMenuCache = new HashMap<>();
    public void SkillMenuView() {
        SkillMenuCache.clear();
        Inventory inv = decoInv(SkillMenuDisplay, Classes.MaxSlot);
        int slotActive = 0;
        int slotPassive = 8;
        int slot = 0;
        for (int i = 0; i < playerData.Classes.classSlot.length; i++) {
            if (playerData.Classes.classSlot[i] != null) {
                for (SkillData skill : playerData.Classes.classSlot[i].SkillList) {
                    if (skill.SkillType.isPassive()) {
                        SkillMenuCache.put(slotPassive, skill.Id);
                        inv.setItem(slotPassive, skill.view(playerData));
                        slotPassive--;
                    } else {
                        SkillMenuCache.put(slotActive, skill.Id);
                        inv.setItem(slotActive, skill.view(playerData));
                        slotActive++;
                    }
                }
                slot++;
                slotActive = slot * 9;
                slotPassive = (slot + 1) * 9 - 1;
            }
        }
        player.openInventory(inv);
    }

    public void SkillMenuClick(InventoryView view, int Slot) {
        if (equalInv(view, SkillMenuDisplay)) {
            SkillData skillData = getSkillData(SkillMenuCache.get(Slot));
            if (skillData.SkillType.isActive()) {
                playerData.Menu.Trigger.TriggerMenuView();
                playSound(player, SoundList.Click);
            } else {
                player.sendMessage("§e[" + skillData.Display + "]§aは§eパッシブスキル§aです");
                playSound(player, SoundList.Nope);
            }
        }
    }
}



