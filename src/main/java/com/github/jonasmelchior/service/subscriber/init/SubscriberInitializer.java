package com.github.jonasmelchior.service.subscriber.init;

import com.github.jonasmelchior.data.APNProfile;
import com.github.jonasmelchior.data.Subscriber;
import com.github.jonasmelchior.repository.APNProfileRepository;
import com.github.jonasmelchior.repository.SubscriberRepository;

import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
@Startup
@IfBuildProfile("dev")
public class SubscriberInitializer {
    @Inject
    SubscriberRepository subscriberRepository;

    @Inject
    APNProfileRepository apnProfileRepository;

    // This runs at startup
    void onStart(@Observes StartupEvent ev) {
        Log.info("Initializing dev database...");
        insertTestSubscriber();
    }

    @Transactional
    void insertTestSubscriber() {
        if (subscriberRepository.findByImsi("310150123456789") != null) {
            Log.info("Subscriber already exists, skipping insert");
            return;
        }

        Subscriber subscriber = new Subscriber();
        subscriber.imsi = "310150123456789";
        subscriber.msisdn = "15551234567";
        subscriber.kKey = "465B5CE8B199B49FAA5F0A2EE238A6BC";
        subscriber.opc = "E8ED289DEBA952E4283B54E88E6183CA";
        subscriber.amf = "8000";
        subscriber.sqn = 12345L;

        subscriber.subscriberStatus = 0;
        subscriber.accessRestrictionData = 0;
        subscriber.networkAccessMode = 2;

        subscriber.ambrUplink = 50_000_000;
        subscriber.ambrDownlink = 100_000_000;

        // staticIpApnMap
        Map<String, String> staticIpMap = new HashMap<>();
        staticIpMap.put("internet", "10.1.2.3");
        staticIpMap.put("ims", "10.1.2.4");
        subscriber.staticIpApnMap = staticIpMap;

        // apnProfiles
        APNProfile internetProfile = new APNProfile();
        internetProfile.apnName = "internet";
        internetProfile.pdnType = 0;
        internetProfile.pdnGwAllocationType = 1;
        internetProfile.pdnGwIP = null;
        internetProfile.pdnGwHost = null;
        internetProfile.qci = 9;
        internetProfile.priorityLevel = 8;
        internetProfile.preemptionCapability = true;
        internetProfile.preemptionVulnerability = false;
        internetProfile.ambrUL = 50_000_000;
        internetProfile.ambrDL = 100_000_000;

        APNProfile imsProfile = new APNProfile();
        imsProfile.apnName = "ims";
        imsProfile.pdnType = 0;
        imsProfile.pdnGwAllocationType = 0;
        imsProfile.pdnGwIP = "10.42.8.60";
        imsProfile.pdnGwHost = "pgw1.operator.com";
        imsProfile.qci = 5;
        imsProfile.priorityLevel = 1;
        imsProfile.preemptionCapability = true;
        imsProfile.preemptionVulnerability = false;
        imsProfile.ambrUL = 20_000_000;
        imsProfile.ambrDL = 50_000_000;

        apnProfileRepository.persist(internetProfile);
        apnProfileRepository.persist(imsProfile);

        subscriber.apnProfiles = new ArrayList<>();
        subscriber.apnProfiles.add(internetProfile);
        subscriber.apnProfiles.add(imsProfile);

        // apnContextMap
        Map<Integer, String> apnContextMap = new HashMap<>();
        apnContextMap.put(1, "internet");
        apnContextMap.put(2, "ims");
        subscriber.apnContextMap = apnContextMap;

        // defaultApn
        subscriber.defaultApn = internetProfile;

        // lastUpdated
        subscriber.lastUpdated = Instant.parse("2025-10-29T10:15:30Z");

        subscriberRepository.persist(subscriber);
        Log.info("Test subscriber inserted successfully");
    }
}
