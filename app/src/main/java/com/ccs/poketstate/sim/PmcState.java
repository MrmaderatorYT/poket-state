package com.ccs.poketstate.sim;

/** Private Military Company (ПВК): custom name, emblem, squad size, and foreign contracts. */
public final class PmcState {
    public static final String[] NAMES = {"Varta-East", "Phoenix", "Omega Corp", "Shield"};
    public static final String[] EMBLEMS = {"[Sh]", "[Ph]", "[Ω]", "[V]"};

    public int nameIdx = 0;
    public int squadSize = 0;
    public boolean foreignContract = false;
    public int experience = 0;

    public String displayName() { return EMBLEMS[nameIdx % EMBLEMS.length] + " " + NAMES[nameIdx % NAMES.length]; }
    public double combatPower() { return squadSize * (1.7 + Math.min(0.8, experience * 0.02)); }
    public double monthlyContractIncome() { return foreignContract && squadSize > 0 ? squadSize * 7.5 : 0; }
    public double monthlyUpkeep() { return squadSize * 2.4; }
}
