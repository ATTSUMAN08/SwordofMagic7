package swordofmagic7.Life;

public enum FishingCommand {
    Shift("S", "¹"),
    Drop("D", "²"),
    RightClick("R", "³"),
    ;

    String Display;
    String DisplayNum;

    FishingCommand(String Display, String DisplayNum) {
        this.Display = Display;
        this.DisplayNum = DisplayNum;
    }

    public String getDisplayColored(boolean bool) {
        String color = null;
        switch (this) {
            case Shift -> color = "§e";
            case Drop ->  color = "§c";
            case RightClick -> color = "§b";
        }
        return color + getDisplay(bool);
    }

    public String getDisplay(boolean bool) {
        if (bool) return DisplayNum;
        else return Display;
    }
}
