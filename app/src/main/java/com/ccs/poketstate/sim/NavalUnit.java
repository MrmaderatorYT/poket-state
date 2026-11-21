package com.ccs.poketstate.sim;

/** Naval fleet vessels from patrol boats & coast guard up to nuclear subs and aircraft carriers. */
public enum NavalUnit {
    PATROL_BOAT("Патрульний катер", "Охорона узбережжя · Швидкий захист", 620, 22, 15, 45, 0.03, 1.2),
    COAST_GUARD("Корабель берегової охорони", "Притискає контрабанду й тіньову економіку", 860, 28, 20, 58, 0.04, 3.8),
    FRIGATE("Фрегат", "Контроль моря · Підтримка морського десанту", 1650, 56, 45, 135, 0.08, 1.8),
    NUCLEAR_SUB("Атомний підводний човен", "Стратегічне стримування · Висока морська міць", 3400, 110, 65, 290, 0.06, 1.0),
    AIRCRAFT_CARRIER("Авіаносець", "Флагман флоту · Абсолютне панування на морі", 6200, 195, 140, 560, 0.14, 2.0);

    public final String title;
    public final String desc;
    public final int cost;
    public final int upkeep;
    public final int crewNeeded;
    public final double navalPower;
    public final double tradeBonus;
    public final double shadowReduction;

    NavalUnit(String title, String desc, int cost, int upkeep, int crewNeeded,
              double navalPower, double tradeBonus, double shadowReduction) {
        this.title = title;
        this.desc = desc;
        this.cost = cost;
        this.upkeep = upkeep;
        this.crewNeeded = crewNeeded;
        this.navalPower = navalPower;
        this.tradeBonus = tradeBonus;
        this.shadowReduction = shadowReduction;
    }
}
