package swordofmagic7.Data;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.somrpg.swordofmagic7.SomCore;
import net.somrpg.swordofmagic7.TaskUtils;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import swordofmagic7.Attribute.Attribute;
import swordofmagic7.Attribute.AttributeType;
import swordofmagic7.classes.ClassData;
import swordofmagic7.classes.Classes;
import swordofmagic7.Data.Type.DamageLogType;
import swordofmagic7.Data.Type.DropLogType;
import swordofmagic7.Data.Type.StrafeType;
import swordofmagic7.Data.Type.ViewInventoryType;
import swordofmagic7.Effect.EffectManager;
import swordofmagic7.Effect.EffectOwnerType;
import swordofmagic7.Effect.EffectType;
import swordofmagic7.Equipment.Equipment;
import swordofmagic7.Equipment.EquipmentSlot;
import swordofmagic7.Function;
import swordofmagic7.HotBar.HotBar;
import swordofmagic7.HotBar.HotBarData;
import swordofmagic7.InstantBuff.InstantBuff;
import swordofmagic7.Inventory.*;
import swordofmagic7.Item.ItemExtend.ItemPotionType;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Item.ItemStackData;
import swordofmagic7.Item.RuneParameter;
import swordofmagic7.Item.Upgrade;
import swordofmagic7.Life.Gathering;
import swordofmagic7.Life.LifeStatus;
import swordofmagic7.Life.LifeType;
import swordofmagic7.Map.MapData;
import swordofmagic7.Map.MapManager;
import swordofmagic7.Menu.Data;
import swordofmagic7.Menu.Menu;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Particle.ParticleData;
import swordofmagic7.Particle.ParticleManager;
import swordofmagic7.Party.PartyData;
import swordofmagic7.Pet.PetEvolution;
import swordofmagic7.Pet.PetManager;
import swordofmagic7.Pet.PetParameter;
import swordofmagic7.PlayerList;
import swordofmagic7.Quest.QuestManager;
import swordofmagic7.Shop.AccessoryShop;
import swordofmagic7.Shop.PetShop;
import swordofmagic7.Shop.RuneShop;
import swordofmagic7.Shop.Shop;
import swordofmagic7.Skill.CastType;
import swordofmagic7.Skill.Skill;
import swordofmagic7.Sound.SoundList;
import swordofmagic7.Status.Status;
import swordofmagic7.Title.TitleManager;
import swordofmagic7.Tutorial;
import swordofmagic7.viewBar.SideBarToDo.SideBarToDo;
import swordofmagic7.viewBar.ViewBar;

import java.io.File;
import java.time.Duration;
import java.util.*;

import static swordofmagic7.classes.Classes.maxSlot;
import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Function.*;
import static net.somrpg.swordofmagic7.SomCore.instance;
import static swordofmagic7.Sound.CustomSound.playSound;
import static swordofmagic7.Title.TitleManager.DefaultTitle;

public class PlayerData {
    public static final HashMap<Player, PlayerData> playerData = new HashMap<>();
    public synchronized static PlayerData playerData(Player player) {
        if (player.isOnline()) {
            if (!playerData.containsKey(player)) {
                playerData.put(player, new PlayerData(player));
            }
            return playerData.get(player);
        }
        Log("§c" + player.getName() + "§c, " + player.getUniqueId() + " is Offline or Npc", true);
        return playerData.get(player);
    }

    public static void remove(Player player) {
        playerData.keySet().removeIf(key -> key.getName().equals(player.getName()));
    }

    public void remove() {
        playerData.keySet().removeIf(key -> key.getName().equals(player.getName()));
    }

    public static HashMap<Player, PlayerData> getPlayerData() {
        return playerData;
    }

    public static boolean ContainPlayer(Player player) {
        return playerData.containsKey(player);
    }

    public final Player player;
    public swordofmagic7.Inventory.ItemInventory ItemInventory;
    public HotBar HotBar;
    public swordofmagic7.Inventory.RuneInventory RuneInventory;
    public swordofmagic7.Inventory.PetInventory PetInventory;
    public Equipment Equipment;
    public Status Status;
    public Classes Classes;
    public Skill Skill;
    public Menu Menu;
    public Attribute Attribute;
    public EffectManager EffectManager;
    public Upgrade Upgrade;
    public Shop Shop;
    public RuneShop RuneShop;
    public LifeStatus LifeStatus;
    public PetManager PetManager;
    public PetShop PetShop;
    public PetEvolution PetEvolution;
    public AccessoryShop accessoryShop;
    public MapManager MapManager;
    public Gathering Gathering;
    public QuestManager QuestManager;
    public ViewBar ViewBar;
    public SideBarToDo SideBarToDo;
    public Statistics statistics;
    public TitleManager titleManager;

    public String Nick;

    public int Level = 1;
    public int Exp = 0;

    public DamageLogType DamageLog = DamageLogType.None;
    public boolean ExpLog = false;
    public DropLogType DropLog = DropLogType.None;
    public boolean PvPMode = false;
    public boolean playMode = true;
    public StrafeType StrafeMode = StrafeType.DoubleJump;
    public CastType CastMode = CastType.Hold;
    public int Mel = 10000;
    public int ViewFormat = 0;
    public int Strafe = 2;
    public MapData Map = MapList.get("Alden");
    public boolean WallKicked = false;
    public BukkitTask WallKickedTask;
    public List<PetParameter> PetSummon = new ArrayList<>();
    public List<String> ActiveTeleportGate = new ArrayList<>();
    public HashMap<ItemPotionType, Integer> PotionCoolTime = new HashMap<>();
    public int useCookCoolTime = 0;
    private PetParameter PetSelect;
    public boolean isDead = false;
    public boolean RevivalReady = false;
    public boolean FishingDisplayNum = false;
    public boolean HoloSelfView = false;
    public PartyData Party;
    public InstantBuff instantBuff;
    public boolean isLoaded = false;
    public boolean isPTChat = false;
    public LivingEntity targetEntity = null;
    public LivingEntity overrideTargetEntity = null;
    public LivingEntity otherTargetEntity = null;
    public String saveTeleportServer = null;
    public boolean NaturalMessage = true;
    public Location logoutLocation = null;
    public double RuneQualityFilter = 0d;
    public Set<String> RuneIdFilter = new HashSet<>();
    public double HealthRegenDelay = 0d;
    public int AFKTime = 0;
    public boolean interactTick = false;
    public boolean EffectLog = true;
    public boolean isPlayDungeonQuest = false;
    public boolean PetTame = true;
    public Set<String> BlockList = new HashSet<>();
    public int ParticleDensity = 100;
    public boolean DamageHolo = true;

