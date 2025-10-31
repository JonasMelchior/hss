package com.github.jonasmelchior.service.avp;

import com.github.jonasmelchior.data.APNProfile;
import com.github.jonasmelchior.data.Subscriber;
import org.jdiameter.api.AvpSet;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

public class SubscriberAvpTranslator {
    public static void populateSubscriptionData(AvpSet subscriptionDataAvpSet, Subscriber subscriber) throws UnknownHostException {
        subscriptionDataAvpSet.addAvp(1424, subscriber.getSubscriberStatus(), 10415, false, false); // SERVICE_GRANTED (0)
        subscriptionDataAvpSet.addAvp(701, subscriber.getMsisdn().getBytes(StandardCharsets.UTF_8), 10415, false, false);
        //subscriptionData.addAvp(1426, subscriber.getAccessRestrictionData(), 10415, false, false);
        subscriptionDataAvpSet.addAvp(1417, subscriber.getNetworkAccessMode(), 10415, false, false);

        // AMBR (AVP 1435)
        AvpSet ambr = subscriptionDataAvpSet.addGroupedAvp(1435, 10415, false, false);
        ambr.addAvp(516, subscriber.getAmbrUplink(), 10415, false, false); // Max-Requested-Bandwidth-UL
        ambr.addAvp(515, subscriber.getAmbrDownlink(), 10415, false, false); // Max-Requested-Bandwidth-DL

        // APN-Configuration-Profile (AVP 1429)
        AvpSet apnProfile = subscriptionDataAvpSet.addGroupedAvp(1429, 10415, false, false);
        // Always add all APN's
        apnProfile.addAvp(1428, 0, 10415, false, false); // All-APN-Configurations-Included-Indicator

        // Add at least one APN Configuration (Grouped AVP 1430)
        AvpSet apnConfig = apnProfile.addGroupedAvp(1430, 10415, false, false);
        int contextId = 1;
        for (APNProfile apn : subscriber.getApns()) {
            if (subscriber.getDefaultApn().getApnName().equals(apn.getApnName())) {
                subscriptionDataAvpSet.addAvp(1423, contextId, 10415, false, false); // Context-Identifier
            }

            // --- APN-Configuration (1430) ---
            apnConfig.addAvp(493, apn.getApnName().getBytes(StandardCharsets.UTF_8), false, false); // Service-Selection
            //TODO: Maybe need to be modified to support multiple APN configurations for same APN name
            Optional<Map.Entry<Integer, String>> apnContext = subscriber.getApnContextMap().entrySet()
                    .stream().filter( apnContext1 -> apnContext1.getValue().equals(apn.getApnName()))
                    .findFirst();
            if (apnContext.isPresent()) {
                apnConfig.addAvp(1423, apnContext.get().getKey(), 10415, false, false);
                contextId = apnContext.get().getKey() + 1;
            }
            else {
                apnConfig.addAvp(1423, contextId++, 10415, false, false); // Context-Identifier
            }
            apnConfig.addAvp(1456, apn.getPdnType(), 10415, false, false); // PDN-Type
            if (apn.getPdnGwAllocationType() != null) {
                apnConfig.addAvp(1438, apn.getPdnGwAllocationType(), 10415, false, false); // PDN-GW-Allocation-Type
                if (apn.getPdnGwAllocationType() == 0) { // STATIC
                    AvpSet mip6Avp = apnConfig.addGroupedAvp(486, false, false); // MIP6-Agent-Info

                    if (apn.getPdnGwIP() != null) {
                        mip6Avp.addAvp(334, InetAddress.getByName(apn.getPdnGwIP()), false, false); // MIP-Home-Agent-Address
                    }

                }
            }
            if (subscriber.getStaticIpApnMap().get(apn.getApnName()) != null) {
                apnConfig.addAvp(848, InetAddress.getByName(subscriber.getStaticIpApnMap().get(apn.getApnName())), 10415, false, false);
            }

            // --- EPS-Subscribed-QoS-Profile (1431) ---
            if (apn.getQci() != null) {
                AvpSet qosAvp = apnConfig.addGroupedAvp(1431, 10415, false, false);

                qosAvp.addAvp(1028, apn.getQci(), 10415, false, false); // QCI

                // --- ARP (1034) ---
                if (apn.getPriorityLevel() != null || apn.getPreemptionCapability() != null || apn.getPreemptionVulnerability() != null) {
                    AvpSet arpAvp = qosAvp.addGroupedAvp(1034, 10415, false, false);

                    if (apn.getPriorityLevel() != null)
                        arpAvp.addAvp(1046, apn.getPriorityLevel(), 10415, false, false);

                    if (apn.getPreemptionCapability() != null)
                        arpAvp.addAvp(1047, apn.getPreemptionCapability() ? 1 : 0, 10415, false, false);

                    if (apn.getPreemptionVulnerability() != null)
                        arpAvp.addAvp(1048, apn.getPreemptionVulnerability() ? 1 : 0, 10415, false, false);
                }
            }

            // --- AMBR (1435) ---
            if (apn.getAmbrUL() != null || apn.getAmbrDL() != null) {
                AvpSet ambrAvp = apnConfig.addGroupedAvp(1435, 10415, false, false);

                if (apn.getAmbrUL() != null)
                    ambrAvp.addAvp(516, apn.getAmbrUL(), 10415, false, false);

                if (apn.getAmbrDL() != null)
                    ambrAvp.addAvp(515, apn.getAmbrDL(), 10415, false, false);
            }
        }
    }
}
