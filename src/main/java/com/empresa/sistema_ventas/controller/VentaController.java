package com.empresa.sistema_ventas.controller;

import com.empresa.sistema_ventas.dto.VentaRequest;
import com.empresa.sistema_ventas.entity.Inventario;
import com.empresa.sistema_ventas.entity.Sucursal;
import com.empresa.sistema_ventas.entity.Usuario;
import com.empresa.sistema_ventas.entity.Venta;
import com.empresa.sistema_ventas.repository.*;
import com.empresa.sistema_ventas.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;


@Controller
@RequestMapping("/ventas")

public class VentaController {
    @Autowired
    private InventarioRepository inventarioRepository;
    private final VentaService ventaService;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;
    public VentaController(
            VentaService ventaService,
            UsuarioRepository usuarioRepository,
            ClienteRepository clienteRepository,
            ProductoRepository productoRepository,
            VentaRepository ventaRepository
    ) {
        this.ventaService = ventaService;
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
        this.ventaRepository = ventaRepository;    }

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

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(required = false) String cedula,

            @RequestParam(required = false) String fechaInicio,

            @RequestParam(required = false) String fechaFin,

            Model model
    ) {

        Page<Venta> ventas;

        // SI NO HAY FILTROS
        if(
                (cedula == null || cedula.isEmpty()) &&
                        (fechaInicio == null || fechaInicio.isEmpty()) &&
                        (fechaFin == null || fechaFin.isEmpty())
        ){

            ventas = ventaRepository.findAll(
                    PageRequest.of(page, 5)
            );

        } else {

            ventas = ventaRepository.buscarVentas(
                    cedula,
                    fechaInicio,
                    fechaFin,
                    PageRequest.of(page, 5)
            );
        }

        model.addAttribute("ventas", ventas);

        model.addAttribute("currentPage", page);

        model.addAttribute("cedula", cedula);

        model.addAttribute("fechaInicio", fechaInicio);

        model.addAttribute("fechaFin", fechaFin);

        return "ventas/historial";
    }
    @GetMapping("/detalle/{id}")
    public String detalleVenta(
            @PathVariable Long id,
            Model model
    ) {

        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

        model.addAttribute("venta", venta);

        return "ventas/detalle";
    }
}