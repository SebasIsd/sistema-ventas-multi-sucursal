package com.empresa.sistema_ventas.controller;

import com.empresa.sistema_ventas.dto.ClienteDTO;
import com.empresa.sistema_ventas.service.ClienteService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.empresa.sistema_ventas.entity.Cliente;

@Controller
@RequestMapping("/clientes")
@PreAuthorize("hasAnyRole('CAJERO', 'ADMIN')")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping("/listar")
    public String listarClientes(Model model) {

        model.addAttribute("clientes", clienteService.listarClientes());

        return "clientes/listar";
    }

    @PostMapping("/guardar")
    public String guardarCliente(@ModelAttribute ClienteDTO clienteDTO) {

        clienteService.crearCliente(clienteDTO);

        return "redirect:/clientes/listar";
    }
    @GetMapping("/buscar")
    public String buscarCliente(
            @RequestParam("cedula") String cedula,
            Model model
    ) {

        Cliente cliente = clienteService.buscarPorCedula(cedula);

        model.addAttribute("clientes", clienteService.listarClientes());

        if (cliente != null) {
            model.addAttribute("clienteEncontrado", cliente);
        }

        return "clientes/listar";
    }
    @PostMapping("/editar")
    public String editarCliente(@ModelAttribute ClienteDTO clienteDTO) {
        clienteService.actualizarCliente(clienteDTO);
        return "redirect:/clientes/listar";
    }

    // NUEVO: Método para eliminar
    @GetMapping("/eliminar/{id}")
    public String eliminarCliente(@PathVariable Long id) {
        clienteService.eliminarCliente(id);
        return "redirect:/clientes/listar";
    }
}