package com.empresa.sistema_ventas.controller;

import com.empresa.sistema_ventas.entity.Sucursal;
import com.empresa.sistema_ventas.repository.SucursalRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/sucursales")
public class SucursalViewController {

    private final SucursalRepository sucursalRepository;

    public SucursalViewController(SucursalRepository sucursalRepository) {
        this.sucursalRepository = sucursalRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("sucursales", sucursalRepository.findAll());
        return "admin/sucursales/lista";
    }

    @GetMapping("/nueva")
    public String nueva() {
        return "admin/sucursales/form";
    }

    @PostMapping("/guardar")
    public String guardar(
            @RequestParam String codigo,
            @RequestParam String ciudad,
            @RequestParam(required = false) String direccion,
            @RequestParam(required = false) String telefono,
            RedirectAttributes redirectAttributes
    ) {
        if (sucursalRepository.existsByCodigo(codigo)) {
            redirectAttributes.addFlashAttribute("error", "Ya existe una sucursal con ese código.");
            return "redirect:/admin/sucursales/nueva";
        }

        Sucursal sucursal = new Sucursal();
        sucursal.setCodigo(codigo.trim());
        sucursal.setCiudad(ciudad.trim());
        sucursal.setDireccion(direccion);
        sucursal.setTelefono(telefono);
        sucursal.setActivo(true);
        sucursalRepository.save(sucursal);

        redirectAttributes.addFlashAttribute("exito", "Sucursal creada correctamente.");
        return "redirect:/admin/sucursales";
    }
}
