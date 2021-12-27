package swordofmagic7.Pet;

import java.util.HashMap;

public class PetPersonality {
    public final static HashMap<String, PetSkillType> PersonalityA = new HashMap<>();
    public final static HashMap<String, Double> PersonalityB = new HashMap<>();
    public final static HashMap<String, Boolean> PersonalityC = new HashMap<>();
    PetPersonality() {
        PersonalityA.put("イケイケの", PetSkillType.Both);
        PersonalityA.put("グイグイの", PetSkillType.Both);
        PersonalityA.put("陽気な", PetSkillType.Both);
        PersonalityA.put("バリバリの", PetSkillType.Both);
        PersonalityA.put("ボチボチの", PetSkillType.Both);
        PersonalityA.put("気まぐれな", PetSkillType.Both);
        PersonalityA.put("宝の持ち腐れ", PetSkillType.Both);
        PersonalityA.put("救世主の", PetSkillType.Support);
        PersonalityA.put("親切な", PetSkillType.Support);
        PersonalityA.put("奇跡を与える", PetSkillType.Support);
        PersonalityA.put("生の亡者の", PetSkillType.Support);
        PersonalityA.put("攻撃的な", PetSkillType.Attack);
        PersonalityA.put("勇敢な", PetSkillType.Attack);
        PersonalityA.put("破滅へいざなう", PetSkillType.Attack);
        PersonalityA.put("賢者を目指す", PetSkillType.Attack);

        PersonalityB.put("きままな", 32d);
        PersonalityB.put("やる気マンマン", 28d);
        PersonalityB.put("神の如き", 24d);
        PersonalityB.put("ルンルン気分で", 18d);
        PersonalityB.put("深淵より生まれし", 18d);
        PersonalityB.put("超燃え滾る", 17d);
        PersonalityB.put("深緑を護る", 17d);
        PersonalityB.put("光とともに", 17d);
        PersonalityB.put("燃え滾る", 16d);
        PersonalityB.put("闇を支配する", 16d);
        PersonalityB.put("豪炎の使徒", 16d);
        PersonalityB.put("悪魔の如き", 15d);
        PersonalityB.put("光の如き", 15d);
        PersonalityB.put("煌めき羽ばたく", 15d);
        PersonalityB.put("闇へいざなう", 15d);
        PersonalityB.put("暗黒を纏う", 15d);
        PersonalityB.put("ドSな", 14d);
        PersonalityB.put("雑草魂で", 14d);
        PersonalityB.put("情熱を秘めた", 14d);
        PersonalityB.put("雪原を駆ける", 14d);
        PersonalityB.put("ふわふわと", 13d);
        PersonalityB.put("ガッツのある", 13d);
        PersonalityB.put("常夏気分で", 12d);
        PersonalityB.put("吹雪の如き", 12d);
        PersonalityB.put("稲妻の如き", 11d);
        PersonalityB.put("甘えんぼうで", 10d);
        PersonalityB.put("天使の如き", 9d);
        PersonalityB.put("恋人みたいで", 8d);
        PersonalityB.put("気にはしている", 7d);
        PersonalityB.put("ツンデレの", 6d);

        PersonalityC.put("頑固な", false);
        PersonalityC.put("べったりな", false);
        PersonalityC.put("手当たり次第の", true);
        PersonalityC.put("従順な", true);
        PersonalityC.put("ビビリな", false);
        PersonalityC.put("貫禄のある", false);
    }
}
