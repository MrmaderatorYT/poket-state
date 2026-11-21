package com.ccs.poketstate.sim;

import com.ccs.poketstate.i18n.Strings;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Full macroeconomic, regional, ministerial, naval, geopolitical & counter-terror simulation engine.
 */
public final class CityEngine {
    public enum ShockType {
        HELICOPTER_MONEY("Гелікоптерні гроші", "", "+готівка людям, сплеск попиту й інфляції"),
        BANK_PANIC("Банківська паніка", "", "відтік ліквідності, падіння довіри й ВВП"),
        TECH_BOOM("Технологічний бум", "", "шок пропозиції: +продуктивність, дезінфляція"),
        GENERAL_STRIKE("Страйк і блокада", "", "падіння випуску + стагфляція"),
        EPIDEMIC("Спалах епідемії", "", "хвороба знижує працю й довіру (МОЗ / вакцинація)"),
        CYBER_ATTACK("Кібератака / диверсія", "", "удар по банках і мережі (відбивають ГУР і КОРД)"),
        TERROR_RAID("Терористичний рейд", "", "бойовики атакують і можуть захопити регіон"),
        GDP_BOOM("Вибух ВВП (Лабораторія)", "", "експериментальний стрибок продуктивності ×2.2");

        public final String title;
        public final String icon;
        public final String desc;

        ShockType(String title, String icon, String desc) {
            this.title = title;
            this.icon = icon;
            this.desc = desc;
        }
    }

    public enum GameStatus { PLAYING, IMPEACHED, DEFAULTED, LOST_ELECTION, OVERRUN }

    public static final class Checkpoint {
        public final int month;
        public final String label;
        public final double gdp;
        public final double treasury;
        public final String snapshotJson;

        public Checkpoint(int month, String label, double gdp, double treasury, String snapshotJson) {
            this.month = month;
            this.label = label;
            this.gdp = gdp;
            this.treasury = treasury;
            this.snapshotJson = snapshotJson;
        }
    }

    public static final class OffBudgetLine {
        public final String title;
        public final String actionHint;
        public final int targetTab;
        public final int targetSubTab;

        public OffBudgetLine(String title, String actionHint, int targetTab, int targetSubTab) {
            this.title = title;
            this.actionHint = actionHint;
            this.targetTab = targetTab;
            this.targetSubTab = targetSubTab;
        }
    }

    public static final String[] TRADE_LABELS = {"ЗВТ (0%)", "Мита (15%)", "Санкції (40%)"};
    private static final String[] EXTRA_REGIONS = {"Northern", "Steppe", "Western", "Podilskyi", "Carpathian", "Tavrian"};
    public static final String[] SERVICE_NAMES = {"Поліція / КОРД", "Нацгвардія / ТрО", "Рятувальники", "СБФ / ВМС"};
    private static final String[] DOSSIER_NAMES = {"O. Kovalenko", "I. Marchenko", "V. Sydorenko", "T. Bondar", "M. Kravchuk"};

    public String cityName = "Poket State";
    public ScenarioType scenario = ScenarioType.SANDBOX;
    public int month = 1;
    public int population = 620;
    public double treasury = 2800.0;
    public double debt = 0.0;
    public double lastNetIncome = 120.0;
    public double lastTaxRevenue = 280.0;
    public double lastTotalExpense = 160.0;
    public double sovereignFund = 350.0;
    public boolean roadFundEnabled = false;

    // Macroeconomic indicators (uncapped!)
    public double gdp = 1850.0;
    public double gdpPerCapita = 2.98;
    public double inflationPct = 2.6;
    public double cpi = 1.000;
    public double unemploymentPct = 5.4;
    public double trust = 74.0;
    public double ecology = 78.0;
    public double tfp = 1.00;
    public double realWage = 2.85;
    public double gini = 0.31;
    public double lifeExpectancy = 72.4;

    // Policy levers + 2-month lagged policy rate
    public int taxRate = 20;
    public int policyRate = 6;
    public int laggedRate1 = 6;
    public int laggedRate2 = 6;
    public int welfareLevel = 35;
    public int tradeRegime = 0;

    // Currency, FX Reserves & Commodity Prices
    public double fxRate = 40.0;
    public double fxParity = 40.0;
    public double fxReserves = 420.0;
    public double fxBuyLimitLeft = 200.0;
    public long lastSavedEpochMs = 0L;
    public boolean fixedFx = false;
    public double priceEnergy = 1.00;
    public double priceGrain = 1.00;
    public double priceMetal = 1.00;
    public double priceTech = 1.00;

    // Workforce & Specialists
    public int specialists = 45;
    public int foreignSpecialists = 0;
    public int specialistsAbroad = 0;

    // Corruption, ACB Dossier & Mole ("Своя людина в АКБ")
    public double corruption = 24.0;
    public double shadowEconomy = 16.0;
    public double acbAttention = 12.0;
    public int acbCooldown = 0;
    public boolean acbMoleActive = false;
    public boolean acbMoleDormant = true; // "на дні" = 0 risk!
    public int dossierIndex = 0;

    // Crisis & Terrorism
    public double epidemicLevel = 0.0;
    public boolean quarantine = false;
    public boolean vaccination = false;
    public double terrorThreat = 14.0;

    // Parliament (100 seats) & Elections
    public int electionsWon = 0;
    public double lastElectionVotePct = 0.0;
    public int coalitionBonus = 0;
    public final Map<FactionType, Double> factions = new EnumMap<>(FactionType.class);
    public final Map<FactionType, Integer> parliamentSeats = new EnumMap<>(FactionType.class);

    // Laws, Regions, Ministries, Army, Navy, PMC, Neighbors, Union, Partners
    public final Set<LawType> laws = EnumSet.noneOf(LawType.class);
    public final List<RegionState> regions = new ArrayList<>();
    public final Map<MinistryType, Integer> ministries = new EnumMap<>(MinistryType.class);
    public final Map<MilitaryBranch, Integer> military = new EnumMap<>(MilitaryBranch.class);
    public final Map<VehicleType, Integer> vehicles = new EnumMap<>(VehicleType.class);
    public final Map<NavalUnit, Integer> fleet = new EnumMap<>(NavalUnit.class);
    public final int[] serviceEquipment = {4, 4, 3, 2};
    public final int[] serviceInTransit = {0, 0, 0, 0};
    public final PmcState pmc = new PmcState();
    public final UnionState union = new UnionState();
    public final List<NeighborCountry> neighbors = new ArrayList<>();
    public final int[] partnerLastClaimMonth = {0, 0};

    // Mod Menu flags (sandbox / testing)
    public boolean modFreeBuild = false;
    public boolean modAutoParliament = false;
    public boolean modGodMode = false;
    public boolean modNoWearTerror = false;

    // Shock impulses
    public double moneyImpulse = 0.0;
    public double supplyShock = 0.0;
    public double panicImpulse = 0.0;
    private int lowTrustStreak = 0;
    private int stagnantTreasuryStreak = 0;
    private String lastMainExpense = "Утримання будівель";

    public GameStatus status = GameStatus.PLAYING;
    public CityEvent currentEvent;
    public String lastEventId = "";
    private String pendingNote = "Заснування міста";

    public final List<CitySnapshot> history = new ArrayList<>();
    public final List<CitySnapshot> baselineHistory = new ArrayList<>();
    public final List<Checkpoint> checkpoints = new ArrayList<>();
    public final Deque<String> chronicle = new ArrayDeque<>();
    public final Deque<LogEntry> logs = new ArrayDeque<>();
    private final Random rng;

    public CityEngine(long seed) {
        this.rng = new Random(seed);
        reset();
    }

    public void reset() {
        startScenario(ScenarioType.SANDBOX);
    }

    public void startScenario(ScenarioType type) {
        this.scenario = type != null ? type : ScenarioType.SANDBOX;
        cityName = "Poket State";
        month = 1;
        population = 620;
        treasury = 2800.0;
        debt = 0.0;
        lastNetIncome = 120.0;
        lastTaxRevenue = 280.0;
        lastTotalExpense = 160.0;
        sovereignFund = 350.0;
        roadFundEnabled = false;
        inflationPct = 2.6;
        cpi = 1.000;
        trust = 74.0;
        ecology = 78.0;
        tfp = 1.00;
        gini = 0.31;
        lifeExpectancy = 72.4;
        taxRate = 20;
        policyRate = 6;
        laggedRate1 = 6;
        laggedRate2 = 6;
        welfareLevel = 35;
        tradeRegime = 0;

        fxRate = 40.0;
        fxParity = 40.0;
        fxReserves = 420.0;
        fxBuyLimitLeft = 200.0;
        lastSavedEpochMs = System.currentTimeMillis();
        fixedFx = false;
        priceEnergy = 1.00;
        priceGrain = 1.00;
        priceMetal = 1.00;
        priceTech = 1.00;

        specialists = 45;
        foreignSpecialists = 0;
        specialistsAbroad = 0;

        corruption = 24.0;
        shadowEconomy = 16.0;
        acbAttention = 12.0;
        acbCooldown = 0;
        acbMoleActive = false;
        acbMoleDormant = true;
        dossierIndex = 0;

        epidemicLevel = 0.0;
        quarantine = false;
        vaccination = false;
        terrorThreat = 14.0;

        electionsWon = 0;
        lastElectionVotePct = 0.0;
        coalitionBonus = 0;
        laws.clear();

        parliamentSeats.put(FactionType.LIBERALS, 29);
        parliamentSeats.put(FactionType.SOCIALISTS, 24);
        parliamentSeats.put(FactionType.TECHNOCRATS, 26);
        parliamentSeats.put(FactionType.SECUROCRATS, 21);

        moneyImpulse = 0.0;
        supplyShock = 0.0;
        panicImpulse = 0.0;
        lowTrustStreak = 0;
        stagnantTreasuryStreak = 0;
        status = GameStatus.PLAYING;
        lastEventId = "";
        pendingNote = "Сценарій: " + scenario.title;

        // Regions (Зарічень -> Shliakhiv; Coastal is coastal!)
        regions.clear();
        RegionState r0 = new RegionState("Central", 260, 20, EconomicZone.STANDARD, false, 85);
        r0.addBuilding(BuildingType.HOUSING, 2);
        r0.addBuilding(BuildingType.HOSPITAL, 1);
        r0.addBuilding(BuildingType.ECO_PARK, 1);
        r0.addBuilding(BuildingType.COLLEGE, 1);

        RegionState r1 = new RegionState("Shliakhiv", 210, 22, EconomicZone.INDUSTRIAL, false, 95);
        r1.addBuilding(BuildingType.HOUSING, 1);
        r1.addBuilding(BuildingType.FACTORY, 2);
        r1.addBuilding(BuildingType.POWER_PLANT, 1);
        r1.addBuilding(BuildingType.HIGHWAY, 1);

        RegionState r2 = new RegionState("Coastal", 150, 16, EconomicZone.AGRARIAN, true, 90);
        r2.addBuilding(BuildingType.HOUSING, 1);
        r2.addBuilding(BuildingType.FARM, 2);
        r2.addBuilding(BuildingType.PORT, 1);

        regions.add(r0);
        regions.add(r1);
        regions.add(r2);

        ministries.clear();
        for (MinistryType m : MinistryType.values()) ministries.put(m, 0);
        ministries.put(MinistryType.FINANCE, 1);

        military.clear();
        for (MilitaryBranch b : MilitaryBranch.values()) military.put(b, 0);
        military.put(MilitaryBranch.ZSU, 25);
        military.put(MilitaryBranch.TRO, 25);

        vehicles.clear();
        for (VehicleType v : VehicleType.values()) vehicles.put(v, 0);
        vehicles.put(VehicleType.BUS, 2);
        vehicles.put(VehicleType.TRUCK, 1);
        vehicles.put(VehicleType.AMBULANCE, 1);

        fleet.clear();
        for (NavalUnit u : NavalUnit.values()) fleet.put(u, 0);
        fleet.put(NavalUnit.PATROL_BOAT, 1);

        serviceEquipment[0] = 4; serviceEquipment[1] = 4; serviceEquipment[2] = 3; serviceEquipment[3] = 2;
        serviceInTransit[0] = 0; serviceInTransit[1] = 0; serviceInTransit[2] = 0; serviceInTransit[3] = 0;

        pmc.nameIdx = 0; pmc.squadSize = 0; pmc.foreignContract = false; pmc.experience = 0;
        union.unionStage = 0; union.pactStage = 0; union.unionPresidency = false; union.unionCouncilVotesWon = 0;
        partnerLastClaimMonth[0] = 0; partnerLastClaimMonth[1] = 0;

        neighbors.clear();
        neighbors.add(new NeighborCountry("atlantia", "Atlantia", "Adm. Werner", true, 38, 180, 4200, 3, 240));
        neighbors.add(new NeighborCountry("nordland", "Nordland", "Gen. Lind", true, 24, 210, 3600, 3, 210));
        neighbors.add(new NeighborCountry("eastmark", "Eastern March", "Gen. Krass", false, -28, 165, 2900, 3, 190));
        neighbors.add(new NeighborCountry("solaris", "Solar Rep.", "Marshal Moreno", true, 46, 130, 3100, 2, 220));

        switch (this.scenario) {
            case WAR_ECONOMY:
                cityName = "Fortress City";
                treasury = 1900.0;
                debt = 1200.0;
                inflationPct = 7.8;
                fxRate = 43.5;
                fxReserves = 310.0;
                priceEnergy = 1.28;
                terrorThreat = 46.0;
                military.put(MilitaryBranch.ZSU, 75);
                military.put(MilitaryBranch.SSO, 15);
                military.put(MilitaryBranch.CSP, 10);
                military.put(MilitaryBranch.GUR, 10);
                ministries.put(MinistryType.DEFENSE, 2);
                vehicles.put(VehicleType.APC, 2);
                vehicles.put(VehicleType.DRONE_AA, 1);
                r2.addBuilding(BuildingType.NAVAL_BASE, 1);
                laws.add(LawType.MARTIAL_LAW);
                neighbors.get(2).relations = -75;
                neighbors.get(2).atWar = true;
                parliamentSeats.put(FactionType.SECUROCRATS, 36);
                parliamentSeats.put(FactionType.TECHNOCRATS, 24);
                parliamentSeats.put(FactionType.LIBERALS, 20);
                parliamentSeats.put(FactionType.SOCIALISTS, 20);
                break;
            case HYPERINFLATION:
                cityName = "Couponburg";
                treasury = 900.0;
                inflationPct = 22.5;
                moneyImpulse = 6.5;
                fxRate = 68.0;
                fxReserves = 95.0;
                policyRate = 14; laggedRate1 = 14; laggedRate2 = 14;
                corruption = 48.0;
                shadowEconomy = 38.0;
                terrorThreat = 26.0;
                trust = 54.0;
                break;
            case ASIAN_TIGER:
                cityName = "Port Nova";
                treasury = 3400.0;
                r0.taxRate = 12; r0.zone = EconomicZone.TECH_FINANCE; r0.addBuilding(BuildingType.TECH_PARK, 1);
                r1.taxRate = 14;
                r2.taxRate = 10; r2.zone = EconomicZone.FREE_PORT;
                laws.add(LawType.ANTI_CORRUPTION);
                laws.add(LawType.PRIVATIZATION);
                corruption = 14.0;
                shadowEconomy = 9.0;
                terrorThreat = 8.0;
                break;
            case DEBT_CRISIS:
                cityName = "Debtgrad";
                treasury = 650.0;
                debt = 2400.0;
                policyRate = 15; laggedRate1 = 15; laggedRate2 = 15;
                inflationPct = 9.2;
                fxRate = 49.0;
                fxReserves = 140.0;
                corruption = 38.0;
                terrorThreat = 22.0;
                trust = 58.0;
                break;
            case SANDBOX:
            default:
                break;
        }

        migrateNonCoastalPorts();
        history.clear();
        chronicle.clear();
        logs.clear();
        syncAggregateTax();
        recomputeFactions();
        recomputeDerived();
        recordSnapshot("Старт: " + this.scenario.title);
        log(LogEntry.Category.ACTION, Strings.f("Сценарій «%s». Вибори через 23 міс.", Strings.t(this.scenario.title)), false);
        currentEvent = CityEvent.pick(this, rng);
    }

    /** Ensures all port terminals & naval bases sit strictly in coastal regions. */
    public void migrateNonCoastalPorts() {
        RegionState coastalTarget = null;
        for (RegionState r : regions) if (r.coastal) { coastalTarget = r; break; }
        if (coastalTarget == null && !regions.isEmpty()) {
            coastalTarget = regions.get(regions.size() - 1);
            coastalTarget.coastal = true;
        }
        if (coastalTarget == null) return;
        for (RegionState r : regions) {
            if (r.coastal) continue;
            for (BuildingType b : new BuildingType[]{BuildingType.PORT, BuildingType.NAVAL_BASE}) {
                int moved = r.count(b);
                if (moved > 0) {
                    r.buildings.put(b, 0);
                    coastalTarget.addBuilding(b, moved);
                }
            }
        }
    }

    // -------------------------------------------------------- Counters & Stats

    public int count(BuildingType type) {
        int sum = 0;
        for (RegionState r : regions) sum += r.count(type);
        return sum;
    }

