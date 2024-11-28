package swordofmagic7.TextView;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
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


    private static final TextComponent newLine = new TextComponent(ComponentSerializer.parse("{text: \"\n\"}"));
    public TextComponent toComponent() {
        String[] data = this.data.split(",");
        TextComponent finalText = new net.md_5.bungee.api.chat.TextComponent();
        TextComponent text = new net.md_5.bungee.api.chat.TextComponent();
        TextComponent hover = new net.md_5.bungee.api.chat.TextComponent();
        for (int i = 1; i < data.length; i++) {
            String[] split = data[i].split(":", 2);
            if (split[0].equalsIgnoreCase("Reset")) {
                finalText.addExtra(text);
                text = new TextComponent();
            } else if (split[0].equalsIgnoreCase("Text")) {
                text.addExtra(split[1]);
            } else if (split[0].equalsIgnoreCase("Hover")) {
                boolean first = true;
                for (String str : split[1].split("\n")) {
                    if (!first) hover.addExtra(newLine);
                    hover.addExtra(str);
                    first = false;
                }
                text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));
                hover = new TextComponent();
            }
        }
        finalText.addExtra(text);
        return finalText;
    }
}
