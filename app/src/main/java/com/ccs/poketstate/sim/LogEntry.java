package com.ccs.poketstate.sim;

/** Structured in-app log entry replacing transient OS toasts. */
public final class LogEntry {
    public enum Category {
        ACTION("ДІЯ"),
        ECONOMY("ЕКОНОМІКА"),
        PARLIAMENT("ПАРЛАМЕНТ"),
        SECURITY("ОБОРОНА"),
        CRISIS("КРИЗА");

        public final String tag;
        Category(String tag) { this.tag = tag; }
    }

    public final int month;
    public final Category category;
    public final String message;
    public final boolean urgent;

    public LogEntry(int month, Category category, String message, boolean urgent) {
        this.month = month;
        this.category = category;
        this.message = message;
        this.urgent = urgent;
    }
}
