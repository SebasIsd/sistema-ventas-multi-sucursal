package com.empresa.sistema_ventas.repository;

import com.empresa.sistema_ventas.entity.Factura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FacturaRepository extends JpaRepository<Factura, Long> {
	Optional<Factura> findByVenta_Id(Long ventaId);
}