    public boolean isAFK() {
        return AFKTime > SomCore.AFK_TIME;
    }

    public ViewInventoryType ViewInventory = ViewInventoryType.ItemInventory;

    PlayerData(Player player) {
        this.player = player;
        if (player == null) {
            Log("PlayerDataのクラス生成にエラーが発生しました");
            return;
        }
        ItemInventory = new ItemInventory(player, this);
        HotBar = new HotBar(player, this);
        RuneInventory = new RuneInventory(player, this);
        PetInventory = new PetInventory(player, this);
        Equipment = new Equipment(player, this);
        Classes = new Classes(player, this);
        Skill = new Skill(player, this, instance);
        Status = new Status(player, this, Classes, Skill);
        Menu = new Menu(player, this);
        Attribute = new Attribute(player, this);
        EffectManager = new EffectManager(player, EffectOwnerType.Player, this);
        Upgrade = new Upgrade(player, this);
        Shop = new Shop(player, this);
        RuneShop = new RuneShop(player, this);
        accessoryShop = new AccessoryShop(player, this);
        LifeStatus = new LifeStatus(player, this);
        PetManager = new PetManager(player, this);
        PetShop = new PetShop(player, this);
        PetEvolution = new PetEvolution(player, this);
        MapManager = new MapManager(player, this);
        Gathering = new Gathering(player, this);
        QuestManager = new QuestManager(player, this);
        ViewBar = new ViewBar(player, this, Status);
        SideBarToDo = new SideBarToDo(this);
        titleManager = new TitleManager(this);
        statistics = new Statistics(player, this);
        instantBuff = new InstantBuff(this);

        Nick = player.getName();

        initHologram();
        initBossBar();

        // クールダウン用タスク
        TaskUtils.runTaskTimerAsync(count -> {
            if (useCookCoolTime > 0) useCookCoolTime--;
            for (Map.Entry<ItemPotionType, Integer> entry : PotionCoolTime.entrySet()) {
                PotionCoolTime.merge(entry.getKey(), -1, Integer::sum);
            }
            PotionCoolTime.entrySet().removeIf(entry -> entry.getValue() <= 0);
            MultiThread.sleepTick(20);
            return playerWhileCheck(this);
        }, 0, 20);

        //MultiThread.TaskRun(this::sendMenuPacket, "UserMenuPacket");
    }

    public Hologram hologram;
    public String holoTitle;
    public int HoloWait = 0;
    public int HoloAnim = 0;

    public void sendMenuPacket() {
        ArrayList<WrapperPlayServerSetSlot> packets = new ArrayList<>();
        packets.add(new WrapperPlayServerSetSlot(0, 0, 1,
                SpigotConversionUtil.fromBukkitItemStack(Data.UserMenu_ItemInventory)
        ));
        packets.add(new WrapperPlayServerSetSlot(0, 0, 2,
                SpigotConversionUtil.fromBukkitItemStack(Data.UserMenu_RuneInventory)
        ));
        packets.add(new WrapperPlayServerSetSlot(0, 0, 3,
                SpigotConversionUtil.fromBukkitItemStack(Data.UserMenu_PetInventory)
        ));
        packets.add(new WrapperPlayServerSetSlot(0, 0, 4,
                SpigotConversionUtil.fromBukkitItemStack(Data.UserMenu_HotBar)
        ));
        for (WrapperPlayServerSetSlot packet : packets) {
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
        }
    }

    private void initHologram() {
        MultiThread.TaskRunSynchronized(() -> {
            if (hologram != null && !hologram.isDisabled()) hologram.delete();
            hologram = SomCore.instance.createHologram(playerHoloLocation());
            if (!HoloSelfView) hologram.setHidePlayer(player);
            DHAPI.addHologramLine(hologram, DefaultTitle.Display[0]);
            DHAPI.addHologramLine(hologram, "NameTag");
            DHAPI.addHologramLine(hologram, "HealthBar");

            TaskUtils.runTaskTimerAsync(count -> {
                if (hologram.isDefaultVisibleState()) {
                    hologram.getShowPlayers().clear();
                    hologram.getHidePlayers().clear();

                    if (HoloSelfView && !isAFK()) hologram.setShowPlayer(player);
                    else hologram.setHidePlayer(player);
                    Set<Player> nonViewer = PlayerList.getNear(player.getLocation(), 64+1);
                    nonViewer.removeAll(PlayerList.getNearNonAFK(player.getLocation(), 16));
                    nonViewer.addAll(BlockListAtPlayer());
                    if (Party != null) for (Player player : Party.Members) {
                        if (!playerData(player).isAFK()) nonViewer.remove(player);
                    }
                    for (Player player : nonViewer) {
                        hologram.setHidePlayer(player);
                    }
                }
                return playerWhileCheck(this);
            }, 0, 30);

            TaskUtils.runTaskTimerAsync(count -> {
                if (titleManager.Title.flame > 1) {
                    if (titleManager.Title.flame - 1 > HoloAnim) {
                        HoloWait++;
                        if (HoloWait > titleManager.Title.waitTick[HoloAnim]) {
                            HoloWait = 0;
                            HoloAnim++;
                        }
                    } else {
                        HoloWait++;
                        if (HoloWait > titleManager.Title.waitTick[HoloAnim]) {
                            HoloWait = 0;
                            HoloAnim = 0;
                        }
                    }
                    holoTitle = titleManager.Title.Display[HoloAnim];
                } else {
                    holoTitle = titleManager.Title.Display[0];
                }

                if (holoTitle != null) {
                    DHAPI.setHologramLine(hologram, 0, holoTitle);
                }
                DHAPI.moveHologram(hologram, playerHoloLocation());

                return playerWhileCheck(this);
            }, 0, 1);
        }, "HologramInitialize");
    }

    public BossBar BossBarTargetInfo = BossBar.bossBar(Component.text(), 1, BossBar.Color.RED, BossBar.Overlay.PROGRESS);
    public BossBar BossBarOther = BossBar.bossBar(Component.text(), 1, BossBar.Color.RED, BossBar.Overlay.PROGRESS);
    public BossBar BossBarTimer = BossBar.bossBar(Component.text(), 1, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS);
    public BossBar BossBarSkillProgress = BossBar.bossBar(Component.text(), 0, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);

    public boolean bossBarInitialized = false;

    public void initBossBar() {
        player.showBossBar(BossBarTargetInfo);
        bossBarInitialized = true;
    }

