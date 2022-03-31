package swordofmagic7.Particle;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.PlayerList;
import swordofmagic7.RayTrace.RayTrace;

import java.util.HashSet;
import java.util.Set;

import static swordofmagic7.System.random;

public final class ParticleManager {

    public static void onLoad() {
        MultiThread.TaskRunTimer(() -> {
            particleCount = particleCount < 0 ? 0 : particleCount -500;
        }, 1);
    }

    public static double angle(Vector vector) {
        return angle(new Vector(), vector);
    }

    public static double angle(Vector vector, Vector vector2) {
        double angle = Math.atan2(vector.getZ() - vector2.getZ(), vector.getX() - vector2.getX());
        if (angle < 0) {
            angle += 2 * Math.PI;
        }
        return Math.floor(angle * 360 / (2 * Math.PI));
    }

    public static Set<LivingEntity> FanShapedCollider(Location location, Set<LivingEntity> targetList, double angle) {
        Set<LivingEntity> Return = new HashSet<>();
        for (LivingEntity target : targetList) {
            Location location2 = target.getLocation();
            angle /= 2;
            double Angle = angle(location.getDirection());
            double Angle2 = angle(location.toVector(), location2.toVector());
            if (Angle - angle < 0) {
                double AngleAux = 360 + (Angle - angle);
                if (AngleAux <= Angle2 || Angle2 <= Angle + angle) Return.add(target);
            } else if (360 < Angle + angle) {
                double AngleAux = (Angle + angle) - 360;
                if (Angle - angle <= Angle2 || Angle2 <= AngleAux) Return.add(target);
            } else {
                if (Angle - angle <= Angle2 && Angle2 <= Angle + angle) Return.add(target);
            }
        }
        return Return;
    }

    public static Set<LivingEntity> RectangleCollider(Location location, Set<LivingEntity> targetList, double length, double width) {
        final double posX0 = -width/2;
        final double posY0 = length;
        final double posX1 = width/2;
        final double posY1 = 0;
        double angle = angle(location.getDirection());
        Set<LivingEntity> Return = new HashSet<>();
        for (LivingEntity target : targetList) {
            double distance = location.distance(target.getLocation());
            double posAngle = angle(location.toVector(), target.getLocation().toVector()) - angle;
            double posX = distance * Math.sin(posAngle * (Math.PI / 180));
            double posY = distance * Math.cos(posAngle * (Math.PI / 180));
            if (posX0 <= posX && posX <= posX1 && posY1 <= posY && posY <= posY0) {
                Return.add(target);
            }
        }
        return Return;
    }

    public static boolean LineCollider(Location locationA, Location locationA2, Location locationB, Location locationB2) {
        double x0 = locationA.getX();
        double y0 = locationA.getZ();
        double x1 = locationA2.getX();
        double y1 = locationA2.getZ();

        double x2 = locationB.getX();
        double y2 = locationB.getZ();
        double x3 = locationB2.getX();
        double y3 = locationB2.getZ();

        if (Math.abs(x1-x0) < 0.01) x1 = x0 + 0.01;
        if (Math.abs(x3-x2) < 0.01) x3 = x2 + 0.01;

        double t0 = (y1 - y0) / (x1 - x0);
        double t1 = (y3 - y2) / (x3 - x2);

        double x = 0;
        double y = 0;
        if (t0 != t1) {
             x = (y2 - y0 + t0 * x0 - t1 * x2) / (t0 - t1);
             y = t0 * (x-x0) + y0;
        }

        double r0 = (x - x0) / (x1 - x0);
        double r1 = (x - x2) / (x3 - x2);
        return 0 < r0 && r0 < 1 && 0 < r1 && r1 < 1;
    }

    private static int particleCount = 0;
    public static void spawnParticle(ParticleData particleData, Location location) {
        if (particleCount > 10000) return;
        particleCount++;
        float speed;
        Vector vector;
        Vector offset;
        if (particleData.speedRandom == ParticleData.IgnoreValue) speed = particleData.speed;
        else speed = random.nextFloat(particleData.speed, particleData.speedRandom);
        if (particleData.vector != ParticleData.RandomVector) vector = particleData.vector;
        else vector = new Vector(random.nextFloat()*2-1, random.nextFloat()*2-1, random.nextFloat()*2-1);
        if (!particleData.randomOffset) offset = new Vector(0, 0.15, 0);
        else offset = new Vector(random.nextFloat()*particleData.randomOffsetMultiply, random.nextFloat()*particleData.randomOffsetMultiply, random.nextFloat()*particleData.randomOffsetMultiply);
        Set<Player> Players = PlayerList.getNear(location, 96);
        if (particleData.particle != Particle.REDSTONE) {
            for (Player player : Players) {
                player.spawnParticle(particleData.particle, location.clone().add(offset), 0, vector.getX(), vector.getY(), vector.getZ(), speed);
            }
        } else {
            for (Player player : Players) {
                player.spawnParticle(particleData.particle, location.clone().add(offset), 0, vector.getX(), vector.getY(), vector.getZ(), speed, particleData.dustOptions);
            }
        }
    }

    public static void FanShapedParticle(ParticleData particleData, Location location, double radius, double angle, double density) {
        angle /= 2;
        double multiply = 1 / density;
        location.setPitch(0);
        Location locR = location.clone();
        Location locL = location.clone();
        locR.setYaw((float) (locR.getYaw() + angle));
        locL.setYaw((float) (locL.getYaw() - angle));
        Vector vecR = locR.getDirection().multiply(multiply);
        Vector vecL = locL.getDirection().multiply(multiply);
        for (int i = 0; i < density * radius; i++) {
            spawnParticle(particleData, locR.add(vecR));
            spawnParticle(particleData, locL.add(vecL));
        }
        Location locF = location.clone();
        locF.setYaw((float) (locF.getYaw() - angle));
        if (density/radius > 10) density = radius * 10;
        double multiply2 = 360/(angle*density);
        for (double i = 0; i < angle*2; i+=multiply2) {
            Vector vecF = locF.getDirection().multiply(radius);
            locF.setYaw((float) (locF.getYaw() + multiply2));
            spawnParticle(particleData, locF.clone().add(vecF));
        }
    }

