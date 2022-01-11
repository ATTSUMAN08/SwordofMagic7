package swordofmagic7.Life.Lumber;

import java.util.ArrayList;
import java.util.List;

public class LumberData {
    public final List<LumberItemData> itemData = new ArrayList<>();
    public final int CoolTime;
    public final int Exp;
    public final int ReqLevel;

    public LumberData(int CoolTime, int Exp, int ReqLevel) {
        this.CoolTime = CoolTime;
        this.Exp = Exp;
        this.ReqLevel = ReqLevel;
    }
}
