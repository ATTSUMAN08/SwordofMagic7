package swordofmagic7.Mob;

import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class MobData {
    public String Display;
    public EntityType entityType;
    public MobDisguise disguise;
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
    public boolean Hostile = false;
    public List<MobSkillData> SkillList = new ArrayList<>();
    public List<DropItemData> DropItemTable = new ArrayList<>();
    public List<DropRuneData> DropRuneTable = new ArrayList<>();
}
