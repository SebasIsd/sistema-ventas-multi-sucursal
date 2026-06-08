package com.empresa.sistema_ventas.repository;

import com.empresa.sistema_ventas.entity.Inventario;
import com.empresa.sistema_ventas.entity.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    // CORREGIDO: Se cambió findByProducto_Id por findByProducto_IdProducto
    Optional<Inventario> findByProducto_IdProductoAndSucursal_Id(Integer productoId, Long sucursalId);

    @Query("SELECT i FROM Inventario i WHERE i.stockActual <= i.stockMinimo")
    List<Inventario> findStockBajo();
    List<Inventario> findBySucursal(Sucursal sucursal);

    List<Inventario> findBySucursal_Id(Long sucursalId);
}