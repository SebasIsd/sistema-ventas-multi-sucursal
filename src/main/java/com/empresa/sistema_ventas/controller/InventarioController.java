package com.empresa.sistema_ventas.controller;

import com.empresa.sistema_ventas.dto.AjusteStockRequest;
import com.empresa.sistema_ventas.dto.TransferenciaStockRequest;
import com.empresa.sistema_ventas.entity.Inventario;
import com.empresa.sistema_ventas.entity.MovimientoInventario;
import com.empresa.sistema_ventas.entity.Sucursal;
import com.empresa.sistema_ventas.service.InventarioService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventario")
@PreAuthorize("hasAnyRole('BODEGA', 'ADMIN')")
public class InventarioController {

    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @GetMapping("/stock")
    public List<Inventario> listarStock() {
        return inventarioService.listarTodos();
    }

    @GetMapping("/stock/sucursal/{sucursalId}")
    public List<Inventario> listarStockPorSucursal(@PathVariable Long sucursalId) {
        Sucursal sucursal = new Sucursal();
        sucursal.setId(sucursalId);
        return inventarioService.listarInventarioPorSucursal(sucursal);
    }

    @GetMapping("/stock-bajo")
    public List<Inventario> getStockBajo() {
        return inventarioService.getStockBajo();
    }

    @PostMapping("/ajustes/guardar")
    public String ajustarStock(@RequestBody AjusteStockRequest request) {
        inventarioService.aumentarStock(
                request.getProductoId(),
                request.getSucursalId(),
                request.getCantidad(),
                request.getObservacion()
        );

        return "Stock ajustado correctamente";
    }

    @PostMapping("/transferencias/procesar")
    public String transferirStock(@RequestBody TransferenciaStockRequest request) {
        inventarioService.transferirStock(
                request.getProductoId(),
                request.getSucursalOrigenId(),
                request.getSucursalDestinoId(),
                request.getCantidad(),
                request.getObservacion()
        );

        return "Stock transferido correctamente";
    }

    @GetMapping("/historial")
    public List<MovimientoInventario> historial() {
        return inventarioService.getHistorialMovimientos();
    }
}