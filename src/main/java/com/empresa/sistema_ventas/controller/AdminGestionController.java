package com.empresa.sistema_ventas.controller;

import com.empresa.sistema_ventas.repository.RolRepository;
import com.empresa.sistema_ventas.repository.SucursalRepository;
import com.empresa.sistema_ventas.repository.UsuarioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminGestionController {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final SucursalRepository sucursalRepository;

    public AdminGestionController(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            SucursalRepository sucursalRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.sucursalRepository = sucursalRepository;
    }

    @GetMapping("/usuarios")
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioRepository.findAll());
        return "admin/usuarios/lista";
    }

    @GetMapping("/usuarios/nuevo")
    public String nuevoUsuario(Model model) {
        model.addAttribute("roles", rolRepository.findAll());
        model.addAttribute("sucursales", sucursalRepository.findAll());
        return "admin/usuarios/nuevo";
    }

    @GetMapping("/sucursales")
    public String listarSucursales(Model model) {
        model.addAttribute("sucursales", sucursalRepository.findAll());
        return "admin/sucursales/lista";
    }

    @GetMapping("/sucursales/nueva")
    public String nuevaSucursal() {
        return "admin/sucursales/nueva";
    }
}
