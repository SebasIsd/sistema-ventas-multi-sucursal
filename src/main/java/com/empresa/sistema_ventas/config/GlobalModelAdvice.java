package com.empresa.sistema_ventas.config;

import com.empresa.sistema_ventas.entity.Usuario;
import com.empresa.sistema_ventas.repository.SolicitudTransferenciaRepository;
import com.empresa.sistema_ventas.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    private final SolicitudTransferenciaRepository solicitudRepo;
    private final UsuarioRepository usuarioRepo;

    public GlobalModelAdvice(SolicitudTransferenciaRepository solicitudRepo,
                             UsuarioRepository usuarioRepo) {
        this.solicitudRepo = solicitudRepo;
        this.usuarioRepo = usuarioRepo;
    }

    @ModelAttribute("username")
    public String username(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getName();
    }

    // Badge de solicitudes pendientes para el sidebar de bodega
    @ModelAttribute("solicitudesPendientes")
    public long solicitudesPendientes(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return 0L;
        }
        try {
            boolean esBodega = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_BODEGA") || a.getAuthority().equals("ROLE_ADMIN"));
            if (!esBodega) return 0L;

            Usuario usuario = usuarioRepo.findByUsername(authentication.getName()).orElse(null);
            if (usuario == null || usuario.getSucursal() == null) return 0L;

            return solicitudRepo.countBySucursalOrigen_IdAndEstado(
                    usuario.getSucursal().getId(), "PENDIENTE");
        } catch (Exception e) {
            return 0L;
        }
    }
}