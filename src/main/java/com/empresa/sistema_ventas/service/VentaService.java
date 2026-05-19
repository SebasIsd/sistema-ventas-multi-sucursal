package com.empresa.sistema_ventas.service;

import com.empresa.sistema_ventas.dto.ItemVentaDTO;
import com.empresa.sistema_ventas.entity.*;
import com.empresa.sistema_ventas.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final InventarioRepository inventarioRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    public VentaService(
            VentaRepository ventaRepository,
            ClienteRepository clienteRepository,
            UsuarioRepository usuarioRepository,
            ProductoRepository productoRepository,
            InventarioRepository inventarioRepository,
            MovimientoInventarioRepository movimientoInventarioRepository
    ) {
        this.ventaRepository = ventaRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.inventarioRepository = inventarioRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
    }

    @Transactional
    public Venta procesarVenta(Long clienteId, List<ItemVentaDTO> items, Long usuarioId) {

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Sucursal sucursal = usuario.getSucursal();

        Venta venta = new Venta();
        venta.setCliente(cliente);
        venta.setUsuario(usuario);
        venta.setSucursal(sucursal);
        venta.setEstado("PAGADO");

        Set<DetalleVenta> detalles = new HashSet<>();

        for (ItemVentaDTO item : items) {

            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            Inventario inventario = inventarioRepository
                    .findByProducto_IdProductoAndSucursal_Id(
                            producto.getIdProducto(),
                            sucursal.getId()
                    )
                    .orElseThrow(() -> new RuntimeException(
                            "No existe inventario para el producto: " + producto.getNombre()
                    ));

            if (inventario.getStockActual() < item.getCantidad()) {
                throw new RuntimeException(
                        "Stock insuficiente para el producto: " + producto.getNombre()
                );
            }

            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecioUnitario());

            detalle.prePersist();

            detalles.add(detalle);

            inventario.setStockActual(
                    inventario.getStockActual() - item.getCantidad()
            );

            inventarioRepository.save(inventario);

            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setProducto(producto);
            movimiento.setSucursal(sucursal);
            movimiento.setTipoMovimiento("SALIDA_VENTA");
            movimiento.setCantidad(item.getCantidad());
            movimiento.setStockResultante(inventario.getStockActual());
            movimiento.setObservacion(
                    "Venta realizada ID producto: " + producto.getIdProducto()
            );

            movimientoInventarioRepository.save(movimiento);
        }

        venta.setDetalles(detalles);

        venta.calcularTotales();

        return ventaRepository.save(venta);
    }

    @Transactional
    public Venta anularVenta(Long ventaId) {

        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

        venta.setEstado("ANULADO");

        for (DetalleVenta detalle : venta.getDetalles()) {

            Inventario inventario = inventarioRepository
                    .findByProducto_IdProductoAndSucursal_Id(
                    detalle.getProducto().getIdProducto(),
                    venta.getSucursal().getId()
            )
                    .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

            inventario.setStockActual(
                    inventario.getStockActual() + detalle.getCantidad()
            );

            inventarioRepository.save(inventario);

            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setProducto(detalle.getProducto());
            movimiento.setSucursal(venta.getSucursal());
            movimiento.setTipoMovimiento("AJUSTE");
            movimiento.setCantidad(detalle.getCantidad());
            movimiento.setStockResultante(inventario.getStockActual());
            movimiento.setObservacion(
                    "Anulación venta ID: " + venta.getId()
            );

            movimientoInventarioRepository.save(movimiento);
        }

        return ventaRepository.save(venta);
    }

    public List<Venta> getHistorialVentas(Long sucursalId) {
        return ventaRepository.findBySucursalId(sucursalId);
    }
}