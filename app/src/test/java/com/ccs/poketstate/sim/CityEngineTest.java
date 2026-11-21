package com.ccs.poketstate.sim;

import org.junit.Test;

import static org.junit.Assert.*;

public final class CityEngineTest {

    @Test
    public void buildingUpdatesCapacityAndDeductsTreasury() {
        CityEngine e = new CityEngine(42L);
        int capBefore = e.housingCapacity();
        double cashBefore = e.treasury;
        int cost = BuildingType.HOUSING.costFor(e.count(BuildingType.HOUSING));

        String res = e.build(BuildingType.HOUSING);
        assertTrue(res.contains("Побудовано"));
        assertEquals(capBefore + BuildingType.HOUSING.housing, e.housingCapacity());
        assertEquals(cashBefore - cost, e.treasury, 0.01);
    }

    @Test
    public void helicopterMoneyRaisesInflationVsBaseline() {
        CityEngine base = new CityEngine(101L);
        CityEngine shock = new CityEngine(101L);

        shock.applyShock(CityEngine.ShockType.HELICOPTER_MONEY);
        for (int i = 0; i < 3; i++) {
            base.nextMonth();
            shock.nextMonth();
        }
        assertTrue("Helicopter money must raise inflation", shock.inflationPct > base.inflationPct + 1.0);
        assertTrue("Helicopter money must raise CPI", shock.cpi > base.cpi);
    }

    @Test
    public void techBoomBoostsGdpAndCoolsInflation() {
        CityEngine base = new CityEngine(202L);
        CityEngine boom = new CityEngine(202L);

        boom.applyShock(CityEngine.ShockType.TECH_BOOM);
        for (int i = 0; i < 3; i++) {
            base.nextMonth();
            boom.nextMonth();
        }
        assertTrue("Tech boom must increase GDP", boom.gdp > base.gdp * 1.08);
        assertTrue("Tech boom supply boost should cool inflation", boom.inflationPct < base.inflationPct);
    }

    @Test
    public void generalStrikeCausesStagflation() {
        CityEngine base = new CityEngine(303L);
        CityEngine strike = new CityEngine(303L);

        strike.applyShock(CityEngine.ShockType.GENERAL_STRIKE);
        base.nextMonth();
        strike.nextMonth();

        assertTrue("Strike must reduce GDP", strike.gdp < base.gdp);
        assertTrue("Strike must raise inflation", strike.inflationPct > base.inflationPct);
    }

    @Test
    public void highPolicyRateCoolsInflation() {
        CityEngine loose = new CityEngine(404L);
        CityEngine tight = new CityEngine(404L);
        loose.policyRate = 2;
        tight.policyRate = 20;

        for (int i = 0; i < 4; i++) {
            loose.nextMonth();
            tight.nextMonth();
        }
        assertTrue("Higher policy rate must lower inflation", tight.inflationPct < loose.inflationPct - 2.0);
    }

    @Test
    public void ftaBeatsProtectionismOnGdp() {
        CityEngine fta = new CityEngine(505L);
        CityEngine closed = new CityEngine(505L);
        fta.tradeRegime = 0;
        closed.tradeRegime = 2;

        for (int i = 0; i < 4; i++) {
            fta.nextMonth();
            closed.nextMonth();
        }
        assertTrue("FTA should deliver higher GDP", fta.gdp > closed.gdp * 1.15);
    }

    @Test
    public void csvExportAndEventResolutionWork() {
        CityEngine e = new CityEngine(606L);
        assertNotNull(e.currentEvent);
        String out = e.resolveEvent(0);
        assertFalse(out.isEmpty());
        assertNull(e.currentEvent);

        e.nextMonth();
        e.nextMonth();
        String csv = e.exportCsv();
        String[] lines = csv.trim().split("\n");
        assertEquals(4, lines.length); // header + 3 monthly rows
        assertTrue(lines[0].startsWith("month,population,treasury,debt,gdp"));
        assertTrue(lines[0].contains("defense_power,troops,vehicles"));
    }

