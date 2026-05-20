package com.empresa.sistema_ventas.repository;

import com.empresa.sistema_ventas.entity.Factura;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacturaRepository extends JpaRepository<Factura, Long> {
}