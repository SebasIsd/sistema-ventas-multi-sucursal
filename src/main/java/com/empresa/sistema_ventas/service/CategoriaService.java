package com.empresa.sistema_ventas.service;

import com.empresa.sistema_ventas.dto.CategoriaDTO;
import com.empresa.sistema_ventas.entity.Categoria;
import com.empresa.sistema_ventas.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    public List<Categoria> listar() {
        return categoriaRepository.findAll();
    }

    public Categoria buscarPorId(Integer id) {
        return categoriaRepository.findById(id).orElse(null);
    }

    public Categoria guardar(CategoriaDTO dto) {

        Categoria categoria = new Categoria();

        categoria.setNombre(dto.getNombre());
        categoria.setDescripcion(dto.getDescripcion());

        // ✅ Respetar el valor enviado, o true por defecto si viene null
        categoria.setActivo(dto.getActivo() != null ? dto.getActivo() : true);

        return categoriaRepository.save(categoria);
    }

    public Categoria actualizar(Integer id, CategoriaDTO dto) {

        Categoria categoria = buscarPorId(id);

        if (categoria == null) {
            return null;
        }

        categoria.setNombre(dto.getNombre());
        categoria.setDescripcion(dto.getDescripcion());

        // ✅ Corrección: también se actualiza el campo activo
        if (dto.getActivo() != null) {
            categoria.setActivo(dto.getActivo());
        }

        return categoriaRepository.save(categoria);
    }

    public void eliminar(Integer id) {

        Categoria categoria = buscarPorId(id);

        if (categoria != null) {
            // ✅ Soft delete: se marca como inactivo en lugar de borrar
            categoria.setActivo(false);
            categoriaRepository.save(categoria);
        }
    }
}