package com.jubosongho.membership.domain;

public enum Role implements Labeled {
    SUPER_ADMIN("কেন্দ্রীয় এডমিন"),
    ZILA_ADMIN("যেলা এডমিন");

    private final String label;

    Role(String label) { this.label = label; }

    @Override
    public String getLabel() { return label; }
}
