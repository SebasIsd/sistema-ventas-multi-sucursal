package com.empresa.sistema_ventas.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashSet;
import java.util.Set;

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

    @Column(columnDefinition = "TEXT")
    private String direccion;

    @Column(length = 20)
    private String telefono;

    @Column(nullable = false)
    private boolean activo = true;

    @OneToMany(mappedBy = "sucursal")
    @JsonIgnore
    private Set<Usuario> usuarios = new HashSet<>();

    @OneToMany(mappedBy = "sucursal")
    @JsonIgnore
    private Set<Inventario> inventarios = new HashSet<>();

    @OneToMany(mappedBy = "sucursal")
    @JsonIgnore
    private Set<Venta> ventas = new HashSet<>();

    @OneToMany(mappedBy = "sucursal")
    @JsonIgnore
    private Set<Factura> facturas = new HashSet<>();

    public Sucursal(String codigo, String ciudad) {
        this.codigo = codigo;
        this.ciudad = ciudad;
    }
}