    @Test
    public void regionsCustomTaxAndZonesAffectGdpAndFounding() {
        CityEngine base = new CityEngine(707L);
        CityEngine boosted = new CityEngine(707L);

        assertEquals(3, boosted.regions.size());
        boosted.setRegionZone(0, EconomicZone.TECH_FINANCE);
        boosted.setRegionTax(0, 10);
        boosted.setRegionTax(1, 35);
        assertEquals(10, boosted.regions.get(0).taxRate);
        assertEquals(35, boosted.regions.get(1).taxRate);

        base.nextMonth();
        boosted.nextMonth();
        assertTrue("IT/Finance zone + lower tax must lift region 0 GDP",
                boosted.regions.get(0).gdp > base.regions.get(0).gdp * 1.15);

        String foundMsg = boosted.foundRegion();
        assertTrue(foundMsg.contains("Засновано регіон"));
        assertEquals(4, boosted.regions.size());
    }

    @Test
    public void ministriesMilitaryAndVehiclesWorkTogether() {
        CityEngine e = new CityEngine(808L);
        e.treasury = 10000;

        double powerBefore = e.defensePower();
        e.upgradeMinistry(MinistryType.DEFENSE);
        assertEquals(1, e.ministryLevel(MinistryType.DEFENSE));
        assertTrue("Defense ministry should raise combat power", e.defensePower() > powerBefore);

        double afterMin = e.defensePower();
        e.recruit(MilitaryBranch.SSO);
        e.recruit(MilitaryBranch.GUR);
        assertEquals(MilitaryBranch.SSO.batchSize, e.troops(MilitaryBranch.SSO));
        assertEquals(MilitaryBranch.GUR.batchSize, e.troops(MilitaryBranch.GUR));
        assertTrue("SSO + GUR must raise combat power", e.defensePower() > afterMin);

        double beforeTank = e.defensePower();
        e.buyVehicle(VehicleType.TANK);
        e.buyVehicle(VehicleType.TRUCK);
        assertEquals(1, e.vehicleCount(VehicleType.TANK));
        assertTrue("Tank must raise combat power", e.defensePower() > beforeTank + 70);

        double cashBeforeSell = e.treasury;
        e.sellVehicle(VehicleType.TANK);
        assertEquals(0, e.vehicleCount(VehicleType.TANK));
        assertTrue("Selling tank refunds cash", e.treasury > cashBeforeSell);

        e.dismiss(MilitaryBranch.SSO);
        assertEquals(0, e.troops(MilitaryBranch.SSO));
    }

    @Test
    public void lawsParliamentAndElectionsWork() {
        CityEngine e = new CityEngine(909L);
        double libBefore = e.factionSupport(FactionType.LIBERALS);
        e.toggleLaw(LawType.PRIVATIZATION);
        e.toggleLaw(LawType.LAND_MARKET);
        assertTrue(e.hasLaw(LawType.PRIVATIZATION));
        assertTrue("Liberals should support privatization & land market",
                e.factionSupport(FactionType.LIBERALS) > libBefore + 10);

        // Advance to Month 24 election with healthy economy -> win re-election
        for (int i = 0; i < 23; i++) e.nextMonth();
        assertEquals(24, e.month);
        assertEquals(1, e.electionsWon);
        assertTrue("Healthy city should win election >= 50%", e.lastElectionVotePct >= 50.0);
    }

