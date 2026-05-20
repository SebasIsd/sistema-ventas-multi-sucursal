package com.empresa.sistema_ventas.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/cajero")
public class CajeroDashboardController {

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('CAJERO')")
    public String cajeroDashboard(Authentication authentication, Model model) {
        model.addAttribute("username", authentication != null ? authentication.getName() : "");
        model.addAttribute("role", "CAJERO");
        return "dashboard-cajero";
    }
}