package com.empresa.sistema_ventas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClienteDTO {

    @NotBlank(message = "La cédula o RUC es obligatoria")
    @Pattern(regexp = "^\\d{10}(\\d{3})?$", message = "La cédula debe tener 10 dígitos y el RUC 13 dígitos y contener solo números")
    private String cedulaRuc;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]+$", message = "El nombre solo debe contener letras y espacios")
    private String nombres;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no puede superar los 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]+$", message = "El apellido solo debe contener letras y espacios")
    private String apellidos;

    @Size(max = 255, message = "La dirección no puede superar los 255 caracteres")
    private String direccion;

    @Pattern(regexp = "^$|^\\+?\\d{7,15}$", message = "El teléfono debe contener entre 7 y 15 dígitos numéricos")
    private String telefono;

    @Email(message = "El formato de correo no es válido")
    @Size(max = 150, message = "El correo no puede superar los 150 caracteres")
    private String email;

    @NotBlank(message = "El tipo de cliente es obligatorio")
    private String tipoCliente;
}