package com.empresa.sistema_ventas.controller;

import com.empresa.sistema_ventas.dto.SucursalDTO;
import com.empresa.sistema_ventas.entity.Sucursal;
import com.empresa.sistema_ventas.service.SucursalService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sucursales")
@CrossOrigin("*")
public class SucursalController {

    private final SucursalService sucursalService;

    public SucursalController(SucursalService sucursalService) {
        this.sucursalService = sucursalService;
    }

    @GetMapping
    public List<Sucursal> listar() {
        return sucursalService.listar();
    }

    @GetMapping("/{id}")
    public Sucursal buscar(@PathVariable Long id) {
        return sucursalService.buscarPorId(id);
    }

    @PostMapping
    public Sucursal guardar(@RequestBody SucursalDTO dto) {
        return sucursalService.guardar(dto);
    }

    @PutMapping("/{id}")
    public Sucursal actualizar(@PathVariable Long id,
                               @RequestBody SucursalDTO dto) {

        return sucursalService.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        sucursalService.eliminar(id);
    }
}
