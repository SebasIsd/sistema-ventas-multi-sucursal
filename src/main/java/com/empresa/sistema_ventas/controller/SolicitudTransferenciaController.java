package com.empresa.sistema_ventas.controller;

import com.empresa.sistema_ventas.entity.*;
import com.empresa.sistema_ventas.repository.*;
import com.empresa.sistema_ventas.service.InventarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class SolicitudTransferenciaController {

    private final SolicitudTransferenciaRepository solicitudRepo;
    private final InventarioRepository inventarioRepo;
    private final SucursalRepository sucursalRepo;
    private final ProductoRepository productoRepo;
    private final UsuarioRepository usuarioRepo;
    private final InventarioService inventarioService;

    public SolicitudTransferenciaController(
            SolicitudTransferenciaRepository solicitudRepo,
            InventarioRepository inventarioRepo,
            SucursalRepository sucursalRepo,
            ProductoRepository productoRepo,
            UsuarioRepository usuarioRepo,
            InventarioService inventarioService) {
        this.solicitudRepo = solicitudRepo;
        this.inventarioRepo = inventarioRepo;
        this.sucursalRepo = sucursalRepo;
        this.productoRepo = productoRepo;
        this.usuarioRepo = usuarioRepo;
        this.inventarioService = inventarioService;
    }

    // -----------------------------------------------------------------------
    // CAJERO: consulta stock en otras sucursales (llamado desde el modal AJAX)
    // -----------------------------------------------------------------------
    @GetMapping("/ventas/stock-otras-sucursales")
    @ResponseBody
    @PreAuthorize("hasAnyRole('CAJERO', 'ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> stockOtrasSucursales(
            @RequestParam Integer productoId,
            Authentication auth) {

        Usuario usuario = usuarioRepo.findByUsername(auth.getName()).orElseThrow();
        Sucursal sucursalActual = usuario.getSucursal();

        List<Sucursal> todasSucursales = sucursalRepo.findAll();

        List<Map<String, Object>> resultado = todasSucursales.stream()
                .filter(s -> !s.getId().equals(sucursalActual.getId()) && s.isActivo())
                .map(sucursal -> {
                    int stock = inventarioRepo
                            .findByProducto_IdProductoAndSucursal_Id(productoId, sucursal.getId())
                            .map(Inventario::getStockActual)
                            .orElse(0);
                    Map<String, Object> item = new HashMap<>();
                    item.put("sucursalId", sucursal.getId());
                    item.put("ciudad", sucursal.getCiudad());
                    item.put("codigo", sucursal.getCodigo());
                    item.put("stock", stock);
                    return item;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(resultado);
    }

    // -----------------------------------------------------------------------
    // CAJERO: crea la solicitud de transferencia
    // -----------------------------------------------------------------------
    @PostMapping("/ventas/solicitar-transferencia")
    @ResponseBody
    @PreAuthorize("hasAnyRole('CAJERO', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> solicitarTransferencia(
            @RequestParam Integer productoId,
            @RequestParam Long sucursalOrigenId,
            @RequestParam Integer cantidad,
            Authentication auth) {

        Map<String, Object> resp = new HashMap<>();
        try {
            Usuario usuario = usuarioRepo.findByUsername(auth.getName()).orElseThrow();
            Sucursal sucursalDestino = usuario.getSucursal();

            Producto producto = productoRepo.findById(productoId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            Sucursal sucursalOrigen = sucursalRepo.findById(sucursalOrigenId)
                    .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));

            // Verificar que realmente haya stock en la sucursal origen
            int stockOrigen = inventarioRepo
                    .findByProducto_IdProductoAndSucursal_Id(productoId, sucursalOrigenId)
                    .map(Inventario::getStockActual)
                    .orElse(0);

            if (stockOrigen < cantidad) {
                resp.put("ok", false);
                resp.put("mensaje", "Stock insuficiente en la sucursal seleccionada (disponible: " + stockOrigen + ")");
                return ResponseEntity.ok(resp);
            }

            SolicitudTransferencia solicitud = new SolicitudTransferencia();
            solicitud.setProducto(producto);
            solicitud.setSucursalOrigen(sucursalOrigen);
            solicitud.setSucursalDestino(sucursalDestino);
            solicitud.setCantidad(cantidad);
            solicitud.setObservacion("Solicitud desde caja de " + sucursalDestino.getCiudad());
            solicitudRepo.save(solicitud);

            resp.put("ok", true);
            resp.put("mensaje", "Solicitud enviada correctamente al bodeguero de " + sucursalOrigen.getCiudad());
        } catch (Exception e) {
            resp.put("ok", false);
            resp.put("mensaje", "Error: " + e.getMessage());
        }
        return ResponseEntity.ok(resp);
    }

    // -----------------------------------------------------------------------
    // BODEGA: ver solicitudes pendientes dirigidas a su sucursal
    // -----------------------------------------------------------------------
    @GetMapping("/bodega/solicitudes")
    @PreAuthorize("hasAnyRole('BODEGA', 'ADMIN')")
    public String verSolicitudes(Authentication auth, Model model) {
        Sucursal sucursal = getSucursalBodeguero(auth);

        List<SolicitudTransferencia> pendientes = solicitudRepo
                .findBySucursalOrigen_IdAndEstadoOrderByFechaSolicitudDesc(sucursal.getId(), "PENDIENTE");

        List<SolicitudTransferencia> historial = solicitudRepo
                .findBySucursalOrigen_IdOrderByFechaSolicitudDesc(sucursal.getId());

        model.addAttribute("pendientes", pendientes);
        model.addAttribute("historial", historial);
        model.addAttribute("sucursal", sucursal);
        return "bodega/solicitudes";
    }

    // -----------------------------------------------------------------------
    // BODEGA: aceptar solicitud → ejecuta la transferencia real de stock
    // -----------------------------------------------------------------------
    @PostMapping("/bodega/solicitudes/{id}/aceptar")
    @PreAuthorize("hasAnyRole('BODEGA', 'ADMIN')")
    public String aceptarSolicitud(@PathVariable Long id,
                                   Authentication auth,
                                   RedirectAttributes ra) {
        try {
            SolicitudTransferencia solicitud = solicitudRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

            if (!"PENDIENTE".equals(solicitud.getEstado())) {
                ra.addFlashAttribute("errorMessage", "Esta solicitud ya fue procesada.");
                return "redirect:/bodega/solicitudes";
            }

            // Verificar nuevamente el stock antes de ejecutar
            Sucursal sucursalBodeguero = getSucursalBodeguero(auth);
            if (!solicitud.getSucursalOrigen().getId().equals(sucursalBodeguero.getId())) {
                ra.addFlashAttribute("errorMessage", "No tienes permiso para procesar esta solicitud.");
                return "redirect:/bodega/solicitudes";
            }

            inventarioService.transferirStock(
                    solicitud.getProducto().getIdProducto(),
                    solicitud.getSucursalOrigen().getId(),
                    solicitud.getSucursalDestino().getId(),
                    solicitud.getCantidad(),
                    "Transferencia aprobada por bodega — solicitud #" + solicitud.getId()
            );

            solicitud.setEstado("ACEPTADA");
            solicitudRepo.save(solicitud);

            ra.addFlashAttribute("successMessage",
                    "Transferencia aceptada. Se enviaron " + solicitud.getCantidad()
                    + " unidades de " + solicitud.getProducto().getNombre()
                    + " a " + solicitud.getSucursalDestino().getCiudad());
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Error al aceptar: " + e.getMessage());
        }
        return "redirect:/bodega/solicitudes";
    }

    // -----------------------------------------------------------------------
    // BODEGA: rechazar solicitud
    // -----------------------------------------------------------------------
    @PostMapping("/bodega/solicitudes/{id}/rechazar")
    @PreAuthorize("hasAnyRole('BODEGA', 'ADMIN')")
    public String rechazarSolicitud(@PathVariable Long id,
                                    Authentication auth,
                                    RedirectAttributes ra) {
        try {
            SolicitudTransferencia solicitud = solicitudRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

            if (!"PENDIENTE".equals(solicitud.getEstado())) {
                ra.addFlashAttribute("errorMessage", "Esta solicitud ya fue procesada.");
                return "redirect:/bodega/solicitudes";
            }

            Sucursal sucursalBodeguero = getSucursalBodeguero(auth);
            if (!solicitud.getSucursalOrigen().getId().equals(sucursalBodeguero.getId())) {
                ra.addFlashAttribute("errorMessage", "No tienes permiso para procesar esta solicitud.");
                return "redirect:/bodega/solicitudes";
            }

            solicitud.setEstado("RECHAZADA");
            solicitudRepo.save(solicitud);

            ra.addFlashAttribute("successMessage", "Solicitud rechazada.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Error al rechazar: " + e.getMessage());
        }
        return "redirect:/bodega/solicitudes";
    }

    private Sucursal getSucursalBodeguero(Authentication auth) {
        Usuario usuario = usuarioRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (usuario.getSucursal() == null) {
            throw new RuntimeException("El bodeguero no tiene sucursal asignada");
        }
        return usuario.getSucursal();
    }
}
