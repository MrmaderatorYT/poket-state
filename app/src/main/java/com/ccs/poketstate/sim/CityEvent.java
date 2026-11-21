package com.ccs.poketstate.sim;

import com.ccs.poketstate.i18n.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Contextual economic & political dilemmas presented to the mayor. */
public final class CityEvent {
    public static final class Choice {
        public final String label;
        public final String hint;
        public final int treasuryDelta;
        public final int debtDelta;
        public final double trustDelta;
        public final double inflationDelta;
        public final double tfpDelta;
        public final double ecologyDelta;
        public final int popDelta;
        public final BuildingType bonusBuilding;
        public final String outcome;

        public Choice(String label, String hint, int treasuryDelta, int debtDelta, double trustDelta,
                      double inflationDelta, double tfpDelta, double ecologyDelta, int popDelta,
                      BuildingType bonusBuilding, String outcome) {
            this.label = label;
            this.hint = hint;
            this.treasuryDelta = treasuryDelta;
            this.debtDelta = debtDelta;
            this.trustDelta = trustDelta;
            this.inflationDelta = inflationDelta;
            this.tfpDelta = tfpDelta;
            this.ecologyDelta = ecologyDelta;
            this.popDelta = popDelta;
            this.bonusBuilding = bonusBuilding;
            this.outcome = outcome;
        }
    }

    public final String id;
    public final String badge;
    public final String title;
    public final String body;
    public final Choice[] choices;

    public CityEvent(String id, String badge, String title, String body, Choice... choices) {
        this.id = id;
        this.badge = badge;
        this.title = title;
        this.body = body;
        this.choices = choices;
    }

