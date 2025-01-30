package swordofmagic7.viewBar.SideBarToDo;

import org.bukkit.entity.Player;
import swordofmagic7.classes.ClassData;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Function;
import swordofmagic7.Inventory.ItemParameterStack;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Life.LifeType;
import swordofmagic7.Sound.SoundList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static swordofmagic7.Function.decoLore;
import static swordofmagic7.Function.decoText;

public class SideBarToDo {

    private final Player player;
    private final PlayerData playerData;

    public List<SideBarToDoData> list = new ArrayList<>();

    public SideBarToDo(PlayerData playerData) {
        this.playerData = playerData;
        player = playerData.player;
    }

    public void SideBarToDoCommand(String[] args) {
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("itemAmount") || args[0].equalsIgnoreCase("iA")) {
                if (args.length >= 2) {
                    for (int i = 1; i < args.length; i++) {
                        ItemParameter item = null;
                        if (DataBase.ItemList.containsKey(args[i])) {
                            item = DataBase.getItemParameter(args[i]);
                        } else {
                            try {
                                item = playerData.ItemInventory.getItemParameter(Integer.parseInt(args[i]));
                            } catch (Exception ignored) {
                            }
                        }
                        if (item != null) {
                            SideBarToDoData data = new SideBarToDoData(SideBarToDoType.ItemAmount, item);
                            list.add(data);
                        } else {
                            player.sendMessage("§e" + args[i] + "§aは存在しない§eアイテム§aです");
                        }
                    }
                }
            } else if (args[0].equalsIgnoreCase("recipeInfo") || args[0].equalsIgnoreCase("rI")) {
                if (DataBase.ItemRecipeList.containsKey(args[1])) {
                    for (ItemParameterStack stack : DataBase.getItemRecipe(args[1]).ReqStack) {
                        SideBarToDoData data = new SideBarToDoData(SideBarToDoType.ItemAmount, stack.itemParameter);
                        if (data.key != null) {
                            list.add(data);
                        } else {
                            viewHelp();
                        }
                    }
                } else {
                    player.sendMessage("§e" + args[1] + "§aは存在しない§eレシピ§aです");
                }
            } else if (args[0].equalsIgnoreCase("lifeInfo") || args[0].equalsIgnoreCase("lI")) {
                SideBarToDoData data = new SideBarToDoData(SideBarToDoType.LifeInfo, LifeType.getData(args[1]));
                if (data.key != null) {
                    list.add(data);
                } else {
                    viewHelp();
                }
            } else if (args[0].equalsIgnoreCase("classInfo") || args[0].equalsIgnoreCase("cI")) {
                if (args.length >= 2) {
                    Set<SideBarToDoData> data = new HashSet<>();
                    if (DataBase.ClassList.containsKey(args[1]) || DataBase.ClassListDisplay.containsKey(args[1])) {
                        data.add(new SideBarToDoData(SideBarToDoType.ClassInfo, DataBase.getClassData(args[1])));
                    } else if (args[1].equals("classes")) {
                        for (ClassData classData : playerData.Classes.classSlot) {
                            if (classData != null) data.add(new SideBarToDoData(SideBarToDoType.ClassInfo, classData));
                        }
                    }
                    list.addAll(data);
                } else {
                    Function.sendMessage(player, "§a正しい§eクラス名§aを入力してください", SoundList.NOPE);
                }
            } else if (args[0].equalsIgnoreCase("clear") || args[0].equalsIgnoreCase("c")) {
                if (args.length >= 2) {
                    try {
                        list.remove(Integer.parseInt(args[1]));
                    } catch (Exception e) {
                        viewHelp();
                    }
                } else {
                    list.clear();
                }
            } else {
                viewHelp();
            }
        } else {
            viewHelp();
        }
    }

    public void refresh() {
        if (!list.isEmpty()) {
            List<String> textData = new ArrayList<>();
            textData.add(decoText("SideBarToDo"));
            for (SideBarToDoData data : list) {
                if (data.type.isItemAmount()) {
                    ItemParameter item = (ItemParameter) data.key;
                    int amount = playerData.ItemInventory.getItemParameterStack(item).Amount;
                    textData.add(decoLore("アイテム数[" + item.Display + "]") + amount + "個");
                } else if (data.type.isLifeInfo()) {
                    LifeType type = (LifeType) data.key;
                    textData.add("§7・§e§l" + type.Display + " Lv" + playerData.LifeStatus.getLevel(type) + " " + playerData.LifeStatus.viewExpPercent(type));
                } else if (data.type.isClassInfo()) {
                    ClassData classData = (ClassData) data.key;
                    textData.add("§7・" + classData.Color + "§l" + classData.Display + " §e§lLv" + playerData.Classes.getClassLevel(classData) + " §a§l" + playerData.Classes.viewExpPercent(classData));
                }
            }
            playerData.ViewBar.setSideBar("SideBarToDo", textData);
        } else {
            playerData.ViewBar.resetSideBar("SideBarToDo");
        }
    }

    void viewHelp() {
        player.sendMessage(decoText("SideBarToDo Commands"));
        player.sendMessage("§e/sideBarToDo itemAmount <ItemName>");
        player.sendMessage("§e/sideBarToDo recipeInfo <RecipeId>");
        player.sendMessage("§e/sideBarToDo LifeInfo <LifeID>");
        player.sendMessage("§e/sideBarToDo ClassInfo <ClassID>");
        player.sendMessage("§e/sideBarToDo clear [<index>]");
    }
}
