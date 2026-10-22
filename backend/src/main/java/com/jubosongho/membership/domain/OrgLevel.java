package com.jubosongho.membership.domain;

/** সাংগঠনিক স্তর - ফরমের ক্রম অনুযায়ী: শাখা, এলাকা, উপযেলা, যেলা, কেন্দ্র */
public enum OrgLevel implements Labeled {
    SHAKHA("শাখা"),
    ELAKA("এলাকা"),
    UPAZILA("উপযেলা"),
    ZILA("যেলা"),
    KENDRO("কেন্দ্র");

    private final String label;

    OrgLevel(String label) { this.label = label; }

    @Override
    public String getLabel() { return label; }
}
