package co.za.mrhdigital.wpcbroadsheet.config;

import co.za.mrhdigital.wpcbroadsheet.model.*;
import co.za.mrhdigital.wpcbroadsheet.repository.*;
import co.za.mrhdigital.wpcbroadsheet.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Seeds the database on first boot with the same data as the Android SampleData.
 * Idempotent — only runs if no users exist yet.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final UserRepository       userRepository;
    private final SiteRepository       siteRepository;
    private final ResidentRepository   residentRepository;
    private final ResidentAuditRepository auditRepository;
    private final MealEntryRepository  entryRepository;
    private final MealPricingRepository pricingRepository;
    private final UserService          userService;
    private final ObjectMapper         objectMapper;

    @Bean
    public ApplicationRunner seed() {
        return args -> {
            if (userRepository.countBy() > 0) {
                log.info("Database already seeded — skipping.");
                return;
            }
            log.info("Seeding initial data...");
            long now = System.currentTimeMillis();
            seedSites(now);
            seedUsers(now);
            seedResidents(now);
            seedJanuaryEntries(now);
            seedPricingConfigs(now);
            log.info("Seeding complete.");
        };
    }

    // ── Sites ──────────────────────────────────────────────────────────────────

    private void seedSites(long now) {
        List.of(
            site("lizane",  "Lizane Village",     now),
            site("bakkies", "Bakkies Estate",      now),
            site("sunhill", "Sunhill Retirement",  now)
        ).forEach(siteRepository::save);
    }

    private SiteEntity site(String id, String name, long now) {
        return SiteEntity.builder()
            .id(id).name(name).isActive(true)
            .createdAt(now).createdBy("system")
            .lastModifiedAt(now).lastModifiedBy("system")
            .build();
    }

    // ── Users ──────────────────────────────────────────────────────────────────

    private void seedUsers(long now) {
        String hash = userService.sha256("wpc2026");
        List.of(
            user("admin1", "System Admin",         "admin@wpc.co.za",       UserRole.ADMIN,               "", null,       hash, now),
            user("u1",     "Chernay Hildebrandt",  "chernay@wpc.co.za",     UserRole.OPERATIONS_MANAGER,  "+27 82 555 0100", null, hash, now),
            user("u2",     "Megan van Rooyen",     "vanrooyen@wpc.co.za",   UserRole.UNIT_MANAGER,        "+27 72 555 0200", "lizane",  hash, now),
            user("u3",     "Martin Nothnagel",     "nothnagel@wpc.co.za",   UserRole.UNIT_MANAGER,        "+27 72 555 0300", "bakkies", hash, now)
        ).forEach(userRepository::save);
    }

    private UserEntity user(String id, String name, String email, UserRole role,
                            String phone, String siteId, String hash, long now) {
        return UserEntity.builder()
            .id(id).name(name).email(email).passwordHash(hash)
            .role(role).phone(phone).siteId(siteId).isActive(true)
            .createdAt(now).createdBy("system")
            .lastModifiedAt(now).lastModifiedBy("system")
            .build();
    }

    // ── Residents ──────────────────────────────────────────────────────────────

    private void seedResidents(long now) {
        // Lizane Village
        List.of(
            r("001","Me van Rooyen",           1, ResidentType.RENTAL, "lizane"),
            r("002","Mev Pestana",              1, ResidentType.OWNER,  "lizane"),
            r("003","Mnr & Me Nothnagel",       2, ResidentType.OWNER,  "lizane"),
            r("004","Mrs Bloomfield",           1, ResidentType.OWNER,  "lizane"),
            r("005","Gillen",                   1, ResidentType.RENTAL, "lizane"),
            r("006","Mnr & Me Swanepoel",       2, ResidentType.OWNER,  "lizane"),
            r("007","Me Fouche",                1, ResidentType.RENTAL, "lizane"),
            r("008","Mr & Mrs Bothe",           2, ResidentType.OWNER,  "lizane"),
            r("009","Mr & Mev VD Banks",        2, ResidentType.RENTAL, "lizane"),
            r("010","Mrs Mason",                1, ResidentType.OWNER,  "lizane"),
            r("011","Mrs Matheson",             1, ResidentType.RENTAL, "lizane"),
            r("012","Mrs Retief",               2, ResidentType.OWNER,  "lizane"),
            r("013","Mnr Welgemoed",            1, ResidentType.OWNER,  "lizane"),
            r("014","Me Klein (Brandt)",        1, ResidentType.OWNER,  "lizane"),
            r("015","Mr & Mrs Calitz",          2, ResidentType.RENTAL, "lizane"),
            r("016","Me Heine",                 1, ResidentType.OWNER,  "lizane"),
            r("017","Me Smith",                 1, ResidentType.OTP,    "lizane"),
            r("018","Mnr & Mev Wolhuter",       1, ResidentType.RENTAL, "lizane"),
            r("019","Me Beylefeldt",            1, ResidentType.RENTAL, "lizane"),
            r("021","Me Banister",              1, ResidentType.OWNER,  "lizane"),
            r("022","Ay Mcpetrie",              1, ResidentType.OWNER,  "lizane"),
            r("023","Mrs Edwards",              1, ResidentType.OWNER,  "lizane"),
            r("024","Mnr J Nothnagel",          1, ResidentType.RENTAL, "lizane"),
            // Bakkies Estate
            r("B01","Mr & Mrs Kruger",          2, ResidentType.OWNER,  "bakkies"),
            r("B02","Me Joubert",               1, ResidentType.RENTAL, "bakkies"),
            r("B03","Mnr & Mev Fourie",         2, ResidentType.OWNER,  "bakkies"),
            r("B04","Mrs Pretorius",            1, ResidentType.OWNER,  "bakkies"),
            r("B05","Me du Plessis",            1, ResidentType.RENTAL, "bakkies"),
            r("B06","Mnr & Me Steyn",           2, ResidentType.OWNER,  "bakkies"),
            r("B07","Mrs Lombard",              1, ResidentType.RENTAL, "bakkies"),
            r("B08","Mr & Mrs Venter",          2, ResidentType.OWNER,  "bakkies"),
            r("B09","Mev Coetzee",              1, ResidentType.OWNER,  "bakkies"),
            r("B10","Mnr & Mev Nel",            2, ResidentType.RENTAL, "bakkies"),
            r("B11","Me van der Merwe",         1, ResidentType.OWNER,  "bakkies"),
            r("B12","Mrs Bosman",               1, ResidentType.RENTAL, "bakkies"),
            r("B13","Mnr & Me Potgieter",       2, ResidentType.OWNER,  "bakkies"),
            r("B14","Me Louw",                  1, ResidentType.OTP,    "bakkies"),
            r("B15","Mr & Mrs Erasmus",         2, ResidentType.OWNER,  "bakkies"),
            r("B16","Mev Visser",               1, ResidentType.RENTAL, "bakkies"),
            r("B17","Me Theron",                1, ResidentType.OWNER,  "bakkies"),
            r("B18","Mnr & Mev Grobler",        2, ResidentType.OWNER,  "bakkies"),
            // Sunhill Retirement
            r("S01","Mr & Mrs Olivier",         2, ResidentType.OWNER,  "sunhill"),
            r("S02","Me Pietersen",             1, ResidentType.RENTAL, "sunhill"),
            r("S03","Mnr & Mev Kotze",          2, ResidentType.OWNER,  "sunhill"),
            r("S04","Mrs van Wyk",              1, ResidentType.OWNER,  "sunhill"),
            r("S05","Me Boshoff",               1, ResidentType.RENTAL, "sunhill"),
            r("S06","Mnr & Me Swart",           2, ResidentType.OWNER,  "sunhill"),
            r("S07","Mrs le Roux",              1, ResidentType.RENTAL, "sunhill"),
            r("S08","Mr & Mrs Human",           2, ResidentType.OWNER,  "sunhill"),
            r("S09","Mev Bekker",               1, ResidentType.OWNER,  "sunhill"),
            r("S10","Mnr & Mev Burger",         2, ResidentType.RENTAL, "sunhill"),
            r("S11","Me de Beer",               1, ResidentType.OWNER,  "sunhill"),
            r("S12","Mrs Brits",                1, ResidentType.RENTAL, "sunhill"),
            r("S13","Mnr & Me Marais",          2, ResidentType.OWNER,  "sunhill"),
            r("S14","Me Jansen",                1, ResidentType.OTP,    "sunhill"),
            r("S15","Mr & Mrs Pienaar",         2, ResidentType.OWNER,  "sunhill"),
            r("S16","Mev Schoeman",             1, ResidentType.RENTAL, "sunhill"),
            r("S17","Me Hugo",                  1, ResidentType.OWNER,  "sunhill"),
            r("S18","Mnr & Mev Barnard",        2, ResidentType.OWNER,  "sunhill"),
            r("S19","Mrs Nortje",               1, ResidentType.RENTAL, "sunhill"),
            r("S20","Me Cronje",                1, ResidentType.OWNER,  "sunhill"),
            r("S21","Mnr & Mev Smit",           2, ResidentType.OWNER,  "sunhill"),
            r("S22","Me van Niekerk",           1, ResidentType.RENTAL, "sunhill"),
            r("S23","Mrs Geldenhuys",           1, ResidentType.OWNER,  "sunhill"),
            r("S24","Mr & Mrs Roux",            2, ResidentType.OWNER,  "sunhill"),
            r("S25","Mev Liebenberg",           1, ResidentType.RENTAL, "sunhill"),
            r("S26","Me Engelbrecht",           1, ResidentType.OWNER,  "sunhill"),
            r("S27","Mnr & Me Viljoen",         2, ResidentType.OWNER,  "sunhill"),
            r("S28","Mrs Ferreira",             1, ResidentType.RENTAL, "sunhill"),
            r("S29","Me Lötter",                1, ResidentType.OWNER,  "sunhill"),
            r("S30","Mnr & Mev Scheepers",      2, ResidentType.OWNER,  "sunhill"),
            r("S31","Me Rademeyer",             1, ResidentType.OTP,    "sunhill")
        ).forEach(entity -> {
            residentRepository.save(entity);
            auditRepository.save(ResidentAuditEntity.builder()
                .siteId(entity.getSiteId())
                .unitNumber(entity.getUnitNumber())
                .action(AuditAction.CREATED)
                .actor("system")
                .at(now)
                .note("Seeded from SampleData")
                .build());
        });
    }

    private ResidentEntity r(String unit, String name, int occ, ResidentType type, String site) {
        long now = System.currentTimeMillis();
        return ResidentEntity.builder()
            .unitNumber(unit).clientName(name).totalOccupants(occ)
            .residentType(type).siteId(site).isActive(true)
            .createdBy("system").createdAt(now)
            .lastModifiedBy("system").lastModifiedAt(now)
            .build();
    }

    // ── January 2026 meal entries ──────────────────────────────────────────────

    private void seedJanuaryEntries(long now) {
        // Lizane entries
        entry("001","lizane",2026,1, Map.of("COURSE_1",4,"COURSE_2",2,"SUN_1_COURSE",3,"DINNER",4,"SOUP_DESSERT",6,"TA_BAKKIES",8), now);
        entry("002","lizane",2026,1, Map.of("COURSE_1",3,"COURSE_2",7,"TA_BAKKIES",2), now);
        entry("003","lizane",2026,1, Map.of("COURSE_1",21,"DINNER",1,"TA_BAKKIES",11), now);
        entry("004","lizane",2026,1, Map.of(), now);
        entry("005","lizane",2026,1, Map.of("COURSE_1",9,"SUN_1_COURSE",2,"BREAKFAST",1), now);
        entry("006","lizane",2026,1, Map.of("COURSE_1",4,"COURSE_2",2,"SUN_1_COURSE",2,"SOUP_DESSERT",2), now);
        entry("007","lizane",2026,1, Map.of("COURSE_1",15,"SUN_1_COURSE",4,"TA_BAKKIES",3), now);
        entry("008","lizane",2026,1, Map.of(), now);
        entry("009","lizane",2026,1, Map.of("COURSE_1",12,"COURSE_2",7,"SUN_1_COURSE",7,"SOUP_DESSERT",7,"TA_BAKKIES",10), now);
        entry("010","lizane",2026,1, Map.of("COURSE_1",6), now);
        entry("011","lizane",2026,1, Map.of("COURSE_1",18,"COURSE_2",4,"SUN_1_COURSE",7,"SOUP_DESSERT",2), now);
        entry("012","lizane",2026,1, Map.of("COURSE_1",7,"TA_BAKKIES",2), now);
        entry("013","lizane",2026,1, Map.of("COURSE_1",21,"TA_BAKKIES",1), now);
        entry("014","lizane",2026,1, Map.of("COURSE_1",3,"COURSE_2",4,"BREAKFAST",1), now);
        entry("015","lizane",2026,1, Map.of("COURSE_2",17,"SUN_1_COURSE",2,"SOUP_DESSERT",4), now);
        entry("016","lizane",2026,1, Map.of("COURSE_1",3,"COURSE_2",2,"TA_BAKKIES",1), now);
        entry("017","lizane",2026,1, Map.of("COURSE_1",3,"COURSE_2",2), now);
        entry("018","lizane",2026,1, Map.of("COURSE_1",2,"SUN_1_COURSE",2,"VISITOR_MON_SAT",1), now);
        entry("019","lizane",2026,1, Map.of(), now);
        entry("021","lizane",2026,1, Map.of("COURSE_1",4,"SUN_1_COURSE",1,"TA_BAKKIES",1), now);
        entry("022","lizane",2026,1, Map.of("COURSE_1",5,"TA_BAKKIES",1), now);
        entry("023","lizane",2026,1, Map.of("COURSE_1",5,"COURSE_2",2,"SUN_1_COURSE",1,"BREAKFAST",1,"TA_BAKKIES",1), now);
        entry("024","lizane",2026,1, Map.of(), now);
        // Bakkies entries
        entry("B01","bakkies",2026,1, Map.of("COURSE_1",8,"COURSE_2",4,"SUN_1_COURSE",3,"TA_BAKKIES",5), now);
        entry("B02","bakkies",2026,1, Map.of("COURSE_1",12,"COURSE_2",3,"TA_BAKKIES",2), now);
        entry("B03","bakkies",2026,1, Map.of("COURSE_1",18,"SUN_1_COURSE",4,"DINNER",2), now);
        entry("B04","bakkies",2026,1, Map.of("COURSE_1",7,"COURSE_2",6), now);
        entry("B05","bakkies",2026,1, Map.of("COURSE_1",10,"BREAKFAST",2,"TA_BAKKIES",3), now);
        entry("B06","bakkies",2026,1, Map.of("COURSE_1",5,"COURSE_2",8,"SUN_1_COURSE",2,"SOUP_DESSERT",3), now);
        entry("B07","bakkies",2026,1, Map.of("COURSE_1",14,"SUN_1_COURSE",3,"TA_BAKKIES",1), now);
        entry("B08","bakkies",2026,1, Map.of(), now);
        entry("B09","bakkies",2026,1, Map.of("COURSE_1",9,"COURSE_2",5,"SUN_1_COURSE",4,"TA_BAKKIES",6), now);
        entry("B10","bakkies",2026,1, Map.of("COURSE_1",6,"DINNER",1), now);
        entry("B11","bakkies",2026,1, Map.of("COURSE_1",16,"COURSE_2",2,"SUN_1_COURSE",5), now);
        entry("B12","bakkies",2026,1, Map.of("COURSE_1",4,"TA_BAKKIES",1), now);
        entry("B13","bakkies",2026,1, Map.of("COURSE_1",19,"SUN_1_COURSE",3,"TA_BAKKIES",2), now);
        entry("B14","bakkies",2026,1, Map.of("COURSE_1",2,"COURSE_2",3,"BREAKFAST",1), now);
        entry("B15","bakkies",2026,1, Map.of("COURSE_2",14,"SUN_1_COURSE",2,"SOUP_DESSERT",3), now);
        entry("B16","bakkies",2026,1, Map.of("COURSE_1",3,"COURSE_2",1,"TA_BAKKIES",1), now);
        entry("B17","bakkies",2026,1, Map.of("COURSE_1",11,"COURSE_2",3), now);
        entry("B18","bakkies",2026,1, Map.of("COURSE_1",3,"SUN_1_COURSE",2,"TA_BAKKIES",1), now);
        // Sunhill entries
        entry("S01","sunhill",2026,1, Map.of("COURSE_1",6,"COURSE_2",3,"SUN_1_COURSE",2,"TA_BAKKIES",4), now);
        entry("S02","sunhill",2026,1, Map.of("COURSE_1",10,"COURSE_2",5,"TA_BAKKIES",3), now);
        entry("S03","sunhill",2026,1, Map.of("COURSE_1",15,"DINNER",2,"TA_BAKKIES",7), now);
        entry("S04","sunhill",2026,1, Map.of(), now);
        entry("S05","sunhill",2026,1, Map.of("COURSE_1",8,"SUN_1_COURSE",3,"BREAKFAST",1), now);
        entry("S06","sunhill",2026,1, Map.of("COURSE_1",3,"COURSE_2",4,"SUN_1_COURSE",2,"SOUP_DESSERT",2), now);
        entry("S07","sunhill",2026,1, Map.of("COURSE_1",13,"SUN_1_COURSE",5,"TA_BAKKIES",2), now);
        entry("S08","sunhill",2026,1, Map.of("COURSE_1",1), now);
        entry("S09","sunhill",2026,1, Map.of("COURSE_1",11,"COURSE_2",6,"SUN_1_COURSE",4,"SOUP_DESSERT",5,"TA_BAKKIES",8), now);
        entry("S10","sunhill",2026,1, Map.of("COURSE_1",5,"DINNER",1), now);
        entry("S11","sunhill",2026,1, Map.of("COURSE_1",17,"COURSE_2",3,"SUN_1_COURSE",6,"SOUP_DESSERT",1), now);
        entry("S12","sunhill",2026,1, Map.of("COURSE_1",6,"TA_BAKKIES",2), now);
        entry("S13","sunhill",2026,1, Map.of("COURSE_1",20,"TA_BAKKIES",3), now);
        entry("S14","sunhill",2026,1, Map.of("COURSE_1",2,"COURSE_2",5,"BREAKFAST",1), now);
        entry("S15","sunhill",2026,1, Map.of("COURSE_2",15,"SUN_1_COURSE",3,"SOUP_DESSERT",5), now);
        entry("S16","sunhill",2026,1, Map.of("COURSE_1",4,"COURSE_2",2,"TA_BAKKIES",1), now);
        entry("S17","sunhill",2026,1, Map.of("COURSE_1",4,"COURSE_2",3), now);
        entry("S18","sunhill",2026,1, Map.of("COURSE_1",3,"SUN_1_COURSE",2,"VISITOR_MON_SAT",1), now);
        entry("S19","sunhill",2026,1, Map.of(), now);
        entry("S20","sunhill",2026,1, Map.of("COURSE_1",5,"SUN_1_COURSE",2,"TA_BAKKIES",1), now);
        entry("S21","sunhill",2026,1, Map.of("COURSE_1",7,"TA_BAKKIES",2), now);
        entry("S22","sunhill",2026,1, Map.of("COURSE_1",6,"COURSE_2",3,"SUN_1_COURSE",1,"BREAKFAST",1,"TA_BAKKIES",1), now);
        entry("S23","sunhill",2026,1, Map.of(), now);
        entry("S24","sunhill",2026,1, Map.of("COURSE_1",9,"COURSE_2",2,"SUN_1_COURSE",3), now);
        entry("S25","sunhill",2026,1, Map.of("COURSE_1",4,"DINNER",1,"TA_BAKKIES",2), now);
        entry("S26","sunhill",2026,1, Map.of("COURSE_1",8,"COURSE_2",4), now);
        entry("S27","sunhill",2026,1, Map.of("COURSE_1",12,"SUN_1_COURSE",2,"TA_BAKKIES",3), now);
        entry("S28","sunhill",2026,1, Map.of("COURSE_1",3,"COURSE_2",2,"SOUP_DESSERT",1), now);
        entry("S29","sunhill",2026,1, Map.of("COURSE_1",5,"SUN_1_COURSE",1), now);
        entry("S30","sunhill",2026,1, Map.of("COURSE_1",10,"COURSE_2",3,"TA_BAKKIES",4), now);
        entry("S31","sunhill",2026,1, Map.of("COURSE_1",2,"BREAKFAST",1), now);
    }

    private void entry(String unit, String site, int year, int month,
                       Map<String,Integer> counts, long now) {
        try {
            entryRepository.save(MealEntryEntity.builder()
                .siteId(site).unitNumber(unit).year(year).month(month)
                .countsJson(objectMapper.writeValueAsString(counts))
                .lastModifiedAt(now).lastModifiedBy("system")
                .build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize counts for " + unit, e);
        }
    }

    // ── Pricing configs (current + next month, all sites) ──────────────────────

    private void seedPricingConfigs(long now) {
        Calendar cal       = Calendar.getInstance();
        int thisYear       = cal.get(Calendar.YEAR);
        int thisMonth      = cal.get(Calendar.MONTH) + 1;
        int nextMonth      = thisMonth == 12 ? 1 : thisMonth + 1;
        int nextYear       = thisMonth == 12 ? thisYear + 1 : thisYear;

        // Also seed January 2026 so the sample entries have matching pricing
        seedPricingForMonth("lizane",  2026, 1, lizanePricing(),   now);
        seedPricingForMonth("bakkies", 2026, 1, bakkiesPricing(),  now);
        seedPricingForMonth("sunhill", 2026, 1, sunhillPricing(),  now);

        for (int[] ym : new int[][]{{thisYear, thisMonth}, {nextYear, nextMonth}}) {
            seedPricingForMonth("lizane",  ym[0], ym[1], lizanePricing(),  now);
            seedPricingForMonth("bakkies", ym[0], ym[1], bakkiesPricing(), now);
            seedPricingForMonth("sunhill", ym[0], ym[1], sunhillPricing(), now);
        }
    }

    private void seedPricingForMonth(String siteId, int year, int month,
                                     double[] p, long now) {
        // Skip if already exists (protects re-runs)
        if (pricingRepository.findBySiteIdAndYearAndMonth(siteId, year, month).isPresent()) return;
        pricingRepository.save(MealPricingEntity.builder()
            .siteId(siteId).year(year).month(month)
            .course1(p[0]).course2(p[1]).course3(p[2]).fullBoard(p[3])
            .sun1Course(p[4]).sun3Course(p[5]).breakfast(p[6]).dinner(p[7])
            .soupDessert(p[8]).visitorMonSat(p[9]).visitorSun1(p[10]).visitorSun3(p[11])
            .taBakkies(p[12]).vatRate(p[13]).compulsoryMealsDeduction(p[14])
            .lastModifiedAt(now).lastModifiedBy("system")
            .lastSyncedAt(now)
            .build());
    }

    private double[] lizanePricing() {
        return new double[]{35.652174,42.608696,54.782609,81.739130,
            53.913043,68.695652,23.913043,27.391304,
            13.043478,45.217391,63.478261,76.521739,5.0,0.15,246.0};
    }
    private double[] bakkiesPricing() {
        return new double[]{32.173913,39.130435,50.434783,75.652174,
            49.565217,63.478261,21.739130,25.217391,
            11.739130,41.304348,58.260870,70.434783,5.0,0.15,246.0};
    }
    private double[] sunhillPricing() {
        return new double[]{38.260870,46.086957,59.130435,87.826087,
            57.391304,73.913043,26.086957,30.434783,
            14.347826,48.695652,68.260870,82.608696,5.0,0.15,246.0};
    }
}
