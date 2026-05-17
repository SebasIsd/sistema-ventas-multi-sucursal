package com.empresa.sistema_ventas.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_inventario")
@Data
@NoArgsConstructor
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento")
    private Long idMovimiento;

    @Column(name = "id_producto", nullable = false)
    private Long productoId;

    @Column(name = "id_sucursal", nullable = false)
    private Long sucursalId;

    @Column(name = "tipo_movimiento", nullable = false)
    private String tipoMovimiento;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "stock_resultante", nullable = false)
    private Integer stockResultante;

    @Column(columnDefinition = "TEXT")
    private String observacion;

    private LocalDateTime fecha = LocalDateTime.now();
}