package swordofmagic7.InstantBuff;

import swordofmagic7.Status.StatusParameter;

import java.util.HashMap;

public class InstantBuffData {
    public HashMap<StatusParameter, Double> Fixed;
    public HashMap<StatusParameter, Double> Multiply;
    public int time;

    public InstantBuffData(HashMap<StatusParameter, Double> Fixed, HashMap<StatusParameter, Double> Multiply, int time) {
        this.Fixed = Fixed;
        this.Multiply = Multiply;
        this.time = time;
    }
}
