package com.empresa.sistema_ventas.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "facturas")
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_factura")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_venta", nullable = false, unique = true)
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sucursal", nullable = false)
    private Sucursal sucursal;

    @Column(name = "numero_factura", unique = true, nullable = false, length = 50)
    private String numeroFactura;

    @Column(name = "clave_acceso", unique = true, nullable = false, length = 49)
    private String claveAcceso;

    @Column(name = "numero_autorizacion", length = 50)
    private String numeroAutorizacion;

    @Column(name = "fecha_autorizacion")
    private LocalDateTime fechaAutorizacion;

    @Column(name = "estado_sri", length = 20)
    private String estadoSri = "PENDIENTE";

    @Column(name = "motivo_rechazo", columnDefinition = "TEXT")
    private String motivoRechazo;

    @Column(nullable = false)
    private Integer ambiente = 1; // 1: PRUEBAS, 2: PRODUCCION

    @Column(name = "tipo_emision")
    private Integer tipoEmision = 1; // 1: NORMAL, 2: CONTINGENCIA

    @Column(name = "ruta_pdf", columnDefinition = "TEXT")
    private String rutaPdf;

    @Column(name = "xml_firmado", columnDefinition = "TEXT")
    private String xmlFirmado;

    @Column(name = "fecha_emision")
    private LocalDateTime fechaEmision = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        fechaEmision = LocalDateTime.now();
    }
}