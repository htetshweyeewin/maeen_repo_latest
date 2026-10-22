package com.jubosongho.membership.domain;

public enum BloodGroup implements Labeled {
    A_POS("এ+"), A_NEG("এ-"),
    B_POS("বি+"), B_NEG("বি-"),
    O_POS("ও+"), O_NEG("ও-"),
    AB_POS("এবি+"), AB_NEG("এবি-");

    private final String label;

    BloodGroup(String label) { this.label = label; }

    @Override
    public String getLabel() { return label; }
}
