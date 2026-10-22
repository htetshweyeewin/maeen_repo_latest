package com.jubosongho.membership.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jubosongho.membership.domain.*;
import com.jubosongho.membership.repository.AdminUserRepository;
import com.jubosongho.membership.repository.GeoAreaRepository;
import com.jubosongho.membership.repository.OrgZilaRepository;
import com.jubosongho.membership.service.GeoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * প্রথমবার চালু হলে:
 * ১. বাংলাদেশের বিভাগ/যেলা/উপযেলা/ইউনিয়ন লোড (geo/bd-geo.json)
 * ২. কেন্দ্রীয় এডমিন তৈরি
 * ৩. নমুনা সাংগঠনিক যেলা "ময়মনসিংহ দক্ষিণ" ও তার এডমিন তৈরি
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    /** ময়মনসিংহ দক্ষিণ যেলার অধীনস্থ উপযেলা (bd-geo.json এর id): সদর, ত্রিশাল, ফুলবাড়িয়া, ভালুকা, গফরগাঁও, মুক্তাগাছা */
    private static final long[] MYMENSINGH_SOUTH_UPAZILAS = {466, 463, 462, 464, 471, 465};

    private final GeoAreaRepository geoRepo;
    private final GeoService geoService;
    private final AdminUserRepository users;
    private final OrgZilaRepository zilas;
    private final PasswordEncoder encoder;
    private final ObjectMapper mapper;
    private final AppProperties props;

    public DataInitializer(GeoAreaRepository geoRepo, GeoService geoService, AdminUserRepository users,
                           OrgZilaRepository zilas, PasswordEncoder encoder, ObjectMapper mapper, AppProperties props) {
        this.geoRepo = geoRepo;
        this.geoService = geoService;
        this.users = users;
        this.zilas = zilas;
        this.encoder = encoder;
        this.mapper = mapper;
        this.props = props;
    }

    record GeoItem(long id, Long parentId, String name) {
    }

    record GeoFile(List<GeoItem> divisions, List<GeoItem> districts, List<GeoItem> upazilas, List<GeoItem> unions) {
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws IOException {
        seedGeo();
        geoService.reload();
        seedSuperAdmin();
        if (props.bootstrap().seedSampleZila()) seedSampleZila();
    }

    private void seedGeo() throws IOException {
        if (geoRepo.count() > 0) return;
        GeoFile file;
        try (InputStream in = new ClassPathResource("geo/bd-geo.json").getInputStream()) {
            file = mapper.readValue(in, GeoFile.class);
        }
        List<GeoArea> all = new ArrayList<>();
        file.divisions().forEach(i -> all.add(new GeoArea(GeoLevel.DIVISION, i.id(), null, i.name())));
        file.districts().forEach(i -> all.add(new GeoArea(GeoLevel.DISTRICT, i.id(), i.parentId(), i.name())));
        file.upazilas().forEach(i -> all.add(new GeoArea(GeoLevel.UPAZILA, i.id(), i.parentId(), i.name())));
        file.unions().forEach(i -> all.add(new GeoArea(GeoLevel.UNION, i.id(), i.parentId(), i.name())));
        geoRepo.saveAll(all);
        geoRepo.flush();
        log.info("ঠিকানা ডাটা লোড হয়েছে: {} টি রেকর্ড", all.size());
    }

    private void seedSuperAdmin() {
        String username = props.bootstrap().superAdminUsername();
        if (users.existsByUsername(username)) return;
        AdminUser u = new AdminUser();
        u.setUsername(username);
        u.setFullName("কেন্দ্রীয় এডমিন");
        u.setRole(Role.SUPER_ADMIN);
        u.setPasswordHash(encoder.encode(props.bootstrap().superAdminPassword()));
        users.save(u);
        log.warn("কেন্দ্রীয় এডমিন তৈরি হয়েছে (ইউজারনেম: {}) - লগইন করে অবিলম্বে পাসওয়ার্ড পরিবর্তন করুন", username);
    }

    private void seedSampleZila() {
        String name = "ময়মনসিংহ দক্ষিণ";
        if (zilas.findByName(name).isPresent()) return;
        OrgZila zila = new OrgZila();
        zila.setName(name);
        for (long id : MYMENSINGH_SOUTH_UPAZILAS) zila.getUpazilaIds().add(GeoArea.keyOf(GeoLevel.UPAZILA, id));
        zilas.save(zila);

        String username = "mymensingh-south";
        if (!users.existsByUsername(username)) {
            AdminUser u = new AdminUser();
            u.setUsername(username);
            u.setFullName("ময়মনসিংহ দক্ষিণ যেলা এডমিন");
            u.setRole(Role.ZILA_ADMIN);
            u.setOrgZila(zila);
            u.setPasswordHash(encoder.encode(props.bootstrap().sampleZilaAdminPassword()));
            users.save(u);
            log.warn("নমুনা যেলা এডমিন তৈরি হয়েছে (ইউজারনেম: {}) - পাসওয়ার্ড পরিবর্তন করুন", username);
        }
    }
}
