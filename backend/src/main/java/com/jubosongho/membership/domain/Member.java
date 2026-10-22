package com.jubosongho.membership.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "member", indexes = {
        @Index(name = "idx_member_status", columnList = "status"),
        @Index(name = "idx_member_zila", columnList = "org_zila_id"),
        @Index(name = "idx_member_mobile1", columnList = "mobile1")
})
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 120)
    private String fatherName;

    @Column(nullable = false, length = 120)
    private String motherName;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private BloodGroup bloodGroup;

    @Column(length = 200)
    private String education;

    @Column(length = 150)
    private String email;

    @Column(nullable = false, length = 20)
    private String mobile1;
    private boolean mobile1Call;
    private boolean mobile1WhatsApp;

    @Column(length = 20)
    private String mobile2;
    private boolean mobile2Call;
    private boolean mobile2WhatsApp;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "divisionId", column = @Column(name = "perm_division_id")),
            @AttributeOverride(name = "divisionName", column = @Column(name = "perm_division_name", length = 120)),
            @AttributeOverride(name = "districtId", column = @Column(name = "perm_district_id")),
            @AttributeOverride(name = "districtName", column = @Column(name = "perm_district_name", length = 120)),
            @AttributeOverride(name = "upazilaId", column = @Column(name = "perm_upazila_id")),
            @AttributeOverride(name = "upazilaName", column = @Column(name = "perm_upazila_name", length = 120)),
            @AttributeOverride(name = "unionId", column = @Column(name = "perm_union_id")),
            @AttributeOverride(name = "unionName", column = @Column(name = "perm_union_name", length = 120)),
            @AttributeOverride(name = "ward", column = @Column(name = "perm_ward")),
            @AttributeOverride(name = "postOffice", column = @Column(name = "perm_post_office", length = 120)),
            @AttributeOverride(name = "village", column = @Column(name = "perm_village"))
    })
    private Address permanentAddress = new Address();

    private boolean presentSameAsPermanent;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "divisionId", column = @Column(name = "pres_division_id")),
            @AttributeOverride(name = "divisionName", column = @Column(name = "pres_division_name", length = 120)),
            @AttributeOverride(name = "districtId", column = @Column(name = "pres_district_id")),
            @AttributeOverride(name = "districtName", column = @Column(name = "pres_district_name", length = 120)),
            @AttributeOverride(name = "upazilaId", column = @Column(name = "pres_upazila_id")),
            @AttributeOverride(name = "upazilaName", column = @Column(name = "pres_upazila_name", length = 120)),
            @AttributeOverride(name = "unionId", column = @Column(name = "pres_union_id")),
            @AttributeOverride(name = "unionName", column = @Column(name = "pres_union_name", length = 120)),
            @AttributeOverride(name = "ward", column = @Column(name = "pres_ward")),
            @AttributeOverride(name = "postOffice", column = @Column(name = "pres_post_office", length = 120)),
            @AttributeOverride(name = "village", column = @Column(name = "pres_village"))
    })
    private Address presentAddress = new Address();

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private OrgLevel responsibilityLevel;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private Designation designation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrgRank orgRank;

    private LocalDate oathDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status = MemberStatus.PENDING;

    @Column(length = 500)
    private String rejectReason;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "org_zila_id")
    private OrgZila orgZila;

    /** কেন্দ্রীয় এডমিন হাতে যেলা নির্ধারণ করলে true; তখন ঠিকানা বদলালেও যেলা বদলাবে না */
    private boolean orgZilaManual;

    @Column(length = 60)
    private String reviewedBy;
    private Instant reviewedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = updatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getFatherName() { return fatherName; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }
    public String getMotherName() { return motherName; }
    public void setMotherName(String motherName) { this.motherName = motherName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public BloodGroup getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(BloodGroup bloodGroup) { this.bloodGroup = bloodGroup; }
    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMobile1() { return mobile1; }
    public void setMobile1(String mobile1) { this.mobile1 = mobile1; }
    public boolean isMobile1Call() { return mobile1Call; }
    public void setMobile1Call(boolean mobile1Call) { this.mobile1Call = mobile1Call; }
    public boolean isMobile1WhatsApp() { return mobile1WhatsApp; }
    public void setMobile1WhatsApp(boolean mobile1WhatsApp) { this.mobile1WhatsApp = mobile1WhatsApp; }
    public String getMobile2() { return mobile2; }
    public void setMobile2(String mobile2) { this.mobile2 = mobile2; }
    public boolean isMobile2Call() { return mobile2Call; }
    public void setMobile2Call(boolean mobile2Call) { this.mobile2Call = mobile2Call; }
    public boolean isMobile2WhatsApp() { return mobile2WhatsApp; }
    public void setMobile2WhatsApp(boolean mobile2WhatsApp) { this.mobile2WhatsApp = mobile2WhatsApp; }
    public Address getPermanentAddress() { return permanentAddress; }
    public void setPermanentAddress(Address permanentAddress) { this.permanentAddress = permanentAddress; }
    public boolean isPresentSameAsPermanent() { return presentSameAsPermanent; }
    public void setPresentSameAsPermanent(boolean presentSameAsPermanent) { this.presentSameAsPermanent = presentSameAsPermanent; }
    public Address getPresentAddress() { return presentAddress; }
    public void setPresentAddress(Address presentAddress) { this.presentAddress = presentAddress; }
    public OrgLevel getResponsibilityLevel() { return responsibilityLevel; }
    public void setResponsibilityLevel(OrgLevel responsibilityLevel) { this.responsibilityLevel = responsibilityLevel; }
    public Designation getDesignation() { return designation; }
    public void setDesignation(Designation designation) { this.designation = designation; }
    public OrgRank getOrgRank() { return orgRank; }
    public void setOrgRank(OrgRank orgRank) { this.orgRank = orgRank; }
    public LocalDate getOathDate() { return oathDate; }
    public void setOathDate(LocalDate oathDate) { this.oathDate = oathDate; }
    public MemberStatus getStatus() { return status; }
    public void setStatus(MemberStatus status) { this.status = status; }
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
    public OrgZila getOrgZila() { return orgZila; }
    public void setOrgZila(OrgZila orgZila) { this.orgZila = orgZila; }
    public boolean isOrgZilaManual() { return orgZilaManual; }
    public void setOrgZilaManual(boolean orgZilaManual) { this.orgZilaManual = orgZilaManual; }
    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
    public Instant getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Instant reviewedAt) { this.reviewedAt = reviewedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
