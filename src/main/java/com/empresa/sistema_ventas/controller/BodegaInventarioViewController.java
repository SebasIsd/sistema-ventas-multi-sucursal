package com.empresa.sistema_ventas.controller;

import com.empresa.sistema_ventas.entity.Sucursal;
import com.empresa.sistema_ventas.entity.Usuario;
import com.empresa.sistema_ventas.repository.MovimientoInventarioRepository;
import com.empresa.sistema_ventas.repository.ProductoRepository;
import com.empresa.sistema_ventas.repository.SucursalRepository;
import com.empresa.sistema_ventas.repository.UsuarioRepository;
import com.empresa.sistema_ventas.service.InventarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/bodega/inventario")
public class BodegaInventarioViewController {

    private final InventarioService inventarioService;
    private final ProductoRepository productoRepository;
    private final SucursalRepository sucursalRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    public BodegaInventarioViewController(InventarioService inventarioService,
                                          ProductoRepository productoRepository,
                                          SucursalRepository sucursalRepository,
                                          UsuarioRepository usuarioRepository,
                                          MovimientoInventarioRepository movimientoInventarioRepository) {
        this.inventarioService = inventarioService;
        this.productoRepository = productoRepository;
        this.sucursalRepository = sucursalRepository;
        this.usuarioRepository = usuarioRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
    }

    @GetMapping
    public String listarInventario(Authentication auth, Model model) {
        Sucursal sucursal = requireSucursal(auth);
        model.addAttribute("inventarios", inventarioService.listarPorSucursal(sucursal.getId()));
        model.addAttribute("sucursal", sucursal);
        return "bodega/inventario/lista";
    }

    @GetMapping("/ajuste")
    public String formularioAjuste(Authentication auth, Model model) {
        Sucursal sucursal = requireSucursal(auth);
        model.addAttribute("productos", productoRepository.findAll());
        model.addAttribute("sucursal", sucursal);
        return "bodega/inventario/ajuste";
    }

    @PostMapping("/ajuste")
    public String guardarAjuste(Authentication auth,
                                @RequestParam Integer productoId,
                                @RequestParam Integer cantidad,
                                @RequestParam String observacion) {
        Sucursal sucursal = requireSucursal(auth);
        inventarioService.aumentarStock(productoId, sucursal.getId(), cantidad, observacion);
        return "redirect:/bodega/inventario";
    }

    @GetMapping("/transferencia")
    public String formularioTransferencia(Authentication auth, Model model) {
        Sucursal sucursalOrigen = requireSucursal(auth);
        model.addAttribute("productos", productoRepository.findAll());
        model.addAttribute("sucursalOrigen", sucursalOrigen);
        
        List<Sucursal> sucursalesDestino = sucursalRepository.findAll().stream()
                .filter(s -> !s.getId().equals(sucursalOrigen.getId()))
                .collect(Collectors.toList());
        model.addAttribute("sucursales", sucursalesDestino);
        
        return "bodega/inventario/transferencia";
    }

    @PostMapping("/transferencia")
    public String procesarTransferencia(Authentication auth,
                                        @RequestParam Integer productoId,
                                        @RequestParam Long sucursalDestinoId,
                                        @RequestParam Integer cantidad,
                                        @RequestParam String observacion) {
        Sucursal sucursalOrigen = requireSucursal(auth);
        inventarioService.transferirStock(productoId, sucursalOrigen.getId(), sucursalDestinoId, cantidad, observacion);
        return "redirect:/bodega/inventario";
    }

    @GetMapping("/historial")
    public String historial(Authentication auth, Model model) {
        Sucursal sucursal = requireSucursal(auth);
        model.addAttribute("movimientos",
                movimientoInventarioRepository.findBySucursalIdOrderByFechaMovimientoDesc(sucursal.getId()));
        model.addAttribute("sucursal", sucursal);
        return "bodega/inventario/historial";
    }

    private Sucursal requireSucursal(Authentication auth) {
        Usuario usuario = usuarioRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (usuario.getSucursal() == null) {
            throw new RuntimeException("El usuario no tiene sucursal asignada");
        }
        return usuario.getSucursal();
    }
}