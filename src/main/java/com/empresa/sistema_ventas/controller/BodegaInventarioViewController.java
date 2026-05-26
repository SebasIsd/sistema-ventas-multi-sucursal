package com.empresa.sistema_ventas.controller;

import com.empresa.sistema_ventas.service.InventarioService;
import com.empresa.sistema_ventas.repository.ProductoRepository;
import com.empresa.sistema_ventas.repository.SucursalRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/bodega/inventario")
public class BodegaInventarioViewController {

    private final InventarioService inventarioService;
    private final ProductoRepository productoRepository;
    private final SucursalRepository sucursalRepository;

    public BodegaInventarioViewController(InventarioService inventarioService,
                                          ProductoRepository productoRepository,
                                          SucursalRepository sucursalRepository) {
        this.inventarioService = inventarioService;
        this.productoRepository = productoRepository;
        this.sucursalRepository = sucursalRepository;
    }

    @GetMapping
    public String listarInventario(Model model) {
        model.addAttribute("inventarios", inventarioService.listarTodos());
        return "bodega/inventario/lista";
    }

    @GetMapping("/ajuste")
    public String formularioAjuste(Model model) {
        model.addAttribute("productos", productoRepository.findAll());
        model.addAttribute("sucursales", sucursalRepository.findAll());
        return "bodega/inventario/ajuste";
    }

    @PostMapping("/ajuste")
    public String guardarAjuste(@RequestParam Integer productoId,
                                @RequestParam Long sucursalId,
                                @RequestParam Integer cantidad,
                                @RequestParam String observacion) {
        inventarioService.aumentarStock(productoId, sucursalId, cantidad, observacion);
        return "redirect:/bodega/inventario";
    }

    @GetMapping("/transferencia")
    public String formularioTransferencia(Model model) {
        model.addAttribute("productos", productoRepository.findAll());
        model.addAttribute("sucursales", sucursalRepository.findAll());
        return "bodega/inventario/transferencia";
    }

    @PostMapping("/transferencia")
    public String procesarTransferencia(@RequestParam Integer productoId,
                                        @RequestParam Long sucursalOrigenId,
                                        @RequestParam Long sucursalDestinoId,
                                        @RequestParam Integer cantidad,
                                        @RequestParam String observacion) {
        inventarioService.transferirStock(productoId, sucursalOrigenId, sucursalDestinoId, cantidad, observacion);
        return "redirect:/bodega/inventario";
    }

    @GetMapping("/historial")
    public String historial(Model model) {
        model.addAttribute("movimientos", inventarioService.getHistorialMovimientos());
        return "bodega/inventario/historial";
    }
}