    @Test
    public void fxInterventionsAntiCorruptionEpidemicAndScenariosWork() {
        CityEngine base = new CityEngine(1010L);
        CityEngine reform = new CityEngine(1010L);
        reform.toggleLaw(LawType.ANTI_CORRUPTION);
        for (int i = 0; i < 6; i++) {
            base.nextMonth();
            reform.nextMonth();
        }
        assertTrue("Anti-corruption bureau must lower corruption", reform.corruption < base.corruption - 5.0);
        assertTrue("Anti-corruption bureau must shrink shadow economy", reform.shadowEconomy < base.shadowEconomy - 2.0);

        // FX intervention
        double fxBefore = reform.fxRate;
        double resBefore = reform.fxReserves;
        reform.fxIntervene(-50);
        assertTrue("Selling reserves strengthens currency", reform.fxRate < fxBefore);
        assertEquals(resBefore - 50.0, reform.fxReserves, 0.1);

        // Epidemic containment
        reform.applyShock(CityEngine.ShockType.EPIDEMIC);
        assertTrue(reform.epidemicLevel > 30.0);
        reform.toggleQuarantine();
        reform.toggleVaccination();
        double peak = reform.epidemicLevel;
        reform.nextMonth();
        reform.nextMonth();
        assertTrue("Quarantine + vaccination must push epidemic down", reform.epidemicLevel < peak - 15.0);

        // Scenarios + A/B baseline
        reform.captureBaseline();
        assertTrue(reform.hasBaseline());
        reform.startScenario(ScenarioType.HYPERINFLATION);
        assertTrue(reform.inflationPct > 20.0);
        assertTrue(reform.exportCsv().contains("fx_rate,fx_reserves,corruption,shadow_econ"));
    }

    @Test
    public void terroristsCanSeizeAndArmyCanLiberateRegion() {
        CityEngine e = new CityEngine(1111L);
        assertEquals(0, e.occupiedRegions());

        // Weak defense -> terrorist raid seizes a region
        e.applyShock(CityEngine.ShockType.TERROR_RAID);
        assertEquals("Terrorist raid should seize a region when defense is low", 1, e.occupiedRegions());
        assertTrue("Urgent crisis log must be recorded", e.latestLog().urgent);

        int occIdx = -1;
        for (int i = 0; i < e.regions.size(); i++) if (e.regions.get(i).occupied) occIdx = i;
        assertTrue(occIdx >= 0);

        // Build up SSO + Tanks so liberation chance becomes very high (~96%)
        e.treasury = 12000;
        e.recruit(MilitaryBranch.SSO);
        e.recruit(MilitaryBranch.GUR);
        e.buyVehicle(VehicleType.TANK);
        e.buyVehicle(VehicleType.TANK);
        assertTrue("Strong army must raise liberation chance >= 85%", e.liberationChancePct(occIdx) >= 85);

        // Attempt liberation (with 96% chance, at most 3 tries guarantees success deterministically)
        for (int tries = 0; tries < 3 && e.regions.get(occIdx).occupied; tries++) {
            e.liberateRegion(occIdx);
        }
        assertEquals("Region should be liberated", 0, e.occupiedRegions());
    }

    @Test
    public void parliamentSeatsVotingAndCoalitionNegotiationWork() {
        CityEngine e = new CityEngine(1212L);
        int sum = 0;
        for (FactionType f : FactionType.values()) sum += e.seats(f);
        assertEquals(100, sum);

        // Martial law in peacetime should be rejected by parliament
        assertTrue(e.forecastLawVotes(LawType.MARTIAL_LAW) < 51);
        String rej = e.toggleLaw(LawType.MARTIAL_LAW);
        assertTrue(rej.contains("відхилив"));
        assertFalse(e.hasLaw(LawType.MARTIAL_LAW));

        // Coalition negotiations boost vote count
        int before = e.forecastLawVotes(LawType.LUXURY_TAX);
        e.negotiateCoalition();
        assertTrue(e.forecastLawVotes(LawType.LUXURY_TAX) > before);
    }

