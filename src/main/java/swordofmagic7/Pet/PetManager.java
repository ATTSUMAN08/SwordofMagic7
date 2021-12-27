package swordofmagic7.Pet;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Equipment.EquipmentCategory;
import swordofmagic7.Equipment.EquipmentSlot;
import swordofmagic7.Item.ItemStackData;
import swordofmagic7.Mob.MobManager;
import swordofmagic7.RayTrace.Ray;
import swordofmagic7.RayTrace.RayTrace;
import swordofmagic7.Sound.SoundList;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;

import static swordofmagic7.Data.DataBase.getPetData;
import static swordofmagic7.Data.PlayerData.playerData;
import static swordofmagic7.Function.*;
import static swordofmagic7.Menu.Data.PetShopDisplay;
import static swordofmagic7.Sound.CustomSound.playSound;

public class PetManager {
    public final static HashMap<UUID, PetParameter> PetSummonedList = new HashMap<>();

    public static Predicate<Entity> PredicatePet(Player player) {
        return entity -> entity != player && isPet((LivingEntity) entity) && PetParameter((LivingEntity) entity).player == player;
    }

    public static boolean isPet(LivingEntity entity) {
        return PetSummonedList.containsKey(entity.getUniqueId());
    }

    public static PetParameter PetParameter(LivingEntity entity) {
        if (isPet(entity)) {
            return PetSummonedList.get(entity.getUniqueId());
        }
        Log("§cNon-PetParameter: " + entity.getName(), true);
        return new PetParameter();
    }

    private Player player;
    private PlayerData playerData;

    public PetManager(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
    }

    public void PetShop() {
        Inventory inv = decoInv(PetShopDisplay, 1);
        inv.setItem(0, new ItemStackData(Material.WOLF_SPAWN_EGG, decoText("オースオオカミ"), "§a§l無料配布のペットです").view());
    }

    public void PetSelect(LivingEntity entity) {
        PlayerData playerData = playerData(player);
        if (PetManager.isPet(entity)) {
            PetParameter pet = PetManager.PetParameter(entity);
            if (pet.player == player) {
                playerData.PetSelect = pet;
                player.sendMessage("§e[" + pet.petData.Display + "]§aを選択しました");
                playSound(player, SoundList.Click);
            } else {
                player.sendMessage("§a自身の§e[ペット]§aを選択してください");
                playSound(player, SoundList.Nope);
            }
        } else {
            player.sendMessage("§e[ペット]§aを選択してください");
            playSound(player, SoundList.Nope);
        }
    }

    public boolean usingBaton() {
        PlayerData playerData = playerData(player);
        if (playerData.Equipment.isEquip(EquipmentSlot.MainHand)) {
            EquipmentCategory category = playerData.Equipment.getEquip(EquipmentSlot.MainHand).EquipmentCategory;
            if (category == EquipmentCategory.Baton) {
                return true;
            }
        }
        return false;
    }

    public void PetAISelect() {
        PlayerData playerData = playerData(player);
        if (playerData.PetSelect != null && PetManager.isPet(playerData.PetSelect.entity)) {
            PetParameter pet = playerData.PetSelect;
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
            player.sendMessage("§e[" + pet.petData.Display + "]§aに§b[" + pet.AIState.Display + "]§aを指示しました");
            playSound(player, SoundList.Click);
        } else {
            player.sendMessage("§a指揮する§e[ペット]§aを選択してください");
            playSound(player, SoundList.Nope);
        }
    }

    public void PetAITarget() {
        PlayerData playerData = playerData(player);
        if (playerData.PetSelect != null) {
            switch (playerData.PetSelect.AIState) {
                case Attack -> {
                    Ray ray = RayTrace.rayLocationEntity(player.getEyeLocation(), 24, 1, playerData.Skill.SkillProcess.PredicateE());
                    if (ray.isHitEntity()) {
                        String Display = null;
                        if (ray.HitEntity instanceof Player target) {
                            Display = target.getDisplayName();
                        } else if (MobManager.isEnemy(ray.HitEntity)){
                            Display = MobManager.EnemyTable(ray.HitEntity.getUniqueId()).mobData.Display;
                        } else if (PetManager.isPet(ray.HitEntity)) {
                            Display = PetManager.PetParameter(ray.HitEntity).petData.Display;
                        } else {
                            player.sendMessage("§c[攻撃対象]§aを選択してください");
                            playSound(player, SoundList.Nope);
                        }
                        if (Display != null) {
                            playerData.PetSelect.target = ray.HitEntity;
                            player.sendMessage("§c[" + Display + "]§aを§c[攻撃対象]§aにしました");
                            playSound(player, SoundList.Click);
                        }
                    } else {
                        player.sendMessage("§c[攻撃対象]§aを選択してください");
                        playSound(player, SoundList.Nope);
                    }
                }
            }
        } else {
            player.sendMessage("§a指揮する§e[ペット]§aを選択してください");
            playSound(player, SoundList.Nope);
        }
    }

    public void PetShopClick(InventoryView view, ItemStack currentItem) {
        if (equalInv(view, PetShopDisplay)) {
            if (currentItem.getType() == Material.WOLF_SPAWN_EGG) {
                if (playerData.PetInventory.getList().size() == 0) {
                    PetData petData = getPetData("オースオオカミ");
                    Random random = new Random();
                    PetParameter petParameter = new PetParameter(player, playerData, petData, 1, 30, 0, random.nextDouble() + 0.5);
                    playerData.PetInventory.addPetParameter(petParameter);
                    player.sendMessage("§e[" + petData.Display + "]§aを受け取りました");
                    playSound(player, SoundList.LevelUp);
                } else {
                    player.sendMessage("§aすでに§eペット§aを所持しています");
                    playSound(player, SoundList.Nope);
                }
            }
        }
    }
}