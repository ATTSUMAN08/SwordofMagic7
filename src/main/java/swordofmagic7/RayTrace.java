package swordofmagic7;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.RayTraceResult;

import java.util.function.Predicate;

public class RayTrace {

    public Ray rayLocation(Location loc, double distance, double size, boolean ignore, Predicate predicate) {
        World world = loc.getWorld();
        RayTraceResult rayData = world.rayTrace(loc, loc.getDirection(), distance, FluidCollisionMode.NEVER, ignore, size, predicate);
        Ray ray = new Ray();
        if (rayData == null) {
            ray.HitPosition = loc.add(loc.getDirection().multiply(distance));
        } else {
            ray.HitPosition = rayData.getHitPosition().toLocation(world);
            if (rayData.getHitEntity() != null) {
                ray.HitEntity = (LivingEntity) rayData.getHitEntity();
            }
        }
        return ray;
    }

    public Ray rayLocationBlock(Location loc, double distance, boolean ignore) {
        World world = loc.getWorld();
        RayTraceResult rayData = world.rayTraceBlocks(loc, loc.getDirection(), distance, FluidCollisionMode.NEVER, ignore);
        Ray ray = new Ray();
        if (rayData == null) {
            ray.HitPosition = loc.add(loc.getDirection().multiply(distance));
        } else {
            ray.HitPosition = rayData.getHitPosition().toLocation(world);
            if (rayData.getHitBlock() != null) {
                ray.HitBlock = rayData.getHitBlock();
            }
        }
        return ray;
    }

    public Ray rayLocationEntity(Location loc, double distance, double size, Predicate<Entity> predicate) {
        World world = loc.getWorld();
        RayTraceResult rayData = world.rayTraceEntities(loc, loc.getDirection(), distance, size, predicate);
        Ray ray = new Ray();
        if (rayData == null) {
            ray.HitPosition = loc.add(loc.getDirection().multiply(distance));
        } else {
            ray.HitPosition = rayData.getHitPosition().toLocation(world);
            if (rayData.getHitBlock() != null) {
                ray.HitBlock = rayData.getHitBlock();
            }
        }
        return ray;
    }
}

class Ray {
    LivingEntity HitEntity = null;
    Location HitPosition = null;
    Block HitBlock = null;

    boolean isHitEntity() {
        return HitEntity != null;
    }
    boolean isHitBlock() {
        return HitBlock != null;
    }
}