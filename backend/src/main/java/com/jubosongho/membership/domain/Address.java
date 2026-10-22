package com.jubosongho.membership.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * ঠিকানা। id-গুলো ড্রপডাউন থেকে আসে; নামগুলো সার্ভারে পূরণ করা হয়
 * যাতে তালিকা দেখানো ও সার্চ করা সহজ হয়।
 */
@Embeddable
public class Address {

    private Long divisionId;
    @Column(length = 120)
    private String divisionName;

    private Long districtId;
    @Column(length = 120)
    private String districtName;

    private Long upazilaId;
    @Column(length = 120)
    private String upazilaName;

    private Long unionId;
    @Column(length = 120)
    private String unionName;

    private Integer ward;

    @Column(length = 120)
    private String postOffice;

    @Column(length = 255)
    private String village;

    public Address copy() {
        Address a = new Address();
        a.divisionId = divisionId;
        a.divisionName = divisionName;
        a.districtId = districtId;
        a.districtName = districtName;
        a.upazilaId = upazilaId;
        a.upazilaName = upazilaName;
        a.unionId = unionId;
        a.unionName = unionName;
        a.ward = ward;
        a.postOffice = postOffice;
        a.village = village;
        return a;
    }

    /** সংক্ষিপ্ত ঠিকানা: গ্রাম, ইউনিয়ন, উপযেলা, যেলা */
    public String toShortText() {
        StringBuilder sb = new StringBuilder();
        for (String part : new String[]{village, unionName, upazilaName, districtName}) {
            if (part != null && !part.isBlank()) {
                if (!sb.isEmpty()) sb.append(", ");
                sb.append(part);
            }
        }
        return sb.toString();
    }

    public Long getDivisionId() { return divisionId; }
    public void setDivisionId(Long divisionId) { this.divisionId = divisionId; }
    public String getDivisionName() { return divisionName; }
    public void setDivisionName(String divisionName) { this.divisionName = divisionName; }
    public Long getDistrictId() { return districtId; }
    public void setDistrictId(Long districtId) { this.districtId = districtId; }
    public String getDistrictName() { return districtName; }
    public void setDistrictName(String districtName) { this.districtName = districtName; }
    public Long getUpazilaId() { return upazilaId; }
    public void setUpazilaId(Long upazilaId) { this.upazilaId = upazilaId; }
    public String getUpazilaName() { return upazilaName; }
    public void setUpazilaName(String upazilaName) { this.upazilaName = upazilaName; }
    public Long getUnionId() { return unionId; }
    public void setUnionId(Long unionId) { this.unionId = unionId; }
    public String getUnionName() { return unionName; }
    public void setUnionName(String unionName) { this.unionName = unionName; }
    public Integer getWard() { return ward; }
    public void setWard(Integer ward) { this.ward = ward; }
    public String getPostOffice() { return postOffice; }
    public void setPostOffice(String postOffice) { this.postOffice = postOffice; }
    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }
}
