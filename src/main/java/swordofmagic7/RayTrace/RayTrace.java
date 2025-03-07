package swordofmagic7.RayTrace;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import swordofmagic7.Function;
import swordofmagic7.Mob.EnemyData;
import swordofmagic7.Mob.MobManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class RayTrace {

    public static Ray rayLocationBlock(Location loc, double distance, boolean ignore) {
        loc = loc.clone();
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

    public static Ray rayLocationEntity(Location loc, double distance, double size, Predicate<LivingEntity> predicate) {
        return originalRayTrace(loc, distance, size, predicate);
    }

    private static Ray originalRayTrace(Location loc, double distance, double size, Predicate<LivingEntity> predicate) {
        World world = loc.getWorld();
        List<Ray> rayDataList = new ArrayList<>();
        double distanceCheck = distance;
        for (LivingEntity entity : Function.NearLivingEntity(loc, distance, predicate)) {
            if (entity.getLocation().distance(loc) < distance) {
                BoundingBox box = entity.getBoundingBox().expand(size);
                if (MobManager.isEnemy(entity)) {
                    EnemyData enemyData = MobManager.EnemyTable(entity.getUniqueId());
                    box.expand(enemyData.mobData.ColliderSize);
                    box.expand(0, enemyData.mobData.ColliderSizeY, 0);
                }
                RayTraceResult rayData = box.rayTrace(loc.toVector(), loc.getDirection(), distance);
                if (rayData != null) {
                    Ray ray = new Ray();
                    ray.HitPosition = rayData.getHitPosition().toLocation(world);
                    ray.HitEntity = entity;
                    double distance2 = ray.HitPosition.distance(loc);
                    if (distanceCheck > distance2) {
                        distanceCheck = distance2;
                        rayDataList.addFirst(ray);
                    } else {
                        rayDataList.add(ray);
                    }
                }
            }
        }
        if (rayDataList.isEmpty()) {
            Ray ray = new Ray();
            ray.HitPosition = loc.add(loc.getDirection().multiply(distance));
            return ray;
        } else {
            return rayDataList.getFirst();
        }
    }
}

