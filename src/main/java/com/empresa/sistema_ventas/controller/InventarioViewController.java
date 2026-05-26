package com.empresa.sistema_ventas.controller;

import com.empresa.sistema_ventas.service.InventarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.empresa.sistema_ventas.repository.ProductoRepository;
import com.empresa.sistema_ventas.repository.SucursalRepository;

@Controller
public class InventarioViewController {

    private final InventarioService inventarioService;
    private final ProductoRepository productoRepository;
    private final SucursalRepository sucursalRepository;

    public InventarioViewController(InventarioService inventarioService,
                                    ProductoRepository productoRepository,
                                    SucursalRepository sucursalRepository) {
        this.inventarioService = inventarioService;
        this.productoRepository = productoRepository;
        this.sucursalRepository = sucursalRepository;
    }

    @GetMapping("/admin/inventario")
    public String inventario(Model model) {
        model.addAttribute("inventarios", inventarioService.listarTodos());
        return "admin/inventario/lista";
    }

    @GetMapping("/admin/inventario/ajustar")
    public String mostrarFormularioAjuste(Model model) {
        model.addAttribute("productos", productoRepository.findAll());
        model.addAttribute("sucursales", sucursalRepository.findAll());
        return "admin/inventario/ajustar";
    }

    @PostMapping("/admin/inventario/ajustar")
    public String ajustarStock(@RequestParam Integer productoId,
                               @RequestParam Long sucursalId,
                               @RequestParam Integer cantidad) {

        inventarioService.aumentarStock(productoId, sucursalId, cantidad);

        return "redirect:/admin/inventario";
    }

    @GetMapping("/bodega/stock")
    public String inventarioBodega(Model model) {
        model.addAttribute("inventarios", inventarioService.listarTodos());
        return "admin/inventario/lista";
    }

    @GetMapping("/bodega/ajustar")
    public String mostrarFormularioAjusteBodega(Model model) {
        model.addAttribute("productos", productoRepository.findAll());
        model.addAttribute("sucursales", sucursalRepository.findAll());
        return "admin/inventario/ajustar";
    }

    @PostMapping("/bodega/ajustar")
    public String ajustarStockBodega(@RequestParam Integer productoId,
                                     @RequestParam Long sucursalId,
                                     @RequestParam Integer cantidad) {

        inventarioService.aumentarStock(productoId, sucursalId, cantidad);

        return "redirect:/bodega/stock";
    }
}