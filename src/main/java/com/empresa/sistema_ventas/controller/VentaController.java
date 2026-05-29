package com.empresa.sistema_ventas.controller;

import com.empresa.sistema_ventas.dto.VentaRequest;
import com.empresa.sistema_ventas.entity.Inventario;
import com.empresa.sistema_ventas.entity.Producto;
import com.empresa.sistema_ventas.entity.Sucursal;
import com.empresa.sistema_ventas.entity.Usuario;
import com.empresa.sistema_ventas.entity.Venta;
import com.empresa.sistema_ventas.repository.*;
import com.empresa.sistema_ventas.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

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

        // INVENTARIO DE ESA SUCURSAL (Cargar todos los productos activos)
        List<Producto> productosActivos = productoRepository.findByActivoTrue();
        List<Inventario> inventariosExistentes = inventarioRepository.findBySucursal(sucursal);

        List<Inventario> inventarios = productosActivos.stream().<Inventario>map(prod -> {
            return inventariosExistentes.stream()
                    .filter(inv -> inv.getProducto().getIdProducto().equals(prod.getIdProducto()))
                    .findFirst()
                    .orElseGet(() -> {
                        Inventario inv = new Inventario();
                        inv.setProducto(prod);
                        inv.setSucursal(sucursal);
                        inv.setStockActual(0);
                        inv.setStockMinimo(0);
                        return inv;
                    });
        }).toList();

        model.addAttribute("clientes", clienteRepository.findAll());

        model.addAttribute("inventarios", inventarios);

        return "ventas/nueva";
    }

    @PostMapping("/procesar")
    public String procesarVenta(
             @Valid @ModelAttribute VentaRequest request,
             BindingResult bindingResult,
             Authentication authentication,
             RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .findFirst()
                    .orElse("Datos de venta inválidos");
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            return "redirect:/ventas/nueva";
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Debe agregar al menos un producto a la venta");
            return "redirect:/ventas/nueva";
        }

        // Validar productos duplicados y cantidades negativas/cero
        java.util.Set<Integer> productIds = new java.util.HashSet<>();
        for (com.empresa.sistema_ventas.dto.ItemVentaDTO item : request.getItems()) {
            if (item.getProductoId() == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Fila de producto inválida o sin producto seleccionado");
                return "redirect:/ventas/nueva";
            }
            if (item.getCantidad() == null || item.getCantidad() < 1) {
                redirectAttributes.addFlashAttribute("errorMessage", "La cantidad para cada producto debe ser mayor o igual a 1");
                return "redirect:/ventas/nueva";
            }
            if (productIds.contains(item.getProductoId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "No se permiten productos duplicados. Consolide las cantidades en una sola fila.");
                return "redirect:/ventas/nueva";
            }
            productIds.add(item.getProductoId());
        }

        String username = authentication.getName();

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        try {
            ventaService.procesarVenta(
                    request.getClienteId(),
                    request.getItems(),
                    usuario.getId()
            );
            redirectAttributes.addFlashAttribute("successMessage", "Venta registrada con éxito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar venta: " + e.getMessage());
            return "redirect:/ventas/nueva";
        }

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
                    PageRequest.of(page, 10)
            );

        } else {

            ventas = ventaRepository.buscarVentas(
                    cedula,
                    fechaInicio,
                    fechaFin,
                    PageRequest.of(page, 10)
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