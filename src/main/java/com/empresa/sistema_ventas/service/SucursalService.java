package com.empresa.sistema_ventas.service;

import com.empresa.sistema_ventas.dto.SucursalDTO;
import com.empresa.sistema_ventas.entity.Sucursal;
import com.empresa.sistema_ventas.repository.SucursalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SucursalService {

    @Autowired
    private SucursalRepository sucursalRepository;

    public List<Sucursal> listar() {
        return sucursalRepository.findAll();
    }

    public Sucursal buscarPorId(Long id) {
        return sucursalRepository.findById(id).orElse(null);
    }

    public Sucursal guardar(SucursalDTO dto) {

        Sucursal s = new Sucursal();

        s.setCodigo(dto.getCodigo());
        s.setCiudad(dto.getCiudad());
        s.setDireccion(dto.getDireccion());
        s.setTelefono(dto.getTelefono());
        s.setActivo(true);

        return sucursalRepository.save(s);
    }

    public Sucursal actualizar(Long id, SucursalDTO dto) {

        Sucursal s = buscarPorId(id);

        if (s == null) {
            return null;
        }

        s.setCodigo(dto.getCodigo());
        s.setCiudad(dto.getCiudad());
        s.setDireccion(dto.getDireccion());
        s.setTelefono(dto.getTelefono());

        return sucursalRepository.save(s);
    }

    public void eliminar(Long id) {
        sucursalRepository.deleteById(id);
    }
}
