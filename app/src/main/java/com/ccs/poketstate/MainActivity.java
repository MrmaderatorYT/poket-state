package com.ccs.poketstate;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.ccs.poketstate.i18n.Strings;
import com.ccs.poketstate.sim.BuildingType;
import com.ccs.poketstate.sim.CityEngine;
import com.ccs.poketstate.sim.CityEvent;
import com.ccs.poketstate.sim.CitySnapshot;
import com.ccs.poketstate.sim.EconomicZone;
import com.ccs.poketstate.sim.FactionType;
import com.ccs.poketstate.sim.LawType;
import com.ccs.poketstate.sim.LogEntry;
import com.ccs.poketstate.sim.MilitaryBranch;
import com.ccs.poketstate.sim.MinistryType;
import com.ccs.poketstate.sim.NavalUnit;
import com.ccs.poketstate.sim.NeighborCountry;
import com.ccs.poketstate.sim.RegionState;
import com.ccs.poketstate.sim.ScenarioType;
import com.ccs.poketstate.sim.UnionState;
import com.ccs.poketstate.sim.VehicleType;
import com.ccs.poketstate.ui.ChartView;
import com.ccs.poketstate.ui.CsvExporter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Poket State v2 — minimalist obsidian grand macroeconomic & geopolitical text simulator.
 * Zero OS Toasts; auto-saves continuously with checkpoints & cross-device JSON backup.
 */
public final class MainActivity extends AppCompatActivity {
    private static final String PREFS = "poketworld_text_save";
    private static final String KEY_STATE = "city_json_v5";
    private static final String KEY_BACKUP = "city_json_backup_v5";

    private static final String KEY_LANG = "pref_lang";     // "auto" | "uk" | "en"
    private static final String KEY_THEME = "pref_theme";   // "dark" | "light"

    // Palette is switched by applyPalette() before any view is built.
    private static int BG = Color.parseColor("#0B0C0E");
    private static int SURFACE = Color.parseColor("#131519");
    private static int ELEVATED = Color.parseColor("#1B1E24");
    private static int HAIRLINE = Color.parseColor("#242830");
    private static int INK = Color.parseColor("#F4F5F7");
    private static int INK_SUB = Color.parseColor("#8A909E");
    private static int INK_DIM = Color.parseColor("#575D6B");
    private static int POSITIVE = Color.parseColor("#4ADE80");
    private static int NEGATIVE = Color.parseColor("#F87171");
    private static int WARN = Color.parseColor("#FBBF24");
    private static boolean lightTheme = false;
    private static boolean pendingSettings = false;  // reopen settings after a language/theme recreate

    private CityEngine engine;
    private int activeTab = 0;       // 0=Місто, 1=Регіони, 2=Держава, 3=Світ, 4=Економіка
    private int regionSubTab = 0;    // 0=Області, 1=Кадри, 2=Техніка
    private int regionSort = 0;      // 0=ВВП, 1=Насел, 2=Податок, 3=Знос, 4=Безпека
    private int stateSubTab = 0;     // 0=Парламент, 1=Мін-ва, 2=Армія, 3=Флот
    private int worldSubTab = 0;     // 0=Дипломатія, 1=Унія/Пакт, 2=Партнери
    private int econSubTab = 0;      // 0=Графіки, 1=Фінанси
    private int activeChart = 0;
    private int customRepayAmount = 500;
    private boolean compareAb = false;
    private boolean logOpen = false;
    private boolean modOpen = false;
    private boolean settingsOpen = false;
    private LogEntry.Category logFilter = null;

