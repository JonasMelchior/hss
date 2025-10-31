package com.github.jonasmelchior.data;


import jakarta.persistence.*;

@Entity
@Table(name = "apn_profile")
public class APNProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String apnName;

    public Integer pdnType = 0;
    public Integer pdnGwAllocationType = 1;
    public String pdnGwIP;
    public String pdnGwHost;

    public Integer qci;
    public Integer priorityLevel;
    public Boolean preemptionCapability;
    public Boolean preemptionVulnerability;

    public Integer ambrUL;
    public Integer ambrDL;
    public APNProfile() {
    }


    public String getPdnGwIP() {
        return pdnGwIP;
    }

    public void setPdnGwIP(String pdnGwIP) {
        this.pdnGwIP = pdnGwIP;
    }

    public String getApnName() {
        return apnName;
    }

    public void setApnName(String apnName) {
        this.apnName = apnName;
    }

    public Integer getPdnType() {
        return pdnType;
    }

    public void setPdnType(Integer pdnType) {
        this.pdnType = pdnType;
    }

    public Integer getPdnGwAllocationType() {
        return pdnGwAllocationType;
    }

    public void setPdnGwAllocationType(Integer pdnGwAllocationType) {
        this.pdnGwAllocationType = pdnGwAllocationType;
    }

    public String getPdnGwHost() {
        return pdnGwHost;
    }

    public void setPdnGwHost(String pdnGwHost) {
        this.pdnGwHost = pdnGwHost;
    }

    public Integer getQci() {
        return qci;
    }

    public void setQci(Integer qci) {
        this.qci = qci;
    }

    public Integer getPriorityLevel() {
        return priorityLevel;
    }

    public void setPriorityLevel(Integer priorityLevel) {
        this.priorityLevel = priorityLevel;
    }

    public Boolean getPreemptionCapability() {
        return preemptionCapability;
    }

    public void setPreemptionCapability(Boolean preemptionCapability) {
        this.preemptionCapability = preemptionCapability;
    }

    public Boolean getPreemptionVulnerability() {
        return preemptionVulnerability;
    }

    public void setPreemptionVulnerability(Boolean preemptionVulnerability) {
        this.preemptionVulnerability = preemptionVulnerability;
    }

    public Integer getAmbrUL() {
        return ambrUL;
    }

    public void setAmbrUL(Integer ambrUL) {
        this.ambrUL = ambrUL;
    }

    public Integer getAmbrDL() {
        return ambrDL;
    }

    public void setAmbrDL(Integer ambrDL) {
        this.ambrDL = ambrDL;
    }

    @Override
    public String toString() {
        return "APNProfile{" +
                "apnName='" + apnName + '\'' +
                ", pdnType=" + pdnType +
                ", pdnGwAllocationType=" + pdnGwAllocationType +
                ", pdnGwID='" + pdnGwHost + '\'' +
                ", qci=" + qci +
                ", priorityLevel=" + priorityLevel +
                ", preemptionCapability=" + preemptionCapability +
                ", preemptionVulnerability=" + preemptionVulnerability +
                ", ambrUL=" + ambrUL +
                ", ambrDL=" + ambrDL +
                '}';
    }
}
