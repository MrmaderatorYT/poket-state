package com.ccs.poketstate.i18n;

import java.util.Map;

/** English translations: {ukrainian key, english text}. */
final class EventText {
    static final Map<String, String> EN = Strings.dict(new String[][] {
            // inflation_unions
            {"ІНФЛЯЦІЯ", "INFLATION"},
            {"Профспілки вимагають індексації", "Unions demand wage indexation"},
            {"Ціни зросли, і працівники погрожують зупинити транспорт, якщо місто не компенсує витрати.", "Prices have risen, and workers threaten to halt public transport unless the city covers their costs."},
            {"Індексувати виплати", "Index payments"},
            {"-650 ₴ · Довіра +8 · Інфляція +1.4%", "-650 ₴ · Trust +8 · Inflation +1.4%"},
            {"Виплати заспокоїли вулиці, але підігріли інфляційні очікування.", "The payments calmed the streets but fueled inflation expectations."},
            {"Жорстко стримати бюджет", "Hold the budget firmly"},
            {"Довіра -7 · Інфляція -1.8%", "Trust -7 · Inflation -1.8%"},
            {"Бюджет втримано, ціни почали сповільнюватися попри невдоволення.", "The budget was held, and prices began to slow despite public discontent."},
            {"Субсидії фермерам", "Farmer subsidies"},
            {"-450 ₴ · Інфляція -1.2% · +Агроферма", "-450 ₴ · Inflation -1.2% · +Agro Farm"},
            {"Дешева пропозиція продовольства збила тиск на ціни.", "Cheaper food supply eased price pressure."},

            // imf_program
            {"БОРГ", "DEBT"},
            {"Пропозиція МВФ щодо реструктуризації", "IMF restructuring offer"},
            {"Кредитори готові надати дешевий транш, але вимагають скоротити дефіцит і пільги.", "Lenders are ready to provide a cheap tranche but demand cuts to the deficit and benefits."},
            {"Прийняти програму МВФ", "Accept the IMF program"},
            {"+1400 ₴ · Борг -400 · Довіра -6", "+1400 ₴ · Debt -400 · Trust -6"},
            {"Ринки схвально оцінили угоду, але громадяни відчули економію.", "Markets welcomed the deal, but citizens felt the austerity."},
            {"Відмовитися і покластися на ріст", "Decline and rely on growth"},
            {"Довіра +4 · Інфляція +1.0%", "Trust +4 · Inflation +1.0%"},
            {"Місто зберегло незалежність бюджету, але ризик боргу лишається.", "The city kept its budget independent, but the debt risk remains."},

            // public_works
            {"РИНОК ПРАЦІ", "LABOR MARKET"},
            {"Програма громадських робіт", "Public works program"},
            {"Безробіття перевищило 11%. Економісти пропонують кейнсіанський стимул інфраструктурою.", "Unemployment has exceeded 11%. Economists propose a Keynesian infrastructure stimulus."},
            {"Запустити будівництво", "Launch construction"},
            {"-700 ₴ · +Завод · Довіра +6", "-700 ₴ · +Factory · Trust +6"},
            {"Новий завод створив робочі місця та пожвавив попит.", "The new factory created jobs and revived demand."},
            {"Курси перекваліфікації", "Retraining courses"},
            {"-400 ₴ · Продуктивність +4%", "-400 ₴ · Productivity +4%"},
            {"Працівники швидше знаходять роботу у високодохідних секторах.", "Workers find jobs faster in high-income sectors."},

            // blackout
            {"ЕНЕРГЕТИКА", "ENERGY"},
            {"Дефіцит потужності в мережі", "Power shortage on the grid"},
            {"Заводи й квартали споживають більше струму, ніж виробляє місто. Почалися віялові відключення.", "Factories and districts consume more power than the city produces. Rolling blackouts have begun."},
            {"Терміновий імпорт струму", "Emergency power import"},
            {"-550 ₴ · Довіра +2", "-550 ₴ · Trust +2"},
            {"Імпорт врятував мережу цього місяця, але потрібна власна генерація.", "Imports saved the grid this month, but the city needs its own generation."},
            {"Екстрений енергоблок", "Emergency power unit"},
            {"-800 ₴ · +Електростанція", "-800 ₴ · +Power Plant"},
            {"Нова станція стабілізувала енергосистему міста.", "The new station stabilized the city's power system."},

            // eco_protest
            {"ЕКОЛОГІЯ", "ECOLOGY"},
            {"Смог над промисловим районом", "Smog over the industrial district"},
            {"Мешканці вийшли з петицією через дим заводів. Промисловці проти нових штрафів.", "Residents launched a petition over factory smoke. Industrialists oppose new fines."},
            {"Зелені фільтри й парк", "Green filters and a park"},
            {"-500 ₴ · Екологія +15 · +Парк", "-500 ₴ · Ecology +15 · +Park"},
            {"Повітря стало чистішим, а рейтинг мера зріс.", "The air became cleaner, and the mayor's rating rose."},
            {"Підтримати промисловість", "Support industry"},
            {"+450 ₴ · Екологія -8 · Довіра -6", "+450 ₴ · Ecology -8 · Trust -6"},
            {"Заводи наростили випуск, але громадяни незадоволені смогом.", "Factories ramped up output, but citizens are unhappy with the smog."},

            // border_tension
            {"ОБОРОНА", "DEFENSE"},
            {"Напруга на зовнішніх кордонах", "Tension on external borders"},
            {"Розвідка фіксує провокації біля торгового коридору. Інвестори очікують гарантій безпеки.", "Intelligence reports provocations near the trade corridor. Investors expect security guarantees."},
            {"Посилити оборонний контракт", "Strengthen the defense contract"},
            {"-550 ₴ · Довіра +6 · Продукт. +2%", "-550 ₴ · Trust +6 · Output +2%"},
            {"Зміцнення оборони заспокоїло бізнес і мешканців.", "Stronger defense reassured business and residents."},
            {"Дипломатичні переговори через МЗС", "Diplomatic talks via the Foreign Ministry"},
            {"-200 ₴ · Довіра +2", "-200 ₴ · Trust +2"},
            {"Напругу тимчасово знижено, але армію варто посилити.", "Tension was temporarily reduced, but the army should be strengthened."},

            // gur_intel
            {"ГУР / ССО", "GUR / SSO"},
            {"Оперативна інформація розвідки", "Intelligence operational information"},
            {"ГУР перехопило дані про спробу спекулятивного зриву поставок пального.", "GUR intercepted data on an attempt to speculatively disrupt fuel supplies."},
            {"Нейтралізувати через ССО", "Neutralize via SSO"},
            {"-250 ₴ · Інфляція -1.4% · Довіра +5", "-250 ₴ · Inflation -1.4% · Trust +5"},
            {"Спецоперація зірвала диверсію, ціни залишилися стабільними.", "A special operation foiled the sabotage, and prices remained stable."},
            {"Передати справу Мінфіну", "Refer the case to the Finance Ministry"},
            {"+400 ₴ · Довіра +2", "+400 ₴ · Trust +2"},
            {"Активи спекулянтів конфісковано до бюджету міста.", "The speculators' assets were confiscated into the city budget."},

            // corruption_scandal
            {"КОРУПЦІЯ", "CORRUPTION"},
            {"Журналістське розслідування тендерів", "Journalistic investigation of tenders"},
            {"Медіа оприлюднили факти завищення цін на підрядах. Парламент вимагає реакції.", "Media published evidence of inflated contract prices. Parliament demands a response."},
            {"Публічний аудит і звільнення", "Public audit and dismissals"},
            {"-300 ₴ · Корупція -- · Довіра +6", "-300 ₴ · Corruption -- · Trust +6"},
            {"Розслідування повернуло довіру та зменшило втрати бюджету.", "The investigation restored trust and reduced budget losses."},
            {"Зам'яти справу", "Hush up the case"},
            {"+250 ₴ · Довіра -8 · Продукт. -2%", "+250 ₴ · Trust -8 · Output -2%"},
            {"Скандал вдарив по рейтингу, а тіньовий сектор зріс.", "The scandal hit the ratings, and the shadow sector grew."},

            // epidemic_wave
            {"ЕПІДЕМІЯ", "EPIDEMIC"},
            {"Перевантаження інфекційних відділень", "Infectious disease wards overloaded"},
            {"Кількість хворих зростає. МОЗ просить додаткове фінансування для лікарень і вакцин.", "The number of patients is rising. The Health Ministry requests extra funding for hospitals and vaccines."},
            {"Екстрені закупівлі вакцин", "Emergency vaccine purchases"},
            {"-600 ₴ · Довіра +6 · +Лікарня", "-600 ₴ · Trust +6 · +Hospital"},
            {"Медична система витримала пік епідемії.", "The medical system withstood the epidemic peak."},
            {"Обмежитися карантином", "Limit to quarantine"},
            {"Довіра -4 · ВВП -", "Trust -4 · GDP -"},
            {"Карантин стримав інфекцію, але бізнес втратив частину виручки.", "Quarantine contained the infection, but businesses lost part of their revenue."},

            // fx_pressure
            {"ВАЛЮТА", "CURRENCY"},
            {"Девальваційний тиск на курс", "Devaluation pressure on the exchange rate"},
            {"Населення масово скуповує валюту через побоювання інфляції. Імпорт дорожчає.", "The public is buying up foreign currency en masse over inflation fears. Imports are getting more expensive."},
            {"Валютна стабілізація", "Currency stabilization"},
            {"-500 ₴ · Інфляція -1.5% · Довіра +4", "-500 ₴ · Inflation -1.5% · Trust +4"},
            {"Сигнал Центробанку заспокоїв валютний ринок.", "The central bank's signal calmed the currency market."},
            {"Відпустити курс на користь експорту", "Let the rate float in favor of exports"},
            {"+450 ₴ · Інфляція +1.6%", "+450 ₴ · Inflation +1.6%"},
            {"Експортери отримали надприбутки, але імпортні товари подорожчали.", "Exporters earned windfall profits, but imported goods became more expensive."},

            // pre_election
            {"ВИБОРИ", "ELECTIONS"},
            {"Передвиборчі дебати в парламенті", "Pre-election debates in parliament"},
            {"До виборів залишилися лічені місяці. Опозиція пропонує роздати виборцям премії.", "Only a few months remain before the election. The opposition proposes handing out bonuses to voters."},
            {"Соціальний пакет перед виборами", "Social package before the elections"},
            {"-700 ₴ · Довіра +9 · Інфляція +1.3%", "-700 ₴ · Trust +9 · Inflation +1.3%"},
            {"Рейтинг стрімко зріс, хоча економісти попереджають про ціни.", "The rating rose sharply, although economists warn about prices."},
            {"Відзвітувати реформами без популізму", "Report on reforms without populism"},
            {"Довіра +3 · Інфляція -0.5% · Продукт. +2%", "Trust +3 · Inflation -0.5% · Output +2%"},
            {"Технократи й інвестори підтримали відповідальну політику.", "Technocrats and investors backed responsible policy."},

            // terror_ultimatum
            {"ТЕРОРИЗМ", "TERRORISM"},
            {"Ультиматум бойовиків у прикордонні", "Militant ultimatum in the border area"},
            {"Озброєна група погрожує диверсіями та захопленням адміністрації регіону, якщо місто не сплатить відкуп.", "An armed group threatens sabotage and seizure of the regional administration unless the city pays a ransom."},
            {"Контртерористичний штурм (ССО / КОРД)", "Counterterrorist assault (SSO / KORD)"},
            {"-350 ₴ · Загроза -- · Довіра +6", "-350 ₴ · Threat -- · Trust +6"},
            {"Спецпідрозділи зірвали плани бойовиків і зміцнили контроль.", "Special units foiled the militants' plans and strengthened control."},
            {"Спробувати переговори", "Try negotiations"},
            {"-500 ₴ · Довіра -6", "-500 ₴ · Trust -6"},
            {"Тимчасове перемир'я дорого обійшлося репутації влади.", "The temporary truce came at a high cost to the authorities' reputation."},

            // parliament_gridlock
            {"ПАРЛАМЕНТ", "PARLIAMENT"},
            {"Коаліційна криза у парламенті", "Coalition crisis in parliament"},
            {"Фракції заблокували трибуну та вимагають поступок в обмін на голоси за реформи.", "Factions have blocked the rostrum and demand concessions in exchange for votes on reforms."},
            {"Погодити компромісний бюджет", "Agree to a compromise budget"},
            {"-350 ₴ · Підтримка парламенту +", "-350 ₴ · Parliament support +"},
            {"Коаліцію відновлено, голосування за закони розблоковано.", "The coalition was restored, and voting on laws was unblocked."},
            {"Звернутися напряму до громадян", "Appeal directly to citizens"},
            {"Довіра +4 · Парламент -", "Trust +4 · Parliament -"},
            {"Виборці оцінили принциповість, але фракції стали жорсткішими.", "Voters valued the principled stance, but the factions became harder-line."},

            // brain_drain
            {"КАДРИ", "WORKFORCE"},
            {"Відтік мізків за кордон", "Brain drain abroad"},
            {"Іноземні корпорації переманюють інженерів і лікарів вищими зарплатами. Ринок праці втрачає кадри.", "Foreign corporations are luring engineers and doctors with higher salaries. The labor market is losing talent."},
            {"Гранти й житло для фахівців", "Grants and housing for specialists"},
            {"Масштабована ціна · Зберегти кадри · TFP +3%", "Scaled cost · Retain staff · TFP +3%"},
            {"Ключові спеціалісти залишилися в країні, продуктивність зросла.", "Key specialists stayed in the country, and productivity rose."},
            {"Оплатити у борг (облігації)", "Pay with debt (bonds)"},
            {"У борг · Зберегти кадри · Довіра +3", "Debt · Retain staff · Trust +3"},
            {"Програму профінансовано облігаціями — відтік зупинено.", "The program was funded with bonds, and the outflow was stopped."},
            {"Не втручатися", "Do not intervene"},
            {"Кадри - · Довіра -4", "Staff - · Trust -4"},
            {"Частина кваліфікованих працівників виїхала на закордонні контракти.", "Some qualified workers left for overseas contracts."},

            // minister_initiative
            {"ІНІЦІАТИВА МІНІСТРА", "MINISTER'S INITIATIVE"},
            {"Міністр пропонує нову хвилю реформ", "The minister proposes a new wave of reforms"},
            {"Сильний профільний міністр підготував пакет модернізації після вичерпання базових заходів.", "A strong sector minister has prepared a modernization package after the basic measures were exhausted."},
            {"Запустити нову хвилю реформ", "Launch a new wave of reforms"},
            {"Ціна від скарбниці · TFP +6% · Довіра +5", "Cost from the treasury · TFP +6% · Trust +5"},
            {"Нова хвиля реформ прискорила зростання економіки та знизила ціни!", "The new wave of reforms accelerated economic growth and lowered prices!"},
            {"Профінансувати у борг", "Fund with debt"},
            {"У борг · TFP +5%", "Debt · TFP +5%"},
            {"Реформи запущено за рахунок запозичень без тиску на скарбницю.", "Reforms were launched with borrowed funds, without pressure on the treasury."},
            {"Відкласти на наступний квартал", "Postpone to next quarter"},
            {"Без витрат", "No cost"},
            {"Міністерство продовжило роботу в поточному режимі.", "The ministry continued operating as before."},

            // neighbor_threat
            {"ДИПЛОМАТІЯ", "DIPLOMACY"},
            {"Дипломатична нота сусіда", "Neighbor's diplomatic note"},
            {"Сусідня держава звинувачує вас у торгових бар'єрах і проводить навчання біля кордону.", "The neighboring state accuses you of trade barriers and is holding drills near the border."},
            {"Посилити ППО і стримування", "Strengthen air defense and deterrence"},
            {"Ціна від скарбниці · Безпека + · Довіра +4", "Cost from the treasury · Security + · Trust +4"},
            {"Демонстрація готовності армії та флоту зняла напругу.", "A demonstration of army and navy readiness eased tensions."},
            {"У борг: оборонний пакет", "Debt: defense package"},
            {"У борг · Безпека +", "Debt · Security +"},
            {"Оборонний пакет профінансовано за рахунок облігацій.", "The defense package was funded with bonds."},
            {"Дипломатичний компроміс", "Diplomatic compromise"},
            {"Відносини + · Довіра -2", "Relations + · Trust -2"},
            {"Дипломати пом'якшили риторику та відклали ескалацію.", "Diplomats softened the rhetoric and postponed escalation."},

            // tech_investor
            {"ІНВЕСТИЦІЇ", "INVESTMENT"},
            {"Іноземний IT-фонд шукає хаб", "Foreign IT fund seeks a hub"},
            {"Міжнародний фонд готовий відкрити R&D-центр у Poket State, якщо місто дасть податкові канікули.", "An international fund is ready to open an R&D center in Poket State if the city grants tax holidays."},
            {"Дати пільги і землю", "Grant benefits and land"},
            {"-350 ₴ · +Технопарк · ВВП▲", "-350 ₴ · +Tech Park · GDP▲"},
            {"Технопарк запрацював: приїхали інженери, виросли зарплати!", "The tech park is up and running: engineers have arrived and wages have grown!"},
            {"Вимагати повні податки", "Demand full taxes"},
            {"+500 ₴ разово · Довіра +1", "+500 ₴ one-time · Trust +1"},
            {"Фонд обмежився невеликим офісом, але бюджет поповнився.", "The fund settled for a small office, but the budget was replenished."},

            // grain_boom
            {"ЕКСПОРТ", "EXPORT"},
            {"Світовий попит на продовольство", "Global demand for food"},
            {"Ціни на зерно у світі підскочили. Експортери хочуть вивезти частину врожаю за кордон.", "Global grain prices have jumped. Exporters want to ship part of the harvest abroad."},
            {"Дозволити вільний експорт", "Allow free exports"},
            {"+750 ₴ · Інфляція +1.1%", "+750 ₴ · Inflation +1.1%"},
            {"Валютна виручка наповнила бюджет, але продукти трохи подорожчали.", "Currency revenue filled the budget, but food prices rose a little."},
            {"Залишити зерно для міста", "Keep the grain for the city"},
            {"Інфляція -1.2% · Довіра +5", "Inflation -1.2% · Trust +5"},
            {"Хліб залишився дешевим, мешканці вдячні за стабільність.", "Bread stayed cheap, and residents are grateful for the stability."},

            // housing_boom
            {"МІСТОБУДУВАННЯ", "URBAN DEVELOPMENT"},
            {"Девелопери пропонують новий мікрорайон", "Developers propose a new neighborhood"},
            {"Забудовники готові звести доступне житло за співфінансування міста.", "Developers are ready to build affordable housing with city co-financing."},
            {"Співфінансувати квартал", "Co-finance the district"},
            {"-300 ₴ · +Житло · +60 жит.", "-300 ₴ · +Housing · +60 residents"},
            {"Сотні сімей отримали нові квартири, місто росте.", "Hundreds of families got new apartments, and the city is growing."},
            {"Зосередитися на парках", "Focus on parks"},
            {"-200 ₴ · Екологія +10 · Довіра +3", "-200 ₴ · Ecology +10 · Trust +3"},
            {"Центр міста став зеленішим і привабливішим.", "The city center became greener and more attractive."},

            // fintech_sandbox
            {"ФІНАНСИ", "FINANCE"},
            {"Запуск міської фінтех-пісочниці", "Launch of the city fintech sandbox"},
            {"Місцеві банки та стартапи просять спростити регуляцію для цифрових кредитів малому бізнесу.", "Local banks and startups ask to simplify regulation for digital loans to small businesses."},
            {"Схвалити дерегуляцію", "Approve deregulation"},
            {"+300 ₴ · Продуктивність +4% · Інфляція +0.6%", "+300 ₴ · Productivity +4% · Inflation +0.6%"},
            {"Малий бізнес отримав швидке фінансування і розширив найм.", "Small businesses received fast financing and expanded hiring."},
            {"Залишити суворий нагляд", "Keep strict oversight"},
            {"Інфляція -0.5% · Довіра +2", "Inflation -0.5% · Trust +2"},
            {"Фінансова система лишилася консервативною та стійкою.", "The financial system remained conservative and stable."},

            // shadow_economy
            {"ПОДАТКИ", "TAXES"},
            {"Аудит тіньового сектору", "Shadow sector audit"},
            {"Податкова виявила схему ухилення серед великих торговців.", "The tax service uncovered an evasion scheme among large traders."},
            {"Жорсткий аудит і штрафи", "Strict audit and fines"},
            {"+850 ₴ · Довіра -3 · Продуктивність -1%", "+850 ₴ · Trust -3 · Productivity -1%"},
            {"Бюджет отримав додаткові кошти, але бізнес став обережнішим.", "The budget received extra funds, but business became more cautious."},
            {"Податкова амністія", "Tax amnesty"},
            {"+350 ₴ · Довіра +4 · Продуктивність +2%", "+350 ₴ · Trust +4 · Productivity +2%"},
            {"Частина бізнесу вийшла в «білу» без конфлікту з підприємцями.", "Part of the business moved into the \"white\" economy without conflict with entrepreneurs."},

            // refugee_wave
            {"ДЕМОГРАФІЯ", "DEMOGRAPHICS"},
            {"Хвиля переселенців із сусіднього регіону", "Wave of displaced people from a neighboring region"},
            {"До кордонів Poket State прибули 110 фахівців і сімей у пошуках безпеки та роботи.", "110 specialists and families have arrived at Poket State's borders seeking safety and work."},
            {"Прийняти й надати гранти", "Accept them and provide grants"},
            {"-450 ₴ · +110 жит. · Продуктивність +3%", "-450 ₴ · +110 residents · Productivity +3%"},
            {"Нові мешканці швидко інтегрувалися і посилили економіку.", "The new residents integrated quickly and strengthened the economy."},
            {"Обмежити квоту", "Limit the quota"},
            {"+0 ₴ · +35 жит. · Довіра -2", "+0 ₴ · +35 residents · Trust -2"},
            {"Місто прийняло лише частину сімей, зекономивши бюджет.", "The city accepted only some families, saving the budget."},

            // university_grant
            {"НАУКА", "SCIENCE"},
            {"Грант на міський університет", "Grant for the city university"},
            {"Академія наук готова подвоїти інвестиції міста в лабораторію прикладної економіки.", "The Academy of Sciences is ready to double the city's investment in the applied economics lab."},
            {"Співфінансувати кампус", "Co-finance the campus"},
            {"-550 ₴ · +Університет · Продуктивність +5%", "-550 ₴ · +University · Productivity +5%"},
            {"Новий університет підняв людський капітал усього міста!", "The new university raised the human capital of the entire city!"},
            {"Перенаправити на медицину", "Redirect to medicine"},
            {"-450 ₴ · +Лікарня · Довіра +6", "-450 ₴ · +Hospital · Trust +6"},
            {"Нова лікарня знизила захворюваність і підняла довіру.", "The new hospital reduced sickness rates and raised trust."},

            // supply_squeeze
            {"ЛОГІСТИКА", "LOGISTICS"},
            {"Перебої на транспортному коридорі", "Disruptions on the transport corridor"},
            {"Через негоду затрималися поставки сировини. Заводи просять допомоги.", "Raw material deliveries were delayed by bad weather. Factories are asking for help."},
            {"Побудувати логістичний хаб", "Build a logistics hub"},
            {"-750 ₴ · +Логістичний хаб", "-750 ₴ · +Logistics Hub"},
            {"Власний хаб розблокував маршрути й збільшив експорт.", "Our own hub unblocked routes and increased exports."},
            {"Перечекати затримку", "Wait out the delay"},
            {"Інфляція +1.0% · Довіра -3", "Inflation +1.0% · Trust -3"},
            {"Ціни тимчасово підросли через нестачу комплектуючих.", "Prices temporarily rose due to a shortage of components."},
    });

    private EventText() {}
}
