package swordofmagic7.Mob;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class MobData {
    public String Id;
    public List<String> Lore;
    public String Display;
    public EntityType entityType;
    public Disguise disguise;
    public Material Icon;
    public double ColliderSize;
    public boolean Glowing;
    public boolean Invisible = false;
    public boolean NoAI = false;
    public double Health;
    public double ATK;
    public double DEF;
    public double ACC;
    public double EVA;
    public double CriticalRate;
    public double CriticalResist;
    public double Exp;
    public double Mov;
    public double Reach;
    public double Search;
    public boolean Hostile = false;
    public boolean DamageRanking = false;
    public List<MobSkillData> SkillList = new ArrayList<>();
    public List<DropItemData> DropItemTable = new ArrayList<>();
    public List<DropRuneData> DropRuneTable = new ArrayList<>();
    public List<Double> HPStop = new ArrayList<>();
    public EnemyType enemyType = EnemyType.Normal;
    public int Size;
    public boolean isHide = false;
    public boolean NonTame = false;
}
