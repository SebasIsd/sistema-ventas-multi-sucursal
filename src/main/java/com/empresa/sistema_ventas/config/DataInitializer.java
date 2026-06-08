package com.empresa.sistema_ventas.config;

import com.empresa.sistema_ventas.entity.*;
import com.empresa.sistema_ventas.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepo;
    private final SucursalRepository sucRepo;
    private final UsuarioRepository userRepo;
    private final CategoriaRepository categoriaRepo;
    private final ProductoRepository productoRepo;
    private final ClienteRepository clienteRepo;
    private final InventarioRepository inventarioRepo;
    private final VentaRepository ventaRepo;
    private final PasswordEncoder encoder;

    public DataInitializer(RolRepository rolRepo,
                           SucursalRepository sucRepo,
                           UsuarioRepository userRepo,
                           CategoriaRepository categoriaRepo,
                           ProductoRepository productoRepo,
                           ClienteRepository clienteRepo,
                           InventarioRepository inventarioRepo,
                           VentaRepository ventaRepo,
                           PasswordEncoder encoder) {
        this.rolRepo = rolRepo;
        this.sucRepo = sucRepo;
        this.userRepo = userRepo;
        this.categoriaRepo = categoriaRepo;
        this.productoRepo = productoRepo;
        this.clienteRepo = clienteRepo;
        this.inventarioRepo = inventarioRepo;
        this.ventaRepo = ventaRepo;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // ✅ Solo ejecutar si la BD está vacía (opcional)
        if (rolRepo.count() > 0 && userRepo.count() > 5) {
            System.out.println("⚠️ La base de datos ya tiene datos - Skipping DataInitializer");
            return;
        }

        System.out.println("🚀 Iniciando carga de datos de prueba...");

        // ===========================
        // 1. ROLES
        // ===========================
        Rol admin = getOrCreateRol("ADMIN");
        Rol cajero = getOrCreateRol("CAJERO");
        Rol bodega = getOrCreateRol("BODEGA");

        // ===========================
        // 2. SUCURSALES
        // ===========================
        Sucursal quito = getOrCreateSucursal("QUI", "Quito", "Av. Amazonas N24-345", "02-2234-567");
        Sucursal ambato = getOrCreateSucursal("AMB", "Ambato", "Av. de los Andes", "03-2456-789");
        Sucursal cuenca = getOrCreateSucursal("CUE", "Cuenca", "Av. Ordóñez Lasso", "07-2890-123");

        // ===========================
        // 3. USUARIOS
        // ===========================
        createUsuario("admin", "admin123", "Administrador General", "admin@sistema.com", "0999999999", admin, quito);
        createUsuario("cajero1", "cajero123", "Juan Pérez", "cajero1@sistema.com", "0988888888", cajero, quito);
        createUsuario("cajero2", "cajero123", "Ana Gómez", "cajero2@sistema.com", "0977777777", cajero, ambato);
        createUsuario("bodega1", "bodega123", "Carlos López", "bodega1@sistema.com", "0966666666", bodega, ambato);
        createUsuario("bodega2", "bodega123", "María Sánchez", "bodega2@sistema.com", "0955555555", bodega, cuenca);

        // ===========================
        // 4. CATEGORÍAS
        // ===========================
        Categoria electronics = getOrCreateCategoria("Electrónica", "Dispositivos y accesorios electrónicos");
        Categoria ropa = getOrCreateCategoria("Ropa", "Vestimenta y calzado");
        Categoria alimentos = getOrCreateCategoria("Alimentos", "Productos de consumo masivo");
        Categoria hogar = getOrCreateCategoria("Hogar", "Artículos para el hogar");

        // ===========================
        // 5. PRODUCTOS
        // ===========================
        Producto p1 = getOrCreateProducto("Laptop HP 15\"", "Laptop HP Core i5 8GB RAM",
                new BigDecimal("850.00"), new BigDecimal("15.00"), electronics, "LAP-001");
        Producto p2 = getOrCreateProducto("Mouse Inalámbrico", "Mouse Logitech inalámbrico",
                new BigDecimal("25.00"), new BigDecimal("15.00"), electronics, "MOU-001");
        Producto p3 = getOrCreateProducto("Teclado Mecánico", "Teclado gamer RGB",
                new BigDecimal("65.00"), new BigDecimal("15.00"), electronics, "TEC-001");
        Producto p4 = getOrCreateProducto("Camiseta Polo", "Camiseta algodón 100%",
                new BigDecimal("18.50"), new BigDecimal("15.00"), ropa, "ROP-001");
        Producto p5 = getOrCreateProducto("Jeans Slim Fit", "Pantalón mezclilla",
                new BigDecimal("35.00"), new BigDecimal("15.00"), ropa, "ROP-002");
        Producto p6 = getOrCreateProducto("Arroz 2kg", "Arroz blanco premium",
                new BigDecimal("2.50"), BigDecimal.ZERO, alimentos, "ALI-001");
        Producto p7 = getOrCreateProducto("Aceite 1L", "Aceite vegetal",
                new BigDecimal("3.20"), new BigDecimal("15.00"), alimentos, "ALI-002");
        Producto p8 = getOrCreateProducto("Lámpara LED", "Lámpara de escritorio",
                new BigDecimal("22.00"), new BigDecimal("15.00"), hogar, "HOG-001");

        // ===========================
        // 6. INVENTARIO POR SUCURSAL (Con validación de duplicados)
        // ===========================
        createInventarioIfNotExists(p1, quito, 15, 5);
        createInventarioIfNotExists(p2, quito, 45, 10);
        createInventarioIfNotExists(p3, quito, 20, 8);
        createInventarioIfNotExists(p4, quito, 100, 20);
        createInventarioIfNotExists(p5, quito, 3, 10);

        createInventarioIfNotExists(p1, ambato, 8, 5);
        createInventarioIfNotExists(p6, ambato, 200, 50);
        createInventarioIfNotExists(p7, ambato, 150, 40);
        createInventarioIfNotExists(p8, ambato, 25, 10);

        createInventarioIfNotExists(p2, cuenca, 30, 10);
        createInventarioIfNotExists(p3, cuenca, 12, 8);
        createInventarioIfNotExists(p4, cuenca, 2, 15);
        createInventarioIfNotExists(p6, cuenca, 180, 50);

        // ===========================
        // 7. CLIENTES
        // ===========================
        createCliente("1712345678", "Juan", "Pérez", "0987654321", "juan.perez@email.com", "RUC");
        createCliente("1723456789", "María", "Gómez", "0987654322", "maria.gomez@email.com", "RUC");
        createCliente("1734567890", "Carlos", "López", "0987654323", "carlos.lopez@email.com", "RUC");
        createCliente("0000000000", "Consumidor", "Final", "", "", "CONSUMIDOR_FINAL");

        // ===========================
        // 8. VENTAS DE EJEMPLO (Solo si no hay ventas aún)
        // ===========================
        if (ventaRepo.count() == 0) {
            System.out.println("📦 Creando ventas de ejemplo...");
            Cliente cliente1 = clienteRepo.findByCedulaRuc("1712345678").orElse(null);
            Cliente clienteFinal = clienteRepo.findByCedulaRuc("0000000000").orElse(null);
            Usuario cajero1 = userRepo.findByUsername("cajero1").orElse(null);

            if (cliente1 != null && cajero1 != null) {
                createVenta(cliente1, cajero1, quito, p1, 1, new BigDecimal("850.00"));
                createVenta(clienteFinal, cajero1, quito, p2, 2, new BigDecimal("25.00"));
                createVenta(cliente1, cajero1, quito, p4, 3, new BigDecimal("18.50"));
            }
        } else {
            System.out.println("⚠️ Ya existen ventas en la BD - Skipping creación de ventas demo");
        }

        System.out.println("✅ Datos de prueba cargados exitosamente!");
        System.out.println("📊 Resumen:");
        System.out.println("   📦 Productos: " + productoRepo.count());
        System.out.println("   🏢 Sucursales: " + sucRepo.count());
        System.out.println("   👥 Usuarios: " + userRepo.count());
        System.out.println("   💰 Ventas: " + ventaRepo.count());
        System.out.println("   📋 Clientes: " + clienteRepo.count());
    }

    private Rol getOrCreateRol(String nombre) {
        Optional<Rol> existing = rolRepo.findByNombre(nombre);
        if (existing.isPresent()) {
            System.out.println("  ✓ Rol ya existe: " + nombre);
            return existing.get();
        }
        Rol r = new Rol(nombre);
        System.out.println("  ➕ Creando rol: " + nombre);
        return rolRepo.save(r);
    }

    private Sucursal getOrCreateSucursal(String codigo, String ciudad, String dir, String tel) {
        Optional<Sucursal> existing = sucRepo.findByCodigo(codigo);
        if (existing.isPresent()) {
            System.out.println("  ✓ Sucursal ya existe: " + ciudad);
            return existing.get();
        }
        Sucursal s = new Sucursal(codigo, ciudad);
        s.setDireccion(dir);
        s.setTelefono(tel);
        s.setActivo(true);
        System.out.println("  ➕ Creando sucursal: " + ciudad);
        return sucRepo.save(s);
    }

    private void createUsuario(String username, String pass, String nombre,
                               String email, String tel, Rol rol, Sucursal suc) {
        if (userRepo.existsByUsername(username)) {
            System.out.println("  ✓ Usuario ya existe: " + username);
            return;
        }
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setPassword(encoder.encode(pass));
        u.setNombreCompleto(nombre);
        u.setEmail(email);
        u.setTelefono(tel);
        u.setRol(rol);
        u.setSucursal(suc);
        u.setActivo(true);
        userRepo.save(u);
        System.out.println("  ✅ Usuario creado: " + username);
    }

    private Categoria getOrCreateCategoria(String nombre, String desc) {
        Categoria existing = categoriaRepo.findByNombre(nombre);

        if (existing != null) {
            System.out.println("  ✓ Categoría ya existe: " + nombre);
            return existing;
        }
        Categoria c = new Categoria();
        c.setNombre(nombre);
        c.setDescripcion(desc);
        c.setActivo(true);
        System.out.println("  ➕ Creando categoría: " + nombre);
        return categoriaRepo.save(c);
    }

    private Producto getOrCreateProducto(String nombre, String desc, BigDecimal precio,
                                         BigDecimal iva, Categoria cat, String codigo) {
        Producto existing = productoRepo.findByCodigoBarras(codigo);

        if (existing != null) {
            System.out.println("  ✓ Producto ya existe: " + nombre);
            return existing;
        }
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setDescripcion(desc);
        p.setPrecioUnitario(precio);
        p.setPorcentajeIva(iva);
        p.setCategoria(cat);
        p.setCodigoBarras(codigo);
        p.setActivo(true);
        System.out.println("  ➕ Creando producto: " + nombre);
        return productoRepo.save(p);
    }

    private void createInventarioIfNotExists(Producto producto, Sucursal sucursal, int stock, int stockMin) {
        // Buscar si ya existe este inventario para esta combinación producto+sucursal
        boolean exists = inventarioRepo.findAll().stream()
                .anyMatch(inv -> inv.getProducto().getIdProducto().equals(producto.getIdProducto())
                        && inv.getSucursal().getId().equals(sucursal.getId()));

        if (exists) {
            System.out.println("  ✓ Inventario ya existe: " + producto.getNombre() + " en " + sucursal.getCiudad());
            return;
        }

        Inventario inv = new Inventario();
        inv.setProducto(producto);
        inv.setSucursal(sucursal);
        inv.setStockActual(stock);
        inv.setStockMinimo(stockMin);
        inventarioRepo.save(inv);
        System.out.println(
                "  ➕ Inventario creado: " + producto.getNombre() + " (" + stock + ") en " + sucursal.getCiudad());
    }

    private void createCliente(String cedula, String nombres, String apellidos,
                               String tel, String email, String tipo) {
        if (clienteRepo.findByCedulaRuc(cedula).isPresent()) {
            System.out.println("  ✓ Cliente ya existe: " + cedula);
            return;
        }
        Cliente c = new Cliente();
        c.setCedulaRuc(cedula);
        c.setNombres(nombres);
        c.setApellidos(apellidos);
        c.setTelefono(tel);
        c.setEmail(email);
        c.setTipoCliente(tipo);
        c.setActivo(true);
        clienteRepo.save(c);
        System.out.println("  ✅ Cliente creado: " + nombres + " " + apellidos);
    }

    private void createVenta(Cliente cliente, Usuario vendedor, Sucursal suc,
                             Producto prod, int cantidad, BigDecimal precioUnit) {
        Venta v = new Venta();
        v.setCliente(cliente);
        v.setUsuario(vendedor);
        v.setSucursal(suc);
        v.setFecha(LocalDateTime.now());

        BigDecimal subtotal = precioUnit.multiply(BigDecimal.valueOf(cantidad));
        BigDecimal iva = subtotal.multiply(BigDecimal.valueOf(0.15));
        BigDecimal total = subtotal.add(iva);

        v.setSubtotalSinIva(subtotal);
        v.setSubtotalConIva(subtotal);
        v.setIvaTotal(iva);
        v.setTotal(total);
        v.setEstado("PAGADO");

        ventaRepo.save(v);

        // Detalle de venta
        DetalleVenta dv = new DetalleVenta();
        dv.setVenta(v);
        dv.setProducto(prod);
        dv.setCantidad(cantidad);
        dv.setPrecioUnitario(precioUnit);
        dv.setSubtotal(subtotal);

        System.out.println("  💰 Venta creada: #" + v.getId() + " - Total: $" + total);
    }
}