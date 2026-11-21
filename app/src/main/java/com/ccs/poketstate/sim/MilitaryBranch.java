package com.ccs.poketstate.sim;

/** Ukrainian defense, security, intelligence, and special-operations branches. */
public enum MilitaryBranch {
    ZSU("ЗСУ", "Сухопутні війська · Основна сила оборони", 25, 220, 14, 1.30, 0.2),
    TRO("ТрО / Нацгвардія", "Територіальна оборона · Локальний захист", 25, 130, 8, 0.85, 0.4),
    SSO("ССО", "Сили спецоперацій · Рейди в тил противника", 15, 340, 22, 2.40, 0.3),
    CSP("ЦСП «Альфа» (СБУ)", "Центр спецоперацій · Антитерор та диверсії", 10, 360, 24, 2.55, 1.4),
    GUR("ГУР", "Воєнна розвідка · Зменшує шкоду від криз", 10, 320, 20, 1.80, 1.2),
    AIR_FORCE("Повітряні сили / ППО", "ППО та авіація · Прикриття неба", 15, 420, 28, 2.75, 0.2),
    NAVY("ВМС (Екіпажі)", "Морські сили · Екіпажі кораблів і десант", 15, 340, 22, 2.00, 0.2),
    KORD("КОРД / Поліція", "Внутрішня безпека та порядок", 15, 260, 16, 1.25, 1.1);

    public final String title;
    public final String desc;
    public final int batchSize;
    public final int batchCost;
    public final int monthlyUpkeepPerBatch;
    public final double powerPerPerson;
    public final double securityPerPerson;

    MilitaryBranch(String title, String desc, int batchSize, int batchCost,
                   int monthlyUpkeepPerBatch, double powerPerPerson, double securityPerPerson) {
        this.title = title;
        this.desc = desc;
        this.batchSize = batchSize;
        this.batchCost = batchCost;
        this.monthlyUpkeepPerBatch = monthlyUpkeepPerBatch;
        this.powerPerPerson = powerPerPerson;
        this.securityPerPerson = securityPerPerson;
    }
}
