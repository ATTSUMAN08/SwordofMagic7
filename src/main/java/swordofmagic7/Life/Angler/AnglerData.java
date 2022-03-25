package swordofmagic7.Life.Angler;

import java.util.ArrayList;
import java.util.List;

public class AnglerData {
    public final List<AnglerItemData> itemData = new ArrayList<>();
    public final int Exp;
    public final int ReqLevel;

    public AnglerData(int Exp, int ReqLevel) {
        this.Exp = Exp;
        this.ReqLevel = ReqLevel;
    }
}
