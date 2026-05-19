package com.empresa.sistema_ventas.dto;

import lombok.Data;

import java.util.List;

@Data
public class VentaRequest {

    private Long clienteId;
    private Integer usuarioId;
    private List<ItemVentaDTO> items;
}