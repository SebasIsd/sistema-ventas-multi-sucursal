package com.empresa.sistema_ventas.service;

import com.empresa.sistema_ventas.entity.Inventario;
import com.empresa.sistema_ventas.entity.Venta;
import com.empresa.sistema_ventas.repository.InventarioRepository;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReporteService {

    private static final Logger log = LoggerFactory.getLogger(ReporteService.class);

    private final VentaRepository ventaRepository;
    private final InventarioRepository inventarioRepository;

    private volatile JasperReport compiledVentasReport;
    private volatile JasperReport compiledInventarioReport;

    public ReporteService(VentaRepository ventaRepository, InventarioRepository inventarioRepository) {
        this.ventaRepository = ventaRepository;
        this.inventarioRepository = inventarioRepository;
    }

    public byte[] generarReporteVentas(Long sucursalId, String fechaInicio, String fechaFin) {
        try {
            JasperReport jasperReport = cargarReporteVentas();
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("FECHA_INICIO", fechaInicio != null ? fechaInicio : "Todos");
            parametros.put("FECHA_FIN", fechaFin != null ? fechaFin : "Todos");
            parametros.put("SUCURSAL", sucursalId != null ? "Sucursal ID: " + sucursalId : "Todas");

            List<VentaRow> rows = construirVentasRows(sucursalId, fechaInicio, fechaFin);

            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    jasperReport, parametros, new JRBeanCollectionDataSource(rows));

            return JasperExportManager.exportReportToPdf(jasperPrint);
        } catch (IOException | JRException ex) {
            log.error("Error generando reporte de ventas", ex);
            throw new RuntimeException("No se pudo generar el reporte de ventas", ex);
        }
    }

    public byte[] generarReporteInventario(Long sucursalId) {
        try {
            JasperReport jasperReport = cargarReporteInventario();
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("SUCURSAL", sucursalId != null ? "Sucursal ID: " + sucursalId : "Todas");

            List<InventarioRow> rows = construirInventarioRows(sucursalId);

            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    jasperReport, parametros, new JRBeanCollectionDataSource(rows));

            return JasperExportManager.exportReportToPdf(jasperPrint);
        } catch (IOException | JRException ex) {
            log.error("Error generando reporte de inventario", ex);
            throw new RuntimeException("No se pudo generar el reporte de inventario", ex);
        }
    }

    private List<VentaRow> construirVentasRows(Long sucursalId, String fechaInicio, String fechaFin) {
        List<Venta> ventas;
        if (sucursalId != null) {
            ventas = ventaRepository.findBySucursalId(sucursalId);
        } else {
            ventas = ventaRepository.findAll();
        }

        return ventas.stream()
                .map(v -> new VentaRow(
                        v.getId(),
                        v.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                        v.getCliente() != null ? v.getCliente().getNombres() + " " + v.getCliente().getApellidos() : "Consumidor Final",
                        v.getTotal()
                ))
                .collect(Collectors.toList());
    }

    private List<InventarioRow> construirInventarioRows(Long sucursalId) {
        List<Inventario> inventarios;
        if (sucursalId != null) {
            inventarios = inventarioRepository.findBySucursal_Id(sucursalId);
        } else {
            inventarios = inventarioRepository.findAll();
        }

        return inventarios.stream()
                .map(i -> new InventarioRow(
                        i.getProducto().getNombre(),
                        i.getSucursal().getCiudad(),
                        i.getStockActual(),
                        i.getStockMinimo(),
                        i.getStockActual() <= i.getStockMinimo() ? "BAJO" : "OK"
                ))
                .collect(Collectors.toList());
    }

    private JasperReport cargarReporteVentas() throws IOException, JRException {
        if (compiledVentasReport == null) {
            synchronized (this) {
                if (compiledVentasReport == null) {
                    log.info("Compilando reporte de ventas...");
                    ClassPathResource resource = new ClassPathResource("reports/ventas.jrxml");
                    byte[] xmlBytes = resource.getInputStream().readAllBytes();
                    compiledVentasReport = JasperCompileManager.compileReport(
                            new ByteArrayInputStream(xmlBytes));
                    log.info("Reporte de ventas compilado OK");
                }
            }
        }
        return compiledVentasReport;
    }

    private JasperReport cargarReporteInventario() throws IOException, JRException {
        if (compiledInventarioReport == null) {
            synchronized (this) {
                if (compiledInventarioReport == null) {
                    log.info("Compilando reporte de inventario...");
                    ClassPathResource resource = new ClassPathResource("reports/inventario.jrxml");
                    byte[] xmlBytes = resource.getInputStream().readAllBytes();
                    compiledInventarioReport = JasperCompileManager.compileReport(
                            new ByteArrayInputStream(xmlBytes));
                    log.info("Reporte de inventario compilado OK");
                }
            }
        }
        return compiledInventarioReport;
    }

    // DTO rows
    public record VentaRow(Long idVenta, String fecha, String cliente, BigDecimal total) {}
    public record InventarioRow(String producto, String sucursal, Integer stock, Integer stockMin, String estado) {}
}