    public int ministryLevel(MinistryType type) { Integer v = ministries.get(type); return v == null ? 0 : v; }
    public int troops(MilitaryBranch branch) { Integer v = military.get(branch); return v == null ? 0 : v; }
    public int vehicleCount(VehicleType type) { Integer v = vehicles.get(type); return v == null ? 0 : v; }
    public int navalCount(NavalUnit unit) { Integer v = fleet.get(unit); return v == null ? 0 : v; }
    public boolean hasLaw(LawType law) { return laws.contains(law); }
    public int seats(FactionType f) { Integer v = parliamentSeats.get(f); return v == null ? 25 : v; }

    public LogEntry latestLog() {
        return logs.isEmpty() ? new LogEntry(month, LogEntry.Category.ACTION, Strings.t("Місто готове до управління."), false) : logs.peekFirst();
    }

    public int occupiedRegions() {
        int n = 0;
        for (RegionState r : regions) if (r.occupied) n++;
        return n;
    }

    public int totalTroops() {
        int sum = 0;
        for (MilitaryBranch b : MilitaryBranch.values()) sum += troops(b);
        return sum;
    }

    public int totalVehicles() {
        int sum = 0;
        for (VehicleType v : VehicleType.values()) sum += vehicleCount(v);
        return sum;
    }

    public int militaryVehicles() {
        int sum = 0;
        for (VehicleType v : VehicleType.values()) if (v.military) sum += vehicleCount(v);
        return sum;
    }

    public int totalNavalVessels() {
        int sum = 0;
        for (NavalUnit u : NavalUnit.values()) sum += navalCount(u);
        return sum;
    }

    public int requiredNavalCrew() {
        int sum = 0;
        for (NavalUnit u : NavalUnit.values()) sum += navalCount(u) * u.crewNeeded;
        return sum;
    }

    public double navalPower() {
        double raw = 0;
        for (NavalUnit u : NavalUnit.values()) raw += navalCount(u) * u.navalPower;
        int req = requiredNavalCrew();
        double crewFactor = req <= 0 ? 1.0 : clamp(troops(MilitaryBranch.NAVY) / (double) req, 0.35, 1.15);
        double baseBoost = 1.0 + count(BuildingType.NAVAL_BASE) * 0.12;
        return raw * crewFactor * baseBoost;
    }

    public double defensePower() {
        double p = 0;
        for (MilitaryBranch b : MilitaryBranch.values()) p += troops(b) * b.powerPerPerson;
        for (VehicleType v : VehicleType.values()) p += vehicleCount(v) * v.combatPower;
        p += navalPower() * 0.75;
        if (!pmc.foreignContract) p += pmc.combatPower();
        double mult = 1.0 + ministryLevel(MinistryType.DEFENSE) * 0.12;
        if (hasLaw(LawType.CONTRACT_ARMY)) mult *= 1.22;
        if (hasLaw(LawType.MARTIAL_LAW)) mult *= 1.30;
        if (union.isPactMember()) mult *= 1.35;
        return p * mult;
    }

    /** Uncapped State Power rating combining all security branches, army, navy, GDP & reserves. */
    public double statePowerRating() {
        return defensePower() * 1.4 + navalPower() * 0.6 + (gdp / 18.0) + (fxReserves * 0.35) + (sovereignFund * 0.08);
    }

    public String gdpAndPowerExplanation() {
        return Strings.f("ВВП (%.0f ₴) = зайняті × продуктивність (%.2f×) × регіони + експорт нафти/порту. " +
                "Рейтинговий ВВП (річний): %.0f ₴. Сила держави (%.0f) = Армія/ЦСП/ГУР/КОРД (%.0f) + Флот (%.0f) + Економіка і ЗВР.",
                gdp, tfp, gdp * 12.0, statePowerRating(), defensePower(), navalPower());
    }

    public double securityScore() {
        double s = 12.0;
        for (MilitaryBranch b : MilitaryBranch.values()) s += troops(b) * b.securityPerPerson;
        for (VehicleType v : VehicleType.values()) s += vehicleCount(v) * v.securityBoost * 0.35;
        s += ministryLevel(MinistryType.DEFENSE) * 4.0 + count(BuildingType.COURT) * 3.0 + count(BuildingType.FIRE_STATION) * 2.5;
        if (hasLaw(LawType.MARTIAL_LAW)) s += 16.0;
        return clamp(s, 0.0, 100.0);
    }

    public int serviceNorm(int svcIdx) {
        double scale = Math.max(1.0, population / 450.0) + Math.max(0, regions.size() - 3) * 0.4;
        int[] base = {4, 4, 3, 2};
        return Math.max(2, (int) Math.round(base[svcIdx % 4] * scale));
    }

    public double factionSupport(FactionType f) { Double v = factions.get(f); return v == null ? 50.0 : v; }

    public double parliamentSupport() {
        double weighted = 0;
        int totalSeats = 0;
        for (FactionType f : FactionType.values()) {
            int s = seats(f);
            weighted += factionSupport(f) * s;
            totalSeats += s;
        }
        return totalSeats <= 0 ? 50.0 : weighted / totalSeats;
    }

    public int monthsToElection() { int mod = month % 24; return mod == 0 ? 0 : (24 - mod); }

    public int strongestMinistryLevel() {
        int max = 0;
        for (MinistryType m : MinistryType.values()) max = Math.max(max, ministryLevel(m));
        return max;
    }

    public boolean hasThreateningNeighbor() {
        for (NeighborCountry n : neighbors) if (!n.capitulated && (n.relations < -15 || n.atWar)) return true;
        return false;
    }

    public int activeWarsCount() {
        int w = 0;
        for (NeighborCountry n : neighbors) if (n.atWar && !n.capitulated) w++;
        return w;
    }

    /** Fair crisis reaction cost scaled to treasury + Sovereign Fund / Insurance / Union Solidarity discounts. */
    public int fairCrisisCost(int base) {
        double scale = clamp(0.65 + treasury / 6000.0, 0.60, 2.2);
        double discount = 1.0;
        if (hasLaw(LawType.STATE_INSURANCE)) discount *= 0.75;
        if (sovereignFund >= 500) discount *= 0.85;
        if (union.isUnionMember()) discount *= 0.85; // 15% Solidarity Fund rebate!
        return Math.max(120, (int) (Math.round(base * scale * discount / 10.0) * 10));
    }

    public int cspOperationCost() {
        return (int) (Math.round(320.0 * (1.0 + gdp / 3200.0) / 10.0) * 10);
    }

    public int housingCapacity() {
        int cap = 120;
        for (BuildingType b : BuildingType.values()) cap += count(b) * b.housing;
        return cap;
    }

    public int totalJobs() {
        int jobs = 90;
        for (BuildingType b : BuildingType.values()) jobs += count(b) * b.jobs;
        return jobs;
    }

    public int energyBalance() {
        int net = 25;
        for (BuildingType b : BuildingType.values()) net += count(b) * b.energyDelta;
        return net;
    }

    public int foodCapacity() {
        int base = 180;
        for (BuildingType b : BuildingType.values()) base += count(b) * b.foodSupply;
        double zoneFood = 0;
        int popSum = Math.max(1, totalRegionPop());
        for (RegionState r : regions) {
            double mult = r.occupied ? 0.25 : r.zone.foodMult;
            zoneFood += mult * (r.population / (double) popSum);
        }
        double landMult = hasLaw(LawType.LAND_MARKET) ? 1.25 : 1.0;
        return (int) Math.round(base * (zoneFood <= 0 ? 1.0 : zoneFood) * landMult);
    }

    public double serviceCoverage() {
        double raw = 18.0;
        for (BuildingType b : BuildingType.values()) raw += count(b) * b.serviceScore;
        for (VehicleType v : VehicleType.values()) raw += vehicleCount(v) * v.serviceBoost;
        raw += ministryLevel(MinistryType.HEALTH) * 9.0;
        double need = Math.max(1.0, population / 15.0);
        return clamp(raw / need * 100.0, 5.0, 100.0);
    }

    public double debtServiceRatioPct() {
        double debtDiscount = Math.max(0.60, 1.0 - ministryLevel(MinistryType.FINANCE) * 0.08);
        double monthlyInt = debt * ((0.04 + laggedRate2 * 0.008) / 12.0) * debtDiscount;
        return monthlyInt * 100.0 / Math.max(100.0, lastTaxRevenue);
    }

    public double estimateInterestSavedMonthly(double repayAmount) {
        double pay = Math.min(repayAmount, debt);
        double debtDiscount = Math.max(0.60, 1.0 - ministryLevel(MinistryType.FINANCE) * 0.08);
        return pay * ((0.04 + laggedRate2 * 0.008) / 12.0) * debtDiscount;
    }

    public double sovereignEnduranceMonths(double extraAmount) {
        double burn = Math.max(120.0, lastTotalExpense * 0.45);
        return (sovereignFund + extraAmount) / burn;
    }

    public double creditLimit() {
        return 2800.0 + count(BuildingType.BANK) * 2400.0 + ministryLevel(MinistryType.FINANCE) * 700.0 + gdp * 0.85;
    }

    public double maxRegionWear() {
        double w = 0;
        for (RegionState r : regions) w = Math.max(w, r.wear);
        return w;
    }

    public int totalRepairCost() {
        double sum = 0;
        for (RegionState r : regions) {
            if (r.wear < 5.0) continue;
            int wearCount = 0;
            for (BuildingType b : BuildingType.values()) if (b.wearsOut) wearCount += r.count(b);
            sum += r.wear * (3.5 + wearCount * 2.2);
        }
        double disc = union.isUnionMember() ? 0.85 : 1.0;
        return (int) (Math.round(sum * disc / 10.0) * 10);
    }

    public String stagnantTreasuryHint() {
        if (lastNetIncome < 25 && lastNetIncome >= -10) return Strings.f("Скарбниця майже не зростає · Головна витрата: %s", Strings.t(lastMainExpense));
        if (lastNetIncome < -10) return Strings.f("Дефіцит бюджету · Головна витрата: %s", Strings.t(lastMainExpense));
        return Strings.f("Головна стаття витрат: %s", Strings.t(lastMainExpense));
    }

    public String treasuryChangeExplanation() {
        return Strings.f("Баланс місяця %+.0f ₴ (Доходи %.0f − Витрати %.0f). Скарбниця також змінюється разовими діями: будівництвом, технікою, інтервенціями ЗВР, внесками у фонд і подарунками партнерів.",
                lastNetIncome, lastTaxRevenue, lastTotalExpense);
    }

    public List<OffBudgetLine> offBudgetLines() {
        List<OffBudgetLine> list = new ArrayList<>();
        if (maxRegionWear() >= 35.0) {
            list.add(new OffBudgetLine(Strings.f("Знос енергетики/промисловості %s%%", Math.round(maxRegionWear())), Strings.t("Ремонт →"), 1, 0));
        }
        if (requiredNavalCrew() > troops(MilitaryBranch.NAVY)) {
            list.add(new OffBudgetLine(Strings.f("Брак екіпажів ВМС (%s/%s)", troops(MilitaryBranch.NAVY), requiredNavalCrew()), Strings.t("Набрати →"), 2, 2));
        }
        if (energyBalance() < 0) {
            list.add(new OffBudgetLine(Strings.f("Дефіцит енергії %s МВт", energyBalance()), Strings.t("АЕС/ГЕС →"), 1, 0));
        }
        if (acbAttention >= 55.0) {
            list.add(new OffBudgetLine(Strings.f("Висока увага АКБ (%s%%)", Math.round(acbAttention)), Strings.t("Парламент →"), 2, 0));
        }
        if (activeWarsCount() > 0) {
            list.add(new OffBudgetLine(Strings.f("Активна війна (%s)", activeWarsCount()), Strings.t("Фронт →"), 3, 0));
        }
        return list;
    }

    public String cityRank() {
        if (activeWarsCount() > 0) return Strings.f("Війна (%s)", activeWarsCount());
        if (occupiedRegions() > 0) return Strings.f("Оборона (%s окупов.)", occupiedRegions());
        if (gdp >= 1_000_000) return Strings.t("Наддержава");
        if (population >= 3000 && gdpPerCapita >= 5.0) return Strings.t("Економічне диво");
        if (population >= 2000) return Strings.t("Мегаполіс");
        if (population >= 1200) return Strings.t("Обласний центр");
        if (population >= 800) return Strings.t("Місто");
        return Strings.t("Містечко");
    }

    public String currentDossierName() {
        return Strings.t(DOSSIER_NAMES[dossierIndex % DOSSIER_NAMES.length]);
    }

    private int totalRegionPop() {
        int s = 0;
        for (RegionState r : regions) s += r.population;
        return s;
    }

    private void syncAggregateTax() {
        int popSum = totalRegionPop();
        if (popSum <= 0) return;
        double weighted = 0;
        for (RegionState r : regions) weighted += r.taxRate * (r.population / (double) popSum);
        taxRate = (int) Math.round(weighted);
    }

    // --------------------------------------------------- Regional Construction & ROI

    public int buildingCostInRegion(int regionIdx, BuildingType b) {
        if (modFreeBuild) return 0;
        int base = b.costFor(count(b));
        if (regionIdx < 0 || regionIdx >= regions.size()) return base;
        double mult = regions.get(regionIdx).densityCostMultiplier();
        return (int) (Math.round(base * mult / 10.0) * 10);
    }

    /** Estimates payback period in months for constructing b inside regionIdx. */
    public int estimateRoiMonths(int regionIdx, BuildingType b) {
        int cost = buildingCostInRegion(regionIdx, b);
        RegionState r = (regionIdx >= 0 && regionIdx < regions.size()) ? regions.get(regionIdx) : null;
        double zoneGdp = r != null ? r.zone.gdpMult * r.sizeDevelopmentFactor() : 1.0;
        double roadMult = (b == BuildingType.HIGHWAY && hasLaw(LawType.ROAD_VIGNETTE)) ? 1.25 : 1.0;
        double monthlyReturn = b.exportIncome * roadMult + b.jobs * 0.72 * zoneGdp + b.productivityWeight * 32.0 * zoneGdp
                + (b.energyDelta > 0 ? b.energyDelta * 0.55 : 0) - b.upkeep;
        if (monthlyReturn <= 5.0) return 0; // Social/service object (non-commercial)
        return Math.max(2, (int) Math.round(cost / monthlyReturn));
    }

    public String build(BuildingType type) {
        // Smart auto-selection: coastal for coastal-only, least dense unoccupied otherwise
        int best = -1;
        double bestScore = Double.MAX_VALUE;
        for (int i = 0; i < regions.size(); i++) {
            RegionState r = regions.get(i);
            if (r.occupied) continue;
            if (type.coastalOnly && !r.coastal) continue;
            double score = r.densityCostMultiplier() + (r.gdp / 5000.0);
            if (score < bestScore) { bestScore = score; best = i; }
        }
        if (best < 0) {
            String msg = type.coastalOnly ? Strings.t("Потрібна вільна приморська область!") : Strings.t("Немає доступної області.");
            log(LogEntry.Category.ACTION, msg, true);
            return msg;
        }
        return buildInRegion(best, type);
    }

    public String buildInRegion(int regionIdx, BuildingType type) {
        if (status != GameStatus.PLAYING) return Strings.t("Каденція завершена.");
        if (regionIdx < 0 || regionIdx >= regions.size()) return Strings.t("Оберіть область.");
        RegionState r = regions.get(regionIdx);
        if (r.occupied) return Strings.t("Не можна будувати в окупованому регіоні.");
        if (type.coastalOnly && !r.coastal) {
            String msg = Strings.f("%s будується лише у приморській області (наприклад, Coastal).", Strings.t(type.title));
            log(LogEntry.Category.ACTION, msg, true);
            return msg;
        }
        int cost = buildingCostInRegion(regionIdx, type);
        if (treasury < cost) {
            String msg = Strings.f("Недостатньо коштів: потрібно %s ₴.", cost);
            log(LogEntry.Category.ACTION, msg, false);
            return msg;
        }
        treasury -= cost;
        r.addBuilding(type, 1);
        ecology = clamp(ecology + type.ecologyDelta * 0.5, 5, 100);
        recomputeDerived();
        int roi = estimateRoiMonths(regionIdx, type);
        String roiTxt = roi > 0 ? Strings.f(" · окупн. ~%s міс.", roi) : "";
        String msg = Strings.f("Побудовано %s у «%s» (-%s ₴%s)", Strings.t(type.title), Strings.t(r.name), cost, roiTxt);
        log(LogEntry.Category.ACTION, msg, false);
        appendNote("Буд:" + type.title + "@" + r.name);
        return msg;
    }

    public String expandRegionLand(int regionIdx) {
        if (regionIdx < 0 || regionIdx >= regions.size()) return "";
        RegionState r = regions.get(regionIdx);
        int cost = 520;
        if (treasury < cost) {
            String msg = Strings.f("Потрібно %s ₴ для освоєння землі.", cost);
            log(LogEntry.Category.ACTION, msg, false);
            return msg;
        }
        treasury -= cost;
        r.landArea += 20;
        String msg = Strings.f("%s: освоєно +20 од. землі (всього %s).", Strings.t(r.name), r.totalLand());
        log(LogEntry.Category.ACTION, msg, false);
        return msg;
    }

    public String leaseIndustrialZone(int regionIdx) {
        if (regionIdx < 0 || regionIdx >= regions.size()) return "";
        NeighborCountry partner = null;
        for (NeighborCountry n : neighbors) if (!n.capitulated && !n.atWar && n.relations >= 0) { partner = n; break; }
        if (partner == null) {
            String msg = Strings.t("Немає дружнього сусіда для оренди промзони.");
            log(LogEntry.Category.ACTION, msg, false);
            return msg;
        }
        int cost = 420;
        if (treasury < cost) {
            String msg = Strings.f("Потрібно %s ₴ для оренди промзони.", cost);
            log(LogEntry.Category.ACTION, msg, false);
            return msg;
        }
        treasury -= cost;
        RegionState r = regions.get(regionIdx);
        r.leasedZones++;
        partner.leasedIndustrialZone = true;
        String msg = Strings.f("%s: орендовано промзону в %s (+14 землі).", Strings.t(r.name), Strings.t(partner.name));
        log(LogEntry.Category.ACTION, msg, false);
        return msg;
    }

