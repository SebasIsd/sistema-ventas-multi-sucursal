package com.empresa.sistema_ventas.service;

import com.empresa.sistema_ventas.entity.Inventario;
import com.empresa.sistema_ventas.entity.MovimientoInventario;
import com.empresa.sistema_ventas.repository.InventarioRepository;
import com.empresa.sistema_ventas.repository.MovimientoInventarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventarioService {

    private final InventarioRepository inventarioRepository;
    private final MovimientoInventarioRepository movimientoRepository;

    public InventarioService(InventarioRepository inventarioRepository,
                             MovimientoInventarioRepository movimientoRepository) {
        this.inventarioRepository = inventarioRepository;
        this.movimientoRepository = movimientoRepository;
    }

    public boolean verificarStock(Long productoId, Long sucursalId, Integer cantidad) {

        return inventarioRepository
                .findByProductoIdAndSucursalId(productoId, sucursalId)
                .map(inventario -> inventario.getStockActual() >= cantidad)
                .orElse(false);
    }

    public void descontarStock(Long productoId, Long sucursalId, Integer cantidad) {

        Inventario inventario = inventarioRepository
                .findByProductoIdAndSucursalId(productoId, sucursalId)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

        if (inventario.getStockActual() < cantidad) {
            throw new RuntimeException("Stock insuficiente");
        }

        inventario.setStockActual(inventario.getStockActual() - cantidad);

        inventarioRepository.save(inventario);

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProductoId(productoId);
        movimiento.setSucursalId(sucursalId);
        movimiento.setTipoMovimiento("SALIDA_VENTA");
        movimiento.setCantidad(cantidad);
        movimiento.setStockResultante(inventario.getStockActual());
        movimiento.setObservacion("Descuento por venta");

        movimientoRepository.save(movimiento);
    }

    public void aumentarStock(Long productoId, Long sucursalId, Integer cantidad) {

        Inventario inventario = inventarioRepository
                .findByProductoIdAndSucursalId(productoId, sucursalId)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

        inventario.setStockActual(inventario.getStockActual() + cantidad);

        inventarioRepository.save(inventario);

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProductoId(productoId);
        movimiento.setSucursalId(sucursalId);
        movimiento.setTipoMovimiento("ENTRADA_COMPRA");
        movimiento.setCantidad(cantidad);
        movimiento.setStockResultante(inventario.getStockActual());
        movimiento.setObservacion("Aumento de stock");

        movimientoRepository.save(movimiento);
    }

    public void transferirStock(Long productoId,
                                Long sucursalOrigenId,
                                Long sucursalDestinoId,
                                Integer cantidad) {

        descontarStock(productoId, sucursalOrigenId, cantidad);

        aumentarStock(productoId, sucursalDestinoId, cantidad);
    }

    public List<Inventario> getStockBajo() {
        return inventarioRepository.findStockBajo();
    }
}