    public void updateBossbar() {
        LivingEntity entity = overrideTargetEntity != null ? overrideTargetEntity : targetEntity;
        if (entity != null && !entity.isDead()) {
            player.showBossBar(BossBarTargetInfo);
            float percent = (float) Math.min(Math.max(entity.getHealth()/Function.getMaxHealth(entity), 0), 1);
            BossBarTargetInfo.name(Component.text("§c§l" + entity.getName() + " §e§l[HP:" + String.format("%.2f", percent*100) + "%]"));
            BossBarTargetInfo.progress(percent);
        } else {
            player.hideBossBar(BossBarTargetInfo);
        }
        if (otherTargetEntity != null && !otherTargetEntity.isDead()) {
            player.showBossBar(BossBarOther);
            float percent = (float) Math.min(Math.max(otherTargetEntity.getHealth()/Function.getMaxHealth(otherTargetEntity), 0), 1);
            BossBarOther.name(Component.text("§c§l" + otherTargetEntity.getName() + " §e§l[HP:" + String.format("%.2f", percent*100) + "%]"));
            BossBarOther.progress(percent);
        } else if (otherTargetEntity != null && otherTargetEntity.isDead()) {
            otherTargetEntity = null;
            player.hideBossBar(BossBarOther);
        }
    }

    public Location playerHoloLocation() {
        Location loc = player.getEyeLocation().clone();
        loc.setY(loc.getY()+1.3);
        return loc;
    }

    public boolean isBlockPlayer(Player player) {
        return BlockList.contains(player.getUniqueId().toString());
    }

    public boolean isBlockFromPlayer(Player player) {
        return PlayerData.playerData(player).BlockList.contains(this.player.getUniqueId().toString());
    }

    public Set<String> BlockListFromOther = new HashSet<>();
    private boolean nextUpdateBlockPlayer = false;
    public synchronized void updateBlockPlayer() {
        if (nextUpdateBlockPlayer) return;
        nextUpdateBlockPlayer = true;
        MultiThread.TaskRunSynchronizedLater(() -> {
            for (Player player2 : PlayerList.get()) {
                String uuid = player.getUniqueId().toString();
                PlayerData targetData = PlayerData.playerData(player2);
                if (isBlockPlayer(player2) && !Skill.SkillProcess.Predicate().test(player2)) {
                    player.hidePlayer(instance, player2);
                    player2.hidePlayer(instance, player);
                    targetData.BlockListFromOther.add(uuid);
                } else if (!isBlockFromPlayer(player2) && !targetData.hideFlag) {
                    player.showPlayer(instance, player2);
                    player2.showPlayer(instance, player);
                    targetData.BlockListFromOther.remove(uuid);
                }
            }
            nextUpdateBlockPlayer = false;
        }, 5);
    }

    public Set<String> BlockListAtString() {
        Set<String> list = new HashSet<>();
        list.addAll(BlockList);
        list.addAll(BlockListFromOther);
        return list;
    }

    public Set<Player> BlockListAtPlayer() {
        Set<Player> list = new HashSet<>();
        for (Player player : PlayerList.get()) {
            String uuid = player.getUniqueId().toString();
            if (BlockList.contains(uuid)) list.add(player);
            if (BlockListFromOther.contains(uuid)) list.add(player);
        }
        return list;
    }

    public String getNick() {
        return getNick(false);
    }

    public String getNick(boolean bold) {
        String prefix = "§e";
        if (bold) prefix += "§l";
        return prefix + Nick;
    }

    public void changeViewFormat() {
        if (ViewFormat < 3) setViewFormat(ViewFormat+1);
        else setViewFormat(0);
    }

    public void DamageLog() {
        switch (DamageLog) {
            case None -> DamageLog(DamageLogType.DamageOnly);
            case DamageOnly -> DamageLog(DamageLogType.Detail);
            case Detail -> DamageLog(DamageLogType.All);
            case All -> DamageLog(DamageLogType.None);
        }
    }

    void DamageLog(DamageLogType bool) {
        DamageLog = bool;
        String msg = "§c[ダメージログ]§aを§b[" + DamageLog.Display + "]§aにしました";
        sendMessage(player, msg, SoundList.CLICK);
    }

    public void ExpLog() {
        ExpLog(!ExpLog);
    }

    void ExpLog(boolean bool) {
        ExpLog = bool;
        String msg = "§e[経験値ログ]§aを" + (bool ? "§b[有効]" : "§c[無効]") + "§aにしました";
        sendMessage(player, msg, SoundList.CLICK);
    }

    public void DropLog() {
        switch (DropLog) {
            case None -> DropLog(DropLogType.All);
            case All -> DropLog(DropLogType.Item);
            case Item -> DropLog(DropLogType.Rune);
            case Rune -> DropLog(DropLogType.Rare);
            case Rare -> DropLog(DropLogType.None);
        }
    }

    void DropLog(DropLogType bool) {
        DropLog = bool;
        String msg = "§e[ドロップログ]§aを§b[" + DropLog.Display + "]§aにしました";
        sendMessage(player, msg, SoundList.CLICK);
    }

    public void PvPMode() {
        PvPMode(!PvPMode);
    }

    void PvPMode(boolean bool) {
        PvPMode = bool;
        String msg = "§e[PvPモード]§aを" + (bool ? "§b[有効]" : "§c[無効]") + "§aにしました";
        Status.StatusUpdate();
        updateBlockPlayer();
        sendMessage(player, msg, SoundList.CLICK);
    }

    public void DamageHolo() {
        DamageHolo(!DamageHolo);
    }

    void DamageHolo(boolean bool) {
        DamageHolo = bool;
        String msg = "§e[ダメージホログラム]§aを" + (bool ? "§b[有効]" : "§c[無効]") + "§aにしました";
        sendMessage(player, msg, SoundList.CLICK);
    }

    public void PetTame() {
        PetTame(!PetTame);
    }

    void PetTame(boolean bool) {
        PetTame = bool;
        String msg = "§e[懐柔モード]§aを" + (bool ? "§b[有効]" : "§c[無効]") + "§aにしました";
        sendMessage(player, msg, SoundList.CLICK);
    }

    public void EffectLog() {
        EffectLog(!EffectLog);
    }

    void EffectLog(boolean bool) {
        EffectLog = bool;
        String msg = "§e[効果ログ]§aを" + (bool ? "§b[有効]" : "§c[無効]") + "§aにしました";
        Status.StatusUpdate();
        sendMessage(player, msg, SoundList.CLICK);
    }

    public void NaturalMessage() {
        NaturalMessage(!NaturalMessage);
    }

    void NaturalMessage(boolean bool) {
        NaturalMessage = bool;
        String msg = "§e[当たり前条件メッセージ]§aを" + (bool ? "§b[有効]" : "§c[無効]") + "§aにしました";
        sendMessage(player, msg, SoundList.CLICK);
    }

