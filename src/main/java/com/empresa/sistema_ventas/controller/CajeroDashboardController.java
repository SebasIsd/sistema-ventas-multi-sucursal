package com.empresa.sistema_ventas.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/cajero")
public class CajeroDashboardController {

    @GetMapping("/dashboard")
    public String cajeroDashboard(Authentication authentication, Model model) {
        model.addAttribute("role", "Cajero");
        return "dashboard-cajero";
    }
}