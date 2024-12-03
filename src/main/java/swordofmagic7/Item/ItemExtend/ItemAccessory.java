package swordofmagic7.Item.ItemExtend;

import swordofmagic7.Status.StatusParameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static net.somrpg.swordofmagic7.SomCore.random;

public class ItemAccessory implements Cloneable {
    public HashMap<StatusParameter, Double> Base = new HashMap<>();
    public HashMap<StatusParameter, Double> Range = new HashMap<>();
    public HashMap<StatusParameter, Double> Parameter = new HashMap<>();
    public int maxSlot = 1;

    public void randomize() {
        Parameter.clear();
        int slot = random.nextInt(maxSlot)+1;
        List<StatusParameter> list = new ArrayList<>();
        for (StatusParameter param : StatusParameter.values()) {
            if (Base.containsKey(param)) {
                list.add(param);
            }
        }
        for (int i = 0; i < slot; i++) {
            StatusParameter param = list.get(random.nextInt(list.size()));
            list.remove(param);
            double range = Range.get(param);
            Parameter.put(param, Base.get(param) * (1+(random.nextDouble(range)*2-range)));
        }
    }

    @Override
    public ItemAccessory clone() {
        try {
            ItemAccessory clone = (ItemAccessory) super.clone();
            clone.Parameter = new HashMap<>(Parameter);
            // TODO: このクローンが元の内部を変更できないようにミュータブルな状態をここにコピーします
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
