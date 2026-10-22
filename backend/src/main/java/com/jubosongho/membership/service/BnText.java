package com.jubosongho.membership.service;

/** বাংলা অঙ্ক ও মোবাইল নম্বর সংক্রান্ত সহায়ক মেথড। */
public final class BnText {

    private static final String BN_DIGITS = "০১২৩৪৫৬৭৮৯";

    private BnText() {
    }

    public static String toAsciiDigits(String s) {
        if (s == null) return null;
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            int idx = BN_DIGITS.indexOf(c);
            sb.append(idx >= 0 ? (char) ('0' + idx) : c);
        }
        return sb.toString();
    }

    public static String toBnDigits(Object value) {
        if (value == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : value.toString().toCharArray()) {
            sb.append(c >= '0' && c <= '9' ? BN_DIGITS.charAt(c - '0') : c);
        }
        return sb.toString();
    }

    /**
     * মোবাইল নম্বরকে 01XXXXXXXXX ফরম্যাটে আনে। বাংলা অঙ্ক, স্পেস, ড্যাশ ও +88 গ্রহণযোগ্য।
     * খালি হলে null, অবৈধ হলে IllegalArgumentException।
     */
    public static String normalizeMobile(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String digits = toAsciiDigits(raw).replaceAll("[^0-9]", "");
        if (digits.startsWith("880")) digits = digits.substring(2);
        else if (digits.startsWith("88") && digits.length() == 13) digits = digits.substring(2);
        if (!digits.matches("01[3-9]\\d{8}")) {
            throw new IllegalArgumentException("সঠিক মোবাইল নম্বর দিন (যেমন: ০১৭XXXXXXXX)");
        }
        return digits;
    }

    public static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
