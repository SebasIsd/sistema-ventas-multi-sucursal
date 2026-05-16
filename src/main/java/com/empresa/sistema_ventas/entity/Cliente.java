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
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Long id;

    @Column(name = "cedula_ruc", unique = true, nullable = false, length = 20)
    private String cedulaRuc;

    @Column(nullable = false, length = 150)
    private String nombres;

    @Column(length = 150)
    private String apellidos;

    @Column(columnDefinition = "TEXT")
    private String direccion;

    @Column(length = 20)
    private String telefono;

    @Column(length = 150)
    private String email;

    @Column(name = "tipo_cliente", length = 20)
    private String tipoCliente = "CONSUMIDOR_FINAL";

    @Column(nullable = false)
    private boolean activo = true;

    @OneToMany(mappedBy = "cliente")
    @JsonIgnore
    private Set<Venta> ventas = new HashSet<>();

    public Cliente(String cedulaRuc, String nombres) {
        this.cedulaRuc = cedulaRuc;
        this.nombres = nombres;
    }
}