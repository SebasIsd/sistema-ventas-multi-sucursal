package com.empresa.sistema_ventas.repository;

import com.empresa.sistema_ventas.entity.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {
    List<Inventario> findByStockActualLessThanEqual(int stockMinimo);
    List<Inventario> findBySucursalId(Long sucursalId);
}