package com.empresa.sistema_ventas.service;

import com.empresa.sistema_ventas.dto.ProductoDTO;
import com.empresa.sistema_ventas.entity.Categoria;
import com.empresa.sistema_ventas.entity.Producto;
import com.empresa.sistema_ventas.repository.CategoriaRepository;
import com.empresa.sistema_ventas.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    // ✅ Retorna List<ProductoDTO> en lugar de List<Producto>
    // Así evitamos el bucle infinito Producto -> Categoria -> Productos -> ...
    // y además incluimos nombreCategoria que necesita el frontend
    public List<ProductoDTO> listar() {
        return productoRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ✅ Retorna ProductoDTO en lugar de Producto
    public ProductoDTO buscarPorId(Integer id) {
        Producto producto = productoRepository.findById(id).orElse(null);
        if (producto == null) return null;
        return toDTO(producto);
    }

    // ✅ Retorna ProductoDTO en lugar de Producto
    public ProductoDTO guardar(ProductoDTO dto) {

        Categoria categoria = categoriaRepository
                .findById(dto.getIdCategoria())
                .orElse(null);

        Producto producto = new Producto();

        producto.setCodigoBarras(dto.getCodigoBarras());
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecioUnitario(dto.getPrecioUnitario());
        producto.setPorcentajeIva(dto.getPorcentajeIva());
        producto.setCategoria(categoria);

        // ✅ Respetar el valor de activo del DTO, o true por defecto
        producto.setActivo(dto.getActivo() != null ? dto.getActivo() : true);

        return toDTO(productoRepository.save(producto));
    }

    // ✅ Retorna ProductoDTO en lugar de Producto
    public ProductoDTO actualizar(Integer id, ProductoDTO dto) {

        Producto producto = productoRepository.findById(id).orElse(null);

        if (producto == null) return null;

        Categoria categoria = categoriaRepository
                .findById(dto.getIdCategoria())
                .orElse(null);

        producto.setCodigoBarras(dto.getCodigoBarras());
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecioUnitario(dto.getPrecioUnitario());
        producto.setPorcentajeIva(dto.getPorcentajeIva());
        producto.setCategoria(categoria);

        // ✅ También se actualiza el campo activo
        if (dto.getActivo() != null) {
            producto.setActivo(dto.getActivo());
        }

        return toDTO(productoRepository.save(producto));
    }

    public void eliminar(Integer id) {

        Producto producto = productoRepository.findById(id).orElse(null);

        if (producto != null) {
            // ✅ Soft delete
            producto.setActivo(false);
            productoRepository.save(producto);
        }
    }

    // ✅ Método auxiliar: convierte Producto Entity -> ProductoDTO
    // Aquí se mapea nombreCategoria para que el frontend pueda mostrarlo
    private ProductoDTO toDTO(Producto p) {

        ProductoDTO dto = new ProductoDTO();

        dto.setIdProducto(p.getIdProducto());
        dto.setCodigoBarras(p.getCodigoBarras());
        dto.setNombre(p.getNombre());
        dto.setDescripcion(p.getDescripcion());
        dto.setPrecioUnitario(p.getPrecioUnitario());
        dto.setPorcentajeIva(p.getPorcentajeIva());
        dto.setActivo(p.getActivo());

        if (p.getCategoria() != null) {
            dto.setIdCategoria(p.getCategoria().getIdCategoria());
            dto.setNombreCategoria(p.getCategoria().getNombre());
        }

        return dto;
    }
}