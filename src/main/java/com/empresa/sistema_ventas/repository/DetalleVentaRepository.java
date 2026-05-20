package com.empresa.sistema_ventas.repository;

import com.empresa.sistema_ventas.entity.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {
}