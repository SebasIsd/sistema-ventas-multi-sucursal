package com.empresa.sistema_ventas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CategoriaViewController {

    @GetMapping("/admin/categorias")
    public String categorias() {
        return "admin/categorias/lista";
    }
}