    private TextView headerTitle, headerSub, headerDate, headerModBtn;
    private TextView kpiPop, kpiCash, kpiCashDelta, kpiTrust, kpiInfl;
    private View tickerDot;
    private TextView tickerTag, tickerText, tickerBtn;
    private LinearLayout contentContainer;
    private ScrollView contentScroll;
    private final TextView[] navLabels = new TextView[5];
    private final View[] navIndicators = new View[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        applyLanguagePref(prefs);
        lightTheme = "light".equals(prefs.getString(KEY_THEME, "dark"));
        applyPalette(lightTheme);
        if (pendingSettings) {
            settingsOpen = true;
            pendingSettings = false;
        }
        engine = new CityEngine(System.currentTimeMillis());
        if (!engine.loadJson(prefs.getString(KEY_STATE, null))) {
            engine.loadJson(prefs.getString(KEY_BACKUP, null));
        }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        root.addView(buildHeader());
        root.addView(hairline());
        root.addView(buildStatusTicker());
        root.addView(hairline());

        contentScroll = new ScrollView(this);
        contentScroll.setFillViewport(true);
        contentScroll.setVerticalScrollBarEnabled(false);
        contentContainer = new LinearLayout(this);
        contentContainer.setOrientation(LinearLayout.VERTICAL);
        contentContainer.setPadding(dp(18), dp(14), dp(18), dp(24));
        contentScroll.addView(contentContainer, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(contentScroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        root.addView(hairline());
        root.addView(buildBottomNav());

        setContentView(root);
        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(getWindow(), root);
        insetsController.setAppearanceLightStatusBars(lightTheme);
        insetsController.setAppearanceLightNavigationBars(lightTheme);
        renderAll();
    }

    @Override
    protected void onPause() {
        super.onPause();
        persist();
    }

    private void persist() {
        String json = engine.toJson();
        if (json == null || json.isEmpty()) return;
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        String prev = prefs.getString(KEY_STATE, null);
        SharedPreferences.Editor ed = prefs.edit().putString(KEY_STATE, json);
        if (prev != null && !prev.isEmpty()) ed.putString(KEY_BACKUP, prev);
        ed.apply();
    }

    // ------------------------------------------------------------- Top Header

    private View buildHeader() {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setBackgroundColor(BG);
        wrap.setPadding(dp(18), dp(12), dp(18), dp(11));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout titleCol = new LinearLayout(this);
        titleCol.setOrientation(LinearLayout.VERTICAL);
        headerTitle = text(Strings.t("Poket State"), 19.5f, INK, true);
        headerSub = text(Strings.t("Містечко"), 11.5f, INK_SUB, false);
        titleCol.addView(headerTitle);
        titleCol.addView(headerSub);
        top.addView(titleCol, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        headerDate = text(Strings.t("Р1 · М1"), 12, INK_SUB, true);
        headerDate.setFontFeatureSettings("tnum");
        top.addView(headerDate);

        headerModBtn = text(Strings.t("Мод"), 11.5f, WARN, true);
        headerModBtn.setPadding(dp(10), dp(5), 0, dp(5));
        headerModBtn.setOnClickListener(v -> {
            logOpen = false;
            settingsOpen = false;
            modOpen = !modOpen;
            contentScroll.scrollTo(0, 0);
            renderAll();
        });
        top.addView(headerModBtn);

        TextView cpBtn = text(Strings.t("Сейв"), 11.5f, INK_DIM, false);
        cpBtn.setPadding(dp(10), dp(5), 0, dp(5));
        cpBtn.setOnClickListener(v -> showCheckpointsDialog());
        top.addView(cpBtn);

        TextView scenBtn = text(Strings.t("Сценарії"), 11.5f, INK_DIM, false);
        scenBtn.setPadding(dp(10), dp(5), 0, dp(5));
        scenBtn.setOnClickListener(v -> showScenarioPicker());
        top.addView(scenBtn);

        TextView setBtn = text("⚙", 14, INK_DIM, false);
        setBtn.setPadding(dp(10), dp(3), 0, dp(4));
        setBtn.setOnClickListener(v -> {
            logOpen = false;
            modOpen = false;
            settingsOpen = true;
            contentScroll.scrollTo(0, 0);
            renderAll();
        });
        top.addView(setBtn);

        wrap.addView(top);

        LinearLayout kpis = new LinearLayout(this);
        kpis.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams klp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        klp.topMargin = dp(11);

        kpiPop = addKpiColumn(kpis, Strings.t("НАСЕЛЕННЯ"));
        addVerticalHairline(kpis);

        LinearLayout cashCol = new LinearLayout(this);
        cashCol.setOrientation(LinearLayout.VERTICAL);
        cashCol.addView(microLabel(Strings.t("СКАРБНИЦЯ")));
        LinearLayout cashLine = new LinearLayout(this);
        cashLine.setOrientation(LinearLayout.HORIZONTAL);
        cashLine.setGravity(Gravity.BOTTOM);
        kpiCash = text("-", 15, INK, true);
        kpiCash.setFontFeatureSettings("tnum");
        kpiCashDelta = text("", 10.5f, POSITIVE, false);
        kpiCashDelta.setFontFeatureSettings("tnum");
        kpiCashDelta.setPadding(dp(4), 0, 0, dp(1));
        cashLine.addView(kpiCash);
        cashLine.addView(kpiCashDelta);
        addTop(cashCol, cashLine, 2);
        kpis.addView(cashCol, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.35f));

        addVerticalHairline(kpis);
        kpiTrust = addKpiColumn(kpis, Strings.t("ДОВІРА"));
        addVerticalHairline(kpis);
        kpiInfl = addKpiColumn(kpis, Strings.t("ІНФЛЯЦІЯ"));

        wrap.addView(kpis, klp);
        return wrap;
    }

    private View buildStatusTicker() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setBackgroundColor(SURFACE);
        bar.setPadding(dp(16), dp(7), dp(14), dp(7));

        tickerDot = new View(this);
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(dp(6), dp(6));
        dlp.setMarginEnd(dp(8));
        bar.addView(tickerDot, dlp);

        tickerTag = text(Strings.t("ДІЯ"), 9.5f, INK_DIM, true);
        tickerTag.setLetterSpacing(0.08f);
        tickerTag.setPadding(0, 0, dp(8), 0);
        bar.addView(tickerTag);

        tickerText = text("", 12, INK_SUB, false);
        tickerText.setSingleLine(true);
        tickerText.setEllipsize(TextUtils.TruncateAt.END);
        bar.addView(tickerText, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        tickerBtn = text(Strings.t("Лог"), 11, INK, true);
        tickerBtn.setBackground(roundRect(ELEVATED, HAIRLINE, 6, 1));
        tickerBtn.setPadding(dp(8), dp(4), dp(8), dp(4));
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        blp.setMarginStart(dp(8));
        bar.addView(tickerBtn, blp);

        bar.setOnClickListener(v -> {
            modOpen = false;
            settingsOpen = false;
            logOpen = !logOpen;
            contentScroll.scrollTo(0, 0);
            renderAll();
        });
        return bar;
    }

    private TextView addKpiColumn(LinearLayout parent, String caption) {
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.addView(microLabel(caption));
        TextView val = text("-", 15, INK, true);
        val.setFontFeatureSettings("tnum");
        addTop(col, val, 2);
        parent.addView(col, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        return val;
    }

    private void addVerticalHairline(LinearLayout parent) {
        View v = new View(this);
        v.setBackgroundColor(HAIRLINE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(1), dp(22));
        lp.gravity = Gravity.CENTER_VERTICAL;
        lp.setMargins(dp(7), 0, dp(7), 0);
        parent.addView(v, lp);
    }

    // ------------------------------------------------------------- Bottom Nav

    private View buildBottomNav() {
        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setBackgroundColor(BG);
        nav.setPadding(dp(8), dp(6), dp(8), dp(8));

        String[] names = {Strings.t("Місто"), Strings.t("Регіони"), Strings.t("Держава"), Strings.t("Світ"), Strings.t("Економіка")};
        for (int i = 0; i < names.length; i++) {
            final int index = i;
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);
            item.setPadding(dp(2), dp(7), dp(2), dp(5));

            TextView label = text(names[i], 12, INK_DIM, false);
            item.addView(label);

            View bar = new View(this);
            LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(dp(16), dp(2));
            blp.gravity = Gravity.CENTER_HORIZONTAL;
            blp.topMargin = dp(4);
            item.addView(bar, blp);

            item.setOnClickListener(v -> {
                logOpen = false;
                modOpen = false;
                settingsOpen = false;
                activeTab = index;
                contentScroll.scrollTo(0, 0);
                renderAll();
            });

            nav.addView(item, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            navLabels[i] = label;
            navIndicators[i] = bar;
        }
        return nav;
    }

    // --------------------------------------------------------------- Rendering

    private void renderAll() {
        int year = 1 + (engine.month - 1) / 12;
        int mInYear = 1 + (engine.month - 1) % 12;
        headerTitle.setText(engine.cityName);
        String sub = Strings.f("%s · %.1f ₴/$ · Сила %.0f", Strings.t(engine.cityRank()), engine.fxRate, engine.statePowerRating());
        if (engine.lastNetIncome < 25) sub = sub + Strings.t(" · Скарбниця слабка");
        headerSub.setText(sub);
        headerSub.setTextColor((engine.occupiedRegions() > 0 || engine.activeWarsCount() > 0) ? NEGATIVE
                : (engine.lastNetIncome < 25 ? WARN : INK_SUB));
        headerDate.setText(Strings.f("Р%d · М%d", year, mInYear));

        kpiPop.setText(String.valueOf(engine.population));
        kpiCash.setText(compactNum(engine.treasury));
        kpiCash.setTextColor(engine.treasury < 0 ? NEGATIVE : INK);
        String sign = engine.lastNetIncome >= 0 ? "+" : "";
        kpiCashDelta.setText(sign + compactNum(engine.lastNetIncome));
        kpiCashDelta.setTextColor(engine.lastNetIncome >= 0 ? POSITIVE : NEGATIVE);

        kpiTrust.setText(String.format(Locale.ROOT, "%.0f%%", engine.trust));
        kpiTrust.setTextColor(engine.trust >= 55 ? INK : (engine.trust >= 35 ? WARN : NEGATIVE));

        kpiInfl.setText(String.format(Locale.ROOT, "%.1f%%", engine.inflationPct));
        kpiInfl.setTextColor(engine.inflationPct <= 6.0 ? INK : (engine.inflationPct <= 10.0 ? WARN : NEGATIVE));

        LogEntry latest = engine.latestLog();
        int dotColor = latest.urgent ? NEGATIVE : (latest.category == LogEntry.Category.PARLIAMENT ? WARN : POSITIVE);
        tickerDot.setBackground(roundRect(dotColor, Color.TRANSPARENT, 6, 0));
        tickerTag.setText(Strings.t(latest.category.tag));
        tickerTag.setTextColor(latest.urgent ? NEGATIVE : INK_DIM);
        tickerText.setText(Strings.t(latest.message));
        tickerText.setTextColor(latest.urgent ? INK : INK_SUB);
        tickerBtn.setText(logOpen ? Strings.t("Закрити") : Strings.f("Лог %d", engine.logs.size()));

        boolean anyMod = engine.modFreeBuild || engine.modAutoParliament || engine.modGodMode || engine.modNoWearTerror;
        headerModBtn.setText(modOpen ? Strings.t("Закр.") : (anyMod ? Strings.t("Мод ●") : Strings.t("Мод")));
        headerModBtn.setTextColor(modOpen ? INK : (anyMod ? POSITIVE : WARN));

        for (int i = 0; i < navLabels.length; i++) {
            boolean sel = !logOpen && !modOpen && !settingsOpen && i == activeTab;
            navLabels[i].setTextColor(sel ? INK : INK_DIM);
            navLabels[i].setTypeface(null, sel ? Typeface.BOLD : Typeface.NORMAL);
            navIndicators[i].setBackground(roundRect(sel ? INK : Color.TRANSPARENT, Color.TRANSPARENT, 2, 0));
        }

        contentContainer.removeAllViews();
        if (logOpen) {
            renderLogPanel();
        } else if (modOpen) {
            renderModMenuPanel();
        } else if (settingsOpen) {
            renderSettingsPanel();
        } else {
            switch (activeTab) {
                case 0: renderCityTab(); break;
                case 1: renderRegionsTab(); break;
                case 2: renderStateTab(); break;
                case 3: renderWorldTab(); break;
                case 4: renderEconomyTab(); break;
            }
        }
        persist();
    }

    // --------------------------------------------------- Settings Panel

    private void applyLanguagePref(SharedPreferences prefs) {
        String lang = prefs.getString(KEY_LANG, "auto");
        boolean english = "en".equals(lang)
                || ("auto".equals(lang) && !"uk".equals(Locale.getDefault().getLanguage()));
        Strings.setEnglish(english);
    }

    private void applyPalette(boolean light) {
        if (light) {
            BG = Color.parseColor("#F5F6F8");
            SURFACE = Color.parseColor("#FFFFFF");
            ELEVATED = Color.parseColor("#ECEEF2");
            HAIRLINE = Color.parseColor("#D9DDE3");
            INK = Color.parseColor("#111318");
            INK_SUB = Color.parseColor("#5B6170");
            INK_DIM = Color.parseColor("#9099A8");
            POSITIVE = Color.parseColor("#16A34A");
            NEGATIVE = Color.parseColor("#DC2626");
            WARN = Color.parseColor("#B45309");
            ChartView.setPalette(INK, Color.parseColor("#E3E6EB"), INK_SUB);
        } else {
            BG = Color.parseColor("#0B0C0E");
            SURFACE = Color.parseColor("#131519");
            ELEVATED = Color.parseColor("#1B1E24");
            HAIRLINE = Color.parseColor("#242830");
            INK = Color.parseColor("#F4F5F7");
            INK_SUB = Color.parseColor("#8A909E");
            INK_DIM = Color.parseColor("#575D6B");
            POSITIVE = Color.parseColor("#4ADE80");
            NEGATIVE = Color.parseColor("#F87171");
            WARN = Color.parseColor("#FBBF24");
            ChartView.setPalette(INK, Color.parseColor("#20232A"), INK_SUB);
        }
    }

    private void setPref(String key, String value) {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(key, value).apply();
        pendingSettings = true;
        recreate();
    }

    private void renderSettingsPanel() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        String lang = prefs.getString(KEY_LANG, "auto");
        String theme = prefs.getString(KEY_THEME, "dark");

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(text(Strings.t("Налаштування"), 18, INK, true), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        top.addView(outlineButton(Strings.t("Закрити"), INK, () -> { settingsOpen = false; renderAll(); }));
        contentContainer.addView(top);

        addTop(contentContainer, microLabel(Strings.t("Мова")), 16);
        LinearLayout langRow = new LinearLayout(this);
        langRow.setOrientation(LinearLayout.HORIZONTAL);
        addChoice(langRow, Strings.t("Системна"), "auto".equals(lang), () -> setPref(KEY_LANG, "auto"));
        addChoice(langRow, "Українська", "uk".equals(lang), () -> setPref(KEY_LANG, "uk"));
        addChoice(langRow, "English", "en".equals(lang), () -> setPref(KEY_LANG, "en"));
        addTop(contentContainer, langRow, 8);

        addTop(contentContainer, microLabel(Strings.t("Тема")), 16);
        LinearLayout themeRow = new LinearLayout(this);
        themeRow.setOrientation(LinearLayout.HORIZONTAL);
        addChoice(themeRow, Strings.t("Темна"), !"light".equals(theme), () -> setPref(KEY_THEME, "dark"));
        addChoice(themeRow, Strings.t("Світла"), "light".equals(theme), () -> setPref(KEY_THEME, "light"));
        addTop(contentContainer, themeRow, 8);

        addTop(contentContainer, text(Strings.t("Зміни мови та теми застосовуються одразу."), 11.5f, INK_DIM, false), 10);

        addTop(contentContainer, microLabel(Strings.t("Застосунок")), 18);
        String versionName = "-";
        long versionCode = 0;
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = pi.versionName;
            versionCode = pi.getLongVersionCode();
        } catch (PackageManager.NameNotFoundException ignored) {
            // Own package is always visible to itself; keep placeholders if it ever is not.
        }
        addSettingRow(Strings.t("Версія"), versionName);
        addSettingRow(Strings.t("Код версії"), String.valueOf(versionCode));
    }

    private void addChoice(LinearLayout parent, String label, boolean selected, Runnable action) {
        TextView btn = text(label, 13, selected ? BG : INK, true);
        btn.setGravity(Gravity.CENTER);
        btn.setBackground(roundRect(selected ? INK : ELEVATED, selected ? Color.TRANSPARENT : HAIRLINE, 8, selected ? 0 : 1));
        btn.setPadding(dp(8), dp(10), dp(8), dp(10));
        btn.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMarginEnd(dp(6));
        parent.addView(btn, lp);
    }

    private void addSettingRow(String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(12), 0, dp(12));
        row.addView(text(label, 14, INK, false), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(text(value, 14, INK_SUB, true));
        contentContainer.addView(row);
        contentContainer.addView(hairline());
    }

    // --------------------------------------------------- Mod Menu Panel

    private void renderModMenuPanel() {
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout titleCol = new LinearLayout(this);
        titleCol.setOrientation(LinearLayout.VERTICAL);
        titleCol.addView(text(Strings.t("Мод-меню"), 18, INK, true));
        titleCol.addView(text(Strings.t("Пісочниця, тестування та режим Бога"), 12, INK_SUB, false));
        top.addView(titleCol, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        top.addView(outlineButton(Strings.t("Закрити"), INK, () -> { modOpen = false; renderAll(); }));
        contentContainer.addView(top);

        // 1. God Mode Switches
        addTop(contentContainer, microLabel(Strings.t("ПЕРЕМИКАЧІ РЕЖИМУ БОГА")), 16);
        addModToggle(Strings.t("Безкоштовне будівництво й покупки"), Strings.t("Усі будівлі, міністерства, армія й флот — 0 ₴"),
                engine.modFreeBuild, () -> {
                    engine.modFreeBuild = !engine.modFreeBuild;
                    engine.log(LogEntry.Category.ACTION, Strings.f("МОД: безкоштовні покупки %s", (engine.modFreeBuild ? Strings.t("ВКЛ") : Strings.t("ВИМК"))), false);
                    renderAll();
                });
        contentContainer.addView(hairline());
        addModToggle(Strings.t("100% голосів у Парламенті"), Strings.t("Будь-який закон і міністр ухвалюється миттєво"),
                engine.modAutoParliament, () -> {
                    engine.modAutoParliament = !engine.modAutoParliament;
                    engine.log(LogEntry.Category.PARLIAMENT, Strings.f("МОД: 100%% у Парламенті %s", (engine.modAutoParliament ? Strings.t("ВКЛ") : Strings.t("ВИМК"))), false);
                    renderAll();
                });
        contentContainer.addView(hairline());
        addModToggle(Strings.t("Безсмертя (без поразки)"), Strings.t("Вимкнути імпічмент, дефолт, поразку на виборах"),
                engine.modGodMode, () -> {
                    engine.modGodMode = !engine.modGodMode;
                    engine.log(LogEntry.Category.ACTION, Strings.f("МОД: безсмертя %s", (engine.modGodMode ? Strings.t("ВКЛ") : Strings.t("ВИМК"))), false);
                    renderAll();
                });
        contentContainer.addView(hairline());
        addModToggle(Strings.t("Без зносу та терору"), Strings.t("Знос об'єктів 0% · Терористична загроза 0%"),
                engine.modNoWearTerror, () -> {
                    engine.modNoWearTerror = !engine.modNoWearTerror;
                    if (engine.modNoWearTerror) {
                        engine.terrorThreat = 0;
                        for (RegionState r : engine.regions) r.wear = 0;
                    }
                    engine.log(LogEntry.Category.ACTION, Strings.f("МОД: без зносу і терору %s", (engine.modNoWearTerror ? Strings.t("ВКЛ") : Strings.t("ВИМК"))), false);
                    renderAll();
                });

        // 2. Instant Resources & Economy
        addTop(contentContainer, microLabel(Strings.t("РЕСУРСИ ТА ЕКОНОМІКА")), 18);
        addModPair("+10 000 ₴", () -> { engine.modAddCash(10_000); renderAll(); },
                "+1 000 000 ₴", () -> { engine.modAddCash(1_000_000); renderAll(); });
        addModPair(Strings.t("Списати весь борг"), () -> { engine.modClearDebt(); renderAll(); },
                Strings.t("+$500M ЗВР"), () -> { engine.modAddReserves(500); renderAll(); });
        addModPair(Strings.t("+500 жит. / +100 спец."), () -> { engine.modAddPopAndSpecialists(500, 100); renderAll(); },
                Strings.t("Вибух ВВП ×2.5"), () -> { engine.modMultiplyGdp(2.5); renderAll(); });
        addModPair(Strings.t("Мега-вибух ВВП ×10"), () -> { engine.modMultiplyGdp(10.0); renderAll(); },
                Strings.t("Стабілізувати все"), () -> { engine.modStabilizeAll(); renderAll(); });

        // 3. State, Army, Navy, Diplomacy
        addTop(contentContainer, microLabel(Strings.t("ДЕРЖАВА, ВІЙСЬКО Й ДИПЛОМАТІЯ")), 18);
        addModPair(Strings.t("Усі міністерства 5/5"), () -> { engine.modMaxMinistries(); renderAll(); },
                Strings.t("Вступ до Унії й Пакту"), () -> { engine.modJoinUnionAndPact(); renderAll(); });
        addModPair(Strings.t("Армія + Флот МАКС"), () -> { engine.modMaxArmyAndNavy(); renderAll(); },
                Strings.t("Звільнити все + Мир"), () -> { engine.modPeaceAndLiberateAll(); renderAll(); });

        // 4. Time Fast-Forward & Crisis Triggers
        addTop(contentContainer, microLabel(Strings.t("ПЕРЕМОТКА ЧАСУ ТА ТЕСТИ")), 18);
        LinearLayout ffRow = new LinearLayout(this);
        ffRow.setOrientation(LinearLayout.HORIZONTAL);
        TextView ff6 = outlineButton(Strings.t("+6 міс."), INK, () -> { engine.modFastForward(6); renderAll(); });
        TextView ff12 = outlineButton(Strings.t("+12 міс."), INK, () -> { engine.modFastForward(12); renderAll(); });
        TextView ff24 = outlineButton(Strings.t("+24 міс. (Вибори)"), POSITIVE, () -> { engine.modFastForward(24); renderAll(); });
        LinearLayout.LayoutParams t = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        t.setMargins(dp(2), 0, dp(2), 0);
        ffRow.addView(ff6, t);
        ffRow.addView(ff12, new LinearLayout.LayoutParams(t));
        ffRow.addView(ff24, new LinearLayout.LayoutParams(t));
        addTop(contentContainer, ffRow, 8);

        addModPair(Strings.t("Тест: Рейд терористів"), () -> { engine.applyShock(CityEngine.ShockType.TERROR_RAID); renderAll(); },
                Strings.t("Тест: Епідемія"), () -> { engine.applyShock(CityEngine.ShockType.EPIDEMIC); renderAll(); });
    }

    private void addModToggle(String title, String sub, boolean on, Runnable action) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(11), 0, dp(11));

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.addView(text(title, 14, INK, true));
        addTop(info, text(sub, 11.5f, INK_SUB, false), 2);
        row.addView(info, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView pill = text(on ? Strings.t("ВКЛ") : Strings.t("ВИМК"), 11.5f, on ? BG : INK_DIM, true);
        pill.setBackground(roundRect(on ? POSITIVE : Color.TRANSPARENT, on ? Color.TRANSPARENT : HAIRLINE, 8, on ? 0 : 1));
        pill.setPadding(dp(12), dp(7), dp(12), dp(7));
        pill.setOnClickListener(v -> action.run());
        row.addView(pill);
        contentContainer.addView(row);
    }

    private void addModPair(String labelA, Runnable actionA, String labelB, Runnable actionB) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        TextView a = outlineButton(labelA, INK, actionA);
        TextView b = outlineButton(labelB, INK, actionB);
        LinearLayout.LayoutParams l1 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        l1.setMarginEnd(dp(4));
        LinearLayout.LayoutParams l2 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        l2.setMarginStart(dp(4));
        row.addView(a, l1);
        row.addView(b, l2);
        addTop(contentContainer, row, 8);
    }

    // --------------------------------------------------- Log Panel

    private void renderLogPanel() {
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(text(Strings.t("Журнал подій"), 18, INK, true), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        top.addView(outlineButton(Strings.t("Назад"), INK, () -> { logOpen = false; renderAll(); }));
        contentContainer.addView(top);

        String[] filterLabels = {Strings.t("Усі"), Strings.t("Кризи"), Strings.t("Оборона"), Strings.t("Парламент"), Strings.t("Екон.")};
        LogEntry.Category[] cats = {null, LogEntry.Category.CRISIS, LogEntry.Category.SECURITY, LogEntry.Category.PARLIAMENT, LogEntry.Category.ECONOMY};
        int selIdx = 0;
        for (int i = 0; i < cats.length; i++) if (cats[i] == logFilter) selIdx = i;
        addTop(contentContainer, segmentedControl(filterLabels, selIdx, i -> { logFilter = cats[i]; renderAll(); }), 12);
        addTop(contentContainer, hairline(), 12);

        int shown = 0;
        for (LogEntry e : engine.logs) {
            if (logFilter != null && e.category != logFilter) continue;
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(0, dp(9), 0, dp(9));
            TextView tag = text(Strings.f("М%s · %s", e.month, Strings.t(e.category.tag)), 10, e.urgent ? NEGATIVE : INK_DIM, true);
            tag.setLetterSpacing(0.08f);
            row.addView(tag);
            addTop(row, text(e.message, 13, e.urgent ? INK : INK_SUB, e.urgent), 2);
            contentContainer.addView(row);
            contentContainer.addView(hairline());
            shown++;
        }
        if (shown == 0) addTop(contentContainer, text(Strings.t("У цій категорії ще немає записів."), 13, INK_DIM, false), 14);
    }

    // ------------------------------------------------------- Tab 0: City Hall

    private void renderCityTab() {
        if (engine.status != CityEngine.GameStatus.PLAYING) {
            LinearLayout card = surfaceBox();
            String title = engine.status == CityEngine.GameStatus.OVERRUN ? Strings.t("Усі регіони захоплено терористами")
                    : (engine.status == CityEngine.GameStatus.IMPEACHED ? Strings.t("Імпічмент мера")
                    : (engine.status == CityEngine.GameStatus.LOST_ELECTION ? Strings.f("Поразка на виборах (%.1f%%)", engine.lastElectionVotePct)
                    : Strings.t("Дефолт країни")));
            card.addView(microLabel(Strings.t("КАДЕНЦІЮ ЗАВЕРШЕНО")));
            addTop(card, text(title, 19, NEGATIVE, true), 6);
            addTop(card, text(Strings.f("Термін: %d міс. · Населення: %d · ВВП: %s ₴",
                    engine.month, engine.population, compactNum(engine.gdp)), 13.5f, INK_SUB, false), 8);
            addTop(card, primaryButton(Strings.t("Сценарії / Контрольні точки"), this::showScenarioPicker), 16);
            contentContainer.addView(card);
            return;
        }

        // Briefing line: main expense + stagnant treasury hint
        TextView brief = text(Strings.t(engine.stagnantTreasuryHint()), 12, engine.lastNetIncome < 25 ? WARN : INK_SUB, false);
        contentContainer.addView(brief);

        // Repair-all reminder when energy/industry wears out
        if (engine.maxRegionWear() >= 35.0) {
            LinearLayout rep = surfaceBox();
            rep.setBackground(roundRect(SURFACE, WARN, 11, 1));
            rep.setPadding(dp(12), dp(10), dp(12), dp(10));
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.addView(text(Strings.f("Знос об'єктів %d%%", Math.round(engine.maxRegionWear())), 13, WARN, true),
                    new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(priceChip(Strings.f("Полагодити все · %s ₴", engine.totalRepairCost()), true, () -> {
                engine.repairAllInfrastructure(); renderAll();
            }));
            rep.addView(row);
            addTop(contentContainer, rep, 8);
        }

        // Occupied regions banner
        if (engine.occupiedRegions() > 0) {
            LinearLayout occ = surfaceBox();
            occ.setBackground(roundRect(SURFACE, NEGATIVE, 12, 1));
            occ.addView(microLabel(Strings.t("ТЕРОРИСТИЧНА ОКУПАЦІЯ")));
            for (int i = 0; i < engine.regions.size(); i++) {
                RegionState r = engine.regions.get(i);
                if (!r.occupied) continue;
                final int idx = i;
                int chance = engine.liberationChancePct(i);
                addTop(occ, text(Strings.f("%s захоплено (сила %d)", Strings.t(r.name), Math.round(r.terrorStrength)), 13.5f, NEGATIVE, true), 5);
                addTop(occ, outlineButton(Strings.f("Звільнити %s · Шанс %s%% · 480 ₴", Strings.t(r.name), chance), INK, () -> {
                    engine.liberateRegion(idx); renderAll();
                }), 6);
            }
            addTop(contentContainer, occ, 8);
        }

        // Epidemic banner
        if (engine.epidemicLevel > 5.0) {
            LinearLayout ep = surfaceBox();
            ep.setBackground(roundRect(SURFACE, WARN, 12, 1));
            ep.addView(microLabel(Strings.f("ЕПІДЕМІЯ · %d%%", Math.round(engine.epidemicLevel))));
            LinearLayout btns = new LinearLayout(this);
            btns.setOrientation(LinearLayout.HORIZONTAL);
            TextView qBtn = outlineButton(engine.quarantine ? Strings.t("Карантин: ВКЛ") : Strings.t("Карантин: ВИМК"),
                    engine.quarantine ? POSITIVE : INK, () -> { engine.toggleQuarantine(); renderAll(); });
            TextView vBtn = outlineButton(engine.vaccination ? Strings.t("Вакцинація: ВКЛ") : Strings.t("Вакцинація: ВИМК"),
                    engine.vaccination ? POSITIVE : INK, () -> { engine.toggleVaccination(); renderAll(); });
            LinearLayout.LayoutParams h1 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            h1.setMarginEnd(dp(4));
            LinearLayout.LayoutParams h2 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            h2.setMarginStart(dp(4));
            btns.addView(qBtn, h1);
            btns.addView(vBtn, h2);
            addTop(ep, btns, 8);
            addTop(contentContainer, ep, 8);
        }

        // Active Dilemma Card
        if (engine.currentEvent != null) {
            CityEvent ev = engine.currentEvent;
            LinearLayout card = surfaceBox();
            card.addView(microLabel(Strings.t(ev.badge)));
            addTop(card, text(Strings.t(ev.title), 17.5f, INK, true), 5);
            TextView body = text(Strings.t(ev.body), 13.5f, INK_SUB, false);
            body.setLineSpacing(dp(2.5f), 1f);
            addTop(card, body, 6);
            addTop(card, hairline(), 12);
            for (int i = 0; i < ev.choices.length; i++) {
                final int idx = i;
                CityEvent.Choice ch = ev.choices[i];
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(0, dp(11), 0, dp(11));

                LinearLayout left = new LinearLayout(this);
                left.setOrientation(LinearLayout.VERTICAL);
                left.addView(text(Strings.t(ch.label), 14, INK, true));
                addTop(left, text(Strings.t(ch.hint), 11.5f, INK_SUB, false), 2);
                row.addView(left, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                row.addView(text("›", 19, INK_DIM, false));
                row.setOnClickListener(v -> { engine.resolveEvent(idx); renderAll(); });
                card.addView(row);
                if (i < ev.choices.length - 1) card.addView(hairline());
            }
            addTop(contentContainer, card, 10);
        }

        int toElec = engine.monthsToElection();
        String nextLabel = toElec <= 3 ? Strings.f("Наступний місяць · Вибори за %s міс.", toElec) : Strings.t("Наступний місяць");
        addTop(contentContainer, primaryButton(nextLabel, () -> { engine.nextMonth(); renderAll(); }), 14);

        // "Поза бюджетом" actionable jump lines
        List<CityEngine.OffBudgetLine> alerts = engine.offBudgetLines();
        if (!alerts.isEmpty()) {
            addTop(contentContainer, microLabel(Strings.t("ПОЗА БЮДЖЕТОМ · ПОТРЕБУЄ УВАГИ")), 18);
            for (CityEngine.OffBudgetLine line : alerts) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(0, dp(8), 0, dp(8));
                row.addView(text(Strings.t(line.title), 12.5f, WARN, false), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                row.addView(text(Strings.t(line.actionHint), 12, INK, true));
                row.setOnClickListener(v -> {
                    activeTab = line.targetTab;
                    if (line.targetTab == 1) regionSubTab = line.targetSubTab;
                    if (line.targetTab == 2) stateSubTab = line.targetSubTab;
                    if (line.targetTab == 3) worldSubTab = line.targetSubTab;
                    renderAll();
                });
                contentContainer.addView(row);
                contentContainer.addView(hairline());
            }
        }

        addTop(contentContainer, microLabel(Strings.t("СТАН ДЕРЖАВИ")), 18);
        double housingPct = Math.min(100.0, engine.housingCapacity() * 100.0 / Math.max(1, engine.population));
        addMinimalBar(Strings.t("Житло"), engine.population + " / " + engine.housingCapacity(), housingPct, housingPct >= 95 ? INK : NEGATIVE);
        addMinimalBar(Strings.t("Зайнятість · Кадри"), Strings.f("%.0f%% · %d спец.", 100 - engine.unemploymentPct, engine.specialists), 100 - engine.unemploymentPct, INK);
        addMinimalBar(Strings.t("Енергія"), Strings.f("%s%s МВт", engine.energyBalance() >= 0 ? "+" : "", engine.energyBalance()), Math.max(0, Math.min(100, 60 + engine.energyBalance())), engine.energyBalance() >= 0 ? INK : NEGATIVE);
        addMinimalBar(Strings.t("Тривалість життя · Еко"), Strings.f("%.1f р. · %.0f%%", engine.lifeExpectancy, engine.ecology), (engine.lifeExpectancy - 55) * 3.0, INK);
        addMinimalBar(Strings.t("Корупція · Увага АКБ"), Strings.f("%.0f%% · АКБ %.0f%%", engine.corruption, engine.acbAttention), Math.max(5, 100 - engine.corruption), engine.corruption <= 18 ? POSITIVE : WARN);
        addMinimalBar(Strings.t("Оборона · Загроза терору"), Strings.f("%.0f міць · %.0f%%", engine.defensePower(), engine.terrorThreat), engine.terrorThreat, engine.terrorThreat < 35 ? INK : NEGATIVE);
    }

    // ----------------------------------------- Tab 1: Regions / Workforce / Fleet

    private void renderRegionsTab() {
        contentContainer.addView(segmentedControl(
                new String[]{Strings.t("Області"), Strings.t("Кадри"), Strings.t("Техніка")},
                regionSubTab,
                idx -> { regionSubTab = idx; renderAll(); }));

        if (regionSubTab == 0) renderRegionsList();
        else if (regionSubTab == 1) renderWorkforceSubTab();
        else renderVehiclesAndNormsSubTab();
    }

    private void renderRegionsList() {
        // Sort controls + Repair All
        String[] sorts = {Strings.t("ВВП"), Strings.t("Насел."), Strings.t("Податок"), Strings.t("Знос"), Strings.t("Безпека")};
        addTop(contentContainer, segmentedControl(sorts, regionSort, idx -> { regionSort = idx; renderAll(); }), 10);

        if (engine.totalRepairCost() > 0) {
            addTop(contentContainer, outlineButton(Strings.f("Полагодити всю енергетику й промисловість · %s ₴", engine.totalRepairCost()),
                    WARN, () -> { engine.repairAllInfrastructure(); renderAll(); }), 10);
        }

        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < engine.regions.size(); i++) order.add(i);
        order.sort((a, b) -> {
            RegionState ra = engine.regions.get(a), rb = engine.regions.get(b);
            switch (regionSort) {
                case 1: return Integer.compare(rb.population, ra.population);
                case 2: return Integer.compare(rb.taxRate, ra.taxRate);
                case 3: return Double.compare(rb.wear, ra.wear);
                case 4: return Double.compare(rb.securityScore, ra.securityScore);
                default: return Double.compare(rb.gdp, ra.gdp);
            }
        });

        for (int idx : order) {
            RegionState r = engine.regions.get(idx);
            LinearLayout card = surfaceBox();
            if (r.occupied) card.setBackground(roundRect(SURFACE, NEGATIVE, 12, 1));

            LinearLayout head = new LinearLayout(this);
            head.setOrientation(LinearLayout.HORIZONTAL);
            head.setGravity(Gravity.CENTER_VERTICAL);

            LinearLayout titleCol = new LinearLayout(this);
            titleCol.setOrientation(LinearLayout.VERTICAL);
            if (r.occupied) titleCol.addView(text(Strings.t("ЗАХОПЛЕНО ТЕРОРИСТАМИ"), 10, NEGATIVE, true));
            titleCol.addView(text(Strings.t(r.name) + (r.coastal ? Strings.t(" · море") : ""), 15.5f, r.occupied ? NEGATIVE : INK, true));
            TextView meta = text(Strings.f("%d жит. · ВВП %.0f · Земля %d/%d · Знос %.0f%%",
                    r.population, r.gdp, r.usedLand(), r.totalLand(), r.wear), 11.5f, INK_SUB, false);
            meta.setFontFeatureSettings("tnum");
            addTop(titleCol, meta, 2);
            head.addView(titleCol, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            if (!r.occupied) {
                TextView zonePill = text(Strings.t(r.zone.title) + " ▾", 11.5f, INK, true);
                zonePill.setBackground(roundRect(ELEVATED, HAIRLINE, 8, 1));
                zonePill.setPadding(dp(9), dp(6), dp(9), dp(6));
                zonePill.setOnClickListener(v -> showZonePicker(idx));
                head.addView(zonePill);
            }
            card.addView(head);

            if (r.occupied) {
                int chance = engine.liberationChancePct(idx);
                addTop(card, primaryButton(Strings.f("Звільнити · Шанс %s%% · 480 ₴", chance), () -> {
                    engine.liberateRegion(idx); renderAll();
                }), 10);
            } else {
                TextView regStats = text(Strings.f(
                        "Мед %.0f%% · Осв %.0f%% · Безп %.0f%% · Щільність ×%.2f",
                        r.healthScore, r.eduScore, r.securityScore, r.densityCostMultiplier()), 11, INK_DIM, false);
                regStats.setFontFeatureSettings("tnum");
                addTop(card, regStats, 5);

                LinearLayout taxHead = new LinearLayout(this);
                taxHead.setOrientation(LinearLayout.HORIZONTAL);
                taxHead.addView(text(Strings.t("Податок області"), 12.5f, INK_SUB, false), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                TextView taxVal = text(r.taxRate + "%", 13, INK, true);
                taxVal.setFontFeatureSettings("tnum");
                taxHead.addView(taxVal);
                addTop(card, taxHead, 8);

                addTop(card, monochromeSeek(55, r.taxRate, p -> {
                    taxVal.setText(p + "%");
                    engine.setRegionTax(idx, p);
                    syncTickerOnly();
                    persist();
                }), 2);

                // Per-region Build, Expand Land & Lease Industrial Zone buttons
                LinearLayout actRow = new LinearLayout(this);
                actRow.setOrientation(LinearLayout.HORIZONTAL);
                TextView buildBtn = outlineButton(Strings.t("Будувати тут"), INK, () -> showRegionBuilderDialog(idx));
                TextView landBtn = outlineButton(Strings.t("+Земля · 520"), INK_SUB, () -> { engine.expandRegionLand(idx); renderAll(); });
                TextView leaseBtn = outlineButton(Strings.t("Промзона · 420"), INK_SUB, () -> { engine.leaseIndustrialZone(idx); renderAll(); });
                LinearLayout.LayoutParams t1 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.15f);
                t1.setMarginEnd(dp(3));
                LinearLayout.LayoutParams t2 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                t2.setMargins(dp(2), 0, dp(2), 0);
                LinearLayout.LayoutParams t3 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                t3.setMarginStart(dp(3));
                actRow.addView(buildBtn, t1);
                actRow.addView(landBtn, t2);
                actRow.addView(leaseBtn, t3);
                addTop(card, actRow, 8);
            }
            addTop(contentContainer, card, 10);
        }

        if (engine.canFoundRegion()) {
            addTop(contentContainer, outlineButton(Strings.f("Заснувати область · %s ₴", engine.foundRegionCost()),
                    INK, () -> { engine.foundRegion(); renderAll(); }), 12);
        }
    }

    /** Per-region construction picker showing all 22 objects + dynamic ROI payback in months! */
    private void showRegionBuilderDialog(int regionIdx) {
        RegionState r = engine.regions.get(regionIdx);
        List<BuildingType> eligible = new ArrayList<>();
        for (BuildingType b : BuildingType.values()) {
            if (b.coastalOnly && !r.coastal) continue;
            eligible.add(b);
        }
        String[] labels = new String[eligible.size()];
        for (int i = 0; i < eligible.size(); i++) {
            BuildingType b = eligible.get(i);
            int cost = engine.buildingCostInRegion(regionIdx, b);
            int roi = engine.estimateRoiMonths(regionIdx, b);
            String roiStr = roi > 0 ? Strings.f(" · окупн. ~%s міс.", roi) : Strings.t(" · соц./сервіс");
            labels[i] = Strings.f("%s (×%s) — %s ₴%s\n%s", Strings.t(b.title), r.count(b), cost, roiStr, Strings.t(b.summary));
        }
        new AlertDialog.Builder(this)
                .setTitle(Strings.f("Будівництво · %s%s", Strings.t(r.name), r.coastal ? Strings.t(" (море)") : ""))
                .setItems(labels, (d, which) -> {
                    engine.buildInRegion(regionIdx, eligible.get(which));
                    renderAll();
                })
                .setNegativeButton(Strings.t("Закрити"), null)
                .show();
    }

    private void showZonePicker(int regionIndex) {
        EconomicZone[] zones = EconomicZone.values();
        String[] labels = new String[zones.length];
        for (int i = 0; i < zones.length; i++) labels[i] = Strings.f("%s — %s", Strings.t(zones[i].title), Strings.t(zones[i].desc));
        new AlertDialog.Builder(this)
                .setTitle(Strings.f("Економічна зона · %s", Strings.t(engine.regions.get(regionIndex).name)))
                .setItems(labels, (d, which) -> { engine.setRegionZone(regionIndex, zones[which]); renderAll(); })
                .setNegativeButton(Strings.t("Скасувати"), null)
                .show();
    }

    private void renderWorkforceSubTab() {
        LinearLayout sum = surfaceBox();
        LinearLayout cols = new LinearLayout(this);
        cols.setOrientation(LinearLayout.HORIZONTAL);
        addMiniStat(cols, Strings.t("СПЕЦІАЛІСТИ"), String.valueOf(engine.specialists));
        addMiniStat(cols, Strings.t("ІНОЗЕМНІ"), String.valueOf(engine.foreignSpecialists));
        addMiniStat(cols, Strings.t("ЗА КОРДОНОМ"), String.valueOf(engine.specialistsAbroad));
        sum.addView(cols);
        int grads = engine.count(BuildingType.COLLEGE) * 6 + engine.count(BuildingType.UNIVERSITY) * 10;
        addTop(sum, text(Strings.f("Випуск коледжів (%s) та університетів (%s): +%s кадрів/міс.", engine.count(BuildingType.COLLEGE), engine.count(BuildingType.UNIVERSITY), grads), 12, INK_SUB, false), 10);
        addTop(contentContainer, sum, 12);

        addTop(contentContainer, microLabel(Strings.t("КАДРОВА ПОЛІТИКА")), 16);
        addActionRow(Strings.t("Найняти іноземних спеціалістів (+25)"), Strings.t("Поповнюють ринок праці й TFP · 480 ₴"), "480 ₴", engine.treasury >= 480,
                () -> { engine.hireForeignSpecialists(); renderAll(); });
        contentContainer.addView(hairline());
        addActionRow(Strings.t("Повернути громадян із-за кордону (+60)"), Strings.t("+60 жителів та +14 спеціалістів · 540 ₴"), "540 ₴", engine.treasury >= 540,
                () -> { engine.repatriateCitizens(); renderAll(); });
        contentContainer.addView(hairline());
        addActionRow(Strings.t("Спеціалісти за кордон (-15 вільних)"), Strings.t("+67 ₴/міс у бюджет і кращі відносини з країнами"), Strings.t("Відправити"), engine.specialists >= 20,
                () -> { engine.sendSpecialistsAbroad(); renderAll(); });
    }

    private void renderVehiclesAndNormsSubTab() {
        // Equipment norms & inter-service transfer
        LinearLayout normCard = surfaceBox();
        normCard.addView(microLabel(Strings.t("НОРМИ ТЕХНІКИ СЛУЖБ ТА ПЕРЕДАЧА")));
        for (int i = 0; i < 4; i++) {
            int have = engine.serviceEquipment[i];
            int norm = engine.serviceNorm(i);
            int tr = engine.serviceInTransit[i];
            String trStr = tr > 0 ? Strings.f(" (+%s в дорозі)", tr) : "";
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.addView(text(Strings.t(CityEngine.SERVICE_NAMES[i]), 13, INK, false), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            TextView val = text(have + " / " + norm + trStr, 13, have >= norm ? POSITIVE : WARN, true);
            val.setFontFeatureSettings("tnum");
            row.addView(val);
            addTop(normCard, row, 7);
        }
        addTop(normCard, outlineButton(Strings.t("Передати техніку між службами (Макс / кількість)"), INK, this::showTransferDialog), 10);
        addTop(contentContainer, normCard, 12);

        addTop(contentContainer, microLabel(Strings.t("ЦИВІЛЬНИЙ ТА КОМУНАЛЬНИЙ АВТОПАРК")), 16);
        renderVehicleGroup(false);
        addTop(contentContainer, microLabel(Strings.t("ВІЙСЬКОВА ТЕХНІКА")), 18);
        renderVehicleGroup(true);
    }

    private void showTransferDialog() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(20), dp(12), dp(20), dp(8));
        final int[] from = {0};
        final int[] to = {1};
        EditText qtyInput = new EditText(this);
        qtyInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        qtyInput.setText("1");
        TextView info = text(Strings.f("З: %s (%s) → До: %s", Strings.t(CityEngine.SERVICE_NAMES[0]), engine.serviceEquipment[0], Strings.t(CityEngine.SERVICE_NAMES[1])), 13, INK, true);
        box.addView(info);
        TextView cycleFrom = outlineButton(Strings.t("Змінити службу-відправника"), INK, () -> {
            from[0] = (from[0] + 1) % 4;
            if (from[0] == to[0]) to[0] = (to[0] + 1) % 4;
            info.setText(Strings.f("З: %s (%s) → До: %s", Strings.t(CityEngine.SERVICE_NAMES[from[0]]), engine.serviceEquipment[from[0]], Strings.t(CityEngine.SERVICE_NAMES[to[0]])));
        });
        TextView cycleTo = outlineButton(Strings.t("Змінити службу-отримувача"), INK_SUB, () -> {
            to[0] = (to[0] + 1) % 4;
            if (from[0] == to[0]) to[0] = (to[0] + 1) % 4;
            info.setText(Strings.f("З: %s (%s) → До: %s", Strings.t(CityEngine.SERVICE_NAMES[from[0]]), engine.serviceEquipment[from[0]], Strings.t(CityEngine.SERVICE_NAMES[to[0]])));
        });
        TextView maxBtn = outlineButton(Strings.t("Макс"), POSITIVE, () -> qtyInput.setText(String.valueOf(Math.max(1, engine.serviceEquipment[from[0]]))));
        addTop(box, cycleFrom, 8);
        addTop(box, cycleTo, 6);
        addTop(box, qtyInput, 8);
        addTop(box, maxBtn, 6);

        new AlertDialog.Builder(this)
                .setTitle(Strings.t("Передача техніки"))
                .setView(box)
                .setPositiveButton(Strings.t("Відправити"), (d, w) -> {
                    int q = 1;
                    try { q = Integer.parseInt(qtyInput.getText().toString().trim()); } catch (Exception ignored) {}
                    engine.transferEquipment(from[0], to[0], q);
                    renderAll();
                })
                .setNegativeButton(Strings.t("Скасувати"), null)
                .show();
    }

    private void renderVehicleGroup(boolean militaryOnly) {
        boolean first = true;
        for (VehicleType v : VehicleType.values()) {
            if (v.military != militaryOnly) continue;
            if (!first) contentContainer.addView(hairline());
            first = false;
            int have = engine.vehicleCount(v);
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(11), 0, dp(11));

            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            info.addView(text(Strings.t(v.title) + (have > 0 ? "  " + have : ""), 14.5f, INK, true));
            addTop(info, text(Strings.f("%s · -%s/міс", Strings.t(v.desc), v.upkeep), 11.5f, INK_SUB, false), 2);
            row.addView(info, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            if (have > 0) {
                TextView sell = text("−", 14, INK_SUB, true);
                sell.setBackground(roundRect(Color.TRANSPARENT, HAIRLINE, 8, 1));
                sell.setPadding(dp(9), dp(7), dp(9), dp(7));
                sell.setOnClickListener(view -> { engine.sellVehicle(v); renderAll(); });
                LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                slp.setMarginEnd(dp(5));
                row.addView(sell, slp);
            }
            row.addView(priceChip(v.cost + " ₴", engine.treasury >= v.cost, () -> { engine.buyVehicle(v); renderAll(); }));
            contentContainer.addView(row);
        }
    }

    // ----------------------------- Tab 2: Parliament / Ministries / Army / Navy

    private void renderStateTab() {
        contentContainer.addView(segmentedControl(
                new String[]{Strings.t("Парламент"), Strings.t("Мін-ва"), Strings.t("Армія"), Strings.t("Флот")},
                stateSubTab,
                idx -> { stateSubTab = idx; renderAll(); }));

        switch (stateSubTab) {
            case 0: renderParliamentSubTab(); break;
            case 1: renderMinistriesSubTab(); break;
            case 2: renderMilitarySubTab(); break;
            default: renderNavySubTab(); break;
        }
    }

    private void renderParliamentSubTab() {
        LinearLayout parlBox = surfaceBox();
        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.addView(microLabel(Strings.t("ПАРЛАМЕНТ · 100 МАНДАТІВ")), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        head.addView(text(Strings.f("Вибори за %s міс.", engine.monthsToElection()), 11.5f, INK_SUB, true));
        parlBox.addView(head);

        int[] seatColors = {INK, POSITIVE, INK_SUB, WARN};
        FactionType[] allFactions = FactionType.values();
        LinearLayout seatBar = new LinearLayout(this);
        seatBar.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < allFactions.length; i++) {
            View seg = new View(this);
            seg.setBackgroundColor(seatColors[i]);
            LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(0, dp(6), Math.max(1, engine.seats(allFactions[i])));
            if (i < allFactions.length - 1) slp.setMarginEnd(dp(2));
            seatBar.addView(seg, slp);
        }
        addTop(parlBox, seatBar, 10);

        for (int i = 0; i < allFactions.length; i++) {
            FactionType f = allFactions[i];
            double sup = engine.factionSupport(f);
            LinearLayout fHead = new LinearLayout(this);
            fHead.setOrientation(LinearLayout.HORIZONTAL);
            fHead.addView(text(Strings.f("%s · %s місць", Strings.t(f.title), engine.seats(f)), 12.5f, INK, false), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            fHead.addView(text(String.format(Locale.ROOT, "%.0f%%", sup), 12.5f, sup >= 50 ? POSITIVE : WARN, true));
            addTop(parlBox, fHead, 8);
        }

        LinearLayout parlActions = new LinearLayout(this);
        parlActions.setOrientation(LinearLayout.HORIZONTAL);
        TextView negBtn = outlineButton(Strings.t("Переговори (+12 · 280 ₴)"), INK, () -> { engine.negotiateCoalition(); renderAll(); });
        TextView confBtn = outlineButton(Strings.t("Вотум довіри"), INK_SUB, () -> { engine.voteOfConfidence(); renderAll(); });
        LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.3f);
        p1.setMarginEnd(dp(4));
        LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        p2.setMarginStart(dp(4));
        parlActions.addView(negBtn, p1);
        parlActions.addView(confBtn, p2);
        addTop(parlBox, parlActions, 10);
        addTop(contentContainer, parlBox, 12);

        // ACB Dossier & Mole ("Своя людина в АКБ")
        LinearLayout acbCard = surfaceBox();
        acbCard.addView(microLabel(Strings.t("ДОСЬЄ АКБ · «СВОЯ ЛЮДИНА В АКБ»")));
        addTop(acbCard, text(Strings.f(
                "Куратор досьє: %s · Увага АКБ: %.0f%% · Корупція: %.0f%%",
                engine.currentDossierName(), engine.acbAttention, engine.corruption), 12.5f, INK_SUB, false), 5);
        addTop(acbCard, text(engine.acbMoleActive
                ? Strings.t("Агент «на дні»: без доручень ризику викриття немає (лише -35 ₴/міс). Блокує вступ до Унії.")
                : Strings.t("Агента не залучено. Досьє завжди має наступника після звільнень."), 11.5f, INK_DIM, false), 3);
        LinearLayout acbBtns = new LinearLayout(this);
        acbBtns.setOrientation(LinearLayout.HORIZONTAL);
        TextView moleBtn = outlineButton(engine.acbMoleActive ? Strings.t("Відпустити агента") : Strings.t("Залучити агента · 350 ₴"),
                engine.acbMoleActive ? WARN : INK, () -> { engine.toggleAcbMole(); renderAll(); });
        acbBtns.addView(moleBtn, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        if (engine.acbMoleActive) {
            TextView taskBtn = outlineButton(Strings.t("Дати доручення (ризик)"), NEGATIVE, () -> { engine.triggerAcbMoleTask(); renderAll(); });
            LinearLayout.LayoutParams tl = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            tl.setMarginStart(dp(6));
            acbBtns.addView(taskBtn, tl);
        }
        addTop(acbCard, acbBtns, 8);
        addTop(contentContainer, acbCard, 10);

        // Laws & Economic Switches (with explicit inflation impact + confirmation for Martial Law!)
        addTop(contentContainer, microLabel(Strings.t("ЗАКОНИ, РЕФОРМИ ТА ПРОМИСЛОВІ ПРОЄКТИ (51/100 ЗА)")), 16);
        LawType[] allLaws = LawType.values();
        for (int i = 0; i < allLaws.length; i++) {
            LawType l = allLaws[i];
            boolean on = engine.hasLaw(l);
            int forecast = engine.forecastLawVotes(l);
            String inflSign = l.inflationImpact >= 0 ? "+" : "";

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(11), 0, dp(11));

            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            info.addView(text(Strings.t(l.title), 14, INK, true));
            addTop(info, text(Strings.t(l.desc), 11.5f, INK_SUB, false), 2);
            addTop(info, text(Strings.f("Прогноз: %d/100 · Інфляція %s%.2f%%", forecast, inflSign, l.inflationImpact),
                    11, forecast >= 51 ? POSITIVE : NEGATIVE, true), 2);
            row.addView(info, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            TextView toggle = text(on ? Strings.t("ДІЄ") : Strings.t("ГОЛОС"), 11.5f, on ? BG : INK, true);
            toggle.setBackground(roundRect(on ? INK : ELEVATED, on ? Color.TRANSPARENT : HAIRLINE, 8, on ? 0 : 1));
            toggle.setPadding(dp(11), dp(8), dp(11), dp(8));
            toggle.setOnClickListener(v -> {
                if (l == LawType.MARTIAL_LAW && !on) {
                    confirmAction(Strings.t("Запровадити Воєнний стан?"), Strings.t("Оборона +30%, але ВВП -10% і падіння довіри."), () -> {
                        engine.toggleLaw(l); renderAll();
                    });
                } else {
                    engine.toggleLaw(l); renderAll();
                }
            });
            row.addView(toggle);
            contentContainer.addView(row);
            if (i < allLaws.length - 1) contentContainer.addView(hairline());
        }

        // Road Fund switch
        addTop(contentContainer, outlineButton(engine.roadFundEnabled ? Strings.t("Дорожній фонд: ВКЛ") : Strings.t("Дорожній фонд: ВИМК"),
                engine.roadFundEnabled ? POSITIVE : INK_SUB, () -> {
                    engine.roadFundEnabled = !engine.roadFundEnabled;
                    engine.log(LogEntry.Category.ECONOMY, Strings.f("Дорожній фонд: %s", (engine.roadFundEnabled ? Strings.t("увімкнено") : Strings.t("вимкнено"))), false);
                    renderAll();
                }), 10);
    }

    private void renderMinistriesSubTab() {
        addTop(contentContainer, text(Strings.t("Міністрів затверджує парламент (потрібно ≥ 51/100 голосів). Сильні міністри (Рів. 3+) запускають нову хвилю реформ."), 12, INK_SUB, false), 12);
        addTop(contentContainer, hairline(), 10);

        MinistryType[] all = MinistryType.values();
        for (int i = 0; i < all.length; i++) {
            MinistryType m = all[i];
            int lvl = engine.ministryLevel(m);
            boolean maxed = lvl >= MinistryType.MAX_LEVEL;
            int cost = maxed ? 0 : m.upgradeCost(lvl);
            int votes = engine.forecastMinisterVotes(m);
            boolean canAfford = !maxed && engine.treasury >= cost && votes >= 51;

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(12), 0, dp(12));

            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            info.addView(text(Strings.f("%s · Рів. %s/5", Strings.t(m.title), lvl), 14.5f, INK, true));
            addTop(info, text(Strings.f("%s · -%s/рів.", Strings.t(m.effect), m.upkeepPerLevel), 11.5f, INK_SUB, false), 2);
            if (!maxed) {
                addTop(info, text(Strings.f("Парламент: %s/100 ЗА", votes), 11, votes >= 51 ? POSITIVE : NEGATIVE, true), 2);
            }
            row.addView(info, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(priceChip(maxed ? Strings.t("Макс") : cost + " ₴", canAfford, () -> {
                if (!maxed) { engine.upgradeMinistry(m); renderAll(); }
            }));
            contentContainer.addView(row);
            if (i < all.length - 1) contentContainer.addView(hairline());
        }
    }

    private void renderMilitarySubTab() {
        LinearLayout sum = surfaceBox();
        LinearLayout cols = new LinearLayout(this);
        cols.setOrientation(LinearLayout.HORIZONTAL);
        addMiniStat(cols, Strings.t("СКЛАД"), String.valueOf(engine.totalTroops()));
        addMiniStat(cols, Strings.t("МІЦЬ"), String.format(Locale.ROOT, "%.0f", engine.defensePower()));
        addMiniStat(cols, Strings.t("ЗАГРОЗА"), String.format(Locale.ROOT, "%.0f%%", engine.terrorThreat));
        sum.addView(cols);

        LinearLayout ops = new LinearLayout(this);
        ops.setOrientation(LinearLayout.HORIZONTAL);
        TextView sweep = outlineButton(Strings.t("Зачистка · 320 ₴"), INK, () -> { engine.counterTerrorSweep(); renderAll(); });
        TextView cspBtn = outlineButton(Strings.f("ЦСП «Альфа» · %s ₴", engine.cspOperationCost()), WARN, () -> {
            engine.runCspOperation(-1); renderAll();
        });
        LinearLayout.LayoutParams l1 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        l1.setMarginEnd(dp(4));
        LinearLayout.LayoutParams l2 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.15f);
        l2.setMarginStart(dp(4));
        ops.addView(sweep, l1);
        ops.addView(cspBtn, l2);
        addTop(sum, ops, 10);
        addTop(contentContainer, sum, 12);

        // PMC Card ("Приватна військова компанія")
        LinearLayout pmcCard = surfaceBox();
        pmcCard.addView(microLabel(Strings.t("ПРИВАТНА ВІЙСЬКОВА КОМПАНІЯ (ПВК)")));
        addTop(pmcCard, text(Strings.f("%s · Загін: %s", engine.pmc.displayName(), engine.pmc.squadSize)
                + Strings.f(" · Досвід: %s%s", engine.pmc.experience, engine.pmc.foreignContract ? Strings.t(" · НА КОНТРАКТІ") : Strings.t(" · Вдома")), 13.5f, INK, true), 5);
        LinearLayout pmcBtns = new LinearLayout(this);
        pmcBtns.setOrientation(LinearLayout.HORIZONTAL);
        TextView nameBtn = outlineButton(Strings.t("Емблема"), INK_SUB, () -> { engine.cyclePmcName(); renderAll(); });
        TextView hireBtn = outlineButton("+20 · 440 ₴", INK, () -> { engine.hirePmcSquad(); renderAll(); });
        TextView conBtn = outlineButton(engine.pmc.foreignContract ? Strings.t("Повернути") : Strings.t("Контракт"), POSITIVE, () -> {
            engine.togglePmcContract(); renderAll();
        });
        LinearLayout.LayoutParams t = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        t.setMargins(dp(2), 0, dp(2), 0);
        pmcBtns.addView(nameBtn, t);
        pmcBtns.addView(hireBtn, new LinearLayout.LayoutParams(t));
        pmcBtns.addView(conBtn, new LinearLayout.LayoutParams(t));
        addTop(pmcCard, pmcBtns, 8);
        addTop(contentContainer, pmcCard, 10);

        addTop(contentContainer, microLabel(Strings.t("СИЛИ ОБОРОНИ, ЦСП ТА РОЗВІДКИ")), 14);
        MilitaryBranch[] all = MilitaryBranch.values();
        for (int i = 0; i < all.length; i++) {
            MilitaryBranch b = all[i];
            int have = engine.troops(b);
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(11), 0, dp(11));

            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            info.addView(text(Strings.t(b.title) + "  " + have, 14.5f, INK, true));
            addTop(info, text(Strings.f("%s · -%s/міс", Strings.t(b.desc), b.monthlyUpkeepPerBatch), 11.5f, INK_SUB, false), 2);
            row.addView(info, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            if (have > 0) {
                TextView dis = text("−", 14, INK_SUB, true);
                dis.setBackground(roundRect(Color.TRANSPARENT, HAIRLINE, 8, 1));
                dis.setPadding(dp(9), dp(7), dp(9), dp(7));
                dis.setOnClickListener(v -> { engine.dismiss(b); renderAll(); });
                LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dlp.setMarginEnd(dp(5));
                row.addView(dis, dlp);
            }
            row.addView(priceChip("+" + b.batchSize + " · " + b.batchCost + " ₴", engine.treasury >= b.batchCost, () -> {
                engine.recruit(b); renderAll();
            }));
            contentContainer.addView(row);
            if (i < all.length - 1) contentContainer.addView(hairline());
        }
    }

    private void renderNavySubTab() {
        LinearLayout sum = surfaceBox();
        LinearLayout cols = new LinearLayout(this);
        cols.setOrientation(LinearLayout.HORIZONTAL);
        addMiniStat(cols, Strings.t("КОРАБЛІВ"), String.valueOf(engine.totalNavalVessels()));
        addMiniStat(cols, Strings.t("МІЦЬ ВМС"), String.format(Locale.ROOT, "%.0f", engine.navalPower()));
        addMiniStat(cols, Strings.t("ЕКІПАЖІ"), engine.troops(MilitaryBranch.NAVY) + "/" + engine.requiredNavalCrew());
        sum.addView(cols);
        addTop(sum, text(Strings.f("Морських баз: %s (потрібна на узбережжі для фрегатів, АПЧ і авіаносця). Берегова охорона притискає тіньову економіку.", engine.count(BuildingType.NAVAL_BASE)), 11.5f, INK_SUB, false), 8);
        addTop(contentContainer, sum, 12);

        addTop(contentContainer, microLabel(Strings.t("КОРАБЛІ ВІЙСЬКОВО-МОРСЬКОГО ФЛОТУ")), 14);
        NavalUnit[] all = NavalUnit.values();
        for (int i = 0; i < all.length; i++) {
            NavalUnit u = all[i];
            int have = engine.navalCount(u);
            addActionRow(Strings.t(u.title) + (have > 0 ? "  ×" + have : ""),
                    Strings.f("%s · Міць %s · Екіпаж %s · -%s/міс", Strings.t(u.desc), (int) u.navalPower, u.crewNeeded, u.upkeep),
                    u.cost + " ₴", engine.treasury >= u.cost,
                    () -> { engine.buyNavalUnit(u); renderAll(); });
            if (i < all.length - 1) contentContainer.addView(hairline());
        }
    }

    // ----------------------------- Tab 3: World (Diplomacy 2.0, Union/Pact, Partners)

    private void renderWorldTab() {
        contentContainer.addView(segmentedControl(
                new String[]{Strings.t("Дипломатія"), Strings.t("Унія / Пакт"), Strings.t("Партнери")},
                worldSubTab,
                idx -> { worldSubTab = idx; renderAll(); }));

        if (worldSubTab == 0) renderDiplomacySubTab();
        else if (worldSubTab == 1) renderUnionPactSubTab();
        else renderPartnersSubTab();
    }

    private void renderDiplomacySubTab() {
        addTop(contentContainer, microLabel(Strings.t("ТОРГІВЛЯ 2.0, ДИПЛОМАТІЯ ТА ВІЙНИ")), 12);
        addTop(contentContainer, segmentedControl(translateAll(CityEngine.TRADE_LABELS), engine.tradeRegime, regime -> {
            engine.tradeRegime = regime;
            engine.log(LogEntry.Category.ECONOMY, Strings.f("Загальний торговий режим: %s", Strings.t(CityEngine.TRADE_LABELS[regime])), false);
            renderAll();
        }), 8);

        for (int i = 0; i < engine.neighbors.size(); i++) {
            final int idx = i;
            NeighborCountry n = engine.neighbors.get(i);
            LinearLayout card = surfaceBox();
            if (n.atWar) card.setBackground(roundRect(SURFACE, NEGATIVE, 12, 1));

            LinearLayout head = new LinearLayout(this);
            head.setOrientation(LinearLayout.HORIZONTAL);
            head.addView(text(Strings.t(n.name) + (n.coastal ? Strings.t(" · море") : ""), 15.5f, n.atWar ? NEGATIVE : INK, true),
                    new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            String badge = n.capitulated ? Strings.t("ПРИЄДНАНО") : (n.atWar ? Strings.f("ВІЙНА %s%%", n.warProgress) : Strings.f("Віднос. %s", n.relations));
            head.addView(text(badge, 12, n.atWar ? NEGATIVE : (n.relations >= 20 ? POSITIVE : WARN), true));
            card.addView(head);

            if (n.capitulated) {
                addTop(card, text(Strings.t("Території й населення приєднано до вашої держави."), 12, POSITIVE, false), 4);
                addTop(contentContainer, card, 10);
                continue;
            }

            String dealStr = n.tradeDealDaysLeft > 0
                    ? Strings.f("Договір: %s дн. (%s год)", n.tradeDealDaysLeft, n.tradeDealDaysLeft * 24)
                    : Strings.t("Без договору");
            addTop(card, text(Strings.f(
                    "Армія %.0f · %s · Обл: %d · Мито %d%% · %s",
                    n.militaryPower, n.commander, n.regionsCount, n.tariffPct, dealStr), 11.5f, INK_SUB, false), 4);
            addTop(card, text(Strings.f("Посольство: %s", n.embassy ? Strings.t("Є") : Strings.t("Ні"))
                    + Strings.f(" · Культ. центр: %s", n.culturalCenter ? Strings.t("Є") : Strings.t("Ні"))
                    + Strings.f(" · Допомога: %s%%", n.aidSharePct), 11.5f, INK_DIM, false), 2);

            if (n.atWar) {
                LinearLayout wRow1 = new LinearLayout(this);
                wRow1.setOrientation(LinearLayout.HORIZONTAL);
                TextView offBtn = outlineButton(Strings.t("Наступ · 450"), NEGATIVE, () -> { engine.borderOffensive(idx); renderAll(); });
                TextView cspBtn = outlineButton(Strings.f("Рейд ЦСП · %s", engine.cspOperationCost()), WARN, () -> { engine.runCspOperation(idx); renderAll(); });
                TextView peaceBtn = outlineButton(Strings.t("Мир"), INK_SUB, () -> { engine.proposePeace(idx); renderAll(); });
                LinearLayout.LayoutParams t = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                t.setMargins(dp(2), 0, dp(2), 0);
                wRow1.addView(offBtn, t);
                wRow1.addView(cspBtn, new LinearLayout.LayoutParams(t));
                wRow1.addView(peaceBtn, new LinearLayout.LayoutParams(t));
                addTop(card, wRow1, 8);
                if (n.coastal) {
                    addTop(card, outlineButton(Strings.t("Морська блокада і десант ВМС · 520 ₴"), INK, () -> {
                        engine.navalBlockadeAndLanding(idx); renderAll();
                    }), 6);
                }
            } else {
                LinearLayout dRow1 = new LinearLayout(this);
                dRow1.setOrientation(LinearLayout.HORIZONTAL);
                TextView dealBtn = outlineButton(Strings.t("Договір"), INK, () -> { engine.signTradeDeal(idx); renderAll(); });
                TextView cuBtn = outlineButton(n.customsUnion ? Strings.t("Союз: Є") : Strings.t("Мит. союз"), INK_SUB, () -> { engine.toggleCustomsUnion(idx); renderAll(); });
                TextView courtBtn = outlineButton(Strings.t("Торг. суд"), INK_SUB, () -> { engine.fileTradeCourtCase(idx); renderAll(); });
                LinearLayout.LayoutParams t = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                t.setMargins(dp(2), 0, dp(2), 0);
                dRow1.addView(dealBtn, t);
                dRow1.addView(cuBtn, new LinearLayout.LayoutParams(t));
                dRow1.addView(courtBtn, new LinearLayout.LayoutParams(t));
                addTop(card, dRow1, 8);

                LinearLayout dRow2 = new LinearLayout(this);
                dRow2.setOrientation(LinearLayout.HORIZONTAL);
                TextView embBtn = outlineButton(n.embassy ? Strings.t("Культ. 360") : Strings.t("Посоль. 460"), INK_SUB, () -> {
                    if (n.embassy) engine.openCulturalCenter(idx); else engine.openEmbassy(idx);
                    renderAll();
                });
                TextView aidBtn = outlineButton(Strings.t("Допом. +15%"), INK_SUB, () -> { engine.adjustNeighborAid(idx, 15); renderAll(); });
                TextView visitOrWar = n.coastal && n.relations >= 0
                        ? outlineButton(Strings.t("Візит ВМС"), POSITIVE, () -> { engine.friendlyNavalVisit(idx); renderAll(); })
                        : outlineButton(Strings.t("Війна"), NEGATIVE, () -> confirmAction(Strings.f("Оголосити війну %s?", Strings.t(n.name)),
                        Strings.f("Командувач: %s · Сила: %s", n.commander, Math.round(n.militaryPower)), () -> {
                            engine.declareWar(idx); renderAll();
                        }));
                dRow2.addView(embBtn, new LinearLayout.LayoutParams(t));
                dRow2.addView(aidBtn, new LinearLayout.LayoutParams(t));
                dRow2.addView(visitOrWar, new LinearLayout.LayoutParams(t));
                addTop(card, dRow2, 6);
            }
            addTop(contentContainer, card, 10);
        }
    }

    private void renderUnionPactSubTab() {
        // Union Card
        LinearLayout uCard = surfaceBox();
        uCard.addView(microLabel(Strings.t("ЕКОНОМІЧНА УНІЯ · РАДА УНІЇ")));
        String uStatus = engine.union.isUnionMember()
                ? Strings.t("Повноправний член Унії") + (engine.union.unionPresidency ? Strings.t(" · Головування!") : "")
                : Strings.f("Етап %s/%s: %s", engine.union.unionStage, UnionState.UNION_STAGES.length, Strings.t(UnionState.UNION_STAGES[engine.union.unionStage]));
        addTop(uCard, text(uStatus, 15, engine.union.isUnionMember() ? POSITIVE : INK, true), 5);
        addTop(uCard, text(Strings.f("Критерії (прогрес не тане): %s", Strings.t(engine.unionCriteriaStatus())), 11.5f,
                engine.meetsUnionCriteria() ? POSITIVE : WARN, false), 4);
        if (!engine.union.isUnionMember()) {
            int cost = engine.union.nextUnionCost();
            int need = Math.max(0, (int) Math.ceil(cost - engine.treasury));
            addTop(uCard, text(Strings.f("Ціна етапу: %s ₴ · %s", cost, need == 0 ? Strings.t("Можна переходити!") : Strings.f("Ще накопичити: %s ₴", need)), 12, INK_SUB, false), 4);
            addTop(uCard, primaryButton(Strings.f("Пройти етап Унії · %s ₴", cost), () -> { engine.advanceUnionStage(); renderAll(); }), 10);
        } else {
            addTop(uCard, text(Strings.t("Фонд солідарності автоматично повертає 15% витрат на кризи й ремонт."), 11.5f, INK_SUB, false), 4);
            addTop(uCard, primaryButton(Strings.t("Сесія Ради Унії (голосування і транш)"), () -> { engine.voteUnionCouncil(); renderAll(); }), 10);
        }
        addTop(contentContainer, uCard, 12);

        // Defense Pact Card
        LinearLayout pCard = surfaceBox();
        pCard.addView(microLabel(Strings.t("ОБОРОННИЙ ПАКТ")));
        String pStatus = engine.union.isPactMember()
                ? Strings.t("Член Пакту · Бойова міць +35%")
                : Strings.f("Етап %s/%s: %s", engine.union.pactStage, UnionState.PACT_STAGES.length, Strings.t(UnionState.PACT_STAGES[engine.union.pactStage]));
        addTop(pCard, text(pStatus, 15, engine.union.isPactMember() ? POSITIVE : INK, true), 5);
        if (!engine.union.isPactMember()) {
            int cost = engine.union.nextPactCost();
            int need = Math.max(0, (int) Math.ceil(cost - engine.treasury));
            addTop(pCard, text(Strings.f("Ціна етапу: %s ₴ · %s", cost, need == 0 ? Strings.t("Достатньо коштів!") : Strings.f("Ще накопичити: %s ₴", need)), 12, INK_SUB, false), 4);
            addTop(pCard, outlineButton(Strings.f("Пройти етап Пакту · %s ₴", cost), INK, () -> { engine.advancePactStage(); renderAll(); }), 10);
        }
        addTop(contentContainer, pCard, 12);
    }

    private void renderPartnersSubTab() {
        addTop(contentContainer, microLabel(Strings.t("ПАРТНЕРИ · ЩОДЕННИЙ / ЩОМІСЯЧНИЙ ПОДАРУНОК У СКАРБНИЦЮ")), 12);
        String[] names = {Strings.t("Development Fund"), Strings.t("Northern Alliance (new partner)")};
        int[] gifts = {360, 420};
        for (int i = 0; i < 2; i++) {
            final int pIdx = i;
            boolean ready = engine.canClaimPartnerGift(i);
            LinearLayout card = surfaceBox();
            card.addView(text(names[i], 15, INK, true));
            addTop(card, text(Strings.f("Безповоротний грант підтримки +%s ₴ у скарбницю.", gifts[i]), 12, INK_SUB, false), 4);
            addTop(card, ready ? primaryButton(Strings.f("Отримати +%s ₴", gifts[i]), () -> { engine.claimPartnerGift(pIdx); renderAll(); })
                    : outlineButton(Strings.t("Вже отримано цього місяця"), INK_DIM, () -> {}), 10);
            addTop(contentContainer, card, 10);
        }
    }

    // ----------------------------- Tab 4: Economy (Charts & Lab / Finance & Backup)

    private void renderEconomyTab() {
        contentContainer.addView(segmentedControl(
                new String[]{Strings.t("Графіки і Лаб"), Strings.t("Фінанси і Сейв")},
                econSubTab,
                idx -> { econSubTab = idx; renderAll(); }));

        if (econSubTab == 0) renderChartsAndLabSubTab();
        else renderFinanceAndBackupSubTab();
    }

    private void renderChartsAndLabSubTab() {
        addTop(contentContainer, segmentedControl(
                new String[]{Strings.t("ВВП"), Strings.t("Інфл."), Strings.t("Курс"), Strings.t("Тінь"), Strings.t("Праця")},
                activeChart,
                idx -> { activeChart = idx; renderAll(); }), 10);

        LinearLayout chartBox = surfaceBox();
        chartBox.setPadding(dp(8), dp(10), dp(8), dp(8));
        ChartView chart = new ChartView(this);
        bindChart(chart);
        chartBox.addView(chart, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(186)));
        addTop(contentContainer, chartBox, 10);

        LinearLayout abRow = new LinearLayout(this);
        abRow.setOrientation(LinearLayout.HORIZONTAL);
        TextView capBtn = outlineButton(Strings.f("База A (%s)", engine.history.size()), INK, () -> {
            engine.captureBaseline(); compareAb = engine.hasBaseline(); renderAll();
        });
        boolean abActive = compareAb && engine.hasBaseline();
        TextView togBtn = outlineButton(abActive ? Strings.t("A/B: ВКЛ") : Strings.t("A/B: ВИМК"), abActive ? WARN : INK_SUB, () -> {
            if (engine.hasBaseline()) { compareAb = !compareAb; renderAll(); }
        });
        TextView csvBtn = outlineButton("CSV", POSITIVE, () -> {
            String file = "poketstate_" + engine.scenario.name().toLowerCase(Locale.ROOT) + "_m" + engine.month + ".csv";
            String res = CsvExporter.saveToDownloads(this, file, engine.exportCsv());
            engine.log(LogEntry.Category.ECONOMY, res, false);
            renderAll();
        });
        LinearLayout.LayoutParams t = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        t.setMargins(dp(2), 0, dp(2), 0);
        abRow.addView(capBtn, t);
        abRow.addView(togBtn, new LinearLayout.LayoutParams(t));
        abRow.addView(csvBtn, new LinearLayout.LayoutParams(t));
        addTop(contentContainer, abRow, 8);

        // Explanations of GDP, State Power & Treasury delta
        LinearLayout expCard = surfaceBox();
        expCard.addView(microLabel(Strings.t("ЯК РАХУЮТЬСЯ ВВП, СИЛА ДЕРЖАВИ ТА БАЛАНС")));
        addTop(expCard, text(Strings.t(engine.gdpAndPowerExplanation()), 11.5f, INK_SUB, false), 5);
        addTop(expCard, text(Strings.t(engine.treasuryChangeExplanation()), 11.5f, INK_DIM, false), 5);
        addTop(contentContainer, expCard, 12);

        addTop(contentContainer, microLabel(Strings.t("ЛАБОРАТОРІЯ ШОКІВ ТА «ВИБУХ ВВП»")), 14);
        CityEngine.ShockType[] shocks = CityEngine.ShockType.values();
        for (int r = 0; r < shocks.length; r += 2) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.addView(shockTile(shocks[r]), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            if (r + 1 < shocks.length) {
                row.addView(new View(this), new LinearLayout.LayoutParams(dp(8), 1));
                row.addView(shockTile(shocks[r + 1]), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            }
            addTop(contentContainer, row, 8);
        }
    }

    private void renderFinanceAndBackupSubTab() {
        // 1. Central Bank (Lagged Policy Rate + FX Parity + Reserves)
        addTop(contentContainer, microLabel(Strings.t("ЦЕНТРОБАНК (ЛАГ СТАВКИ 2 МІС.) · ПАРИТЕТ І ЗВР")), 12);
        addSliderRow(Strings.f("Ключова ставка ЦБ (діє: %s%%)", engine.laggedRate2), engine.policyRate + "%", 25, engine.policyRate, p -> {
            engine.policyRate = p; persist();
        });
        addSliderRow(Strings.t("Соціальні виплати"), engine.welfareLevel + "%", 100, engine.welfareLevel, p -> {
            engine.welfareLevel = p; persist();
        });

        LinearLayout fxCard = surfaceBox();
        LinearLayout fxCols = new LinearLayout(this);
        fxCols.setOrientation(LinearLayout.HORIZONTAL);
        addMiniStat(fxCols, Strings.t("КУРС ₴/$"), String.format(Locale.ROOT, "%.1f", engine.fxRate));
        addMiniStat(fxCols, Strings.t("ПАРИТЕТ"), String.format(Locale.ROOT, "%.1f", engine.fxParity));
        addMiniStat(fxCols, Strings.t("ЗВР НБК"), String.format(Locale.ROOT, "$%.0fM", engine.fxReserves));
        fxCard.addView(fxCols);
        addTop(fxCard, text(Strings.f(
                "Курс залежить від довіри ринку. $1 = %.1f ₴ · €1 ≈ %.1f ₴ · Ліміт купівлі ЗВР: $%.0fM (відновлюється поза грою).",
                engine.fxRate, engine.fxRate * 1.09, engine.fxBuyLimitLeft), 11, INK_DIM, false), 6);
        LinearLayout fxBtns = new LinearLayout(this);
        fxBtns.setOrientation(LinearLayout.HORIZONTAL);
        TextView mBtn = outlineButton(engine.fixedFx ? Strings.t("Плав. курс") : Strings.t("Фікс. курс"), INK, () -> { engine.toggleFixedFx(); renderAll(); });
        TextView sBtn = outlineButton(Strings.t("-$50M ЗВР"), INK, () -> { engine.fxIntervene(-50); renderAll(); });
        TextView bBtn = outlineButton(Strings.t("+$50M ЗВР"), INK_SUB, () -> { engine.fxIntervene(50); renderAll(); });
        LinearLayout.LayoutParams t = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        t.setMargins(dp(2), 0, dp(2), 0);
        fxBtns.addView(mBtn, t);
        fxBtns.addView(sBtn, new LinearLayout.LayoutParams(t));
        fxBtns.addView(bBtn, new LinearLayout.LayoutParams(t));
        addTop(fxCard, fxBtns, 8);
        addTop(contentContainer, fxCard, 10);

        // 2. Sovereign Fund with Endurance Preview
        LinearLayout sfCard = surfaceBox();
        sfCard.addView(microLabel(Strings.t("СУВЕРЕННИЙ ФОНД (СТАБФОНД)")));
        double curEnd = engine.sovereignEnduranceMonths(0);
        double nextEnd = engine.sovereignEnduranceMonths(400);
        addTop(sfCard, text(Strings.f("Фонд: %.0f ₴ · Витримка: %.1f міс. (після +400 ₴ → %.1f міс.)",
                engine.sovereignFund, curEnd, nextEnd), 12.5f, INK, false), 5);
        addTop(sfCard, outlineButton(Strings.t("Внести +400 ₴ у Суверенний фонд"), POSITIVE, () -> {
            engine.contributeSovereignFund(400); renderAll();
        }), 8);
        addTop(contentContainer, sfCard, 10);

        // 3. Debt evaluated by interest affordability + Custom Amount Repayment with exact savings
        LinearLayout debtCard = surfaceBox();
        debtCard.addView(microLabel(Strings.t("ДЕРЖБОРГ · ОЦІНКА ЗА ПОСИЛЬНІСТЮ ВІДСОТКІВ")));
        addTop(debtCard, text(Strings.f(
                "Борг: %.0f ₴ · Обслуговування: %.1f%% доходів (безпечно <25%%)",
                engine.debt, engine.debtServiceRatioPct()), 12.5f,
                engine.debtServiceRatioPct() < 25 ? INK : NEGATIVE, false), 5);
        int maxSlider = Math.max(100, (int) Math.min(engine.debt, Math.max(100, engine.treasury)));
        if (customRepayAmount > maxSlider) customRepayAmount = maxSlider;
        TextView savePreview = text(Strings.f(
                "Погасити %d ₴ → економія %.1f ₴/міс на відсотках",
                customRepayAmount, engine.estimateInterestSavedMonthly(customRepayAmount)), 11.5f, POSITIVE, false);
        addTop(debtCard, savePreview, 6);
        addTop(debtCard, monochromeSeek(maxSlider, Math.max(0, customRepayAmount), p -> {
            customRepayAmount = Math.max(10, p);
            savePreview.setText(Strings.f("Погасити %d ₴ → економія %.1f ₴/міс на відсотках",
                    customRepayAmount, engine.estimateInterestSavedMonthly(customRepayAmount)));
        }), 2);
        LinearLayout dBtns = new LinearLayout(this);
        dBtns.setOrientation(LinearLayout.HORIZONTAL);
        TextView bor = outlineButton(Strings.t("Кредит +1500 ₴"), INK, () -> { engine.borrow(1500); renderAll(); });
        TextView rep = outlineButton(Strings.t("Погасити обрану суму"), POSITIVE, () -> { engine.repayCustom(customRepayAmount); renderAll(); });
        LinearLayout.LayoutParams h1 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        h1.setMarginEnd(dp(4));
        LinearLayout.LayoutParams h2 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.2f);
        h2.setMarginStart(dp(4));
        dBtns.addView(bor, h1);
        dBtns.addView(rep, h2);
        addTop(debtCard, dBtns, 8);
        addTop(contentContainer, debtCard, 10);

        // 4. Global Commodities & Oil Export
        LinearLayout comm = new LinearLayout(this);
        comm.setOrientation(LinearLayout.HORIZONTAL);
        addCommodityMini(comm, Strings.t("НАФТА/ЕН."), engine.priceEnergy);
        addCommodityMini(comm, Strings.t("ЗЕРНО"), engine.priceGrain);
        addCommodityMini(comm, Strings.t("МЕТАЛ"), engine.priceMetal);
        addCommodityMini(comm, Strings.t("ТЕХНО"), engine.priceTech);
        addTop(contentContainer, comm, 12);

        // 5. Checkpoints & Cross-Device Backup
        addTop(contentContainer, outlineButton(Strings.t("Контрольні точки і резервна копія (синхронізація)"), INK, this::showCheckpointsDialog), 14);
    }

    private void showCheckpointsDialog() {
        List<String> items = new ArrayList<>();
        items.add(Strings.f("+ Створити контрольну точку зараз (Міс. %s)", engine.month));
        items.add(Strings.t("Скопіювати код збереження (для перенесення на інший пристрій)"));
        items.add(Strings.t("Вставити код збереження (відновити з буфера)"));
        for (CityEngine.Checkpoint cp : engine.checkpoints) {
            items.add(Strings.f("Відновити: %s · ВВП %s · Скарбн. %s", cp.label, compactNum(cp.gdp), compactNum(cp.treasury)));
        }
        new AlertDialog.Builder(this)
                .setTitle(Strings.t("Збереження і контрольні точки"))
                .setItems(items.toArray(new String[0]), (d, which) -> {
                    if (which == 0) {
                        engine.saveCheckpoint("Ручна · Міс. " + engine.month);
                        engine.log(LogEntry.Category.ACTION, Strings.f("Створено контрольну точку (Міс. %s).", engine.month), false);
                        renderAll();
                    } else if (which == 1) {
                        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("PoketStateSave", engine.toJson()));
                        engine.log(LogEntry.Category.ACTION, Strings.t("Резервну копію скопійовано в буфер обміну."), false);
                        renderAll();
                    } else if (which == 2) {
                        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        if (cm != null && cm.hasPrimaryClip() && cm.getPrimaryClip() != null && cm.getPrimaryClip().getItemCount() > 0) {
                            CharSequence raw = cm.getPrimaryClip().getItemAt(0).getText();
                            if (raw != null && engine.loadJson(raw.toString())) {
                                engine.log(LogEntry.Category.ACTION, Strings.t("Прогрес відновлено з резервної копії!"), false);
                            } else {
                                engine.log(LogEntry.Category.CRISIS, Strings.t("Некоректний формат резервної копії."), true);
                            }
                            renderAll();
                        }
                    } else {
                        engine.restoreCheckpoint(which - 3);
                        renderAll();
                    }
                })
                .setNegativeButton(Strings.t("Закрити"), null)
                .show();
    }

    private void bindChart(ChartView chart) {
        List<CitySnapshot> h = engine.history;
        int n = h.size();
        double[] a = new double[n], b = new double[n];
        double[] base = (compareAb && engine.hasBaseline()) ? extractMetric(engine.baselineHistory, activeChart) : null;
        switch (activeChart) {
            case 0:
                for (int i = 0; i < n; i++) { a[i] = h.get(i).gdp; b[i] = h.get(i).debt; }
                chart.setData(a, Strings.t("ВВП"), "₴", INK, b, Strings.t("Держборг"), "₴", INK_SUB);
                break;
            case 1:
                for (int i = 0; i < n; i++) { a[i] = h.get(i).inflationPct; b[i] = h.get(i).policyRate; }
                chart.setData(a, Strings.t("Інфляція"), "%", NEGATIVE, b, Strings.t("Ставка ЦБ"), "%", INK_SUB);
                break;
            case 2:
                for (int i = 0; i < n; i++) { a[i] = h.get(i).fxRate; b[i] = h.get(i).fxReserves; }
                chart.setData(a, Strings.t("Курс"), "₴", INK, b, Strings.t("ЗВР"), "$M", POSITIVE);
                break;
            case 3:
                for (int i = 0; i < n; i++) { a[i] = h.get(i).corruption; b[i] = h.get(i).shadowEconomy; }
                chart.setData(a, Strings.t("Корупція"), "%", WARN, b, Strings.t("Тінь"), "%", INK_SUB);
                break;
            default:
                for (int i = 0; i < n; i++) { a[i] = h.get(i).unemploymentPct; b[i] = h.get(i).trust; }
                chart.setData(a, Strings.t("Безробіття"), "%", WARN, b, Strings.t("Довіра"), "%", POSITIVE);
                break;
        }
        chart.setBaseline(base);
    }

    private double[] extractMetric(List<CitySnapshot> list, int chartIdx) {
        double[] out = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            CitySnapshot s = list.get(i);
            switch (chartIdx) {
                case 0: out[i] = s.gdp; break;
                case 1: out[i] = s.inflationPct; break;
                case 2: out[i] = s.fxRate; break;
                case 3: out[i] = s.corruption; break;
                default: out[i] = s.unemploymentPct; break;
            }
        }
        return out;
    }

    // ------------------------------------------------------------- UI Helpers

    private void addActionRow(String title, String sub, String btnText, boolean enabled, Runnable action) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(11), 0, dp(11));
        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.addView(text(title, 14, INK, true));
        addTop(info, text(sub, 11.5f, INK_SUB, false), 2);
        row.addView(info, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(priceChip(btnText, enabled, action));
        contentContainer.addView(row);
    }

