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
        // CORREGIDO: Nombre del método del repositorio
        return inventarioRepository
                .findByProducto_IdProductoAndSucursal_Id(productoId, sucursalId)
                .map(inventario -> inventario.getStockActual() >= cantidad)
                .orElse(false);
    }

    @Transactional
    public void descontarStock(Integer productoId, Long sucursalId, Integer cantidad) {
        // CORREGIDO: Nombre del método del repositorio
        Inventario inventario = inventarioRepository
                .findByProducto_IdProductoAndSucursal_Id(productoId, sucursalId)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

        if (inventario.getStockActual() < cantidad) {
            throw new RuntimeException("Stock insuficiente");
        }

        inventario.setStockActual(inventario.getStockActual() - cantidad);
        inventarioRepository.save(inventario);

        MovimientoInventario movimiento = new MovimientoInventario();

        Producto prod = new Producto();
        prod.setIdProducto(productoId);
        movimiento.setProducto(prod);

        Sucursal suc = new Sucursal();
        suc.setId(sucursalId);
        movimiento.setSucursal(suc);

        movimiento.setTipoMovimiento("SALIDA_VENTA");
        movimiento.setCantidad(cantidad);
        movimiento.setStockResultante(inventario.getStockActual());
        movimiento.setObservacion("Descuento por venta");

        movimientoRepository.save(movimiento);
    }

    @Transactional
    public void aumentarStock(Integer productoId, Long sucursalId, Integer cantidad) {
        // CORREGIDO: Nombre del método del repositorio
        Inventario inventario = inventarioRepository
                .findByProducto_IdProductoAndSucursal_Id(productoId, sucursalId)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

        inventario.setStockActual(inventario.getStockActual() + cantidad);
        inventarioRepository.save(inventario);

        MovimientoInventario movimiento = new MovimientoInventario();

        Producto prod = new Producto();
        prod.setIdProducto(productoId);
        movimiento.setProducto(prod);

        Sucursal suc = new Sucursal();
        suc.setId(sucursalId);
        movimiento.setSucursal(suc);

        movimiento.setTipoMovimiento("ENTRADA_COMPRA");
        movimiento.setCantidad(cantidad);
        movimiento.setStockResultante(inventario.getStockActual());
        movimiento.setObservacion("Aumento de stock");

        movimientoRepository.save(movimiento);
    }

    @Transactional
    public void transferirStock(Integer productoId,
                                Long sucursalOrigenId,
                                Long sucursalDestinoId,
                                Integer cantidad) {
        descontarStock(productoId, sucursalOrigenId, cantidad);
        aumentarStock(productoId, sucursalDestinoId, cantidad);
    }

    public List<Inventario> getStockBajo() {
        return inventarioRepository.findStockBajo();
    }
    public List<Inventario> listarTodos() {return inventarioRepository.findAll();}
}