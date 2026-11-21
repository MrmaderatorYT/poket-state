package com.ccs.poketstate.sim;

/** Special economic zones assignable independently to each region. */
public enum EconomicZone {
    STANDARD("Стандартна", "Збалансовані умови", 1.00, 1.00, 0.0, 0.0, 0.0),
    AGRARIAN("Аграрна", "Їжа +35% · Стабільні ціни", 0.94, 1.35, 1.5, 0.8, 0.0),
    INDUSTRIAL("Промислова", "ВВП +22% · Екологія -", 1.22, 0.85, -4.5, -1.0, 0.0),
    TECH_FINANCE("IT / Фінансова", "ВВП +28% · Зарплати +", 1.28, 0.90, 0.5, 1.0, 0.008),
    FREE_PORT("Вільний порт", "Експорт + · Міграція +", 1.16, 1.05, -1.0, 0.5, 0.012),
    RESIDENTIAL("Житлова", "Довіра + · Приріст жителів", 0.96, 1.00, 3.0, 2.4, 0.015);

    public final String title;
    public final String desc;
    public final double gdpMult;
    public final double foodMult;
    public final double ecologyDelta;
    public final double trustBonus;
    public final double migrationBonus;

    EconomicZone(String title, String desc, double gdpMult, double foodMult,
                 double ecologyDelta, double trustBonus, double migrationBonus) {
        this.title = title;
        this.desc = desc;
        this.gdpMult = gdpMult;
        this.foodMult = foodMult;
        this.ecologyDelta = ecologyDelta;
        this.trustBonus = trustBonus;
        this.migrationBonus = migrationBonus;
    }
}
