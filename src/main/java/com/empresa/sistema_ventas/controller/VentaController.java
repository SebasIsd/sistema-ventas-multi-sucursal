package com.empresa.sistema_ventas.controller;

import com.empresa.sistema_ventas.dto.VentaRequest;
import com.empresa.sistema_ventas.entity.Usuario;
import com.empresa.sistema_ventas.repository.UsuarioRepository;
import com.empresa.sistema_ventas.service.VentaService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/ventas")
public class VentaController {

    private final VentaService ventaService;
    private final UsuarioRepository usuarioRepository;

    public VentaController(
            VentaService ventaService,
            UsuarioRepository usuarioRepository
    ) {
        this.ventaService = ventaService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/nueva")
    public String nuevaVenta() {
        return "ventas/nueva";
    }

    @PostMapping("/procesar")
    public String procesarVenta(
            @ModelAttribute VentaRequest request,
            Authentication authentication
    ) {

        String username = authentication.getName();

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        ventaService.procesarVenta(
                request.getClienteId(),
                request.getItems(),
                usuario.getId()
        );

        return "redirect:/ventas/historial";
    }

    @GetMapping("/historial")
    public String historialVentas(
            Authentication authentication,
            Model model
    ) {

        String username = authentication.getName();

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        model.addAttribute(
                "ventas",
                ventaService.getHistorialVentas(
                        usuario.getSucursal().getId()
                )
        );

        return "ventas/historial";
    }
}