    public String repairAllInfrastructure() {
        int cost = totalRepairCost();
        if (cost <= 0) {
            String msg = Strings.t("Усі енергетичні та промислові об'єкти справні.");
            log(LogEntry.Category.ACTION, msg, false);
            return msg;
        }
        if (treasury < cost) {
            String msg = Strings.f("Потрібно %s ₴ для ремонту всіх об'єктів.", cost);
            log(LogEntry.Category.ACTION, msg, true);
            return msg;
        }
        treasury -= cost;
        for (RegionState r : regions) r.wear = 0.0;
        String msg = Strings.f("Полагоджено всю енергетику й промисловість (-%s ₴%s).", cost,
                union.isUnionMember() ? Strings.t(", -15% Фонд солідарності Унії") : "");
        log(LogEntry.Category.ACTION, msg, false);
        return msg;
    }

    // --------------------------------------------------- Workforce / Specialists

    public String hireForeignSpecialists() {
        int cost = 480;
        if (treasury < cost) {
            String msg = Strings.f("Потрібно %s ₴ на контракти іноземних спеціалістів.", cost);
            log(LogEntry.Category.ECONOMY, msg, false);
            return msg;
        }
        treasury -= cost;
        foreignSpecialists += 25;
        specialists += 25;
        tfp += 0.025;
        recomputeDerived();
        String msg = Strings.t("Найнято +25 іноземних спеціалістів за контрактом (+працівники, +TFP).");
        log(LogEntry.Category.ECONOMY, msg, false);
        return msg;
    }

    public String repatriateCitizens() {
        int cost = 540;
        if (treasury < cost) {
            String msg = Strings.f("Потрібно %s ₴ на програму повернення громадян.", cost);
            log(LogEntry.Category.ECONOMY, msg, false);
            return msg;
        }
        treasury -= cost;
        if (!regions.isEmpty()) regions.get(0).population += 60;
        population += 60;
        specialists += 14;
        trust = clamp(trust + 3.0, 0, 100);
        recomputeDerived();
        String msg = Strings.t("Повернуто +60 громадян (+14 спеціалістів) із-за кордону!");
        log(LogEntry.Category.ECONOMY, msg, false);
        return msg;
    }

    public String sendSpecialistsAbroad() {
        if (specialists < 20) {
            String msg = Strings.t("Потрібно щонайменше 20 вільних спеціалістів (будуйте коледжі/університети).");
            log(LogEntry.Category.ECONOMY, msg, false);
            return msg;
        }
        specialists -= 15;
        specialistsAbroad += 15;
        for (NeighborCountry n : neighbors) if (!n.atWar && !n.capitulated) n.relations = Math.min(100, n.relations + 4);
        String msg = Strings.t("15 спеціалістів відправлено на закордонні контракти (+дохід і відносини).");
        log(LogEntry.Category.ECONOMY, msg, false);
        return msg;
    }

    // --------------------------------------------------- Navy, CSP, PMC & Transfer

    public String buyNavalUnit(NavalUnit unit) {
        if (status != GameStatus.PLAYING) return Strings.t("Каденція завершена.");
        if ((unit == NavalUnit.FRIGATE || unit == NavalUnit.NUCLEAR_SUB || unit == NavalUnit.AIRCRAFT_CARRIER)
                && count(BuildingType.NAVAL_BASE) <= 0) {
            String msg = Strings.f("Для %s спочатку побудуйте Військово-морську базу на узбережжі.", Strings.t(unit.title));
            log(LogEntry.Category.SECURITY, msg, true);
            return msg;
        }
        int cost = modFreeBuild ? 0 : unit.cost;
        if (treasury < cost) {
            String msg = Strings.f("Потрібно %s ₴ для побудови %s.", cost, Strings.t(unit.title));
            log(LogEntry.Category.SECURITY, msg, false);
            return msg;
        }
        treasury -= cost;
        fleet.put(unit, navalCount(unit) + 1);
        String msg = Strings.f("ВМС поповнено: %s (-%s ₴ · екіпаж %s).", Strings.t(unit.title), cost, unit.crewNeeded);
        log(LogEntry.Category.SECURITY, msg, false);
        return msg;
    }

    public String friendlyNavalVisit(int neighborIdx) {
        if (neighborIdx < 0 || neighborIdx >= neighbors.size()) return "";
        NeighborCountry n = neighbors.get(neighborIdx);
        if (n.atWar || n.capitulated || !n.coastal) return Strings.t("Дружній візит можливий лише до мирної приморської держави.");
        if (totalNavalVessels() <= 0) return Strings.t("Немає кораблів ВМС для візиту.");
        int cost = 180;
        if (treasury < cost) return Strings.t("Потрібно 180 ₴ на похід флоту.");
        treasury -= cost;
        n.relations = Math.min(100, n.relations + 12);
        String msg = Strings.f("Дружній візит флоту до %s: відносини +12 (%s).", Strings.t(n.name), n.relations);
        log(LogEntry.Category.SECURITY, msg, false);
        return msg;
    }

    public String runCspOperation(int targetNeighborIdx) {
        if (troops(MilitaryBranch.CSP) <= 0) {
            String msg = Strings.t("Спочатку наберіть підрозділ ЦСП «Альфа» (СБУ) у вкладці Армія.");
            log(LogEntry.Category.SECURITY, msg, true);
            return msg;
        }
        int cost = cspOperationCost();
        if (treasury < cost) {
            String msg = Strings.f("Потрібно %s ₴ на спецоперацію ЦСП (ціна зростає з ВВП).", cost);
            log(LogEntry.Category.SECURITY, msg, false);
            return msg;
        }
        treasury -= cost;
        if (targetNeighborIdx >= 0 && targetNeighborIdx < neighbors.size() && neighbors.get(targetNeighborIdx).atWar) {
            NeighborCountry enemy = neighbors.get(targetNeighborIdx);
            double dmg = 26.0 + troops(MilitaryBranch.CSP) * 1.1;
            enemy.militaryPower = Math.max(20, enemy.militaryPower - dmg);
            enemy.warProgress = Math.min(100, enemy.warProgress + 16);
            String msg = Strings.f("Рейд ЦСП «Альфа» в тил %s: армія ворога -%s, прогрес %s%%.", Strings.t(enemy.name), Math.round(dmg), enemy.warProgress);
            log(LogEntry.Category.SECURITY, msg, false);
            return msg;
        } else {
            double drop = 24.0 + troops(MilitaryBranch.CSP) * 0.6;
            terrorThreat = clamp(terrorThreat - drop, 0, 100);
            for (RegionState r : regions) if (r.occupied) r.terrorStrength = Math.max(20, r.terrorStrength * 0.78);
            String msg = Strings.f("Спецоперація ЦСП «Альфа» (-%d ₴): загроза терору %.0f%%, позиції бойовиків ослаблено.", cost, terrorThreat);
            log(LogEntry.Category.SECURITY, msg, false);
            return msg;
        }
    }

    public String cyclePmcName() {
        pmc.nameIdx = (pmc.nameIdx + 1) % PmcState.NAMES.length;
        String msg = Strings.f("ПВК перейменовано: %s", Strings.t(pmc.displayName()));
        log(LogEntry.Category.SECURITY, msg, false);
        return msg;
    }

    public String hirePmcSquad() {
        int cost = 440;
        if (treasury < cost) {
            String msg = Strings.f("Потрібно %s ₴ на загін ПВК.", cost);
            log(LogEntry.Category.SECURITY, msg, false);
            return msg;
        }
        treasury -= cost;
        pmc.squadSize += 20;
        String msg = Strings.f("%s: +20 бійців (всього %s).", Strings.t(pmc.displayName()), pmc.squadSize);
        log(LogEntry.Category.SECURITY, msg, false);
        return msg;
    }

    public String togglePmcContract() {
        if (pmc.squadSize <= 0) return "Спочатку найміть загін ПВК.";
        pmc.foreignContract = !pmc.foreignContract;
        String msg = pmc.foreignContract
                ? Strings.f("%s відправлено на закордонний контракт (+%s ₴/міс).", Strings.t(pmc.displayName()), Math.round(pmc.monthlyContractIncome()))
                : Strings.f("%s повернуто додому для оборони.", Strings.t(pmc.displayName()));
        log(LogEntry.Category.SECURITY, msg, false);
        return msg;
    }

    public String transferEquipment(int fromSvc, int toSvc, int qty) {
        if (fromSvc == toSvc || fromSvc < 0 || fromSvc >= 4 || toSvc < 0 || toSvc >= 4) return "";
        int actual = Math.min(qty, serviceEquipment[fromSvc]);
        if (actual <= 0) return "Немає техніки для передачі.";
        serviceEquipment[fromSvc] -= actual;
        serviceInTransit[toSvc] += actual;
        String msg = Strings.f("Передача %s од. техніки: %s → %s (у дорозі).", actual, Strings.t(SERVICE_NAMES[fromSvc]), Strings.t(SERVICE_NAMES[toSvc]));
        log(LogEntry.Category.ACTION, msg, false);
        return msg;
    }

    // --------------------------------------------------- Diplomacy 2.0, Trade 2.0, War & Partners

    public String signTradeDeal(int idx) {
        if (idx < 0 || idx >= neighbors.size()) return "";
        NeighborCountry n = neighbors.get(idx);
        if (n.atWar || n.capitulated) return Strings.t("Неможливо під час війни.");
        n.tradeDealDaysLeft = 180;
        n.tariffPct = Math.max(0, n.tariffPct - 6);
        n.relations = Math.min(100, n.relations + 8);
        String msg = Strings.f("Торговий договір із %s на 180 дн. (4320 год) · мито %s%%.", Strings.t(n.name), n.tariffPct);
        log(LogEntry.Category.ECONOMY, msg, false);
        return msg;
    }

    public String toggleCustomsUnion(int idx) {
        if (idx < 0 || idx >= neighbors.size()) return "";
        NeighborCountry n = neighbors.get(idx);
        if (!n.customsUnion && n.relations < 35) {
            String msg = Strings.f("Для митного союзу з %s потрібні відносини ≥ 35 (зараз %s).", Strings.t(n.name), n.relations);
            log(LogEntry.Category.ECONOMY, msg, false);
            return msg;
        }
        n.customsUnion = !n.customsUnion;
        if (n.customsUnion) n.tariffPct = 0;
        String msg = n.customsUnion ? Strings.f("Укладено митний союз із %s (мито 0%%).", Strings.t(n.name)) : Strings.f("Вихід із митного союзу з %s.", Strings.t(n.name));
        log(LogEntry.Category.ECONOMY, msg, false);
        return msg;
    }

    public String openEmbassy(int idx) {
        if (idx < 0 || idx >= neighbors.size()) return "";
        NeighborCountry n = neighbors.get(idx);
        if (n.embassy) return Strings.t("Посольство вже відкрито.");
        int cost = 460;
        if (treasury < cost) return Strings.t("Потрібно 460 ₴ для відкриття посольства.");
        treasury -= cost;
        n.embassy = true;
        n.relations = Math.min(100, n.relations + 14);
        String msg = Strings.f("Відкрито посольство в %s (+14 відносин).", Strings.t(n.name));
        log(LogEntry.Category.ACTION, msg, false);
        return msg;
    }

    public String openCulturalCenter(int idx) {
        if (idx < 0 || idx >= neighbors.size()) return "";
        NeighborCountry n = neighbors.get(idx);
        if (n.culturalCenter) return Strings.t("Культурний центр вже працює.");
        int cost = 360;
        if (treasury < cost) return Strings.t("Потрібно 360 ₴ для культурного центру.");
        treasury -= cost;
        n.culturalCenter = true;
        n.relations = Math.min(100, n.relations + 10);
        String msg = Strings.f("Відкрито культурний центр у %s (+10 відносин, +експорт).", Strings.t(n.name));
        log(LogEntry.Category.ACTION, msg, false);
        return msg;
    }

    public String adjustNeighborAid(int idx, int delta) {
        if (idx < 0 || idx >= neighbors.size()) return "";
        NeighborCountry n = neighbors.get(idx);
        n.aidSharePct = Math.max(0, Math.min(100, n.aidSharePct + delta));
        String msg = Strings.f("Частка допомоги для %s: %s%%.", Strings.t(n.name), n.aidSharePct);
        log(LogEntry.Category.ECONOMY, msg, false);
        return msg;
    }

    public String fileTradeCourtCase(int idx) {
        if (idx < 0 || idx >= neighbors.size()) return "";
        NeighborCountry n = neighbors.get(idx);
        boolean won = (ministryLevel(MinistryType.FOREIGN) * 15 + (union.isUnionMember() ? 25 : 0) + rng.nextInt(70)) >= 55;
        if (won) {
            int comp = 380;
            treasury += comp;
            n.tariffPct = Math.max(0, n.tariffPct - 5);
            String msg = Strings.f("Вердикт торгового суду (миттєво): ВИГРАШ проти %s (+%s ₴, мито -5%%).", Strings.t(n.name), comp);
            log(LogEntry.Category.ECONOMY, msg, false);
            return msg;
        } else {
            String msg = Strings.f("Вердикт торгового суду (миттєво): позов проти %s відхилено.", Strings.t(n.name));
            log(LogEntry.Category.ECONOMY, msg, false);
            return msg;
        }
    }

    public String declareWar(int idx) {
        if (idx < 0 || idx >= neighbors.size()) return "";
        NeighborCountry n = neighbors.get(idx);
        if (n.capitulated || n.atWar) return Strings.t("Вже у стані війни.");
        n.atWar = true;
        n.customsUnion = false;
        n.tradeDealDaysLeft = 0;
        n.relations = -100;
        n.warProgress = 10;
        String msg = Strings.f("ВІЙНА! Оголошено війну %s (командувач ворога: %s).", Strings.t(n.name), Strings.t(n.commander));
        log(LogEntry.Category.CRISIS, msg, true);
        return msg;
    }

    public String borderOffensive(int idx) {
        if (idx < 0 || idx >= neighbors.size()) return "";
        NeighborCountry n = neighbors.get(idx);
        if (!n.atWar || n.capitulated) return Strings.f("Немає активної війни з %s.", Strings.t(n.name));
        int cost = 450;
        if (treasury < cost) return Strings.t("Потрібно 450 ₴ на прикордонний наступ.");
        treasury -= cost;
        double ratio = defensePower() / Math.max(40.0, n.militaryPower);
        if (ratio + rng.nextDouble() * 0.45 >= 0.95) {
            n.militaryPower = Math.max(20, n.militaryPower * 0.72);
            n.warProgress = Math.min(100, n.warProgress + 34);
            if (n.warProgress >= 100 || n.regionsCount <= 1) {
                return capitulateNeighbor(n);
            } else {
                n.regionsCount--;
                annexOneRegionFrom(n);
                String msg = Strings.f("Наступ успішний! Приєднано регіон %s (прогрес війни %s%%).", Strings.t(n.name), n.warProgress);
                log(LogEntry.Category.SECURITY, msg, false);
                return msg;
            }
        } else {
            n.militaryPower = Math.max(25, n.militaryPower * 0.88);
            trust = clamp(trust - 3.0, 0, 100);
            String msg = Strings.f("Наступ на %s зупинено командувачем %s. Посильте армію/ППО!", Strings.t(n.name), Strings.t(n.commander));
            log(LogEntry.Category.CRISIS, msg, true);
            return msg;
        }
    }

    public String navalBlockadeAndLanding(int idx) {
        if (idx < 0 || idx >= neighbors.size()) return "";
        NeighborCountry n = neighbors.get(idx);
        if (!n.atWar || !n.coastal || n.capitulated) return Strings.t("Морський десант можливий лише проти приморського ворога.");
        if (navalPower() < 80) return Strings.t("Потрібна морська міць ≥ 80 (побудуйте фрегати/човни/авіаносець).");
        int cost = 520;
        if (treasury < cost) return Strings.t("Потрібно 520 ₴ на морський десант і блокаду.");
        treasury -= cost;
        n.seaBlockaded = true;
        n.militaryPower = Math.max(20, n.militaryPower * 0.75);
        n.warProgress = Math.min(100, n.warProgress + 38);
        if (n.warProgress >= 100) return capitulateNeighbor(n);
        String msg = Strings.f("Морська блокада й десант на узбережжя %s! Прогрес війни: %s%%.", Strings.t(n.name), n.warProgress);
        log(LogEntry.Category.SECURITY, msg, false);
        return msg;
    }

    private String capitulateNeighbor(NeighborCountry n) {
        n.capitulated = true;
        n.atWar = false;
        int annexed = Math.max(1, n.regionsCount);
        n.regionsCount = 0;
        int addedPop = annexed * n.populationPerRegion;
        for (int i = 0; i < annexed && regions.size() < 9; i++) {
            annexOneRegionFrom(n);
        }
        treasury += 1200;
        trust = clamp(trust + 10.0, 0, 100);
        String msg = Strings.f("ПОВНА КАПІТУЛЯЦІЯ %s! Приєднано %s рег. (+%s жит., +1200 ₴). Норми служб зросли.", Strings.t(n.name), annexed, addedPop);
        log(LogEntry.Category.SECURITY, msg, false);
        return msg;
    }

