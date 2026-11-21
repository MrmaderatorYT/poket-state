package com.ccs.poketstate.sim;

/** Fictional neighbor country for Diplomacy 2.0, Trade 2.0, Embassies, Aid, and Wars. */
public final class NeighborCountry {
    public final String id;
    public final String name;
    public final String commander;
    public final boolean coastal;
    public int relations;         // -100..+100
    public double militaryPower;
    public double gdp;
    public int regionsCount;
    public int populationPerRegion;

    // Trade 2.0 & Diplomacy
    public int tariffPct;
    public boolean customsUnion;
    public int tradeDealDaysLeft; // Real-time equivalent days/hours displayed
    public boolean embassy;
    public boolean culturalCenter;
    public boolean leasedIndustrialZone;
    public int aidSharePct;       // Uneven foreign aid distribution weight

    // War & Blockade
    public boolean atWar;
    public boolean seaBlockaded;
    public boolean capitulated;
    public int warProgress;       // 0..100

    public NeighborCountry(String id, String name, String commander, boolean coastal,
                           int relations, double militaryPower, double gdp,
                           int regionsCount, int populationPerRegion) {
        this.id = id;
        this.name = name;
        this.commander = commander;
        this.coastal = coastal;
        this.relations = relations;
        this.militaryPower = militaryPower;
        this.gdp = gdp;
        this.regionsCount = regionsCount;
        this.populationPerRegion = populationPerRegion;
        this.tariffPct = 10;
        this.customsUnion = false;
        this.tradeDealDaysLeft = 0;
        this.embassy = false;
        this.culturalCenter = false;
        this.leasedIndustrialZone = false;
        this.aidSharePct = 25;
        this.atWar = false;
        this.seaBlockaded = false;
        this.capitulated = false;
        this.warProgress = 0;
    }
}
