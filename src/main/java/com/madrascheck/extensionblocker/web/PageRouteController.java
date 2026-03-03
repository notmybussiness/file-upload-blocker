package com.madrascheck.extensionblocker.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageRouteController {

    @GetMapping("/")
    public String root() {
        return "redirect:/client";
    }

    @GetMapping("/client")
    public String client() {
        return "redirect:/client/index.html";
    }

    @GetMapping("/admin")
    public String admin() {
        return "redirect:/admin/index.html";
    }
}