    private void addCommodityMini(LinearLayout parent, String label, double val) {
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.addView(microLabel(label));
        int color = val >= 1.12 ? POSITIVE : (val <= 0.88 ? NEGATIVE : INK);
        TextView tv = text(String.format(Locale.ROOT, "%.2f×", val), 13.5f, color, true);
        tv.setFontFeatureSettings("tnum");
        addTop(col, tv, 2);
        parent.addView(col, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
    }

    private void addMiniStat(LinearLayout parent, String label, String val) {
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.addView(microLabel(label));
        TextView tv = text(val, 15, INK, true);
        tv.setFontFeatureSettings("tnum");
        addTop(col, tv, 2);
        parent.addView(col, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
    }

    private void syncTickerOnly() {
        LogEntry latest = engine.latestLog();
        tickerTag.setText(Strings.t(latest.category.tag));
        tickerText.setText(Strings.t(latest.message));
        tickerBtn.setText(logOpen ? Strings.t("Закрити") : Strings.f("Лог %d", engine.logs.size()));
    }

    private LinearLayout segmentedControl(String[] labels, int selected, java.util.function.IntConsumer onSelect) {
        LinearLayout seg = new LinearLayout(this);
        seg.setOrientation(LinearLayout.HORIZONTAL);
        seg.setBackground(roundRect(SURFACE, HAIRLINE, 10, 1));
        seg.setPadding(dp(3), dp(3), dp(3), dp(3));
        for (int i = 0; i < labels.length; i++) {
            final int idx = i;
            boolean active = selected == i;
            TextView chip = text(labels[i], 11.5f, active ? BG : INK_SUB, active);
            chip.setGravity(Gravity.CENTER);
            chip.setBackground(roundRect(active ? INK : Color.TRANSPARENT, Color.TRANSPARENT, 7, 0));
            chip.setPadding(dp(3), dp(8), dp(3), dp(8));
            chip.setOnClickListener(v -> onSelect.accept(idx));
            seg.addView(chip, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        }
        return seg;
    }

    private View shockTile(CityEngine.ShockType s) {
        LinearLayout box = surfaceBox();
        box.setPadding(dp(11), dp(11), dp(11), dp(11));
        int col = s == CityEngine.ShockType.TERROR_RAID ? NEGATIVE : (s == CityEngine.ShockType.GDP_BOOM ? POSITIVE : INK);
        box.addView(text(Strings.t(s.title), 12.5f, col, true));
        addTop(box, text(Strings.t(s.desc), 11, INK_SUB, false), 3);
        box.setOnClickListener(v -> { engine.applyShock(s); renderAll(); });
        return box;
    }

    private void addSliderRow(String label, String initialVal, int max, int current, java.util.function.IntConsumer onChange) {
        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.addView(text(label, 13.5f, INK, false), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView val = text(initialVal, 13.5f, INK, true);
        val.setFontFeatureSettings("tnum");
        head.addView(val);
        addTop(contentContainer, head, 10);

        addTop(contentContainer, monochromeSeek(max, current, p -> {
            val.setText(p + "%");
            onChange.accept(p);
        }), 3);
    }

    private SeekBar monochromeSeek(int max, int current, java.util.function.IntConsumer onChange) {
        SeekBar sb = new SeekBar(this);
        sb.setMax(max);
        sb.setProgress(current);
        sb.setProgressTintList(ColorStateList.valueOf(INK));
        sb.setThumbTintList(ColorStateList.valueOf(INK));
        sb.setProgressBackgroundTintList(ColorStateList.valueOf(HAIRLINE));
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) onChange.accept(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        return sb;
    }

    private TextView priceChip(String label, boolean enabled, Runnable action) {
        TextView btn = text(label, 12, enabled ? INK : INK_DIM, true);
        btn.setFontFeatureSettings("tnum");
        btn.setBackground(roundRect(enabled ? ELEVATED : Color.TRANSPARENT, HAIRLINE, 8, 1));
        btn.setPadding(dp(10), dp(7), dp(10), dp(7));
        btn.setOnClickListener(v -> action.run());
        return btn;
    }

    private void addMinimalBar(String label, String valueText, double pct, int fillColor) {
        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.addView(text(label, 12.5f, INK_SUB, false), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView v = text(valueText, 12.5f, INK, true);
        v.setFontFeatureSettings("tnum");
        head.addView(v);
        addTop(contentContainer, head, 11);

        LinearLayout track = new LinearLayout(this);
        track.setBackground(roundRect(HAIRLINE, Color.TRANSPARENT, 2, 0));
        View fill = new View(this);
        fill.setBackground(roundRect(fillColor, Color.TRANSPARENT, 2, 0));
        float frac = (float) Math.max(0.02, Math.min(1.0, pct / 100.0));
        track.addView(fill, new LinearLayout.LayoutParams(0, dp(3), frac));
        if (frac < 1f) track.addView(new View(this), new LinearLayout.LayoutParams(0, dp(3), 1f - frac));
        addTop(contentContainer, track, 5);
    }

    private LinearLayout surfaceBox() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackground(roundRect(SURFACE, HAIRLINE, 12, 1));
        box.setPadding(dp(14), dp(13), dp(14), dp(13));
        return box;
    }

    private TextView primaryButton(String label, Runnable action) {
        TextView btn = text(label, 14, BG, true);
        btn.setGravity(Gravity.CENTER);
        btn.setBackground(roundRect(INK, Color.TRANSPARENT, 10, 0));
        btn.setPadding(dp(14), dp(13), dp(14), dp(13));
        btn.setOnClickListener(v -> action.run());
        return btn;
    }

    private TextView outlineButton(String label, int color, Runnable action) {
        TextView btn = text(label, 12, color, true);
        btn.setGravity(Gravity.CENTER);
        btn.setBackground(roundRect(SURFACE, HAIRLINE, 9, 1));
        btn.setPadding(dp(7), dp(9), dp(7), dp(9));
        btn.setOnClickListener(v -> action.run());
        return btn;
    }

    private TextView microLabel(String s) {
        TextView tv = text(s, 9.5f, INK_DIM, true);
        tv.setLetterSpacing(0.10f);
        return tv;
    }

    private TextView text(String s, float sp, int color, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(s);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        tv.setTextColor(color);
        if (bold) tv.setTypeface(null, Typeface.BOLD);
        return tv;
    }

    private View hairline() {
        View v = new View(this);
        v.setBackgroundColor(HAIRLINE);
        v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));
        return v;
    }

    private GradientDrawable roundRect(int fill, int stroke, float radiusDp, float strokeDp) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(fill);
        d.setCornerRadius(dp(radiusDp));
        if (strokeDp > 0 && stroke != Color.TRANSPARENT) d.setStroke(Math.max(1, dp(strokeDp)), stroke);
        return d;
    }

    private void addTop(LinearLayout parent, View child, int topDp) {
        int height = child.getLayoutParams() != null ? child.getLayoutParams().height : ViewGroup.LayoutParams.WRAP_CONTENT;
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        lp.topMargin = dp(topDp);
        parent.addView(child, lp);
    }

    private void confirmAction(String title, String message, Runnable onConfirm) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(Strings.t("Підтвердити"), (d, w) -> onConfirm.run())
                .setNegativeButton(Strings.t("Скасувати"), null)
                .show();
    }

