package com.empresa.sistema_ventas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
public class CategoriaViewController {

    @GetMapping("/admin/categorias")
    @PreAuthorize("hasRole('ADMIN')")
    public String categorias() {
        return "admin/categorias/lista";
    }
}