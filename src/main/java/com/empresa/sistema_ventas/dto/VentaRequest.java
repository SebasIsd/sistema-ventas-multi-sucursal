package com.empresa.sistema_ventas.dto;

import lombok.Data;

import java.util.List;

@Data
public class VentaRequest {

    private Long clienteId;
    private Long usuarioId;
    private List<ItemVentaDTO> items;
}