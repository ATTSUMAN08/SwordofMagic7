package swordofmagic7.Effect;

import java.util.List;

public enum EffectType {
    Stun("スタン", false, "移動できなくなります"),
    Fixed("固定", false, "座標が固定されます"),
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
    InsufficientFilling("充填不足", EffectRank.Impossible, false, "攻撃力が低下します", true),
    HallucinationSmoke("ハルシネーションスモーク", false, "クリティカル抵抗と回避が減少します", true),
    RecoveryInhibition("回復阻害", false, "体力回復系効果が無効化されます"),
    Confusion("混乱", false, "50%の確率で発動するスキルスロットが変更されます"),
    SequelaeReducedDistortion("縮小歪曲後遺症", false, "効果はありません"),
    Concussion("脳震盪", false, "[氷結]と同じ効果です"),
    DeathVerdict("デスヴァーディクト", false, "被ダメージ耐性が減少します", true),
    Adhesive("粘着", false, "攻撃力が低下します", true),
    Unconscious("無自覚", EffectRank.Impossible, false, "「自分では、自分が狂っていることはわからないものです」", false),
    Decay("崩壊", EffectRank.Impossible, false, "「一度なにかに縋ってしまうと、それ以外何も見えなくなってしまうのは良くないところです」", false),
    CantLook("見堪", EffectRank.Impossible, false, "「ときに同調圧力は、文明を滅ぼします」", false),
    IronHook("アイアンフック", EffectRank.Impossible, false, "[氷結]と同じ状態になります", false),
    Keelhauling("キールハウリング", EffectRank.Impossible, false, "[氷結]と[固定]を合わせた効果です", false),
    SprinkleSand("スプリンクルサンド", false, "命中が下がります", true),
    BreakBrick("ブレイクブリック", false, "クリティカル発生が下がります", true),
    Scary("恐怖", false, "怖いです", false),
    Gevura("ゲブラ", false, "被ダメージ耐性が減少します", true),
    ExcessiveTreatment("過剰な治療", false, "「過剰な治療は時に死ぬ招くこともあります」", false),
    UnpleasantOmen("周りが気になります", false, "「どこかから嫌な気配を感じます」", false),
    Capote("カポーテ", false, "命中と回避が減少します", true),

    Covert("隠密", true, "ノーマルターゲット判定を受けません"),
    Cloaking("クローキング", true, "移動職度が上昇します", true),
    Invincible("無敵", EffectRank.Impossible, true, "ダメージを受け無くなります", false, true),
    Teleportation("テレポーテーション", true, "ヘイト値がリセットされます"),
    PetBoost("ペトブースト", true, "攻撃力と防御力上昇します", true),
    ExtraAttack("エクストラアタック", true, "攻撃力が上昇します", true),
    ExtraDefense("エクストラディフェンス", true, "防御力が上昇します", true),
    ExtraCritical("エクストラクリティカル", true, "クリティカル発生が上昇します", true),
    EyeSight("アイサイト", true, "命中が上昇します", true),
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
    Indulgence("インダルジェンス", 5, true, "[一般]デバフを付与されるのを防ぎます"),
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
    Brutality("ブルタリティ", true, true, "魔法与ダメ－ジが上昇し、持続中はマナが消費されます", true),
    CoveringFire("カバーリングファイア", true, "通常攻撃が強化されます"),
    JollyRoger("ジャリーロジャー", true, "与ダメージが上昇します", true),
    JollyRogerCombo("ジャリーロジャー:コンボ", 100, true, "エネミーを攻撃するとジャリーロジャー:コンボが増えます"),
    Bully("ブリー", 30, true, "回避が上昇します", true),
    Rampage("ランページ", true, "回避が減少します\n防御力が減少します\n被ダメージが上昇します\n与ダメージが上昇します", true),
    Nachash("ナハシ", true, "攻撃時に体力を回復します"),
    Sevenfold("セブンフォールド", true, "被ダメージ耐性が上昇します\n被弾すると解除されます", true),
    Ayinsof("アインソフ", true, "最大体力が上昇します", true),
    SubzeroShield("サブゼロシールド", true, "[氷結]にかからなくなります", false),
    BeakMask("ビークマスク", true, "[スタン]と[スロー]以外の一般解除可能デバフにかからなくなります", false),
    HealingFactor("ヒーリングファクター", true, "持続的に体力を回復します\n付与時以上には回復しません", false),
    Modafinil("モダフィニル", true, "移動速度が上昇します", true),
    Warcry("ウォークライ", 20, true, "物理与ダメージが上昇します", true),
    Frenzy("フレンジー", 20, true, "物理与ダメージが上昇します", true),
    Ole("OLE", true, "クリティカル発生と移動速度が上昇します", true),
    Muleta("ムレタ", true, "反撃します", true),
    ShadowPool("シャドウプール", true, "あらゆる死を回避します", false),
    ShadowFatter("シャドウファッター", EffectRank.High, true, "[固定]を同じ状態になります", false),
    ;

    public String Display;
    public EffectRank effectRank = EffectRank.Normal;
    public boolean Buff;
    public List<String> Lore;
    public int MaxStack = 1;
    public boolean view = true;
    public boolean isUpdateStatus = false;
    public boolean isStatic = false;
    public boolean isToggle = false;

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

    EffectType(String Display, boolean Buff, boolean isToggle, String Lore, boolean isUpdateStatus) {
        this.Display = Display;
        this.Buff = Buff;
        this.Lore = List.of(Lore.split("\n"));
        this.isUpdateStatus = isUpdateStatus;
        this.isToggle = isToggle;
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

    public boolean isFixed() {
        return this == Fixed || this == Keelhauling || this == ShadowFatter;
    }

    public boolean isFreeze() {
        return this == Freeze || this == Stop || this == PeaceMaker || this == Concussion || this == IronHook || this == Keelhauling;
    }

    public boolean isCrowdControl() {
        return this == Stun || isFreeze();
    }

    public boolean isSkillsNotAvailable() {
        return this == Silence || this == ShadowPool || isFreeze();
    }

    public boolean isInvincible() {
        return this == Invincible || this == Stop || this == ShadowPool;
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
