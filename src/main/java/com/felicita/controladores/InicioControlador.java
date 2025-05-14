package com.felicita.controladores;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InicioControlador {

    @GetMapping("/")
    public String inicio(Model model) {
        // Verificar si el usuario está autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAutenticado = auth != null && auth.isAuthenticated() && 
                              !auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ANONYMOUS"));
        
        model.addAttribute("isAutenticado", isAutenticado);
        
        // Si está autenticado, obtener el nombre y roles
        if (isAutenticado) {
            model.addAttribute("nombreUsuario", auth.getName());
            model.addAttribute("roles", auth.getAuthorities());
        }
        
        return "index";
    }
    
    @GetMapping("/acerca")
    public String acerca() {
        return "acerca";
    }
    
    @GetMapping("/contacto")
    public String contacto() {
        return "contacto";
    }
}