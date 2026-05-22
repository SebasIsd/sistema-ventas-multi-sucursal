package com.empresa.sistema_ventas.controller;

import com.empresa.sistema_ventas.entity.Usuario;
import com.empresa.sistema_ventas.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // ✅ LISTAR USUARIOS
    @GetMapping
    public String listarUsuarios(Model model) {
        try {
            model.addAttribute("usuarios", usuarioService.listarUsuarios());
        } catch (Exception e) {
            model.addAttribute("mensaje", "Error al cargar usuarios: " + e.getMessage());
            model.addAttribute("tipoMensaje", "danger");
        }
        return "admin/usuarios/list";
    }

    // ✅ FORMULARIO NUEVO USUARIO
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        try {
            model.addAttribute("usuario", new Usuario());
            model.addAttribute("roles", usuarioService.listarRoles());
            model.addAttribute("sucursales", usuarioService.listarSucursales());
            model.addAttribute("modo", "crear");
        } catch (Exception e) {
            model.addAttribute("mensaje", "Error: " + e.getMessage());
            model.addAttribute("tipoMensaje", "danger");
        }
        return "admin/usuarios/form";
    }

    // ✅ FORMULARIO EDITAR USUARIO
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        try {
            Usuario usuario = usuarioService.obtenerPorId(id)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

            model.addAttribute("usuario", usuario);
            model.addAttribute("roles", usuarioService.listarRoles());
            model.addAttribute("sucursales", usuarioService.listarSucursales());
            model.addAttribute("modo", "editar");
        } catch (Exception e) {
            model.addAttribute("mensaje", e.getMessage());
            model.addAttribute("tipoMensaje", "danger");
            return "redirect:/admin/usuarios";
        }
        return "admin/usuarios/form";
    }

    // ✅ GUARDAR USUARIO (POST)
    @PostMapping("/guardar")
    public String guardarUsuario(@ModelAttribute Usuario usuario,
                                 @RequestParam(required = false) String password,
                                 @RequestParam Long rolId,
                                 @RequestParam(required = false) Long sucursalId,
                                 RedirectAttributes redirectAttributes) {

        System.out.println("=== DEBUG GUARDAR USUARIO ===");
        System.out.println("ID: " + usuario.getId());
        System.out.println("Username: " + usuario.getUsername());
        System.out.println("Nombre: " + usuario.getNombreCompleto());
        System.out.println("Email: " + usuario.getEmail());
        System.out.println("Password: " + (password != null ? "[PROVIDED]" : "[NULL]"));
        System.out.println("Rol ID: " + rolId);
        System.out.println("Sucursal ID: " + sucursalId);
        System.out.println("Activo: " + usuario.isActivo());
        System.out.println("=============================");

        try {
            // Si es edición y no hay password, usar null para mantener el actual
            String passwordAUsar = (password != null && !password.isEmpty()) ? password : null;

            usuarioService.guardarUsuario(usuario, passwordAUsar, rolId, sucursalId);

            redirectAttributes.addFlashAttribute("mensaje", "Usuario guardado exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");

        } catch (RuntimeException e) {
            System.err.println("ERROR al guardar: " + e.getMessage());
            e.printStackTrace();

            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");

            // Si es edición, volver al mismo formulario de edición
            if (usuario.getId() != null) {
                redirectAttributes.addFlashAttribute("usuario", usuario);
                redirectAttributes.addFlashAttribute("modo", "editar");
                return "redirect:/admin/usuarios/editar/" + usuario.getId();
            } else {
                return "redirect:/admin/usuarios/nuevo";
            }
        }

        return "redirect:/admin/usuarios";
    }

    // ✅ ELIMINAR USUARIO (CAMBIADO A POST)
    @PostMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.eliminarUsuario(id);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario eliminado correctamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al eliminar: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error inesperado: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        }
        return "redirect:/admin/usuarios";
    }
}