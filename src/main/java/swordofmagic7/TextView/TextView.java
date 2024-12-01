package swordofmagic7.TextView;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import swordofmagic7.Sound.SoundList;

import java.util.UUID;

public class TextView {
    String data = "Reset";
    SoundList sound = null;
    boolean isNatural = false;
    boolean isMute = false;

    public TextView() {}
    public TextView(String str) {
        addText(str);
    }

    public boolean isEmpty() {
        return data.equals("Reset");
    }

    @Override
    public String toString() {
        if (isNatural) data += ",isNatural";
        if (isMute) data += ",isMute";
        if (sound != null) data += ",Sound:" + sound;
        return data;
    }

    public TextView addText(String str) {
        data += ",Text:" + str;
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
        this.sound = sound;
        return this;
    }

    public TextView setNatural() {
        this.isNatural = true;
        return this;
    }

    public TextView setUUID(UUID uuid) {
        data += ",UUID:" + uuid;
        return this;
    }

    public TextView setSender(String sender) {
        data += ",Sender:" + sender;
        return this;
    }

    public TextView setDisplay(String display) {
        data += ",Display:" + display;
        return this;
    }

    public TextView setFrom(String from) {
        data += ",From:" + from;
        return this;
    }

    public TextView setMute(boolean mute) {
        isMute = mute;
        return this;
    }

    public Component toComponent() {
        String[] data = this.data.split(",");
        Component finalText = Component.empty();
        Component text = Component.empty();
        Component hover = Component.empty();
        for (int i = 1; i < data.length; i++) {
            String[] split = data[i].split(":", 2);
            if (split[0].equalsIgnoreCase("Reset")) {
                finalText = finalText.append(text);
                text = Component.empty();
            } else if (split[0].equalsIgnoreCase("Text")) {
                text = text.append(Component.text(split[1]));
            } else if (split[0].equalsIgnoreCase("Hover")) {
                boolean first = true;
                for (String str : split[1].split("\n")) {
                    if (!first) hover = hover.appendNewline();
                    hover = hover.append(Component.text(str));
                    first = false;
                }
                text = text.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
            }
        }
        finalText = finalText.append(text);
        return finalText;
    }
}
