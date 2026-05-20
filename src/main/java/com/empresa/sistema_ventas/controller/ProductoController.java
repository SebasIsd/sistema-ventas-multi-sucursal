package com.empresa.sistema_ventas.controller;

import com.empresa.sistema_ventas.dto.ProductoDTO;
import com.empresa.sistema_ventas.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// ✅ Cambiado a @RestController y mapeo correcto a /api/productos
// ✅ Eliminado el viejo @Controller con vistas Thymeleaf (eso va en ProductoViewController)
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public List<ProductoDTO> listar() {
        return productoService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO> buscarPorId(@PathVariable Integer id) {
        ProductoDTO dto = productoService.buscarPorId(id);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<ProductoDTO> guardar(@RequestBody ProductoDTO dto) {
        ProductoDTO nuevo = productoService.guardar(dto);
        return ResponseEntity.ok(nuevo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoDTO> actualizar(@PathVariable Integer id,
                                                  @RequestBody ProductoDTO dto) {
        ProductoDTO actualizado = productoService.actualizar(id, dto);
        if (actualizado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}