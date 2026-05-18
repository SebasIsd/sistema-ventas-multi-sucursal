package com.empresa.sistema_ventas.service;

import com.empresa.sistema_ventas.dto.ClienteDTO;
import com.empresa.sistema_ventas.entity.Cliente;
import com.empresa.sistema_ventas.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
    }

    public Cliente buscarPorCedula(String cedula) {
        return clienteRepository.findByCedulaRuc(cedula)
                .orElse(null);
    }

    public Cliente crearCliente(ClienteDTO dto) {

        Cliente cliente = new Cliente();

        cliente.setCedulaRuc(dto.getCedulaRuc());
        cliente.setNombres(dto.getNombres());
        cliente.setApellidos(dto.getApellidos());
        cliente.setDireccion(dto.getDireccion());
        cliente.setTelefono(dto.getTelefono());
        cliente.setEmail(dto.getEmail());
        cliente.setTipoCliente(dto.getTipoCliente());
        cliente.setActivo(true);

        return clienteRepository.save(cliente);
    }
}