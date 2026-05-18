package com.empresa.sistema_ventas.controller;

import com.empresa.sistema_ventas.dto.ClienteDTO;
import com.empresa.sistema_ventas.service.ClienteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/clientes")
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
}