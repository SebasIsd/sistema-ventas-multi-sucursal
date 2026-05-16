package com.empresa.sistema_ventas.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Long id;

    @Column(unique = true, length = 50)
    private String codigoBarras;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "porcentaje_iva", precision = 5, scale = 2)
    private BigDecimal porcentajeIva = BigDecimal.valueOf(15.00);

    @Column(nullable = false)
    private boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    @OneToMany(mappedBy = "producto")
    @JsonIgnore
    private Set<Inventario> inventarios = new HashSet<>();

    @OneToMany(mappedBy = "producto")
    @JsonIgnore
    private Set<DetalleVenta> detallesVenta = new HashSet<>();

    @OneToMany(mappedBy = "producto")
    @JsonIgnore
    private Set<MovimientoInventario> movimientos = new HashSet<>();

    public Producto(String nombre, BigDecimal precioUnitario) {
        this.nombre = nombre;
        this.precioUnitario = precioUnitario;
    }
}