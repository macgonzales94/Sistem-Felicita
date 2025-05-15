package com.felicita.controladores;

import com.felicita.dto.JwtResponseDTO;
import com.felicita.dto.LoginDTO;
import com.felicita.dto.RegistroDTO;
import com.felicita.excepciones.UsuarioExcepcion;
import com.felicita.servicios.AutenticacionServicio;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    @PostMapping("/login")
    public String procesarLogin(@Valid @ModelAttribute("loginDTO") LoginDTO loginDTO,
                               BindingResult result,
                               RedirectAttributes redirectAttributes,
                               HttpServletResponse response) {
        
        if (result.hasErrors()) {
            return "auth/login";
        }
        
        try {
            // Obtener el token JWT
            JwtResponseDTO respuesta = autenticacionServicio.autenticar(loginDTO);
            
            // Almacenar el token JWT en una cookie
            Cookie jwtCookie = new Cookie("jwtToken", respuesta.getToken());
            jwtCookie.setPath("/");
            jwtCookie.setHttpOnly(true); // Para seguridad, no accesible via JavaScript
            jwtCookie.setMaxAge(86400); // 1 día en segundos
            response.addCookie(jwtCookie);
            
            // Redireccionar según el rol del usuario
            if (respuesta.getRoles().contains("ADMINISTRADOR")) {
                return "redirect:/admin/dashboard";
            } else if (respuesta.getRoles().contains("PROADMIN")) {
                return "redirect:/proadmin/dashboard";
            } else if (respuesta.getRoles().contains("CLIENTE")) {
                return "redirect:/cliente/inicio";
            } else {
                return "redirect:/";
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Credenciales incorrectas. Por favor intenta de nuevo.");
            return "redirect:/login";
        }
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
    
    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        // Eliminar la cookie JWT
        Cookie cookie = new Cookie("jwtToken", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        
        // Limpiar el contexto de seguridad
        SecurityContextHolder.clearContext();
        
        return "redirect:/login?logout";
    }
}