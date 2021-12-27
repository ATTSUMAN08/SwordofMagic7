package swordofmagic7.RayTrace;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

public class Ray {
    public LivingEntity HitEntity = null;
    public Location HitPosition = null;
    public Block HitBlock = null;

    public boolean isHitEntity() {
        return HitEntity != null;
    }

    public boolean isHitBlock() {
        return HitBlock != null;
    }
}