    @Test
    public void regionalEnergyCoastalPortsRoiWearAndWorkforceWork() {
        CityEngine e = new CityEngine(1313L);
        e.treasury = 20000;
        assertEquals("Shliakhiv", e.regions.get(1).name);

        // Non-coastal region rejects PORT
        String reject = e.buildInRegion(0, BuildingType.PORT);
        assertTrue(reject.contains("приморській"));

        // Coastal region accepts AES, OIL_REFINERY, NAVAL_BASE and reports ROI
        int roi = e.estimateRoiMonths(2, BuildingType.OIL_REFINERY);
        assertTrue("Oil refinery must have positive ROI months", roi > 0);
        e.buildInRegion(2, BuildingType.AES);
        e.buildInRegion(2, BuildingType.OIL_REFINERY);
        e.buildInRegion(2, BuildingType.NAVAL_BASE);
        assertTrue(e.count(BuildingType.AES) >= 1);
        assertTrue(e.energyBalance() > 150);

        // Wear accumulates and 1-button repair clears it
        for (int i = 0; i < 6; i++) e.nextMonth();
        assertTrue(e.maxRegionWear() > 10.0);
        assertTrue(e.totalRepairCost() > 0);
        e.repairAllInfrastructure();
        assertEquals(0.0, e.maxRegionWear(), 0.01);

        // Workforce: foreign specialists, repatriation, sending abroad
        int spBefore = e.specialists;
        e.hireForeignSpecialists();
        assertEquals(spBefore + 25, e.specialists);
        e.sendSpecialistsAbroad();
        assertEquals(15, e.specialistsAbroad);
    }

    @Test
    public void navyCspPmcDiplomacyUnionSovereignFundAndCheckpointsWork() {
        CityEngine e = new CityEngine(1414L);
        e.treasury = 50000;

        // Navy & Coast Guard
        e.buildInRegion(2, BuildingType.NAVAL_BASE);
        e.buyNavalUnit(NavalUnit.COAST_GUARD);
        e.buyNavalUnit(NavalUnit.FRIGATE);
        assertTrue(e.navalPower() > 50);
        int relBefore = e.neighbors.get(0).relations;
        e.friendlyNavalVisit(0);
        assertTrue(e.neighbors.get(0).relations > relBefore);

        // CSP scales with GDP & PMC earns contract income
        int costLow = e.cspOperationCost();
        e.applyShock(CityEngine.ShockType.GDP_BOOM);
        assertTrue("CSP op cost must scale with GDP", e.cspOperationCost() > costLow);
        e.recruit(MilitaryBranch.CSP);
        e.runCspOperation(-1);

        e.hirePmcSquad();
        e.togglePmcContract();
        assertTrue(e.pmc.monthlyContractIncome() > 0);

        // Sovereign fund endurance increases + custom debt repayment saves interest
        double endBefore = e.sovereignEnduranceMonths(0);
        e.contributeSovereignFund(400);
        assertTrue(e.sovereignEnduranceMonths(0) > endBefore);

        e.borrow(1500);
        assertTrue(e.estimateInterestSavedMonthly(500) > 0);
        e.repayCustom(500);
        assertEquals(1000.0, e.debt, 0.1);

        // ACB mole dormant + Partners daily gift + Checkpoints
        e.toggleAcbMole();
        assertTrue(e.acbMoleActive && e.acbMoleDormant);
        e.toggleAcbMole();
        assertFalse(e.acbMoleActive);

        double cashBeforeGift = e.treasury;
        e.claimPartnerGift(0);
        assertTrue(e.treasury > cashBeforeGift);

        e.saveCheckpoint("TestCP");
        assertFalse(e.checkpoints.isEmpty());
    }

    @Test
    public void modMenuSwitchesAndBoostsWork() {
        CityEngine e = new CityEngine(1515L);
        e.modFreeBuild = true;
        e.modAutoParliament = true;
        e.modGodMode = true;
        e.modNoWearTerror = true;

        assertEquals(0, e.buildingCostInRegion(0, BuildingType.AES));
        assertEquals(100, e.forecastLawVotes(LawType.MARTIAL_LAW));
        e.toggleLaw(LawType.MARTIAL_LAW);
        assertTrue(e.hasLaw(LawType.MARTIAL_LAW));

        e.modAddCash(100000);
        e.modMaxMinistries();
        e.modMaxArmyAndNavy();
        e.modJoinUnionAndPact();
        assertTrue(e.union.isUnionMember() && e.union.isPactMember());
        assertEquals(MinistryType.MAX_LEVEL, e.ministryLevel(MinistryType.DEFENSE));

        int mBefore = e.month;
        e.modFastForward(6);
        assertEquals(mBefore + 6, e.month);
        assertEquals(0.0, e.terrorThreat, 0.01);
        assertEquals(0.0, e.maxRegionWear(), 0.01);
    }
}
