package swordofmagic7.Pet;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Equipment.EquipmentCategory;
import swordofmagic7.Equipment.EquipmentSlot;
import swordofmagic7.Mob.MobManager;
import swordofmagic7.RayTrace.Ray;
import swordofmagic7.RayTrace.RayTrace;
import swordofmagic7.Sound.SoundList;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Predicate;

import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Function.Log;
import static swordofmagic7.Function.sendMessage;
import static swordofmagic7.Sound.CustomSound.playSound;

public class PetManager {
    public final static HashMap<UUID, PetParameter> PetSummonedList = new HashMap<>();
    public final static String ReqPetSelect = "§e[ペット]§aを選択してください";
    public final static String ReqCommandPetSelect = "§a指揮する§e[ペット]§aを選択してください";
    public final static String ReqAttackTarget = "§c[攻撃対象]§aを選択してください";

    public static Predicate<Entity> PredicatePet(Player player) {
        return entity -> entity != player && isPet(entity) && PetParameter(entity).player == player;
    }

    public static boolean isPet(Entity entity) {
        if (entity == null) return false;
        return PetSummonedList.containsKey(entity.getUniqueId());
    }

    public static PetParameter PetParameter(Entity entity) {
        if (isPet(entity)) {
            return PetSummonedList.get(entity.getUniqueId());
        }
        Log("§cNon-PetParameter: " + entity.getName(), true);
        return new PetParameter();
    }

    private final Player player;
    private final PlayerData playerData;

    public PetManager(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
    }

    public void PetSelect() {
        Ray ray = RayTrace.rayLocationEntity(player.getEyeLocation(), 24, 1, PetManager::isPet);
        if (ray.isHitEntity())
        PetSelect(ray.HitEntity);
    }
    public void PetSelect(LivingEntity entity) {
        PlayerData playerData = playerData(player);
        if (PetManager.isPet(entity)) {
            PetParameter pet = PetManager.PetParameter(entity);
            if (pet.player == player) {
                playerData.setPetSelect(pet);
                playerData.getPetSelect().entity = entity;
                sendMessage(player, "§e[" + pet.petData.Display + "]§aを選択しました§b[" + pet.getSummonId() + "]", SoundList.Click);
            }
        } else {
            sendMessage(player, ReqPetSelect, SoundList.Nope);
        }
    }

    public boolean usingBaton() {
        PlayerData playerData = playerData(player);
        if (playerData.Equipment.isWeaponEquip()) {
            EquipmentCategory category = playerData.Equipment.getEquip(EquipmentSlot.MainHand).itemEquipmentData.EquipmentCategory;
            return category == EquipmentCategory.Baton;
        }
        return false;
    }

    public void PetAISelect() {
        PlayerData playerData = playerData(player);
        PetParameter pet = playerData.getPetSelect();
        if (pet != null && PetManager.isPet(pet.entity)) {
            switch (pet.AIState) {
                case Follow -> {
                    pet.AIState = PetAIState.Attack;
                }
                case Attack -> {
                    pet.AIState = PetAIState.Support;
                    pet.target = null;
                }
                case Support -> {
                    pet.AIState = PetAIState.Follow;
                    pet.target = null;
                }
            }
            sendMessage(player, "§e[" + pet.petData.Display + "]§aに§b[" + pet.AIState.Display + "]§aを指示しました§b[" + pet.getSummonId() + "]", SoundList.Click);
        } else {
            sendMessage(player, ReqCommandPetSelect, SoundList.Nope);
        }
    }

    public void PetAITarget() {
        PlayerData playerData = playerData(player);
        PetParameter pet = playerData.getPetSelect();
        if (pet != null) {
            if (pet.AIState == PetAIState.Attack) {
                Ray ray = RayTrace.rayLocationEntity(player.getEyeLocation(), 24, 1, playerData.Skill.SkillProcess.Predicate());
                if (ray.isHitEntity()) {
                    String Display = null;
                    if (ray.HitEntity instanceof Player target) {
                        Display = target.getDisplayName();
                    } else if (MobManager.isEnemy(ray.HitEntity)) {
                        Display = MobManager.EnemyTable(ray.HitEntity.getUniqueId()).mobData.Display;
                    } else if (PetManager.isPet(ray.HitEntity)) {
                        Display = PetManager.PetParameter(ray.HitEntity).petData.Display;
                    } else {
                        sendMessage(player, ReqAttackTarget, SoundList.Nope);
                    }
                    if (Display != null) {
                        pet.target = ray.HitEntity;
                        player.sendMessage("§c[" + Display + "]§aを§c[攻撃対象]§aにしました§b[" + pet.getSummonId() + "]");
                        playSound(player, SoundList.Click);
                    }
                } else {
                    sendMessage(player, ReqAttackTarget, SoundList.Nope);
                }
            }
        } else {
            sendMessage(player, ReqCommandPetSelect, SoundList.Nope);
        }
    }
}