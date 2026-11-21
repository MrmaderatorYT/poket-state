package com.ccs.poketstate.sim;

/** Ready-made macroeconomic scenarios for gameplay variety and thesis experiments. */
public enum ScenarioType {
    SANDBOX("Стандартний старт", "Збалансоване місто-держава для вільної гри та експериментів."),
    WAR_ECONOMY("Воєнна економіка", "Високе навантаження на оборону, дефіцит енергії й ризик інфляції."),
    HYPERINFLATION("Гіперінфляція 90-х", "Інфляція 22%, слабка валюта, велика тінь і порожні ЗВР."),
    ASIAN_TIGER("Економічне диво (Сінгапур)", "Низькі податки, ЗВТ, ставка на технопарки й антикорупцію."),
    DEBT_CRISIS("Боргова криза", "Борг >110% ВВП, тиск кредиторів і висока облікова ставка.");

    public final String title;
    public final String desc;

    ScenarioType(String title, String desc) {
        this.title = title;
        this.desc = desc;
    }
}
