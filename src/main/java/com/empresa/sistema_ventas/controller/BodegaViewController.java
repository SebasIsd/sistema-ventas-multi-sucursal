package com.empresa.sistema_ventas.controller;

import com.empresa.sistema_ventas.entity.Inventario;
import com.empresa.sistema_ventas.entity.Sucursal;
import com.empresa.sistema_ventas.entity.Usuario;
import com.empresa.sistema_ventas.repository.MovimientoInventarioRepository;
import com.empresa.sistema_ventas.repository.ProductoRepository;
import com.empresa.sistema_ventas.repository.SucursalRepository;
import com.empresa.sistema_ventas.repository.UsuarioRepository;
import com.empresa.sistema_ventas.service.InventarioService;
import com.empresa.sistema_ventas.service.ProductoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/bodega")
public class BodegaViewController {

    private final InventarioService inventarioService;
    private final ProductoService productoService;
    private final ProductoRepository productoRepository;
    private final SucursalRepository sucursalRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    public BodegaViewController(
            InventarioService inventarioService,
            ProductoService productoService,
            ProductoRepository productoRepository,
            SucursalRepository sucursalRepository,
            UsuarioRepository usuarioRepository,
            MovimientoInventarioRepository movimientoInventarioRepository
    ) {
        this.inventarioService = inventarioService;
        this.productoService = productoService;
        this.productoRepository = productoRepository;
        this.sucursalRepository = sucursalRepository;
        this.usuarioRepository = usuarioRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
    }

    @GetMapping("/stock")
    public String stock(Authentication auth, Model model) {
        Sucursal sucursal = requireSucursal(auth);
        List<Inventario> inventarios = inventarioService.listarPorSucursal(sucursal.getId());
        model.addAttribute("inventarios", inventarios);
        model.addAttribute("sucursalNombre", sucursal.getCiudad());
        return "bodega/stock";
    }

    @GetMapping("/ajustar")
    public String mostrarAjuste(Authentication auth, Model model) {
        Sucursal sucursal = requireSucursal(auth);
        model.addAttribute("productos", productoRepository.findAll());
        model.addAttribute("sucursal", sucursal);
        return "bodega/ajustar";
    }

    @PostMapping("/ajustar")
    public String ajustarStock(
            Authentication auth,
            @RequestParam(required = false) Integer productoId,
            @RequestParam(required = false) Integer cantidad,
            RedirectAttributes redirectAttributes
    ) {
        if (productoId == null) {
            redirectAttributes.addFlashAttribute("error", "Debe seleccionar un producto.");
            redirectAttributes.addFlashAttribute("errorMessage", "Debe seleccionar un producto.");
            return "redirect:/bodega/ajustar";
        }
        if (cantidad == null || cantidad < 1) {
            redirectAttributes.addFlashAttribute("error", "La cantidad debe ser mayor o igual a 1.");
            redirectAttributes.addFlashAttribute("errorMessage", "La cantidad debe ser mayor o igual a 1.");
            return "redirect:/bodega/ajustar";
        }

        Sucursal sucursal = requireSucursal(auth);
        try {
            inventarioService.aumentarStock(productoId, sucursal.getId(), cantidad);
            redirectAttributes.addFlashAttribute("exito", "Stock ajustado correctamente.");
            redirectAttributes.addFlashAttribute("successMessage", "Stock ajustado correctamente.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/bodega/ajustar";
        }
        return "redirect:/bodega/stock";
    }

    @GetMapping("/transferencias")
    public String mostrarTransferencia(Authentication auth, Model model) {
        Sucursal sucursal = requireSucursal(auth);
        model.addAttribute("inventarios", inventarioService.listarPorSucursal(sucursal.getId()));
        model.addAttribute("sucursalOrigen", sucursal);
        model.addAttribute("sucursales", sucursalRepository.findAll().stream()
                .filter(s -> !s.getId().equals(sucursal.getId()))
                .collect(Collectors.toList()));
        return "bodega/transferencias";
    }

    @PostMapping("/transferencias")
    public String transferirStock(
            Authentication auth,
            @RequestParam(required = false) Integer productoId,
            @RequestParam(required = false) Long sucursalDestinoId,
            @RequestParam(required = false) Integer cantidad,
            RedirectAttributes redirectAttributes
    ) {
        if (productoId == null) {
            redirectAttributes.addFlashAttribute("error", "Debe seleccionar un producto.");
            redirectAttributes.addFlashAttribute("errorMessage", "Debe seleccionar un producto.");
            return "redirect:/bodega/transferencias";
        }
        if (sucursalDestinoId == null) {
            redirectAttributes.addFlashAttribute("error", "Debe seleccionar la sucursal de destino.");
            redirectAttributes.addFlashAttribute("errorMessage", "Debe seleccionar la sucursal de destino.");
            return "redirect:/bodega/transferencias";
        }
        if (cantidad == null || cantidad < 1) {
            redirectAttributes.addFlashAttribute("error", "La cantidad a transferir debe ser mayor o igual a 1.");
            redirectAttributes.addFlashAttribute("errorMessage", "La cantidad a transferir debe ser mayor o igual a 1.");
            return "redirect:/bodega/transferencias";
        }

        Sucursal sucursalOrigen = requireSucursal(auth);
        if (sucursalOrigen.getId().equals(sucursalDestinoId)) {
            redirectAttributes.addFlashAttribute("error", "La sucursal origen y destino no pueden ser iguales.");
            redirectAttributes.addFlashAttribute("errorMessage", "La sucursal origen y destino no pueden ser iguales.");
            return "redirect:/bodega/transferencias";
        }

        try {
            boolean tieneStock = inventarioService.verificarStock(productoId, sucursalOrigen.getId(), cantidad);
            if (!tieneStock) {
                redirectAttributes.addFlashAttribute("error", "Stock insuficiente en la sucursal origen.");
                redirectAttributes.addFlashAttribute("errorMessage", "Stock insuficiente en la sucursal origen.");
                return "redirect:/bodega/transferencias";
            }

            inventarioService.transferirStock(
                    productoId,
                    sucursalOrigen.getId(),
                    sucursalDestinoId,
                    cantidad
            );
            redirectAttributes.addFlashAttribute("exito", "Transferencia realizada correctamente.");
            redirectAttributes.addFlashAttribute("successMessage", "Transferencia realizada correctamente.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/bodega/transferencias";
        }
        return "redirect:/bodega/historial";
    }

    @GetMapping("/historial")
    public String historial(Authentication auth, Model model) {
        Sucursal sucursal = requireSucursal(auth);
        model.addAttribute("movimientos",
                movimientoInventarioRepository.findBySucursalIdOrderByFechaMovimientoDesc(sucursal.getId()));
        model.addAttribute("sucursalNombre", sucursal.getCiudad());
        return "bodega/historial";
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