    private void annexOneRegionFrom(NeighborCountry n) {
        String rName = n.name + "-" + (regions.size() + 1);
        RegionState nr = new RegionState(rName, n.populationPerRegion, taxRate, EconomicZone.STANDARD, n.coastal, 80);
        nr.addBuilding(BuildingType.HOUSING, 1);
        nr.addBuilding(BuildingType.FACTORY, 1);
        regions.add(nr);
        population += n.populationPerRegion;
        recomputeDerived();
    }

    public String proposePeace(int idx) {
        if (idx < 0 || idx >= neighbors.size()) return "";
        NeighborCountry n = neighbors.get(idx);
        if (!n.atWar) return Strings.t("Війна не триває.");
        n.atWar = false;
        n.seaBlockaded = false;
        n.relations = -15;
        String msg = Strings.f("Підписано мирний договір із %s.", Strings.t(n.name));
        log(LogEntry.Category.ACTION, msg, false);
        return msg;
    }

    public boolean canClaimPartnerGift(int partnerIdx) {
        return partnerIdx >= 0 && partnerIdx < 2 && (month - partnerLastClaimMonth[partnerIdx]) >= 1;
    }

    public String claimPartnerGift(int partnerIdx) {
        if (!canClaimPartnerGift(partnerIdx)) return Strings.t("Подарунок партнера вже отримано цього місяця.");
        partnerLastClaimMonth[partnerIdx] = month;
        int gift = partnerIdx == 0 ? 360 : 420;
        treasury += gift;
        String name = partnerIdx == 0 ? Strings.t("Development Fund") : Strings.t("Northern Alliance");
        String msg = Strings.f("Партнер «%s» перерахував грант +%s ₴ до скарбниці!", name, gift);
        log(LogEntry.Category.ECONOMY, msg, false);
        return msg;
    }

    // --------------------------------------------------- Union & Pact

    public String unionCriteriaStatus() {
        double debtRatio = debt * 100.0 / Math.max(1.0, gdp);
        StringBuilder sb = new StringBuilder();
        if (corruption > 18.0) sb.append(Strings.f("Корупція %s%% (>18%%) · ", Math.round(corruption)));
        if (inflationPct > 12.0) sb.append(Strings.f("Інфляція %.1f%% (>12%%) · ", inflationPct));
        if (debtRatio > 90.0) sb.append(Strings.f("Борг %s%% (>90%% ВВП) · ", Math.round(debtRatio)));
        if (acbMoleActive) sb.append(Strings.t("Є «кришування» в АКБ · "));
        return sb.length() == 0 ? Strings.t("Усі критерії Унії виконано!") : sb.toString();
    }

    public boolean meetsUnionCriteria() {
        return corruption <= 18.0 && inflationPct <= 12.0
                && (debt * 100.0 / Math.max(1.0, gdp)) <= 90.0 && !acbMoleActive;
    }

    public String advanceUnionStage() {
        if (union.isUnionMember()) return Strings.t("Країна вже є членом Унії!");
        int cost = union.nextUnionCost();
        if (treasury < cost) {
            int need = (int) Math.ceil(cost - treasury);
            String msg = Strings.f("Для етапу «%s» ще потрібно накопичити %s ₴.", Strings.t(UnionState.UNION_STAGES[union.unionStage]), need);
            log(LogEntry.Category.ACTION, msg, false);
            return msg;
        }
        if (union.unionStage >= 2 && !meetsUnionCriteria()) {
            String msg = Strings.f("Не виконано критерії Унії: %s", unionCriteriaStatus());
            log(LogEntry.Category.PARLIAMENT, msg, true);
            return msg;
        }
        treasury -= cost;
        String doneName = UnionState.UNION_STAGES[union.unionStage];
        union.unionStage++;
        if (union.isUnionMember()) {
            treasury += 3500;
            trust = clamp(trust + 12, 0, 100);
            String msg = Strings.t("ВСТУП ДО УНІЇ! Отримано разовий пакет приєднання +3500 ₴ та 15% Фонд солідарності!");
            log(LogEntry.Category.PARLIAMENT, msg, false);
            return msg;
        }
        String msg = Strings.f("Унія: завершено етап «%s» (-%s ₴). Прогрес зафіксовано.", Strings.t(doneName), cost);
        log(LogEntry.Category.PARLIAMENT, msg, false);
        return msg;
    }

    public String advancePactStage() {
        if (union.isPactMember()) return Strings.t("Країна вже в Оборонному Пакті!");
        int cost = union.nextPactCost();
        if (treasury < cost) {
            int need = (int) Math.ceil(cost - treasury);
            String msg = Strings.f("Для етапу Пакту ще потрібно накопичити %s ₴.", need);
            log(LogEntry.Category.SECURITY, msg, false);
            return msg;
        }
        treasury -= cost;
        String done = UnionState.PACT_STAGES[union.pactStage];
        union.pactStage++;
        String msg = union.isPactMember()
                ? Strings.t("ВСТУП ДО ОБОРОННОГО ПАКТУ! Бойова міць +35% та колективна оборона!")
                : Strings.f("Пакт: пройдено етап «%s» (-%s ₴).", Strings.t(done), cost);
        log(LogEntry.Category.SECURITY, msg, false);
        return msg;
    }

    public String voteUnionCouncil() {
        if (!union.isUnionMember()) return Strings.t("Доступно після вступу до Унії.");
        if (month - union.lastTrancheMonth < 2) return Strings.f("Наступна сесія Ради Унії через %s міс.", 2 - (month - union.lastTrancheMonth));
        union.lastTrancheMonth = month;
        union.unionCouncilVotesWon++;
        union.unionPresidency = (union.unionCouncilVotesWon % 3 == 0);
        int tranche = union.unionPresidency ? 950 : 620;
        treasury += tranche;
        String msg = Strings.f("Рада Унії: ініціативу схвалено, плановий транш фондів +%s ₴%s", tranche,
                union.unionPresidency ? Strings.t(" (Головування в Унії!)") : ".");
        log(LogEntry.Category.PARLIAMENT, msg, false);
        return msg;
    }

    // --------------------------------------------------- Sovereign Fund, ACB Mole & Custom Debt

    public String contributeSovereignFund(int amount) {
        if (treasury < amount) return Strings.t("Недостатньо коштів у скарбниці.");
        treasury -= amount;
        sovereignFund += amount;
        String msg = Strings.f("Внесок у Суверенний фонд +%d ₴ (витримка %.1f міс.).", amount, sovereignEnduranceMonths(0));
        log(LogEntry.Category.ECONOMY, msg, false);
        return msg;
    }

    public String toggleAcbMole() {
        if (!acbMoleActive) {
            if (treasury < 350) return Strings.t("Потрібно 350 ₴ на вербування агента.");
            treasury -= 350;
            acbMoleActive = true;
            acbMoleDormant = true; // Safe "на дні" by default
            String msg = Strings.t("«Своя людина в АКБ» на дні: ризику викриття немає, лише утримання -35 ₴/міс.");
            log(LogEntry.Category.PARLIAMENT, msg, false);
            return msg;
        } else {
            acbMoleActive = false;
            acbMoleDormant = true;
            String msg = Strings.t("Агента в АКБ відпущено — зв'язок припинено.");
            log(LogEntry.Category.PARLIAMENT, msg, false);
            return msg;
        }
    }

    public String triggerAcbMoleTask() {
        if (!acbMoleActive) return Strings.t("Спочатку залучіть агента в АКБ.");
        acbMoleDormant = false;
        acbAttention = Math.max(0, acbAttention - 28.0);
        if (rng.nextDouble() < 0.22) {
            acbMoleActive = false;
            acbAttention = Math.min(100, acbAttention + 35);
            trust = clamp(trust - 8, 0, 100);
            dossierIndex++; // Successor fills the chair!
            String msg = Strings.f("ВИКРИТТЯ! Агента в АКБ викрито — досьє передано наступнику (%s).", currentDossierName());
            log(LogEntry.Category.CRISIS, msg, true);
            return msg;
        }
        acbMoleDormant = true;
        String msg = Strings.f("Доручення агенту АКБ виконано: увага АКБ знижена до %s%%. Агент знову «на дні».", Math.round(acbAttention));
        log(LogEntry.Category.PARLIAMENT, msg, false);
        return msg;
    }

    public String repayCustom(double amount) {
        if (status != GameStatus.PLAYING) return Strings.t("Каденція завершена.");
        double pay = Math.min(amount, Math.min(debt, Math.max(0, treasury)));
        if (pay < 10) return Strings.t("Немає боргу або коштів для погашення.");
        double saved = estimateInterestSavedMonthly(pay);
        treasury -= pay;
        debt -= pay;
        trust = clamp(trust + 1.0, 0, 100);
        String msg = Strings.f("Погашено борг -%.0f ₴ (економія %.1f ₴/міс на відсотках).", pay, saved);
        log(LogEntry.Category.ECONOMY, msg, false);
        appendNote("Погашення-" + Math.round(pay));
        return msg;
    }

    public String repay(int amount) { return repayCustom(amount); }

    // --------------------------------------------------- Interactive Parliament & Ministries

    public int forecastLawVotes(LawType law) {
        if (modAutoParliament) return 100;
        boolean enacting = !hasLaw(law);
        double yes = coalitionBonus;
        for (FactionType f : FactionType.values()) {
            double stance = factionLawStance(f, law);
            if (!enacting) stance = -stance * 0.7;
            double baseProb = 0.42 + (factionSupport(f) - 45.0) * 0.0065 + stance;
            if (law == LawType.MARTIAL_LAW && enacting && (terrorThreat > 45 || occupiedRegions() > 0 || activeWarsCount() > 0)) {
                baseProb += 0.32;
            }
            yes += seats(f) * clamp(baseProb, 0.05, 0.96);
        }
        return (int) Math.round(clamp(yes, 0, 100));
    }

    public int forecastMinisterVotes(MinistryType type) {
        if (modAutoParliament) return 100;
        double yes = 52.0 + coalitionBonus + (parliamentSupport() - 45.0) * 0.85;
        if (type == MinistryType.DEFENSE && (activeWarsCount() > 0 || terrorThreat > 40)) yes += 15;
        return (int) Math.round(clamp(yes, 15, 98));
    }

    private double factionLawStance(FactionType f, LawType l) {
        switch (l) {
            case PRIVATIZATION:
                return f == FactionType.LIBERALS ? 0.42 : (f == FactionType.TECHNOCRATS ? 0.25 : (f == FactionType.SOCIALISTS ? -0.35 : 0.05));
            case LAND_MARKET:
                return f == FactionType.LIBERALS ? 0.38 : (f == FactionType.TECHNOCRATS ? 0.30 : (f == FactionType.SOCIALISTS ? -0.22 : 0.05));
            case ANTI_CORRUPTION:
                return f == FactionType.TECHNOCRATS ? 0.45 : (f == FactionType.LIBERALS ? 0.28 : (f == FactionType.SOCIALISTS ? 0.18 : 0.08));
            case MIN_WAGE:
            case STATE_INSURANCE:
                return f == FactionType.SOCIALISTS ? 0.45 : (f == FactionType.LIBERALS ? -0.25 : 0.12);
            case LUXURY_TAX:
            case CHILDLESS_TAX:
                return f == FactionType.SOCIALISTS ? 0.35 : (f == FactionType.LIBERALS ? -0.38 : 0.02);
            case GREEN_TAX:
            case ROAD_VIGNETTE:
                return f == FactionType.TECHNOCRATS ? 0.38 : (f == FactionType.LIBERALS ? 0.12 : 0.05);
            case CONTRACT_ARMY:
                return f == FactionType.SECUROCRATS ? 0.38 : (f == FactionType.TECHNOCRATS ? 0.22 : 0.12);
            case MARTIAL_LAW:
            default:
                return f == FactionType.SECUROCRATS ? 0.35 : (f == FactionType.LIBERALS ? -0.42 : -0.18);
        }
    }

    public String toggleLaw(LawType law) {
        if (status != GameStatus.PLAYING) return Strings.t("Каденція завершена.");
        boolean enacting = !laws.contains(law);
        int votes = forecastLawVotes(law);
        if (votes < 51) {
            String msg = Strings.f("Парламент відхилив «%s» (%s/100 ЗА). Проведіть переговори.", Strings.t(law.title), votes);
            log(LogEntry.Category.PARLIAMENT, msg, true);
            return msg;
        }
        if (enacting) laws.add(law);
        else laws.remove(law);
        if (coalitionBonus > 0) coalitionBonus = Math.max(0, coalitionBonus - 4);
        recomputeFactions();
        recomputeDerived();
        String msg = Strings.f("Парламент %s «%s» (%s/100 ЗА).", enacting ? Strings.t("ухвалив") : Strings.t("скасував"), Strings.t(law.title), votes);
        log(LogEntry.Category.PARLIAMENT, msg, false);
        appendNote("Закон:" + law.title + (enacting ? "+" : "-"));
        return msg;
    }

    public String upgradeMinistry(MinistryType type) {
        if (status != GameStatus.PLAYING) return Strings.t("Каденція завершена.");
        int lvl = ministryLevel(type);
        if (lvl >= MinistryType.MAX_LEVEL) return Strings.f("%s вже на макс. рівні.", Strings.t(type.title));
        int votes = forecastMinisterVotes(type);
        if (votes < 51) {
            String msg = Strings.f("Парламент не затвердив міністра %s (%s/100 ЗА). Проведіть переговори.", Strings.t(type.title), votes);
            log(LogEntry.Category.PARLIAMENT, msg, true);
            return msg;
        }
        int cost = modFreeBuild ? 0 : type.upgradeCost(lvl);
        if (treasury < cost) {
            String msg = Strings.f("Недостатньо коштів: потрібно %s ₴.", cost);
            log(LogEntry.Category.ACTION, msg, false);
            return msg;
        }
        treasury -= cost;
        ministries.put(type, lvl + 1);
        recomputeFactions();
        recomputeDerived();
        String msg = Strings.f("Парламент затвердив (%s/100): %s → Рів. %s (-%s ₴)", votes, Strings.t(type.title), lvl + 1, cost);
        log(LogEntry.Category.PARLIAMENT, msg, false);
        appendNote(type.title + ":L" + (lvl + 1));
        return msg;
    }

    public String negotiateCoalition() {
        if (status != GameStatus.PLAYING) return Strings.t("Каденція завершена.");
        int cost = 280;
        if (treasury < cost) {
            String msg = Strings.f("Потрібно %s ₴ на коаліційні поступки.", cost);
            log(LogEntry.Category.PARLIAMENT, msg, false);
            return msg;
        }
        treasury -= cost;
        coalitionBonus = Math.min(24, coalitionBonus + 12);
        String msg = Strings.f("Коаліційні переговори: +%s голосів підтримки в парламенті (-280 ₴).", coalitionBonus);
        log(LogEntry.Category.PARLIAMENT, msg, false);
        return msg;
    }

    public String voteOfConfidence() {
        if (status != GameStatus.PLAYING) return Strings.t("Каденція завершена.");
        int yes = (int) Math.round(clamp(parliamentSupport() + coalitionBonus, 5, 98));
        if (yes >= 51) {
            trust = clamp(trust + 5.0, 0, 100);
            coalitionBonus = Math.min(20, coalitionBonus + 6);
            String msg = Strings.f("Вотум довіри пройдено (%s/100 ЗА): довіра +5%%.", yes);
            log(LogEntry.Category.PARLIAMENT, msg, false);
            return msg;
        } else {
            trust = clamp(trust - 5.0, 0, 100);
            String msg = Strings.f("Вотум довіри провалено (%s/100 ЗА): довіра -5%%.", yes);
            log(LogEntry.Category.PARLIAMENT, msg, true);
            return msg;
        }
    }

    // --------------------------------------------------- Counter-Terror & Core Actions

    public int liberationChancePct(int regionIndex) {
        if (regionIndex < 0 || regionIndex >= regions.size()) return 0;
        RegionState r = regions.get(regionIndex);
        if (!r.occupied) return 100;
        double specOps = troops(MilitaryBranch.SSO) * 2.2 + troops(MilitaryBranch.CSP) * 2.5
                + troops(MilitaryBranch.GUR) * 1.8 + troops(MilitaryBranch.KORD) * 1.0;
        double force = defensePower() + specOps;
        double ratio = force / Math.max(25.0, r.terrorStrength);
        return (int) Math.round(clamp(22.0 + ratio * 44.0, 15.0, 96.0));
    }

    public String liberateRegion(int regionIndex) {
        if (status != GameStatus.PLAYING) return Strings.t("Каденція завершена.");
        if (regionIndex < 0 || regionIndex >= regions.size()) return "";
        RegionState r = regions.get(regionIndex);
        if (!r.occupied) return Strings.t("Регіон уже під контролем.");
        int cost = 480;
        if (treasury < cost) {
            String msg = Strings.f("Потрібно %s ₴ на операцію звільнення.", cost);
            log(LogEntry.Category.SECURITY, msg, true);
            return msg;
        }
        treasury -= cost;
        int chance = liberationChancePct(regionIndex);
        if ((rng.nextDouble() * 100.0) < chance) {
            r.occupied = false;
            r.terrorStrength = 0;
            terrorThreat = clamp(terrorThreat - 22.0, 0, 100);
            trust = clamp(trust + 8.0, 0, 100);
            recomputeFactions();
            String msg = Strings.f("ЗВІЛЬНЕНО! Сили оборони деокупували регіон %s (шанс %s%%).", Strings.t(r.name), chance);
            log(LogEntry.Category.SECURITY, msg, false);
            appendNote("Звільнено:" + r.name);
            return msg;
        } else {
            r.terrorStrength = Math.max(25.0, r.terrorStrength * 0.72);
            trust = clamp(trust - 3.0, 0, 100);
            String msg = Strings.f("Штурм %s відбито (шанс %s%%), сили бойовиків ослаблено.", Strings.t(r.name), chance);
            log(LogEntry.Category.CRISIS, msg, true);
            return msg;
        }
    }

