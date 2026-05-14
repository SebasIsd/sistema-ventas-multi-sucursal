package com.empresa.sistema_ventas.config;

import com.empresa.sistema_ventas.entity.*;
import com.empresa.sistema_ventas.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private final RolRepository rolRepo;
    private final SucursalRepository sucRepo;
    private final UsuarioRepository userRepo;
    private final PasswordEncoder encoder;

    public DataInitializer(RolRepository rolRepo, SucursalRepository sucRepo, UsuarioRepository userRepo, PasswordEncoder encoder) {
        this.rolRepo = rolRepo;
        this.sucRepo = sucRepo;
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        Rol admin = rolRepo.findByNombre("ADMIN").orElseGet(() -> rolRepo.save(new Rol("ADMIN")));
        Rol cajero = rolRepo.findByNombre("CAJERO").orElseGet(() -> rolRepo.save(new Rol("CAJERO")));
        Rol bodega = rolRepo.findByNombre("BODEGA").orElseGet(() -> rolRepo.save(new Rol("BODEGA")));

        Sucursal quito = sucRepo.findByCodigo("QUI").orElseGet(() -> {
            Sucursal s = new Sucursal();
            s.setCodigo("QUI");
            s.setCiudad("Quito");
            s.setDireccion("Av. Principal 123");
            s.setTelefono("02-1234567");
            return sucRepo.save(s);
        });

        if (!userRepo.existsByUsername("admin")) {
            Usuario u = new Usuario();
            u.setUsername("admin");
            u.setPassword(encoder.encode("admin123"));
            u.setNombreCompleto("Administrador General");
            u.setEmail("admin@sistema.com");
            u.setRol(admin);
            u.setSucursal(quito);
            u.setActivo(true);
            userRepo.save(u);
        }
    }
}