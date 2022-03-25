package swordofmagic7.Inventory;

import swordofmagic7.Pet.PetParameter;

import java.util.Comparator;

public enum PetSortType {
    Name("名前順"),
    Level("レベル順"),
    GrowthRate("成長率順"),
    ;

    public String Display;

    PetSortType(String Display) {
        this.Display = Display;
    }

    public boolean isName() {
        return this == Name;
    }

    public boolean isLevel() {
        return this == Level;
    }

    public boolean isGrowthRate() {
        return this == GrowthRate;
    }
}

class PetSortName implements Comparator<PetParameter> {
    public int compare(PetParameter Pet1, PetParameter Pet2) {
        return Pet1.petData.Id.compareTo(Pet2.petData.Id);
    }
}

class PetSortLevel implements Comparator<PetParameter> {
    public int compare(PetParameter Pet1, PetParameter Pet2) {
        return Pet1.Level - Pet2.Level;
    }
}

class PetSortGrowthRate implements Comparator<PetParameter> {
    public int compare(PetParameter Pet1, PetParameter Pet2) {
        if (Pet1.GrowthRate < Pet2.GrowthRate) {
            return 1;
        } else if (Pet1.GrowthRate > Pet2.GrowthRate) {
            return -1;
        } else return 0;
    }
}