    public String counterTerrorSweep() {
        if (status != GameStatus.PLAYING) return Strings.t("Каденція завершена.");
        int cost = 320;
        if (treasury < cost) return Strings.t("Потрібно 320 ₴ на зачистку.");
        treasury -= cost;
        double drop = 16.0 + troops(MilitaryBranch.GUR) * 0.45 + troops(MilitaryBranch.SSO) * 0.35
                + troops(MilitaryBranch.CSP) * 0.55 + troops(MilitaryBranch.KORD) * 0.25;
        terrorThreat = clamp(terrorThreat - drop, 0, 100);
        String msg = Strings.f("Зачистка ГУР/ССО/ЦСП: загроза терору знижена на %.0f%% (до %.0f%%).", drop, terrorThreat);
        log(LogEntry.Category.SECURITY, msg, false);
        return msg;
    }

    private RegionState seizeWeakestRegion(double baseStrength) {
        RegionState target = null;
        for (RegionState r : regions) {
            if (r.occupied) continue;
            if (target == null || r.population < target.population) target = r;
        }
        if (target == null) return null;
        target.occupied = true;
        target.terrorStrength = clamp(baseStrength, 50.0, 260.0);
        trust = clamp(trust - 10.0, 0, 100);
        fxRate = clamp(fxRate * 1.05, 20.0, 180.0);
        String msg = Strings.f("ТРИВОГА! Терористи захопили регіон %s (сила %s).", Strings.t(target.name), Math.round(target.terrorStrength));
        log(LogEntry.Category.CRISIS, msg, true);
        appendNote("ОКУПАЦІЯ:" + target.name);
        return target;
    }

    public String toggleFixedFx() {
        fixedFx = !fixedFx;
        String msg = fixedFx ? Strings.f("Курс зафіксовано біля паритету %.1f ₴/$", fxRate)
                : Strings.t("Повернуто плаваючий валютний курс");
        log(LogEntry.Category.ECONOMY, msg, false);
        return msg;
    }

    public String fxIntervene(int reserveDeltaM) {
        if (status != GameStatus.PLAYING) return Strings.t("Каденція завершена.");
        if (reserveDeltaM < 0) {
            double sell = Math.min(fxReserves, -reserveDeltaM);
            if (sell < 10) return Strings.t("Недостатньо ЗВР.");
            fxReserves -= sell;
            fxRate = clamp(fxRate * (1.0 - sell * 0.0012), 20.0, 150.0);
            moneyImpulse -= sell * 0.025;
            String msg = Strings.f("Інтервенція НБК: продано $%.0fM ЗВР → курс %.1f ₴/$", sell, fxRate);
            log(LogEntry.Category.ECONOMY, msg, false);
            return msg;
        } else {
            double buy = Math.min(reserveDeltaM, fxBuyLimitLeft);
            if (buy < 10) {
                String msg = Strings.t("Ліміт купівлі ЗВР вичерпано (відновлюється з часом і поза грою).");
                log(LogEntry.Category.ECONOMY, msg, false);
                return msg;
            }
            double cost = buy * 6.0;
            if (treasury < cost) return Strings.f("Недостатньо бюджету (%s ₴).", (int) cost);
            treasury -= cost;
            fxReserves += buy;
            fxBuyLimitLeft -= buy;
            String msg = Strings.f("Викуп ЗВР +$%.0fM (-%.0f ₴ · ліміт $%.0fM)", buy, cost, fxBuyLimitLeft);
            log(LogEntry.Category.ECONOMY, msg, false);
            return msg;
        }
    }

    public String toggleQuarantine() {
        quarantine = !quarantine;
        String msg = quarantine ? Strings.t("Карантин увімкнено") : Strings.t("Карантин знято");
        log(LogEntry.Category.CRISIS, msg, false);
        return msg;
    }

    public String toggleVaccination() {
        vaccination = !vaccination;
        String msg = vaccination ? Strings.t("Кампанія вакцинації запущена (-120 ₴/міс)") : Strings.t("Вакцинацію призупинено");
        log(LogEntry.Category.CRISIS, msg, false);
        return msg;
    }

    public void captureBaseline() {
        baselineHistory.clear();
        baselineHistory.addAll(history);
        log(LogEntry.Category.ECONOMY, Strings.f("Зафіксовано базову траєкторію A (%s міс.).", baselineHistory.size()), false);
    }

    public boolean hasBaseline() { return baselineHistory.size() >= 2; }

    public String setRegionTax(int index, int newRate) {
        if (index < 0 || index >= regions.size()) return "";
        RegionState r = regions.get(index);
        r.taxRate = Math.max(0, Math.min(55, newRate));
        syncAggregateTax();
        recomputeFactions();
        String msg = Strings.f("%s: податок %s%%", Strings.t(r.name), r.taxRate);
        log(LogEntry.Category.ECONOMY, msg, false);
        return msg;
    }

    public String setRegionZone(int index, EconomicZone zone) {
        if (index < 0 || index >= regions.size() || zone == null) return "";
        RegionState r = regions.get(index);
        r.zone = zone;
        String msg = Strings.f("%s: зона «%s»", Strings.t(r.name), Strings.t(zone.title));
        log(LogEntry.Category.ECONOMY, msg, false);
        return msg;
    }

    public int foundRegionCost() { return 900 + Math.max(0, regions.size() - 3) * 450; }
    public boolean canFoundRegion() { return regions.size() < 9; }

    public String foundRegion() {
        if (status != GameStatus.PLAYING) return Strings.t("Каденція завершена.");
        if (!canFoundRegion()) return Strings.t("Максимум областей (9).");
        int cost = foundRegionCost();
        if (treasury < cost) return Strings.f("Потрібно %s ₴.", cost);
        treasury -= cost;
        int extraIdx = Math.max(0, regions.size() - 3) % EXTRA_REGIONS.length;
        String name = EXTRA_REGIONS[extraIdx];
        boolean coastal = extraIdx % 2 == 1;
        regions.add(new RegionState(name, 85, 18, EconomicZone.STANDARD, coastal, 80 + extraIdx * 10));
        population += 85;
        syncAggregateTax();
        recomputeDerived();
        String msg = Strings.f("Засновано регіон: %s%s (-%s ₴)", Strings.t(name), coastal ? Strings.t(" (приморський)") : "", cost);
        log(LogEntry.Category.ACTION, msg, false);
        return msg;
    }

    public String recruit(MilitaryBranch branch) {
        if (status != GameStatus.PLAYING) return Strings.t("Каденція завершена.");
        int cost = modFreeBuild ? 0 : branch.batchCost;
        if (treasury < cost) return Strings.f("Недостатньо коштів: потрібно %s ₴.", cost);
        if (!modGodMode && totalTroops() + branch.batchSize > population * 0.28) return Strings.t("Ліміт мобілізації (28% населення).");
        treasury -= cost;
        military.put(branch, troops(branch) + branch.batchSize);
        recomputeFactions();
        String msg = Strings.f("%s: +%s бійців (-%s ₴)", Strings.t(branch.title), branch.batchSize, cost);
        log(LogEntry.Category.SECURITY, msg, false);
        return msg;
    }

    public String dismiss(MilitaryBranch branch) {
        int have = troops(branch);
        if (have <= 0) return Strings.t("Немає особового складу.");
        int cut = Math.min(have, branch.batchSize);
        military.put(branch, have - cut);
        recomputeFactions();
        String msg = Strings.f("%s: демобілізовано -%s", Strings.t(branch.title), cut);
        log(LogEntry.Category.SECURITY, msg, false);
        return msg;
    }

    public String buyVehicle(VehicleType type) {
        int cost = modFreeBuild ? 0 : type.cost;
        if (treasury < cost) return Strings.f("Недостатньо коштів: потрібно %s ₴.", cost);
        treasury -= cost;
        vehicles.put(type, vehicleCount(type) + 1);
        recomputeDerived();
        String msg = Strings.f("Придбано: %s (-%s ₴)", Strings.t(type.title), cost);
        log(type.military ? LogEntry.Category.SECURITY : LogEntry.Category.ACTION, msg, false);
        return msg;
    }

    public String sellVehicle(VehicleType type) {
        int have = vehicleCount(type);
        if (have <= 0) return Strings.t("Немає одиниць для продажу.");
        int refund = (int) Math.round(type.cost * 0.55);
        vehicles.put(type, have - 1);
        treasury += refund;
        String msg = Strings.f("Продано: %s (+%s ₴)", Strings.t(type.title), refund);
        log(LogEntry.Category.ACTION, msg, false);
        return msg;
    }

    public String borrow(int amount) {
        if (debt + amount > creditLimit()) return Strings.t("Перевищено кредитний ліміт.");
        treasury += amount;
        debt += amount;
        String msg = Strings.f("Облігації +%s ₴ (борг %s ₴)", amount, Math.round(debt));
        log(LogEntry.Category.ECONOMY, msg, false);
        return msg;
    }

    public String applyShock(ShockType shock) {
        if (status != GameStatus.PLAYING) return Strings.t("Каденція завершена.");
        double intelShield = Math.max(0.45, 1.0 - (troops(MilitaryBranch.GUR) * 0.010
                + troops(MilitaryBranch.CSP) * 0.012 + troops(MilitaryBranch.KORD) * 0.005));
        if (hasLaw(LawType.MARTIAL_LAW)) intelShield *= 0.78;
        String msg;
        switch (shock) {
            case HELICOPTER_MONEY:
                moneyImpulse += 5.2;
                trust = clamp(trust + 5.0, 0, 100);
                msg = Strings.t("Гелікоптерні гроші: попит і ціни розігріваються.");
                log(LogEntry.Category.ECONOMY, msg, false);
                break;
            case BANK_PANIC:
                panicImpulse += 4.5 * intelShield;
                trust = clamp(trust - 11.0 * intelShield, 0, 100);
                treasury -= fairCrisisCost(350) * intelShield;
                fxRate *= (1.0 + 0.04 * intelShield);
                msg = Strings.t("Банківська паніка: відтік депозитів і тиск на валюту.");
                log(LogEntry.Category.CRISIS, msg, true);
                break;
            case TECH_BOOM:
                tfp += 0.14; // Uncapped!
                supplyShock -= 1.8;
                trust = clamp(trust + 4.0, 0, 100);
                msg = Strings.t("Технологічний бум: +14% продуктивності (TFP).");
                log(LogEntry.Category.ECONOMY, msg, false);
                break;
            case GDP_BOOM:
                tfp *= 2.2; // Uncapped lab GDP explosion!
                recomputeDerived();
                msg = Strings.f("ВИБУХ ВВП (Лабораторія): TFP %.2f× → ВВП %.0f ₴!", tfp, gdp);
                log(LogEntry.Category.ECONOMY, msg, false);
                break;
            case EPIDEMIC:
                epidemicLevel = clamp(epidemicLevel + 42.0, 0, 100);
                trust = clamp(trust - 5.0, 0, 100);
                msg = Strings.t("Спалах епідемії! Увімкніть карантин або вакцинацію.");
                log(LogEntry.Category.CRISIS, msg, true);
                break;
            case CYBER_ATTACK:
                double dmg = fairCrisisCost(420) * intelShield;
                treasury -= dmg;
                panicImpulse += 2.2 * intelShield;
                msg = Strings.f("Кібератака (-%.0f ₴ · захист ГУР/ЦСП/КОРД %.0f%%).", dmg, (1 - intelShield) * 100);
                log(LogEntry.Category.CRISIS, msg, true);
                break;
            case TERROR_RAID:
                terrorThreat = clamp(terrorThreat + 38.0 * intelShield, 0, 100);
                if (defensePower() < 115.0 || terrorThreat >= 65.0) {
                    RegionState seized = seizeWeakestRegion(65.0 + terrorThreat * 0.55);
                    msg = seized != null ? Strings.f("Терористичний рейд: захоплено %s!", Strings.t(seized.name)) : Strings.t("Посилення натиску бойовиків!");
                    if (occupiedRegions() >= regions.size()) status = GameStatus.OVERRUN;
                } else {
                    msg = Strings.f("ЗСУ та ЦСП відбили рейд! Загроза: %.0f%%.", terrorThreat);
                    log(LogEntry.Category.SECURITY, msg, false);
                }
                break;
            case GENERAL_STRIKE:
            default:
                supplyShock += 3.8 * intelShield;
                trust = clamp(trust - 9.0 * intelShield, 0, 100);
                msg = Strings.t("Страйк і блокада: поставки зірвано, ризик стагфляції.");
                log(LogEntry.Category.CRISIS, msg, true);
                break;
        }
        appendNote("Шок:" + shock.title);
        return msg;
    }

    public String resolveEvent(int choiceIndex) {
        if (currentEvent == null || choiceIndex < 0 || choiceIndex >= currentEvent.choices.length) return "";
        CityEvent.Choice c = currentEvent.choices[choiceIndex];
        treasury += c.treasuryDelta;
        debt = Math.max(0, debt + c.debtDelta);
        trust = clamp(trust + c.trustDelta, 0, 100);
        moneyImpulse += c.inflationDelta;
        tfp = Math.max(0.5, tfp + c.tfpDelta);
        ecology = clamp(ecology + c.ecologyDelta, 5, 100);
        if ("corruption_scandal".equals(currentEvent.id)) {
            corruption = clamp(corruption + (choiceIndex == 0 ? -9.0 : 8.0), 2, 95);
            if (choiceIndex == 0) dossierIndex++;
            else { acbAttention = Math.min(100, acbAttention + 18); log(LogEntry.Category.CRISIS, Strings.t("АКБ зафіксувало підозрілий переказ після скандалу (+18% уваги)."), true); }
        }
        if ("epidemic_wave".equals(currentEvent.id) && choiceIndex == 0) epidemicLevel = Math.max(0, epidemicLevel - 18.0);
        if ("fx_pressure".equals(currentEvent.id) && choiceIndex == 0) fxRate = Math.max(25.0, fxRate * 0.94);
        if ("terror_ultimatum".equals(currentEvent.id)) terrorThreat = clamp(terrorThreat + (choiceIndex == 0 ? -16.0 : 12.0), 0, 100);
        if ("parliament_gridlock".equals(currentEvent.id)) coalitionBonus = Math.max(0, Math.min(24, coalitionBonus + (choiceIndex == 0 ? 10 : -4)));
        if ("brain_drain".equals(currentEvent.id) && choiceIndex == 2) specialists = Math.max(0, specialists - 18);
        if ("neighbor_threat".equals(currentEvent.id) && choiceIndex == 2) {
            for (NeighborCountry n : neighbors) if (n.relations < 0 && !n.atWar) n.relations += 12;
        }
        if (c.popDelta != 0 && !regions.isEmpty()) {
            regions.get(0).population = Math.max(40, regions.get(0).population + c.popDelta);
            population = Math.max(100, totalRegionPop());
        }
        if (c.bonusBuilding != null) build(c.bonusBuilding);
        lastEventId = currentEvent.id;
        log(LogEntry.Category.ACTION, Strings.f("%s — %s", Strings.t(currentEvent.title), Strings.t(c.outcome)), false);
        appendNote(currentEvent.badge + ":" + c.label);
        currentEvent = null;
        recomputeFactions();
        recomputeDerived();
        return Strings.t(c.outcome);
    }

    // --------------------------------------------------- Checkpoints & Offline Regen

    public void saveCheckpoint(String label) {
        String snap = toJsonWithoutCheckpoints();
        checkpoints.add(0, new Checkpoint(month, label, gdp, treasury, snap));
        while (checkpoints.size() > 5) checkpoints.remove(checkpoints.size() - 1);
    }

    public boolean restoreCheckpoint(int index) {
        if (index < 0 || index >= checkpoints.size()) return false;
        List<Checkpoint> preserved = new ArrayList<>(checkpoints);
        boolean ok = loadJson(preserved.get(index).snapshotJson);
        if (ok) {
            checkpoints.clear();
            checkpoints.addAll(preserved);
            log(LogEntry.Category.ACTION, Strings.f("Відновлено контрольну точку: %s", preserved.get(index).label), false);
        }
        return ok;
    }

    public void regenerateOfflineLimits() {
        long now = System.currentTimeMillis();
        if (lastSavedEpochMs > 0 && now > lastSavedEpochMs) {
            double minutesAway = (now - lastSavedEpochMs) / 60000.0;
            fxBuyLimitLeft = Math.min(350.0, fxBuyLimitLeft + minutesAway * 8.0);
        }
        lastSavedEpochMs = now;
    }

    // -------------------------------------------------------- Monthly Step

