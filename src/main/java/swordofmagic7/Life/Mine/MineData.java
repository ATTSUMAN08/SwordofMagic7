package swordofmagic7.Life.Mine;

import java.util.ArrayList;
import java.util.List;

public class MineData {
    public final List<MineItemData> itemData = new ArrayList<>();
    public final int CoolTime;
    public final int Exp;
    public final int ReqLevel;

    public MineData(int CoolTime, int Exp, int ReqLevel) {
        this.CoolTime = CoolTime;
        this.Exp = Exp;
        this.ReqLevel = ReqLevel;
    }
}
