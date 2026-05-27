package com.empresa.sistema_ventas.controller;

import com.empresa.sistema_ventas.entity.Usuario;
import com.empresa.sistema_ventas.repository.UsuarioRepository;
import com.empresa.sistema_ventas.service.InventarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/bodega")
public class BodegaDashboardController {

    private final InventarioService inventarioService;
    private final UsuarioRepository usuarioRepository;

    public BodegaDashboardController(
            InventarioService inventarioService,
            UsuarioRepository usuarioRepository
    ) {
        this.inventarioService = inventarioService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/dashboard")
    public String bodegaDashboard(Authentication authentication, Model model) {
        model.addAttribute("role", "Bodeguero");

        usuarioRepository.findByUsername(authentication.getName())
                .map(Usuario::getSucursal)
                .ifPresent(sucursal -> model.addAttribute(
                        "stockBajo",
                        inventarioService.getStockBajoPorSucursal(sucursal.getId())
                ));

        return "dashboard-bodega";
    }
}