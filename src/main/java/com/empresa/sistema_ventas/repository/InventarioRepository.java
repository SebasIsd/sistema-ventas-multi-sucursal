package com.empresa.sistema_ventas.repository;
import java.util.Optional;
import com.empresa.sistema_ventas.entity.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {
    List<Inventario> findByStockActualLessThanEqual(int stockMinimo);
    List<Inventario> findBySucursalId(Long sucursalId);
    Optional<Inventario> findByProductoIdAndSucursalId(Long productoId, Long sucursalId);
}