    private void showScenarioPicker() {
        ScenarioType[] all = ScenarioType.values();
        String[] labels = new String[all.length];
        for (int i = 0; i < all.length; i++) labels[i] = Strings.f("%s\n%s", Strings.t(all[i].title), Strings.t(all[i].desc));
        new AlertDialog.Builder(this)
                .setTitle(Strings.t("Сценарії старту"))
                .setItems(labels, (d, which) -> {
                    engine.startScenario(all[which]);
                    activeTab = 0;
                    logOpen = false;
                    compareAb = false;
                    renderAll();
                })
                .setNegativeButton(Strings.t("Скасувати"), null)
                .show();
    }

    private static String compactNum(double v) {
        double abs = Math.abs(v);
        if (abs >= 1e12) return String.format(Locale.ROOT, "%.1fT", v / 1e12);
        if (abs >= 1e9) return String.format(Locale.ROOT, "%.1fB", v / 1e9);
        if (abs >= 1e6) return String.format(Locale.ROOT, "%.1fM", v / 1e6);
        if (abs >= 1e4) return String.format(Locale.ROOT, "%.1fk", v / 1e3);
        return String.format(Locale.ROOT, "%.0f", v);
    }

    private static String[] translateAll(String[] labels) {
        String[] out = new String[labels.length];
        for (int i = 0; i < labels.length; i++) out[i] = Strings.t(labels[i]);
        return out;
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
