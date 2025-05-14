package com.felicita.controladores;

import com.felicita.dto.LoginDTO;
import com.felicita.dto.RegistroDTO;
import com.felicita.excepciones.UsuarioExcepcion;
import com.felicita.servicios.AutenticacionServicio;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AutenticacionControladorThymeleaf {

    @Autowired
    private AutenticacionServicio autenticacionServicio;

    @GetMapping("/login")
    public String mostrarLogin(@RequestParam(value = "error", required = false) String error,
                              Model model) {
        if (error != null) {
            model.addAttribute("mensajeError", "Credenciales incorrectas. Por favor intenta de nuevo.");
        }
        model.addAttribute("loginDTO", new LoginDTO());
        return "auth/login";
    }

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("registroDTO", new RegistroDTO());
        return "auth/registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@Valid @ModelAttribute("registroDTO") RegistroDTO registroDTO,
                                  BindingResult result,
                                  RedirectAttributes redirectAttributes) {
        // Validar que las contraseñas coincidan
        if (!registroDTO.getPassword().equals(registroDTO.getConfirmarPassword())) {
            result.rejectValue("confirmarPassword", "error.registroDTO", "Las contraseñas no coinciden");
        }

        if (result.hasErrors()) {
            return "auth/registro";
        }

        try {
            autenticacionServicio.registrar(registroDTO);
            redirectAttributes.addFlashAttribute("mensaje", "Registro exitoso. Ahora puedes iniciar sesión.");
            return "redirect:/login";
        } catch (UsuarioExcepcion e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/registro";
        }
    }

    @GetMapping("/acceso-denegado")
    public String accesoDenegado() {
        return "error/403";
    }

    @GetMapping("/redirect-dashboard")
    public String redirigirDashboard(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))) {
            return "redirect:/admin/dashboard";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_PROADMIN"))) {
            return "redirect:/proadmin/dashboard";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CLIENTE"))) {
            return "redirect:/cliente/inicio";
        } else {
            return "redirect:/";
        }
    }
}