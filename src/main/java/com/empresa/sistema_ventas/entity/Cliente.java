package com.empresa.sistema_ventas.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Long id;

    @jakarta.validation.constraints.NotBlank(message = "La cédula o RUC es obligatoria")
    @jakarta.validation.constraints.Pattern(regexp = "^\\d{10}(\\d{3})?$", message = "La cédula debe tener 10 dígitos y el RUC 13 dígitos y contener solo números")
    @Column(name = "cedula_ruc", unique = true, nullable = false, length = 20)
    private String cedulaRuc;

    @jakarta.validation.constraints.NotBlank(message = "El nombre es obligatorio")
    @jakarta.validation.constraints.Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
    @jakarta.validation.constraints.Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]+$", message = "El nombre solo debe contener letras y espacios")
    @Column(nullable = false, length = 150)
    private String nombres;

    @jakarta.validation.constraints.NotBlank(message = "El apellido es obligatorio")
    @jakarta.validation.constraints.Size(max = 150, message = "El apellido no puede superar los 150 caracteres")
    @jakarta.validation.constraints.Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]+$", message = "El apellido solo debe contener letras y espacios")
    @Column(length = 150)
    private String apellidos;

    @Column(columnDefinition = "TEXT")
    private String direccion;

    @jakarta.validation.constraints.Pattern(regexp = "^$|^\\+?\\d{7,15}$", message = "El teléfono debe contener entre 7 y 15 dígitos numéricos")
    @Column(length = 20)
    private String telefono;

    @jakarta.validation.constraints.Email(message = "El formato de correo no es válido")
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