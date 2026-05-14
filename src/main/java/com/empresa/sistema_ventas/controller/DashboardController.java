package com.empresa.sistema_ventas.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        // Opcional: agregar datos al modelo
        if (authentication != null) {
            model.addAttribute("username", authentication.getName());
        }
        return "dashboard";  // ✅ Busca templates/dashboard.html
    }
}