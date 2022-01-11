package swordofmagic7.Life.Harvest;

import java.util.ArrayList;
import java.util.List;

public class HarvestData {
    public final List<HarvestItemData> itemData = new ArrayList<>();
    public final int CoolTime;
    public final int Exp;
    public final int ReqLevel;

    public HarvestData(int CoolTime, int Exp, int ReqLevel) {
        this.CoolTime = CoolTime;
        this.Exp = Exp;
        this.ReqLevel = ReqLevel;
    }
}
