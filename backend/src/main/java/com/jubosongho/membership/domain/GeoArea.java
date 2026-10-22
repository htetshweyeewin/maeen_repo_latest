package com.jubosongho.membership.domain;

import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

/** বিভাগ / যেলা / উপযেলা / ইউনিয়ন - একটি টেবিলে, parentId দিয়ে স্তরবিন্যাস। */
@Entity
@Table(name = "geo_area", indexes = @Index(name = "idx_geo_parent", columnList = "parent_id"))
public class GeoArea implements Persistable<Long> {

    @Id
    private Long id; // keyOf(level, sourceId)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private GeoLevel level;

    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(nullable = false, length = 120)
    private String name;

    @Transient
    private boolean isNew = true;

    protected GeoArea() {
    }

    public GeoArea(GeoLevel level, Long sourceId, Long parentSourceId, String name) {
        this.level = level;
        this.sourceId = sourceId;
        this.id = keyOf(level, sourceId);
        this.parentId = parentSourceId == null ? null : keyOf(GeoLevel.values()[level.ordinal() - 1], parentSourceId);
        this.name = name;
    }

    /** প্রতিটি স্তরের id আলাদা রাখতে স্তরভিত্তিক অফসেট যোগ করা হয়। */
    public static long keyOf(GeoLevel level, long sourceId) {
        return (level.ordinal() + 1) * 1_000_000L + sourceId;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        isNew = false;
    }

    @Override
    public boolean isNew() { return isNew; }

    @Override
    public Long getId() { return id; }
    public GeoLevel getLevel() { return level; }
    public Long getSourceId() { return sourceId; }
    public Long getParentId() { return parentId; }
    public String getName() { return name; }
}
