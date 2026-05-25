package com.empresa.sistema_ventas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/bodega")
public class BodegaViewController {

    @GetMapping("/stock")
    public String stock() {
        return "bodega/inventario/listar";
    }

    @GetMapping("/transferencias")
    public String transferencias() {
        return "bodega/inventario/transferencias";
    }
}