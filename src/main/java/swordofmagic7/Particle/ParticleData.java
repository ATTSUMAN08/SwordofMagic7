package swordofmagic7.Particle;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

public class ParticleData implements Cloneable {
    Particle particle = Particle.REDSTONE;
    Particle.DustOptions dustOptions = new Particle.DustOptions(Color.RED, 1);
    ParticleType particleType = ParticleType.General;
    float speed = 0;
    Vector vector = new Vector();

    ParticleData() {
    }

    public ParticleData(Particle particle) {
        this.particle = particle;
    }

    public ParticleData(Particle particle, Particle.DustOptions dustOptions) {
        this.particle = particle;
        this.dustOptions = dustOptions;
    }

    public ParticleData(Particle particle, float speed) {
        this.particle = particle;
        this.speed = speed;
    }

    public ParticleData(Particle particle, float speed, Vector vector) {
        this.particle = particle;
        this.speed = speed;
        this.vector = vector;
    }

    @Override
    public ParticleData clone() {
        try {
            ParticleData clone = (ParticleData) super.clone();
            // TODO: このクローンが元の内部を変更できないようにミュータブルな状態をここにコピーします
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
