package com.empresa.sistema_ventas.service;

import com.empresa.sistema_ventas.dto.ClienteDTO;
import com.empresa.sistema_ventas.entity.Cliente;
import com.empresa.sistema_ventas.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    // NUEVO: Método para actualizar
    @Transactional
    public Cliente actualizarCliente(ClienteDTO clienteDTO) {
        Cliente clienteExistente = clienteRepository.findByCedulaRuc(clienteDTO.getCedulaRuc())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con cédula: " + clienteDTO.getCedulaRuc()));

        clienteExistente.setNombres(clienteDTO.getNombres());
        clienteExistente.setApellidos(clienteDTO.getApellidos());
        clienteExistente.setDireccion(clienteDTO.getDireccion());
        clienteExistente.setTelefono(clienteDTO.getTelefono());
        clienteExistente.setEmail(clienteDTO.getEmail());
        clienteExistente.setTipoCliente(clienteDTO.getTipoCliente());

        return clienteRepository.save(clienteExistente);
    }

    // NUEVO: Método para eliminar
    @Transactional
    public void eliminarCliente(Long id) {
        clienteRepository.deleteById(id);
    }
}