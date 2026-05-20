package com.empresa.sistema_ventas.controller;

import com.empresa.sistema_ventas.repository.RolRepository;
import com.empresa.sistema_ventas.repository.SucursalRepository;
import com.empresa.sistema_ventas.repository.UsuarioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.empresa.sistema_ventas.entity.Usuario;
import com.empresa.sistema_ventas.entity.Sucursal;

@Controller
@RequestMapping("/admin")
public class AdminGestionController {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final SucursalRepository sucursalRepository;

    private final PasswordEncoder passwordEncoder;

    public AdminGestionController(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            SucursalRepository sucursalRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.sucursalRepository = sucursalRepository;
        this.passwordEncoder = passwordEncoder;
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

    @PostMapping("/usuarios/guardar")
    public String guardarUsuario(
            @RequestParam String username,
            @RequestParam String nombreCompleto,
            @RequestParam String email,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) Long rolId,
            @RequestParam(required = false) Long sucursalId
    ) {
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setNombreCompleto(nombreCompleto);
        u.setEmail(email);
        u.setTelefono(telefono);
        String pwd = (password == null || password.isBlank()) ? "changeme" : password;
        u.setPassword(passwordEncoder.encode(pwd));
        u.setActivo(true);

        if (rolId != null) {
            rolRepository.findById(rolId).ifPresent(u::setRol);
        }

        if (sucursalId != null) {
            sucursalRepository.findById(sucursalId).ifPresent(u::setSucursal);
        }

        usuarioRepository.save(u);

        return "redirect:/admin/usuarios";
    }

    @PostMapping("/sucursales/guardar")
    public String guardarSucursal(
            @RequestParam String codigo,
            @RequestParam String ciudad,
            @RequestParam(required = false) String direccion,
            @RequestParam(required = false) String telefono
    ) {
        Sucursal s = new Sucursal();
        s.setCodigo(codigo);
        s.setCiudad(ciudad);
        s.setDireccion(direccion);
        s.setTelefono(telefono);
        s.setActivo(true);

        sucursalRepository.save(s);

        return "redirect:/admin/sucursales";
    }
}
