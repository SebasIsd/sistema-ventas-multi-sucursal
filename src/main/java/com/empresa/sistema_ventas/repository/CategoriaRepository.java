package com.empresa.sistema_ventas.repository;

import com.empresa.sistema_ventas.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
      Categoria findByNombre(String nombre);
}