    public void StrafeMode() {
        switch (StrafeMode) {
            case DoubleJump -> StrafeMode(StrafeType.AirDash);
            case AirDash -> StrafeMode(StrafeType.All);
            case All -> StrafeMode(StrafeType.DoubleJump);
        }
    }

    void StrafeMode(StrafeType mode) {
        StrafeMode = mode;
        String msg = "§e[ストレイフ条件]§aを§b[" + StrafeMode.Display + "]§aにしました";
        sendMessage(player, msg, SoundList.CLICK);
    }

    public void CastMode() {
        switch (CastMode) {
            case Renewed -> CastMode(CastType.Legacy);
            case Legacy -> CastMode(CastType.Hold);
            case Hold -> CastMode(CastType.Renewed);
        }
    }

    void CastMode(CastType bool) {
        CastMode = bool;
        String msg = "§e[キャストモード]§aを§b[" + CastMode.Display + "]§aにしました";
        sendMessage(player, msg, SoundList.CLICK);
    }

    public void FishingDisplayNum() {
        FishingDisplayNum(!FishingDisplayNum);
    }

    void FishingDisplayNum(boolean bool) {
        FishingDisplayNum = bool;
        String msg = "§e[釣獲コンボ表記]§aを" + (bool ? "§b[数字]" : "§c[アルファベット]") + "§aにしました";
        sendMessage(player, msg, SoundList.CLICK);
    }

    public void FishingUseCombo() {
        FishingUseCombo(!Gathering.FishingUseCombo);
    }

    void FishingUseCombo(boolean bool) {
        if (Gathering.FishingInProgress) {
            sendMessage(player, "§e釣獲中§aは切り替えできません", SoundList.NOPE);
            return;
        }
        Gathering.FishingUseCombo = bool;
        String msg = "§e[釣獲モード]§aを" + (bool ? "§b[エンドレス]" : "§c[タイムアタック]") + "§aにしました";
        sendMessage(player, msg, SoundList.CLICK);
    }

    public void HoloSelfView() {
        HoloSelfView(!HoloSelfView, true);
    }

    void HoloSelfView(boolean bool, boolean message) {
        HoloSelfView = bool;
        if (bool) hologram.setShowPlayer(player);
        else hologram.setHidePlayer(player);
        if (message) {
            String msg = "§e[自視点ステータスバー]§aを" + (bool ? "§b[表示]" : "§c[非表示]") + "§aにしました";
            sendMessage(player, msg, SoundList.CLICK);
        }
    }

    public int ParticleDensityCache = 0;
    public void ParticleDensity() {
        if (ParticleDensity > 0) {
            ParticleDensity(ParticleDensity-10);
        } else ParticleDensity(100);
    }

    void ParticleDensity(int density) {
        ParticleDensity = density;
        String msg = "§c[パーティクル密度]§aを§b[" + ParticleDensity + "%]§aにしました";
        sendMessage(player, msg, SoundList.CLICK);
    }

    public String ViewFormat() {
        return "%." + ViewFormat + "f";
    }

    public void setViewFormat(int ViewFormat) {
        this.ViewFormat = ViewFormat;
        String msg = "§e表記小数桁数§aを§e[" + ViewFormat + "桁]§aに§e設定§aしました";
        sendMessage(player, msg, SoundList.CLICK);
        viewUpdate();
    }

    public String viewExpPercent() {
        return String.format("%.3f", (float)Exp/ swordofmagic7.classes.Classes.reqExp(Level) * 100);
    }

    public boolean isPvPModeNonMessage() {
        if (PvPMode) sendMessage(player,"§c[PvP中]§aは使用できません", SoundList.NOPE);
        return PvPMode;
    }

    public void saveCloseInventory() {
        MultiThread.TaskRunSynchronized(() -> {
            CloseInventory(player);
            Bukkit.getScheduler().runTaskLater(instance, this::save, 2);
        });
    }

    public void addPlayerLevel(int addLevel) {
        Level += addLevel;
        if (Level > MaxLevel) {
            Level = MaxLevel;
            Exp = 0;
        } else {
            changeHealth(Status.MaxHealth);
            changeMana(Status.MaxMana);
            BroadCast(getNick() + "§aさんが§eLv" + Level + "§aになりました", true);
            Attribute.addPoint(addLevel * 5);
            if (Level == MaxLevel) Exp = 0;
            playSound(player, SoundList.LEVEL_UP);
        }
    }

    public static final int MaxLevel = 40;

    public synchronized void addPlayerExp(int addExp) {
        Exp += addExp;
        if (swordofmagic7.classes.Classes.reqExp(Level) <= Exp) {
            int addLevel = 0;
            while (swordofmagic7.classes.Classes.reqExp(Level) <= Exp) {
                Exp -= swordofmagic7.classes.Classes.reqExp(Level);
                addLevel++;
            }
            if (Level >= 60) ItemInventory.addItemParameter(getItemParameter("レベル報酬箱Lv60"), 1);
            else if (Level >= 50) ItemInventory.addItemParameter(getItemParameter("レベル報酬箱Lv50"), 1);
            else if (Level >= 30) ItemInventory.addItemParameter(getItemParameter("レベル報酬箱Lv30"), 1);
            else if (Level >= 10) ItemInventory.addItemParameter(getItemParameter("レベル報酬箱Lv10"), 1);
            if (Level < MaxLevel) addPlayerLevel(addLevel);
        }
        if (ExpLog) player.sendMessage("§e経験値[キャラ]§7: §a+" + addExp + " §7(" + String.format(format, (double) addExp/ swordofmagic7.classes.Classes.reqExp(Level)*100) + "%)");
    }

