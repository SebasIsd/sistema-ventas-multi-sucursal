package com.empresa.sistema_ventas.controller;

import com.empresa.sistema_ventas.dto.ClienteDTO;
import com.empresa.sistema_ventas.repository.ClienteRepository;
import com.empresa.sistema_ventas.service.ClienteService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.empresa.sistema_ventas.entity.Cliente;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/clientes")
@PreAuthorize("hasAnyRole('CAJERO', 'ADMIN')")
public class ClienteController {

    private final ClienteService clienteService;
    private final ClienteRepository clienteRepository;
    
    public ClienteController(ClienteService clienteService, ClienteRepository clienteRepository) {
        this.clienteService = clienteService;
        this.clienteRepository = clienteRepository;
    }

    @GetMapping("/listar")
    public String listarClientes(Model model) {
        model.addAttribute("clientes", clienteService.listarClientes());
        return "clientes/listar";
    }

    @PostMapping("/guardar")
    public String guardarCliente(
            @Valid @ModelAttribute ClienteDTO clienteDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .findFirst()
                    .orElse("Datos inválidos");
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            return "redirect:/clientes/listar";
        }

        try {
            // Trim inputs
            clienteDTO.setCedulaRuc(clienteDTO.getCedulaRuc().trim());
            clienteDTO.setNombres(clienteDTO.getNombres().trim());
            clienteDTO.setApellidos(clienteDTO.getApellidos().trim());
            if (clienteDTO.getTelefono() != null) clienteDTO.setTelefono(clienteDTO.getTelefono().trim());
            if (clienteDTO.getEmail() != null) clienteDTO.setEmail(clienteDTO.getEmail().trim());
            if (clienteDTO.getDireccion() != null) clienteDTO.setDireccion(clienteDTO.getDireccion().trim());

            if (clienteService.buscarPorCedula(clienteDTO.getCedulaRuc()) != null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Ya existe un cliente registrado con la cédula/RUC: " + clienteDTO.getCedulaRuc());
                return "redirect:/clientes/listar";
            }

            clienteService.crearCliente(clienteDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Cliente registrado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al registrar cliente: " + e.getMessage());
        }

        return "redirect:/clientes/listar";
    }

    @GetMapping("/buscar")
    public String buscarCliente(
            @RequestParam("cedula") String cedula,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (cedula == null || cedula.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Debe ingresar una cédula o RUC para buscar.");
            return "redirect:/clientes/listar";
        }

        String searchCedula = cedula.trim();
        if (!searchCedula.matches("^\\d+$")) {
            redirectAttributes.addFlashAttribute("errorMessage", "La cédula o RUC para buscar debe contener únicamente números.");
            return "redirect:/clientes/listar";
        }

        Cliente cliente = clienteService.buscarPorCedula(searchCedula);
        model.addAttribute("clientes", clienteService.listarClientes());

        if (cliente != null) {
            model.addAttribute("clienteEncontrado", cliente);
        } else {
            model.addAttribute("errorMessage", "No se encontró ningún cliente con la cédula/RUC: " + searchCedula);
        }

        return "clientes/listar";
    }

    @PostMapping("/editar")
    public String editarCliente(
            @Valid @ModelAttribute ClienteDTO clienteDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .findFirst()
                    .orElse("Datos inválidos");
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            return "redirect:/clientes/listar";
        }

        try {
            // Trim inputs
            clienteDTO.setCedulaRuc(clienteDTO.getCedulaRuc().trim());
            clienteDTO.setNombres(clienteDTO.getNombres().trim());
            clienteDTO.setApellidos(clienteDTO.getApellidos().trim());
            if (clienteDTO.getTelefono() != null) clienteDTO.setTelefono(clienteDTO.getTelefono().trim());
            if (clienteDTO.getEmail() != null) clienteDTO.setEmail(clienteDTO.getEmail().trim());
            if (clienteDTO.getDireccion() != null) clienteDTO.setDireccion(clienteDTO.getDireccion().trim());

            clienteService.actualizarCliente(clienteDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Cliente actualizado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar cliente: " + e.getMessage());
        }
        return "redirect:/clientes/listar";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarCliente(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            clienteService.eliminarCliente(id);
            redirectAttributes.addFlashAttribute("successMessage", "Cliente eliminado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar cliente: " + e.getMessage());
        }
        return "redirect:/clientes/listar";
    }

    @PostMapping("/guardar-rapido")
    public String guardarRapido(
            @Valid @ModelAttribute Cliente cliente,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .findFirst()
                    .orElse("Datos de cliente inválidos");
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            return "redirect:/ventas/nueva";
        }

        try {
            // Trim inputs
            cliente.setCedulaRuc(cliente.getCedulaRuc().trim());
            cliente.setNombres(cliente.getNombres().trim());
            cliente.setApellidos(cliente.getApellidos().trim());
            if (cliente.getTelefono() != null) cliente.setTelefono(cliente.getTelefono().trim());
            if (cliente.getEmail() != null) cliente.setEmail(cliente.getEmail().trim());
            if (cliente.getDireccion() != null) cliente.setDireccion(cliente.getDireccion().trim());

            if (clienteRepository.findByCedulaRuc(cliente.getCedulaRuc()).isPresent()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Ya existe un cliente con la cédula/RUC: " + cliente.getCedulaRuc());
                return "redirect:/ventas/nueva";
            }

            cliente.setActivo(true);
            clienteRepository.save(cliente);
            redirectAttributes.addFlashAttribute("successMessage", "Cliente registrado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al registrar cliente de forma rápida: " + e.getMessage());
        }

        return "redirect:/ventas/nueva";
    }
}