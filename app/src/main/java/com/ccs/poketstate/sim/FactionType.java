package com.ccs.poketstate.sim;

/** Four parliamentary factions shaping governance and 24-month elections. */
public enum FactionType {
    LIBERALS("Ліберали", "Низькі податки, ЗВТ, приватизація, ринок землі"),
    SOCIALISTS("Соціал-демократи", "Соцвиплати, мінімальна з/п, податок на розкіш"),
    TECHNOCRATS("Технократи", "Освіта, антикорупція, стабільний курс, низька інфляція"),
    SECUROCRATS("Силовики", "ЗСУ, ССО, ГУР, оборона та безпека");

    public final String title;
    public final String agenda;

    FactionType(String title, String agenda) {
        this.title = title;
        this.agenda = agenda;
    }
}
