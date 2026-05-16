package com.empresa.sistema_ventas.security;

import com.empresa.sistema_ventas.entity.Usuario;
import com.empresa.sistema_ventas.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImp implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UserDetailsServiceImp(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        if (!usuario.isActivo()) {
            throw new UsernameNotFoundException("Usuario inactivo: " + username);
        }
        
        var authority = new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getNombre());

        return new User(
                usuario.getUsername(),
                usuario.getPassword(),
                List.of(authority)
        );
    }
}