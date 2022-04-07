package swordofmagic7.Effect;

import java.util.List;

public enum EffectType {
    Stun("スタン", false, "移動できなくなります"),
    Silence("沈黙", false, "スキルが使えなくなります"),
    Freeze("氷結", false, "[スタン]と[沈黙]を合わせた効果です"),
    Rigidity("硬直", EffectRank.Impossible, false, false, "様々なアクションが実行できなくなります", true),
    Monstrance("モンストランス", false, "回避が減少します", true),
    Slow("スロー", false, "移動速度が低下します"),
    Stop("ストップ", false, "[氷結]と同じ状態になります\nバフ・デバフ・ダメージを受け無くなります", false, true),
    TimeTravelSequelae("タイムトラベル後遺症", EffectRank.Impossible, false, "[パス]の効果を受け無くなります"),
    PeaceMaker("ピースメーカー", false, "[氷結]と同じ状態になり攻撃力が減少します", true),
    Glory("栄光", false, "被ダメージが2倍になります"),
    Blind("盲目", false, "視界が暗くなります"),
    InsufficientFilling("充填不足", EffectRank.Impossible, false, "攻撃力が半分になります", true),
    HallucinationSmoke("ハルシネーションスモーク", false, "クリティカル抵抗と回避が減少します", true),
    RecoveryInhibition("回復阻害", false, "体力回復系効果が無効化されます"),
    Confusion("混乱", false, "50%の確率で発動するスキルスロットが変更されます"),
    SequelaeReducedDistortion("縮小歪曲後遺症", false, "効果はありません"),
    Concussion("脳震盪", false, "[氷結]と同じ効果です"),
    DeathVerdict("デスヴァーディクト", false, "[氷結]と同じ効果です", true),

    Covert("隠密", true, "ノーマルターゲット判定を受けません"),
    Cloaking("クローキング", true, "移動職度が上昇します", true),
    Invincible("無敵", EffectRank.Impossible, true, "ダメージを受け無くなります", false, true),
    Teleportation("テレポーテーション", true, "ヘイト値がリセットされます"),
    PetBoost("ペトブースト", true, "攻撃力と防御力上昇します", true),
    ExtraAttack("エクストラアタック", true, "攻撃力が上昇します", true),
    PainBarrier("ペインバリア", true, "防御力が上昇します", true),
    Aiming("エイミング", true, "与ダメージが上昇します", true),
    HolyDefense("ホーリーディフェンス", true, "物理防御力が上昇します", true),
    HolyAttack("ホーリーアタック", true, "物理与ダメージが上昇します", true),
    Revive("リバイブ", EffectRank.High, true, "致死量ダメージを受けた際HPが50%まで回復します"),
    HighGuard("ハイガート", true, "防御力が上昇します", true),
    SwashBaring("スワッシュバリング", true, "ヘイト増加量が上昇します"),
    HatePriority("ヘイト優先", true, "ノーマルターゲットが自身になります"),
    ElementalBurst("エレメンタルバースト", true, "魔法与ダメージが上昇します", true),
    Outrage("アウトレイジ", 20, true, "魔法与ダメージが上昇します", true),
    TracerBullet("トレーサーバレット", true, "命中が上昇します", true),
    DoubleGunStance("ダブルガンスタンス", true, "[バレットマーカースキル]の与ダメージが上昇します"),
    DeedsOfValor("ディーズオブヴァロー", true, "物理与ダメージが上昇します\n防御力が減少します", true),
    Zornhau("ツォルンハウ", true, "[ツーケン]が使用できるようになります"),
    Zucken("ツーケン", true, "[レーデル]が使用できるようになります"),
    Indulgendia("インダルゲンディア", true, "効果中は体力が回復します"),
    IncreaseMagicDef("インクリースマジックDEF", true, "魔法防御力が上昇します", true),
    Indulgence("インダルジェンス", 3, true, "[一般]デバフを付与されるのを防ぎます"),
    HeadShot("ヘッドショット", true, "[クリティカル発生]が上昇します", true),
    RedemptionAble("リデンプション発動可能", true, "[リデンプション]を使用できます"),
    Redemption("リデンプション", true, "回避が上昇します", true),
    TimeForward("タイムフォーワンド免疫", true, "[タイムフォーワンド]の効果を受け無くなります", false, true),
    Seiko("聖光", true, "被ダメージが1/3倍になります"),
    Reflection("反射", true, "被ダメージの10%を反射します"),
    MissileHole("ミサイルホール", true, "魔法被ダメージ耐性が上昇します", true),
    CrossGuard("クロスガード", true, "効果中にダメージを受けた場合自分に[クロスガード:カウンター]バフが付与されます", false),
    CrossGuardCounter("クロスガード:カウンター", true, "[物理与ダメージ]が上昇します", true),
    ArcaneEnergy("アーケインエナジー", true, "スキルのマナ消費が0になります", false),
    Foretell("フォアテル", true, "被ダメージ耐性増加が上昇します", true),
    ;

