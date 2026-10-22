package com.jubosongho.membership.dto;

import com.jubosongho.membership.domain.BloodGroup;
import com.jubosongho.membership.domain.Designation;
import com.jubosongho.membership.domain.OrgLevel;
import com.jubosongho.membership.domain.OrgRank;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record MemberRequest(
        @NotBlank(message = "নাম লিখুন") @Size(max = 120, message = "নাম অনেক বড়") String name,
        @NotBlank(message = "পিতার নাম লিখুন") @Size(max = 120, message = "পিতার নাম অনেক বড়") String fatherName,
        @NotBlank(message = "মাতার নাম লিখুন") @Size(max = 120, message = "মাতার নাম অনেক বড়") String motherName,
        @NotNull(message = "জন্ম তারিখ দিন") @Past(message = "জন্ম তারিখ সঠিক নয়") LocalDate dateOfBirth,
        BloodGroup bloodGroup,
        @Size(max = 200, message = "শিক্ষাগত যোগ্যতা অনেক বড়") String education,
        @Email(message = "সঠিক ই-মেইল দিন") @Size(max = 150) String email,

        @NotBlank(message = "মোবাইল নম্বর দিন") String mobile1,
        boolean mobile1Call,
        boolean mobile1WhatsApp,
        String mobile2,
        boolean mobile2Call,
        boolean mobile2WhatsApp,

        @NotNull(message = "স্থায়ী ঠিকানা দিন") @Valid AddressDto permanentAddress,
        boolean presentSameAsPermanent,
        /* presentSameAsPermanent=false হলে সার্ভিসে যাচাই করা হয় */
        AddressDto presentAddress,

        OrgLevel responsibilityLevel,
        Designation designation,
        @NotNull(message = "সাংগঠনিক মান নির্বাচন করুন") OrgRank orgRank,
        @PastOrPresent(message = "শপথ গ্রহণের তারিখ ভবিষ্যতের হতে পারে না") LocalDate oathDate,

        /* শুধু কেন্দ্রীয় এডমিনের সম্পাদনায় ব্যবহৃত: null = স্বয়ংক্রিয়, 0 = কোনো যেলা নয় */
        Long orgZilaId
) {
}
