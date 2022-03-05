package swordofmagic7.Effect;

import java.util.List;

public enum EffectType {
    Stun("スタン", false, "移動できなくなります"),
    Silence("沈黙", false, "スキルが使えなくなります"),
    Covert("隠密", true, "ノーマルターゲット判定を受けません"),
    Invincible("無敵", true, "ダメージを受け無くなります"),
    Teleportation("テレポーテーション", true, "ヘイト値がリセットされます"),
    PetBoost("ペトブースト", true, "攻撃力と防御力上昇します"),
    PainBarrier("ペインバリア", true, "防御力が上昇します"),
    Aiming("エイミング", true, "与ダメージが上昇します"),
    HolyDefense("ホーリーディフェンス", true, "物理防御力が上昇します"),
    HolyAttack("ホーリーアタック ", true, "物理与ダメージが上昇します"),
    Revive("リバイブ ", true, "致死量ダメージを受けた際HPが50%まで回復します"),
    Rigidity("硬直", false, "様々なアクションが実行できなくなります"),
    Monstrance("モンストランス", false, "回避が減少します"),
    ;

    public String Display;
    public boolean Buff;
    public List<String> Lore;

    EffectType(String Display, boolean Buff, String Lore) {
        this.Display = Display;
        this.Buff = Buff;
        this.Lore = List.of(Lore.split("\n"));
    }
}
