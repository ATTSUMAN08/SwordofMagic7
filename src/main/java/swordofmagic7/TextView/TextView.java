package swordofmagic7.TextView;

import swordofmagic7.Sound.SoundList;

public class TextView {
    String data = "Reset";

    public TextView() {}
    public TextView(String str) {
        addText(str);
    }

    @Override
    public String toString() {
        return data;
    }

    public TextView addText(String str) {
        if (data == null) data = "Text:" + str;
        else data += ",Text:" + str;
        return this;
    }

    public TextView addHover(String str) {
        data += ",Hover:" + str;
        return this;
    }

    public TextView reset() {
        data += ",Reset";
        return this;
    }

    public TextView addView(TextView view) {
        data += "," + view;
        return this;
    }

    public TextView setSound(SoundList sound) {
        data += ",Sound:" + sound;
        return this;
    }

    public TextView setNatural() {
        data += ",isNatural";
        return this;
    }
}