    private boolean isNonSave = false;
    public void save() {
        if (isNonSave) return;
        /*
        if (Tutorial.TutorialProcess.containsKey(player) && Level == 1) {
            player.sendMessage(Tutorial.TutorialNonSave);
            return;
        }
        */
        File playerFile = new File(DataBasePath, "PlayerData/" + player.getUniqueId() + ".yml");
        if (!playerFile.exists()) {
            try {
                playerFile.createNewFile();
            } catch (Exception e) {
                Bukkit.getConsoleSender().sendMessage("Error creating " + playerFile.getName() + "!");
            }
        }
        FileConfiguration data = YamlConfiguration.loadConfiguration(playerFile);

        if (statistics.playTime < data.getInt("Statistics.PlayTime")) {
            player.sendMessage("§eロールバック§aを検知したため§eデータ保護§aのため§bロビ－§aに転送しました");
            Log("§cロールバック検知: §f" + player.getName() + ", " + player.getUniqueId(), true);
            isNonSave = true;
            if (player.isOnline()) teleportServer(player, "Lobby");
            return;
        }

        if (Menu.SmithEquipment.MaterializationCache[0] != null) {
            ItemInventory.addItemParameter(Menu.SmithEquipment.MaterializationCache[0], 1);
            Menu.SmithEquipment.MaterializationCache[0] = null;
        }
        if (Upgrade.UpgradeCache[0] != null) {
            ItemInventory.addItemParameter(Upgrade.UpgradeCache[0], 1);
            Upgrade.UpgradeCache[0] = null;
        }
        if (RuneShop.RuneCache != null) {
            ItemInventory.addItemParameter(RuneShop.RuneCache, 1);
            RuneShop.RuneCache = null;
        }
        for (int i = 0; i < 1; i++) {
            if (RuneShop.RuneUpgradeCache[i] != null) {
                RuneInventory.addRuneParameter(RuneShop.RuneUpgradeCache[i]);
                RuneShop.RuneUpgradeCache[i] = null;
            }
            if (PetShop.PetSyntheticCache[i] != null) {
                PetInventory.addPetParameter(PetShop.PetSyntheticCache[i]);
                PetShop.PetSyntheticCache[i] = null;
            }
        }

        Location lastLocation;
        if (isPlayDungeonQuest) {
            lastLocation = player.getWorld().getSpawnLocation();
        } else lastLocation = Objects.requireNonNullElseGet(logoutLocation, () -> player.getLocation().clone());
        lastLocation.add(0, 1, 0);
        data.set("Location.x", lastLocation.getX());
        data.set("Location.y", lastLocation.getY());
        data.set("Location.z", lastLocation.getZ());
        data.set("Location.yaw", lastLocation.getYaw());
        data.set("Location.pitch", lastLocation.getPitch());

        data.set("Mel", Mel);
        data.set("Health", Status.Health);
        data.set("Mana", Status.Mana);
        data.set("Map", Map.Id);
        data.set("Nick", Nick);

        data.set("Setting.DamageHolo", DamageHolo);
        data.set("Setting.DamageLog", DamageLog.toString());
        data.set("Setting.ExpLog", ExpLog);
        data.set("Setting.DropLog", DropLog.toString());
        data.set("Setting.PvPMode", PvPMode);
        data.set("Setting.CastMode", CastMode.toString());
        data.set("Setting.EffectLog", EffectLog);
        data.set("Setting.StrafeMode", StrafeMode.toString());
        data.set("Setting.ShopAmountReset", Shop.AmountReset);
        data.set("Setting.ViewFormat", ViewFormat);
        data.set("Setting.FishingDisplayNum", FishingDisplayNum);
        data.set("Setting.FishingUseCombo", Gathering.FishingUseCombo);
        if (HoloSelfView) data.set("Setting.HoloSelfView", "VISIBLE");
        else data.set("Setting.HoloSelfView", "HIDDEN");
        data.set("Others.FishingCombo", Gathering.FishingComboBoost);
        data.set("Others.FishingSetCombo", Gathering.FishingSetCombo);
        data.set("Setting.PlayMode", playMode);
        data.set("Setting.ViewFormat", ViewFormat);
        data.set("Setting.ParticleDensity", ParticleDensity);
        data.set("Setting.NaturalMessage", NaturalMessage);
        data.set("Setting.RuneFilter.Quality", RuneQualityFilter);
        data.set("Setting.RuneFilter.Id", new ArrayList<>(RuneIdFilter));
        data.set("Setting.PetTame", PetTame);
        data.set("Setting.Inventory.ViewInventory", ViewInventory.toString());
        data.set("Setting.Inventory.ItemInventorySort", ItemInventory.Sort.toString());
        data.set("Setting.Inventory.RuneInventorySort", RuneInventory.Sort.toString());
        data.set("Setting.Inventory.PetInventorySort", PetInventory.Sort.toString());
        data.set("Setting.Inventory.ItemInventorySortReverse", ItemInventory.SortReverse);
        data.set("Setting.Inventory.RuneInventorySortReverse", RuneInventory.SortReverse);
        data.set("Setting.Inventory.PetInventorySortReverse", PetInventory.SortReverse);
        data.set("BlockList", new ArrayList<>(BlockList));

        data.set("Title.List", new ArrayList<>(titleManager.TitleList));
        data.set("Title.Select", titleManager.Title.Id);

        data.set("ActiveTeleportGate", ActiveTeleportGate);

        for (LifeType type : LifeType.values()) {
            data.set("Life." + type + "Level", LifeStatus.getLevel(type));
            data.set("Life." + type + "Exp", LifeStatus.getExp(type));
        }

        data.set("Level", Level);
        data.set("Exp", Exp);

        for (Map.Entry<String, ClassData> classData : getClassList().entrySet()) {
            data.set("ClassData." + classData.getKey() + ".Level", Classes.getClassLevel(classData.getValue()));
            data.set("ClassData." + classData.getKey() + ".Exp", Classes.getClassExp(classData.getValue()));
        }

        for (int i = 0; i <= maxSlot -1; i++) {
            if (Classes.classSlot[i] != null) {
                data.set("Class.Slot" + i, Classes.classSlot[i].Id);
            } else {
                data.set("Class.Slot" + i, "None");
            }
        }

        data.set("Attribute.Point", Attribute.getAttributePoint());
        for (AttributeType attr : AttributeType.values()) {
            data.set("Attribute." + attr.toString(), Attribute.getAttribute(attr));
        }

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            data.set("Inventory." + slot.toString(), new ItemParameterStack(Equipment.getEquip(slot)).toString());
        }
        List<String> itemList = new ArrayList<>();
        for (ItemParameterStack stack : ItemInventory.getList()) {
            itemList.add(stack.toString());
        }
        data.set("Inventory.ItemList", itemList);

        List<String> runeList = new ArrayList<>();
        for (RuneParameter rune : RuneInventory.getList()) {
            runeList.add(rune.toString());
        }
        data.set("Inventory.RuneList", runeList);

        List<String> petList = new ArrayList<>();
        for (PetParameter pet : PetInventory.getList()) {
            petList.add(pet.toString());
        }
        data.set("Inventory.PetList", petList);

        List<String> hotBarList = new ArrayList<>();
        for (HotBarData hotBarData : HotBar.getHotBar()) {
            if (hotBarData != null) {
                hotBarList.add(hotBarData.toString());
            } else {
                hotBarList.add("None");
            }
        }
        data.set("Inventory.HotBar", hotBarList);

