package com.empresa.sistema_ventas.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "movimientos_inventario")
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sucursal", nullable = false)
    private Sucursal sucursal;

    @Column(name = "tipo_movimiento", nullable = false, length = 20)
    private String tipoMovimiento;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "stock_resultante", nullable = false)
    private Integer stockResultante;

    @Column(columnDefinition = "TEXT")
    private String observacion;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        fecha = LocalDateTime.now();
    }
}