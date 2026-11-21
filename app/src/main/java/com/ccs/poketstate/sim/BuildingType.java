package com.ccs.poketstate.sim;

/** Per-region buildable energy, agro, industrial, civil, and coastal facilities. */
public enum BuildingType {
    // Energy & Resources
    AES("АЕС", "Енергія", "Енергія +220 МВт · Висока окупність", 2400, 95, 4, false, true, 0, 80, 220, 0, 2.2, 0, -2, 0),
    GES("ГЕС", "Енергія", "Енергія +115 МВт · Чиста енергія", 1250, 44, 3, false, true, 0, 40, 115, 0, 1.0, 0, 2, 0),
    POWER_PLANT("ТЕС", "Енергія", "Енергія +85 МВт · Швидкий запуск", 850, 50, 2, false, true, 0, 35, 85, 0, 0.4, 0, -8, 0),
    SOLAR_WIND("СЕС / ВЕС", "Енергія", "Енергія +60 МВт · Екологія +", 950, 28, 2, false, true, 0, 25, 60, 0, 0.5, 0, 6, 0),
    OIL_REFINERY("Видобуток нафти / НПЗ", "Енергія", "Експорт нафти і палива на світовий ринок", 1550, 66, 3, false, true, 0, 70, -18, 0, 2.1, 0, -9, 140),

    // Agro
    FARM("Агроферма", "Агро", "Їжа +180 · Стримує ціни", 480, 24, 2, false, false, 15, 55, -6, 180, 0.4, 0, -1, 0),
    FOOD_PLANT("Агрокомбінат / Елеватор", "Агро", "Їжа +260 · Агроекспорт +", 820, 36, 2, false, true, 0, 65, -12, 260, 1.0, 0, -3, 45),

    // Housing & Industry
    HOUSING("Житловий квартал", "Місто", "Житло +180 місць", 440, 20, 2, false, false, 180, 10, -8, 0, 0.1, 0, 0, 0),
    FACTORY("Завод", "Промисловість", "Виробництво і робочі місця", 840, 46, 3, false, true, 0, 110, -24, 0, 1.3, 0, -8, 25),
    TECH_PARK("Технопарк", "Промисловість", "Високий ВВП, TFP і зарплати", 1300, 68, 2, false, true, 0, 80, -18, 0, 2.1, 0, -2, 35),
    BANK("Банк", "Фінанси", "Кредитний ліміт і інвестиції", 1050, 42, 1, false, false, 0, 45, -8, 0, 0.7, 5, 0, 0),

    // Civil Ministries Facilities
    COLLEGE("Коледж", "Освіта", "Випускники поповнюють ринок праці (+кадри)", 720, 44, 2, false, false, 0, 35, -8, 0, 0.6, 10, 0, 0),
    UNIVERSITY("Університет", "Освіта", "Кадри + · Продуктивність TFP +", 1100, 66, 2, false, false, 0, 45, -10, 0, 1.1, 16, 2, 0),
    HOSPITAL("Лікарня", "Медицина", "Здоров'я, тривалість життя, захист від епідемій", 780, 58, 2, false, false, 0, 40, -10, 0, 0.2, 18, 0, 0),
    HIGHWAY("Автобан / Дороги", "Інфраструктура", "Логістика + · Дохід платних доріг", 680, 28, 2, false, true, 0, 30, -4, 0, 0.9, 6, -2, 35),
    AIRPORT("Аеропорт", "Інфраструктура", "Торгівля, туризм та діловий експорт", 1650, 74, 4, false, true, 0, 75, -20, 0, 1.9, 8, -5, 85),
    COURT("Суд", "Цивільні", "Знижує корупцію та тінь", 640, 36, 1, false, false, 0, 30, -5, 0, 0.3, 12, 0, 0),
    STADIUM("Стадіон / Культура", "Цивільні", "Довіра + · Тривалість життя +", 620, 30, 2, false, false, 0, 25, -8, 0, 0.3, 10, 2, 0),
    FIRE_STATION("Пожежна частина", "Цивільні", "Рятувальники · Нижчі збитки від криз", 580, 32, 1, false, false, 0, 30, -5, 0, 0.1, 12, 0, 0),
    ECO_PARK("Лісовідновлення / Переробка", "Екологія", "Екологія ++ · Тривалість життя +", 540, 26, 2, false, false, 0, 20, -4, 0, 0.2, 8, 16, 0),

    // Coastal-only Maritime Facilities
    PORT("Портовий термінал", "Море", "Лише на узбережжі · Морський експорт", 1180, 54, 3, true, true, 0, 65, -14, 0, 1.3, 0, -4, 110),
    NAVAL_BASE("Військово-морська база", "Море", "Лише на узбережжі · Відкриває ВМС та екіпажі", 1400, 62, 3, true, false, 0, 50, -14, 0, 0.4, 4, -2, 0);

    public final String title;
    public final String group;
    public final String summary;
    public final int baseCost;
    public final int upkeep;
    public final int landUnits;
    public final boolean coastalOnly;
    public final boolean wearsOut;
    public final int housing;
    public final int jobs;
    public final int energyDelta;
    public final int foodSupply;
    public final double productivityWeight;
    public final int serviceScore;
    public final int ecologyDelta;
    public final double exportIncome;

    BuildingType(String title, String group, String summary, int baseCost, int upkeep,
                 int landUnits, boolean coastalOnly, boolean wearsOut,
                 int housing, int jobs, int energyDelta, int foodSupply,
                 double productivityWeight, int serviceScore, int ecologyDelta, double exportIncome) {
        this.title = title;
        this.group = group;
        this.summary = summary;
        this.baseCost = baseCost;
        this.upkeep = upkeep;
        this.landUnits = landUnits;
        this.coastalOnly = coastalOnly;
        this.wearsOut = wearsOut;
        this.housing = housing;
        this.jobs = jobs;
        this.energyDelta = energyDelta;
        this.foodSupply = foodSupply;
        this.productivityWeight = productivityWeight;
        this.serviceScore = serviceScore;
        this.ecologyDelta = ecologyDelta;
        this.exportIncome = exportIncome;
    }

    public int costFor(int existingCount) {
        double scaled = baseCost * Math.pow(1.14, Math.max(0, existingCount));
        return (int) (Math.round(scaled / 10.0) * 10);
    }
}
