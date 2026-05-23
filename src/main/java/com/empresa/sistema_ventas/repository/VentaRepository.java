package com.empresa.sistema_ventas.repository;

import com.empresa.sistema_ventas.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Long> {
    List<Venta> findTop10ByOrderByFechaDesc();

    List<Venta> findBySucursalId(Long sucursalId);

    @Query("SELECT SUM(v.total) FROM Venta v WHERE v.sucursal.id = ?1")
    Double getTotalVentasBySucursal(Long sucursalId);

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.estado = 'PAGADO'")
    Double getTotalIngresos();
}