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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
                                @RequestParam(required = false) Integer productoId,
                                @RequestParam(required = false) Integer cantidad,
                                @RequestParam(required = false) String observacion,
                                RedirectAttributes redirectAttributes) {
        if (productoId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Debe seleccionar un producto.");
            return "redirect:/bodega/inventario/ajuste";
        }
        if (cantidad == null || cantidad < 1) {
            redirectAttributes.addFlashAttribute("errorMessage", "La cantidad debe ser mayor o igual a 1.");
            return "redirect:/bodega/inventario/ajuste";
        }

        String obs = (observacion == null || observacion.trim().isEmpty()) ? "Ajuste manual de stock" : observacion.trim();
        if (obs.length() > 255) {
            redirectAttributes.addFlashAttribute("errorMessage", "La observación no puede superar los 255 caracteres.");
            return "redirect:/bodega/inventario/ajuste";
        }

        Sucursal sucursal = requireSucursal(auth);
        try {
            inventarioService.aumentarStock(productoId, sucursal.getId(), cantidad, obs);
            redirectAttributes.addFlashAttribute("successMessage", "Stock ajustado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al ajustar stock: " + e.getMessage());
            return "redirect:/bodega/inventario/ajuste";
        }
        return "redirect:/bodega/inventario";
    }

    @GetMapping("/transferencia")
    public String formularioTransferencia(Authentication auth, Model model) {
        Sucursal sucursalOrigen = requireSucursal(auth);
        model.addAttribute("inventarios", inventarioService.listarPorSucursal(sucursalOrigen.getId()));
        model.addAttribute("sucursalOrigen", sucursalOrigen);
        
        List<Sucursal> sucursalesDestino = sucursalRepository.findAll().stream()
                .filter(s -> !s.getId().equals(sucursalOrigen.getId()))
                .collect(Collectors.toList());
        model.addAttribute("sucursales", sucursalesDestino);
        
        return "bodega/inventario/transferencia";
    }

    @PostMapping("/transferencia")
    public String procesarTransferencia(Authentication auth,
                                        @RequestParam(required = false) Integer productoId,
                                        @RequestParam(required = false) Long sucursalDestinoId,
                                        @RequestParam(required = false) Integer cantidad,
                                        @RequestParam(required = false) String observacion,
                                        RedirectAttributes redirectAttributes) {
        if (productoId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Debe seleccionar un producto.");
            return "redirect:/bodega/inventario/transferencia";
        }
        if (sucursalDestinoId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Debe seleccionar la sucursal de destino.");
            return "redirect:/bodega/inventario/transferencia";
        }
        if (cantidad == null || cantidad < 1) {
            redirectAttributes.addFlashAttribute("errorMessage", "La cantidad a transferir debe ser mayor o igual a 1.");
            return "redirect:/bodega/inventario/transferencia";
        }

        String obs = (observacion == null || observacion.trim().isEmpty()) ? "Transferencia de stock" : observacion.trim();
        if (obs.length() > 255) {
            redirectAttributes.addFlashAttribute("errorMessage", "La observación no puede superar los 255 caracteres.");
            return "redirect:/bodega/inventario/transferencia";
        }

        Sucursal sucursalOrigen = requireSucursal(auth);
        if (sucursalOrigen.getId().equals(sucursalDestinoId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "La sucursal origen y destino no pueden ser iguales.");
            return "redirect:/bodega/inventario/transferencia";
        }

        try {
            boolean tieneStock = inventarioService.verificarStock(productoId, sucursalOrigen.getId(), cantidad);
            if (!tieneStock) {
                redirectAttributes.addFlashAttribute("errorMessage", "Stock insuficiente en la sucursal origen.");
                return "redirect:/bodega/inventario/transferencia";
            }

            inventarioService.transferirStock(productoId, sucursalOrigen.getId(), sucursalDestinoId, cantidad, obs);
            redirectAttributes.addFlashAttribute("successMessage", "Transferencia realizada correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar transferencia: " + e.getMessage());
            return "redirect:/bodega/inventario/transferencia";
        }
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