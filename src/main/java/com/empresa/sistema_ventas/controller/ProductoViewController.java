package com.empresa.sistema_ventas.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// ✅ Solo maneja las vistas HTML, sin lógica de negocio
@Controller
public class ProductoViewController {

    @GetMapping("/admin/productos")
    @PreAuthorize("hasRole('ADMIN')")
    public String productos() {
        return "admin/productos/lista";
    }
}