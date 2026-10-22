package com.jubosongho.membership.domain;

public enum MemberStatus implements Labeled {
    PENDING("অপেক্ষমাণ"),
    APPROVED("অনুমোদিত"),
    REJECTED("বাতিল");

    private final String label;

    MemberStatus(String label) { this.label = label; }

    @Override
    public String getLabel() { return label; }
}
