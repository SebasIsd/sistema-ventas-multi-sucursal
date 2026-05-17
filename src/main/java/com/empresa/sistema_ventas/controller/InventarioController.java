package com.empresa.sistema_ventas.controller;

import com.empresa.sistema_ventas.dto.AjusteStockRequest;
import com.empresa.sistema_ventas.dto.TransferenciaStockRequest;
import com.empresa.sistema_ventas.entity.Inventario;
import com.empresa.sistema_ventas.service.InventarioService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventario")
public class InventarioController {

    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @GetMapping("/stock")
    public List<Inventario> getStockBajo() {
        return inventarioService.getStockBajo();
    }

    @PostMapping("/ajustar")
    public String ajustarStock(@RequestBody AjusteStockRequest request) {
        inventarioService.aumentarStock(
                request.getProductoId(),
                request.getSucursalId(),
                request.getCantidad()
        );

        return "Stock ajustado correctamente";
    }

    @PostMapping("/transferir")
    public String transferirStock(@RequestBody TransferenciaStockRequest request) {
        inventarioService.transferirStock(
                request.getProductoId(),
                request.getSucursalOrigenId(),
                request.getSucursalDestinoId(),
                request.getCantidad()
        );

        return "Stock transferido correctamente";
    }
}