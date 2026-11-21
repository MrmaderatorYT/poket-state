package com.ccs.poketstate.sim;

/** Purchasable municipal, logistics, and military vehicles. */
public enum VehicleType {
    // Civilian / Municipal
    BUS("Електроавтобус", "Громадський транспорт · Зайнятість + · Еко +", false, 320, 12, 0, 1.8, 1.5, 0),
    TRUCK("Вантажівка", "Логістика · ВВП + · Знижує дефіцит", false, 380, 14, 0, 2.6, 0.4, 0),
    AMBULANCE("Швидка допомога", "Екстрена медицина · Сервіси + · Довіра +", false, 420, 16, 0, 0.5, 2.5, 0),
    FIRE_TRUCK("Пожежне авто", "Цивільний захист · Безпека міста", false, 450, 16, 0, 0.4, 2.0, 6),
    CARGO_SHIP("Торгове судно", "Морський експорт · Дохід торгівлі +", false, 780, 26, 0, 5.2, 0.5, 0),

    // Military
    ARMORED_CAR("Бронеавтомобіль", "Мобільність ССО, КОРД та патрулів", true, 520, 18, 22, 0, 0.6, 4),
    APC("БТР / БМП", "Бронетехніка для ЗСУ та ТрО", true, 880, 28, 45, 0, 0.4, 8),
    TANK("Танк", "Важка ударна міць ЗСУ", true, 1450, 44, 85, 0, 0.2, 12),
    DRONE_AA("Комплекс ППО / БПЛА", "Розвідка ГУР і прикриття неба", true, 1150, 34, 72, 0, 0.8, 14),
    PATROL_BOAT("Патрульний катер", "Охорона узбережжя та ВМС", true, 960, 30, 54, 1.4, 0.5, 9);

    public final String title;
    public final String desc;
    public final boolean military;
    public final int cost;
    public final int upkeep;
    public final double combatPower;
    public final double economyBoost;
    public final double serviceBoost;
    public final double securityBoost;

    VehicleType(String title, String desc, boolean military, int cost, int upkeep,
                double combatPower, double economyBoost, double serviceBoost, double securityBoost) {
        this.title = title;
        this.desc = desc;
        this.military = military;
        this.cost = cost;
        this.upkeep = upkeep;
        this.combatPower = combatPower;
        this.economyBoost = economyBoost;
        this.serviceBoost = serviceBoost;
        this.securityBoost = securityBoost;
    }
}
