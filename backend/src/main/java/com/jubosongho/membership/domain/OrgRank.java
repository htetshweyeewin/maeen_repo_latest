package com.jubosongho.membership.domain;

/** সাংগঠনিক মান */
public enum OrgRank implements Labeled {
    PRIMARY_MEMBER("প্রাথমিক সদস্য"),
    KORMI("কর্মী"),
    CENTRAL_COUNCIL_MEMBER("কেন্দ্রীয় কাউন্সিল সদস্য");

    private final String label;

    OrgRank(String label) { this.label = label; }

    @Override
    public String getLabel() { return label; }
}
