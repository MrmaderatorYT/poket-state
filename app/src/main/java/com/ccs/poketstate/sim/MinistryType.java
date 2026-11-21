package com.ccs.poketstate.sim;

/** Upgradable state ministries (levels 0..5) affecting macro parameters. */
public enum MinistryType {
    FINANCE("Мінфін", "Збір податків +5% · Нижчі % по боргу", 700, 38),
    ECONOMY("Мінекономіки", "ВВП +4% · Стійкість ринку", 750, 42),
    DEFENSE("Міноборони", "Боєздатність +12% · Нижче утримання війська", 800, 46),
    HEALTH("МОЗ", "Сервіси +8% · Довіра +1.5/рів.", 680, 40),
    EDUCATION("Міносвіти", "Продуктивність TFP +4% за рівень", 720, 42),
    FOREIGN("МЗС", "Експорт +6% · Гранти та дипломатія", 650, 34),
    INFRASTRUCTURE("МінІнфраструктури", "Ефективність транспорту · Нижча інфляція", 740, 40);

    public static final int MAX_LEVEL = 5;

    public final String title;
    public final String effect;
    public final int baseCost;
    public final int upkeepPerLevel;

    MinistryType(String title, String effect, int baseCost, int upkeepPerLevel) {
        this.title = title;
        this.effect = effect;
        this.baseCost = baseCost;
        this.upkeepPerLevel = upkeepPerLevel;
    }

    public int upgradeCost(int currentLevel) {
        return (int) (Math.round(baseCost * Math.pow(1.45, currentLevel) / 10.0) * 10);
    }
}
