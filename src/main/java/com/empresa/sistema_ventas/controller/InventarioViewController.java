package com.empresa.sistema_ventas.controller;

import com.empresa.sistema_ventas.service.InventarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class InventarioViewController {

    private final InventarioService inventarioService;

    public InventarioViewController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @GetMapping("/admin/inventario")
    public String inventario(Model model) {
        model.addAttribute("inventarios", inventarioService.listarTodos());
        return "admin/inventario/lista";
    }

    @GetMapping("/admin/inventario/ajustar")
    public String mostrarFormularioAjuste() {
        return "admin/inventario/ajustar";
    }

    @PostMapping("/admin/inventario/ajustar")
    public String ajustarStock(@RequestParam Integer productoId,
                               @RequestParam Long sucursalId,
                               @RequestParam Integer cantidad) {

        inventarioService.aumentarStock(productoId, sucursalId, cantidad);

        return "redirect:/admin/inventario";
    }
}