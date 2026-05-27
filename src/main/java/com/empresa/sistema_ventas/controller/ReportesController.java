package com.empresa.sistema_ventas.controller;

import com.empresa.sistema_ventas.entity.Usuario;
import com.empresa.sistema_ventas.repository.UsuarioRepository;
import com.empresa.sistema_ventas.service.ReporteService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/reportes")
public class ReportesController {

    private final ReporteService reporteService;
    private final UsuarioRepository usuarioRepository;

    public ReportesController(ReporteService reporteService, UsuarioRepository usuarioRepository) {
        this.reporteService = reporteService;
        this.usuarioRepository = usuarioRepository;
    }

    // ===== VENTAS =====

    @GetMapping("/ventas")
    public String verReporteVentas(
            @RequestParam(required = false) Long sucursalId,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            Authentication auth,
            Model model
    ) {
        Long userSucursalId = getSucursalId(auth);
        model.addAttribute("sucursalId", userSucursalId);
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        return "reportes/ver-ventas";
    }

    @GetMapping("/ventas/pdf")
    public ResponseEntity<byte[]> verReporteVentasPdf(
            @RequestParam(required = false) Long sucursalId,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            Authentication auth
    ) {
        Long userSucursalId = getSucursalId(auth);
        byte[] pdf = reporteService.generarReporteVentas(userSucursalId, fechaInicio, fechaFin);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline()
                .filename("reporte-ventas.pdf")
                .build());
        headers.set("X-Frame-Options", "SAMEORIGIN");

        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    @GetMapping("/ventas/descargar")
    public ResponseEntity<byte[]> descargarReporteVentas(
            @RequestParam(required = false) Long sucursalId,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            Authentication auth
    ) {
        Long userSucursalId = getSucursalId(auth);
        byte[] pdf = reporteService.generarReporteVentas(userSucursalId, fechaInicio, fechaFin);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("reporte-ventas.pdf")
                .build());

        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    // ===== INVENTARIO =====

    @GetMapping("/inventario")
    public String verReporteInventario(
            @RequestParam(required = false) Long sucursalId,
            Authentication auth,
            Model model
    ) {
        Long userSucursalId = getSucursalId(auth);
        boolean isAdmin = hasRole(auth, "ADMIN");
        model.addAttribute("sucursalId", userSucursalId);
        model.addAttribute("isAdmin", isAdmin);
        return "reportes/ver-inventario";
    }

    @GetMapping("/inventario/pdf")
    public ResponseEntity<byte[]> verReporteInventarioPdf(
            @RequestParam(required = false) Long sucursalId,
            Authentication auth
    ) {
        Long userSucursalId = getSucursalId(auth);
        byte[] pdf = reporteService.generarReporteInventario(sucursalId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline()
                .filename("reporte-inventario.pdf")
                .build());
        headers.set("X-Frame-Options", "SAMEORIGIN");

        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    @GetMapping("/inventario/descargar")
    public ResponseEntity<byte[]> descargarReporteInventario(
            @RequestParam(required = false) Long sucursalId,
            Authentication auth
    ) {
        Long userSucursalId = getSucursalId(auth);
        byte[] pdf = reporteService.generarReporteInventario(sucursalId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("reporte-inventario.pdf")
                .build());

        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    // ===== Helpers =====

    private Long getSucursalId(Authentication auth) {
        Usuario usuario = usuarioRepository.findByUsername(auth.getName()).orElse(null);
        return usuario != null && usuario.getSucursal() != null ? usuario.getSucursal().getId() : null;
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }
}