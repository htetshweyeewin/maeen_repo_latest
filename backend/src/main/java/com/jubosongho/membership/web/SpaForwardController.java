package com.jubosongho.membership.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Angular বিল্ড (dist/jubosongho/browser) src/main/resources/static এ কপি করে
 * একই সার্ভার থেকে চালালে, রিফ্রেশে Angular রাউটগুলো যেন index.html এ যায়।
 */
@Controller
public class SpaForwardController {

    @GetMapping({"/admin", "/admin/**"})
    public String forward() {
        return "forward:/index.html";
    }
}
