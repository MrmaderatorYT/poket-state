package com.ccs.poketstate.sim;

import java.util.EnumMap;
import java.util.Map;

/** Regional oblast with land capacity, coastal flag, individual buildings, wear, zone, and custom tax. */
public final class RegionState {
    public String name;
    public int population;
    public int taxRate;
    public EconomicZone zone;
    public boolean coastal;
    public int landArea;
    public int leasedZones;
    public double wear; // 0..100% wear on energy & industrial facilities
    public double gdp;
    public double trust;
    public double unemploymentPct;
    public double healthScore;
    public double eduScore;
    public double securityScore;
    public boolean occupied;
    public double terrorStrength;
    public final Map<BuildingType, Integer> buildings = new EnumMap<>(BuildingType.class);

    public RegionState(String name, int population, int taxRate, EconomicZone zone, boolean coastal, int landArea) {
        this.name = name;
        this.population = population;
        this.taxRate = taxRate;
        this.zone = zone;
        this.coastal = coastal;
        this.landArea = landArea;
        this.leasedZones = 0;
        this.wear = 4.0;
        this.gdp = population * 2.5;
        this.trust = 74.0;
        this.unemploymentPct = 5.0;
        this.healthScore = 65.0;
        this.eduScore = 60.0;
        this.securityScore = 55.0;
        this.occupied = false;
        this.terrorStrength = 0.0;
        for (BuildingType b : BuildingType.values()) buildings.put(b, 0);
    }

    public int count(BuildingType b) {
        Integer v = buildings.get(b);
        return v == null ? 0 : v;
    }

    public void addBuilding(BuildingType b, int delta) {
        buildings.put(b, Math.max(0, count(b) + delta));
    }

    public int usedLand() {
        int u = 0;
        for (BuildingType b : BuildingType.values()) u += count(b) * b.landUnits;
        return u;
    }

    public int totalLand() {
        return landArea + leasedZones * 14;
    }

    /** Dense construction increases land acquisition & construction costs. */
    public double densityCostMultiplier() {
        double ratio = usedLand() / (double) Math.max(10, totalLand());
        if (ratio <= 0.55) return 1.0;
        return 1.0 + (ratio - 0.55) * 1.15;
    }

    /** Size & land scale factor for regional development potential. */
    public double sizeDevelopmentFactor() {
        return 0.85 + Math.min(0.65, landArea / 160.0);
    }
}
