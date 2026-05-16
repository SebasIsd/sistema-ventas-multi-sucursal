package com.empresa.sistema_ventas.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/bodega")
public class BodegaDashboardController {

    @GetMapping("/dashboard")
    public String bodegaDashboard(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("role", "BODEGUERO");
        return "dashboard-bodega";
    }
}