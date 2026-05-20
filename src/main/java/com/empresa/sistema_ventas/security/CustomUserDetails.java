package com.empresa.sistema_ventas.security;

import com.empresa.sistema_ventas.entity.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails, Serializable {

    private static final long serialVersionUID = 1L;

    private final String username;
    private final String password;
    private final String nombreCompleto;
    private final String sucursalNombre;
    private final String rolNombre;
    private final boolean activo;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(Usuario usuario) {
        this.username = usuario.getUsername();
        this.password = usuario.getPassword();
        this.nombreCompleto = usuario.getNombreCompleto();
        this.activo = usuario.isActivo();
        this.rolNombre = usuario.getRol().getNombre();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + this.rolNombre));
        
        // Evitar LazyInitializationException manejando el null o cargando el dato aquí mismo
        if (usuario.getSucursal() != null) {
            this.sucursalNombre = usuario.getSucursal().getCiudad();
        } else {
            this.sucursalNombre = "Sin Sucursal";
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getSucursalNombre() {
        return sucursalNombre;
    }

    public String getRolNombre() {
        return rolNombre;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }
}