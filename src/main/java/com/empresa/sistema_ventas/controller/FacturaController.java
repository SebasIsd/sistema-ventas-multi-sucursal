package com.empresa.sistema_ventas.controller;

import com.empresa.sistema_ventas.entity.Factura;
import com.empresa.sistema_ventas.service.FacturaService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/facturas")
public class FacturaController {

    private final FacturaService facturaService;

    public FacturaController(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    @GetMapping("/ver/{ventaId}")
    public String verFactura(@PathVariable Long ventaId, Model model) {
        Optional<Factura> factura = facturaService.obtenerFacturaPorVentaId(ventaId);

        model.addAttribute("ventaId", ventaId);
        model.addAttribute("factura", factura.orElse(null));

        return "facturas/ver";
    }

    @GetMapping("/descargar")
    public String pantallaDescarga() {
        return "facturas/descargar";
    }

    @GetMapping("/descargar/{ventaId}")
    public ResponseEntity<byte[]> descargarFactura(@PathVariable Long ventaId) {
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
    public String enviarSRI(@PathVariable Long ventaId) {
        facturaService.enviarSRI(ventaId);
        return "redirect:/facturas/ver/" + ventaId;
    }
}
