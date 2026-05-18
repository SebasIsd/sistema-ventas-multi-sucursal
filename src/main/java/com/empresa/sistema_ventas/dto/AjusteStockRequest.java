package com.empresa.sistema_ventas.dto;

import lombok.Data;

@Data
public class AjusteStockRequest {

    private Integer productoId;
    private Long sucursalId;
    private Integer cantidad;
    private String observacion;
}