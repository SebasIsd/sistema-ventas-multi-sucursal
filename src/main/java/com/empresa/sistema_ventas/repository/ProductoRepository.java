package com.empresa.sistema_ventas.repository;

import com.empresa.sistema_ventas.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    List<Producto> findByActivoTrue();

    List<Producto> findByCategoriaIdCategoria(Integer idCategoria);

    Producto findByCodigoBarras(String codigoBarras);
}