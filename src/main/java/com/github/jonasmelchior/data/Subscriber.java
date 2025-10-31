package com.github.jonasmelchior.data;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Entity
@Table(name = "subscriber")
public class Subscriber extends PanacheEntity {

    // --- Identification ---
    @Column(unique = true)
    public String imsi;

    @Column(unique = true)
    public String msisdn;

    private String imei;

    // --- Authentication ---
    public String kKey;      // renamed from 'K' to avoid keyword conflict
    public String opc;
    public String amf;
    public long sqn;

    // --- Subscription Data ---
    public Integer subscriberStatus;
    public Integer accessRestrictionData;
    public Integer networkAccessMode;

    public Integer ambrUplink;
    public Integer ambrDownlink;


    @ElementCollection
    @CollectionTable(
            name = "subscriber_static_ip_apn_map",
            joinColumns = @JoinColumn(name = "subscriber_id")
    )
    @MapKeyColumn(name = "apn")
    @Column(name = "ip_address")
    public Map<String, String> staticIpApnMap = new HashMap<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "subscriber_apn_profiles",
            joinColumns = @JoinColumn(name = "subscriber_id"),
            inverseJoinColumns = @JoinColumn(name = "apn_profile_id")
    )
    public List<APNProfile> apnProfiles = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "subscriber_apn_context_map",
            joinColumns = @JoinColumn(name = "subscriber_id")
    )
    @MapKeyColumn(name = "context_id")
    @Column(name = "apn_name")
    public Map<Integer, String> apnContextMap = new HashMap<>();

    public Instant lastUpdated = Instant.now();

    public String servingMMERealm;
    public String servingMMEHost;
    public Boolean purged = false;

    @ManyToOne(cascade = CascadeType.ALL)
    public APNProfile defaultApn;

    public Subscriber() {
    }

    public Subscriber(String imsi, String msisdn, String kKey, String opc, String amf, long sqn, List<APNProfile> apnProfiles) {
        this.imsi = imsi;
        this.msisdn = msisdn;
        this.kKey = kKey;
        this.opc = opc;
        this.amf = amf;
        this.sqn = sqn;
        this.apnProfiles = apnProfiles;
    }

    public String getkKey() {
        return kKey;
    }

    public void setkKey(String kKey) {
        this.kKey = kKey;
    }

    public Boolean getPurged() {
        return purged;
    }

    public void setPurged(Boolean purged) {
        this.purged = purged;
    }

    public void setSubscriberStatus(Integer subscriberStatus) {
        this.subscriberStatus = subscriberStatus;
    }

    public void setAccessRestrictionData(Integer accessRestrictionData) {
        this.accessRestrictionData = accessRestrictionData;
    }

    public void setNetworkAccessMode(Integer networkAccessMode) {
        this.networkAccessMode = networkAccessMode;
    }
    public Map<String, String> getStaticIpApnMap() {
        return staticIpApnMap;
    }

    public void setStaticIpApnMap(Map<String, String> staticIpApnMap) {
        this.staticIpApnMap = staticIpApnMap;
    }

    public APNProfile getDefaultApn() {
        return defaultApn;
    }

    public void setDefaultApn(APNProfile defaultApn) {
        this.defaultApn = defaultApn;
    }

    public int getNetworkAccessMode() {
        return networkAccessMode;
    }

    public void setNetworkAccessMode(int networkAccessMode) {
        this.networkAccessMode = networkAccessMode;
    }

    public List<APNProfile> getApnProfiles() {
        return apnProfiles;
    }

    public void setApnProfiles(List<APNProfile> apnProfiles) {
        this.apnProfiles = apnProfiles;
    }

    public Map<Integer, String> getApnContextMap() {
        return apnContextMap;
    }

    public void setApnContextMap(Map<Integer, String> apnContextMap) {
        this.apnContextMap = apnContextMap;
    }

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getK() {
        return kKey;
    }

    public void setK(String kKey) {
        this.kKey = kKey;
    }

    public String getOpc() {
        return opc;
    }

    public void setOpc(String opc) {
        this.opc = opc;
    }

    public String getAmf() {
        return amf;
    }

    public void setAmf(String amf) {
        this.amf = amf;
    }

    public long getSqn() {
        return sqn;
    }

    public void setSqn(long sqn) {
        this.sqn = sqn;
    }

    public int getSubscriberStatus() {
        return subscriberStatus;
    }

    public void setSubscriberStatus(int subscriberStatus) {
        this.subscriberStatus = subscriberStatus;
    }

    public int getAccessRestrictionData() {
        return accessRestrictionData;
    }

    public void setAccessRestrictionData(int accessRestrictionData) {
        this.accessRestrictionData = accessRestrictionData;
    }

    public Integer getAmbrUplink() {
        return ambrUplink;
    }

    public void setAmbrUplink(Integer ambrUplink) {
        this.ambrUplink = ambrUplink;
    }

    public Integer getAmbrDownlink() {
        return ambrDownlink;
    }

    public void setAmbrDownlink(Integer ambrDownlink) {
        this.ambrDownlink = ambrDownlink;
    }

    public List<APNProfile> getApns() {
        return apnProfiles;
    }

    public void setApns(List<APNProfile> apns) {
        this.apnProfiles = apnProfiles;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getServingMMERealm() {
        return servingMMERealm;
    }

    public void setServingMMERealm(String servingMMERealm) {
        this.servingMMERealm = servingMMERealm;
    }

    public String getServingMMEHost() {
        return servingMMEHost;
    }

    public void setServingMMEHost(String servingMMEHost) {
        this.servingMMEHost = servingMMEHost;
    }

    @Override
    public String toString() {
        return "Subscriber{" +
                "imsi='" + imsi + '\'' +
                ", msisdn='" + msisdn + '\'' +
                ", kKey='" + kKey + '\'' +
                ", opc='" + opc + '\'' +
                ", amf='" + amf + '\'' +
                ", sqn=" + sqn +
                ", subscriberStatus=" + subscriberStatus +
                ", accessRestrictionData=" + accessRestrictionData +
                ", networkAccessMode=" + networkAccessMode +
                ", ambrUplink=" + ambrUplink +
                ", ambrDownlink=" + ambrDownlink +
                ", staticIpApnMap=" + staticIpApnMap +
                ", apnProfiles=" + apnProfiles +
                ", apnContextMap=" + apnContextMap +
                ", lastUpdated=" + lastUpdated +
                ", servingMMERealm='" + servingMMERealm + '\'' +
                ", servingMMEHost='" + servingMMEHost + '\'' +
                ", purged=" + purged +
                ", defaultApn=" + defaultApn +
                '}';
    }
}
