package swordofmagic7.Inventory;

import swordofmagic7.Item.RuneParameter;

import java.util.Comparator;

public enum RuneSortType {
    Name("名前順"),
    Level("レベル順"),
    Quality("品質順"),
    ;

    public String Display;

    RuneSortType(String Display) {
        this.Display = Display;
    }

    public boolean isName() {
        return this == Name;
    }

    public boolean isLevel() {
        return this == Level;
    }

    public boolean isQuality() {
        return this == Quality;
    }
}

class RuneSortName implements Comparator<RuneParameter> {
    public int compare(RuneParameter rune1, RuneParameter rune2) {
        return rune1.Id.compareTo(rune2.Id);
    }
}

class RuneSortLevel implements Comparator<RuneParameter> {
    public int compare(RuneParameter rune1, RuneParameter rune2) {
        return rune1.Level - rune2.Level;
    }
}

class RuneSortQuality implements Comparator<RuneParameter> {
    public int compare(RuneParameter rune1, RuneParameter rune2) {
        if (rune1.Quality < rune2.Quality) {
            return 1;
        } else if (rune1.Quality > rune2.Quality) {
            return -1;
        } else return 0;
    }
}