    public void nextMonth() {
        if (status != GameStatus.PLAYING) return;
        month++;
        if (coalitionBonus > 0) coalitionBonus = Math.max(0, coalitionBonus - 2);
        fxBuyLimitLeft = Math.min(350.0, fxBuyLimitLeft + 35.0);

        // Deliver in-transit service equipment
        for (int i = 0; i < 4; i++) {
            if (serviceInTransit[i] > 0) {
                serviceEquipment[i] += serviceInTransit[i];
                serviceInTransit[i] = 0;
            }
        }

        // 0. 2-month lagged policy rate transmission (like a real central bank!)
        int effectiveRate = laggedRate2;
        laggedRate2 = laggedRate1;
        laggedRate1 = policyRate;

        // 1. Compounding organic productivity growth (uncapped path for developed economies!)
        double devGrowth = 0.002 + count(BuildingType.UNIVERSITY) * 0.003 + count(BuildingType.TECH_PARK) * 0.0035
                + ministryLevel(MinistryType.EDUCATION) * 0.002;
        tfp = tfp * (1.0 + devGrowth);

        // Graduates from Colleges & Universities replenish real workforce!
        int grads = count(BuildingType.COLLEGE) * 6 + count(BuildingType.UNIVERSITY) * 10;
        specialists += grads;

        // 2. Global commodity cycles
        double wave = month * 0.42;
        priceEnergy = clamp(1.0 + 0.22 * Math.sin(wave) + (rng.nextDouble() - 0.5) * 0.08, 0.65, 1.65);
        priceGrain = clamp(1.0 + 0.18 * Math.cos(wave * 0.8 + 1.1) + (rng.nextDouble() - 0.5) * 0.06, 0.70, 1.55);
        priceMetal = clamp(1.0 + 0.20 * Math.sin(wave * 0.7 + 2.4) + (rng.nextDouble() - 0.5) * 0.06, 0.70, 1.55);
        priceTech = clamp(1.0 + 0.15 * Math.cos(wave * 0.55) + 0.004 * month, 0.80, 2.20);

        // 3. Neighbor relations, trade deals countdown & war pressure
        for (NeighborCountry n : neighbors) {
            if (n.capitulated) continue;
            if (n.tradeDealDaysLeft > 0) n.tradeDealDaysLeft = Math.max(0, n.tradeDealDaysLeft - 30);
            if (n.embassy && !n.atWar) n.relations = Math.min(100, n.relations + 1);
            if (n.culturalCenter && !n.atWar) n.relations = Math.min(100, n.relations + 1);
            if (n.aidSharePct > 30 && !n.atWar) n.relations = Math.min(100, n.relations + 2);
            if (n.atWar) {
                if (n.seaBlockaded) n.militaryPower = Math.max(20, n.militaryPower * 0.94);
                // Enemy counter-pressure if our defense is weaker
                if (defensePower() < n.militaryPower * 0.85) {
                    treasury -= 180;
                    trust = clamp(trust - 2.5, 0, 100);
                }
            } else if (n.relations < -65 && defensePower() < n.militaryPower * 0.75 && rng.nextDouble() < 0.14) {
                n.atWar = true;
                log(LogEntry.Category.CRISIS, Strings.f("НАПАД! %s (%s) розпочала війну проти вас!", Strings.t(n.name), Strings.t(n.commander)), true);
            }
        }
        if (pmc.foreignContract && pmc.squadSize > 0) pmc.experience += 2;

        // 4. Terror threat & regional wear
        double sec = securityScore();
        double def = defensePower();
        if (modNoWearTerror) {
            terrorThreat = 0.0;
            for (RegionState r : regions) r.wear = 0.0;
        } else {
            double terrorDelta = (sec < 38 ? (38 - sec) * 0.14 : -(sec - 38) * 0.16)
                    + (corruption > 32 ? (corruption - 32) * 0.09 : 0)
                    + (trust < 42 ? (42 - trust) * 0.10 : -0.6)
                    - (def > 120 ? 1.8 : 0) + occupiedRegions() * 2.5;
            terrorThreat = clamp(terrorThreat + terrorDelta, 0.0, 100.0);
            if (terrorThreat >= 72.0 && occupiedRegions() < regions.size()) {
                seizeWeakestRegion(60.0 + terrorThreat * 0.60);
                terrorThreat = Math.max(40.0, terrorThreat - 22.0);
            }

            // Infrastructure wear on energy & industry
            for (RegionState r : regions) {
                int wearObjs = 0;
                for (BuildingType b : BuildingType.values()) if (b.wearsOut) wearObjs += r.count(b);
                if (wearObjs > 0) r.wear = clamp(r.wear + 1.8 + wearObjs * 0.45, 0, 100);
            }
        }

        // 5. Epidemic, Realistic Corruption, ACB Attention & Coast Guard shadow crackdown
        double medPower = count(BuildingType.HOSPITAL) * 3.5 + vehicleCount(VehicleType.AMBULANCE) * 2.2
                + ministryLevel(MinistryType.HEALTH) * 3.8;
        if (epidemicLevel > 0.5) {
            double spread = (quarantine ? -7.5 : 3.5) - (vaccination ? (8.5 + medPower * 0.45) : medPower * 0.22);
            epidemicLevel = clamp(epidemicLevel + spread, 0.0, 100.0);
        }

        double corruptTarget = 22.0 + Math.max(0, taxRate - 25) * 0.55
                - (hasLaw(LawType.ANTI_CORRUPTION) ? 18.0 : 0)
                - ministryLevel(MinistryType.FINANCE) * 2.4
                - count(BuildingType.COURT) * 3.0
                - troops(MilitaryBranch.GUR) * 0.12;
        corruption = clamp(corruption * 0.85 + clamp(corruptTarget, 3, 85) * 0.15, 2.0, 90.0);

        double coastGuardPush = navalCount(NavalUnit.COAST_GUARD) * NavalUnit.COAST_GUARD.shadowReduction;
        double shadowTarget = 8.0 + Math.max(0, taxRate - 18) * 0.75 + corruption * 0.42
                - (hasLaw(LawType.ANTI_CORRUPTION) ? 7.0 : 0) - coastGuardPush;
        shadowEconomy = clamp(shadowEconomy * 0.82 + clamp(shadowTarget, 3, 65) * 0.18, 3.0, 65.0);

        // Realistic ACB attention with cooldown between cases
        if (acbCooldown > 0) acbCooldown--;
        else if (corruption > 35 || treasury < 0) {
            double bump = (corruption > 35 ? 4.5 : 0) + (treasury < 0 ? 5.0 : 0);
            acbAttention = clamp(acbAttention + bump, 0, 100);
            if (bump > 0) {
                String why = treasury < 0 ? Strings.t("бюджетна «діра» й перекази") : Strings.t("висока корупція");
                log(LogEntry.Category.PARLIAMENT, Strings.f("АКБ (%s): увага зросла до %s%% (%s).", currentDossierName(), Math.round(acbAttention), why), acbAttention >= 70);
                acbCooldown = 2; // Pause between cases!
            }
        } else {
            acbAttention = Math.max(0, acbAttention - 3.0);
        }

        // 6. Labor force & employment (Specialists genuinely expand workforce!)
        int laborForce = (int) Math.round(population * 0.62) + foreignSpecialists + (specialists / 4);
        double rateCoolFactor = 1.0 - Math.max(0, effectiveRate - 6) * 0.014;
        double panicFactor = Math.max(0.65, 1.0 - panicImpulse * 0.055);
        double strikeFactor = Math.max(0.70, 1.0 - Math.max(0, supplyShock) * 0.05);
        double epidemicLabor = Math.max(0.68, 1.0 - epidemicLevel * 0.003 - (quarantine ? 0.07 : 0));
        double minWageJobs = hasLaw(LawType.MIN_WAGE) ? 0.965 : 1.0;
        double busMobility = 1.0 + Math.min(0.18, vehicleCount(VehicleType.BUS) * 0.015);
        int effectiveJobs = (int) Math.round(totalJobs() * rateCoolFactor * panicFactor * strikeFactor * epidemicLabor * minWageJobs * busMobility);
        int employed = Math.min(laborForce, Math.max(0, effectiveJobs));
        unemploymentPct = laborForce <= 0 ? 0 : clamp((laborForce - employed) * 100.0 / laborForce, 1.5, 45.0);

        // 7. Regional GDP & Taxes (with size factor, wear penalty, per-region buildings, oil export!)
        int energyBal = energyBalance();
        double energyPenalty = energyBal < 0 ? Math.max(0.62, 1.0 + energyBal * 0.008) : 1.0;
        double eduBoost = 1.0 + count(BuildingType.UNIVERSITY) * 0.055 + count(BuildingType.COLLEGE) * 0.025
                + count(BuildingType.TECH_PARK) * 0.04 + ministryLevel(MinistryType.EDUCATION) * 0.04;
        double econMinBoost = 1.0 + ministryLevel(MinistryType.ECONOMY) * 0.04;
        double tradeMult = tradeRegime == 0 ? 1.09 : (tradeRegime == 1 ? 1.00 : 0.87);
        double portBoost = 1.0 + count(BuildingType.PORT) * 0.045 + ministryLevel(MinistryType.FOREIGN) * 0.03;

        double seaTradeBonus = 1.0;
        for (NavalUnit u : NavalUnit.values()) seaTradeBonus += navalCount(u) * u.tradeBonus;

        double lawGdpMult = 1.0;
        for (LawType l : laws) lawGdpMult += l.gdpEffect;
        lawGdpMult = clamp(lawGdpMult, 0.72, 1.45);

        double commodityBoost = 1.0
                + (priceMetal - 1.0) * Math.min(0.18, count(BuildingType.FACTORY) * 0.04)
                + (priceTech - 1.0) * Math.min(0.22, count(BuildingType.TECH_PARK) * 0.06)
                + (priceGrain - 1.0) * Math.min(0.16, count(BuildingType.FARM) * 0.04);

        double fleetEconBoost = 0;
        for (VehicleType v : VehicleType.values()) fleetEconBoost += vehicleCount(v) * v.economyBoost;
        double infraScale = 1.0 + ministryLevel(MinistryType.INFRASTRUCTURE) * 0.12;
        double fleetGdpBonus = fleetEconBoost * 18.0 * infraScale;

        double capitalWeight = 1.0;
        for (BuildingType b : BuildingType.values()) capitalWeight += count(b) * b.productivityWeight * 0.07;

        double baseOutputPerWorker = 3.9 * tfp * eduBoost * econMinBoost * energyPenalty
                * tradeMult * portBoost * seaTradeBonus * lawGdpMult * commodityBoost * epidemicLabor
                * (0.75 + 0.25 * capitalWeight) * strikeFactor;

        int popSum = Math.max(1, totalRegionPop());
        double totalGdp = 0;
        double totalTaxRevenue = 0;
        double totalExportDirect = 0;
        double finBonus = 1.0 + ministryLevel(MinistryType.FINANCE) * 0.05;
        double corruptionLeak = Math.max(0.62, 1.0 - corruption * 0.0045);
        double shadowLeak = Math.max(0.55, 1.0 - shadowEconomy * 0.006);
        double zoneEcoDelta = 0;
        double zoneTrustBonus = 0;

        for (RegionState r : regions) {
            double share = r.population / (double) popSum;
            double regionWorkers = employed * share;
            double taxIncentive = r.taxRate <= 25 ? 1.0 : Math.max(0.65, 1.0 - (r.taxRate - 25) * 0.012);
            double occPenalty = r.occupied ? 0.25 : 1.0;
            double wearPenalty = Math.max(0.65, 1.0 - r.wear * 0.003);
            double regBuildBonus = 0;
            for (BuildingType b : BuildingType.values()) {
                int cnt = r.count(b);
                if (cnt <= 0) continue;
                regBuildBonus += cnt * b.productivityWeight * 22.0;
                double exp = cnt * b.exportIncome;
                if (b == BuildingType.OIL_REFINERY) exp *= priceEnergy;
                if (b == BuildingType.HIGHWAY && hasLaw(LawType.ROAD_VIGNETTE)) exp *= 1.25;
                if (!r.occupied) totalExportDirect += exp;
            }
            r.gdp = Math.max(20.0, (regionWorkers * baseOutputPerWorker * r.zone.gdpMult * r.sizeDevelopmentFactor()
                    * taxIncentive + fleetGdpBonus * share + regBuildBonus) * occPenalty * wearPenalty);
            r.unemploymentPct = unemploymentPct;
            r.healthScore = clamp(45 + r.count(BuildingType.HOSPITAL) * 22 + ministryLevel(MinistryType.HEALTH) * 6, 10, 100);
            r.eduScore = clamp(40 + r.count(BuildingType.COLLEGE) * 16 + r.count(BuildingType.UNIVERSITY) * 24, 10, 100);
            r.securityScore = clamp(sec - (r.occupied ? 50 : 0), 5, 100);
            totalGdp += r.gdp;

            if (!r.occupied) {
                double compliance = r.taxRate <= 28 ? 1.0 : Math.max(0.50, 1.0 - (r.taxRate - 28) * 0.017);
                totalTaxRevenue += r.gdp * (r.taxRate / 100.0) * compliance * 0.72 * finBonus * corruptionLeak * shadowLeak;
            }
            zoneEcoDelta += r.zone.ecologyDelta * share;
            zoneTrustBonus += r.zone.trustBonus * share;
        }
        if (hasLaw(LawType.LUXURY_TAX)) totalTaxRevenue += totalGdp * 0.025 * corruptionLeak;
        if (hasLaw(LawType.CHILDLESS_TAX)) totalTaxRevenue += totalGdp * 0.010 * corruptionLeak;
        gdp = Math.max(200.0, totalGdp);
        gdpPerCapita = gdp / Math.max(1, population);
        syncAggregateTax();

        // 8. Market-trust FX parity dynamics (no double-drop!)
        double marketConfidence = (trust - 55.0) * 0.0004 - Math.max(0, inflationPct - 5.0) * 0.0035
                + (effectiveRate - 6.0) * 0.0025 - occupiedRegions() * 0.012 - panicImpulse * 0.006;
        fxParity = clamp(fxParity * (1.0 + (inflationPct - 3.0) * 0.0025), 25.0, 140.0);
        if (fixedFx) {
            double reserveBurn = Math.max(0, -marketConfidence) * 550.0;
            fxReserves = Math.max(0, fxReserves - reserveBurn);
            if (fxReserves <= 5.0) {
                fixedFx = false;
                fxRate *= 1.10;
                log(LogEntry.Category.CRISIS, Strings.t("ЗВР вичерпано — фіксований курс відпущено!"), true);
            }
        } else {
            // Converge gently toward parity modulated by market confidence
            double targetFx = fxParity * (1.0 - marketConfidence * 2.5);
            fxRate = clamp(fxRate * 0.85 + targetFx * 0.15, 20.0, 180.0);
            if (tradeRegime == 0 && lastNetIncome > 0) fxReserves = Math.min(5000, fxReserves + 8.0);
        }

        // 9. Inflation & CPI (with law inflation impacts + lagged policy rate!)
        double foodRatio = foodCapacity() / Math.max(1.0, population * 0.82);
        double foodPressure = foodRatio < 1.0 ? (1.0 - foodRatio) * 6.5 : -(foodRatio - 1.0) * 1.4;
        double philipsPressure = Math.max(-1.2, (6.0 - unemploymentPct) * 0.35);
        double monetaryBrake = (effectiveRate - 5.5) * 0.48;
        double logisticsBrake = Math.min(1.6, vehicleCount(VehicleType.TRUCK) * 0.16 + ministryLevel(MinistryType.INFRASTRUCTURE) * 0.25);
        double energyPassThrough = (priceEnergy - 1.0) * 3.2;
        double fxPassThrough = Math.max(-1.5, (fxRate - 40.0) * 0.07);
        double lawInfl = 0;
        for (LawType l : laws) lawInfl += l.inflationImpact;
        double tariffPush = tradeRegime == 2 ? 1.9 : (tradeRegime == 1 ? 0.5 : -0.3);
        double rawInflation = 2.4 + foodPressure + philipsPressure + moneyImpulse * 0.72 + lawInfl
                + supplyShock * 0.85 + tariffPush + energyPassThrough + fxPassThrough - monetaryBrake - logisticsBrake;
        inflationPct = clamp(inflationPct * 0.45 + rawInflation * 0.55, -2.5, 38.0);
        cpi = Math.max(0.5, cpi * (1.0 + inflationPct / 1200.0));

        // 10. Full Budget & Main Expense Tracking
        double tariffRate = tradeRegime == 0 ? 0.0 : (tradeRegime == 1 ? 0.045 : 0.065);
        double freePortBonus = 0;
        for (RegionState r : regions) if (!r.occupied && r.zone == EconomicZone.FREE_PORT) freePortBonus += r.gdp * 0.06;
        double neighborDealsBonus = 0;
        for (NeighborCountry n : neighbors) {
            if (n.atWar || n.capitulated) continue;
            if (n.tradeDealDaysLeft > 0) neighborDealsBonus += 55.0;
            if (n.customsUnion) neighborDealsBonus += 85.0;
            if (n.culturalCenter) neighborDealsBonus += 25.0;
        }
        double fxExportEdge = clamp(fxRate / 40.0, 0.82, 1.35);
        double tradeRevenue = (gdp * tariffRate + totalExportDirect + vehicleCount(VehicleType.CARGO_SHIP) * 75.0
                + freePortBonus + neighborDealsBonus) * (1.0 + ministryLevel(MinistryType.FOREIGN) * 0.06) * seaTradeBonus * fxExportEdge;
        double abroadIncome = specialistsAbroad * 4.5 + pmc.monthlyContractIncome();

        double privUpkeep = hasLaw(LawType.PRIVATIZATION) ? 0.88 : 1.0;
        double buildingUpkeep = 0;
        for (BuildingType b : BuildingType.values()) buildingUpkeep += count(b) * b.upkeep * privUpkeep;

        double ministryUpkeep = 0;
        for (MinistryType m : MinistryType.values()) ministryUpkeep += ministryLevel(m) * m.upkeepPerLevel;

        double milDiscount = Math.max(0.65, 1.0 - ministryLevel(MinistryType.DEFENSE) * 0.06);
        if (hasLaw(LawType.CONTRACT_ARMY)) milDiscount *= 1.25;
        double militaryUpkeep = 0;
        for (MilitaryBranch b : MilitaryBranch.values()) militaryUpkeep += (troops(b) / (double) b.batchSize) * b.monthlyUpkeepPerBatch;
        for (NavalUnit u : NavalUnit.values()) militaryUpkeep += navalCount(u) * u.upkeep;
        militaryUpkeep = militaryUpkeep * milDiscount + pmc.monthlyUpkeep();

        double vehicleUpkeep = 0;
        for (VehicleType v : VehicleType.values()) vehicleUpkeep += vehicleCount(v) * v.upkeep;

        double lawUpkeep = (vaccination ? 120.0 : 0) + (acbMoleActive ? 35.0 : 0) + foreignSpecialists * 2.2;
        for (LawType l : laws) lawUpkeep += l.monthlyCost;

        double welfareCost = population * 0.14 * (welfareLevel / 100.0) * (1.0 + unemploymentPct / 30.0);
        double debtDiscount = Math.max(0.60, 1.0 - ministryLevel(MinistryType.FINANCE) * 0.08);
        double debtInterest = debt * ((0.04 + effectiveRate * 0.008) / 12.0) * debtDiscount;

        // Track main expense for briefing!
        double maxExp = buildingUpkeep; lastMainExpense = "Утримання будівель";
        if (militaryUpkeep > maxExp) { maxExp = militaryUpkeep; lastMainExpense = "Армія та флот"; }
        if (welfareCost > maxExp) { maxExp = welfareCost; lastMainExpense = "Соціальні виплати"; }
        if (ministryUpkeep > maxExp) { maxExp = ministryUpkeep; lastMainExpense = "Міністерства"; }
        if (debtInterest > maxExp) { maxExp = debtInterest; lastMainExpense = "Обслуговування боргу"; }

        lastTaxRevenue = totalTaxRevenue + tradeRevenue + abroadIncome;
        lastTotalExpense = buildingUpkeep + ministryUpkeep + militaryUpkeep + vehicleUpkeep + lawUpkeep + welfareCost + debtInterest;
        lastNetIncome = lastTaxRevenue - lastTotalExpense;
        treasury += lastNetIncome;
        if (treasury < 0) debt += (-treasury) * 0.35;
        if (lastNetIncome < 25) stagnantTreasuryStreak++;
        else stagnantTreasuryStreak = 0;

        // 11. Real wage, Gini, Ecology & Life Expectancy
        double minWageBoost = hasLaw(LawType.MIN_WAGE) ? 1.07 : 1.0;
        double nominalWage = (gdp / Math.max(1, employed)) * 0.68 * (1.0 - taxRate / 220.0) * minWageBoost;
        realWage = nominalWage / cpi;

        double lawGini = 0;
        for (LawType l : laws) lawGini += l.giniDelta;
        double techSkew = count(BuildingType.TECH_PARK) * 0.012;
        double inflationRegressivity = Math.max(0, inflationPct - 4.0) * 0.0045;
        double redistribution = (welfareLevel / 100.0) * 0.08 + (taxRate / 100.0) * 0.04;
        gini = clamp(0.32 + techSkew + inflationRegressivity + lawGini - redistribution, 0.20, 0.60);

        double ecoTarget = 72.0 + zoneEcoDelta + vehicleCount(VehicleType.BUS) * 1.2 + (hasLaw(LawType.GREEN_TAX) ? 10.0 : 0);
        for (BuildingType b : BuildingType.values()) ecoTarget += count(b) * b.ecologyDelta;
        ecology = clamp(ecology * 0.8 + clamp(ecoTarget, 10, 100) * 0.2, 5, 100);
        double services = serviceCoverage();

        double targetLife = 67.5 + (services - 50) * 0.08 + (ecology - 50) * 0.06
                + ministryLevel(MinistryType.HEALTH) * 0.9 + count(BuildingType.STADIUM) * 0.4
                - corruption * 0.06 - epidemicLevel * 0.08;
        lifeExpectancy = clamp(lifeExpectancy * 0.88 + clamp(targetLife, 58, 89) * 0.12, 58.0, 89.0);

        // 12. Trust & Factions
        double lawTrust = 0;
        for (LawType l : laws) lawTrust += l.trustBias;
        double trustDelta = (realWage - 2.3) * 1.4
                - Math.max(0, inflationPct - 5.0) * 0.65
                - Math.max(0, unemploymentPct - 7.5) * 0.45
                - Math.max(0, taxRate - 28) * 0.22
                - epidemicLevel * 0.08
                - Math.max(0, corruption - 30) * 0.06
                - occupiedRegions() * 2.8
                + (services - 55.0) * 0.06
                + (ecology - 55.0) * 0.05
                + (welfareLevel - 30) * 0.03
                + zoneTrustBonus * 0.5 + ministryLevel(MinistryType.HEALTH) * 0.45
                + (sec - 25.0) * 0.025 + lawTrust * 0.35;
        if (energyBal < 0) trustDelta -= 3.5;
        trust = clamp(trust + clamp(trustDelta, -8.5, 6.0), 0, 100);
        recomputeFactions();

        // 13. Regional Population & Migration
        int cap = housingCapacity();
        double housingRatio = cap / (double) Math.max(1, population);
        int newPopSum = 0;
        for (RegionState r : regions) {
            r.trust = clamp(trust + r.zone.trustBonus - Math.max(0, r.taxRate - 26) * 0.6 - (r.occupied ? 35 : 0), 5, 100);
            double regMig = (r.trust - 52.0) * 0.0012 + (realWage - 2.4) * 0.006
                    - Math.max(0, unemploymentPct - 10) * 0.002 - epidemicLevel * 0.0005
                    - (r.occupied ? 0.035 : 0) + r.zone.migrationBonus + (20 - r.taxRate) * 0.0007;
            if (tradeRegime == 0) regMig += 0.006;
            if (housingRatio < 0.95) regMig -= (0.95 - housingRatio) * 0.04;
            int delta = (int) Math.round(r.population * clamp(regMig, -0.055, 0.060));
            r.population = Math.max(40, r.population + delta);
            newPopSum += r.population;
        }
        if (newPopSum > cap + 120) {
            double scale = (cap + 120) / (double) newPopSum;
            newPopSum = 0;
            for (RegionState r : regions) {
                r.population = Math.max(40, (int) Math.round(r.population * scale));
                newPopSum += r.population;
            }
        }
        population = Math.max(120, newPopSum);

        // 14. Decay impulses
        moneyImpulse *= 0.62; if (Math.abs(moneyImpulse) < 0.15) moneyImpulse = 0;
        supplyShock *= 0.60; if (Math.abs(supplyShock) < 0.15) supplyShock = 0;
        panicImpulse *= 0.65; if (Math.abs(panicImpulse) < 0.15) panicImpulse = 0;

        // 15. End conditions (Debt evaluated by interest affordability, NOT hard cap!) & Elections
        if (trust < 15.0) lowTrustStreak++;
        else lowTrustStreak = 0;

        if (!modGodMode && occupiedRegions() >= regions.size()) {
            status = GameStatus.OVERRUN;
            log(LogEntry.Category.CRISIS, Strings.t("Усі регіони захоплено терористами!"), true);
        } else if (!modGodMode && lowTrustStreak >= 2) {
            status = GameStatus.IMPEACHED;
            log(LogEntry.Category.CRISIS, Strings.t("Довіра впала нижче 15% — оголошено імпічмент."), true);
        } else if (!modGodMode && treasury < -3500 && debtServiceRatioPct() > 45.0) {
            status = GameStatus.DEFAULTED;
            log(LogEntry.Category.CRISIS, Strings.t("Дефолт: відсотки по боргу перевищили 45% доходів бюджету!"), true);
        } else if (month % 24 == 0) {
            redistributeSeats();
            double vote = modGodMode ? 88.0 : clamp(trust * 0.55 + parliamentSupport() * 0.35 + (realWage - 2.2) * 8.0
                    - Math.max(0, inflationPct - 6.0) * 1.4 - occupiedRegions() * 12.0, 5, 95);
            lastElectionVotePct = vote;
            if (vote >= 50.0) {
                electionsWon++;
                trust = clamp(trust + 5.0, 0, 100);
                log(LogEntry.Category.PARLIAMENT, Strings.f("ВИБОРИ (М%d): перемога %.1f%%! Склад парламенту оновлено.", month, vote), false);
                currentEvent = CityEvent.pick(this, rng);
            } else {
                status = GameStatus.LOST_ELECTION;
                log(LogEntry.Category.PARLIAMENT, Strings.f("ВИБОРИ (М%d): поразка (%.1f%%).", month, vote), true);
            }
        } else {
            String summary = Strings.f("М%d · ВВП %.0f · Бюджет %+.0f ₴ · Інфл. %.1f%% · Сила %.0f",
                    month, gdp, lastNetIncome, inflationPct, statePowerRating());
            log(LogEntry.Category.ECONOMY, summary, occupiedRegions() > 0);
            currentEvent = CityEvent.pick(this, rng);
        }

        recordSnapshot(pendingNote);
        pendingNote = "";
        if (month % 3 == 0) saveCheckpoint(Strings.f("Авто · Міс. %s", month));
    }

