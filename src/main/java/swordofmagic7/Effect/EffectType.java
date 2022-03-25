package swordofmagic7.Effect;

import java.util.List;

public enum EffectType {
    Stun("スタン", false, "移動できなくなります"),
    Silence("沈黙", false, "スキルが使えなくなります"),
    Freeze("氷結", false, "[スタン]と[沈黙]を合わせた効果です"),
    Rigidity("硬直", EffectRank.Impossible, false, false, "様々なアクションが実行できなくなります"),
    Monstrance("モンストランス", false, "回避が減少します"),
    Slow("スロー", false, "移動速度が低下します"),
    Stop("ストップ", false, "[氷結]と同じ状態になります\nバフ・デバフ・ダメージを受け無くなります"),
    TimeTravelSequelae("タイムトラベル後遺症", EffectRank.Impossible, false, "[パス]の効果を受け無くなります"),
    PeaceMaker("ピースメーカー", false, "[氷結]と同じ状態になり攻撃力が減少します"),
    Glory("栄光", false, "被ダメージが2倍になります"),

    Covert("隠密", true, "ノーマルターゲット判定を受けません"),
    Invincible("無敵", EffectRank.Impossible, true, "ダメージを受け無くなります"),
    Teleportation("テレポーテーション", true, "ヘイト値がリセットされます"),
    PetBoost("ペトブースト", true, "攻撃力と防御力上昇します"),
    PainBarrier("ペインバリア", true, "防御力が上昇します"),
    Aiming("エイミング", true, "与ダメージが上昇します"),
    HolyDefense("ホーリーディフェンス", true, "物理防御力が上昇します"),
    HolyAttack("ホーリーアタック ", true, "物理与ダメージが上昇します"),
    Revive("リバイブ", EffectRank.High, true, "致死量ダメージを受けた際HPが50%まで回復します"),
    HighGuard("ハイガート", true, "防御力が上昇します"),
    SwashBaring("スワッシュバリング", true, "ヘイト増加量が上昇します"),
    ElementalBurst("エレメンタルバースト", true, "魔法与ダメージが上昇します"),
    Outrage("アウトレイジ", 20, true, "魔法与ダメージが上昇します"),
    TracerBullet("トレーサーバレット", true, "命中が上昇します"),
    DoubleGunStance("ダブルガンスタンス", true, "[バレットマーカースキル]の与ダメージが上昇します"),
    DeedsOfValor("ディーズオブヴァロー", true, "物理与ダメージが上昇します\n防御力が減少します"),
    Zornhau("ツォルンハウ", true, "[ツーケン]が使用できるようになります"),
    Zucken("ツーケン", true, "[レーデル]が使用できるようになります"),
    Indulgendia("インダルゲンディア", true, "効果中は体力が回復します"),
    IncreaseMagicDef("インクリースマジックDEF", true, "魔法防御力が上昇します"),
    Indulgence("インダルジェンス", 3, true, "[一般]デバフを付与されるのを防ぎます"),
    HeadShot("ヘッドショット", true, "[クリティカル発生]が上昇します"),
    RedemptionAble("リデンプション発動可能", true, "[リデンプション]を使用できます"),
    Redemption("リデンプション ", true, "回避が上昇します"),
    TimeForward("タイムフォーワンド免疫 ", true, "[タイムフォーワンド]の効果を受け無くなります"),
    Seiko("聖光", true, "被ダメージが1/3倍になります"),
    Reflection("反射", true, "被ダメージの10%を反射します"),
    ;

    public String Display;
    public EffectRank effectRank = EffectRank.Normal;
    public boolean Buff;
    public List<String> Lore;
    public int MaxStack = 1;
    public boolean view = true;

    EffectType(String Display, boolean Buff, String Lore) {
        this.Display = Display;
        this.Buff = Buff;
        this.Lore = List.of(Lore.split("\n"));
    }

    EffectType(String Display, EffectRank effectRank, boolean Buff, String Lore) {
        this.Display = Display;
        this.effectRank = effectRank;
        this.Buff = Buff;
        this.Lore = List.of(Lore.split("\n"));
    }

    EffectType(String Display, EffectRank effectRank, boolean Buff, boolean view, String Lore) {
        this.Display = Display;
        this.effectRank = effectRank;
        this.Buff = Buff;
        this.view = view;
        this.Lore = List.of(Lore.split("\n"));
    }

    EffectType(String Display, int MaxStack, boolean Buff, String Lore) {
        this.Display = Display;
        this.MaxStack = MaxStack;
        this.Buff = Buff;
        this.Lore = List.of(Lore.split("\n"));
    }

    public boolean isFreeze() {
        return this == Freeze || this == Stop || this == PeaceMaker;
    }

    public boolean isCrowdControl() {
        return this == Stun || isFreeze();
    }

    public boolean isSkillsNotAvailable() {
        return this == Silence || isFreeze();
    }

    public boolean isSlow() {
        return this == Slow;
    }

    public String color() {
        if (Buff) return "§e";
        else return "§c";
    }
}
