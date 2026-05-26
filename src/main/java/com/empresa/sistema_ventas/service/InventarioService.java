package com.empresa.sistema_ventas.service;

import com.empresa.sistema_ventas.entity.Inventario;
import com.empresa.sistema_ventas.entity.MovimientoInventario;
import com.empresa.sistema_ventas.entity.Producto;
import com.empresa.sistema_ventas.entity.Sucursal;
import com.empresa.sistema_ventas.repository.InventarioRepository;
import com.empresa.sistema_ventas.repository.MovimientoInventarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public boolean verificarStock(Integer productoId, Long sucursalId, Integer cantidad) {
        return inventarioRepository
                .findByProducto_IdProductoAndSucursal_Id(productoId, sucursalId)
                .map(inventario -> inventario.getStockActual() >= cantidad)
                .orElse(false);
    }

    public List<Inventario> listarTodos() {
        return inventarioRepository.findAll();
    }

    public List<Inventario> listarInventarioPorSucursal(Sucursal sucursal) {
        return inventarioRepository.findBySucursal(sucursal);
    }

    public List<MovimientoInventario> getHistorialMovimientos() {
        return movimientoRepository.findAllByOrderByFechaMovimientoDesc();
    }

    public List<Inventario> getStockBajo() {
        return inventarioRepository.findStockBajo();
    }

    @Transactional
    public void descontarStock(Integer productoId, Long sucursalId, Integer cantidad) {
        Inventario inventario = inventarioRepository
                .findByProducto_IdProductoAndSucursal_Id(productoId, sucursalId)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

        if (inventario.getStockActual() < cantidad) {
            throw new RuntimeException("Stock insuficiente");
        }

        inventario.setStockActual(inventario.getStockActual() - cantidad);
        inventarioRepository.save(inventario);

        guardarMovimiento(productoId, sucursalId, "SALIDA_VENTA", cantidad,
                inventario.getStockActual(), "Descuento por venta");
    }

    @Transactional
    public void aumentarStock(Integer productoId, Long sucursalId, Integer cantidad) {
        aumentarStock(productoId, sucursalId, cantidad, "Aumento de stock");
    }

    @Transactional
    public void aumentarStock(Integer productoId, Long sucursalId, Integer cantidad, String observacion) {
        Inventario inventario = inventarioRepository
                .findByProducto_IdProductoAndSucursal_Id(productoId, sucursalId)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

        inventario.setStockActual(inventario.getStockActual() + cantidad);
        inventarioRepository.save(inventario);

        guardarMovimiento(productoId, sucursalId, "ENTRADA_AJUSTE", cantidad,
                inventario.getStockActual(), observacion);
    }

    @Transactional
    public void transferirStock(Integer productoId,
                                Long sucursalOrigenId,
                                Long sucursalDestinoId,
                                Integer cantidad) {
        transferirStock(productoId, sucursalOrigenId, sucursalDestinoId, cantidad, "Transferencia entre sucursales");
    }

    @Transactional
    public void transferirStock(Integer productoId,
                                Long sucursalOrigenId,
                                Long sucursalDestinoId,
                                Integer cantidad,
                                String observacion) {

        if (sucursalOrigenId.equals(sucursalDestinoId)) {
            throw new RuntimeException("La sucursal origen y destino no pueden ser iguales");
        }

        Inventario inventarioOrigen = inventarioRepository
                .findByProducto_IdProductoAndSucursal_Id(productoId, sucursalOrigenId)
                .orElseThrow(() -> new RuntimeException("Inventario de origen no encontrado"));

        Inventario inventarioDestino = inventarioRepository
                .findByProducto_IdProductoAndSucursal_Id(productoId, sucursalDestinoId)
                .orElseThrow(() -> new RuntimeException("Inventario de destino no encontrado"));

        if (inventarioOrigen.getStockActual() < cantidad) {
            throw new RuntimeException("Stock insuficiente en la sucursal origen");
        }

        inventarioOrigen.setStockActual(inventarioOrigen.getStockActual() - cantidad);
        inventarioDestino.setStockActual(inventarioDestino.getStockActual() + cantidad);

        inventarioRepository.save(inventarioOrigen);
        inventarioRepository.save(inventarioDestino);

        guardarMovimiento(productoId, sucursalOrigenId, "SALIDA_TRANSFERENCIA", cantidad,
                inventarioOrigen.getStockActual(), observacion);

        guardarMovimiento(productoId, sucursalDestinoId, "ENTRADA_TRANSFERENCIA", cantidad,
                inventarioDestino.getStockActual(), observacion);
    }

    private void guardarMovimiento(Integer productoId,
                                   Long sucursalId,
                                   String tipoMovimiento,
                                   Integer cantidad,
                                   Integer stockResultante,
                                   String observacion) {

        MovimientoInventario movimiento = new MovimientoInventario();

        Producto producto = new Producto();
        producto.setIdProducto(productoId);
        movimiento.setProducto(producto);

        Sucursal sucursal = new Sucursal();
        sucursal.setId(sucursalId);
        movimiento.setSucursal(sucursal);

        movimiento.setTipoMovimiento(tipoMovimiento);
        movimiento.setCantidad(cantidad);
        movimiento.setStockResultante(stockResultante);
        movimiento.setObservacion(observacion);

        movimientoRepository.save(movimiento);
    }
}