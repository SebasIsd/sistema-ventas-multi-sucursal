package com.empresa.sistema_ventas.repository;

import com.empresa.sistema_ventas.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    List<Venta> findTop10ByOrderByFechaDesc();

    List<Venta> findBySucursalId(Long sucursalId);

    @Query("SELECT SUM(v.total) FROM Venta v WHERE v.sucursal.id = ?1")
    Double getTotalVentasBySucursal(Long sucursalId);

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.estado = 'PAGADO'")
    Double getTotalIngresos();

    Page<Venta> findAll(Pageable pageable);

    @Query("""
SELECT v
FROM Venta v
WHERE
(:cedula IS NULL
 OR :cedula = ''
 OR v.cliente.cedulaRuc LIKE %:cedula%)
AND
(
(:fechaInicio IS NULL OR :fechaInicio = '')
OR
FUNCTION('DATE', v.fecha) >= CAST(:fechaInicio AS date)
)
AND
(
(:fechaFin IS NULL OR :fechaFin = '')
OR
FUNCTION('DATE', v.fecha) <= CAST(:fechaFin AS date)
)
ORDER BY v.fecha DESC
""")
    Page<Venta> buscarVentas(
            String cedula,
            String fechaInicio,
            String fechaFin,
            Pageable pageable
    );
}