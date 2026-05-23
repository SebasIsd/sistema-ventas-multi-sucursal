package com.empresa.sistema_ventas.repository;

import com.empresa.sistema_ventas.entity.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    @Query("""
            SELECT m FROM MovimientoInventario m
            JOIN FETCH m.producto
            WHERE m.sucursal.id = :sucursalId
            ORDER BY m.fechaMovimiento DESC
            """)
    List<MovimientoInventario> findBySucursalIdOrderByFechaMovimientoDesc(@Param("sucursalId") Long sucursalId);
}