    private void recomputeFactions() {
        double lib = 52.0 + (22 - taxRate) * 1.1 + (tradeRegime == 0 ? 12 : (tradeRegime == 2 ? -14 : 0))
                + (hasLaw(LawType.PRIVATIZATION) ? 12 : 0) + (hasLaw(LawType.LAND_MARKET) ? 9 : 0)
                - (hasLaw(LawType.LUXURY_TAX) ? 10 : 0) - (hasLaw(LawType.MARTIAL_LAW) ? 12 : 0);

        double soc = 48.0 + (welfareLevel - 35) * 0.45 + (hasLaw(LawType.MIN_WAGE) ? 14 : 0)
                + (hasLaw(LawType.LUXURY_TAX) ? 12 : 0) - (hasLaw(LawType.PRIVATIZATION) ? 10 : 0)
                + (0.35 - gini) * 90.0 - Math.max(0, inflationPct - 6) * 1.5;

        double tech = 50.0 + count(BuildingType.UNIVERSITY) * 6 + count(BuildingType.TECH_PARK) * 7
                + ministryLevel(MinistryType.EDUCATION) * 4 + (hasLaw(LawType.ANTI_CORRUPTION) ? 14 : 0)
                - corruption * 0.35 - Math.max(0, inflationPct - 5) * 1.4 - (debtServiceRatioPct() > 20 ? 10 : 0);

        double sec = 44.0 + Math.min(28.0, defensePower() * 0.14) + ministryLevel(MinistryType.DEFENSE) * 5
                + (hasLaw(LawType.MARTIAL_LAW) ? 14 : 0) + (hasLaw(LawType.CONTRACT_ARMY) ? 8 : 0)
                + (tradeRegime == 2 ? 6 : 0) - occupiedRegions() * 10.0;

        factions.put(FactionType.LIBERALS, clamp(lib, 5, 98));
        factions.put(FactionType.SOCIALISTS, clamp(soc, 5, 98));
        factions.put(FactionType.TECHNOCRATS, clamp(tech, 5, 98));
        factions.put(FactionType.SECUROCRATS, clamp(sec, 5, 98));
    }

    private void redistributeSeats() {
        double total = 0;
        for (FactionType f : FactionType.values()) total += Math.max(10.0, factionSupport(f));
        int assigned = 0;
        FactionType[] all = FactionType.values();
        for (int i = 0; i < all.length; i++) {
            int s = (i == all.length - 1)
                    ? Math.max(5, 100 - assigned)
                    : Math.max(8, (int) Math.round(100.0 * Math.max(10.0, factionSupport(all[i])) / total));
            parliamentSeats.put(all[i], s);
            assigned += s;
        }
    }

    private void recomputeDerived() {
        int laborForce = (int) Math.round(population * 0.62) + foreignSpecialists + (specialists / 4);
        int employed = Math.min(laborForce, totalJobs());
        unemploymentPct = laborForce <= 0 ? 0 : clamp((laborForce - employed) * 100.0 / laborForce, 1.5, 45.0);
        double minBoost = 1.0 + ministryLevel(MinistryType.ECONOMY) * 0.04 + ministryLevel(MinistryType.EDUCATION) * 0.04;
        gdp = Math.max(200.0, employed * 3.9 * tfp * minBoost);
        gdpPerCapita = gdp / Math.max(1, population);
        realWage = (gdp / Math.max(1, employed)) * 0.68 / cpi;
    }

    private void recordSnapshot(String note) {
        history.add(new CitySnapshot(month, population, treasury, debt, gdp, gdpPerCapita,
                inflationPct, cpi, unemploymentPct, trust, realWage, gini,
                taxRate, policyRate, tradeRegime, defensePower(), totalTroops(), totalVehicles(),
                fxRate, fxReserves, corruption, shadowEconomy, epidemicLevel, parliamentSupport(),
                terrorThreat, occupiedRegions(),
                priceEnergy, priceGrain, priceMetal, priceTech, note));
        if (history.size() > 240) history.remove(0);
    }

    public void log(LogEntry.Category category, String message, boolean urgent) {
        logs.addFirst(new LogEntry(month, category, message, urgent));
        while (logs.size() > 60) logs.removeLast();
        chronicle.addFirst(message);
        while (chronicle.size() > 18) chronicle.removeLast();
    }

    private void appendNote(String item) {
        if (pendingNote.isEmpty()) pendingNote = item;
        else pendingNote = pendingNote + " | " + item;
    }

    public String exportCsv() {
        StringBuilder sb = new StringBuilder(history.size() * 148 + 240);
        sb.append(CitySnapshot.csvHeader());
        for (CitySnapshot s : history) sb.append(s.toCsvRow());
        return sb.toString();
    }

    // -------------------------------------------------------- Persistence + Checkpoints

    public String toJson() {
        try {
            JSONObject root = buildCoreJson();
            JSONArray cpArr = new JSONArray();
            for (Checkpoint cp : checkpoints) {
                JSONObject o = new JSONObject();
                o.put("m", cp.month); o.put("l", cp.label); o.put("g", cp.gdp);
                o.put("t", cp.treasury); o.put("j", cp.snapshotJson);
                cpArr.put(o);
            }
            root.put("checkpoints", cpArr);
            return root.toString();
        } catch (JSONException | RuntimeException e) {
            return "";
        }
    }

    private String toJsonWithoutCheckpoints() {
        try { return buildCoreJson().toString(); } catch (JSONException | RuntimeException e) { return ""; }
    }

    private JSONObject buildCoreJson() throws JSONException {
        JSONObject root = new JSONObject();
        root.put("cityName", cityName);
        root.put("scenario", scenario.name());
        root.put("month", month);
        root.put("population", population);
        root.put("treasury", treasury);
        root.put("debt", debt);
        root.put("lastNetIncome", lastNetIncome);
        root.put("sovereignFund", sovereignFund);
        root.put("roadFundEnabled", roadFundEnabled);
        root.put("gdp", gdp);
        root.put("gdpPerCapita", gdpPerCapita);
        root.put("inflationPct", inflationPct);
        root.put("cpi", cpi);
        root.put("unemploymentPct", unemploymentPct);
        root.put("trust", trust);
        root.put("ecology", ecology);
        root.put("tfp", tfp);
        root.put("realWage", realWage);
        root.put("gini", gini);
        root.put("lifeExpectancy", lifeExpectancy);
        root.put("taxRate", taxRate);
        root.put("policyRate", policyRate);
        root.put("laggedRate1", laggedRate1);
        root.put("laggedRate2", laggedRate2);
        root.put("welfareLevel", welfareLevel);
        root.put("tradeRegime", tradeRegime);
        root.put("fxRate", fxRate);
        root.put("fxParity", fxParity);
        root.put("fxReserves", fxReserves);
        root.put("fxBuyLimitLeft", fxBuyLimitLeft);
        root.put("lastSavedEpochMs", System.currentTimeMillis());
        root.put("fixedFx", fixedFx);
        root.put("specialists", specialists);
        root.put("foreignSpecialists", foreignSpecialists);
        root.put("specialistsAbroad", specialistsAbroad);
        root.put("corruption", corruption);
        root.put("shadowEconomy", shadowEconomy);
        root.put("acbAttention", acbAttention);
        root.put("acbMoleActive", acbMoleActive);
        root.put("acbMoleDormant", acbMoleDormant);
        root.put("dossierIndex", dossierIndex);
        root.put("epidemicLevel", epidemicLevel);
        root.put("quarantine", quarantine);
        root.put("vaccination", vaccination);
        root.put("terrorThreat", terrorThreat);
        root.put("electionsWon", electionsWon);
        root.put("coalitionBonus", coalitionBonus);
        root.put("status", status.name());
        root.put("unionStage", union.unionStage);
        root.put("pactStage", union.pactStage);
        root.put("pmcName", pmc.nameIdx);
        root.put("pmcSize", pmc.squadSize);
        root.put("pmcContract", pmc.foreignContract);
        root.put("modFreeBuild", modFreeBuild);
        root.put("modAutoParliament", modAutoParliament);
        root.put("modGodMode", modGodMode);
        root.put("modNoWearTerror", modNoWearTerror);

        JSONArray lawArr = new JSONArray();
        for (LawType l : laws) lawArr.put(l.name());
        root.put("laws", lawArr);

        JSONArray regArr = new JSONArray();
        for (RegionState r : regions) {
            JSONObject o = new JSONObject();
            o.put("name", r.name); o.put("pop", r.population); o.put("tax", r.taxRate);
            o.put("zone", r.zone.name()); o.put("coastal", r.coastal); o.put("land", r.landArea);
            o.put("leased", r.leasedZones); o.put("wear", r.wear); o.put("gdp", r.gdp);
            o.put("trust", r.trust); o.put("occ", r.occupied); o.put("ts", r.terrorStrength);
            JSONObject rb = new JSONObject();
            for (BuildingType b : BuildingType.values()) if (r.count(b) > 0) rb.put(b.name(), r.count(b));
            o.put("b", rb);
            regArr.put(o);
        }
        root.put("regions", regArr);

        JSONObject mObj = new JSONObject();
        for (MinistryType m : MinistryType.values()) mObj.put(m.name(), ministryLevel(m));
        root.put("ministries", mObj);

        JSONObject milObj = new JSONObject();
        for (MilitaryBranch b : MilitaryBranch.values()) milObj.put(b.name(), troops(b));
        root.put("military", milObj);

        JSONObject vObj = new JSONObject();
        for (VehicleType v : VehicleType.values()) vObj.put(v.name(), vehicleCount(v));
        root.put("vehicles", vObj);

        JSONObject fObj = new JSONObject();
        for (NavalUnit u : NavalUnit.values()) fObj.put(u.name(), navalCount(u));
        root.put("fleet", fObj);

        JSONArray nArr = new JSONArray();
        for (NeighborCountry n : neighbors) {
            JSONObject o = new JSONObject();
            o.put("id", n.id); o.put("rel", n.relations); o.put("mp", n.militaryPower);
            o.put("reg", n.regionsCount); o.put("tar", n.tariffPct); o.put("cu", n.customsUnion);
            o.put("td", n.tradeDealDaysLeft); o.put("emb", n.embassy); o.put("cul", n.culturalCenter);
            o.put("aid", n.aidSharePct); o.put("war", n.atWar); o.put("cap", n.capitulated); o.put("wp", n.warProgress);
            nArr.put(o);
        }
        root.put("neighbors", nArr);
        root.put("history", encodeSnapshots(history));
        root.put("baseline", encodeSnapshots(baselineHistory));
        return root;
    }

