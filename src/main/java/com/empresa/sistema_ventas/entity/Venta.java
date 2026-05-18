package com.empresa.sistema_ventas.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@Entity
@Table(name = "ventas", indexes = {
        @Index(name = "idx_venta_fecha", columnList = "fecha")
})
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_venta")
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sucursal", nullable = false)
    private Sucursal sucursal;

    @Column(name = "subtotal_sin_iva", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotalSinIva = BigDecimal.ZERO;

    @Column(name = "subtotal_con_iva", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotalConIva = BigDecimal.ZERO;

    @Column(name = "iva_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal ivaTotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(length = 20)
    private String estado = "PAGADO";

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<DetalleVenta> detalles = new HashSet<>();

    @OneToOne(mappedBy = "venta", cascade = CascadeType.ALL)
    @JsonIgnore
    private Factura factura;

    @PrePersist
    protected void onCreate() {
        fecha = LocalDateTime.now();
    }

    // ✅ MÉTODO CORREGIDO - Calcular totales automáticamente
    public void calcularTotales() {
        // Calcular subtotal sin IVA
        this.subtotalSinIva = BigDecimal.ZERO;
        for (DetalleVenta detalle : detalles) {
            this.subtotalSinIva = this.subtotalSinIva.add(detalle.getSubtotal());
        }

        // Calcular IVA total (suma del IVA de cada detalle)
        this.ivaTotal = BigDecimal.ZERO;
        for (DetalleVenta detalle : detalles) {
            BigDecimal porcentajeIva = detalle.getProducto().getPorcentajeIva();
            BigDecimal ivaDelDetalle = detalle.getSubtotal()
                    .multiply(porcentajeIva)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            this.ivaTotal = this.ivaTotal.add(ivaDelDetalle);
        }

        // Calcular subtotal con IVA y total
        this.subtotalConIva = this.subtotalSinIva.add(this.ivaTotal);
        this.total = this.subtotalConIva;
    }
}