package com.empresa.sistema_ventas.controller;

import com.empresa.sistema_ventas.dto.VentaRequest;
import com.empresa.sistema_ventas.entity.Inventario;
import com.empresa.sistema_ventas.entity.Sucursal;
import com.empresa.sistema_ventas.entity.Usuario;
import com.empresa.sistema_ventas.repository.InventarioRepository;
import com.empresa.sistema_ventas.repository.UsuarioRepository;
import com.empresa.sistema_ventas.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.empresa.sistema_ventas.repository.ClienteRepository;
import com.empresa.sistema_ventas.repository.ProductoRepository;

import java.util.List;


@Controller
@RequestMapping("/ventas")
@PreAuthorize("hasAnyRole('CAJERO', 'ADMIN')")
public class VentaController {
    @Autowired
    private InventarioRepository inventarioRepository;
    private final VentaService ventaService;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;

    public VentaController(
            VentaService ventaService,
            UsuarioRepository usuarioRepository,
            ClienteRepository clienteRepository,
            ProductoRepository productoRepository
    ) {
        this.ventaService = ventaService;
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
    }

    @GetMapping("/nueva")
    public String nuevaVenta(Authentication auth, Model model) {

        // USUARIO LOGUEADO
        Usuario usuario = usuarioRepository
                .findByUsername(auth.getName())
                .orElseThrow();

        // SUCURSAL DEL CAJERO
        Sucursal sucursal = usuario.getSucursal();

        // INVENTARIO DE ESA SUCURSAL
        List<Inventario> inventarios =
                inventarioRepository.findBySucursal(sucursal);

        model.addAttribute("clientes", clienteRepository.findAll());

        model.addAttribute("inventarios", inventarios);

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