package com.empresa.sistema_ventas.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String nombre;
    
    @OneToMany(mappedBy = "rol")
    @JsonIgnore
    private Set<Usuario> usuarios = new HashSet<>();

    public Rol(String nombre) {
        this.nombre = nombre;
    }
}