        List<String> skillCT = new ArrayList<>();
        List<String> potionCT = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : Skill.SkillCoolTime.entrySet()) {
            skillCT.add(entry.getKey() + "," + entry.getValue());
        }
        for (Map.Entry<ItemPotionType, Integer> entry : PotionCoolTime.entrySet()) {
            potionCT.add(entry.getKey().toString() + "," + entry.getValue());
        }
        data.set("CoolTime.Skill", skillCT);
        data.set("CoolTime.Potion", potionCT);
        data.set("CoolTime.Cook", useCookCoolTime);

        statistics.save(data);

        try {
            data.save(playerFile);
            player.sendMessage("§eプレイヤデータ§aの§bセーブ§aが完了しました");
            MultiThread.TaskRunSynchronizedLater(() -> {
                if (saveTeleportServer != null) {
                    isNonSave = true;
                    Function.teleportServer(player, saveTeleportServer);
                }
                MultiThread.TaskRunSynchronizedLater(() -> {
                    if (player.isOnline() && saveTeleportServer != null) {
                        saveTeleportServer = null;
                        isNonSave = false;
                        player.sendMessage("§eチャンネル§aの移動に失敗しました");
                    }
                }, 20);
            }, 5);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load() {
        File playerFile = new File(DataBasePath, "PlayerData/" + player.getUniqueId() + ".yml");
        if (playerFile.exists()) {
            FileConfiguration data = YamlConfiguration.loadConfiguration(playerFile);

            Mel = data.getInt("Mel", 10000);
            Status.Health = data.getDouble("Health", -1);
            Status.Mana = data.getDouble("Mana", -1);
            Map = getMapData(data.getString("Map", "Alden"));
            Nick = data.getString("Nick", player.getName());

            DamageHolo = data.getBoolean("Setting.DamageHolo", true);
            DamageLog = DamageLogType.fromString(data.getString("Setting.DamageLog"));
            ExpLog = data.getBoolean("Setting.ExpLog", false);
            DropLog = DropLogType.fromString(data.getString("Setting.DropLog"));
            CastMode = CastType.valueOf(data.getString("Setting.CastMode", "Renewed"));
            EffectLog = data.getBoolean("Setting.EffectLog", true);
            StrafeMode = StrafeType.fromString(data.getString("Setting.StrafeMode"));
            Shop.AmountReset = data.getBoolean("Setting.ShopAmountReset");
            PvPMode = data.getBoolean("Setting.PvPMode", false);
            FishingDisplayNum = data.getBoolean("Setting.FishingDisplayNum", false);
            HoloSelfView = data.getString("Setting.HoloSelfView", "HIDDEN").equals("VISIBLE");
            Gathering.FishingComboBoost = data.getInt("Others.FishingCombo", 0);
            Gathering.FishingSetCombo = data.getInt("Others.FishingSetCombo", 0);
            Gathering.FishingUseCombo = data.getBoolean("Setting.FishingUseCombo", true);
            playMode = data.getBoolean("Setting.PlayMode", true);
            ViewFormat = data.getInt("Setting.ViewFormat",0);
            ParticleDensity = data.getInt("Setting.ParticleDensity",100);
            NaturalMessage = data.getBoolean("Setting.NaturalMessage",true);
            RuneQualityFilter = data.getDouble("Setting.RuneFilter.Quality",0d);
            RuneIdFilter = new HashSet<>(data.getStringList("Setting.RuneFilter.Id"));
            RuneIdFilter.removeIf(runeId -> !RuneList.containsKey(runeId));
            PetTame = data.getBoolean("Setting.PetTame",true);
            ViewInventory = ViewInventoryType.valueOf(data.getString("Setting.Inventory.ViewInventory","ItemInventory"));
            ItemInventory.Sort = ItemSortType.valueOf(data.getString("Setting.Inventory.ItemInventorySort","Name"));
            RuneInventory.Sort = RuneSortType.valueOf(data.getString("Setting.Inventory.RuneInventorySort","Name"));
            PetInventory.Sort = PetSortType.valueOf(data.getString("Setting.Inventory.PetInventorySort","Name"));
            ItemInventory.SortReverse = data.getBoolean("Setting.Inventory.ItemInventorySortReverse",false);
            RuneInventory.SortReverse = data.getBoolean("Setting.Inventory.RuneInventorySortReverse",false);
            PetInventory.SortReverse = data.getBoolean("Setting.Inventory.PetInventorySortReverse",false);
            BlockList = new HashSet<>(data.getStringList("BlockList"));

            titleManager.TitleList = new HashSet<>(data.getStringList("Title.List"));
            titleManager.TitleList.removeIf(title -> !TitleDataList.containsKey(title));
            titleManager.Title = TitleDataList.getOrDefault(data.getString("Title.Select"), DefaultTitle);

            ActiveTeleportGate = data.getStringList("ActiveTeleportGate");

            for (LifeType type : LifeType.values()) {
                LifeStatus.setLevel(type, data.getInt("Life." + type + "Level", 1));
                LifeStatus.setExp(type, data.getInt("Life." + type + "Exp", 0));
            }

            Level = data.getInt("Level", 1);
            Exp = data.getInt("Exp", 1);

            for (Map.Entry<String, ClassData> classData : getClassList().entrySet()) {
                Classes.setClassLevel(classData.getValue(), data.getInt("ClassData." + classData.getKey() + ".Level"));
                Classes.setClassExp(classData.getValue(), data.getInt("ClassData." + classData.getKey() + ".Exp"));
            }

            for (int i = 0; i <= maxSlot -1; i++) {
                String id = data.getString("Class.Slot" + i, "None");
                if (!id.equalsIgnoreCase("None"))
                    Classes.classSlot[i] = getClassData(id);
            }

            Attribute.setPoint(data.getInt("Attribute.Point"));
            for (AttributeType attr : AttributeType.values()) {
                Attribute.setAttribute(attr, data.getInt("Attribute." + attr.toString()));
            }

            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemParameter param = ItemParameterStack.fromString(data.getString("Inventory." + slot.toString(), "None")).itemParameter;
                if (!param.isEmpty()) Equipment.Equip(slot, param);
            }

            List<String> itemList = data.getStringList("Inventory.ItemList");
            ItemInventory.clear();
            for (String itemData : itemList) {
                ItemParameterStack stack = ItemParameterStack.fromString(itemData);
                if (!stack.isEmpty()) ItemInventory.addItemParameter(stack);
            }

            List<String> runeList = data.getStringList("Inventory.RuneList");
            RuneInventory.clear();
            for (String runeData : runeList) {
                RuneParameter rune = RuneParameter.fromString(runeData);
                if (!rune.isEmpty()) RuneInventory.addRuneParameter(rune);
            }

            List<String> petList = data.getStringList("Inventory.PetList");
            PetInventory.clear();
            for (String petData : petList) {
                PetParameter pet = new PetParameter(player, this, petData);
                PetInventory.addPetParameter(pet);
            }

            List<String> hotBarList = data.getStringList("Inventory.HotBar");
            int i = 0;
            for (String hotBar : hotBarList) {
                HotBar.setHotBar(i, HotBarData.fromString(hotBar));
                i++;
            }

            for (String str : data.getStringList("CoolTime.Skill")) {
                String[] split = str.split(",");
                Skill.SkillCoolTime.put(split[0], Integer.valueOf(split[1]));
                Skill.SkillStack.put(split[0], 0);
            }
            for (String str : data.getStringList("CoolTime.Potion")) {
                String[] split = str.split(",");
                PotionCoolTime.put(ItemPotionType.valueOf(split[0]), Integer.valueOf(split[1]));
            }
            useCookCoolTime = data.getInt("CoolTime.Cook");

            if (playMode) {
                viewUpdate();
                World world = player.getWorld();
                double x = data.getDouble("Location.x", SpawnLocation.getX());
                double y = data.getDouble("Location.y", SpawnLocation.getY());
                double z = data.getDouble("Location.z", SpawnLocation.getZ());
                float yaw = (float) data.getDouble("Location.yaw", SpawnLocation.getYaw());
                float pitch = (float) data.getDouble("Location.pitch", SpawnLocation.getPitch());
                Location loc = new Location(world, x, y, z, yaw, pitch);
                player.teleportAsync(loc);
                player.setGameMode(GameMode.SURVIVAL);
            } else {
                player.setGameMode(GameMode.CREATIVE);
            }

            statistics.load(data);
        } else {
            // 初回ログイン
            MultiThread.TaskRunSynchronizedLater(() -> {
                ItemInventory.addItemParameter(DataBase.getItemParameter("ノービスブレード"), 1);
                ItemInventory.addItemParameter(DataBase.getItemParameter("ノービスメイス"), 1);
                ItemInventory.addItemParameter(DataBase.getItemParameter("ノービスロッド"), 1);
                ItemInventory.addItemParameter(DataBase.getItemParameter("ノービスアクトガン"), 1);
                ItemInventory.addItemParameter(DataBase.getItemParameter("ノービスシールド"), 1);
                ItemInventory.addItemParameter(DataBase.getItemParameter("ノービストリンケット"), 1);
                ItemInventory.addItemParameter(DataBase.getItemParameter("ノービスアーマー"), 1);
                player.teleport(SpawnLocation);
                // Tutorial.tutorialTrigger(player, 0);
            }, 10, "TutorialTrigger");
        }
        MultiThread.TaskRunSynchronizedLater(() -> {
            isLoaded = true;
            MultiThread.TaskRunSynchronizedLater(() -> {
                Status.StatusUpdate();
                ViewBar.tickUpdate();
                Status.Health = Status.MaxHealth;
                Status.Mana = Status.MaxMana;
                viewUpdate();
            }, 20, "LoadUpdate");
        }, 5);
    }

    public static String booleanToTextOrder(boolean bool) {
        if (bool) return "降順";
        else return "昇順";
    }

    ItemStack UserMenuIcon() {
        List<String> Lore = new ArrayList<>();
        Lore.add("§a§lユーザーメニューを開きます");
        Lore.add("§a§lシフトクリックでインベントリ表示を");
        Lore.add("§a§l瞬時に切り替えることが出来ます");
        Lore.add(decoText("§3§lインベントリ表示"));
        Lore.add(decoLore("§e§lインベントリ表示") + ViewInventory.Display);
        if (ViewInventory.isItem()) {
            Lore.add(decoLore("§e§lインベントリ容量") + ItemInventory.getList().size() + "/" + ItemInventory.MaxSlot);
            Lore.add(decoLore("§e§lソート方法/順") + ItemInventory.Sort.Display + "/" + booleanToTextOrder(ItemInventory.SortReverse));
        } else if (ViewInventory.isRune()) {
            Lore.add(decoLore("§e§lインベントリ容量") + RuneInventory.getList().size() + "/" + RuneInventory.MaxSlot);
            Lore.add(decoLore("§e§lソート方法/順") + RuneInventory.Sort.Display + "/" + booleanToTextOrder(RuneInventory.SortReverse));
        } else if (ViewInventory.isPet()) {
            Lore.add(decoLore("§e§lペットケージ容量") + PetInventory.getList().size() + "/" + PetInventory.MaxSlot);
            Lore.add(decoLore("§e§lソート方法/順") + PetInventory.Sort.Display + "/" + booleanToTextOrder(PetInventory.SortReverse));
        }
        Lore.add("§c§l※統合版は選択した後インベントリを閉じるとメニューが開きます");
        return new ItemStackData(Material.BOOK, decoText("§e§lユーザーメニュー"), Lore).view();
    }

    private boolean isNextViewUpdate = false;
    public synchronized void viewUpdate() {
        if (isNextViewUpdate) return;
        isNextViewUpdate = true;
        MultiThread.TaskRunLater(() -> {
            if (playMode) {
                switch (ViewInventory) {
                    case ItemInventory -> ItemInventory.viewInventory();
                    case RuneInventory -> RuneInventory.viewRune();
                    case PetInventory -> PetInventory.viewPet();
                    case HotBar -> HotBar.viewTop();
                }
                HotBar.viewBottom();
                Equipment.viewEquip();
                player.getInventory().setItem(26, UserMenuIcon());
                player.getInventory().setItem(17, UpScrollItem);
                player.getInventory().setItem(35, DownScrollItem);
                sendMenuPacket();
            }
            isNextViewUpdate = false;
        }, 1, "NextViewUpdate");
    }

    public void setView(ViewInventoryType ViewInventory) {
        setView(ViewInventory, true);
    }

    public void setView(ViewInventoryType ViewInventory, boolean log) {
        this.ViewInventory = ViewInventory;
        if (log) player.sendMessage("§eインベントリ表示§aを§e[" + ViewInventory.Display + "]§aに切り替えました");
        viewUpdate();
        Tutorial.tutorialTrigger(player, 1);
    }

    public void changeHealth(double health) {
        if (Status.Health+health > Status.MaxHealth) {
            Status.Health = Status.MaxHealth;
        } else if (Status.Health+health < 0) {
            dead();
        } else {
            Status.Health += health;
        }
    }

    public BukkitTask ShieldTask;
    public void changeShield(double shield, int time) {
        if (Status.Shield <= shield) {
            stopShieldTask();
            Status.Shield = shield;
            ShieldTask = MultiThread.TaskRunLater(() -> Status.Shield = 0, time, "Shield");
        }
    }
    public void stopShieldTask() {
        if (ShieldTask != null) ShieldTask.cancel();
    }



    public void setHealth(double health) {
        Status.Health = health;
        changeHealth(0);
    }

    public void changeMana(double mana) {
        if (Status.Mana+mana > Status.MaxMana) {
            Status.Mana = Status.MaxMana;
        } else if (Status.Mana+mana < 0) {
            Status.Mana = 0;
        } else {
            Status.Mana += mana;
        }
    }

    public void setMana(double mana) {
        Status.Mana = mana;
        changeMana(0);
    }

    private boolean RightClickHold = false;
    private BukkitTask RightClickHoldTask;

    public void setRightClickHold() {
        RightClickHold = true;
        if (CastMode.isRenewed()) HotBar.UpdateHotBar();
        setRightClickHoldTask();
    }

    void setRightClickHoldTask() {
        if (RightClickHoldTask != null) RightClickHoldTask.cancel();
        RightClickHoldTask = Bukkit.getScheduler().runTaskLater(instance, () -> {
            if (player.isHandRaised() || player.isBlocking()) {
                setRightClickHoldTask();
            } else {
                RightClickHold = false;
                if (CastMode.isRenewed()) HotBar.UpdateHotBar();
            }
        }, 10);
    }

    public boolean isRightClickHold() {
        return RightClickHold || player.isHandRaised() || player.isBlocking();
    }

    public BukkitTask showHideTask;
    public boolean hideFlag = false;
    public void showHide(int time) {
        showHideTask();
        MultiThread.TaskRunSynchronized(() -> {
            hideFlag = true;
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.hidePlayer(instance, this.player);
            }
        });
        showHideTask = MultiThread.TaskRunSynchronizedLater(() -> {
            hideFlag = false;
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.showPlayer(instance, this.player);
            }
        }, time);
    }
    public void showHideTask() {
        if (showHideTask != null) showHideTask.cancel();
    }

    private double DPS = 0;
    public int getDPS() {
        DPS = Math.max(0, DPS);
        return (int) Math.floor(DPS/10);
    }
    public void addDPS(double dps) {
        MultiThread.TaskRun(() -> {
            for (int i = 0 ; i < 4; i++) {
                DPS += dps/4;
                MultiThread.sleepTick(5);
            }
            MultiThread.sleepTick(180);
            for (int i = 0 ; i < 4; i++) {
                DPS -= dps/4;
                MultiThread.sleepTick(5);
            }
        }, "DPS");
    }

    public void setPetSelect(PetParameter pet) {
        PetSelect = pet;
    }

    public PetParameter getPetSelect() {
        if (PetSelect != null) {
            if (PetSelect.entity == null) {
                PetSelect = null;
            }
            return PetSelect;
        }
        return null;
    }

    public PetParameter getPetSelectCheckTarget() {
        if (getPetSelect() == null) {
            return null;
        } else if (PetSelect.target == null) {
            return null;
        } else {
            return PetSelect;
        }
    }

    private BukkitTask TargetEntityTask;
    public void setTargetEntity(LivingEntity entity) {
        targetEntity = entity;
        if (TargetEntityTask != null) TargetEntityTask.cancel();
        TargetEntityTask = MultiThread.TaskRunLater(() -> targetEntity = null, 100, "TargetEntityTask");
    }

    public void revival() {
        if (isDead) {
            RevivalReady = true;
        }
    }

    public int deadTime = 0;
    public synchronized void dead() {
        if (EffectManager.hasEffect(EffectType.ShadowPool)) {
            sendMessage(player, "§e[" + EffectType.ShadowPool.Display + "]§aの効果により§c死§aを防ぎました", SoundList.TICK);
            return;
        }
        if (!isDead) {
            isDead = true;
            final Location LastDeadLocation = player.getLocation();
            for (Player player : PlayerList.getNear(LastDeadLocation, 48)) {
                sendMessage(player, getNick() + "§aさんが§cダウン§aしました...");
            }
            MultiThread.TaskRunSynchronized(() -> {
                statistics.DownCount++;
                logoutLocation = player.getWorld().getSpawnLocation();
                player.setGameMode(GameMode.SPECTATOR);
                player.showTitle(Title.title(
                        Component.text("§4§lYou Are Dead"),
                        Component.text(""),
                        Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(10), Duration.ofSeconds(1))
                ));
                deadTime = 1200;
                Hologram hologram = SomCore.instance.createHologram(player.getEyeLocation());
                DHAPI.addHologramLine(hologram, this.hologram.getPage(0).getLine(1).getText());
                ItemStack head = ItemStackPlayerHead(player);
                head.setAmount(1);
                DHAPI.addHologramLine(hologram, head);
                new BukkitRunnable() {
                    final ParticleData particleData = new ParticleData(Particle.END_ROD, 0.1f);
                    @Override
                    public void run() {
                        deadTime -= 10;
                        if (deadTime <= 0) {
                            this.cancel();
                            logoutLocation = null;
                            isDead = false;
                            player.teleportAsync(player.getWorld().getSpawnLocation());
                            player.setGameMode(GameMode.SURVIVAL);
                            Status.Health = Status.MaxHealth;
                            Status.Mana = Status.MaxMana;
                            player.resetTitle();
                            Map = getMapData("Alden");
                            hologram.delete();
                            statistics.DeathCount++;
                        } else if (RevivalReady) {
                            this.cancel();
                            logoutLocation = null;
                            isDead = false;
                            RevivalReady = false;
                            player.teleportAsync(LastDeadLocation);
                            player.setGameMode(GameMode.SURVIVAL);
                            player.resetTitle();
                            hologram.delete();
                            statistics.RevivalCount++;
                        } else {
                            LastDeadLocation.setPitch(player.getLocation().getPitch());
                            LastDeadLocation.setYaw(player.getLocation().getYaw());
                            player.teleportAsync(LastDeadLocation);
                            ParticleManager.RandomVectorParticle(particleData, Function.playerHipsLocation(player), 10);
                            MultiThread.TaskRun(() -> {
                                for (int i = 0; i < 10; i++) {
                                    player.setVelocity(new Vector());
                                    MultiThread.sleepTick(1);
                                }
                            }, "PlayerDeadTick");
                            if (deadTime < 1100) {
                                player.showTitle(Title.title(
                                        Component.text("§4§lYou Are Dead"),
                                        Component.text("§e§lスニークでリスポーン"),
                                        Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ZERO)
                                ));
                            }
                        }
                    }
                }.runTaskTimer(instance, 0, 10);
            }, "PlayerDead");
        }
    }

}
