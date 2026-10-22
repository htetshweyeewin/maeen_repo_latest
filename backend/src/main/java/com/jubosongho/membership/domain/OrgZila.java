package com.jubosongho.membership.domain;

import jakarta.persistence.*;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * সাংগঠনিক যেলা (যেমন: ময়মনসিংহ দক্ষিণ)। এটি প্রশাসনিক যেলা নয় -
 * কয়েকটি উপযেলা নিয়ে গঠিত। সদস্যের বর্তমান ঠিকানার উপযেলা দেখে
 * সদস্যকে স্বয়ংক্রিয়ভাবে সংশ্লিষ্ট সাংগঠনিক যেলায় যুক্ত করা হয়।
 */
@Entity
@Table(name = "org_zila")
public class OrgZila {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "org_zila_upazila", joinColumns = @JoinColumn(name = "org_zila_id"))
    @Column(name = "upazila_id", nullable = false, unique = true)
    private Set<Long> upazilaIds = new LinkedHashSet<>();

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Set<Long> getUpazilaIds() { return upazilaIds; }
    public void setUpazilaIds(Set<Long> upazilaIds) { this.upazilaIds = upazilaIds; }
}
