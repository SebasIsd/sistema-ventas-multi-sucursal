package com.empresa.sistema_ventas.service;

import com.empresa.sistema_ventas.entity.DetalleVenta;
import com.empresa.sistema_ventas.entity.Factura;
import com.empresa.sistema_ventas.entity.Venta;
import com.empresa.sistema_ventas.repository.FacturaRepository;
import com.empresa.sistema_ventas.repository.VentaRepository;
import jakarta.transaction.Transactional;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FacturaService {

    private final VentaRepository ventaRepository;
    private final FacturaRepository facturaRepository;
    private final ResourceLoader resourceLoader;

    @Value("${app.reports.path:classpath:/reports/}")
    private String reportsPath;

    @Value("${app.reports.output-dir:generated-reports}")
    private String reportsOutputDir;

    public FacturaService(
            VentaRepository ventaRepository,
            FacturaRepository facturaRepository,
            ResourceLoader resourceLoader
    ) {
        this.ventaRepository = ventaRepository;
        this.facturaRepository = facturaRepository;
        this.resourceLoader = resourceLoader;
    }

    @Transactional
    public byte[] generarPDF(Long ventaId) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

        Factura factura = obtenerOCrearFactura(venta);

        try {
            JasperReport jasperReport = cargarReporteFactura();
            Map<String, Object> parametros = construirParametrosFactura(venta, factura);
            List<DetalleFacturaRow> detalleRows = construirDetalleRows(venta);

            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    jasperReport,
                    parametros,
                    new JRBeanCollectionDataSource(detalleRows)
            );

            byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);
            guardarPdfEnDisco(factura, pdfBytes);

            return pdfBytes;
        } catch (IOException | JRException ex) {
            throw new RuntimeException("No se pudo generar el PDF de factura", ex);
        }
    }

    @Transactional
    public Factura enviarSRI(Long ventaId) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

        Factura factura = obtenerOCrearFactura(venta);

        // Simula la autorizacion del SRI para el flujo funcional del sistema.
        factura.setEstadoSri("AUTORIZADO");
        factura.setNumeroAutorizacion("AUTH-" + String.format("%010d", venta.getId()));
        factura.setFechaAutorizacion(LocalDateTime.now());
        factura.setMotivoRechazo(null);

        return facturaRepository.save(factura);
    }

    public Optional<Factura> obtenerFacturaPorVentaId(Long ventaId) {
        return facturaRepository.findByVenta_Id(ventaId);
    }

    private JasperReport cargarReporteFactura() throws IOException, JRException {
        String basePath = reportsPath.endsWith("/") ? reportsPath : reportsPath + "/";
        Resource resource = resourceLoader.getResource(basePath + "factura.jrxml");

        try (InputStream inputStream = resource.getInputStream()) {
            return JasperCompileManager.compileReport(inputStream);
        }
    }

    private Map<String, Object> construirParametrosFactura(Venta venta, Factura factura) {
        Map<String, Object> params = new HashMap<>();

        String clienteNombre = venta.getCliente() != null
                ? (venta.getCliente().getNombres() + " " + (venta.getCliente().getApellidos() != null ? venta.getCliente().getApellidos() : "")).trim()
                : "Consumidor Final";

        String clienteIdentificacion = venta.getCliente() != null
                ? venta.getCliente().getCedulaRuc()
                : "9999999999";

        String sucursal = venta.getSucursal() != null
                ? venta.getSucursal().getCodigo() + " - " + venta.getSucursal().getCiudad()
                : "N/A";

        params.put("NUMERO_FACTURA", factura.getNumeroFactura());
        params.put("FECHA_EMISION", venta.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        params.put("CLIENTE_NOMBRE", clienteNombre);
        params.put("CLIENTE_IDENTIFICACION", clienteIdentificacion);
        params.put("SUCURSAL", sucursal);
        params.put("ESTADO_SRI", factura.getEstadoSri());
        params.put("CLAVE_ACCESO", factura.getClaveAcceso());
        params.put("SUBTOTAL", venta.getSubtotalSinIva());
        params.put("IVA_TOTAL", venta.getIvaTotal());
        params.put("TOTAL", venta.getTotal());

        return params;
    }

    private List<DetalleFacturaRow> construirDetalleRows(Venta venta) {
        return venta.getDetalles().stream()
                .map(detalle -> new DetalleFacturaRow(
                        detalle.getProducto() != null ? detalle.getProducto().getNombre() : "Producto",
                        detalle.getCantidad(),
                        detalle.getPrecioUnitario(),
                        detalle.getSubtotal()
                ))
                .collect(Collectors.toList());
    }

    private void guardarPdfEnDisco(Factura factura, byte[] pdfBytes) throws IOException {
        Path outputDir = Paths.get(reportsOutputDir);
        Files.createDirectories(outputDir);

        Path outputPath = outputDir.resolve("factura-" + factura.getVenta().getId() + ".pdf");
        Files.write(outputPath, pdfBytes);

        factura.setRutaPdf(outputPath.toString());
        facturaRepository.save(factura);
    }

    private Factura obtenerOCrearFactura(Venta venta) {
        return facturaRepository.findByVenta_Id(venta.getId())
                .orElseGet(() -> {
                    Factura nuevaFactura = new Factura();
                    nuevaFactura.setVenta(venta);
                    nuevaFactura.setSucursal(venta.getSucursal());
                    nuevaFactura.setNumeroFactura(generarNumeroFactura(venta.getId()));
                    nuevaFactura.setClaveAcceso(generarClaveAcceso(venta.getId()));
                    nuevaFactura.setEstadoSri("PENDIENTE");
                    nuevaFactura.setAmbiente(1);
                    nuevaFactura.setTipoEmision(1);
                    return facturaRepository.save(nuevaFactura);
                });
    }

    private String generarNumeroFactura(Long ventaId) {
        return "FAC-" + String.format("%09d", ventaId);
    }

    private String generarClaveAcceso(Long ventaId) {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
        return fecha + String.format("%041d", ventaId);
    }

    public static class DetalleFacturaRow {
        private final String producto;
        private final Integer cantidad;
        private final BigDecimal precioUnitario;
        private final BigDecimal subtotal;

        public DetalleFacturaRow(String producto, Integer cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {
            this.producto = producto;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
            this.subtotal = subtotal;
        }

        public String getProducto() {
            return producto;
        }

        public Integer getCantidad() {
            return cantidad;
        }

        public BigDecimal getPrecioUnitario() {
            return precioUnitario;
        }

        public BigDecimal getSubtotal() {
            return subtotal;
        }
    }
}
