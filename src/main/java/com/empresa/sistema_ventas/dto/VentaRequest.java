package com.empresa.sistema_ventas.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class VentaRequest {

    @NotNull(message = "Debe seleccionar un cliente")
    private Long clienteId;

    @NotEmpty(message = "Debe agregar al menos un producto a la venta")
    @Valid
    private List<ItemVentaDTO> items;
}