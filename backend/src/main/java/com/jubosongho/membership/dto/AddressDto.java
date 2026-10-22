package com.jubosongho.membership.dto;

import com.jubosongho.membership.domain.Address;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** ইনপুটে শুধু id ও লেখা ঘর ব্যবহৃত হয়; *Name ঘরগুলো রেসপন্সে সার্ভার পূরণ করে। */
public record AddressDto(
        @NotNull(message = "বিভাগ নির্বাচন করুন") Long divisionId,
        String divisionName,
        @NotNull(message = "যেলা নির্বাচন করুন") Long districtId,
        String districtName,
        @NotNull(message = "উপযেলা নির্বাচন করুন") Long upazilaId,
        String upazilaName,
        /* উপযেলার অধীনে ইউনিয়ন থাকলে সার্ভিসে আবশ্যক করা হয় */
        Long unionId,
        String unionName,
        @NotNull(message = "ওয়ার্ড নির্বাচন করুন")
        @Min(value = 1, message = "সঠিক ওয়ার্ড নির্বাচন করুন")
        @Max(value = 99, message = "সঠিক ওয়ার্ড নির্বাচন করুন") Integer ward,
        @Size(max = 120, message = "পোস্ট অফিসের নাম অনেক বড়") String postOffice,
        @NotBlank(message = "গ্রাম/পাড়া/মহল্লা লিখুন")
        @Size(max = 255, message = "গ্রাম/পাড়া/মহল্লা অনেক বড়") String village
) {
    public static AddressDto from(Address a) {
        if (a == null) return null;
        return new AddressDto(a.getDivisionId(), a.getDivisionName(), a.getDistrictId(), a.getDistrictName(),
                a.getUpazilaId(), a.getUpazilaName(), a.getUnionId(), a.getUnionName(), a.getWard(),
                a.getPostOffice(), a.getVillage());
    }
}
