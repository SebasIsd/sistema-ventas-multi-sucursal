package com.empresa.sistema_ventas.controller;

import com.empresa.sistema_ventas.entity.Factura;
import com.empresa.sistema_ventas.entity.Usuario;
import com.empresa.sistema_ventas.repository.UsuarioRepository;
import com.empresa.sistema_ventas.service.FacturaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/facturas")
public class FacturaController {

    private static final Logger log = LoggerFactory.getLogger(FacturaController.class);

    private final FacturaService facturaService;
    private final UsuarioRepository usuarioRepository;

    public FacturaController(FacturaService facturaService, UsuarioRepository usuarioRepository) {
        this.facturaService = facturaService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/ver/{ventaId}")
    public String verFactura(@PathVariable Long ventaId, Authentication auth, Model model) {
        validarSucursal(ventaId, auth);

        Optional<Factura> factura = facturaService.obtenerFacturaPorVentaId(ventaId);
        model.addAttribute("ventaId", ventaId);
        model.addAttribute("factura", factura.orElse(null));

        return "facturas/ver";
    }

    @GetMapping("/ver/{ventaId}/pdf")
    public ResponseEntity<byte[]> verFacturaPdf(@PathVariable Long ventaId, Authentication auth) {
        validarSucursal(ventaId, auth);

        byte[] pdf = facturaService.generarPDF(ventaId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline()
                .filename("factura-" + ventaId + ".pdf")
                .build());
        headers.set("X-Frame-Options", "SAMEORIGIN");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }

    @GetMapping("/descargar")
    public String pantallaDescarga() {
        return "facturas/descargar";
    }

    @GetMapping("/descargar/{ventaId}")
    public ResponseEntity<byte[]> descargarFactura(@PathVariable Long ventaId, Authentication auth) {
        validarSucursal(ventaId, auth);

        byte[] pdf = facturaService.generarPDF(ventaId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("factura-" + ventaId + ".pdf")
                .build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }

    @GetMapping("/enviar-sri/{ventaId}")
    public String enviarSRI(@PathVariable Long ventaId, Authentication auth) {
        validarSucursal(ventaId, auth);
        facturaService.enviarSRI(ventaId);
        return "redirect:/facturas/ver/" + ventaId;
    }

    private void validarSucursal(Long ventaId, Authentication auth) {
        try {
            Usuario usuario = usuarioRepository.findByUsername(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            log.info("validarSucursal: usuario={}, sucursal={}",
                    auth.getName(), usuario.getSucursal().getCodigo());
            facturaService.validarSucursalVenta(ventaId, usuario.getSucursal().getId());
        } catch (RuntimeException ex) {
            log.warn("ACCESO DENEGADO: usuario={}, ventaId={}, error={}",
                    auth.getName(), ventaId, ex.getMessage());
            throw ex;
        }
    }
}
