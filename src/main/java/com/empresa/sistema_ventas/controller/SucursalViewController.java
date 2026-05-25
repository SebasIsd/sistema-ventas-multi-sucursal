package com.empresa.sistema_ventas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/sucursales")
public class SucursalViewController {

    @GetMapping
    public String sucursales() {
        return "admin/sucursales/lista";
    }
}