    public static void ShapedParticle(ParticleData particleData, Location location, double length, double angle, double density, double offsetY, boolean randomLength) {
        angle /= 2;
        double multiply = angle/density;
        float maxAngle = (float) (location.getYaw()+angle);
        float minAngle = (float) (location.getYaw()-angle);
        location.setPitch(0);
        location.add(0, offsetY, 0);
        for (float yaw = minAngle; yaw < maxAngle; yaw+=multiply) {
            location.setYaw(yaw);
            Location loc = location.clone();
            double locLength = length;
            if (randomLength) locLength = (random.nextDouble()*0.9+0.1)*locLength;
            loc.add(loc.getDirection().multiply(locLength));
            spawnParticle(particleData, loc);
        }
    }

    public static void FanShapedFillAnimParticle(ParticleData particleData, Location location, double radius, double angle, double density, double Anim) {
        FanShapedParticle(particleData, location, radius, angle, density);
        if (Anim > 1) Anim = 1;
        angle /= 2;
        double multiply = 1 / density;
        location.setPitch(0);
        if (density/radius > 10) density = radius * 10;
        double multiply2 = 360/(angle*density);
        for (double anim = multiply; anim < radius * Anim; anim += multiply) {
            Location locA = location.clone();
            locA.setYaw((float) (locA.getYaw() - (angle - multiply2)));
            for (double i = 0; i < (angle - multiply2) * 2; i += multiply2) {
                Vector vecA = locA.getDirection().multiply(anim);
                locA.setYaw((float) (locA.getYaw() + multiply2));
                spawnParticle(particleData, locA.clone().add(vecA));
            }
        }
    }

    public static void CylinderParticle(ParticleData particleData, Location location, double radius, double height, double density, double density2) {
        density2 = 1/density2;
        for (double i = 0; i < height; i += density2) {
            CircleParticle(particleData, location.clone().add(0, i,0), radius, density);
        }
    }

    public static void CircleParticle(ParticleData particleData, Location location, double radius, double density) {
        if (density/radius > 10) density = radius * 10;
        density = 1/density;
        for (double i=0; i < 2 * Math.PI ; i += density) {
            double x = Math.cos(i) * radius;
            double z = Math.sin(i) * radius;
            spawnParticle(particleData, location.clone().add(x, 0 ,z));
        }
    }

    public static void CircleFillAnimParticle(ParticleData particleData, Location location, double radius, double density, double Anim) {
        CircleParticle(particleData, location, radius, density);
        double gap = 90/(density * radius);
        for (double anim = gap; anim < (radius - gap) * Anim; anim += gap) {
            CircleParticle(particleData, location, anim, density);
        }
    }

    public static void RectangleParticle(ParticleData particleData, Location location, double length, double width, double density) {
        location.setPitch(0);
        double multiply = 1/density;
        Location locR = location.clone().add(location.getDirection().rotateAroundY(90).multiply(width/2));
        Location locL = location.clone().add(location.getDirection().rotateAroundY(-90).multiply(width/2));
        Vector end = location.getDirection().multiply(length);
        Location locF = locR.clone();
        Location locF2 = locF.clone().add(end);
        double distance = locR.distance(locL);
        Vector vector = location.getDirection().multiply(multiply);
        Vector vector2 = locL.toVector().subtract(locR.toVector()).normalize().multiply(multiply);
        for (double i = multiply; i < length; i += multiply) {
            spawnParticle(particleData, locR.add(vector));
            spawnParticle(particleData, locL.add(vector));
        }
        for (double i = multiply; i < distance; i += multiply) {
            spawnParticle(particleData, locF.add(vector2));
            spawnParticle(particleData, locF2.add(vector2));
        }
    }


    public static void LineParticle(ParticleData particleData, Location location, double length, double width, double density) {
        LineParticle(particleData, location, length, width, density, false);
    }
    public static void LineParticle(ParticleData particleData, Location location, double length, double width, double density, boolean collision) {
        Location loc;
        if (collision) {
            loc = RayTrace.rayLocationBlock(location, length, true).HitPosition;
        } else {
            loc = location.clone().add(location.getDirection().multiply(length));
        }
        LineParticle(particleData, location, loc, width, density);
    }

    public static void LineParticle(ParticleData particleData, Location location, Location location2, double width, double density) {
        final double multiply = 1/density;
        Vector vector = location2.toVector().subtract(location.toVector()).normalize().multiply(multiply);
        double distance = location.distance(location2);
        Location loc = location.clone();
        for (double i = 0; i < distance; i += multiply) {
            Location locWidth = loc.clone();
            locWidth.add(Vector.getRandom().multiply(width*(random.nextDouble()-0.5)));
            spawnParticle(particleData, locWidth);
            loc.add(vector);
        }
    }

    public static void RandomVectorParticle(ParticleData particleData, Location location, int density) {
        for (int i = 0; i < density; i++) {
            ParticleData clone = particleData.clone();
            clone.vector = new Vector(random.nextDouble()*2-1, random.nextDouble()*2, random.nextDouble()*2-1);
            spawnParticle(clone, location);
        }
    }
}