    public String Display;
    public EffectRank effectRank = EffectRank.Normal;
    public boolean Buff;
    public List<String> Lore;
    public int MaxStack = 1;
    public boolean view = true;
    public boolean isUpdateStatus = false;
    public boolean isStatic = false;

    EffectType(String Display, boolean Buff, String Lore) {
        this.Display = Display;
        this.Buff = Buff;
        this.Lore = List.of(Lore.split("\n"));
    }

    EffectType(String Display, boolean Buff, String Lore, boolean isUpdateStatus) {
        this.Display = Display;
        this.Buff = Buff;
        this.Lore = List.of(Lore.split("\n"));
        this.isUpdateStatus = isUpdateStatus;
    }

    EffectType(String Display, EffectRank effectRank, boolean Buff, String Lore) {
        this.Display = Display;
        this.effectRank = effectRank;
        this.Buff = Buff;
        this.Lore = List.of(Lore.split("\n"));
    }

    EffectType(String Display, boolean Buff, String Lore, boolean isUpdateStatus, boolean isStatic) {
        this.Display = Display;
        this.Buff = Buff;
        this.Lore = List.of(Lore.split("\n"));
        this.isUpdateStatus = isUpdateStatus;
        this.isStatic = isStatic;
    }

    EffectType(String Display, EffectRank effectRank, boolean Buff, String Lore, boolean isUpdateStatus) {
        this.Display = Display;
        this.effectRank = effectRank;
        this.Buff = Buff;
        this.Lore = List.of(Lore.split("\n"));
        this.isUpdateStatus = isUpdateStatus;
    }

    EffectType(String Display, EffectRank effectRank, boolean Buff, String Lore, boolean isUpdateStatus, boolean isStatic) {
        this.Display = Display;
        this.effectRank = effectRank;
        this.Buff = Buff;
        this.Lore = List.of(Lore.split("\n"));
        this.isUpdateStatus = isUpdateStatus;
        this.isStatic = isStatic;
    }

    EffectType(String Display, EffectRank effectRank, boolean Buff, boolean view, String Lore) {
        this.Display = Display;
        this.effectRank = effectRank;
        this.Buff = Buff;
        this.view = view;
        this.Lore = List.of(Lore.split("\n"));
    }

    EffectType(String Display, EffectRank effectRank, boolean Buff, boolean view, String Lore, boolean isStatic) {
        this.Display = Display;
        this.effectRank = effectRank;
        this.Buff = Buff;
        this.view = view;
        this.Lore = List.of(Lore.split("\n"));
        this.isStatic = isStatic;
    }

    EffectType(String Display, int MaxStack, boolean Buff, String Lore) {
        this.Display = Display;
        this.MaxStack = MaxStack;
        this.Buff = Buff;
        this.Lore = List.of(Lore.split("\n"));
    }

    EffectType(String Display, int MaxStack, boolean Buff, String Lore, boolean isUpdateStatus) {
        this.Display = Display;
        this.MaxStack = MaxStack;
        this.Buff = Buff;
        this.Lore = List.of(Lore.split("\n"));
        this.isUpdateStatus = isUpdateStatus;
    }

    public boolean isFreeze() {
        return this == Freeze || this == Stop || this == PeaceMaker || this == Concussion;
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

    public boolean isBlind() {
        return this == Blind;
    }

    public String color() {
        if (Buff) return "§e";
        else return "§c";
    }
}