    public static CityEvent pick(CityEngine e, Random rng) {
        List<CityEvent> pool = new ArrayList<>();

        if (e.inflationPct > 7.5) {
            pool.add(new CityEvent("inflation_unions", Strings.t("ІНФЛЯЦІЯ"), Strings.t("Профспілки вимагають індексації"),
                    Strings.t("Ціни зросли, і працівники погрожують зупинити транспорт, якщо місто не компенсує витрати."),
                    new Choice(Strings.t("Індексувати виплати"), Strings.t("-650 ₴ · Довіра +8 · Інфляція +1.4%"),
                            -650, 0, 8, 1.4, 0, 0, 0, null,
                            Strings.t("Виплати заспокоїли вулиці, але підігріли інфляційні очікування.")),
                    new Choice(Strings.t("Жорстко стримати бюджет"), Strings.t("Довіра -7 · Інфляція -1.8%"),
                            0, 0, -7, -1.8, 0.01, 0, 0, null,
                            Strings.t("Бюджет втримано, ціни почали сповільнюватися попри невдоволення.")),
                    new Choice(Strings.t("Субсидії фермерам"), Strings.t("-450 ₴ · Інфляція -1.2% · +Агроферма"),
                            -450, 0, 4, -1.2, 0.02, 0, 0, BuildingType.FARM,
                            Strings.t("Дешева пропозиція продовольства збила тиск на ціни."))));
        }

        if (e.debt > e.gdp * 0.7 && e.debt > 1500) {
            pool.add(new CityEvent("imf_program", Strings.t("БОРГ"), Strings.t("Пропозиція МВФ щодо реструктуризації"),
                    Strings.t("Кредитори готові надати дешевий транш, але вимагають скоротити дефіцит і пільги."),
                    new Choice(Strings.t("Прийняти програму МВФ"), Strings.t("+1400 ₴ · Борг -400 · Довіра -6"),
                            1400, -400, -6, -0.8, 0.03, 0, 0, null,
                            Strings.t("Ринки схвально оцінили угоду, але громадяни відчули економію.")),
                    new Choice(Strings.t("Відмовитися і покластися на ріст"), Strings.t("Довіра +4 · Інфляція +1.0%"),
                            0, 0, 4, 1.0, 0, 0, 0, null,
                            Strings.t("Місто зберегло незалежність бюджету, але ризик боргу лишається."))));
        }

        if (e.unemploymentPct > 11.0) {
            pool.add(new CityEvent("public_works", Strings.t("РИНОК ПРАЦІ"), Strings.t("Програма громадських робіт"),
                    Strings.t("Безробіття перевищило 11%. Економісти пропонують кейнсіанський стимул інфраструктурою."),
                    new Choice(Strings.t("Запустити будівництво"), Strings.t("-700 ₴ · +Завод · Довіра +6"),
                            -700, 0, 6, 0.7, 0.02, -3, 25, BuildingType.FACTORY,
                            Strings.t("Новий завод створив робочі місця та пожвавив попит.")),
                    new Choice(Strings.t("Курси перекваліфікації"), Strings.t("-400 ₴ · Продуктивність +4%"),
                            -400, 0, 4, -0.2, 0.04, 0, 0, null,
                            Strings.t("Працівники швидше знаходять роботу у високодохідних секторах."))));
        }

        if (e.energyBalance() < 0) {
            pool.add(new CityEvent("blackout", Strings.t("ЕНЕРГЕТИКА"), Strings.t("Дефіцит потужності в мережі"),
                    Strings.t("Заводи й квартали споживають більше струму, ніж виробляє місто. Почалися віялові відключення."),
                    new Choice(Strings.t("Терміновий імпорт струму"), Strings.t("-550 ₴ · Довіра +2"),
                            -550, 0, 2, 0.6, 0.01, 0, 0, null,
                            Strings.t("Імпорт врятував мережу цього місяця, але потрібна власна генерація.")),
                    new Choice(Strings.t("Екстрений енергоблок"), Strings.t("-800 ₴ · +Електростанція"),
                            -800, 0, 5, 0.2, 0.02, -4, 0, BuildingType.POWER_PLANT,
                            Strings.t("Нова станція стабілізувала енергосистему міста."))));
        }

        if (e.ecology < 45) {
            pool.add(new CityEvent("eco_protest", Strings.t("ЕКОЛОГІЯ"), Strings.t("Смог над промисловим районом"),
                    Strings.t("Мешканці вийшли з петицією через дим заводів. Промисловці проти нових штрафів."),
                    new Choice(Strings.t("Зелені фільтри й парк"), Strings.t("-500 ₴ · Екологія +15 · +Парк"),
                            -500, 0, 7, 0.2, 0, 15, 0, BuildingType.ECO_PARK,
                            Strings.t("Повітря стало чистішим, а рейтинг мера зріс.")),
                    new Choice(Strings.t("Підтримати промисловість"), Strings.t("+450 ₴ · Екологія -8 · Довіра -6"),
                            450, 0, -6, 0, 0.03, -8, 0, null,
                            Strings.t("Заводи наростили випуск, але громадяни незадоволені смогом."))));
        }

        if (e.defensePower() < 80 && e.month >= 3) {
            pool.add(new CityEvent("border_tension", Strings.t("ОБОРОНА"), Strings.t("Напруга на зовнішніх кордонах"),
                    Strings.t("Розвідка фіксує провокації біля торгового коридору. Інвестори очікують гарантій безпеки."),
                    new Choice(Strings.t("Посилити оборонний контракт"), Strings.t("-550 ₴ · Довіра +6 · Продукт. +2%"),
                            -550, 0, 6, 0.3, 0.02, 0, 0, null,
                            Strings.t("Зміцнення оборони заспокоїло бізнес і мешканців.")),
                    new Choice(Strings.t("Дипломатичні переговори через МЗС"), Strings.t("-200 ₴ · Довіра +2"),
                            -200, 0, 2, 0, 0, 0, 0, null,
                            Strings.t("Напругу тимчасово знижено, але армію варто посилити."))));
        }

        if (e.troops(MilitaryBranch.GUR) >= 10 || e.troops(MilitaryBranch.SSO) >= 15) {
            pool.add(new CityEvent("gur_intel", Strings.t("ГУР / ССО"), Strings.t("Оперативна інформація розвідки"),
                    Strings.t("ГУР перехопило дані про спробу спекулятивного зриву поставок пального."),
                    new Choice(Strings.t("Нейтралізувати через ССО"), Strings.t("-250 ₴ · Інфляція -1.4% · Довіра +5"),
                            -250, 0, 5, -1.4, 0.02, 0, 0, null,
                            Strings.t("Спецоперація зірвала диверсію, ціни залишилися стабільними.")),
                    new Choice(Strings.t("Передати справу Мінфіну"), Strings.t("+400 ₴ · Довіра +2"),
                            400, 0, 2, -0.4, 0, 0, 0, null,
                            Strings.t("Активи спекулянтів конфісковано до бюджету міста."))));
        }

        if (e.corruption > 35) {
            pool.add(new CityEvent("corruption_scandal", Strings.t("КОРУПЦІЯ"), Strings.t("Журналістське розслідування тендерів"),
                    Strings.t("Медіа оприлюднили факти завищення цін на підрядах. Парламент вимагає реакції."),
                    new Choice(Strings.t("Публічний аудит і звільнення"), Strings.t("-300 ₴ · Корупція -- · Довіра +6"),
                            -300, 0, 6, -0.2, 0.03, 0, 0, null,
                            Strings.t("Розслідування повернуло довіру та зменшило втрати бюджету.")),
                    new Choice(Strings.t("Зам'яти справу"), Strings.t("+250 ₴ · Довіра -8 · Продукт. -2%"),
                            250, 0, -8, 0.4, -0.02, 0, 0, null,
                            Strings.t("Скандал вдарив по рейтингу, а тіньовий сектор зріс."))));
        }

        if (e.epidemicLevel > 12) {
            pool.add(new CityEvent("epidemic_wave", Strings.t("ЕПІДЕМІЯ"), Strings.t("Перевантаження інфекційних відділень"),
                    Strings.t("Кількість хворих зростає. МОЗ просить додаткове фінансування для лікарень і вакцин."),
                    new Choice(Strings.t("Екстрені закупівлі вакцин"), Strings.t("-600 ₴ · Довіра +6 · +Лікарня"),
                            -600, 0, 6, 0.2, 0.01, 0, 0, BuildingType.HOSPITAL,
                            Strings.t("Медична система витримала пік епідемії.")),
                    new Choice(Strings.t("Обмежитися карантином"), Strings.t("Довіра -4 · ВВП -"),
                            0, 0, -4, -0.3, -0.02, 0, 0, null,
                            Strings.t("Карантин стримав інфекцію, але бізнес втратив частину виручки."))));
        }

        if (e.fxRate > 46.0) {
            pool.add(new CityEvent("fx_pressure", Strings.t("ВАЛЮТА"), Strings.t("Девальваційний тиск на курс"),
                    Strings.t("Населення масово скуповує валюту через побоювання інфляції. Імпорт дорожчає."),
                    new Choice(Strings.t("Валютна стабілізація"), Strings.t("-500 ₴ · Інфляція -1.5% · Довіра +4"),
                            -500, 0, 4, -1.5, 0.01, 0, 0, null,
                            Strings.t("Сигнал Центробанку заспокоїв валютний ринок.")),
                    new Choice(Strings.t("Відпустити курс на користь експорту"), Strings.t("+450 ₴ · Інфляція +1.6%"),
                            450, 0, -3, 1.6, 0.02, 0, 0, null,
                            Strings.t("Експортери отримали надприбутки, але імпортні товари подорожчали."))));
        }

        if (e.monthsToElection() <= 3 && e.month >= 18) {
            pool.add(new CityEvent("pre_election", Strings.t("ВИБОРИ"), Strings.t("Передвиборчі дебати в парламенті"),
                    Strings.t("До виборів залишилися лічені місяці. Опозиція пропонує роздати виборцям премії."),
                    new Choice(Strings.t("Соціальний пакет перед виборами"), Strings.t("-700 ₴ · Довіра +9 · Інфляція +1.3%"),
                            -700, 0, 9, 1.3, 0, 0, 0, null,
                            Strings.t("Рейтинг стрімко зріс, хоча економісти попереджають про ціни.")),
                    new Choice(Strings.t("Відзвітувати реформами без популізму"), Strings.t("Довіра +3 · Інфляція -0.5% · Продукт. +2%"),
                            0, 0, 3, -0.5, 0.02, 0, 0, null,
                            Strings.t("Технократи й інвестори підтримали відповідальну політику."))));
        }

        if (e.terrorThreat > 35 || e.occupiedRegions() > 0) {
            pool.add(new CityEvent("terror_ultimatum", Strings.t("ТЕРОРИЗМ"), Strings.t("Ультиматум бойовиків у прикордонні"),
                    Strings.t("Озброєна група погрожує диверсіями та захопленням адміністрації регіону, якщо місто не сплатить відкуп."),
                    new Choice(Strings.t("Контртерористичний штурм (ССО / КОРД)"), Strings.t("-350 ₴ · Загроза -- · Довіра +6"),
                            -350, 0, 6, 0, 0.01, 0, 0, null,
                            Strings.t("Спецпідрозділи зірвали плани бойовиків і зміцнили контроль.")),
                    new Choice(Strings.t("Спробувати переговори"), Strings.t("-500 ₴ · Довіра -6"),
                            -500, 0, -6, 0.4, 0, 0, 0, null,
                            Strings.t("Тимчасове перемир'я дорого обійшлося репутації влади."))));
        }

        if (e.parliamentSupport() < 48) {
            pool.add(new CityEvent("parliament_gridlock", Strings.t("ПАРЛАМЕНТ"), Strings.t("Коаліційна криза у парламенті"),
                    Strings.t("Фракції заблокували трибуну та вимагають поступок в обмін на голоси за реформи."),
                    new Choice(Strings.t("Погодити компромісний бюджет"), Strings.t("-350 ₴ · Підтримка парламенту +"),
                            -350, 0, 3, 0.3, 0, 0, 0, null,
                            Strings.t("Коаліцію відновлено, голосування за закони розблоковано.")),
                    new Choice(Strings.t("Звернутися напряму до громадян"), Strings.t("Довіра +4 · Парламент -"),
                            0, 0, 4, 0, 0, 0, 0, null,
                            Strings.t("Виборці оцінили принциповість, але фракції стали жорсткішими."))));
        }

        if (e.specialists > 35 && e.realWage < 3.2) {
            pool.add(new CityEvent("brain_drain", Strings.t("КАДРИ"), Strings.t("Відтік мізків за кордон"),
                    Strings.t("Іноземні корпорації переманюють інженерів і лікарів вищими зарплатами. Ринок праці втрачає кадри."),
                    new Choice(Strings.t("Гранти й житло для фахівців"), Strings.t("Масштабована ціна · Зберегти кадри · TFP +3%"),
                            -e.fairCrisisCost(380), 0, 5, 0.2, 0.03, 0, 20, null,
                            Strings.t("Ключові спеціалісти залишилися в країні, продуктивність зросла.")),
                    new Choice(Strings.t("Оплатити у борг (облігації)"), Strings.t("У борг · Зберегти кадри · Довіра +3"),
                            0, e.fairCrisisCost(420), 3, 0.3, 0.02, 0, 15, null,
                            Strings.t("Програму профінансовано облігаціями — відтік зупинено.")),
                    new Choice(Strings.t("Не втручатися"), Strings.t("Кадри - · Довіра -4"),
                            0, 0, -4, 0, -0.02, 0, -25, null,
                            Strings.t("Частина кваліфікованих працівників виїхала на закордонні контракти."))));
        }

        if (e.strongestMinistryLevel() >= 3) {
            pool.add(new CityEvent("minister_initiative", Strings.t("ІНІЦІАТИВА МІНІСТРА"), Strings.t("Міністр пропонує нову хвилю реформ"),
                    Strings.t("Сильний профільний міністр підготував пакет модернізації після вичерпання базових заходів."),
                    new Choice(Strings.t("Запустити нову хвилю реформ"), Strings.t("Ціна від скарбниці · TFP +6% · Довіра +5"),
                            -e.fairCrisisCost(450), 0, 5, -0.4, 0.06, 2, 0, null,
                            Strings.t("Нова хвиля реформ прискорила зростання економіки та знизила ціни!")),
                    new Choice(Strings.t("Профінансувати у борг"), Strings.t("У борг · TFP +5%"),
                            0, e.fairCrisisCost(490), 3, -0.2, 0.05, 1, 0, null,
                            Strings.t("Реформи запущено за рахунок запозичень без тиску на скарбницю.")),
                    new Choice(Strings.t("Відкласти на наступний квартал"), Strings.t("Без витрат"),
                            0, 0, -1, 0, 0, 0, 0, null,
                            Strings.t("Міністерство продовжило роботу в поточному режимі."))));
        }

        if (e.hasThreateningNeighbor()) {
            pool.add(new CityEvent("neighbor_threat", Strings.t("ДИПЛОМАТІЯ"), Strings.t("Дипломатична нота сусіда"),
                    Strings.t("Сусідня держава звинувачує вас у торгових бар'єрах і проводить навчання біля кордону."),
                    new Choice(Strings.t("Посилити ППО і стримування"), Strings.t("Ціна від скарбниці · Безпека + · Довіра +4"),
                            -e.fairCrisisCost(360), 0, 4, 0.2, 0.01, 0, 0, null,
                            Strings.t("Демонстрація готовності армії та флоту зняла напругу.")),
                    new Choice(Strings.t("У борг: оборонний пакет"), Strings.t("У борг · Безпека +"),
                            0, e.fairCrisisCost(400), 2, 0.2, 0, 0, 0, null,
                            Strings.t("Оборонний пакет профінансовано за рахунок облігацій.")),
                    new Choice(Strings.t("Дипломатичний компроміс"), Strings.t("Відносини + · Довіра -2"),
                            0, 0, -2, -0.2, 0, 0, 0, null,
                            Strings.t("Дипломати пом'якшили риторику та відклали ескалацію."))));
        }

        // General rich sandbox dilemmas always eligible
        pool.add(new CityEvent("tech_investor", Strings.t("ІНВЕСТИЦІЇ"), Strings.t("Іноземний IT-фонд шукає хаб"),
                Strings.t("Міжнародний фонд готовий відкрити R&D-центр у Poket State, якщо місто дасть податкові канікули."),
                new Choice(Strings.t("Дати пільги і землю"), Strings.t("-350 ₴ · +Технопарк · ВВП▲"),
                        -350, 0, 4, 0.3, 0.05, 0, 45, BuildingType.TECH_PARK,
                        Strings.t("Технопарк запрацював: приїхали інженери, виросли зарплати!")),
                new Choice(Strings.t("Вимагати повні податки"), Strings.t("+500 ₴ разово · Довіра +1"),
                        500, 0, 1, 0, 0.01, 0, 0, null,
                        Strings.t("Фонд обмежився невеликим офісом, але бюджет поповнився."))));

        pool.add(new CityEvent("grain_boom", Strings.t("ЕКСПОРТ"), Strings.t("Світовий попит на продовольство"),
                Strings.t("Ціни на зерно у світі підскочили. Експортери хочуть вивезти частину врожаю за кордон."),
                new Choice(Strings.t("Дозволити вільний експорт"), Strings.t("+750 ₴ · Інфляція +1.1%"),
                        750, 0, -1, 1.1, 0.02, 0, 0, null,
                        Strings.t("Валютна виручка наповнила бюджет, але продукти трохи подорожчали.")),
                new Choice(Strings.t("Залишити зерно для міста"), Strings.t("Інфляція -1.2% · Довіра +5"),
                        0, 0, 5, -1.2, 0, 0, 0, null,
                        Strings.t("Хліб залишився дешевим, мешканці вдячні за стабільність."))));

        pool.add(new CityEvent("housing_boom", Strings.t("МІСТОБУДУВАННЯ"), Strings.t("Девелопери пропонують новий мікрорайон"),
                Strings.t("Забудовники готові звести доступне житло за співфінансування міста."),
                new Choice(Strings.t("Співфінансувати квартал"), Strings.t("-300 ₴ · +Житло · +60 жит."),
                        -300, 0, 6, -0.3, 0, -2, 60, BuildingType.HOUSING,
                        Strings.t("Сотні сімей отримали нові квартири, місто росте.")),
                new Choice(Strings.t("Зосередитися на парках"), Strings.t("-200 ₴ · Екологія +10 · Довіра +3"),
                        -200, 0, 3, 0, 0, 10, 0, null,
                        Strings.t("Центр міста став зеленішим і привабливішим."))));

        pool.add(new CityEvent("fintech_sandbox", Strings.t("ФІНАНСИ"), Strings.t("Запуск міської фінтех-пісочниці"),
                Strings.t("Місцеві банки та стартапи просять спростити регуляцію для цифрових кредитів малому бізнесу."),
                new Choice(Strings.t("Схвалити дерегуляцію"), Strings.t("+300 ₴ · Продуктивність +4% · Інфляція +0.6%"),
                        300, 0, 3, 0.6, 0.04, 0, 20, null,
                        Strings.t("Малий бізнес отримав швидке фінансування і розширив найм.")),
                new Choice(Strings.t("Залишити суворий нагляд"), Strings.t("Інфляція -0.5% · Довіра +2"),
                        0, 0, 2, -0.5, 0, 0, 0, null,
                        Strings.t("Фінансова система лишилася консервативною та стійкою."))));

        pool.add(new CityEvent("shadow_economy", Strings.t("ПОДАТКИ"), Strings.t("Аудит тіньового сектору"),
                Strings.t("Податкова виявила схему ухилення серед великих торговців."),
                new Choice(Strings.t("Жорсткий аудит і штрафи"), Strings.t("+850 ₴ · Довіра -3 · Продуктивність -1%"),
                        850, 0, -3, -0.3, -0.01, 0, 0, null,
                        Strings.t("Бюджет отримав додаткові кошти, але бізнес став обережнішим.")),
                new Choice(Strings.t("Податкова амністія"), Strings.t("+350 ₴ · Довіра +4 · Продуктивність +2%"),
                        350, 0, 4, 0, 0.02, 0, 15, null,
                        Strings.t("Частина бізнесу вийшла в «білу» без конфлікту з підприємцями."))));

        pool.add(new CityEvent("refugee_wave", Strings.t("ДЕМОГРАФІЯ"), Strings.t("Хвиля переселенців із сусіднього регіону"),
                Strings.t("До кордонів Poket State прибули 110 фахівців і сімей у пошуках безпеки та роботи."),
                new Choice(Strings.t("Прийняти й надати гранти"), Strings.t("-450 ₴ · +110 жит. · Продуктивність +3%"),
                        -450, 0, 5, 0.4, 0.03, 0, 110, null,
                        Strings.t("Нові мешканці швидко інтегрувалися і посилили економіку.")),
                new Choice(Strings.t("Обмежити квоту"), Strings.t("+0 ₴ · +35 жит. · Довіра -2"),
                        0, 0, -2, 0, 0, 0, 35, null,
                        Strings.t("Місто прийняло лише частину сімей, зекономивши бюджет."))));

        pool.add(new CityEvent("university_grant", Strings.t("НАУКА"), Strings.t("Грант на міський університет"),
                Strings.t("Академія наук готова подвоїти інвестиції міста в лабораторію прикладної економіки."),
                new Choice(Strings.t("Співфінансувати кампус"), Strings.t("-550 ₴ · +Університет · Продуктивність +5%"),
                        -550, 0, 5, 0, 0.05, 2, 30, BuildingType.UNIVERSITY,
                        Strings.t("Новий університет підняв людський капітал усього міста!")),
                new Choice(Strings.t("Перенаправити на медицину"), Strings.t("-450 ₴ · +Лікарня · Довіра +6"),
                        -450, 0, 6, 0, 0.01, 0, 15, BuildingType.HOSPITAL,
                        Strings.t("Нова лікарня знизила захворюваність і підняла довіру."))));

        pool.add(new CityEvent("supply_squeeze", Strings.t("ЛОГІСТИКА"), Strings.t("Перебої на транспортному коридорі"),
                Strings.t("Через негоду затрималися поставки сировини. Заводи просять допомоги."),
                new Choice(Strings.t("Побудувати логістичний хаб"), Strings.t("-750 ₴ · +Логістичний хаб"),
                        -750, 0, 4, -0.6, 0.03, -2, 0, BuildingType.PORT,
                        Strings.t("Власний хаб розблокував маршрути й збільшив експорт.")),
                new Choice(Strings.t("Перечекати затримку"), Strings.t("Інфляція +1.0% · Довіра -3"),
                        0, 0, -3, 1.0, -0.02, 0, 0, null,
                        Strings.t("Ціни тимчасово підросли через нестачу комплектуючих."))));

        // Avoid repeating the exact same event twice in a row
        List<CityEvent> filtered = new ArrayList<>();
        for (CityEvent ev : pool) {
            if (!ev.id.equals(e.lastEventId)) filtered.add(ev);
        }
        List<CityEvent> source = filtered.isEmpty() ? pool : filtered;
        return source.get(rng.nextInt(source.size()));
    }
}
