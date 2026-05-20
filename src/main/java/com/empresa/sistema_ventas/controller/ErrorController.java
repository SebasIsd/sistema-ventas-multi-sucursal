package com.empresa.sistema_ventas.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model, Authentication authentication) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            
            if (statusCode == HttpStatus.FORBIDDEN.value()) {
                model.addAttribute("errorCode", 403);
                model.addAttribute("errorMessage", "Acceso Denegado");
                model.addAttribute("errorDescription", "No tienes permisos para acceder a esta sección. Por favor contacta al administrador.");
                
                if (authentication != null && authentication.isAuthenticated()) {
                    model.addAttribute("username", authentication.getName());
                }
                
                return "error/403";
            } else if (statusCode == HttpStatus.NOT_FOUND.value()) {
                model.addAttribute("errorCode", 404);
                model.addAttribute("errorMessage", "Página No Encontrada");
                model.addAttribute("errorDescription", "La página que buscas no existe.");
                return "error/404";
            } else {
                model.addAttribute("errorCode", statusCode);
                model.addAttribute("errorMessage", "Error del Sistema");
                model.addAttribute("errorDescription", "Ocurrió un error inesperado. Por favor intenta de nuevo.");
                return "error/general";
            }
        }
        
        return "error/general";
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }
}
