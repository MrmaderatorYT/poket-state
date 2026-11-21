package com.ccs.poketstate.sim;

/** Structural institutional laws & economic switches showing explicit inflation impact. */
public enum LawType {
    MIN_WAGE("Мінімальна зарплата", "Реальна з/п + · Джині - · Тиск на ціни", 0, 0.0, -0.028, 2.2, 0.45),
    PRIVATIZATION("Приватизація", "ВВП +8% · Нижчі витрати · Джині +", 0, 0.08, 0.025, -0.8, -0.20),
    LAND_MARKET("Ринок землі", "Їжа +25% · ВВП +5% · Охолоджує ціни", 0, 0.05, 0.008, 0.4, -0.35),
    ANTI_CORRUPTION("Антикорупційне бюро (АКБ)", "Корупція -- · Тінь -- · -90 ₴/міс", 90, 0.02, -0.010, 2.0, -0.15),
    CONTRACT_ARMY("Контрактна армія", "Бойова міць +22% · Довіра + · Дорожче військо", 60, 0.0, 0.0, 2.4, 0.10),
    MARTIAL_LAW("Воєнний стан", "Оборона +30% · Гасить страйки · ВВП -10%", 0, -0.10, 0.012, -3.5, 0.60),
    LUXURY_TAX("Податок на розкіш", "Додаткові податки · Джині -", 0, -0.03, -0.032, 1.4, -0.10),
    CHILDLESS_TAX("Податок на бездітність", "+1% ВВП у бюджет · Суспільство не любить", 0, -0.01, 0.004, -3.2, -0.10),
    GREEN_TAX("Зелений податок", "Екологія ++ · Тривалість життя + · Помірно стримує заводи", 0, -0.02, 0.0, 0.6, 0.15),
    STATE_INSURANCE("Держпрограма страхування", "Знижує вартість криз на 25% · -75 ₴/міс", 75, 0.01, -0.005, 1.2, 0.05),
    ROAD_VIGNETTE("Електронна віньєтка", "+25% доходу платних автодоріг · Дорожній фонд", 0, 0.02, 0.0, -0.4, 0.05);

    public final String title;
    public final String desc;
    public final int monthlyCost;
    public final double gdpEffect;
    public final double giniDelta;
    public final double trustBias;
    public final double inflationImpact;

    LawType(String title, String desc, int monthlyCost, double gdpEffect,
            double giniDelta, double trustBias, double inflationImpact) {
        this.title = title;
        this.desc = desc;
        this.monthlyCost = monthlyCost;
        this.gdpEffect = gdpEffect;
        this.giniDelta = giniDelta;
        this.trustBias = trustBias;
        this.inflationImpact = inflationImpact;
    }
}
