package com.empresa.sistema_ventas.dto;

import lombok.Data;

@Data
public class TransferenciaStockRequest {

    private Integer productoId;
    private Long sucursalOrigenId;
    private Long sucursalDestinoId;
    private Integer cantidad;
    private String observacion;
}