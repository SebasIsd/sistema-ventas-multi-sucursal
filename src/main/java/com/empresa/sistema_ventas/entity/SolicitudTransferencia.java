package com.empresa.sistema_ventas.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes_transferencia")
@Getter
@Setter
@NoArgsConstructor
public class SolicitudTransferencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    // Sucursal que TIENE el stock (donde está el bodeguero que acepta)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sucursal_origen", nullable = false)
    private Sucursal sucursalOrigen;

    // Sucursal del cajero que solicita
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sucursal_destino", nullable = false)
    private Sucursal sucursalDestino;

    @Column(nullable = false)
    private Integer cantidad;

    // PENDIENTE, ACEPTADA, RECHAZADA
    @Column(nullable = false, length = 20)
    private String estado = "PENDIENTE";

    @Column(columnDefinition = "TEXT")
    private String observacion;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud = LocalDateTime.now();

    @PrePersist
    private void prePersist() {
        if (fechaSolicitud == null) {
            fechaSolicitud = LocalDateTime.now();
        }
        if (estado == null) {
            estado = "PENDIENTE";
        }
    }
}
