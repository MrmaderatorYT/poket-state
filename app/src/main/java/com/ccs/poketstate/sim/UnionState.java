package com.ccs.poketstate.sim;

/** Supranational integration state: Economic Union (Унія) & Defense Pact (Пакт). */
public final class UnionState {
    public static final String[] UNION_STAGES = {
            "Candidate application", "Law harmonization", "Single market", "Full membership of the Union"
    };
    public static final int[] UNION_COSTS = {1400, 2200, 3200, 4500};

    public static final String[] PACT_STAGES = {
            "Partnership for peace", "Army and air defense standards", "Collective defense of the Pact"
    };
    public static final int[] PACT_COSTS = {1500, 2600, 3800};

    public int unionStage = 0;   // 0..4 (4 = member)
    public int pactStage = 0;    // 0..3 (3 = member)
    public boolean unionPresidency = false;
    public int unionCouncilVotesWon = 0;
    public int lastTrancheMonth = 0;

    public boolean isUnionMember() { return unionStage >= UNION_STAGES.length; }
    public boolean isPactMember() { return pactStage >= PACT_STAGES.length; }

    public int nextUnionCost() { return isUnionMember() ? 0 : UNION_COSTS[unionStage]; }
    public int nextPactCost() { return isPactMember() ? 0 : PACT_COSTS[pactStage]; }
}
