package com.empresa.sistema_ventas.repository;

import com.empresa.sistema_ventas.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Optional<Producto> findByCodigoBarras(String codigoBarras);
}