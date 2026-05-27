package com.empresa.sistema_ventas.service;

import com.empresa.sistema_ventas.entity.Factura;
import com.empresa.sistema_ventas.entity.Venta;
import com.empresa.sistema_ventas.repository.FacturaRepository;
import com.empresa.sistema_ventas.repository.VentaRepository;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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

    private static final Logger log = LoggerFactory.getLogger(FacturaService.class);

    private final VentaRepository ventaRepository;
    private final FacturaRepository facturaRepository;

    @Value("${app.reports.path:classpath:/reports/}")
    private String reportsPath;

    @Value("${app.reports.output-dir:generated-reports}")
    private String reportsOutputDir;

    private volatile JasperReport compiledReport;

    public FacturaService(VentaRepository ventaRepository, FacturaRepository facturaRepository) {
        this.ventaRepository = ventaRepository;
        this.facturaRepository = facturaRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public byte[] generarPDF(Long ventaId) {
        log.info("Iniciando generación de PDF para ventaId={}", ventaId);

        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));
        log.info("Venta cargada: id={}, total={}", venta.getId(), venta.getTotal());

        Factura factura = obtenerOCrearFactura(venta);
        log.info("Factura obtenida/creada: numero={}", factura.getNumeroFactura());

        try {
            JasperReport jasperReport = cargarReporteFactura();
            log.info("JasperReport cargado OK");

            Map<String, Object> parametros = construirParametrosFactura(venta, factura);
            log.info("Parámetros construidos: SUBTOTAL={}, IVA={}, TOTAL={}",
                    parametros.get("SUBTOTAL"), parametros.get("IVA_TOTAL"), parametros.get("TOTAL"));

            List<DetalleFacturaRow> detalleRows = construirDetalleRows(venta);
            log.info("Detalle rows construidos: {} items", detalleRows.size());

            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    jasperReport,
                    parametros,
                    new JRBeanCollectionDataSource(detalleRows)
            );
            log.info("JasperPrint generado OK");

            byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);
            log.info("PDF exportado: {} bytes", pdfBytes.length);

            guardarPdfEnDisco(factura, pdfBytes);

            return pdfBytes;
        } catch (IOException | JRException ex) {
            log.error("Error generando PDF", ex);
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

    public void validarSucursalVenta(Long ventaId, Long sucursalId) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));
        Long ventaSucursalId = venta.getSucursal().getId();
        String ventaSucursalCod = venta.getSucursal().getCodigo();
        log.info("VALIDACION access: ventaId={}, sucursalVenta={}(id={}), sucursalUsuario={}(id={}) → {}",
                ventaId, ventaSucursalCod, ventaSucursalId,
                sucursalId, sucursalId,
                ventaSucursalId.equals(sucursalId) ? "OK" : "DENIED");
        if (!ventaSucursalId.equals(sucursalId)) {
            throw new RuntimeException("No tienes acceso a esta factura");
        }
    }

    private JasperReport cargarReporteFactura() throws IOException, JRException {
        if (compiledReport == null) {
            synchronized (this) {
                if (compiledReport == null) {
                    log.info("Cargando factura.jrxml desde classpath...");
                    ClassPathResource resource = new ClassPathResource("reports/factura.jrxml");
                    if (!resource.exists()) {
                        throw new IOException("No se encontro reports/factura.jrxml en classpath");
                    }
                    byte[] xmlBytes = resource.getInputStream().readAllBytes();
                    log.info("XML leido: {} bytes", xmlBytes.length);
                    log.info("Primeros 200 chars: {}", new String(xmlBytes, 0, Math.min(200, xmlBytes.length)));
                    compiledReport = JasperCompileManager.compileReport(
                            new ByteArrayInputStream(xmlBytes));
                    log.info("Reporte compilado exitosamente");
                }
            }
        }
        return compiledReport;
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
                    try {
                        Factura nuevaFactura = new Factura();
                        nuevaFactura.setVenta(venta);
                        nuevaFactura.setSucursal(venta.getSucursal());
                        nuevaFactura.setNumeroFactura(generarNumeroFactura(venta.getId()));
                        nuevaFactura.setClaveAcceso(generarClaveAcceso(venta.getId()));
                        nuevaFactura.setEstadoSri("PENDIENTE");
                        nuevaFactura.setAmbiente(1);
                        nuevaFactura.setTipoEmision(1);
                        return facturaRepository.save(nuevaFactura);
                    } catch (org.springframework.dao.DataIntegrityViolationException ex) {
                        return facturaRepository.findByVenta_Id(venta.getId())
                                .orElseThrow(() -> ex);
                    }
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
