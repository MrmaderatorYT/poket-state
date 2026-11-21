package com.ccs.poketstate.sim;

import java.util.Locale;

/** One monthly macroeconomic, institutional & security observation for charts, A/B comparison, and CSV export. */
public final class CitySnapshot {
    public final int month;
    public final int population;
    public final double treasury;
    public final double debt;
    public final double gdp;
    public final double gdpPerCapita;
    public final double inflationPct;
    public final double cpi;
    public final double unemploymentPct;
    public final double trust;
    public final double realWage;
    public final double gini;
    public final int taxRate;
    public final int policyRate;
    public final int tradeRegime;
    public final double defensePower;
    public final int troops;
    public final int vehicles;
    public final double fxRate;
    public final double fxReserves;
    public final double corruption;
    public final double shadowEconomy;
    public final double epidemicLevel;
    public final double parliamentSupport;
    public final double terrorThreat;
    public final int occupiedRegions;
    public final double priceEnergy;
    public final double priceGrain;
    public final double priceMetal;
    public final double priceTech;
    public final String note;

    public CitySnapshot(int month, int population, double treasury, double debt, double gdp,
                        double gdpPerCapita, double inflationPct, double cpi, double unemploymentPct,
                        double trust, double realWage, double gini, int taxRate, int policyRate,
                        int tradeRegime, double defensePower, int troops, int vehicles,
                        double fxRate, double fxReserves, double corruption, double shadowEconomy,
                        double epidemicLevel, double parliamentSupport, double terrorThreat, int occupiedRegions,
                        double priceEnergy, double priceGrain, double priceMetal, double priceTech,
                        String note) {
        this.month = month;
        this.population = population;
        this.treasury = treasury;
        this.debt = debt;
        this.gdp = gdp;
        this.gdpPerCapita = gdpPerCapita;
        this.inflationPct = inflationPct;
        this.cpi = cpi;
        this.unemploymentPct = unemploymentPct;
        this.trust = trust;
        this.realWage = realWage;
        this.gini = gini;
        this.taxRate = taxRate;
        this.policyRate = policyRate;
        this.tradeRegime = tradeRegime;
        this.defensePower = defensePower;
        this.troops = troops;
        this.vehicles = vehicles;
        this.fxRate = fxRate;
        this.fxReserves = fxReserves;
        this.corruption = corruption;
        this.shadowEconomy = shadowEconomy;
        this.epidemicLevel = epidemicLevel;
        this.parliamentSupport = parliamentSupport;
        this.terrorThreat = terrorThreat;
        this.occupiedRegions = occupiedRegions;
        this.priceEnergy = priceEnergy;
        this.priceGrain = priceGrain;
        this.priceMetal = priceMetal;
        this.priceTech = priceTech;
        this.note = note == null ? "" : note;
    }

    public static String csvHeader() {
        return "month,population,treasury,debt,gdp,gdp_per_capita,inflation_pct,cpi,unemployment_pct,trust,real_wage,gini,avg_tax_rate,policy_rate,trade_regime,defense_power,troops,vehicles,fx_rate,fx_reserves,corruption,shadow_econ,epidemic,parliament,terror_threat,occupied_regions,p_energy,p_grain,p_metal,p_tech,note\n";
    }

    public String toCsvRow() {
        String safeNote = note.replace(',', ';').replace('\n', ' ');
        return String.format(Locale.ROOT,
                "%d,%d,%.2f,%.2f,%.2f,%.2f,%.2f,%.3f,%.2f,%.1f,%.2f,%.3f,%d,%d,%d,%.1f,%d,%d,%.2f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%d,%.2f,%.2f,%.2f,%.2f,%s\n",
                month, population, treasury, debt, gdp, gdpPerCapita, inflationPct, cpi,
                unemploymentPct, trust, realWage, gini, taxRate, policyRate, tradeRegime,
                defensePower, troops, vehicles, fxRate, fxReserves, corruption, shadowEconomy,
                epidemicLevel, parliamentSupport, terrorThreat, occupiedRegions,
                priceEnergy, priceGrain, priceMetal, priceTech, safeNote);
    }
}
