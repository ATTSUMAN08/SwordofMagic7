package swordofmagic7.Skill;

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
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Skill.SkillClass.Alchemist.Alchemist;
import swordofmagic7.Skill.SkillClass.BulletMarker.FreezeBullet;
import swordofmagic7.Skill.SkillClass.BulletMarker.RestInPeace;
import swordofmagic7.Skill.SkillClass.*;
import swordofmagic7.Sound.SoundList;
import swordofmagic7.Tutorial;

import java.util.ArrayList;
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
    private final HashMap<String, Integer> SkillStack = new HashMap<>();
    int SkillPoint = 0;
    public static final int millis = 50;

    private final Novice novice;
    private final Swordman swordman;
    private final Mage mage;
    private final Gunner gunner;
    private final Cleric cleric;
    private final Tamer tamer;
    private final Priest priest;
    private final Peltast peltast;
    private final Elementalist elementalist;
    private final Doppelsoeldner doppelsoeldner;
    private final Pardoner pardoner;
    private final Chronomancer chronomancer;
    private final Alchemist alchemist;
    private final Sheriff sheriff;

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
        elementalist = new Elementalist(SkillProcess);
        doppelsoeldner = new Doppelsoeldner(SkillProcess);
        pardoner = new Pardoner(SkillProcess);
        chronomancer = new Chronomancer(SkillProcess);
        alchemist = new Alchemist(SkillProcess);
        sheriff = new Sheriff(SkillProcess);

        MultiThread.TaskRun(() -> {
            while (player.isOnline() && plugin.isEnabled()) {
                for (Map.Entry<String, Integer> data : new HashMap<>(SkillCoolTime).entrySet()) {
                    String key = data.getKey();
                    int cooltime = data.getValue()-1;
                    if (cooltime > 0) {
                        SkillCoolTime.put(key, cooltime);
                    } else {
                        SkillCoolTime.remove(key);
                        SkillStack.put(key, getSkillData(key).Stack);
                    }
                }
                if (SkillProcess.normalAttackCoolTime > 0) SkillProcess.normalAttackCoolTime--;
                MultiThread.sleepTick(1);
            }
        }, "SkillCoolTimeTask: " + player.getName());
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
            if (CastReady && isAlive(player) && !player.isInsideVehicle()) {
                SkillData skillData = skillDataBase.clone();
                if (CategoryCheck(skillData)) {
                    if (hasSkill(skillData.Id)) {
                        if (SkillStack(skillData) > 0) {
                            if (playerData.Status.Mana >= skillData.Mana) {
                                if (!playerData.EffectManager.isSkillsNotAvailable()) {
                                    Tutorial.tutorialTrigger(player, 7);
                                    if (hasSkill("MagicEfficiently")) {
                                        SkillData MagicEfficiently = getSkillData("MagicEfficiently");
                                        skillData.Mana = (int) Math.floor(skillData.Mana * (1 - MagicEfficiently.ParameterValue(0) / 100));
                                    }
                                    skillData.CastTime = (int) Math.floor(skillData.CastTime * (1 / playerData.Status.SkillCastTime));
                                    skillData.RigidTime = (int) Math.floor(skillData.RigidTime * (1 / playerData.Status.SkillRigidTime));
                                    skillData.CoolTime = (int) Math.floor(skillData.CoolTime * (1 / playerData.Status.SkillCooltime));
                                    if (skillData.SkillType.isPetSkill()) {
                                        if (playerData.PetSelect == null) {
                                            player.sendMessage("§a指揮する§e[ペット]§aを選択してください");
                                            playSound(player, SoundList.Nope);
                                            return;
                                        } else if (skillData.SkillType.isPetAttack() && playerData.PetSelect.target == null) {
                                            player.sendMessage(ReqAttackTarget);
                                            playSound(player, SoundList.Nope);
                                            return;
                                        }
                                    }
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
                                        case "Infall" -> mage.Infall(skillData, 10);
                                        case "Teleportation" -> mage.Teleportation(skillData);
                                        case "MagicMissile" -> mage.MagicMissile(skillData);
                                        //クレシック
                                        case "Heal" -> cleric.Heal(skillData, 20);
                                        case "Cure" -> cleric.Cure(skillData, 20);
                                        case "Fade" -> cleric.Fade(skillData);
                                        case "Resurrection" -> cleric.Resurrection(skillData, 20);
                                        //テイマー
                                        case "PetAttack" -> tamer.PetAttack(skillData);
                                        case "PetHeal" -> tamer.PetHeal(skillData);
                                        case "PetBoost" -> tamer.PetBoost(skillData);
                                        //プリースト
                                        case "MassHeal" -> priest.MassHeal(skillData);
                                        case "Monstrance" -> priest.Monstrance(skillData);
                                        case "HolyDefense" -> priest.HolyBuff(skillData, new ParticleData(Particle.FIREWORKS_SPARK), EffectType.HolyDefense);
                                        case "HolyAttack" -> priest.HolyBuff(skillData, new ParticleData(Particle.REDSTONE), EffectType.HolyAttack);
                                        case "Revive" -> priest.HolyBuff(skillData, new ParticleData(Particle.VILLAGER_HAPPY), EffectType.Revive);
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
                                        case "TracerBullet" -> SkillProcess.BuffApply(skillData, EffectType.TracerBullet, new ParticleData(Particle.REDSTONE), skillData.ParameterValueInt(0) * 20);
                                        case "DoubleGunStance" -> SkillProcess.BuffApply(skillData, EffectType.DoubleGunStance, new ParticleData(Particle.REDSTONE), skillData.ParameterValueInt(0) * 20);
                                        case "FreezeBullet" -> new FreezeBullet(skillData, SkillProcess);
                                        case "RestInPeace" -> new RestInPeace(skillData, SkillProcess);
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
                                        case "IncreaseMagicDef" -> priest.HolyBuff(skillData, new ParticleData(Particle.SPELL_WITCH), EffectType.IncreaseMagicDef);
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
                                        case "Redemption" -> sheriff.Redemption(skillData);
                                    }
                                    MultiThread.TaskRun(() -> {
                                        if (skillData.CastTime > 0) {
                                            while (SkillCastProgress < 1) {
                                                SkillCastProgress = (float) SkillProcess.SkillCastTime / skillData.CastTime;
                                                player.sendTitle(" ", "§e" + String.format("%.0f", SkillCastProgress * 100) + "%", 0, 10, 0);
                                                SkillProcess.SkillCastTime++;
                                                MultiThread.sleepTick(1);
                                            }
                                        } else {
                                            MultiThread.sleepMillis(10);
                                            SkillCastProgress = 1f;
                                        }
                                    }, "CastTime: " + player.getName());
                                    playerData.changeMana(-skillData.Mana);
                                    useStack(skillData);
                                    setSkillCoolTime(skillData);
                                } else {
                                    player.sendMessage("§c[デバフ効果]§aによりスキルを発動できません");
                                    playSound(player, SoundList.Nope);
                                }
                            } else {
                                player.sendMessage("§b[マナ]§aが足りません");
                                playSound(player, SoundList.Nope);
                            }
                        } else {
                            player.sendMessage("§e[" + skillData.Display + "]§aを§b[使用可能]§aまで§c[" + getSkillCoolTime(skillData) / 20f + "秒]§aです");
                            playSound(player, SoundList.Nope);
                        }
                    } else {
                        sendMessage(player, "§a現在の§eクラス構成§aでは使用できません", SoundList.Nope);
                    }
                }
            }
        }, "SkillCast: " + player.getName());
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

    private boolean CategoryCheck(EquipmentSlot slot, EquipmentCategory category) {
        List<EquipmentCategory> list = new ArrayList<>();
        list.add(category);
        return CategoryCheck(slot, list);
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
            ReqMainHand = CategoryCheck(EquipmentSlot.MainHand, skillData.ReqMainHand);
        }
        if (skillData.ReqOffHand.size() > 0) {
            ReqOffHand = CategoryCheck(EquipmentSlot.OffHand, skillData.ReqOffHand);
        }
        return ReqMainHand && ReqOffHand;
    }

    public boolean CategoryCheck(EquipmentSlot slot, List<EquipmentCategory> categoryList) {
        if (categoryList.size() == 0) return true;
        boolean check = false;
        String Display = "";
        for (EquipmentCategory category : categoryList) {
            if (Display.equals("")) {
                Display = category.Display;
            } else {
                Display += ", " + category.Display;
            }
            if (playerData.Equipment.getEquip(slot).itemEquipmentData.EquipmentCategory == category) {
                check = true;
                break;
            }
        }
        if (check) {
            return true;
        } else {
            player.sendMessage("§aこの§e[スキル]§aの§b発動§aには§e" + slot.Display + "§aに§e[" + Display + "]§aを§e装備§aしてる§c必要§aがあります");
            playSound(player, SoundList.Nope);
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
                        inv.setItem(slotPassive, skill.view());
                        slotPassive--;
                    } else {
                        SkillMenuCache.put(slotActive, skill.Id);
                        inv.setItem(slotActive, skill.view());
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



