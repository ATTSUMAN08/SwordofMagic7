package swordofmagic7.Pet;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.List;

public class PetData {
    public String Id;
    public EntityType entityType;
    public Disguise disguise;

    public String Display;
    public List<String> Lore;
    public Material Icon;
    public double MaxStamina;
    public double MaxHealth;
    public double HealthRegen;
    public double MaxMana;
    public double ManaRegen;
    public double ATK;
    public double DEF;
    public double HLP;
    public double ACC;
    public double EVA;
    public double CriticalRate;
    public double CriticalResist;
    public boolean BossPet;
}
