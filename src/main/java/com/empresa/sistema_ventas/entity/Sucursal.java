package com.empresa.sistema_ventas.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "sucursales")
public class Sucursal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sucursal")
    private Long id;

    @Column(unique = true, nullable = false, length = 10)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String ciudad;

    private String direccion;
    private String telefono;
    private boolean activo = true;

    public Sucursal(String codigo, String ciudad) {
        this.codigo = codigo;
        this.ciudad = ciudad;
    }
}