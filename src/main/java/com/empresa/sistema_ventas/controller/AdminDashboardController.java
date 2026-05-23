package com.empresa.sistema_ventas.controller;

import com.empresa.sistema_ventas.repository.ProductoRepository;
import com.empresa.sistema_ventas.repository.UsuarioRepository;
import com.empresa.sistema_ventas.repository.VentaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final UsuarioRepository usuarioRepository;
    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;

    public AdminDashboardController(
            UsuarioRepository usuarioRepository,
            VentaRepository ventaRepository,
            ProductoRepository productoRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
    }

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("role", "Administrador");
        model.addAttribute("totalUsuarios", usuarioRepository.count());
        model.addAttribute("totalVentas", ventaRepository.count());
        model.addAttribute("totalProductos", productoRepository.count());
        model.addAttribute("totalIngresos", ventaRepository.getTotalIngresos());
        return "dashboard-admin";
    }
}