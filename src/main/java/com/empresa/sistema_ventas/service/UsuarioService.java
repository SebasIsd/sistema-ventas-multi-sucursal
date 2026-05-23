package com.empresa.sistema_ventas.service;

import com.empresa.sistema_ventas.entity.Rol;
import com.empresa.sistema_ventas.entity.Sucursal;
import com.empresa.sistema_ventas.entity.Usuario;
import com.empresa.sistema_ventas.repository.RolRepository;
import com.empresa.sistema_ventas.repository.SucursalRepository;
import com.empresa.sistema_ventas.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final SucursalRepository sucursalRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          RolRepository rolRepository,
                          SucursalRepository sucursalRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.sucursalRepository = sucursalRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> obtenerPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    @Transactional
    public Usuario guardarUsuario(Usuario usuario, String passwordPlano, Long rolId, Long sucursalId) {
        System.out.println(">>> Service: Guardando usuario ID=" + usuario.getId());

        // Si es nuevo (ID null), validar que el username no exista
        if (usuario.getId() == null) {
            if (usuarioRepository.existsByUsername(usuario.getUsername())) {
                throw new RuntimeException("El nombre de usuario '" + usuario.getUsername() + "' ya existe");
            }

            // Password obligatorio para nuevos usuarios
            if (passwordPlano == null || passwordPlano.isEmpty()) {
                throw new RuntimeException("La contraseña es obligatoria para nuevos usuarios");
            }
        } else {
            // Si es edición, buscar el usuario existente
            Usuario existente = usuarioRepository.findById(usuario.getId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuario.getId()));

            // Mantener el username original (no se puede cambiar)
            usuario.setUsername(existente.getUsername());

            // Si no hay password nuevo, mantener el anterior
            if (passwordPlano == null || passwordPlano.isEmpty()) {
                usuario.setPassword(existente.getPassword());
            } else {
                // Si hay password nuevo, encriptarlo
                usuario.setPassword(passwordEncoder.encode(passwordPlano));
            }
        }

        // Asignar Rol (obligatorio)
        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + rolId));
        usuario.setRol(rol);

        // Asignar Sucursal (opcional)
        if (sucursalId != null) {
            Sucursal sucursal = sucursalRepository.findById(sucursalId)
                    .orElseThrow(() -> new RuntimeException("Sucursal no encontrada con ID: " + sucursalId));
            usuario.setSucursal(sucursal);
        } else {
            usuario.setSucursal(null);
        }

        System.out.println(">>> Service: Guardando en BD...");
        Usuario guardado = usuarioRepository.save(usuario);
        System.out.println(">>> Service: Usuario guardado con ID=" + guardado.getId());

        return guardado;
    }

    @Transactional
    public void eliminarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado con ID: " + id);
        }

        // No permitir eliminar el usuario admin principal (id = 1)
        if (id == 1L) {
            throw new RuntimeException("No se puede eliminar el usuario administrador principal");
        }

        try {
            usuarioRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar usuario. Verifique que no tenga ventas asociadas.");
        }
    }

    public List<Rol> listarRoles() {
        return rolRepository.findAll();
    }

    public List<Sucursal> listarSucursales() {
        return sucursalRepository.findAll();
    }
}