package com.ccs.poketstate.i18n;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Minimal gettext-style localizer.
 *
 * The Ukrainian source text is the message key. English translations live in the
 * {@code EN} dictionaries of the classes in this package. In Ukrainian mode {@link #t}
 * returns the key unchanged; in English mode it returns the translation, falling back
 * to the Ukrainian key when no translation exists.
 *
 * Use {@link #f} for printf-style messages: the key (and its translation) holds the
 * format specifiers, e.g. {@code Strings.f("Знос об'єктів %d%%", wear)}.
 */
public final class Strings {
    private static final List<Map<String, String>> EN_DICTS = Arrays.asList(
            UiText.EN, EngineText.EN, EventText.EN, DataText.EN);

    private static volatile boolean english = false;

    private Strings() {}

    /** Switches between Ukrainian (default, source language) and English. */
    public static void setEnglish(boolean value) {
        english = value;
    }

    public static boolean isEnglish() {
        return english;
    }

    /** Translates a message key. */
    public static String t(String key) {
        if (key == null || !english) return key;
        for (Map<String, String> dict : EN_DICTS) {
            String value = dict.get(key);
            if (value != null) return value;
        }
        return key;
    }

    /** Translates a format key and applies the arguments. */
    public static String f(String key, Object... args) {
        return String.format(Locale.ROOT, t(key), args);
    }

    /** Builds a dictionary from {ukrainian, english} pairs. */
    static Map<String, String> dict(String[][] pairs) {
        java.util.HashMap<String, String> map = new java.util.HashMap<>();
        for (String[] pair : pairs) {
            map.put(pair[0], pair[1]);
        }
        return java.util.Collections.unmodifiableMap(map);
    }
}
