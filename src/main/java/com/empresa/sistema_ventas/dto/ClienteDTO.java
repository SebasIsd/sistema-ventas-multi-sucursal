package com.empresa.sistema_ventas.dto;

import lombok.Data;

@Data
public class ClienteDTO {

    private String cedulaRuc;
    private String nombres;
    private String apellidos;
    private String direccion;
    private String telefono;
    private String email;
    private String tipoCliente;
}