    private JSONArray encodeSnapshots(List<CitySnapshot> list) throws JSONException {
        JSONArray arr = new JSONArray();
        for (CitySnapshot s : list) {
            JSONObject o = new JSONObject();
            o.put("m", s.month); o.put("p", s.population); o.put("tr", s.treasury);
            o.put("d", s.debt); o.put("g", s.gdp); o.put("gc", s.gdpPerCapita);
            o.put("inf", s.inflationPct); o.put("cpi", s.cpi); o.put("u", s.unemploymentPct);
            o.put("ts", s.trust); o.put("rw", s.realWage); o.put("gi", s.gini);
            o.put("tx", s.taxRate); o.put("pr", s.policyRate); o.put("rg", s.tradeRegime);
            o.put("dp", s.defensePower); o.put("tp", s.troops); o.put("vh", s.vehicles);
            o.put("fx", s.fxRate); o.put("res", s.fxReserves); o.put("cor", s.corruption);
            o.put("sh", s.shadowEconomy); o.put("ep", s.epidemicLevel); o.put("par", s.parliamentSupport);
            o.put("tt", s.terrorThreat); o.put("oc", s.occupiedRegions);
            o.put("pe", s.priceEnergy); o.put("pg", s.priceGrain); o.put("pm", s.priceMetal); o.put("pt", s.priceTech);
            o.put("n", s.note);
            arr.put(o);
        }
        return arr;
    }

    private void decodeSnapshots(JSONArray arr, List<CitySnapshot> out) throws JSONException {
        out.clear();
        if (arr == null) return;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            out.add(new CitySnapshot(
                    o.getInt("m"), o.getInt("p"), o.getDouble("tr"), o.getDouble("d"),
                    o.getDouble("g"), o.getDouble("gc"), o.getDouble("inf"), o.getDouble("cpi"),
                    o.getDouble("u"), o.getDouble("ts"), o.getDouble("rw"), o.getDouble("gi"),
                    o.getInt("tx"), o.getInt("pr"), o.getInt("rg"),
                    o.optDouble("dp", 0), o.optInt("tp", 0), o.optInt("vh", 0),
                    o.optDouble("fx", 40), o.optDouble("res", 420), o.optDouble("cor", 24),
                    o.optDouble("sh", 16), o.optDouble("ep", 0), o.optDouble("par", 50),
                    o.optDouble("tt", 14), o.optInt("oc", 0),
                    o.optDouble("pe", 1), o.optDouble("pg", 1), o.optDouble("pm", 1), o.optDouble("pt", 1),
                    o.optString("n", "")));
        }
    }

    public boolean loadJson(String raw) {
        if (raw == null || raw.isEmpty()) return false;
        try {
            JSONObject root = new JSONObject(raw);
            cityName = root.optString("cityName", "Poket State");
            // Saves made before the rename carry the old brand name; show the new one.
            if ("Poket City".equals(cityName)) cityName = "Poket State";
            scenario = ScenarioType.valueOf(root.optString("scenario", "SANDBOX"));
            month = root.getInt("month");
            population = root.getInt("population");
            treasury = root.getDouble("treasury");
            debt = root.getDouble("debt");
            lastNetIncome = root.optDouble("lastNetIncome", 0);
            sovereignFund = root.optDouble("sovereignFund", 350.0);
            roadFundEnabled = root.optBoolean("roadFundEnabled", false);
            gdp = root.getDouble("gdp");
            gdpPerCapita = root.optDouble("gdpPerCapita", gdp / Math.max(1, population));
            inflationPct = root.getDouble("inflationPct");
            cpi = root.getDouble("cpi");
            unemploymentPct = root.getDouble("unemploymentPct");
            trust = root.getDouble("trust");
            ecology = root.getDouble("ecology");
            tfp = root.getDouble("tfp");
            realWage = root.optDouble("realWage", 2.5);
            gini = root.optDouble("gini", 0.31);
            lifeExpectancy = root.optDouble("lifeExpectancy", 72.4);
            taxRate = root.getInt("taxRate");
            policyRate = root.getInt("policyRate");
            laggedRate1 = root.optInt("laggedRate1", policyRate);
            laggedRate2 = root.optInt("laggedRate2", policyRate);
            welfareLevel = root.getInt("welfareLevel");
            tradeRegime = root.getInt("tradeRegime");
            fxRate = root.optDouble("fxRate", 40.0);
            fxParity = root.optDouble("fxParity", 40.0);
            fxReserves = root.optDouble("fxReserves", 420.0);
            fxBuyLimitLeft = root.optDouble("fxBuyLimitLeft", 200.0);
            lastSavedEpochMs = root.optLong("lastSavedEpochMs", System.currentTimeMillis());
            fixedFx = root.optBoolean("fixedFx", false);
            specialists = root.optInt("specialists", 45);
            foreignSpecialists = root.optInt("foreignSpecialists", 0);
            specialistsAbroad = root.optInt("specialistsAbroad", 0);
            corruption = root.optDouble("corruption", 24.0);
            shadowEconomy = root.optDouble("shadowEconomy", 16.0);
            acbAttention = root.optDouble("acbAttention", 12.0);
            acbMoleActive = root.optBoolean("acbMoleActive", false);
            acbMoleDormant = root.optBoolean("acbMoleDormant", true);
            dossierIndex = root.optInt("dossierIndex", 0);
            epidemicLevel = root.optDouble("epidemicLevel", 0.0);
            quarantine = root.optBoolean("quarantine", false);
            vaccination = root.optBoolean("vaccination", false);
            terrorThreat = root.optDouble("terrorThreat", 14.0);
            electionsWon = root.optInt("electionsWon", 0);
            coalitionBonus = root.optInt("coalitionBonus", 0);
            status = GameStatus.valueOf(root.optString("status", "PLAYING"));
            union.unionStage = root.optInt("unionStage", 0);
            union.pactStage = root.optInt("pactStage", 0);
            pmc.nameIdx = root.optInt("pmcName", 0);
            pmc.squadSize = root.optInt("pmcSize", 0);
            pmc.foreignContract = root.optBoolean("pmcContract", false);
            modFreeBuild = root.optBoolean("modFreeBuild", false);
            modAutoParliament = root.optBoolean("modAutoParliament", false);
            modGodMode = root.optBoolean("modGodMode", false);
            modNoWearTerror = root.optBoolean("modNoWearTerror", false);

            laws.clear();
            JSONArray lawArr = root.optJSONArray("laws");
            if (lawArr != null) {
                for (int i = 0; i < lawArr.length(); i++) {
                    try { laws.add(LawType.valueOf(lawArr.getString(i))); } catch (IllegalArgumentException ignored) {}
                }
            }

            JSONArray regArr = root.optJSONArray("regions");
            if (regArr != null && regArr.length() > 0) {
                regions.clear();
                for (int i = 0; i < regArr.length(); i++) {
                    JSONObject o = regArr.getJSONObject(i);
                    EconomicZone z = EconomicZone.valueOf(o.optString("zone", "STANDARD"));
                    RegionState r = new RegionState(o.getString("name"), o.getInt("pop"), o.getInt("tax"), z,
                            o.optBoolean("coastal", false), o.optInt("land", 85));
                    r.leasedZones = o.optInt("leased", 0);
                    r.wear = o.optDouble("wear", 0);
                    r.gdp = o.optDouble("gdp", r.population * 2.5);
                    r.trust = o.optDouble("trust", trust);
                    r.occupied = o.optBoolean("occ", false);
                    r.terrorStrength = o.optDouble("ts", 0);
                    JSONObject rb = o.optJSONObject("b");
                    if (rb != null) {
                        for (BuildingType b : BuildingType.values()) r.buildings.put(b, rb.optInt(b.name(), 0));
                    }
                    regions.add(r);
                }
            }
            migrateNonCoastalPorts();

            JSONObject mObj = root.optJSONObject("ministries");
            if (mObj != null) for (MinistryType m : MinistryType.values()) ministries.put(m, mObj.optInt(m.name(), 0));
            JSONObject milObj = root.optJSONObject("military");
            if (milObj != null) for (MilitaryBranch b : MilitaryBranch.values()) military.put(b, milObj.optInt(b.name(), 0));
            JSONObject vObj = root.optJSONObject("vehicles");
            if (vObj != null) for (VehicleType v : VehicleType.values()) vehicles.put(v, vObj.optInt(v.name(), 0));
            JSONObject fObj = root.optJSONObject("fleet");
            if (fObj != null) for (NavalUnit u : NavalUnit.values()) fleet.put(u, fObj.optInt(u.name(), 0));

            JSONArray nArr = root.optJSONArray("neighbors");
            if (nArr != null) {
                for (int i = 0; i < nArr.length() && i < neighbors.size(); i++) {
                    JSONObject o = nArr.getJSONObject(i);
                    NeighborCountry n = neighbors.get(i);
                    n.relations = o.optInt("rel", n.relations);
                    n.militaryPower = o.optDouble("mp", n.militaryPower);
                    n.regionsCount = o.optInt("reg", n.regionsCount);
                    n.tariffPct = o.optInt("tar", n.tariffPct);
                    n.customsUnion = o.optBoolean("cu", false);
                    n.tradeDealDaysLeft = o.optInt("td", 0);
                    n.embassy = o.optBoolean("emb", false);
                    n.culturalCenter = o.optBoolean("cul", false);
                    n.aidSharePct = o.optInt("aid", 25);
                    n.atWar = o.optBoolean("war", false);
                    n.capitulated = o.optBoolean("cap", false);
                    n.warProgress = o.optInt("wp", 0);
                }
            }

            decodeSnapshots(root.optJSONArray("history"), history);
            decodeSnapshots(root.optJSONArray("baseline"), baselineHistory);

            JSONArray cpArr = root.optJSONArray("checkpoints");
            if (cpArr != null) {
                checkpoints.clear();
                for (int i = 0; i < cpArr.length(); i++) {
                    JSONObject o = cpArr.getJSONObject(i);
                    checkpoints.add(new Checkpoint(o.getInt("m"), o.getString("l"), o.getDouble("g"), o.getDouble("t"), o.getString("j")));
                }
            }

            regenerateOfflineLimits();
            recomputeFactions();
            if (status == GameStatus.PLAYING) currentEvent = CityEvent.pick(this, rng);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // --------------------------------------------------- Mod Menu Helpers

    public void modAddCash(double amount) {
        treasury += amount;
        log(LogEntry.Category.ACTION, Strings.f("МОД: +%s ₴ у скарбницю.", amount >= 1e6 ? "1M" : String.valueOf((long) amount)), false);
    }

    public void modClearDebt() {
        debt = 0.0;
        log(LogEntry.Category.ACTION, Strings.t("МОД: держборг повністю списано (0 ₴)."), false);
    }

    public void modAddPopAndSpecialists(int popDelta, int specDelta) {
        if (!regions.isEmpty()) regions.get(0).population += popDelta;
        population += popDelta;
        specialists += specDelta;
        recomputeDerived();
        log(LogEntry.Category.ACTION, Strings.f("МОД: +%s населення та +%s спеціалістів.", popDelta, specDelta), false);
    }

    public void modAddReserves(double millions) {
        fxReserves += millions;
        fxBuyLimitLeft = 350.0;
        log(LogEntry.Category.ACTION, Strings.f("МОД: +$%.0fM ЗВР НБК.", millions), false);
    }

    public void modMultiplyGdp(double factor) {
        tfp *= factor;
        recomputeDerived();
        log(LogEntry.Category.ECONOMY, Strings.f("МОД: продуктивність ×%.1f → ВВП %.0f ₴.", factor, gdp), false);
    }

    public void modStabilizeAll() {
        trust = 100.0;
        corruption = 2.0;
        shadowEconomy = 3.0;
        inflationPct = 2.0;
        moneyImpulse = 0.0;
        supplyShock = 0.0;
        panicImpulse = 0.0;
        epidemicLevel = 0.0;
        terrorThreat = 0.0;
        acbAttention = 0.0;
        ecology = 96.0;
        fxRate = 40.0;
        fxParity = 40.0;
        for (RegionState r : regions) r.wear = 0.0;
        recomputeFactions();
        log(LogEntry.Category.ACTION, Strings.t("МОД: стабілізовано всі показники (Довіра 100%, Корупція 2%, Інфляція 2%)."), false);
    }

    public void modPeaceAndLiberateAll() {
        for (RegionState r : regions) { r.occupied = false; r.terrorStrength = 0; }
        for (NeighborCountry n : neighbors) { n.atWar = false; n.seaBlockaded = false; if (n.relations < 25) n.relations = 25; }
        terrorThreat = 0.0;
        if (status == GameStatus.OVERRUN) status = GameStatus.PLAYING;
        log(LogEntry.Category.SECURITY, Strings.t("МОД: звільнено всі регіони й укладено мир з усіма сусідами."), false);
    }

    public void modJoinUnionAndPact() {
        union.unionStage = UnionState.UNION_STAGES.length;
        union.pactStage = UnionState.PACT_STAGES.length;
        union.unionPresidency = true;
        log(LogEntry.Category.PARLIAMENT, Strings.t("МОД: миттєвий вступ до Унії (з головуванням) та Оборонного Пакту!"), false);
    }

    public void modMaxMinistries() {
        for (MinistryType m : MinistryType.values()) ministries.put(m, MinistryType.MAX_LEVEL);
        recomputeFactions();
        recomputeDerived();
        log(LogEntry.Category.PARLIAMENT, Strings.t("МОД: усі 7 міністерств підвищено до Рів. 5/5."), false);
    }

    public void modMaxArmyAndNavy() {
        for (MilitaryBranch b : MilitaryBranch.values()) military.put(b, troops(b) + b.batchSize * 4);
        for (RegionState r : regions) if (r.coastal) { r.addBuilding(BuildingType.NAVAL_BASE, 1); break; }
        fleet.put(NavalUnit.COAST_GUARD, navalCount(NavalUnit.COAST_GUARD) + 2);
        fleet.put(NavalUnit.FRIGATE, navalCount(NavalUnit.FRIGATE) + 2);
        fleet.put(NavalUnit.NUCLEAR_SUB, navalCount(NavalUnit.NUCLEAR_SUB) + 1);
        fleet.put(NavalUnit.AIRCRAFT_CARRIER, navalCount(NavalUnit.AIRCRAFT_CARRIER) + 1);
        vehicles.put(VehicleType.TANK, vehicleCount(VehicleType.TANK) + 4);
        vehicles.put(VehicleType.DRONE_AA, vehicleCount(VehicleType.DRONE_AA) + 4);
        recomputeFactions();
        log(LogEntry.Category.SECURITY, Strings.f("МОД: розгорнуто повний склад Армії, ЦСП та Флоту (Міць %.0f)!", defensePower()), false);
    }

    public void modFastForward(int months) {
        for (int i = 0; i < months && status == GameStatus.PLAYING; i++) {
            currentEvent = null;
            nextMonth();
        }
        log(LogEntry.Category.ACTION, Strings.f("МОД: перемотано +%s міс. (зараз М%s).", months, month), false);
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
